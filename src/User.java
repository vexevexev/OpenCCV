

import java.awt.CheckboxMenuItem;
import java.awt.Cursor;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;



public class User implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	GLUT glut = new GLUT();
	
	public static int lastMouseX, lastMouseY;

	boolean leftMousePressed;
	boolean rightMousePressed;
	
	public double minScroll = -GraphicalMain.fontSize*8;
	public double scrollSpeed = 20f;
	public double scroll = minScroll;

	public double[] coordinates = new double[5];// x, y, z, rotLR, rotUD;rotation Left/Right & rotation Up/Down, respectively
	
	public Console console = new Console();
	
	public User()
	{
	}

	public void keyResponse(double timeStep)//value: 1 down 0 up; detail: key description;
	{
		if(console.typing||console.dragging||console.scrolling||console.resizing)
			return;
	}

	void mouseResponse()
	{
	}

	public void mouseWheelMoved(MouseWheelEvent e) 
	{		
		scroll += e.getWheelRotation()*scrollSpeed;
		if(scroll < minScroll)
			scroll = minScroll;
	}
	
	boolean dropDownEnabled = false;
	public void mouseDragged(MouseEvent e) 
	{
		if(dropDownEnabled)
		{
			dropDownEnabled = false;
			return;
		}
		else if(rightMousePressed)
		{
			return;
		}
		else
		{
			int mouseXmove = e.getX()-lastMouseX; 
			int mouseYmove = e.getY()-lastMouseY;
			console.handleDrag(e.getX(), e.getY());
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			
			if(!console.scrolling && !console.dragging && !console.resizing)
			{
				scroll -= mouseYmove;
				if(scroll < minScroll)
					scroll = minScroll;
			}
		}
	}

	public void mouseMoved(MouseEvent e) 
	{
		lastMouseX = e.getX();
		lastMouseY = e.getY();
	}

	public void keyPressed(KeyEvent e) 
	{
		int keyCode = e.getKeyCode();

		if(keyCode == KeyEvent.VK_ENTER && !console.typing)
			console.typing = true;
		else if(keyCode == KeyEvent.VK_SLASH && console.input.equalsIgnoreCase("") && !console.typing)
		{
			console.typing = true;
			console.carrotIndex = 1;
			console.carrot = glut.glutBitmapWidth(console.FONT_TYPE, '/');
			
		}
		else if(keyCode == KeyEvent.VK_ENTER && console.typing)
		{
			console.typing = false;
			String rawCommand = console.input;
			console.input = "client > " + console.input;
			console.append();
			processCommand(rawCommand);
		}
		else if(keyCode == KeyEvent.VK_BACK_SPACE && console.typing)
		{	
			if(console.carrotIndex > 0)
			{
				--console.carrotIndex;
				console.carrot -= glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(console.carrotIndex));
				console.input = console.input.substring(0, console.carrotIndex) + console.input.substring(console.carrotIndex+1);

				if(console.carrotIndex < console.startIndex)
					console.startIndex--;

				if(console.carrot < 0)
					console.carrot = 0;
			}
		}
		else if(keyCode == KeyEvent.VK_LEFT && console.typing)
		{
			if(console.carrotIndex > 0)
			{
				--console.carrotIndex;
				console.carrot -= glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(console.carrotIndex));
				if(console.carrot < 0)
					console.carrot = 0;
				if(console.carrotIndex < console.startIndex)
					console.startIndex--;
			}	
		}
		else if(keyCode == KeyEvent.VK_RIGHT && console.typing)
		{
			if(console.carrotIndex < console.input.length())
			{
				++console.carrotIndex;
				
				int width = 0;
				for(int i = console.startIndex; i < console.carrotIndex; i++)
					width += glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(i));
				
				while(width > console.maxLength)
				{
					width -= glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(console.startIndex));
					++console.startIndex;
				}
				
				console.carrot = 0;

				for(int i = console.startIndex; i < console.carrotIndex; i++)
					console.carrot += glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(i));
			}
		}
		else if(keyCode == KeyEvent.VK_HOME && console.typing)
		{
			console.startIndex = 0;
			console.carrotIndex = 0;
			console.carrot = 0;
		}
		else if(keyCode == KeyEvent.VK_END && console.typing)
		{
			console.startIndex = 0;
			console.carrotIndex = console.input.length();
			
			console.carrot = 0;
			int i = 0;
			for(i = console.input.length()-1; i >= 0; i--)
				if(console.carrot + glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(i)) > console.maxLength)
					break;
				else
					console.carrot += glut.glutBitmapWidth(console.FONT_TYPE, console.input.charAt(i));
			
			console.startIndex = i+1;
		}
		else if(console.typing)
		{
			if(console.isWritable(e.getKeyChar()))
				console.insert(e.getKeyChar());
		}
		else if(e.getKeyCode() == KeyEvent.VK_C && e.isControlDown())
			console.setVisible(!console.isVisible());
		else if(keyCode == KeyEvent.VK_F9)
		{
			GraphicalMain.dataView = (GraphicalMain.dataView+1)%GraphicalMain.NUMBER_OF_VIEWS;
		}
	}

	public void keyReleased(KeyEvent e) 
	{
	}

	public void keyTyped(KeyEvent e) 
	{		
	}

	public void mouseClicked(MouseEvent e) 
	{
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public static void handleCallSort(CheckboxMenuItem priority)
	{
		Menu parent = (Menu)priority.getParent();
		String parentName = parent.getLabel();
		
		boolean found = false;
		
		for(int i = 0; i < Call.callSortingOrder.length; i++)
			if(Call.callSortingOrder[i].equalsIgnoreCase(parentName))
				found = true;
		
		if(found)
		{
			String[] newList = new String[Call.callSortingOrder.length-1];
			int j = 0;
			for(int i = 0; i < Call.callSortingOrder.length; i++)
			{
				if(!Call.callSortingOrder[i].equalsIgnoreCase(parentName))
				{
					newList[j] = Call.callSortingOrder[i];
					j++;
				}
			}
			Call.callSortingOrder = newList;
		}
		else
		{
			String[] newList = new String[Call.callSortingOrder.length+1];

			for(int i = 0; i < Call.callSortingOrder.length; i++)
				newList[i] = Call.callSortingOrder[i];
			newList[Call.callSortingOrder.length] = parentName;
			Call.callSortingOrder = newList;
		}
		
		Call.loadUISort(GraphicalMain.self);
	}
		
	public static void handleAgentSort(CheckboxMenuItem priority)
	{
		Menu parent = (Menu)priority.getParent();
		String parentName = parent.getLabel();
		
		boolean found = false;
		
		for(int i = 0; i < Agent.agentSortingOrder.length; i++)
			if(Agent.agentSortingOrder[i].equalsIgnoreCase(parentName))
				found = true;
		
		if(found)
		{
			String[] newList = new String[Agent.agentSortingOrder.length-1];
			int j = 0;
			for(int i = 0; i < Agent.agentSortingOrder.length; i++)
			{
				if(!Agent.agentSortingOrder[i].equalsIgnoreCase(parentName))
				{
					newList[j] = Agent.agentSortingOrder[i];
					j++;
				}
			}
			Agent.agentSortingOrder = newList;
		}
		else
		{
			String[] newList = new String[Agent.agentSortingOrder.length+1];
			
			for(int i = 0; i < Agent.agentSortingOrder.length; i++)
				newList[i] = Agent.agentSortingOrder[i];
			newList[Agent.agentSortingOrder.length] = parentName;
			Agent.agentSortingOrder = newList;
		}
		
		Agent.loadUISort(GraphicalMain.self);
	}
	
	public static CheckboxMenuItem getFirstPriority(Menu field)
	{
		
		for(int i = 0; i < field.getItemCount(); i++)
		{
			CheckboxMenuItem item = (CheckboxMenuItem) field.getItem(i);
			if(item.isEnabled())
				return item;
		}
		return (CheckboxMenuItem) field.getItem(0);
	}
	
	Agent agentSelected = null;
	Call callSelected = null;
	
	public void mousePressed(MouseEvent e) 
	{
		lastMouseX = e.getX();
		lastMouseY = e.getY();
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			leftMousePressed = true;
			if(console.isVisible())
				console.handleLeftClick(e.getX(), GraphicalMain.RENDER_HEIGHT-e.getY());
			else
			{
				if(dropDownEnabled)
					return;
				else if(GraphicalMain.lastBestHit <= 32677 && GraphicalMain.lastBestHit >= 32671)
				{
//					if(!Call.callSortingEnabledItem.isEnabled())
//						GraphicalMain.self.itemStateChanged(new ItemEvent(Call.callSortingEnabledItem, 0, Call.callSortingEnabledItem, 0));
					if(!Call.callSortingEnabledItem.getState())
					{
						
						Call.callSortingEnabledItem.setState(true);
						int subItems = Call.callSortingSubmenu.getItemCount();
						for(int i = 2; i < subItems; i++)
						{
							Menu menuItem = ((Menu) Call.callSortingSubmenu.getItem(i));
							menuItem.setEnabled(true);
							Call.setSortingMenu(menuItem, 1, -1, GraphicalMain.self);
						}
					}
					
					if(GraphicalMain.lastBestHit == 32677)
						handleCallSort(getFirstPriority(Call.callStateSortMenu));
					else if (GraphicalMain.lastBestHit == 32676)
						handleCallSort(getFirstPriority(Call.callNameSortMenu));
					else if (GraphicalMain.lastBestHit == 32675)
						handleCallSort(getFirstPriority(Call.callDataSortMenu));
					else if (GraphicalMain.lastBestHit == 32674)
						handleCallSort(getFirstPriority(Call.callDNISSortMenu));
					else if (GraphicalMain.lastBestHit == 32673)
						handleCallSort(getFirstPriority(Call.callQueueSortMenu));
					else if (GraphicalMain.lastBestHit == 32672)
						handleCallSort(getFirstPriority(Call.callDurationSortMenu));
					else// if (GraphicalMain.lastBestHit == 32671)
						handleCallSort(getFirstPriority(Call.callPrioritySortMenu));
				}
				else if(GraphicalMain.lastBestHit <= 32670 && GraphicalMain.lastBestHit >= 32666)
				{
//					if(!Agent.agentSortingEnabledItem.isEnabled())
//						GraphicalMain.self.itemStateChanged(new ItemEvent(Agent.agentSortingEnabledItem, 0, Agent.agentSortingEnabledItem, 0));
					if(!Agent.agentSortingEnabledItem.getState())
					{
						Agent.agentSortingEnabledItem.setState(true);
						int subItems = Agent.agentSortingSubmenu.getItemCount();
						for(int i = 2; i < subItems; i++)
						{
							Menu menuItem = ((Menu) Agent.agentSortingSubmenu.getItem(i));
							menuItem.setEnabled(true);
							Agent.setSortingMenu(menuItem, 1, -1, GraphicalMain.self);
						}
					}
					if(GraphicalMain.lastBestHit == 32670)
						handleAgentSort(getFirstPriority(Agent.agentStateSortMenu));
					else if(GraphicalMain.lastBestHit == 32669)
						handleAgentSort(getFirstPriority(Agent.agentLoginSortMenu));
					else if(GraphicalMain.lastBestHit == 32668)
						handleAgentSort(getFirstPriority(Agent.agentSkillsSortMenu));
					else if(GraphicalMain.lastBestHit == 32667)
						handleAgentSort(getFirstPriority(Agent.agentReleaseSortMenu));
					else// if(GraphicalMain.lastBestHit == 32666)
						handleAgentSort(getFirstPriority(Agent.agentDurationSortMenu));
				}
				else if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-13)
				{
					GraphicalMain.connection.acd.toggleCallExpansion();
				}
				else if(GraphicalMain.lastBestHit == GraphicalMain.selectBufferSize-14)
				{
					GraphicalMain.connection.acd.toggleAgentExpansion();
				}
				
				GraphicalMain.connection.acd.lock();
				try
				{
					if(GraphicalMain.lastBestHit < GraphicalMain.connection.acd.calls.length && GraphicalMain.lastBestHit != -1)
					{
						if(GraphicalMain.connection.acd.calls[GraphicalMain.lastBestHit].collapsed)
							GraphicalMain.connection.acd.calls[GraphicalMain.lastBestHit].collapsed = false;
						else
							GraphicalMain.connection.acd.calls[GraphicalMain.lastBestHit].collapsed = true;
						
					}
					else if(GraphicalMain.lastBestHit < GraphicalMain.connection.acd.calls.length + GraphicalMain.connection.acd.agents.length && GraphicalMain.lastBestHit != -1)
					{
						if(GraphicalMain.connection.acd.agents[GraphicalMain.lastBestHit-GraphicalMain.connection.acd.calls.length].collapsed)
							GraphicalMain.connection.acd.agents[GraphicalMain.lastBestHit-GraphicalMain.connection.acd.calls.length].collapsed = false;
						else
							GraphicalMain.connection.acd.agents[GraphicalMain.lastBestHit-GraphicalMain.connection.acd.calls.length].collapsed = true;
					}
				}
				catch(Exception exception)
				{
					System.err.println(exception.getMessage());
					exception.printStackTrace();
					//Do nothing.
				}
				GraphicalMain.connection.acd.unlock();
			}
		}
		else if(e.getButton() == MouseEvent.BUTTON3)
		{
			if(console.isVisible())
				console.handleRightClick(e.getX(), GraphicalMain.RENDER_HEIGHT-e.getY());

			if(dropDownEnabled)
			{
				dropDownEnabled = false;
				return;
			}
			dropDownEnabled = true;
			

			GraphicalMain.self.setAgentIdle.setEnabled(false);
			GraphicalMain.self.setAgentReleased.setEnabled(false);
			GraphicalMain.self.spyOnAgent.setEnabled(false);
			GraphicalMain.self.kickAgent.setEnabled(false);
			GraphicalMain.self.hangupAgent.setEnabled(false);
			GraphicalMain.self.dropCall.setEnabled(false);

			if(GraphicalMain.lastBestHit > -1 && GraphicalMain.lastBestHit < GraphicalMain.self.connection.acd.bufferedCalls.length)
			{
				callSelected = GraphicalMain.self.connection.acd.bufferedCalls[GraphicalMain.lastBestHit];
				GraphicalMain.self.dropCall.setEnabled(true);
			}
			else if(GraphicalMain.lastBestHit > -1 && GraphicalMain.lastBestHit < GraphicalMain.self.connection.acd.bufferedCalls.length + GraphicalMain.self.connection.acd.bufferedAgents.length)
			{
				agentSelected = GraphicalMain.self.connection.acd.bufferedAgents[GraphicalMain.lastBestHit-GraphicalMain.self.connection.acd.bufferedCalls.length];
				GraphicalMain.self.setAgentIdle.setEnabled(true);
				GraphicalMain.self.setAgentReleased.setEnabled(true);
				if(agentSelected.state == Agent.ONCALL)
				{
					GraphicalMain.self.spyOnAgent.setEnabled(true);
					GraphicalMain.self.hangupAgent.setEnabled(true);
				}
				GraphicalMain.self.kickAgent.setEnabled(true);
			}
			
			GraphicalMain.self.popupMenu.show(GraphicalMain.canvas, e.getX(), e.getY());
		}
		
	}

	public void mouseReleased(MouseEvent e) 
	{
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			leftMousePressed = false;
			console.dragging = false;
			console.scrolling = false;
			console.clickedUp = false;
			console.clickedDown = false;
			if(console.resizing)
			{
				console.resizing = false;
				console.resize();
			}
		}
		else if(e.getButton() == MouseEvent.BUTTON3)
			rightMousePressed = false;
	}
	
	public void drawMouse(GL gl)
	{	
		if(console.isVisible())
		{
			if(console.clickedResizeUpLeft(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY)||console.clickedResizeDownRight(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY))
			{ 
				GraphicalMain.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
			}
			else if(console.clickedResizeUpRight(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY)||console.clickedResizeDownLeft(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY))
			{ 
				GraphicalMain.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
			}
			else if(console.clickedResizeUp(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY)||console.clickedResizeDown(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY))
			{ GraphicalMain.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			}
			else if(console.clickedResizeLeft(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY)||console.clickedResizeRight(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY))
			{ 
				GraphicalMain.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			}
			else if(console.clickedTitle(lastMouseX, GraphicalMain.RENDER_HEIGHT-lastMouseY))
			{	
				GraphicalMain.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
			else
			{	
				GraphicalMain.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	protected void processCommand(String command)
	{
		GraphicalMain.connection.sendMessage(command + "\n");
	}
}







