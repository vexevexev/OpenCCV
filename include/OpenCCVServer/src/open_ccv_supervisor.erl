-module(open_ccv_supervisor).

-behaviour(supervisor).

-export([start_link/0]).
-export([init/1]).

start_link() ->
    supervisor:start_link(open_ccv_supervisor, []).

init(_Args) ->
    {ok, {{one_for_one, 1, 60},
          [{open_ccv, {open_ccv_server, start_link, [3838]},
            permanent, brutal_kill, worker, [open_ccv]}]}}.