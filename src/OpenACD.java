

import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import JSON.JSONObject;


public class OpenACD 
{

	//public static boolean DEBUGGING = false;
	public static boolean DEBUGGING = true;
	
	private final Lock lock = new ReentrantLock();
	public Agent[] agents = new Agent[0];
	public Call[] calls = new Call[0];
	public StatsPanel stats = new StatsPanel();
	
	public int getAgentIndexByID(String id, Agent[] agents)
	{
		for(int i = 0; i < agents.length; i++)
			if(agents[i].id.equals(id))
				return i;
		return -1;
	}
	
	public Agent getAgentByID(String id)
	{
		for(int i = 0; i < agents.length; i++)
			if(agents[i].id.equals(id))
				return agents[i];
		return null;
	}

	private Agent getAgentByLogin(String login) 
	{
		for(int i = 0; i < agents.length; i++)
			if(agents[i].login.equals(login))
				return agents[i];
		return null;
	}
	
	public Call getCallByID(String id)
	{
		for(int i = 0; i < calls.length; i++)
			if(calls[i].id.equals(id))
				return calls[i];
		return null;
	}
	
	private void addAgent(Agent newAgent)
	{
		Agent redundantAgent = getAgentByID(newAgent.id);
		if(redundantAgent != null && redundantAgent.ending)
		{
			redundantAgent.ending = false;
			redundantAgent.targetX = GraphicalMain.RENDER_WIDTH-Agent.agentGridLength-Agent.padding;
			redundantAgent.spring.springConstant *= 2.0;
			return;
		}
			
		if(GraphicalMain.self.autofoldAgents.getState())
			newAgent.collapsed = true;
		
		Agent[] newAgentArray = new Agent[agents.length+1];
		for(int i = 0; i < agents.length; i++)
			newAgentArray[i] = agents[i];
		newAgentArray[agents.length] = newAgent;
		
		agents = newAgentArray;
		
		sortAgents();
		loadAgentTargetPositions();
		
		newAgent.currentY = newAgent.targetY;
		newAgent.currentX = GraphicalMain.RENDER_WIDTH;
		
		newAgent.targetX = GraphicalMain.RENDER_WIDTH-Agent.agentGridLength-Agent.padding;
		System.out.println(GraphicalMain.RENDER_WIDTH+"\t"+Agent.agentGridLength+"\t"+Agent.padding+"\t"+newAgent.targetX);
	}
	
	public void removeAgent(String id)
	{
		if(getAgentByID(id) == null)
			return;
		
		Agent[] newAgents = new Agent[agents.length-1];
		
		int j = 0;
		for(int i = 0; i < agents.length; i++)
			if(!agents[i].id.equalsIgnoreCase(id))
			{
				newAgents[j] = agents[i];
				j++;
			}
		
		agents = newAgents;
	}
	
	private void endAgent(String id)
	{
		Agent agent = getAgentByID(id);
		if(agent!=null)
			agent.end();
	}
	
	public void sortAgents()
	{
		agents = this.recursiveSublistSort(agents, 0);
	}
	
	public void loadAgentTargetPositions()
	{
		int agentOffset = (int) +Agent.padding*2;
		
		//Set agent target positions
		for(int i = 0; i < agents.length; i++)
		{
			if(agents[i] == null) System.out.println(i+" "+agents.length);
			agents[i].targetY = -agentOffset;

			if(!agents[i].ending)
				agents[i].targetX = GraphicalMain.RENDER_WIDTH-Agent.agentGridLength-Agent.padding;
			
			if(!agents[i].collapsed)
				agentOffset += (GraphicalMain.fontSize + Agent.padding*3)+.5;
			else if(i < agents.length-1 && agents[i+1].collapsed)
				agentOffset += agents[i].collapsedHeight+GraphicalMain.fontSize/8+.5;
			else
				agentOffset += agents[i].collapsedHeight+GraphicalMain.fontSize/2+.5;
		}
	}
	
	private Agent[] recursiveSublistSort(Agent[] sublist, int sortField)
	{
		
		if(sortField == Agent.agentSortingOrder.length)
			return sublist;
		else
		{
			Agent.currentSortField = Agent.agentSortingOrder[sortField];
			Arrays.sort(sublist);
			Agent[][] splitSublist = splitByField(sublist);
			
			Agent[] newSublist = new Agent[0];
			
			for(int i = 0; i < splitSublist.length; i++)
				newSublist = concatAgentLists(newSublist, recursiveSublistSort(splitSublist[i], sortField+1));
			
			return newSublist;
		}
	}
	
	private Agent[] concatAgentLists(Agent[] firstPart, Agent[] secondPart)
	{
		Agent[] concatination = new Agent[firstPart.length+secondPart.length];
		for(int i = 0; i < firstPart.length; i++)
			concatination[i] = firstPart[i];
		for(int i = 0; i < secondPart.length; i++)
			concatination[firstPart.length+i] = secondPart[i];
		return concatination;
	}
	
	private Agent[][] splitByField(Agent[] sublist)
	{
		if(sublist.length == 0)
			return new Agent[0][0];
		
		Agent[][] splitSublist = new Agent[1][1];
		splitSublist[0][0] = sublist[0];
		int currentSublist = 0;
		for(int i = 1; i < sublist.length; i++)
		{
			if(splitSublist[currentSublist][splitSublist[currentSublist].length-1].compareTo(sublist[i]) != 0)
			{
				Agent[][] newSplitSublist = new Agent[splitSublist.length+1][];
				for(int j = 0; j < splitSublist.length; j++)
				{
					newSplitSublist[j] = splitSublist[j];
				}
				newSplitSublist[newSplitSublist.length-1] = new Agent[1];
				newSplitSublist[newSplitSublist.length-1][0] = sublist[i];
				splitSublist = newSplitSublist;
				currentSublist++;
			}
			else
			{
				Agent[] tempSublist = splitSublist[currentSublist];
				Agent[] newSublist = new Agent[tempSublist.length+1];
				for(int j = 0; j < tempSublist.length; j++)
					newSublist[j] = tempSublist[j];
				newSublist[newSublist.length-1] = sublist[i];
				splitSublist[currentSublist] = newSublist;
			}
		}
		return splitSublist;
	}
	
	private void addCall(Call newCall)
	{
		if(GraphicalMain.self.autofoldCalls.getState())
			newCall.collapsed = true;
		
		Call[] newCalls = new Call[calls.length+1];
		for(int i = 0; i < calls.length; i++)
			newCalls[i] = calls[i];
		newCalls[calls.length] = newCall;
		
		calls = newCalls;

		sortCalls();
		loadCallTargetPositions();
		
		newCall.currentY = newCall.targetY;
		newCall.currentX = -Call.callGridLength*1.25f;
		
		newCall.targetX = GraphicalMain.fontSize/2;
	}
	
	public void removeCall(String call_id)
	{
		if(getCallByID(call_id) == null)
			return;
		
		Call call = getCallByID(call_id);
		Call[] newCalls = new Call[calls.length-1];
		
		int j = 0;
		for(int i = 0; i < calls.length; i++)
			if(!calls[i].id.equalsIgnoreCase(call_id))
			{
				newCalls[j] = calls[i];
				j++;
			}
		
		calls = newCalls;
		if(call.agent != null && call.agent.call != null)
			call.agent.call = null;
		
		loadCallTargetPositions();
	}
	
	boolean endCalled = false;
	
	public void endCall(String call_id)
	{	
		if(getCallByID(call_id) == null)
			return;
		
		endCalled = true;
		
		Call call = getCallByID(call_id);
		call.end();
	}
	
	public void sortCalls()
	{
		calls = this.recursiveSublistSort(calls, 0);
	}

	public void loadCallTargetPositions()
	{
		int callOffset = (int) (Call.padding*2);
		
		//Set call target positions
		for(int i = 0; i < calls.length; i++)
		{
			calls[i].targetY = -callOffset+GraphicalMain.RENDER_HEIGHT;
			
			if(!calls[i].collapsed)
				callOffset += (GraphicalMain.fontSize + Call.padding*3) + .5;
			else if(i < calls.length-1 && calls[i+1].collapsed)
				callOffset += calls[i].collapsedHeight+GraphicalMain.fontSize/8 + .5;
			else
				callOffset += calls[i].collapsedHeight+GraphicalMain.fontSize/2 + .5;
		}
	}

	private Call[] recursiveSublistSort(Call[] sublist, int sortField)
	{
		if(sortField == Call.callSortingOrder.length)
			return sublist;
		else
		{
			Call.currentSortField = Call.callSortingOrder[sortField];
			Arrays.sort(sublist);
			Call[][] splitSublist = splitByField(sublist);
			
			Call[] newSublist = new Call[0];
			
			for(int i = 0; i < splitSublist.length; i++)
				newSublist = concatCallLists(newSublist, recursiveSublistSort(splitSublist[i], sortField+1));
			
			return newSublist;
		}
	}
	
	private Call[] concatCallLists(Call[] firstPart, Call[] secondPart)
	{
		Call[] concatination = new Call[firstPart.length+secondPart.length];
		for(int i = 0; i < firstPart.length; i++)
			concatination[i] = firstPart[i];
		for(int i = 0; i < secondPart.length; i++)
			concatination[firstPart.length+i] = secondPart[i];
		return concatination;
	}
	
	private Call[][] splitByField(Call[] sublist)
	{
		if(sublist.length == 0)
			return new Call[0][0];
		
		Call[][] splitSublist = new Call[1][1];
		splitSublist[0][0] = sublist[0];
		int currentSublist = 0;
		for(int i = 1; i < sublist.length; i++)
		{
			if(splitSublist[currentSublist][splitSublist[currentSublist].length-1].compareTo(sublist[i]) != 0)
			{
				Call[][] newSplitSublist = new Call[splitSublist.length+1][];
				for(int j = 0; j < splitSublist.length; j++)
				{
					newSplitSublist[j] = splitSublist[j];
				}
				newSplitSublist[newSplitSublist.length-1] = new Call[1];
				newSplitSublist[newSplitSublist.length-1][0] = sublist[i];
				splitSublist = newSplitSublist;
				currentSublist++;
			}
			else
			{
				Call[] tempSublist = splitSublist[currentSublist];
				Call[] newSublist = new Call[tempSublist.length+1];
				for(int j = 0; j < tempSublist.length; j++)
					newSublist[j] = tempSublist[j];
				newSublist[newSublist.length-1] = sublist[i];
				splitSublist[currentSublist] = newSublist;
			}
		}
		return splitSublist;
	}
	
	public synchronized void handle_login(String agent_id, String agent, String state,
			String oldstate, String start, String end, String profile,
			String timestamp, String[] nodes, String[][] skills) 
	{
		if(DEBUGGING)
			System.out.println("1 - \thandle_login");
		
		Agent newAgent = new Agent(agent_id, agent, profile, nodes, skills);

		lock();
		this.addAgent(newAgent);
		this.sortAgents();
		unlock();
	}

	public synchronized void handle_idle(String agent_id, String agent, String state,
			String oldstate, String start, String end, String profile,
			String timestamp, String[] nodes) 
	{	
		if(DEBUGGING)
			System.out.println("2 - \thandle_idle");
	
		Agent idleAgent = getAgentByID(agent_id);
		lock();
		if(!state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
			idleAgent.setState(state);
		else if(state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
			idleAgent.setState(oldstate);

		this.sortAgents();		
		unlock();
	}

	public synchronized void handle_precall(String agent_id, String agent, String state,
			String oldstate, String start, String end, String profile,
			String timestamp, String[] nodes, boolean is_defualt_client,
			String client_label, String client_id) 
	{
		if(DEBUGGING)
			System.out.println("3 - \thandle_precall");
		
		Agent precallAgent = getAgentByID(agent_id);
		lock();
		precallAgent.setState(Agent.PRECALL);
		this.sortAgents();
		unlock();
	}

	public synchronized void handle_released(String agent_id, String agent, String state,
			String oldstate, String start, String end, String profile,
			String timestamp, String[] nodes, String release_id,
			String release_label, int release_bias) 
	{
		if(DEBUGGING)
			System.out.println("4 - \thandle_released");
		
		Agent releasedAgent = getAgentByID(agent_id);
		lock();
		if(releasedAgent != null && state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
		{
			if(oldstate.equalsIgnoreCase("released"))
				releasedAgent.setReleased(release_id, release_label, release_bias);
			else
				releasedAgent.setState(oldstate);

			this.sortAgents();
		}
		unlock();
	}

	@SuppressWarnings("deprecation")
	public synchronized void handle_call(String agent_id, String agent, String state,
			String oldstate, String start, String end, String profile,
			String timestamp, String[] nodes, String call_id_number,
			String call_type, String caller_id_name, String caller_id_data,
			String call_dnis, boolean call_is_defualt_client,
			String call_client_label, String call_client_id,
			String call_ring_path, String call_media_path,
			String call_direction, String call_node, String call_priority) 
	{
		if(DEBUGGING)
			System.out.println("5 - \thandle_call");
		
		
		lock();

		Agent oncallAgent = getAgentByID(agent_id);
		Call call = getCallByID(call_id_number);
		
		if(call != null)
		{
			call.id = call_id_number;
			call.type = call_type;
			call.caller_id_name = java.net.URLDecoder.decode(caller_id_name);
			call.caller_id_data = java.net.URLDecoder.decode(caller_id_data);
			call.call_dnis = call_dnis;
			call.is_default_client = call_is_defualt_client;
			call.client_label = call_client_label;
			call.client_id = call_client_id;
			call.ring_path = call_ring_path;
			call.media_path = call_media_path;
			call.direction = call_direction;
			call.nodes = new String[]{call_node};
			call.priority = call_priority;
			
			oncallAgent.call = call;

			if(oncallAgent.state!=Agent.WRAPUP)
				//The agent has transferred this call to another agent and is wrapping up.
				//Do not set the agent for this call back to the old one in this case.
				call.agent = oncallAgent;
	
			if(!state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
				call.agent.setState(state);
			else if(state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
				call.agent.setState(oldstate);
			
			this.sortAgents();
			this.sortCalls();
		}
		else
		{
			call = new Call();
			
			call.id = call_id_number;
			call.type = call_type;
			call.caller_id_name = java.net.URLDecoder.decode(caller_id_name);
			call.caller_id_data = java.net.URLDecoder.decode(caller_id_data);
			call.call_dnis = call_dnis;
			call.is_default_client = call_is_defualt_client;
			call.client_label = call_client_label;
			call.client_id = call_client_id;
			call.ring_path = call_ring_path;
			call.media_path = call_media_path;
			call.direction = call_direction;
			call.nodes = new String[]{call_node};
			call.priority = call_priority;
			
			oncallAgent.call = call;
			call.agent = oncallAgent;

			if(!state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
				call.agent.setState(state);
			else if(state.equalsIgnoreCase("undefined") && end.equalsIgnoreCase("undefined"))
				call.agent.setState(oldstate);
			
			if(end.equalsIgnoreCase("undefined"))
			{
				this.addCall(call);
			}
			this.sortAgents();
			this.sortCalls();
		}
		
		unlock();
	}

	@SuppressWarnings("deprecation")
	public synchronized void handle_cdrinit(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String call_id_number, String call_type,
			String caller_id_name, String caller_id_data,
			String call_dnis, boolean call_is_defualt_client,
			String call_client_label, String call_client_id,
			String call_ring_path, String call_media_path,
			String call_direction, String call_node, String call_priority) 
	{
		if(DEBUGGING)
			System.out.println("6 - \thandle_cdrinit");
		
		if(!ended.equalsIgnoreCase("undefined"))
			return;

		lock();
		Call call = getCallByID(call_id);
		unlock();
		
		if(call == null)
		{
			call = new Call();

			call.setState(Call.LIMBO);
			
			call.id = call_id_number;
			call.type = call_type;
			call.caller_id_name = java.net.URLDecoder.decode(caller_id_name);
			call.caller_id_data = java.net.URLDecoder.decode(caller_id_data);
			call.call_dnis = call_dnis;
			call.is_default_client = call_is_defualt_client;
			call.client_label = call_client_label;
			call.client_id = call_client_id;
			call.ring_path = call_ring_path;
			call.media_path = call_media_path;
			call.direction = call_direction;
			call.nodes = new String[]{call_node};
			call.priority = call_priority;
		
			if(ended.equalsIgnoreCase("undefined"))
			{
				lock();
				this.addCall(call);
				this.sortCalls();
				unlock();
			}
		}
		else
		{
			lock();

			call.id = call_id_number;
			call.type = call_type;
			call.caller_id_name = java.net.URLDecoder.decode(caller_id_name);
			call.caller_id_data = java.net.URLDecoder.decode(caller_id_data);
			call.call_dnis = call_dnis;
			call.is_default_client = call_is_defualt_client;
			call.client_label = call_client_label;
			call.client_id = call_client_id;
			call.ring_path = call_ring_path;
			call.media_path = call_media_path;
			call.direction = call_direction;
			call.nodes = new String[]{call_node};
			call.lastChange = System.nanoTime();
			call.priority = call_priority;
			this.sortCalls();
			unlock();
		}
	}

	public synchronized void handle_inivr(String call_id, String transaction, String start,
			String ended, String[] terminates, String timestamp, String[] nodes,
			String dnis) 
	{
		if(DEBUGGING)
			System.out.println("7 - \thandle_inivr");
		
		if(!ended.equalsIgnoreCase("undefined"))
			return;

		lock();
		Call call = getCallByID(call_id);
		unlock();
		
		if(call == null)
		{
			call = new Call();
	
			call.setState(Call.INIVR);
			
			call.id = call_id;
			call.nodes = nodes;
		
			lock();
			this.addCall(call);
			this.sortCalls();
			unlock();
		}
		else
		{
			lock();

			call.setState(Call.INIVR);
			
			call.id = call_id;
			call.nodes = nodes;
			this.sortCalls();
			
			unlock();
		}
	}

	public synchronized void handle_dialoutgoing(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String number_dialed) 
	{	
		if(DEBUGGING)
			System.out.println("8 - \thandle_dialoutgoing");
		
		String terminates_string = "[";
		for(int i = 0; i < terminates.length; i++)
			terminates_string += terminates[i]+",";
		terminates_string += "]";
		
		String nodes_string = "[";
		for(int i = 0; i < nodes.length; i++)
			nodes_string += nodes[i]+",";
		nodes_string += "]";
		
		java.awt.Toolkit.getDefaultToolkit().beep();
		System.err.println("Unhandled message (handle_dialoutgoing):\n"+
							"call_id="+call_id+"\n"+
							"transaction="+transaction+"\n"+
							"start="+start+"\n"+
							"ended="+ended+"\n"+
							"terminates="+terminates_string+"\n"+
							"timestamp="+timestamp+"\n"+
							"nodes="+nodes_string+"\n"+
							"number_dialed="+number_dialed);
	}

	public synchronized void handle_inqueue(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String queue) 
	{
		if(DEBUGGING)
			System.out.println("9 - \thandle_inqueue");
		
		if(!ended.equalsIgnoreCase("undefined"))
			return;

		lock();
		Call call = getCallByID(call_id);
		unlock();
		
		if(call == null)
		{
			call = new Call();

			call.setState(Call.INQUEUE);
			
			call.id = call_id;
			call.nodes = nodes;
			call.queue = queue;
		
			lock();
			this.addCall(call);
			this.sortCalls();
			unlock();
		}
		else
		{
			lock();

			call.setState(Call.INQUEUE);
			
			call.id = call_id;
			call.nodes = nodes;
			call.queue = queue;

			this.sortCalls();			
			unlock();
		}
	}

	public synchronized void handle_ringing(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent) 
	{
		if(DEBUGGING)
			System.out.println("10 - \thandle_ringing");

		if(ended.equalsIgnoreCase("undefined"))
		{
			lock();
			Agent ringingAgent = getAgentByLogin(agent);
			if(ringingAgent == null)
			{
				unlock();
				return;
			}
			Call call = getCallByID(call_id);

			if(call == null)
			{
				unlock();
				return;
			}
			
			call.setState(Call.RINGING);
			call.nodes = nodes;
			ringingAgent.call = call;

			this.sortAgents();
			unlock();
		}
	}

	public synchronized void handle_ringout(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent, String ringout_reason) 
	{
		if(DEBUGGING)
			System.out.println("11 - \thandle_ringout");
		lock();
		Call call = getCallByID(call_id);
		if(call == null)
		{
			unlock();
			return;
		}
		call.setState(Call.RINGOUT);
		if(call.agent != null)
			call.agent.call = null;
		call.agent = null;
		call.nodes = nodes;
		this.sortCalls();
		unlock();
	}

	public synchronized void handle_precall(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String client) 
	{
		if(DEBUGGING)
			System.out.println("12 - \thandle_precall");
		
	}
	
	public synchronized void handle_oncall(String call_id, String transaction, String start,
			String ended, String[] terminates, String timestamp, String[] nodes,
			String agent) 
	{
		if(DEBUGGING)
			System.out.println("13 - \thandle_oncall");

		if(ended.equalsIgnoreCase("undefined"))
		{		
			lock();
			Call call = getCallByID(call_id);

			if(call == null)
			{
				unlock();
				return;
			}
			
			call.setState(Call.ONCALL);
			call.nodes = nodes;
			
			if(!call.agent.login.equalsIgnoreCase(agent))
			{
				if(call.agent.call.id.equalsIgnoreCase(call_id))
				{
					call.agent.call = null;
				}
				call.agent = getAgentByLogin(agent);
			}

			this.sortCalls();
			unlock();
		}
	}

	public synchronized void handle_agent_transfer(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent, String agent_transfer_recipient) 
	{
		if(DEBUGGING)
			System.out.println("14 - \thandle_agent_transfer");
		
		lock();
		Call call = getCallByID(call_id);
		call.agent = getAgentByLogin(agent_transfer_recipient);
		unlock();
	}

	public synchronized void handle_queue_transfer(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String queue) {


		if(DEBUGGING)
			System.out.println("15 - \thandle_queue_transfer");
	}

	public synchronized void handle_transfer(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String transfer_to) {

		if(DEBUGGING)
			System.out.println("16 - \thandle_transfer");
		
	}

	public synchronized void handle_warmxfer_begin(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent, String transfer_to) {


		if(DEBUGGING)
			System.out.println("17 - \thandle_warmxfer_begin");
	}

	public synchronized void handle_warmxfer_end(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent) {


		if(DEBUGGING)
			System.out.println("18 - \thandle_warmxfer_end");
	}

	public synchronized void handle_warmxfer_fail(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent) {


		if(DEBUGGING)
			System.out.println("19 - \thandle_warmxfer_fail");
	}

	public synchronized void handle_warmxfer_complete(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent) {


		if(DEBUGGING)
			System.out.println("20 - \thandle_warmxfer_complete");
	}

	public synchronized void handle_wrapup(String call_id, String transaction, String start,
			String ended, String[] terminates, String timestamp, String[] nodes,
			String agent) {
		if(DEBUGGING)
			System.out.println("21 - \thandle_wrapup");

		
		lock();
		Call call = getCallByID(call_id);
	
		if(call == null)
		{
			unlock();
			return;
		}
		
		if(call.agent != null && call.agent.login != null && call.agent.login.equalsIgnoreCase(agent))
			call.setState(Call.HANGUP);
		unlock();
	}

	public synchronized void handle_endwrapup(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String agent) {
		if(DEBUGGING)
			System.out.println("22 - \thandle_endwrapup");

		lock();
		Call call = getCallByID(call_id);
		
//		try
//		{
//			System.out.println(" " + (call == null));
//			System.out.println(" " + (call.agent == null));
//			System.out.println(" " + call.agent.login + " " + agent);
//			System.out.println(" " + (!call.agent.login.equalsIgnoreCase(agent)));
//		}catch(Exception e){}
		
		if(call == null || call.agent == null || !call.agent.login.equalsIgnoreCase(agent))
		{
			unlock();
			return;
		}
		
		endCall(call_id);
		if(call.agent != null)
			call.agent.call = null;
		call.agent = null;

		this.sortCalls();
		
		unlock();
	}

	public synchronized void handle_abandonqueue(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String queue) {

		if(DEBUGGING)
			System.out.println("23 - \thandle_abandonqueue");
		
	}

	public synchronized void handle_abandonivr(String call_id, String transaction, String start,
			String ended, String[] terminates, String timestamp, String[] nodes) 
	{
		if(DEBUGGING)
			System.out.println("24 - \thandle_abandonivr");
	}

	public synchronized void handle_voicemail(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String queue) {

		if(DEBUGGING)
			System.out.println("25 - \thandle_voicemail");
		
	}

	public synchronized void handle_hangup(String call_id, String transaction, String start,
			String ended, String[] terminates, String timestamp, String[] nodes,
			String hangup_by) 
	{
		if(DEBUGGING)
			System.out.println("26 - \thandle_hangup");
		
		
		lock();
		Call call = getCallByID(call_id);
		
		if(call == null)
		{
			unlock();
			return;
		}
		call.setState(Call.HANGUP);

		this.sortCalls();
		unlock();
	}

	public synchronized void handle_media_custom(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes, String media_custom_name,
			String[] media_custom_terminated) {

		if(DEBUGGING)
			System.out.println("27 - \thandle_media_custom");
		
	}

	public synchronized void handle_cdr_raw(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes) {

		if(DEBUGGING)
			System.out.println("28 - \thandle_cdr_raw");
		
	}

	public synchronized void handle_agent_profile_change(String agent_id,
			String agent_login, String old_profile, String new_profile,
			String[][] skills, String[][] dropped_skills,
			String[][] gained_skills) 
	{
		if(DEBUGGING)
			System.out.println("29 - \thandle_agent_profile_change");
		
		lock();
		Agent agent = getAgentByID(agent_id);
		agent.profile = new_profile;
		agent.skills = skills;
		this.sortAgents();
		unlock();
	}

	@SuppressWarnings("deprecation")
	public synchronized void handle_agent_init_state(String agent_login, String agent_id,
			String agent_profile, String agent_state, String[][] agent_skills,
			String release_id, String release_label, int release_bias,
			String call_id_number, String call_type, String caller_id_name,
			String caller_id_data, String call_dnis, boolean call_is_defualt_client,
			String call_client_label, String call_client_id,
			String call_ring_path, String call_media_path,
			String call_direction, String call_node, String call_priority) 
	{
		if(DEBUGGING)
			System.out.println("30 - \thandle_agent_init_state");

		lock();
		Call call = this.getCallByID(call_id_number);
		unlock();

		Agent newAgent = new Agent(agent_id, agent_login, agent_profile, new String[0], agent_skills);
		
		if(call_id_number != null && call==null)//&& !agent_state.equalsIgnoreCase("wrapup"))
		{
			call = new Call();

			if(Agent.getStateEnum(agent_state) == Agent.WRAPUP)
				call.setState(Call.HANGUP);
			else if(Agent.getStateEnum(agent_state) == Agent.RINGING)
				call.setState(Call.RINGING);
			else
				call.setState(Call.ONCALL);
			
			call.id = call_id_number;
			call.type = call_type;
			call.caller_id_name = java.net.URLDecoder.decode(caller_id_name);
			call.caller_id_data = java.net.URLDecoder.decode(caller_id_data);
			call.call_dnis = call_dnis;
			call.is_default_client = call_is_defualt_client;
			call.client_label = call_client_label;
			call.client_id = call_client_id;
			call.ring_path = call_ring_path;
			call.media_path = call_media_path;
			call.direction = call_direction;
			call.nodes = new String[]{call_node};
			call.agent = newAgent;
			call.priority = call_priority;
			call.priority = call_priority;
		
			newAgent.call = call;
			
			lock();
			this.addCall(call);
			unlock();
		}
		else if(call_id_number != null && call!=null)
		{
			lock();

			if(Agent.getStateEnum(agent_state) == Agent.WRAPUP)
				call.setState(Call.HANGUP);
			else if(Agent.getStateEnum(agent_state) == Agent.RINGING)
				call.setState(Call.RINGING);
			else
				call.setState(Call.HANGUP);
			
			if(call.agent!=null)
			{
				if(call.agent.state == Agent.WRAPUP)
					//This agent transfered the call to another and is wrapping up. Set the call's agent to that other agent.
					call.agent = newAgent;
			}
			unlock();
		}
		
		newAgent.setState(agent_state);
		newAgent.release_id = release_id;
		newAgent.release_label = release_label;
		newAgent.release_bias = release_bias;
		
		lock();
		this.addAgent(newAgent);
		unlock();
	}

	@SuppressWarnings("deprecation")
	public synchronized void handle_queued_call(String queue_name,
			String call_id_number, String call_type, String caller_id_name, String caller_id_data, String call_dnis, boolean call_is_defualt_client,
			String call_client_label, String call_client_id, String call_ring_path, String call_media_path, String call_direction, String call_node, String call_priority) 
	{
		if(DEBUGGING)
			System.out.println("31 - \thandle_queued_call");

		lock();
		Call call = getCallByID(call_id_number);
		unlock();
		if(call == null)
		{
			lock();
			call = new Call();
			
			call.setState(Call.INQUEUE);
			
			call.queue = queue_name;
			
			call.id = call_id_number;
			call.type = call_type;
			call.caller_id_name = java.net.URLDecoder.decode(caller_id_name);
			call.caller_id_data = java.net.URLDecoder.decode(caller_id_data);
			call.call_dnis = call_dnis;
			call.is_default_client = call_is_defualt_client;
			call.client_label = call_client_label;
			call.client_id = call_client_id;
			call.ring_path = call_ring_path;
			call.media_path = call_media_path;
			call.direction = call_direction;
			call.nodes = new String[]{call_node};
			call.priority = call_priority;
			
			this.addCall(call);
			unlock();
		}
		else
		{
			lock();
			call.setState(Call.INQUEUE);
			call.queue = queue_name;
			unlock();
		}
	}


	public void handle_logout(String agent_id, String agent, String state,
			String oldstate, String start, String ended, String profile,
			String timestamp, String[] nodes) 
	{
		if(DEBUGGING)
			System.out.println("32 - \thandle_logout");

		lock();
		Agent logoutAgent = getAgentByID(agent_id);
		
		logoutAgent.setState(Agent.LOGOUT);
		this.endAgent(agent_id);
		unlock();
	}
	
	public String getState()
	{
		String state = "-----------Calls-----------\n";
		for(int i = 0; i < calls.length; i++)
			state+=calls[i].getState()+"\n";
		
		state+= "-----------Agents-----------\n";
		for(int i = 0; i < agents.length; i++)
			state+=agents[i].getState()+"\n";
		
		state+= "-----------End-----------\n";
		
		return state;
	}

	public synchronized void handle_cdr_end(String call_id, String transaction,
			String start, String ended, String[] terminates, String timestamp,
			String[] nodes) 
	{
		lock();
		this.endCall(call_id);
		unlock();
	}
	
	public static int stippleFactor = 2;
	public static short stippleA = (short) 61680;
	public static short stippleB = (short) 61680;
	public static long lastStippleUpdate = System.nanoTime();
	public static double updateRate = .02;

	
	Agent[] bufferedAgents = null;
	Call[] bufferedCalls = null;
	
	long lastTime = System.nanoTime();
	
	public void updateBuffer()
	{
		long thisTime = System.nanoTime();
		double timeStep = (thisTime - lastTime)/1E9;
		lastTime = thisTime;
		
		lock();
		this.loadAgentTargetPositions();
		this.loadCallTargetPositions();
		
		double[] currentPos = new double[3];
		double[] targetPos = new double[3];
		double[] force = new double[3];
		
		for(int i = 0; i < calls.length; i++)
		{
			currentPos[0] = calls[i].currentX;
			currentPos[1] = calls[i].currentY;

			targetPos[0] = calls[i].targetX;
			targetPos[1] = calls[i].targetY;
			
			if(calls[i].spring.getForce(currentPos, targetPos, force))
			{
				calls[i].currentX += force[0]*timeStep;
				calls[i].currentY += force[1]*timeStep;
			}
		}

		for(int i = 0; i < agents.length; i++)
		{
			currentPos[0] = agents[i].currentX;
			currentPos[1] = agents[i].currentY;

			targetPos[0] = agents[i].targetX;
			targetPos[1] = agents[i].targetY;
			
			if(agents[i].spring.getForce(currentPos, targetPos, force))
			{
				agents[i].currentX += force[0]*timeStep;
				agents[i].currentY += force[1]*timeStep;
			}
		}
			
		bufferedAgents = new Agent[agents.length];
		bufferedCalls = new Call[calls.length];
		
		for(int i = 0; i < bufferedAgents.length; i++)
			bufferedAgents[i] = agents[i].clone();
		
		for(int i = 0; i < bufferedCalls.length; i++)
			bufferedCalls[i] = calls[i].clone();
		
		unlock();
		
		if((System.nanoTime() - OpenACD.lastStippleUpdate)/1E9 > OpenACD.updateRate)
		{
			stippleA = (short) ((stippleA << 1) | ((stippleA & 32768) >> 15)); 
			OpenACD.lastStippleUpdate = System.nanoTime();
		}
	}
	
	private int getCallOffset(int index)
	{
		int callOffset = (int) (Call.padding*2);

		for(int i = 0; i < index; i++)
		{
			if(!bufferedCalls[i].collapsed)
				callOffset += (GraphicalMain.fontSize + Call.padding*3);
			else if(i < bufferedCalls.length-1 && bufferedCalls[i+1].collapsed)
				callOffset += bufferedCalls[i].collapsedHeight+GraphicalMain.fontSize/8;
			else
				callOffset += bufferedCalls[i].collapsedHeight+GraphicalMain.fontSize/2;
		}
		if(!bufferedCalls[index].collapsed)
			callOffset += (Call.padding*2+GraphicalMain.fontSize)/2;
		else
			callOffset += bufferedCalls[index].collapsedHeight/2;
		
		return callOffset;
	}
	

	private int getAgentOffset(int index)
	{
		int agentOffset = (int) (Agent.padding*2);

		for(int i = 0; i < index; i++)
		{
			if(!bufferedAgents[i].collapsed)
				agentOffset += (GraphicalMain.fontSize + Agent.padding*3);
			else if(i < bufferedAgents.length-1 && bufferedAgents[i+1].collapsed)
				agentOffset += bufferedAgents[i].collapsedHeight+GraphicalMain.fontSize/8;
			else
				agentOffset += bufferedAgents[i].collapsedHeight+GraphicalMain.fontSize/2;
		}
		
		if(index == -1)
			return 0;
		
		if(!bufferedAgents[index].collapsed)
			agentOffset += (Agent.padding*2+GraphicalMain.fontSize)/2;
		else
			agentOffset += bufferedAgents[index].collapsedHeight/2;
		
		return agentOffset;
	}
	
	public void drawQueueWeight(GL gl)
	{
	}
	
	public void drawCallFlow(GL gl) 
	{	
		stats.draw(gl, this, (int) -((-3)*(GraphicalMain.fontSize*2.5)));
		
		int callOffset = (int) (Call.padding*2);
		
		//Draw Calls
		Call.renderLabels(gl, (int) -((-1)*(GraphicalMain.fontSize + Call.padding*3)+Call.padding));
		for(int i = 0; i < bufferedCalls.length; i++)
		{
	        gl.glPushName(i);
			bufferedCalls[i].draw(gl, (int) -callOffset, GraphicalMain.lastBestHit == i);
			gl.glPopName();
			
			if(!bufferedCalls[i].collapsed)
				callOffset += (GraphicalMain.fontSize + Call.padding*3);
			else if(i < bufferedCalls.length-1 && bufferedCalls[i+1].collapsed)
				callOffset += bufferedCalls[i].collapsedHeight+GraphicalMain.fontSize/8;
			else
				callOffset += bufferedCalls[i].collapsedHeight+GraphicalMain.fontSize/2;
				
		}
		
		int agentOffset = (int) +Agent.padding*2;
		
		//Draw Agents
		Agent.renderLabels(gl, (int) -((-1)*(GraphicalMain.fontSize + Agent.padding*3)+Agent.padding));
		for(int i = 0; i < bufferedAgents.length; i++)
		{
	        gl.glPushName(bufferedCalls.length + i);
			bufferedAgents[i].draw(gl, (int) -agentOffset, GraphicalMain.lastBestHit == bufferedCalls.length + i);
			gl.glPopName();

			if(!bufferedAgents[i].collapsed)
				agentOffset += (GraphicalMain.fontSize + Agent.padding*3);
			else if(i < bufferedAgents.length-1 && bufferedAgents[i+1].collapsed)
				agentOffset += bufferedAgents[i].collapsedHeight+GraphicalMain.fontSize/8;
			else
				agentOffset += bufferedAgents[i].collapsedHeight+GraphicalMain.fontSize/2;
		}
		
		
		//Draw Connected/Connecting tempCalls
		gl.glEnable(GL.GL_LINE_STIPPLE);
		gl.glLineStipple(OpenACD.stippleFactor, OpenACD.stippleA);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBegin(GL.GL_LINES);
		gl.glColor4f(1,1,1,1);
		for(int i = 0; i < bufferedCalls.length; i++)
			if(bufferedCalls[i].agent != null && (bufferedCalls[i].state == Call.RINGING || bufferedCalls[i].state == Call.HANGUP))
			{	
				if(bufferedCalls[i].state == Call.RINGING)
				{
					gl.glColor3f(1, 1, 1);
					int agentIndex = getAgentIndexByID(bufferedCalls[i].agent.id, bufferedAgents);
					if(!bufferedCalls[i].collapsed)
						gl.glVertex3d(bufferedCalls[i].currentX+Call.callGridLength,
								(int) GraphicalMain.user.scroll+bufferedCalls[i].currentY-GraphicalMain.fontSize,//GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll - (int) (i*(GraphicalMain.fontSize + Call.padding*3)+Call.padding*2)-(Call.padding*2+GraphicalMain.fontSize)/2, 
									  0);
					else
						gl.glVertex3d(bufferedCalls[i].currentX+Call.callGridLength,
								(int) GraphicalMain.user.scroll+bufferedCalls[i].currentY,//GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll - (int) (i*(GraphicalMain.fontSize + Call.padding*3)+Call.padding*2)-(Call.padding*2+GraphicalMain.fontSize)/2, 
									  0);
					
					if(!bufferedAgents[agentIndex].collapsed)
						gl.glVertex3d((GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding),
								GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+bufferedAgents[agentIndex].currentY-GraphicalMain.fontSize,
									  0);
					else
						gl.glVertex3d((GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding),
								GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+bufferedAgents[agentIndex].currentY,
									  0);
				}
				else
				{
					gl.glColor3f(1, 1, 0);
					
					int agentIndex = getAgentIndexByID(bufferedCalls[i].agent.id, bufferedAgents);
					
					if(!bufferedAgents[agentIndex].collapsed)
						gl.glVertex3d((GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding),
								GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+bufferedAgents[agentIndex].currentY-GraphicalMain.fontSize,
									  0);
					else
						gl.glVertex3d((GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding),
								GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+bufferedAgents[agentIndex].currentY,
									  0);
					
					if(!bufferedCalls[i].collapsed)
						gl.glVertex3d(bufferedCalls[i].currentX+Call.callGridLength,
								(int) GraphicalMain.user.scroll+bufferedCalls[i].currentY-GraphicalMain.fontSize,//GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll - (int) (i*(GraphicalMain.fontSize + Call.padding*3)+Call.padding*2)-(Call.padding*2+GraphicalMain.fontSize)/2, 
									  0);
					else
						gl.glVertex3d(bufferedCalls[i].currentX+Call.callGridLength,
								(int) GraphicalMain.user.scroll+bufferedCalls[i].currentY,//GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll - (int) (i*(GraphicalMain.fontSize + Call.padding*3)+Call.padding*2)-(Call.padding*2+GraphicalMain.fontSize)/2, 
									  0);
				}
			}
		gl.glEnd();
		gl.glDisable(GL.GL_LINE_STIPPLE);

		gl.glBegin(GL.GL_LINES);
		for(int i = 0; i < bufferedCalls.length; i++)
			if(bufferedCalls[i].agent != null && bufferedCalls[i].state != Call.RINGING && bufferedCalls[i].state != Call.HANGUP)
			{	
				if(bufferedCalls[i].state == Call.ONCALL)
					gl.glColor4f(0, 1, 0, 1);
				else
					gl.glColor4f(1, 1, 0, 1);
				
				int agentIndex = getAgentIndexByID(bufferedCalls[i].agent.id, bufferedAgents);
				
				if(!bufferedCalls[i].collapsed)
					gl.glVertex3d(bufferedCalls[i].currentX+Call.callGridLength,
							(int) GraphicalMain.user.scroll+bufferedCalls[i].currentY-GraphicalMain.fontSize,//GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll - (int) (i*(GraphicalMain.fontSize + Call.padding*3)+Call.padding*2)-(Call.padding*2+GraphicalMain.fontSize)/2, 
								  0);
				else
					gl.glVertex3d(bufferedCalls[i].currentX+Call.callGridLength,
							(int) GraphicalMain.user.scroll+bufferedCalls[i].currentY,//GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll - (int) (i*(GraphicalMain.fontSize + Call.padding*3)+Call.padding*2)-(Call.padding*2+GraphicalMain.fontSize)/2, 
								  0);
				
				if(!bufferedAgents[agentIndex].collapsed)
					gl.glVertex3d((GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding-1),
							GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+bufferedAgents[agentIndex].currentY-GraphicalMain.fontSize,
								  0);
				else
					gl.glVertex3d((GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding-1),
							GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+bufferedAgents[agentIndex].currentY,
								  0);
					
			}
		gl.glEnd();

		gl.glDisable(GL.GL_LINE_STIPPLE);
		gl.glDisable(GL.GL_LINE_SMOOTH);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
		gl.glDisable(GL.GL_BLEND);
	}

	private static String maxRender(String string, int width)
	{
		if(string == null)
			return maxRender("NULL", width);
		
		String tempString = "";
		
		for(int i = 0; i < string.length(); i++)
		{
			if(GraphicalMain.textRenderer.getBounds((tempString + string.charAt(i)).replace(" ", ".")).getWidth() > width)
				return tempString;
			else
				tempString += string.charAt(i);
		}
		return tempString;
	}
	
	private String[] getRenderString(String data)
	{
		data = data.replace("=", ": ");
		String[] lines = data.split("\n");
		
		ArrayList<String> newData = new ArrayList<String>(0);
		for(int i = 0; i < lines.length; i++)
		{
			String tempString = lines[i];
			while(!tempString.equalsIgnoreCase(""))
			{
				String appendedString = maxRender(tempString, GraphicalMain.RENDER_WIDTH/4);
				newData.add(appendedString);
				tempString = tempString.substring(appendedString.length());
			}
		}
		
		String[] returnThis = new String[newData.size()];
		
		for(int i = 0; i < returnThis.length; i++)
			returnThis[i] = newData.get(i);
		
		return returnThis;
	}
	
	boolean mouseBlink = false;
	
	private void mouseBlink()
	{
		if(mouseBlink)
		{
			hideCursor();
			mouseBlink = !mouseBlink;
		}
		else
		{
			showCursor();
			mouseBlink = !mouseBlink;
		}
	}
	
	public void processHit(int bestHitID, GL gl) 
	{
		if(bestHitID == -1)
		{
	        GraphicalMain.user.drawMouse(gl);
			return;
		}
		else if(bestHitID < bufferedCalls.length)
		{

			String[] renderString = getRenderString(bufferedCalls[bestHitID].getState());

			int lastMouseX = User.lastMouseX+GraphicalMain.fontSize;
			int lastMouseY = User.lastMouseY+GraphicalMain.fontSize;
			
			if(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize > GraphicalMain.RENDER_WIDTH)
				lastMouseX += GraphicalMain.RENDER_WIDTH-(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize);

			if(GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2 < 0)
				lastMouseY += GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2;
			
			gl.glColor4f(0,0,0,1);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glEnd();

			gl.glColor4f(1,1,0,1);
			gl.glLineWidth(2.0f);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glEnd();
			gl.glLineWidth(1.0f);
			
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
			GraphicalMain.textRenderer.begin3DRendering();
			
			for(int i = 0; i < renderString.length; i++)
				GraphicalMain.textRenderer.draw3D(renderString[i], lastMouseX+GraphicalMain.fontSize/2, (float) (GraphicalMain.RENDER_HEIGHT-lastMouseY - (i+1)*GraphicalMain.fontSize*1.5), 0, 1);
			
			GraphicalMain.textRenderer.flush();
			GraphicalMain.textRenderer.end3DRendering();
		}
		else if(bestHitID < bufferedCalls.length + bufferedAgents.length)
		{
			String[] renderString = getRenderString(bufferedAgents[bestHitID-bufferedCalls.length].getState());

			int lastMouseX = User.lastMouseX+GraphicalMain.fontSize;
			int lastMouseY = User.lastMouseY+GraphicalMain.fontSize;
			
			if(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize > GraphicalMain.RENDER_WIDTH)
				lastMouseX += GraphicalMain.RENDER_WIDTH-(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize);
			
			if(GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2 < 0)
				lastMouseY += GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2;
			
			gl.glColor4f(0,0,0,1);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glEnd();

			gl.glColor4f(1,1,0,1);
			gl.glLineWidth(2.0f);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY, 0);
			gl.glVertex3d(lastMouseX+GraphicalMain.RENDER_WIDTH/4+GraphicalMain.fontSize, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glVertex3d(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY-renderString.length*GraphicalMain.fontSize*1.5-GraphicalMain.fontSize/2, 0);
			gl.glEnd();
			gl.glLineWidth(1.0f);
			
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
			GraphicalMain.textRenderer.begin3DRendering();
			
			for(int i = 0; i < renderString.length; i++)
				GraphicalMain.textRenderer.draw3D(renderString[i], lastMouseX+GraphicalMain.fontSize/2, (float) (GraphicalMain.RENDER_HEIGHT-lastMouseY - (i+1)*GraphicalMain.fontSize*1.5), 0, 1);
			
			GraphicalMain.textRenderer.flush();
			GraphicalMain.textRenderer.end3DRendering();
		}
	}

	private void showCursor() 
	{
		GraphicalMain.canvas.setCursor(Cursor.getDefaultCursor());
	}
	
	private void hideCursor() 
	{
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImg, new Point(0, 0), "blank cursor"); 
		GraphicalMain.canvas.setCursor(blankCursor);
	}

	boolean expandedAgentsLast = true;
	boolean expandedCallsLast = true;
		
	public void toggleAgentExpansion()
	{
		if(expandedAgentsLast)
		{
			collapseAgents();
			GraphicalMain.self.autofoldAgents.setState(true);
			GraphicalMain.self.autofoldAgents.setLabel("Auto Fold Agents (on)");
		}
		else
		{
			expandAgents();
			GraphicalMain.self.autofoldAgents.setState(false);
			GraphicalMain.self.autofoldAgents.setLabel("Auto Fold Agents (off)");
		}
	}
	
	public void toggleCallExpansion()
	{
		if(expandedCallsLast)
		{
			collapseCalls();
			GraphicalMain.self.autofoldCalls.setState(true);
			GraphicalMain.self.autofoldCalls.setLabel("Auto Fold Calls (on)");
		}
		else
		{
			expandCalls();
			GraphicalMain.self.autofoldCalls.setState(false);
			GraphicalMain.self.autofoldCalls.setLabel("Auto Fold Calls (off)");
		}
	}
	
	public void expandAgents() 
	{
		lock();
		for(int i = 0; i < agents.length; i++)
			agents[i].collapsed = false;
		unlock();
		expandedAgentsLast = true;
	}

	public void collapseAgents() 
	{
		lock();
		for(int i = 0; i < agents.length; i++)
			agents[i].collapsed = true;
		unlock();
		expandedAgentsLast = false;
	}

	public void expandCalls() 
	{
		lock();
		for(int i = 0; i < calls.length; i++)
			calls[i].collapsed = false;
		unlock();
		expandedCallsLast = true;
	}

	public void collapseCalls() 
	{
		lock();
		for(int i = 0; i < calls.length; i++)
			calls[i].collapsed = true;
		unlock();
		expandedCallsLast = false;
	}
	
	public void lock()
	{
//		try
//		{
//			Object o = new Object();
//			OpenACD acd = (OpenACD) o;
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		System.out.println("lock_start");
		lock.lock();
//		System.out.println("lock_end");
	}
	
	public void unlock()
	{
//		try
//		{
//			Object o = new Object();
//			OpenACD acd = (OpenACD) o;
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		System.out.println("unlock_start");
		lock.unlock();
//		System.out.println("unlock_end");
	}
}
