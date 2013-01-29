import java.awt.CheckboxMenuItem;
import java.awt.Menu;

import javax.media.opengl.GL;



public class Call implements Comparable<Call>
{
	/////////////////////////
	//Animation Information//
	/////////////////////////
	
	//This is for animation purposes. Without a steady change in position, 
	//the Call graphics will warp, making it difficult, if not, impossible, to read.
	
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
	
	static int totalSortingFields = 7;
	
    static String callState = "Call State ";
    static String callName = "Caller ID (Name) ";
    static String callData = "Caller ID (Data) ";
    static String callDNIS = "Call DNIS ";
    static String callQueue = "Call Queue ";
    static String callDuration = "Call State Duration ";
    static String callPriority = "Call Priority ";

    static String[] callSortingOrder = new String[0];
    
    static Menu callSortingSubmenu = new Menu("Call Sorting ");

    static String callSortingEnabled = "Enable Call Sorting";
    static CheckboxMenuItem callSortingEnabledItem = new CheckboxMenuItem(callSortingEnabled);
    
    static Menu callStateSortMenu = new Menu(Call.callState);
    static Menu callNameSortMenu = new Menu(Call.callName);
    static Menu callDataSortMenu = new Menu(Call.callData);
    static Menu callDNISSortMenu = new Menu(Call.callDNIS);
    static Menu callQueueSortMenu = new Menu(Call.callQueue);
    static Menu callDurationSortMenu = new Menu(Call.callDuration);
    static Menu callPrioritySortMenu = new Menu(Call.callPriority);
	
    public static synchronized void removeSortOption(String option)
    {
    	int index = -1;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    	{
    		if(Call.callSortingOrder[i].equalsIgnoreCase(option))
    			index = i;
    	}
    	if(index != -1)
    	{
    		String[] newSortingOrder = new String[Call.callSortingOrder.length-1];
    		int j = 0;
    		for(int i = 0; i < Call.callSortingOrder.length; i++)
    		{
    			if(i != index)
    			{
    				newSortingOrder[j] = Call.callSortingOrder[i];
    				j++;
    			}
    		}
    		Call.callSortingOrder = newSortingOrder;
    	}
    	Call.loadUISort(GraphicalMain.self);
    }
    
    public static void setSortingMenu(Menu parent, int max, int target, GraphicalMain itemListener)
    {
    	parent.removeAll();
        for(int i = 0; i < max; i++)
        {
        	CheckboxMenuItem item = new CheckboxMenuItem(""+(i+1)+Call.getOrdinalFor(i+1));
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
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callState))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callStateSortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callStateSortMenu, Call.callSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callName))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callNameSortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callNameSortMenu, Call.callSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callData))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callDataSortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callDataSortMenu, Call.callSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callDNIS))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callDNISSortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callDNISSortMenu, Call.callSortingOrder.length+1, -1, itemListener);
    	
    	found = false;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callQueue))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callQueueSortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callQueueSortMenu, Call.callSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callDuration))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callDurationSortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callDurationSortMenu, Call.callSortingOrder.length+1, -1, itemListener);

    	found = false;
    	for(int i = 0; i < Call.callSortingOrder.length; i++)
    		if(Call.callSortingOrder[i].equalsIgnoreCase(Call.callPriority))
    		{
    			found = true;
    			Call.setSortingMenu(Call.callPrioritySortMenu, Call.callSortingOrder.length, i+1, itemListener);
    		}
    	if(!found)
			Call.setSortingMenu(Call.callPrioritySortMenu, Call.callSortingOrder.length+1, -1, itemListener);
    	
    	
    	GraphicalMain.connection.acd.lock();
    	GraphicalMain.connection.acd.sortCalls();
    	GraphicalMain.connection.acd.unlock();
    }
    
    public static int getSortOrder(String field)
    {
    	String[] tempOrder = Call.callSortingOrder.clone();
    	
    	for(int i = 0; i < tempOrder.length; i++)
    	{
    		if(field.equalsIgnoreCase(tempOrder[i]))
    			return i;
    	}
    	return -1;
    }
    
    public static String getSortOrderString(String field)
    {
    	int order = Call.getSortOrder(field);
    	if(order == -1)
    		return "";
    	else
    		return "(" + (order + 1) + ") ";
    }
    
	//////////////////////////////
	//Internal state information//
	//////////////////////////////
	public static final String[] CALL_STATES = new String[] { "LIMBO", "INIVR", "INQUEUE","RINGING", "RINGOUT", "PRECALL", "ONCALL", "OUTGOING", "HANGUP" };
	public static int LIMBO = 0;
	public static int INIVR = 1;
	public static int INQUEUE = 2;
	public static int RINGING = 3;
	public static int RINGOUT = 4;
	public static int PRECALL = 5;
	public static int ONCALL = 6;
	public static int OUTGOING = 7;
	public static int HANGUP = 8;
	
	public int state = 0;
	
	public String id = "null";
	public String type = "null";
	public String caller_id_name = "null";
	public String caller_id_data = "null";
	public String call_dnis = "null";
	public boolean is_default_client = false;
	public String client_label = "null";
	public String client_id = "null";
	public String ring_path = "null";
	public String media_path = "null";
	public String direction = "null";
	public String[] nodes = new String[0];
	public String queue = "null";
	public String[][] skills = new String[0][0];
	public String priority = "null";
	
	Agent agent = null;
	
	double lastChange;

	boolean collapsed = false;
	int collapsedHeight = 2;
	
	public void setState(int state)
	{
		this.state = state;
		lastChange = System.nanoTime();
	}
	
	public String getState()
	{
		String nodesString = "[";
		for(int i = 0; i < this.nodes.length; i++)
			if(i != this.nodes.length-1)
				nodesString += this.nodes[i]+",";
			else
				nodesString += this.nodes[i];
		nodesString += "]";
		
		String agentString;
		if(this.agent == null)
			agentString = "null";
		else
			agentString = "["+this.agent.id+","+this.agent.login+"]";
		
		return "id="+this.id+"\n"+
			   "state="+Call.CALL_STATES[this.state]+"\n"+
			   "type="+this.type+"\n"+
			   "caller_id_name="+this.caller_id_name+"\n"+
			   "caller_id_data="+this.caller_id_data+"\n"+
			   "call_dnis="+this.call_dnis+"\n"+
			   "is_default_client="+this.is_default_client+"\n"+
			   "client_label="+this.client_label+"\n"+
			   "client_id="+this.client_id+"\n"+
			   "ring_path="+this.ring_path+"\n"+
			   "media_path="+this.media_path+"\n"+
			   "direction="+this.direction+"\n"+
			   "nodes="+nodesString+"\n"+
			   "queue="+this.queue+"\n"+
			   "agent="+agentString+"\n"+
			   "state_duration="+(System.nanoTime()-this.lastChange)/1E9+"\n"+
			   "priority="+this.priority;
	}

	public static double halfWidthRatio = .8;
	
	public static float padding;
	public static float callStateWidth;
	public static float callNameWidth;
	public static float callDataWidth;
	public static float callDNISWidth;
	public static float callQueueWidth;
	public static float callDurationWidth;
	public static float callPriorityWidth;
	
	public static String defaultCallState = "OUTGOING";
	public static String defaultCallName = "Unknown ID";
	public static String defaultCallData = "Unknown ID";
	public static String defaultCallDNIS = "18001234567";
	public static String defaultCallQueue = "default_queue_2";
	public static String defaultCallDuration = "123456";
	public static String defaultCallPriority = "Priority";
	
	public static int callGridLength;
	
	static boolean showCallState = true;
	static boolean showCallName = true;
	static boolean showCallData = true;
	static boolean showCallDNIS = true;
	static boolean showCallQueue = true;
	static boolean showCallDuration = true;
	static boolean showCallPriority = true;
	
	public synchronized static void setGridSize()
	{
		float callGridGap = (float) ((GraphicalMain.RENDER_WIDTH/2.0 - GraphicalMain.RENDER_WIDTH/2.0*halfWidthRatio)/2);
		callGridLength = (int) (GraphicalMain.RENDER_WIDTH/2.0 - callGridGap*2);
		padding = (float) (GraphicalMain.fontSize/2.0);
		
		float callStateWeight = showCallState?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallState).getWidth()):0;
		float callNameWeight = showCallName?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallName).getWidth()):0;
		float callDataWeight = showCallData?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallData).getWidth()):0;
		float callDNISWeight = showCallDNIS?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallDNIS).getWidth()):0;
		float callQueueWeight = showCallQueue?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallQueue).getWidth()):0;
		float callDurationWeight = showCallDuration?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallDuration).getWidth()):0;
		float callPriorityWeight = showCallPriority?(float) (padding*2 + GraphicalMain.textRenderer.getBounds(defaultCallPriority).getWidth()):0;
		
		float totalWeight = callStateWeight + callNameWeight + callDataWeight + callDNISWeight + callQueueWeight + callDurationWeight + callPriorityWeight;

		callStateWidth = (int) (callGridLength*((double)callStateWeight/(double)totalWeight));
		callNameWidth = (int) (callGridLength*((double)callNameWeight/(double)totalWeight));
		callDataWidth = (int) (callGridLength*((double)callDataWeight/(double)totalWeight));
		callDNISWidth = (int) (callGridLength*((double)callDNISWeight/(double)totalWeight));
		callQueueWidth = (int) (callGridLength*((double)callQueueWeight/(double)totalWeight));
		callDurationWidth = (int) (callGridLength*((double)callDurationWeight/(double)totalWeight));
		callPriorityWidth = (int) (callGridLength*((double)callPriorityWeight/(double)totalWeight));
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
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-13) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-13);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+Call.padding*2+GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+Call.padding*2+GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();
		
		gl.glColor3f(1,1,1);
		
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+Call.padding*2+GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+Call.padding*2+GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-1) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
			
        gl.glPushName(GraphicalMain.selectBufferSize-1);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					 GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2, 					GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-2) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-2);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glEnd();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-3) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);

        gl.glPushName(GraphicalMain.selectBufferSize-3);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-4) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-4);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-5) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);

        gl.glPushName(GraphicalMain.selectBufferSize-5);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-6) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);

        gl.glPushName(GraphicalMain.selectBufferSize-6);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		
		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-7) 
			gl.glColor4f(.5f,.5f,.5f,1);
		else gl.glColor4f(0,0,0,1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-7);
        gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();
		gl.glPopName();

		gl.glColor4f(1,1,1,1);
        gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll-Call.padding*2-GraphicalMain.fontSize+verticalOffset, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2+Call.callGridLength, GraphicalMain.RENDER_HEIGHT+(int) GraphicalMain.user.scroll+verticalOffset, 0);
		gl.glEnd();

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		GraphicalMain.textRenderer.begin3DRendering();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-13) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
		GraphicalMain.textRenderer.draw3D(maxRender("Calls", (int) ((int) callGridLength-Call.padding*2.5)),	 	(float) ((callGridLength-Call.padding*2)/2+Call.padding-GraphicalMain.textRenderer.getBounds("Calls").getWidth()/2), (int) (GraphicalMain.RENDER_HEIGHT-Call.padding+GraphicalMain.fontSize+GraphicalMain.user.scroll+verticalOffset), 0, 1);

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-1) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-1);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callState) + "State", (int) ((int) Call.callStateWidth-Call.padding*2)),	 	GraphicalMain.fontSize/2+Call.padding, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll+verticalOffset), 0, 1);
		gl.glPopName();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-2) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		
        gl.glPushName(GraphicalMain.selectBufferSize-2);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callName) + "Call Name", (int) ((int) Call.callNameWidth-Call.padding*2)), 	GraphicalMain.fontSize/2+Call.padding+Call.callStateWidth, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);
		gl.glPopName();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-3) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);

        gl.glPushName(GraphicalMain.selectBufferSize-3);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callData) + "Call Data", (int) ((int) Call.callDataWidth-Call.padding*2)),	GraphicalMain.fontSize/2+Call.padding+Call.callStateWidth+Call.callNameWidth, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);
		gl.glPopName();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-4) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);

        gl.glPushName(GraphicalMain.selectBufferSize-4);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callDNIS) + "DNIS", (int) ((int) Call.callDNISWidth-Call.padding*2)), 		GraphicalMain.fontSize/2+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);
		gl.glPopName();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-5) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);

        gl.glPushName(GraphicalMain.selectBufferSize-5);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callQueue) + "Queue", (int) ((int) Call.callQueueWidth-Call.padding*2)), 	GraphicalMain.fontSize/2+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);
		gl.glPopName();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-6) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);

        gl.glPushName(GraphicalMain.selectBufferSize-6);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callDuration) + "Time", (int) ((int) Call.callDurationWidth-Call.padding*2)),	GraphicalMain.fontSize/2+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);
		gl.glPopName();

		if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-7) 
			GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
		else 
			GraphicalMain.textRenderer.setColor(1, 1, 1, 1);

        gl.glPushName(GraphicalMain.selectBufferSize-7);
		GraphicalMain.textRenderer.draw3D(maxRender(Call.getSortOrderString(Call.callPriority) + "Priority", (int) ((int) Call.callPriorityWidth-Call.padding*2)),	GraphicalMain.fontSize/2+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, (int) (GraphicalMain.RENDER_HEIGHT-Call.padding-GraphicalMain.fontSize+GraphicalMain.user.scroll)+verticalOffset, 0, 1);
		gl.glPopName();

		GraphicalMain.textRenderer.flush();
		
		GraphicalMain.textRenderer.end3DRendering();
	}
	
	private void setColor(GL gl, float scale)
	{
		if(this.state == Call.INQUEUE)
			gl.glColor3f(1*(scale+.2f),0,0);
		else if(this.state == Call.ONCALL)
			gl.glColor3f(0,1*scale,0);
		else if(this.state == Call.HANGUP)
			gl.glColor3f(1*scale,1*scale,0);
		else
			gl.glColor3f(1*scale,1*scale,1*scale);
	}
	
	public synchronized void draw(GL gl, int newY, boolean mouseOver)
	{
		int tempY = (int) (currentY+GraphicalMain.user.scroll);
		
		if(tempY < 0 || 
		   -Call.padding*2-GraphicalMain.fontSize+tempY > GraphicalMain.RENDER_HEIGHT)
		{
			return;
		}
		else if(this.collapsed)
		{
			setColor(gl, 1);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(currentX, 					 tempY, 0);
			gl.glVertex3d(currentX+Call.callGridLength,  tempY, 0);
			gl.glVertex3d(currentX+Call.callGridLength,  tempY-this.collapsedHeight, 0);
			gl.glVertex3d(currentX,  					 tempY-this.collapsedHeight, 0);
			gl.glEnd();
		}
		else
		{
			if(mouseOver)
				setColor(gl, .25f);
			else
				gl.glColor3f(0,0,0);
			
			if(this.state == Call.HANGUP)
			{
				gl.glEnable(GL.GL_LINE_STIPPLE);
				gl.glLineStipple(OpenACD.stippleFactor, OpenACD.stippleB);
				gl.glEnable(GL.GL_LINE_SMOOTH);
				gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
				
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(currentX, 					 tempY, 0);
			gl.glVertex3d(currentX+Call.callGridLength,  tempY, 0);
			gl.glVertex3d(currentX+Call.callGridLength,  -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
			gl.glVertex3d(currentX, 					 -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
			gl.glEnd();

			setColor(gl, 1);
			
			gl.glLineWidth(2.0f);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3d(currentX, 					tempY, 0);
			gl.glVertex3d(currentX+Call.callGridLength, tempY, 0);
			gl.glVertex3d(currentX+Call.callGridLength, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
			gl.glVertex3d(currentX, 					-Call.padding*2-GraphicalMain.fontSize+tempY, 0);
			gl.glEnd();
			gl.glLineWidth(1.0f);
	
			gl.glBegin(GL.GL_LINES);
	
			gl.glVertex3d(currentX+Call.callStateWidth, tempY, 0);
			gl.glVertex3d(currentX+Call.callStateWidth, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
	
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth, tempY, 0);
			gl.glVertex3d(currentX+Call.callStateWidth+callNameWidth, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
	
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, tempY, 0);
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
	
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, tempY, 0);
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
	
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, tempY, 0);
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
	
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callPriorityWidth, tempY, 0);
			gl.glVertex3d(currentX+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callPriorityWidth, -Call.padding*2-GraphicalMain.fontSize+tempY, 0);
			
	//		gl.glVertex3d(0, -Call.padding*2-GraphicalMain.fontSize-10, 0);/*
	//		gl.glVertex3d(callGridLength, -Call.padding*2-GraphicalMain.fontSize-10, 0);
	//		gl.glVertex3d(0, -Call.padding*2-GraphicalMain.fontSize-20, 0);
	//		gl.glVertex3d(GraphicalMain.RENDER_WIDTH/2.0, -Call.padding*2-GraphicalMain.fontSize-20, 0);*/
			
			
			gl.glEnd();

			if(this.state == Call.HANGUP)
			{
				gl.glDisable(GL.GL_LINE_STIPPLE);
				gl.glDisable(GL.GL_LINE_SMOOTH);
				gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
				gl.glDisable(GL.GL_BLEND);
			}
			
			
			if(mouseOver)
			{
				if(this.state == Call.INQUEUE || this.state == Call.ONCALL || this.state == Call.HANGUP)
					GraphicalMain.textRenderer.setColor(1,1,1, 1);
				else
					GraphicalMain.textRenderer.setColor(0, 0, 0, 1);
			}
			else
				GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
			
			GraphicalMain.textRenderer.begin3DRendering();
	
			GraphicalMain.textRenderer.draw3D(maxRender(Call.CALL_STATES[this.state], (int) ((int) Call.callStateWidth-Call.padding*2)),	 				currentX+Call.padding, (int) (-Call.padding-GraphicalMain.fontSize+tempY), 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.caller_id_name, (int) ((int) Call.callNameWidth-Call.padding*2)), 								currentX+Call.padding+Call.callStateWidth, (int) (-Call.padding-GraphicalMain.fontSize)+tempY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.caller_id_data, (int) ((int) Call.callDataWidth-Call.padding*2)),								currentX+Call.padding+Call.callStateWidth+Call.callNameWidth, (int) (-Call.padding-GraphicalMain.fontSize)+tempY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.call_dnis, (int) ((int) Call.callDNISWidth-Call.padding*2)), 									currentX+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth, (int) (-Call.padding-GraphicalMain.fontSize)+tempY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.queue, (int) ((int) Call.callQueueWidth-Call.padding*2)), 										currentX+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth, (int) (-Call.padding-GraphicalMain.fontSize)+tempY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(""+(this.getStateDuration()), (int) ((int) Call.callDurationWidth-Call.padding*2)),					currentX+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth, (int) (-Call.padding-GraphicalMain.fontSize)+tempY, 0, 1);
			GraphicalMain.textRenderer.draw3D(maxRender(this.priority, (int) ((int) Call.callPriorityWidth-Call.padding*2)),								currentX+Call.padding+Call.callStateWidth+Call.callNameWidth+Call.callDataWidth+Call.callDNISWidth+Call.callQueueWidth+Call.callDurationWidth, (int) (-Call.padding-GraphicalMain.fontSize)+tempY, 0, 1);
	
			GraphicalMain.textRenderer.flush();
			
			GraphicalMain.textRenderer.end3DRendering();
		}
	}
	
	public void end()
	{
		this.targetX = -callGridLength*1.25f;
		this.agent = null;
		
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
						while(currentX > -callGridLength)
						{
							Thread.sleep(300);
							currentX--;
						}
						
						GraphicalMain.connection.acd.lock();
						GraphicalMain.connection.acd.removeCall(id);
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
	
	public static Call shallowClone(Call call_to_clone)
	{
		Call call = new Call();
		call.state = call_to_clone.state;
		
		call.id = call_to_clone.id;
		call.type = call_to_clone.type;
		call.caller_id_name = call_to_clone.caller_id_name;
		call.caller_id_data = call_to_clone.caller_id_data;
		call.call_dnis = call_to_clone.call_dnis;
		call.is_default_client = call_to_clone.is_default_client;
		call.client_label = call_to_clone.client_label;
		call.client_id = call_to_clone.client_id;
		call.ring_path = call_to_clone.ring_path;
		call.media_path = call_to_clone.media_path;
		call.direction = call_to_clone.direction;
		call.queue = call_to_clone.queue;
		call.lastChange = call_to_clone.lastChange;
		
		call.nodes = new String[call_to_clone.nodes.length];
		for(int i = 0; i < call_to_clone.nodes.length; i++)
			call.nodes[i] = call_to_clone.nodes[i];
		
		call.skills = new String[call_to_clone.skills.length][];
		for(int i = 0; i < call_to_clone.skills.length; i++)
		{
			call.skills[i] = new String[call_to_clone.skills[i].length];
			for(int j = 0; j < call_to_clone.skills[i].length; j++)
				call.skills[i][j] = call_to_clone.skills[i][j];
		}
		
		call.priority = call_to_clone.priority;
		call.collapsed = call_to_clone.collapsed;

		call.currentX = call_to_clone.currentX;
		call.currentY = call_to_clone.currentY;

		call.targetX = call_to_clone.targetX;
		call.targetY = call_to_clone.targetY;
		
		return call;
	}
	
	public Call clone()
	{
		Call call = Call.shallowClone(this);
		
		if(this.agent != null)
			call.agent = this.agent.clone(call);
		
		return call;
	}
	
	public Call clone(Agent agent)
	{
		Call call = Call.shallowClone(this);
		
		call.agent = agent;
		
		return call;
	}

	public static String currentSortField = "";
	
	public static int compareByField(Call callA, Call callB, String field)
	{
		if(field.equalsIgnoreCase(Call.callState))
		{
			if(callA.state < callB.state)
				return -1;
			else if(callA.state > callB.state)
				return 1;
		}
		else if(field.equalsIgnoreCase(Call.callName))
		{
			int comparison = callA.caller_id_name.compareTo(callB.caller_id_name);
			if(comparison != 0)
				return comparison;
		}
		else if(field.equalsIgnoreCase(Call.callData))
		{
			int comparison = callA.caller_id_data.compareTo(callB.caller_id_data);
			if(comparison != 0)
				return comparison;
		}
		else if(field.equalsIgnoreCase(Call.callDNIS))
		{
//			System.out.println(callA+"\t"+callB+"\t"+callA.call_dnis+"\t"+callB.call_dnis);
			int comparison = callA.call_dnis.compareTo(callB.call_dnis);
			if(comparison != 0)
				return comparison;
		}
		else if(field.equalsIgnoreCase(Call.callQueue))
		{
			String queueA = ""+callA.queue;
			String queueB = ""+callB.queue;
			int comparison = queueA.compareTo(queueB);
			if(comparison != 0)
				return comparison;
		}
		else if(field.equalsIgnoreCase(Call.callDuration))
		{
			if(callA.lastChange != callB.lastChange)
			{
				if(callA.lastChange < callB.lastChange)
					return -1;
				else 
					return 1;
			}
		}
		else if(field.equalsIgnoreCase(Call.callPriority))
		{
			int priorityA = Integer.parseInt(callA.priority);
			int priorityB = Integer.parseInt(callB.priority);
			if(priorityA != priorityB)
			{
				if(priorityA < priorityB)
					return -1;
				else 
					return 1;
			}
			
			int comparison = callA.priority.compareTo(callB.priority);
			if(comparison != 0)
				return comparison;
		}
		
		return 0;
	}
	
	public int compareTo(Call otherCall) 
	{
		return Call.compareByField(this, otherCall, Call.currentSortField);
	}
}
