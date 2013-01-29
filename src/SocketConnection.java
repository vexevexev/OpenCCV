


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import JSON.JSONArray;
import JSON.JSONException;
import JSON.JSONObject;


public class SocketConnection 
{
	OutputStreamWriter out;

	OpenACD acd = new OpenACD();
	
	public static String host = "Enter Server Name";
	
	public SocketConnection()
	{
		try {
			Socket socket = new Socket(host, 3839);
			out = new OutputStreamWriter(socket.getOutputStream());
			final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new Thread(new Runnable()
			{
				public void run()
				{
					try 
					{
						while(true)
						{
							receiveMessage(in.readLine());
						}
					} 
					catch (Exception e) 
					{
						System.err.println(e.getMessage());
						e.printStackTrace();
						connectionLost();
					}
				}
			}).start();
		} 
		catch ( IOException e) 
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
			connectionFailed();
		}
	}
	
	public void sendMessage(String message)
	{
		try {
			out.write(message);
			out.flush();
		} 
		catch ( IOException e) 
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void receiveMessage(String message) {
		GraphicalMain.self.user.console.append("server > " + message);
		try {
			JSONObject JSON = new JSONObject(message);
			String[] fields = JSONObject.getNames(JSON);
			if(fields != null && fields.length > 0 && fields[0].equalsIgnoreCase("agent_state_change"))
			{
				JSON = JSON.getJSONObject("agent_state_change");
				String agent_id = JSON.getString("id");
				String agent = JSON.getString("agent");
				String state = JSON.getString("state");
				String oldstate = JSON.getString("oldstate");
				String start = JSON.getString("start");
				String ended = JSON.getString("ended");
				String profile = JSON.getString("profile");
				String timestamp = JSON.getString("profile");
				JSONArray jsonArray = JSON.getJSONArray("nodes");
				String[] nodes = new String[jsonArray.length()];
				for(int i = 0; i < jsonArray.length(); i++)
					nodes[i] = jsonArray.getString(i);
				
				if(state.equalsIgnoreCase("logout"))
				{
					acd.handle_logout(agent_id, agent, state, oldstate, start, ended, profile, timestamp, nodes);
				}
				else if(oldstate.equalsIgnoreCase("login"))
				{
					jsonArray = JSON.getJSONArray("statedata");
					String[][] skills = new String[jsonArray.length()][];
					for(int i = 0; i < skills.length; i++)
						skills[i] = elementToString(i, jsonArray);
					acd.handle_login(agent_id, agent, state, oldstate, start, ended, profile, timestamp, nodes, skills);
				}
				else if(oldstate.equalsIgnoreCase("idle"))
				{
					acd.handle_idle(agent_id, agent, state, oldstate, start, ended, profile, timestamp, nodes);
				}
				else if(oldstate.equalsIgnoreCase("precall"))
				{
					jsonArray = JSON.getJSONArray("statedata");
					boolean is_defualt_client = jsonArray.getJSONArray(0).getBoolean(1);
					String client_label = jsonArray.getJSONArray(1).getString(1);
					String client_id = jsonArray.getJSONArray(2).getString(1);
					
					acd.handle_precall(agent_id, agent, state, oldstate, start, ended, profile, timestamp, nodes, is_defualt_client, client_label, client_id);
				}
				else if(oldstate.equalsIgnoreCase("released"))
				{
					JSONObject releasedJSON = JSON.getJSONObject("statedata");
					String release_id = releasedJSON.getString("id");
					String release_label = releasedJSON.getString("label");
					int release_bias = Integer.valueOf(releasedJSON.getString("bias"));
					
					acd.handle_released(agent_id, agent, state, oldstate, start, ended, profile, timestamp, nodes, release_id, release_label, release_bias);
				}
				else
				{
					try
					{
						JSONObject callJSON = JSON.getJSONObject("statedata");
						if(callJSON.has("call"))
						{
							callJSON = callJSON.getJSONObject("call");
							String call_id_number = callJSON.getString("id");
							String call_type = callJSON.getString("type");
							
							JSONObject caller_id = callJSON.getJSONObject("caller_id");
							String caller_id_name = caller_id.getString("name");
							String caller_id_data = caller_id.getString("data");
							
							String call_dnis = callJSON.getString("dnis");

							jsonArray = callJSON.getJSONArray("client");
							boolean call_is_defualt_client = jsonArray.getJSONArray(0).getBoolean(1);
							String call_client_label = jsonArray.getJSONArray(1).getString(1);
							String call_client_id = jsonArray.getJSONArray(2).getString(1);
							
							String call_ring_path = callJSON.getString("ring_path");
							String call_media_path = callJSON.getString("media_path");
							String call_direction = callJSON.getString("direction");
							String call_node = callJSON.getString("node");

							String call_priority = callJSON.getString("priority");
							
							acd.handle_call(agent_id, agent, state, oldstate, start, ended, profile, timestamp, nodes,
									call_id_number, call_type, caller_id_name, caller_id_data, call_dnis, call_is_defualt_client,
									call_client_label, call_client_id, call_ring_path, call_media_path, call_direction, call_node, call_priority);
						}
					}
					catch(JSONException e)
					{
						System.err.println(e.getMessage());
						e.printStackTrace();
						GraphicalMain.self.user.console.append("server > [ERROR] while parsing message: " + message);
						handleJSONParseError(e);
					}
				}
			}
			else if(fields != null && fields.length > 0 && fields[0].equalsIgnoreCase("cdr_raw"))
			{	
				JSON = JSON.getJSONObject("cdr_raw");
				String call_id = JSON.getString("call_id");
				String transaction = JSON.getString("transaction");
				String start = JSON.getString("start");
				String ended = JSON.getString("ended");
				
				String[] terminates = null;
				
				try
				{
					JSONArray terminatesArray = JSON.getJSONArray("terminates");
					terminates = new String[terminatesArray.length()];
					for(int i = 0; i < terminates.length; i++)
						terminates[i] = terminatesArray.getString(i);
				}
				catch(JSONException e)
				{
					terminates = new String[1];
					terminates[0] = JSON.getString("terminates");
				}
				String timestamp = JSON.getString("timestamp");
				
				JSONArray jsonNodes = JSON.getJSONArray("nodes");
				String[] nodes = new String[jsonNodes.length()];
				for(int i = 0; i < nodes.length; i++)
					nodes[i] = jsonNodes.getString(i);
				
				if(transaction.equalsIgnoreCase("cdrinit"))
				{
					JSONObject callJSON = JSON.getJSONObject("call");
					callJSON = callJSON.getJSONObject("call");
					String call_id_number = callJSON.getString("id");
					String call_type = callJSON.getString("type");
					JSONObject caller_id = callJSON.getJSONObject("caller_id");
					String caller_id_name = caller_id.getString("name");
					String caller_id_data = caller_id.getString("data");
					String call_dnis = callJSON.getString("dnis");

					JSONArray jsonArray = callJSON.getJSONArray("client");
					boolean call_is_defualt_client = jsonArray.getJSONArray(0).getBoolean(1);
					String call_client_label = jsonArray.getJSONArray(1).getString(1);
					String call_client_id = jsonArray.getJSONArray(2).getString(1);
					
					String call_ring_path = callJSON.getString("ring_path");
					String call_media_path = callJSON.getString("media_path");
					String call_direction = callJSON.getString("direction");
					String call_node = callJSON.getString("node");

					String call_priority = callJSON.getString("priority");
					
					acd.handle_cdrinit(call_id, transaction, start, ended, terminates, timestamp, nodes,
							call_id_number, call_type, caller_id_name, caller_id_data, call_dnis, call_is_defualt_client,
							call_client_label, call_client_id, call_ring_path, call_media_path, call_direction, call_node, call_priority);
				}
				else if(transaction.equalsIgnoreCase("inivr"))
				{
					String dnis = JSON.getString("dnis");
					acd.handle_inivr(call_id, transaction, start, ended, terminates, timestamp, nodes, dnis);
				}
				else if(transaction.equalsIgnoreCase("dialoutgoing"))
				{
					String number_dialed = JSON.getString("number_dialed");
					acd.handle_dialoutgoing(call_id, transaction, start, ended, terminates, timestamp, nodes, number_dialed);
				}
				else if(transaction.equalsIgnoreCase("inqueue"))
				{
					String queue = JSON.getString("queue");
					acd.handle_inqueue(call_id, transaction, start, ended, terminates, timestamp, nodes, queue);
				}
				else if(transaction.equalsIgnoreCase("ringing"))
				{
					String agent = JSON.getString("agent");
					acd.handle_ringing(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("ringout"))
				{
					String agent = JSON.getString("agent");
					String ringout_reason = JSON.getString("ringout_reason");
					acd.handle_ringout(call_id, transaction, start, ended, terminates, timestamp, nodes, agent, ringout_reason);
				}
				else if(transaction.equalsIgnoreCase("precall"))
				{
					String client = JSON.getString("client"); 
					acd.handle_precall(call_id, transaction, start, ended, terminates, timestamp, nodes, client);
				}
				else if(transaction.equalsIgnoreCase("oncall"))
				{
					String agent = JSON.getString("agent");
					acd.handle_oncall(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("agent_transfer"))
				{
					String agent = JSON.getString("agent");
					String agent_transfer_recipient = JSON.getString("agent_transfer_recipient");
					acd.handle_agent_transfer(call_id, transaction, start, ended, terminates, timestamp, nodes, agent, agent_transfer_recipient);
				}
				else if(transaction.equalsIgnoreCase("queue_transfer"))
				{
					String queue = JSON.getString("queue");
					acd.handle_queue_transfer(call_id, transaction, start, ended, terminates, timestamp, nodes, queue);
				}
				else if(transaction.equalsIgnoreCase("transfer"))
				{
					String transfer_to = JSON.getString("transfer_to");
					acd.handle_transfer(call_id, transaction, start, ended, terminates, timestamp, nodes, transfer_to);
				}
				else if(transaction.equalsIgnoreCase("warmxfer_begin"))
				{
					String agent = JSON.getString("agent");
					String transfer_to = JSON.getString("transfer_to");
					acd.handle_warmxfer_begin(call_id, transaction, start, ended, terminates, timestamp, nodes, agent, transfer_to);
				}
				else if(transaction.equalsIgnoreCase("warmxfer_cancel"))
				{
					String agent = JSON.getString("agent");
					acd.handle_warmxfer_end(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("warmxfer_fail"))
				{
					String agent = JSON.getString("agent");
					acd.handle_warmxfer_fail(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("warmxfer_complete"))
				{
					String agent = JSON.getString("agent");
					acd.handle_warmxfer_complete(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("wrapup"))
				{
					String agent = JSON.getString("agent");
					acd.handle_wrapup(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("endwrapup"))
				{
					String agent = JSON.getString("agent");
					acd.handle_endwrapup(call_id, transaction, start, ended, terminates, timestamp, nodes, agent);
				}
				else if(transaction.equalsIgnoreCase("abandonqueue"))
				{
					String queue = JSON.getString("queue");
					acd.handle_abandonqueue(call_id, transaction, start, ended, terminates, timestamp, nodes, queue);
				}
				else if(transaction.equalsIgnoreCase("abandonivr"))
				{
					acd.handle_abandonivr(call_id, transaction, start, ended, terminates, timestamp, nodes);
				}
				else if(transaction.equalsIgnoreCase("voicemail"))
				{
					String queue = JSON.getString("queue");
					acd.handle_voicemail(call_id, transaction, start, ended, terminates, timestamp, nodes, queue);
				} 
				else if(transaction.equalsIgnoreCase("hangup"))
				{
					String hangup_by = JSON.getString("hangup_by");
					acd.handle_hangup(call_id, transaction, start, ended, terminates, timestamp, nodes, hangup_by);
				}
				else if(transaction.equalsIgnoreCase("media_custom"))
				{
					String media_custom_name = JSON.getString("media_custom_name");
					JSONArray jsonArray = JSON.getJSONArray("media_custom_terminated");
					String[] media_custom_terminated = new String[jsonArray.length()];
					for(int i = 0; i < media_custom_terminated.length; i++)
						media_custom_terminated[i] = jsonArray.getString(i);
					acd.handle_media_custom(call_id, transaction, start, ended, terminates, timestamp, nodes, media_custom_name, media_custom_terminated);
				}
				else if(transaction.equalsIgnoreCase("cdrend"))
				{
					acd.handle_cdr_end(call_id, transaction, start, ended, terminates, timestamp, nodes);
				}
				else
				{
					acd.handle_cdr_raw(call_id, transaction, start, ended, terminates, timestamp, nodes);
				}
			}
			else if(fields != null && fields.length > 0 && fields[0].equalsIgnoreCase("agent_profile_change"))
			{
				JSON = JSON.getJSONObject("agent_profile_change");
				
				String agent_id = JSON.getString("agent_id");
				String agent_login = JSON.getString("agent_login");
				String old_profile = JSON.getString("old_profile");
				String new_profile = JSON.getString("new_profile");

				JSONArray jsonArray = JSON.getJSONArray("skills");
				String[][] skills = new String[jsonArray.length()][];
				for(int i = 0; i < skills.length; i++)
					skills[i] = elementToString(i, jsonArray);

				jsonArray = JSON.getJSONArray("dropped_skills");
				String[][] dropped_skills = new String[jsonArray.length()][];
				for(int i = 0; i < dropped_skills.length; i++)
					dropped_skills[i] = elementToString(i, jsonArray);

				jsonArray = JSON.getJSONArray("gained_skills");
				String[][] gained_skills = new String[jsonArray.length()][];
				for(int i = 0; i < gained_skills.length; i++)
					gained_skills[i] = elementToString(i, jsonArray);
				
				acd.handle_agent_profile_change(agent_id, agent_login, old_profile, new_profile, skills, dropped_skills, gained_skills);
				
			}
			else if(fields != null && fields.length > 0 && fields[0].equalsIgnoreCase("agent_list"))
			{
				JSONArray array = JSON.getJSONArray("agent_list");
				
				for(int i = 0; i < array.length(); i++)
				{
					JSONObject agent = array.getJSONObject(i);
					String agent_login = agent.getString("login");
					String agent_id = agent.getString("id");
					String agent_profile = agent.getString("profile");
					String agent_state = agent.getString("state");
 
					JSONArray skillsArray = agent.getJSONArray("skills");
					String[][] agent_skills = new String[skillsArray.length()][];
					for(int j = 0; j < agent_skills.length; j++)
						agent_skills[j] = elementToString(j, skillsArray);
					
					String release_id, release_label;
					int release_bias;
					
					if(agent.has("release_data"))
					{
						JSONObject releasedJSON = agent.getJSONObject("release_data");
						release_id = releasedJSON.getString("id");
						release_label = releasedJSON.getString("label");
						release_bias = Integer.valueOf(releasedJSON.getString("bias"));
					}
					else
					{
						release_id = "null";
						release_label = "null";
						release_bias = 2;
					}
					
					boolean call_is_defualt_client = false;
					String call_id_number = null, call_type = null, caller_id_name = null, caller_id_data = null, call_dnis = null, call_client_label = null, call_client_id = null, 
							call_ring_path = null, call_media_path = null, call_direction = null, call_node = null, call_priority = null;
					
					
					if(agent.has("call"))
					{
						JSONObject callJSON = agent.getJSONObject("call");
						call_id_number = callJSON.getString("id");
						call_type = callJSON.getString("type");
						JSONObject caller_id = callJSON.getJSONObject("caller_id");
						caller_id_name = caller_id.getString("name");
						caller_id_data = caller_id.getString("data");
						call_dnis = callJSON.getString("dnis");
	
						JSONArray jsonArray = callJSON.getJSONArray("client");
						call_is_defualt_client = jsonArray.getJSONArray(0).getBoolean(1);
						call_client_label = jsonArray.getJSONArray(1).getString(1);
						call_client_id = jsonArray.getJSONArray(2).getString(1);
						
						call_ring_path = callJSON.getString("ring_path");
						call_media_path = callJSON.getString("media_path");
						call_direction = callJSON.getString("direction");
						call_node = callJSON.getString("node");
						call_priority = callJSON.getString("priority");
					}
					
					acd.handle_agent_init_state(agent_login, agent_id, agent_profile, agent_state, agent_skills,
							release_id, release_label, release_bias,
							call_id_number, call_type, caller_id_name, caller_id_data, call_dnis, call_is_defualt_client,
							call_client_label, call_client_id, call_ring_path, call_media_path, call_direction, call_node, call_priority);
				}
				acd.lock();
				acd.sortAgents();
				acd.loadAgentTargetPositions();
				acd.unlock();
			}
			else if(fields != null && fields.length > 0 && fields[0].equalsIgnoreCase("queued_calls"))
			{
				JSONArray array = JSON.getJSONArray("queued_calls");
				
				for(int i = 0; i < array.length(); i++)
				{
					JSONObject queued_call = array.getJSONObject(i);
					queued_call = queued_call.getJSONObject("call");
					
					String queue_name = queued_call.getString("queue_name");

//					JSONArray skillsArray = queued_call.getJSONArray("skills");
//					String[][] call_skills = new String[skillsArray.length()][];
//					for(int j = 0; j < call_skills.length; j++)
//						call_skills[j] = elementToString(j, skillsArray);

					String call_id_number = queued_call.getString("id");
					String call_type = queued_call.getString("type");
					JSONObject caller_id = queued_call.getJSONObject("caller_id");
					String caller_id_name = caller_id.getString("name");
					String caller_id_data = caller_id.getString("data");
					String call_dnis = queued_call.getString("dnis");

					JSONArray jsonArray = queued_call.getJSONArray("client");
					boolean call_is_defualt_client = jsonArray.getJSONArray(0).getBoolean(1);
					String call_client_label = jsonArray.getJSONArray(1).getString(1);
					String call_client_id = jsonArray.getJSONArray(2).getString(1);
					
					String call_ring_path = queued_call.getString("ring_path");
					String call_media_path = queued_call.getString("media_path");
					String call_direction = queued_call.getString("direction");
					String call_node = queued_call.getString("node");
					
					String call_priority = queued_call.getString("priority");
					
					acd.handle_queued_call(queue_name, 
							call_id_number, call_type, caller_id_name, caller_id_data, call_dnis, call_is_defualt_client,
							call_client_label, call_client_id, call_ring_path, call_media_path, call_direction, call_node, call_priority);
				}
				acd.lock();
				acd.sortCalls();
				acd.loadCallTargetPositions();
				acd.unlock();
			}
			
		} catch (JSONException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			GraphicalMain.self.user.console.append("server > [ERROR] while parsing message: " + message);
			handleJSONParseError(e);
		}
		
//		System.out.println(acd.getState());
	}

	private String[] elementToString(int i, JSONArray jsonArray) 
	{
		try
		{
			JSONArray subArray = jsonArray.getJSONArray(i);
			String[] skill = new String[2];
			skill[0] = subArray.getString(0);
			skill[1] = subArray.getString(1);
			return skill;
		}
		catch(JSONException e)
		{
			try 
			{
				return new String[]{jsonArray.getString(i)};
			} 
			catch (JSONException e2) 
			{
				handleJSONParseError(e2);
				return null;
			}
		}
	}

	private void handleJSONParseError(JSONException e) 
	{
		GraphicalMain.self.user.console.append(e.getMessage());
		StackTraceElement[] stackTrace = e.getStackTrace();
		for(int i = 0; i < stackTrace.length; i++)
			GraphicalMain.self.user.console.append(stackTrace[i].toString());
		System.err.println(e.getMessage());
		System.err.println(e.getStackTrace());
	}

	public void connectionLost() 
	{
		GraphicalMain.self.user.console.append("Connection Lost.");
	}

	public void connectionFailed() {
	}
	
}
