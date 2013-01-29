import java.awt.CheckboxMenuItem;
import java.awt.Menu;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.spi.SGIImage;




public class Agent implements Comparable<Agent>
{
	//This is for animation purposes. Without a steady change in position, 
	//the Call graphics will warp, making it difficult, if not, impossible, to read.
	/////////////////////////
	//Animation Information//
	/////////////////////////
	
	public static final float MASS = 1;
	
	float currentX = 0;
	float currentY = 0;
	
	float targetX = 0;
	float targetY = 0;
	
	boolean ending = false;
	
	Spring spring = new Spring(5, 0, 3);
	
	//////////////////////////////
	//UI and Sorting information//
	//////////////////////////////
	
	static int totalSortingFields = 5;
	
	static String agentState = "Agent State ";
	static String agentLogin = "Agent Login Name ";
	static String agentSkills = "Agent Skills (Condensed) ";
	static String agentRelease = "Agent Release ";
	static String agentDuration = "Agent State Duration ";

    static String[] agentSortingOrder = new String[0];
    
    static Menu agentSortingSubmenu = new Menu("Agent Sorting ");
    
	static String agentSortingEnabled = "Enable Agent Sorting";
	static CheckboxMenuItem agentSortingEnabledItem = new CheckboxMenuItem(agentSortingEnabled);
	
	static Menu agentStateSortMenu = new Menu(Agent.agentState);
	static Menu agentLoginSortMenu = new Menu(Agent.agentLogin);
	static Menu agentSkillsSortMenu = new Menu(Agent.agentSkills);
	static Menu agentReleaseSortMenu = new Menu(Agent.agentRelease);
	static Menu agentDurationSortMenu = new Menu(Agent.agentDuration);

    public static synchronized void removeSortOption(String option)
    {
    	int index = -1;
    	for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    	{
    		if(Agent.agentSortingOrder[i].equalsIgnoreCase(option))
    			index = i;
    	}
    	if(index != -1)
    	{
    		String[] newSortingOrder = new String[Agent.agentSortingOrder.length-1];
    		int j = 0;
    		for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    		{
    			if(i != index)
    			{
    				newSortingOrder[j] = Agent.agentSortingOrder[i];
    				j++;
    			}
    		}
    		Agent.agentSortingOrder = newSortingOrder;
    	}
    	Agent.loadUISort(GraphicalMain.self);
    }
    
    public static void setSortingMenu(Menu parent, int max, int target, GraphicalMain itemListener)
    {
    	parent.removeAll();
        for(int i = 0; i < max; i++)
        {
        	CheckboxMenuItem item = new CheckboxMenuItem(""+(i+1)+Agent.getOrdinalFor(i+1));
        	item.addItemListener(itemListener);
        	
        	if((i+1) == target)
        		item.setState(true);
        	else
        		item.setState(false);
        	
        	parent.add(item);
        }
    }
    
    private static String getOrdinalFor(int value) 
    {
		int hundredRemainder = value % 100;
		if(hundredRemainder >= 10 && hundredRemainder <= 20) 
		{
			return "th";
		}
		int tenRemainder = value % 10;
		switch (tenRemainder) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
		}
	}

    public static void loadUISort(GraphicalMain itemListener)
    {
    	boolean found;
    	
    	found = false;
    	for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    		if(Agent.agentSortingOrder[i].equalsIgnoreCase(Agent.agentState))
    		{
    			found = true;
    			Agent.setSortingMenu(Agent.agentStateSortMenu, Agent.agentSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Agent.setSortingMenu(Agent.agentStateSortMenu, Agent.agentSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    		if(Agent.agentSortingOrder[i].equalsIgnoreCase(Agent.agentLogin))
    		{
    			found = true;
    			Agent.setSortingMenu(Agent.agentLoginSortMenu, Agent.agentSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Agent.setSortingMenu(Agent.agentLoginSortMenu, Agent.agentSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    		if(Agent.agentSortingOrder[i].equalsIgnoreCase(Agent.agentSkills))
    		{
    			found = true;
    			Agent.setSortingMenu(Agent.agentSkillsSortMenu, Agent.agentSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Agent.setSortingMenu(Agent.agentSkillsSortMenu, Agent.agentSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    		if(Agent.agentSortingOrder[i].equalsIgnoreCase(Agent.agentRelease))
    		{
    			found = true;
    			Agent.setSortingMenu(Agent.agentReleaseSortMenu, Agent.agentSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Agent.setSortingMenu(Agent.agentReleaseSortMenu, Agent.agentSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Agent.agentSortingOrder.length; i++)
    		if(Agent.agentSortingOrder[i].equalsIgnoreCase(Agent.agentDuration))
    		{
    			found = true;
    			Agent.setSortingMenu(Agent.agentDurationSortMenu, Agent.agentSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Agent.setSortingMenu(Agent.agentDurationSortMenu, Agent.agentSortingOrder.length+1, -1, itemListener);

    	GraphicalMain.connection.acd.lock();
    	GraphicalMain.connection.acd.sortAgents();
    	GraphicalMain.connection.acd.unlock();
    }
    
    public static int getSortOrder(String field)
    {
    	String[] tempOrder = Agent.agentSortingOrder.clone();
    	
    	for(int i = 0; i < tempOrder.length; i++)
    	{
    		if(field.equalsIgnoreCase(tempOrder[i]))
    			return i;
    	}
    	return -1;
    }
    
    public static String getSortOrderString(String field)
    {
    	int order = Agent.getSortOrder(field);
    	if(order == -1)
    		return "";
    	else
    		return "(" + (order + 1) + ") ";
    }
    
	//////////////////////////////
	//Internal state information//
	//////////////////////////////
	
	public static final String[] AGENT_STATES = new String[] { "LOGIN", "IDLE", "RINGING", "PRECALL", "ONCALL", "OUTGOING", "RELEASED", "WARMTRANSFER", "WRAPUP", "LOGOUT", "LIMBO"};
	public static final int LOGIN = 0;
	public static final int IDLE = 1;
	public static final int RINGING = 2;
	public static final int PRECALL = 3;
	public static final int ONCALL = 4;
	public static final int OUTGOING = 5;
	public static final int RELEASED = 6;
	public static final int WARMTRANSFER = 7;
	public static final int WRAPUP = 8;
	public static final int LOGOUT = 9;
	public static final int LIMBO = 10;
	
	public int state = Agent.LIMBO;
	
	public String id = "null";
	public String login = "null";
	public String profile = "null";
	public String[] nodes = new String[0];
	public String[][] skills = new String[0][0];
	public double lastChange = 0;

	public String release_id = "null";
	public String release_label = "null";
	public int release_bias = 2;
	
	Call call = null;
	
	boolean collapsed = false;
	int collapsedHeight = 2;
	
	public Agent(String agent_id, String agent_login, String profile,
			String[] nodes, String[][] skills) 
	{
		this.id = agent_id;
		this.login = agent_login;
		this.state = Agent.LOGIN;
		this.profile = profile;
		this.nodes = nodes;
		this.skills = skills;
		this.setState(Agent.LOGIN);
	}
	
	public Agent(){}

	public void setState(int state)
	{
		this.state = state;
		this.lastChange = System.nanoTime();
//		updateCallState();
	}
	
	public void setState(String state)
	{
		this.lastChange = System.nanoTime();
		for(int i = 0; i < Agent.AGENT_STATES.length; i++)
			if(state.equalsIgnoreCase(Agent.AGENT_STATES[i]))
			{	
				this.state = i;
//				updateCallState();
				return;
			}
		this.state = Agent.LIMBO;
	}
	
	public static int getStateEnum(String state)
	{
		for(int i = 0; i < Agent.AGENT_STATES.length; i++)
			if(state.equalsIgnoreCase(Agent.AGENT_STATES[i]))
			{	
				return i;
			}
		return Agent.LIMBO;
	}
	
	private void updateCallState()
	{
		if(this.state == Agent.IDLE)
		{
			if(call != null)
			{
				if(call.agent != null)
				{
					call.agent = null;
				}
				call = null;
			}
		}
	}
	
	public void setReleased(String release_id, String release_label, int release_bias)
	{
		this.release_id = release_id;
		this.release_label = release_label;
		this.release_bias = release_bias;
		this.setState(Agent.RELEASED);
	}
	
	public String getSkillsString()
	{
		String skillsString = "[";
		for(int i = 0; i < skills.length; i++)
		{
			skillsString+="[";
			for(int j = 0; j < skills[i].length; j++)
				if(j != skills[i].length-1)
					skillsString+=skills[i][j]+",";
				else
					skillsString+=skills[i][j]+"]";
		}
		skillsString += "]";
		return skillsString;
	}

	public String getFancySkillsString()
	{
		String skillsString = "";
		for(int i = 0; i < skills.length; i++)
		{
			if(!((skills[i].length == 1 && (skills[i][0].charAt(0)+"").equalsIgnoreCase("_")) ||
			      (skills[i].length == 2 && skills[i][0].equalsIgnoreCase("_agent")) || 
			      (skills[i].length == 2 && skills[i][0].equalsIgnoreCase("_node")) ||
			      (skills[i].length == 2 && skills[i][0].equalsIgnoreCase("_profile"))))
			{	
				if(skillsString.length() != 0)
					if(skills[i].length != 1)
						skillsString+=", " + skills[i][1];
					else
						skillsString+=", " + skills[i][0];
				else
				{
					if(skills[i].length != 1)
						skillsString+=skills[i][1];
					else
						skillsString+=skills[i][0];
				}
			}
		}
		skillsString += "";
		return skillsString;
	}
	
	public String getReleaseLabel()
	{
		if(this.state != Agent.RELEASED)
			return "NULL";
		else
			return this.release_label;
	}
	
	public String getState()
	{
		String skillsString = getSkillsString();
		
		String nodesString = "[";
		for(int i = 0; i < this.nodes.length; i++)
			if(i != this.nodes.length-1)
				nodesString += this.nodes[i]+",";
			else
				nodesString += this.nodes[i];
		nodesString += "]";
		
		String callString;
		if(this.call != null)
			callString = "\n\t[\n     "+this.call.getState().replaceAll("\n", "\n     ") + "\n\t]";
		else
			callString = "null";
		
		return "id="+this.id+"\n"+
			   "agent="+this.login+"\n"+
			   "state="+Agent.AGENT_STATES[this.state]+"\n"+
			   "profile="+this.profile+"\n"+
			   "skills="+skillsString+"\n"+
			   "nodes="+nodesString+"\n"+
			   "state_duration="+(System.nanoTime()-this.lastChange)/1E9+"\n"+
			   "release_data=["+this.release_id+","+this.release_label+","+this.release_bias+"]"+"\n"+
			   "call="+callString;
	}

	public static double halfWidthRatio = .8;
	
	public static float padding;
	public static float agentStateWidth;
	public static float agentLoginWidth;
	public static float agentSkillsWidth;
	public static float agentLabelWidth;
	public static float agentDurationWidth;
	
	public static String defaultAgentState = "warmtransfer";
	public static String defaultAgentLogin = "wolfgang_wesson";
	public static String defaultAgentSkills = "[queue, default_queue][_node][_english]";
	public static String defaultAgentLabel = "On The Phone";
	public static String defaultAgentDuration = "123456";

	public static int agentGridLength;

	static boolean showAgentState = true;
	static boolean showAgentLogin = true;
	static boolean showAgentSkills = true;
	static boolean showAgentLabel = true;
	static boolean showAgentDuration = true;
	
	public synchronized static void setGridSize()
	{
		float callGridGap = (float) ((GraphicalMain.RENDER_WIDTH/2.0 - GraphicalMain.RENDER_WIDTH/2.0*halfWidthRatio)/2);
		agentGridLength = (int) (GraphicalMain.RENDER_WIDTH/2.0 - callGridGap*2);
		padding = (float) (GraphicalMain.fontSize/2.0);

		float agentStateWeight = showAgentState?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultAgentState).getWidth()):0;
		float agentLoginWeight = showAgentLogin?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultAgentLogin).getWidth()):0;
		float agentSkillsWeight = showAgentSkills?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultAgentSkills).getWidth()):0;
		float agentLabelWeight = showAgentLabel?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultAgentLabel).getWidth()):0;
		float agentDurationWeight = showAgentDuration?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultAgentDuration).getWidth()):0;
		
		float totalWeight = agentStateWeight + agentLoginWeight + agentSkillsWeight + agentLabelWeight + agentDurationWeight;

		agentStateWidth = (int) (agentGridLength*((double)agentStateWeight/(double)totalWeight));
		agentLoginWidth = (int) (agentGridLength*((double)agentLoginWeight/(double)totalWeight));
		agentSkillsWidth = (int) (agentGridLength*((double)agentSkillsWeight/(double)totalWeight));
		agentLabelWidth = (int) (agentGridLength*((double)agentLabelWeight/(double)totalWeight));
		agentDurationWidth = (int) (agentGridLength*((double)agentDurationWeight/(double)totalWeight));
	}

	private static String maxRender(String string, int width)
	{
		if(string == null)
			return maxRender("NULL", width);
		
		String tempString = "";
		
		for(int i = 0; i < string.length(); i++)
		{
			if(GraphicalMain.textRenderer.getBounds(tempString + string.charAt(i)).getWidth() > width)
				return tempString;
			else
				tempString += string.charAt(i);
		}
		return tempString;
	}

	public static void renderLabels(GL gl, int verticalOffset)
	{
		int xStartPos = (int) (GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-14) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-14);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset + GraphicalMain.fontSize + Agent.padding*2, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset + GraphicalMain.fontSize + Agent.padding*2, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();
		
		gl.glColor3f(1,1,1);

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset + GraphicalMain.fontSize + Agent.padding*2, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset + GraphicalMain.fontSize + Agent.padding*2, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();


		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-8) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-8);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-9) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-9);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-10) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);

        gl.glPushName(GraphicalMain.selectBufferSize-10);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-11) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-11);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-12) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);

        gl.glPushName(GraphicalMain.selectBufferSize-12);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		GraphicalMain.textRenderer.begin3DRendering();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-14) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender("Agents", (int) ((int) Agent.agentGridLength-Agent.padding*2)),	 	(float) (xStartPos+Agent.agentGridLength/2-GraphicalMain.textRenderer.getBounds("Agents").getWidth()/2), (int) (GraphicalMain.RENDER_HEIGHT+Agent.padding+GraphicalMain.user.scroll+verticalOffset), 0, 1);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-8) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender(Agent.getSortOrderString(Agent.agentState) + "State", (int) ((int) Agent.agentStateWidth-Agent.padding*2)),	 	xStartPos+Agent.padding, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll+verticalOffset), 0, 1);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-9) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender(Agent.getSortOrderString(Agent.agentLogin) + "Name", (int) ((int) Agent.agentLoginWidth-Agent.padding*2)),		xStartPos+Agent.padding+Agent.agentStateWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-10) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender(Agent.getSortOrderString(Agent.agentSkills) + "Skills", (int) ((int) Agent.agentSkillsWidth-Agent.padding*2)), 	xStartPos+Agent.padding+Agent.agentStateWidth+Agent.agentLoginWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-11) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender(Agent.getSortOrderString(Agent.agentRelease) + "Release", (int) ((int) Agent.agentLabelWidth-Agent.padding*2)), 	xStartPos+Agent.padding+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-12) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender(Agent.getSortOrderString(Agent.agentDuration) + "Time", (int) ((int) Agent.agentDurationWidth-Agent.padding*2)),	xStartPos+Agent.padding+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);

		GraphicalMain.textRenderer.flush();
		
		GraphicalMain.textRenderer.end3DRendering();
	}

	private void setColor(GL gl, float scale)
	{
		if(this.state == Agent.IDLE)
			gl.glColor3f(1*(scale+.2f),0,0);
		else if(this.state == Agent.ONCALL)
			gl.glColor3f(0,1*scale,0);
		else if(this.state == Agent.WRAPUP)
			gl.glColor3f(1*scale,1*scale,0);
		else if(this.state == Agent.RELEASED)
			gl.glColor3f(.1f*scale,.1f*scale,1.5f*scale);
		else
			gl.glColor3f(1*scale,1*scale,1*scale);
	}
	
	public synchronized void draw(GL gl, int newY, boolean mouseOver)
	{
		targetY = newY; 
		if(GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY < 0 ||
		   GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY > GraphicalMain.RENDER_HEIGHT)
		{
			return;
		}
		else if(this.collapsed)
		{
			int xStartPos = (int)currentX; //(int) (GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding);
			setColor(gl, 1);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentGridLength,  GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentGridLength,  GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY-this.collapsedHeight, 0);
			gl.glVertex3d(xStartPos,  					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY-this.collapsedHeight, 0);
			gl.glEnd();
		}
		else
		{
			int xStartPos = (int)currentX; //(int) (GraphicalMain.RENDER_WIDTH - Agent.agentGridLength - Agent.padding);

			if(mouseOver)
				setColor(gl, .25f);
			else
				gl.glColor3f(0,0,0);
			
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
			gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
			gl.glEnd();
			
			if(this.state == Agent.IDLE)
				gl.glColor3f(1,0,0);
			else if(this.state == Agent.ONCALL)
				gl.glColor3f(0,1,0);
			else if(this.state == Agent.WRAPUP)
				gl.glColor3f(1,1,0);
			else if(this.state == Agent.RELEASED)
				gl.glColor3f(0.0f,0.0f,1);
			else
				gl.glColor3f(1,1,1);

			if(this.state == Agent.WRAPUP)
			{
				gl.glEnable(GL.GL_LINE_STIPPLE);
				gl.glLineStipple(OpenACD.stippleFactor, OpenACD.stippleB);
				gl.glEnable(GL.GL_LINE_SMOOTH);
				gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
			
			gl.glLineWidth(2.0f);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
			gl.glVertex3d(xStartPos, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
			gl.glEnd();
			gl.glLineWidth(1.0f);
	
			gl.glBegin(GL.GL_LINES);
	
			gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
	
			gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
	
			gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
	
			gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+currentY, 0);
			gl.glVertex3d(xStartPos+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Agent.padding*2-GraphicalMain.fontSize+currentY, 0);
			
			
	//		gl.glVertex3d(0, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize-10, 0);/*
	//		gl.glVertex3d(callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize-10, 0);
	//		gl.glVertex3d(0, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize-20, 0);
	//		gl.glVertex3d(GraphicalMain.RENDER_WIDTH/2.0, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize-20, 0);*/
			
			
			gl.glEnd();

			if(this.state == Agent.WRAPUP)
			{
				gl.glDisable(GL.GL_LINE_STIPPLE);
				gl.glDisable(GL.GL_LINE_SMOOTH);
				gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
				gl.glDisable(GL.GL_BLEND);
			}

			if(mouseOver)
			{
				if(this.state == Agent.IDLE || this.state == Agent.ONCALL || this.state == Agent.WRAPUP || this.state == Agent.RELEASED)
					GraphicalMain.textRenderer.setColor(1,1,1, 1);
				else
					GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
			}
			else
				GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
			
			GraphicalMain.textRenderer.begin3DRendering();
	
			GraphicalMain.textRenderer.draw3D(maxRender(Agent.AGENT_STATES[this.state], (int) ((int) Agent.agentStateWidth-Agent.padding*2)),	 				xStartPos+Agent.padding, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll+currentY), 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.login, (int) ((int) Agent.agentLoginWidth-Agent.padding*2)),										xStartPos+Agent.padding+Agent.agentStateWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+currentY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.getFancySkillsString(), (int) ((int) Agent.agentSkillsWidth-Agent.padding*2)), 					xStartPos+Agent.padding+Agent.agentStateWidth+Agent.agentLoginWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+currentY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.getReleaseLabel(), (int) ((int) Agent.agentLabelWidth-Agent.padding*2)), 							xStartPos+Agent.padding+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+currentY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(""+(this.getStateDuration()), (int) ((int) Agent.agentDurationWidth-Agent.padding*2)),	xStartPos+Agent.padding+Agent.agentStateWidth+Agent.agentLoginWidth+Agent.agentSkillsWidth+Agent.agentLabelWidth, (int) (GraphicalMain.RENDER_HEIGHT-Agent.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+currentY, 0, 1);
	
			GraphicalMain.textRenderer.flush();
			
			GraphicalMain.textRenderer.end3DRendering();
		}
	}
	
	public void end()
	{
		this.targetX = GraphicalMain.RENDER_WIDTH+Agent.agentGridLength*1.25f;
		this.ending = true;
		
		spring.springConstant /= 2.0;
		
		try
		{
			new Thread(new Runnable()
			{
				public void run() 
				{
					try
					{
						while(currentX < GraphicalMain.RENDER_WIDTH)
						{
							Thread.sleep(100);
						}
						
						GraphicalMain.connection.acd.lock();
						if(ending)
							GraphicalMain.connection.acd.removeAgent(id);
						GraphicalMain.connection.acd.unlock();
						
					}
					catch(Exception e)
					{
						
					}
				}
				
				
			}).start();
		}
		catch(Exception e)
		{
			
		}
	}
	
	public String getStateDuration()
	{
		int seconds = (int) ((System.nanoTime()-this.lastChange)/1E9);
		String duration = null;
		if(seconds < 60)
			duration = seconds+"s";
		else if(seconds == 60)
			duration = "1m";
		else
			duration = seconds/60+"m"+seconds%60+"s";
		return duration;
	}
	
	public static Agent shallowClone (Agent agent_to_clone)
	{
		Agent agent = new Agent();
		
		agent.state = agent_to_clone.state;
		
		agent.id = agent_to_clone.id;
		agent.login = agent_to_clone.login;
		agent.profile = agent_to_clone.profile;
		agent.nodes = agent_to_clone.nodes;
		agent.lastChange = agent_to_clone.lastChange;

		agent.release_id = agent_to_clone.release_id;
		agent.release_label = agent_to_clone.release_label;
		agent.release_bias = agent_to_clone.release_bias;

		agent.skills = new String[agent_to_clone.skills.length][];
		for(int i = 0; i < agent_to_clone.skills.length; i++)
		{
			agent.skills[i] = new String[agent_to_clone.skills[i].length];
			for(int j = 0; j < agent_to_clone.skills[i].length; j++)
				agent.skills[i][j] = agent_to_clone.skills[i][j];
		}

		agent.collapsed = agent_to_clone.collapsed;

		agent.currentX = agent_to_clone.currentX;
		agent.currentY = agent_to_clone.currentY;

		agent.targetX = agent_to_clone.targetX;
		agent.targetY = agent_to_clone.targetY;
		
		return agent;
	}
	
	public Agent clone()
	{
		Agent agent = Agent.shallowClone(this);

		if(this.call != null)
			agent.call = this.call.clone(agent);
		
		return agent;
	}

	public Agent clone(Call call)
	{
		Agent agent = Agent.shallowClone(this);
		agent.call = call;
		
		return agent;
	}

	public static String currentSortField = "";
	
	public static int compareByField(Agent agentA, Agent agentB, String field)
	{
		if(field.equalsIgnoreCase(Agent.agentState))
		{
			if(agentA.state < agentB.state)
				return -1;
			else if(agentA.state > agentB.state)
				return 1;
		}
		else if(field.equalsIgnoreCase(Agent.agentLogin))
		{
			return agentA.login.compareTo(agentB.login);
		}
		else if(field.equalsIgnoreCase(Agent.agentSkills))
		{
			String thisSkills = agentA.getSkillsString();
			String thatSkills = agentB.getSkillsString();
			return thisSkills.compareTo(thatSkills);
		}
		else if(field.equalsIgnoreCase(Agent.agentRelease))
		{
			String thisRelease = agentA.getReleaseLabel();
			String thatRelease = agentB.getReleaseLabel();
			if(thisRelease.compareTo(thatRelease) != 0)
				return thisRelease.compareTo(thatRelease);
		}
		else if(field.equalsIgnoreCase(Agent.agentDuration))
		{
			if(agentA.lastChange != agentB.lastChange)
			{
				if(agentA.lastChange < agentB.lastChange)
					return -1;
				else 
					return 1;
			}
		}
		
		return 0;
	}

	public int compareTo(Agent otherAgent) 
	{
		return Agent.compareByField(this, otherAgent, Agent.currentSortField);
	}
}
