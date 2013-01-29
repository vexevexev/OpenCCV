%Module
-module(open_ccv_server).

%Behaviour
-behaviour(gen_server).

%Start Functions
-export([start_link/1, listen_connections_loop/2, listen_messages_loop/3]).

%Gen_Server API
-export([
	init/1,
	handle_call/3,
	handle_cast/2,
	handle_info/2,
	terminate/2,
	code_change/3
]).
-export([cpx_msg_filter/1]).

-record(state, {
				sockets = [] :: [any()]
			   } 
).

%% mochi
-export([stop/0]).

%OpenACD
-include_lib("OpenACD/include/log.hrl").
-include_lib("OpenACD/include/call.hrl").
-include_lib("OpenACD/include/agent.hrl").
-include_lib("OpenACD/include/queue.hrl").
-include_lib("OpenACD/include/web.hrl").
-include_lib("OpenACD/include/cpx_cdr_pb.hrl").

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Gen_Server Stuff %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

start_link(Port) ->
    gen_server:start_link({local, open_ccv_server}, open_ccv_server, [Port], []).

init([Port]) ->
	start(Port),
	NewState = #state{},
    {ok, NewState}.

terminate(_,_) -> 
	ok.

code_change(_, _, State) ->
	{ok, State}.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Mochi-Web Stuff %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


start(Port) ->
	?INFO("Starting Mochiweb on port: ~p", [Port]),
    mochiweb_http:start([{name, open_ccv},
						 {port, Port},
                         {loop, fun(Req) ->
                            Path = Req:get(path),
							?INFO("Received Parsed Request:~p", [Path]),
							handle_request(Path, Req)
                        end}]),
%%     mochiweb_http:start([{port, 8384}, {loop, fun(Req) -> Req:respond({200, [{"Content-Type", "text/html"}], <<"undefined">>}) end}]).

	case gen_tcp:listen(Port+1, [list, {packet, 0}, {active, false}]) of
		{ok, ListenSocket} ->
			spawn(?MODULE, listen_connections_loop, [self(), ListenSocket]);
		_ ->
			stop
	end,
	cpx_monitor:subscribe(fun cpx_msg_filter/1).

stop() ->
    mochiweb_http:stop().

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%% OpenCCV Stuff %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

listen_connections_loop(ServerPID, ListenSocket) ->
	case gen_tcp:accept(ListenSocket) of
		{ok, Connection} ->
			gen_server:call(ServerPID, {new_connection, Connection}),
			spawn(?MODULE, listen_messages_loop, [ServerPID, Connection, ""]);
		_ ->
			stop
	end,
	listen_connections_loop(ServerPID, ListenSocket).

listen_messages_loop (ServerPID, Connection, CurrentMessage) ->
	{ok, Packet} = gen_tcp:recv(Connection, 1),
	case Packet of
		"\n" ->
			gen_server:cast(ServerPID, {handle_packet, CurrentMessage, Connection}),
			listen_messages_loop(ServerPID, Connection, "");
		NewChar ->
			listen_messages_loop(ServerPID, Connection, CurrentMessage++NewChar)
	end.

handle_cast({handle_packet, "get_avail_agents", Connection}, State) ->
	gen_tcp:send(Connection, get_agent_list_JSON() ++"\n"),
	{noreply, State};

handle_cast({handle_packet, "get_queued_calls", Connection}, State) ->
	gen_tcp:send(Connection, get_queued_calls_JSON() ++"\n"),
	{noreply, State};

handle_cast({handle_packet, Packet, Connection}, State) ->
	?INFO("OpenCCV received unhandled message [~p] from connection [~p]", [Packet, Connection]),
    {noreply, State}.

handle_call({new_connection, Connection}, _From, State) ->
	?INFO("New OpenCCV connection received: [~p]", [Connection]),
	gen_tcp:send(Connection, get_agent_list_JSON() ++"\n" ++ get_queued_calls_JSON() ++ "\n"),
	{reply, ok, #state{sockets = State#state.sockets ++ [Connection]}}.

send(_String, []) ->
	[];
	
send(String, [Head | SocketsLeft]) ->
	try
		gen_tcp:send(Head, String++"\n"),
		[Head] ++ send(String, SocketsLeft)
	catch
		_:_ ->
			send(String, SocketsLeft)
	end.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% REST API %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

handle_request("", Req) ->
	handle_request("/OpenCCV.jnlp", Req);

handle_request("/", Req) ->
	handle_request("/OpenCCV.jnlp", Req);

handle_request("/OpenCCV.jnlp", Req) ->
	{file, Here} = code:is_loaded(?MODULE),
	OpenCCV_JNLP_File = filename:join([filename:dirname(filename:dirname(Here)), "priv", "www", "OpenCCV.jnlp"]),
	{ok, IoDevice} = file:open(OpenCCV_JNLP_File, [raw, binary]),
	Req:respond({200, [{"Content-Type", "application/x-java-jnlp-file"}], {file, IoDevice}});

handle_request("/OpenCCV.jar", Req) ->
	{file, Here} = code:is_loaded(?MODULE),
	OpenCCV_JNLP_File = filename:join([filename:dirname(filename:dirname(Here)), "priv", "www", "OpenCCV.jar"]),
	{ok, IoDevice} = file:open(OpenCCV_JNLP_File, [raw, binary]),
	Req:respond({200, [{"Content-Type", "application/java-archive"}], {file, IoDevice}});


handle_request("/resources" ++ Rest, Req) ->
	Path = "/resources" ++ Rest,
	try
		case string:str(Path, "..") of
			0 -> 
				{file, Here} = code:is_loaded(?MODULE),
				File = filename:join([filename:dirname(filename:dirname(Here)), "priv", "www"]) ++ Path,
				{ok, IoDevice} = file:open(File, [raw, binary]),
				Req:respond({200, [{"Content-Type", "application/java-archive"}], {file, IoDevice}});
			_N -> 
				Req:respond({404, [{"Content-Type", "text/html"}], <<"Not Found">>})
		end		
	catch
		_:_ -> Req:respond({404, [{"Content-Type", "text/html"}], <<"Not Found">>})
	end.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%% cpx_monitor stuff %%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

cpx_msg_filter({info, _, {agent_state, _}}) ->
	true;
cpx_msg_filter({info, _, {agent_profile, _}}) ->
	true;
cpx_msg_filter({info, _, {cdr_raw, _}}) ->
	true;
cpx_msg_filter(_M) ->
	%?DEBUG("filtering out message ~p", [M]),
	false.

handle_info({cpx_monitor_event, {info, _Time, {agent_state, Astate}}}, State) ->
	StateData = case Astate#agent_state.oldstate of
		login ->
				[skill_to_json(S) || S <- Astate#agent_state.statedata];
		idle ->
				[];
		precall ->
				client_to_json(Astate#agent_state.statedata);
		released ->
				release_to_json(Astate#agent_state.statedata);
		_ when is_record(Astate#agent_state.statedata, call) ->
				call_to_json(Astate#agent_state.statedata);
		_ ->
				erlang:list_to_atom(term_to_string(Astate#agent_state.statedata))
	end,
	UnencodedJSON = 
		[{agent_state_change, [
		{id, erlang:list_to_atom(Astate#agent_state.id)},
		{agent, erlang:list_to_atom(Astate#agent_state.agent)},
		{state, Astate#agent_state.state},
		{oldstate, Astate#agent_state.oldstate},
		{statedata, StateData},
		{start, to_atom(Astate#agent_state.start)},
		{ended, to_atom(Astate#agent_state.ended)},
		{profile, to_atom(Astate#agent_state.profile)},
		{timestamp, to_atom(Astate#agent_state.timestamp)},
		{nodes, Astate#agent_state.nodes}]}],
	NewSockets = send(mochijson2:encode(UnencodedJSON), State#state.sockets),
	?INFO("Debug (agent_state): ~p", [term_to_string(UnencodedJSON)]),
	{noreply, #state{sockets = NewSockets}};

handle_info({cpx_monitor_event, {info, _Time, {cdr_raw, CdrRaw}}}, State) ->
	Terminates = case CdrRaw#cdr_raw.terminates of
		infoevent ->
			'INFOEVENT';
		_ ->
			[cdr_transaction_to_enum(X) || X <- CdrRaw#cdr_raw.terminates]
	end,
	AdditionalData = case CdrRaw#cdr_raw.transaction of
		cdrinit -> 
			[{call, call_to_json(CdrRaw#cdr_raw.eventdata)}];
		inivr -> 
			[{dnis, to_atom(CdrRaw#cdr_raw.eventdata)}];
		dialoutgoing -> 
			[{number_dialed, to_atom(CdrRaw#cdr_raw.eventdata)}];
		inqueue -> 
			[{queue, to_atom(CdrRaw#cdr_raw.eventdata)}];
		ringing -> 
			[{agent, to_atom(CdrRaw#cdr_raw.eventdata)}];
		ringout -> 
			{RingoutReason, RingoutAgent} = CdrRaw#cdr_raw.eventdata,
			[{agent, to_atom(RingoutAgent)}, {ringout_reason, to_atom(RingoutReason)}];
		precall -> 
			[{client, to_atom(CdrRaw#cdr_raw.eventdata)}];
		oncall -> 
			[{agent, to_atom(CdrRaw#cdr_raw.eventdata)}];
		agent_transfer -> 
			[{agent, to_atom(element(1, CdrRaw#cdr_raw.eventdata))},
			{agent_transfer_recipient, to_atom(element(2, CdrRaw#cdr_raw.eventdata))}];
		queue_transfer -> 
			[{queue, to_atom(CdrRaw#cdr_raw.eventdata)}];
		transfer -> 
			[{transfer_to, to_atom(CdrRaw#cdr_raw.eventdata)}];
		warmxfer_begin -> 
			[{transfer_to, to_atom(element(2, CdrRaw#cdr_raw.eventdata))},
			{agent, to_atom(element(1, CdrRaw#cdr_raw.eventdata))}];
		warmxfer_cancel -> 
			[{agent, to_atom(element(1, CdrRaw#cdr_raw.eventdata))}];
		warmxfer_fail -> 
			[{agent, to_atom(CdrRaw#cdr_raw.eventdata)}];
		warmxfer_complete -> 
			[{agent, to_atom(CdrRaw#cdr_raw.eventdata)}];
		wrapup -> 
			[{agent, to_atom(CdrRaw#cdr_raw.eventdata)}];
		endwrapup -> 
			[{agent, to_atom(CdrRaw#cdr_raw.eventdata)}];
		abandonqueue -> 
			[{queue, to_atom(CdrRaw#cdr_raw.eventdata)}];
		abandonivr -> 
			[];
		voicemail -> 
			[{queue, to_atom(CdrRaw#cdr_raw.eventdata)}];
		hangup -> 
			[{hangup_by, to_atom(CdrRaw#cdr_raw.eventdata)}];
		{media_custom, CustomName} ->
			[{media_custom_name, to_atom(CustomName)},
		  	{media_custom_terminated, [to_atom(Name) || {_Cust, Name} <- CdrRaw#cdr_raw.terminates]}];
		undefined -> [];
		cdrend -> [];
		_ -> []
	end,
	UnencodedJSON = 
		[{cdr_raw, [
			{call_id, erlang:list_to_atom(CdrRaw#cdr_raw.id)},
			{transaction, cdr_transaction_to_enum(CdrRaw#cdr_raw.transaction)},
			{eventdata, erlang:list_to_atom(term_to_string(CdrRaw#cdr_raw.eventdata))},
			{start, erlang:list_to_atom(erlang:integer_to_list(CdrRaw#cdr_raw.start))},
			{ended, to_atom(CdrRaw#cdr_raw.ended)},
			{terminates, Terminates},
			{timestamp, erlang:list_to_atom(erlang:integer_to_list(CdrRaw#cdr_raw.timestamp))},
			{nodes, CdrRaw#cdr_raw.nodes}] ++
			AdditionalData
		}],
	?INFO("Debug (cdr_raw): ~p", [term_to_string(UnencodedJSON)]),
	NewSockets = send(mochijson2:encode(UnencodedJSON), State#state.sockets),
	{noreply, #state{sockets = NewSockets}};

handle_info({cpx_monitor_event, {info, _Time, {agent_profile, AProf}}}, State) ->
	UnencodedJSON = 
		[{agent_profile_change, [
			{agent_id, to_atom(AProf#agent_profile_change.id)},
			{agent_login , to_atom(AProf#agent_profile_change.agent)},
			{old_profile, to_atom(AProf#agent_profile_change.old_profile)},
			{new_profile, to_atom(AProf#agent_profile_change.new_profile)},
			{skills, [skill_to_json(S) || S <- AProf#agent_profile_change.skills]},
			{dropped_skills, [skill_to_json(S) || S <- AProf#agent_profile_change.dropped_skills]},
			{gained_skills, [skill_to_json(S) || S <- AProf#agent_profile_change.gained_skills]}
		]}],
	?INFO("Debug (agent_profile): ~p", [term_to_string(UnencodedJSON)]),
	NewSockets = send(mochijson2:encode(UnencodedJSON), State#state.sockets),
	{noreply, #state{sockets = NewSockets}};

handle_info(Msg, State) ->
	?INFO("unhandled message ~p", [Msg]),
	{noreply, State}.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%% Formatting/JSON stuff %%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

get_queued_calls_JSON() ->
	CallQueueRecordList = call_queue_config:get_queues(),
	CallQueueNameList = [CallQueue#call_queue.name || CallQueue <- CallQueueRecordList],
	QueuedCallsRecords = [{{queue_name, Q}, call_queue:get_calls(queue_manager:get_queue(Q))} || Q <- CallQueueNameList],
	QueuedCallsTupleList = [[MediaPIDTuple, Name, ID, Skills] || {MediaPIDTuple, Name, ID, Skills} <- lists:flatten(
						[[{
						   {media_pid, QueuedCall#queued_call.media},
						   {queue_name, to_atom(QueueName)},
						   {id, to_atom(QueuedCall#queued_call.id)},
						   {skills, [skill_to_json(Skill) || Skill <- QueuedCall#queued_call.skills]}} || {_Key, QueuedCall} <- QueuedCalls]
						|| {{queue_name, QueueName}, QueuedCalls} <- QueuedCallsRecords])],
	
	QueuedCallsRecordList = [ queued_call_to_json(gen_media:get_call(MediaPID), QueueNameTuple) ||  [{media_pid, MediaPID}, QueueNameTuple, _ID, _Skills] <- QueuedCallsTupleList], 
	
	mochijson2:encode([{queued_calls, QueuedCallsRecordList}]).

get_agent_list_JSON() ->
	AvailabilityList = agent_manager:list(),
	NameList = [AgentName || {AgentName, _} <- AvailabilityList],
	AgentRecords = [agent:dump_state(cpx:get_agent(Name)) || Name <- NameList],
	UnencodedJSON = [{agent_list, [agent_record_to_json(Record) || Record <- AgentRecords]}],
	mochijson2:encode(UnencodedJSON).

agent_record_to_json(AgentRecord) ->
	StateData = case AgentRecord#agent.state of
		ringing ->
			call_to_json(AgentRecord#agent.statedata);
		oncall ->
			call_to_json(AgentRecord#agent.statedata);
		outgoing ->
			call_to_json(AgentRecord#agent.statedata);
		wrapup ->
			call_to_json(AgentRecord#agent.statedata);
		released ->
			[{release_data, release_to_json(AgentRecord#agent.statedata)}];
		warmtransfer ->
			{onhold, Call, calling, _SomeString} = AgentRecord#agent.statedata,
			call_to_json(Call);
		_ ->
			[]
	end,
	
	[{login, to_atom(AgentRecord#agent.login)},
	 {id, to_atom(AgentRecord#agent.id)},
	 {skills, [skill_to_json(S) || S <- AgentRecord#agent.skills]},
	 {profile, to_atom(AgentRecord#agent.profile)},
	 {state, to_atom(AgentRecord#agent.state)}]
	 ++ StateData.
			  

skill_to_json({Atom, Expanded}) when is_list(Expanded) ->
	[Atom, erlang:list_to_atom(Expanded)];
skill_to_json({Atom, Expanded}) ->
	[Atom, erlang:list_to_atom(term_to_string(Expanded))];
skill_to_json(Atom) when is_atom(Atom) ->
	Atom.

client_to_json(Client) ->
  	IsDefault = case Client#client.id of undefined -> true; _ -> false end,
	Label = case erlang:is_list(Client#client.label) of true -> erlang:list_to_atom(Client#client.label); _ -> Client#client.label end,
	ID = case erlang:is_list(Client#client.id) of true -> erlang:list_to_atom(Client#client.id); _ -> Client#client.id end, 
	[[is_default, IsDefault], [name, Label], [id, ID]].

release_to_json(default) ->
	[{id, default}, {label, default}, {bias, '0'}];
release_to_json(R) when is_record(R, release_opt) ->
	release_to_json({R#release_opt.id, R#release_opt.label, R#release_opt.bias});
release_to_json({Id, RawLabel, Bias}) ->
	[{id, to_atom(Id)}, {label, to_atom(RawLabel)}, {bias, to_atom(Bias)}].

call_to_json(Call) ->
	[{ call, [
		{id, to_atom(Call#call.id)},
		{type, to_atom(Call#call.type)},
		{caller_id, [
			{name, to_atom(element(1, Call#call.callerid))},
			{data, to_atom(element(2, Call#call.callerid))}]},
		{dnis, to_atom(Call#call.dnis)},
		{client, client_to_json(Call#call.client)},
		{ring_path, to_atom(Call#call.ring_path)},
		{media_path, to_atom(Call#call.media_path)},
		{direction, to_atom(Call#call.direction)},
		{node, to_atom(node(Call#call.source))},
		{priority, to_atom(Call#call.priority)}
	]}].

queued_call_to_json(Call, QueueTuple) ->
	[{ call, [
		{id, to_atom(Call#call.id)},
		{type, to_atom(Call#call.type)},
		{caller_id, [
			{name, to_atom(element(1, Call#call.callerid))},
			{data, to_atom(element(2, Call#call.callerid))}]},
		{dnis, to_atom(Call#call.dnis)},
		{client, client_to_json(Call#call.client)},
		{ring_path, to_atom(Call#call.ring_path)},
		{media_path, to_atom(Call#call.media_path)},
		{direction, to_atom(Call#call.direction)},
		{node, to_atom(node(Call#call.source))},
		{priority, to_atom(Call#call.priority)},
		QueueTuple
	]}].

cdr_transaction_to_enum({media_custom, _}) ->
	cdr_transaction_to_enum(media_custom);
cdr_transaction_to_enum(Transaction) ->
	list_to_atom(string:to_upper(atom_to_list(Transaction))).

to_atom(Term) ->
	case io_lib:printable_list(Term) of
		true ->
			erlang:list_to_atom(Term);
		_ ->
			to_atom2(Term)
	end.
to_atom2(Term) when erlang:is_atom(Term) ->
	Term;
to_atom2(Term) when erlang:is_integer(Term) ->
	erlang:list_to_atom(erlang:integer_to_list(Term));
to_atom2(Term) when erlang:is_pid(Term) ->
	erlang:list_to_atom(erlang:pid_to_list(Term));
to_atom2(Term) ->
	erlang:list_to_atom(term_to_string(Term)).

term_to_string(Term)->
	re:replace(lists:flatten(io_lib:format("~1000000p", [Term])), "\\n ", "", [global, {return, list}]).