import javax.media.opengl.GL;


public class StatsPanel 
{
	public static String defaultAgentsTotal = "Total Agents: ";
	public static String defaultAgentsIdle = "Agents Idle: ";
	public static String defaultAgentsReleased = "Agents Released: ";
	public static String defaultAgentsOncall = "Agents Oncall: ";

	public static int defaultAgentsTotalWidth;
	public static int defaultAgentsIdleWidth;
	public static int defaultAgentsReleasedWidth;
	public static int defaultAgentsOncallWidth;
	
	public static String defaultCallsCurrentTotal = "Total Current Calls: ";
	public static String defaultCallsQueuedTotal = "Calls Inqueue: ";

	public static int defaultCallsCurrentTotalWidth;
	public static int defaultCallsQueuedTotalWidth;
	
	public static String defaultNumber = "12,345";
	public static int defaultNumberWidth;

	public static int agentsTotalWidth;
	public static int agentsIdleWidth;
	public static int agentsReleasedWidth;
	public static int agentsOncallWidth;
	public static int callsTotalWidth;
	public static int callsQueuedWidth;
	
	public static int statsPanelWidth;
	
	public static void setGridSize()
	{
		statsPanelWidth = GraphicalMain.RENDER_WIDTH-GraphicalMain.fontSize;
		
		defaultAgentsTotalWidth = (int) GraphicalMain.textRenderer.getBounds(defaultAgentsTotal).getWidth();
		defaultAgentsIdleWidth = (int) GraphicalMain.textRenderer.getBounds(defaultAgentsIdle).getWidth();
		defaultAgentsReleasedWidth = (int) GraphicalMain.textRenderer.getBounds(defaultAgentsReleased).getWidth();
		defaultAgentsOncallWidth = (int) GraphicalMain.textRenderer.getBounds(defaultAgentsOncall).getWidth();

		defaultCallsCurrentTotalWidth = (int) GraphicalMain.textRenderer.getBounds(defaultCallsCurrentTotal).getWidth();
		defaultCallsQueuedTotalWidth = (int) GraphicalMain.textRenderer.getBounds(defaultCallsQueuedTotal).getWidth();
		
		defaultNumberWidth = (int) GraphicalMain.textRenderer.getBounds(defaultNumber).getWidth();
		
		float agentsTotalWeight = (float) (GraphicalMain.fontSize + defaultAgentsTotalWidth);
		float agentsIdleWeight = (float) (GraphicalMain.fontSize + defaultAgentsIdleWidth);
		float agentsReleasedWeight = (float) (GraphicalMain.fontSize + defaultAgentsReleasedWidth);
		float agentsOncallWeight = (float) (GraphicalMain.fontSize + defaultAgentsOncallWidth);
		float callsTotalWeight = (float) (GraphicalMain.fontSize + defaultCallsCurrentTotalWidth);
		float callsQueuedWeight = (float) (GraphicalMain.fontSize + defaultCallsQueuedTotalWidth);
		
		float totalWeight = agentsTotalWeight + agentsIdleWeight + agentsReleasedWeight + agentsOncallWeight + callsTotalWeight + callsQueuedWeight;

		agentsTotalWidth = (int) (statsPanelWidth*((double)agentsTotalWeight/(double)totalWeight));
		agentsIdleWidth = (int) (statsPanelWidth*((double)agentsIdleWeight/(double)totalWeight));
		agentsReleasedWidth = (int) (statsPanelWidth*((double)agentsReleasedWeight/(double)totalWeight));
		agentsOncallWidth = (int) (statsPanelWidth*((double)agentsOncallWeight/(double)totalWeight));
		callsTotalWidth = (int) (statsPanelWidth*((double)callsTotalWeight/(double)totalWeight));
		callsQueuedWidth = (int) (statsPanelWidth*((double)callsQueuedWeight/(double)totalWeight));
	}
	
	private String maxRender(String string, int width)
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
	
	public void draw(GL gl, OpenACD openACD, int yOffset) 
	{
		int verticalOffset = (int) (GraphicalMain.user.scroll+yOffset);
		
		int startX = (int) (GraphicalMain.fontSize);
		int startY = (int) (GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize*2);
		
		gl.glColor3f(0, 0 ,0 );
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize/2.0, 0);
		gl.glVertex3d(GraphicalMain.RENDER_WIDTH-GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize/2.0, 0);
		gl.glVertex3d(GraphicalMain.RENDER_WIDTH-GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize*2.5, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize*2.5, 0);
		gl.glEnd();

		gl.glColor3f(1, 1 , 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize/2.0, 0);
		gl.glVertex3d(GraphicalMain.RENDER_WIDTH-GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize/2.0, 0);
		gl.glVertex3d(GraphicalMain.RENDER_WIDTH-GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize*2.5, 0);
		gl.glVertex3d(GraphicalMain.fontSize/2.0, verticalOffset+GraphicalMain.RENDER_HEIGHT-GraphicalMain.fontSize*2.5, 0);
		gl.glEnd();

		GraphicalMain.textRenderer.setColor(1, 1, 1, 1);
		GraphicalMain.textRenderer.begin3DRendering();
		
		int accumulatedX = startX;
		GraphicalMain.textRenderer.draw3D(maxRender(defaultAgentsTotal+openACD.bufferedAgents.length, agentsTotalWidth), accumulatedX, verticalOffset+startY, 0, 1);
		accumulatedX += agentsTotalWidth;

		int agentsIdle = 0;
		for(int i = 0; i < openACD.bufferedAgents.length; i++)
			if(openACD.bufferedAgents[i].state == Agent.IDLE)
				agentsIdle ++;
		
		GraphicalMain.textRenderer.draw3D(maxRender(defaultAgentsIdle+agentsIdle, agentsIdleWidth), accumulatedX, verticalOffset+startY, 0, 1);
		accumulatedX += agentsIdleWidth;

		int agentsReleased = 0;
		for(int i = 0; i < openACD.bufferedAgents.length; i++)
			if(openACD.bufferedAgents[i].state == Agent.RELEASED)
				agentsReleased ++;
		
		GraphicalMain.textRenderer.draw3D(maxRender(defaultAgentsReleased+agentsReleased, agentsReleasedWidth), accumulatedX, verticalOffset+startY, 0, 1);
		accumulatedX += agentsReleasedWidth;

		int agentsOncall = 0;
		for(int i = 0; i < openACD.bufferedAgents.length; i++)
			if(openACD.bufferedAgents[i].state == Agent.ONCALL)
				agentsOncall ++;
		
		GraphicalMain.textRenderer.draw3D(maxRender(defaultAgentsOncall + agentsOncall, agentsOncallWidth), accumulatedX, verticalOffset+startY, 0, 1);
		accumulatedX += agentsOncallWidth;
		
		GraphicalMain.textRenderer.draw3D(maxRender(defaultCallsCurrentTotal + openACD.bufferedCalls.length, callsTotalWidth), accumulatedX, verticalOffset+startY, 0, 1);
		accumulatedX += callsTotalWidth;

		int callsQueued = 0;
		for(int i = 0; i < openACD.bufferedCalls.length; i++)
			if(openACD.bufferedCalls[i].state == Call.INQUEUE || openACD.bufferedCalls[i].state == Call.INIVR)
				callsQueued ++;

		GraphicalMain.textRenderer.draw3D(maxRender(defaultCallsQueuedTotal + callsQueued, callsQueuedWidth), accumulatedX, verticalOffset+startY, 0, 1);
		accumulatedX += callsQueuedWidth;
		
		GraphicalMain.textRenderer.flush();
		
		GraphicalMain.textRenderer.end3DRendering();
	}
}
