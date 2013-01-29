import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

import JSON.JSONObject;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.j2d.TextRenderer;

//The OACD community has been unable to agree on a wall-board. This aims to fix that.
public class GraphicalMain implements GLEventListener, ActionListener, ItemListener
{
	GLU glu;
	GLUT glut;
	public static int fontSize = 9;
	public static User user = new User();
	public static TextRenderer textRenderer = null;
	public static GLCanvas canvas;
	public static int RENDER_WIDTH, RENDER_HEIGHT, FOV = 60;
	public static SocketConnection connection;
	public static GraphicalMain self;
	public LoginScreen loginScreen;
	public PopupMenu popupMenu;
	Frame frame;

	public static final int NUMBER_OF_VIEWS = 2;
	public static final int CALL_FLOW_VIEW = 0;
	public static final int QUEUE_WEIGHT_VIEW = 1;	
	public static int dataView = 0; 
	
	public GraphicalMain()
	{	
		self = this;
		
		if(!OpenACD.DEBUGGING)
		{
			loginScreen = new LoginScreen();
			loginScreen.authenticate();
		}
		
		try
		{
			//Windows
			Runtime.getRuntime().loadLibrary("jogl");
//			Runtime.getRuntime().loadLibrary("jogl_awt");
//			Runtime.getRuntime().loadLibrary("gluegen-rt");
		}
		catch(Exception e)
		{
			//Unix
			Runtime.getRuntime().loadLibrary("libjogl");
//			Runtime.getRuntime().loadLibrary("libjogl_awt");
//			Runtime.getRuntime().loadLibrary("libgluegen-rt");
		}
		
		glu = new GLU();
		glut = new GLUT();
		frame = new Frame("Contact Center Visualization Client");
		
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(true);
		canvas = new GLCanvas(caps);

		canvas.addMouseListener(user);
		canvas.addMouseMotionListener(user);
		canvas.addMouseWheelListener(user);
		canvas.addKeyListener(user);
		
//		frame.setLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-width/2, 
//				          (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-height/2);
		canvas.setSize((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), 
		          (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());

		frame.add(canvas);

		frame.pack();
		
		frame.setExtendedState(Frame.MAXIMIZED_BOTH); 
//		frame.setUndecorated(true);
	    
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				if(!OpenACD.DEBUGGING)
					loginScreen.attemptLogout();
				System.exit(0);
			}
		});
		
		
		frame.pack();
		RENDER_WIDTH = canvas.getWidth();
		RENDER_HEIGHT = canvas.getHeight();

		canvas.addGLEventListener( this );

        textRenderer = new TextRenderer(new java.awt.Font("Verdana", java.awt.Font.BOLD, fontSize), false , false);

		popupMenu = loadMenuBar();
		canvas.add(popupMenu);

		System.setProperty("sun.awt.noerasebackground", "true");
		FPSAnimator anim = new FPSAnimator(canvas, 60, true);
//		anim.setRunAsFastAsPossible(true);
		anim.start();
		
		frame.setVisible( true );
		
		canvas.requestFocus();
		
	}

	
	public void actionPerformed(ActionEvent e) 
	{
		try
		{
			MenuItem item = (MenuItem) e.getSource();
			System.out.println("MenuItem event triggered for: "+item.getLabel());
			
			if(item.getLabel().equalsIgnoreCase("Exit"))
				System.exit(0);
			else if(item.getLabel().equalsIgnoreCase(expandAgents.getLabel()))
			{
				connection.acd.expandAgents();
			}
			else if(item.getLabel().equalsIgnoreCase(collapseAgents.getLabel()))
			{
				connection.acd.collapseAgents();
			}
			else if(item.getLabel().equalsIgnoreCase(expandCalls.getLabel()))
			{
				connection.acd.expandCalls();
			}
			else if(item.getLabel().equalsIgnoreCase(collapseCalls.getLabel()))
			{
				connection.acd.collapseCalls();
			}
		}
		catch(Exception exception)
		{
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			//The ActionEvent was not triggered by a MenuItem,  and the cast failed. Do nothing.
		}
	}
	
	public boolean isInteger( String input )  
	{  
		try  
		{  
			Integer.parseInt( input );  
			return true;  
		}  
		catch( Exception e)  
		{  
			return false;  
		}  
	} 
	
	public void itemStateChanged(ItemEvent e) 
	{
		try
		{
			CheckboxMenuItem item =  (CheckboxMenuItem) e.getSource();
			String itemName = item.getLabel();
			
			if(itemName.equalsIgnoreCase(autofoldCalls.getLabel()))
			{
				if(item.getState())
				{
					autofoldCalls.setLabel("Auto Fold Calls (on)");
				}
				else
				{
					autofoldCalls.setLabel("Auto Fold Calls (off)");
				}
			}
			else if(itemName.equalsIgnoreCase(autofoldAgents.getLabel()))
			{
				if(item.getState())
				{
					autofoldAgents.setLabel("Auto Fold Agents (on)");
				}
				else
				{
					autofoldAgents.setLabel("Auto Fold Agents (off)");
				}
			}
			else if(itemName.equalsIgnoreCase(Call.callState))
			{
				Call.showCallState = item.getState();
				if(!Call.showCallState)
					Call.removeSortOption(Call.callState);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callName))
			{
				Call.showCallName = item.getState();
				if(!Call.showCallName)
					Call.removeSortOption(Call.callName);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callData))
			{
				Call.showCallData = item.getState();
				if(!Call.showCallData)
					Call.removeSortOption(Call.callData);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callDNIS))
			{
				Call.showCallDNIS = item.getState();
				if(!Call.showCallDNIS)
					Call.removeSortOption(Call.callDNIS);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callQueue))
			{
				Call.showCallQueue = item.getState();
				if(!Call.showCallQueue)
					Call.removeSortOption(Call.callQueue);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callDuration))
			{
				Call.showCallDuration = item.getState();
				if(!Call.showCallDuration)
					Call.removeSortOption(Call.callDuration);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callPriority))
			{
				Call.showCallPriority = item.getState();
				if(!Call.showCallPriority)
					Call.removeSortOption(Call.callPriority);
				Call.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Agent.agentState))
			{
				Agent.showAgentState = item.getState();
				if(!Agent.showAgentState)
					Agent.removeSortOption(Agent.agentState);
				Agent.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Agent.agentLogin))
			{
				Agent.showAgentLogin = item.getState();
				if(!Agent.showAgentLogin)
					Agent.removeSortOption(Agent.agentLogin);
				Agent.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Agent.agentSkills))
			{
				Agent.showAgentSkills = item.getState();
				if(!Agent.showAgentSkills)
					Agent.removeSortOption(Agent.agentSkills);
				Agent.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Agent.agentRelease))
			{
				Agent.showAgentLabel = item.getState();
				if(!Agent.showAgentLabel)
					Agent.removeSortOption(Agent.agentRelease);
				Agent.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Agent.agentDuration))
			{
				Agent.showAgentDuration = item.getState();
				if(!Agent.showAgentDuration)
					Agent.removeSortOption(Agent.agentDuration);
				Agent.setGridSize();
			}
			else if(itemName.equalsIgnoreCase(Call.callSortingEnabled))
			{
				if(item.getState())
				{
					int subItems = Call.callSortingSubmenu.getItemCount();
					for(int i = 2; i < subItems; i++)
					{
						Menu menuItem = ((Menu) Call.callSortingSubmenu.getItem(i));
						menuItem.setEnabled(true);
						Call.setSortingMenu(menuItem, 1, -1, this);
					}
				}
				else
				{
					int subItems = Call.callSortingSubmenu.getItemCount();
					for(int i = 2; i < subItems; i++)
						((Menu) Call.callSortingSubmenu.getItem(i)).setEnabled(false);
					
					Call.callSortingOrder = new String[0];
				}
			}
			else if(itemName.equalsIgnoreCase(Agent.agentSortingEnabled))
			{
				if(item.getState())
				{
					int subItems = Agent.agentSortingSubmenu.getItemCount();
					for(int i = 2; i < subItems; i++)
					{
						Menu menuItem = ((Menu) Agent.agentSortingSubmenu.getItem(i));
						menuItem.setEnabled(true);
						Agent.setSortingMenu(menuItem, 1, -1, this);
					}
				}
				else
				{
					int subItems = Agent.agentSortingSubmenu.getItemCount();
					for(int i = 2; i < subItems; i++)
						((Menu) Agent.agentSortingSubmenu.getItem(i)).setEnabled(false);
					
					Agent.agentSortingOrder = new String[0];
				}
			}
			else if(itemName.length()>= 3 && isInteger(itemName.substring(0, itemName.length()-2)))
			{
				Menu parent = (Menu)item.getParent();
				String parentName = parent.getLabel();
				Menu grandParent = (Menu)parent.getParent();
				String grandParentName = grandParent.getLabel();
				
				if(grandParentName.equalsIgnoreCase(Call.callSortingSubmenu.getLabel()))
				{
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
						int newIndex = Integer.parseInt(itemName.substring(0, itemName.length()-2))-1;
						String[] newList = new String[Call.callSortingOrder.length+1];
						
						for(int i = 0; i < newList.length; i++)
						{
							if(i < newIndex)
								newList[i] = Call.callSortingOrder[i];
							else if(i == newIndex)
								newList[i] = parentName;
							else
								newList[i] = Call.callSortingOrder[i-1];
									
						}
						Call.callSortingOrder = newList;
					}
					
					Call.loadUISort(this);
				}
				else
				{
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
						int newIndex = Integer.parseInt(itemName.substring(0, itemName.length()-2))-1;
						String[] newList = new String[Agent.agentSortingOrder.length+1];
						
						for(int i = 0; i < newList.length; i++)
						{
							if(i < newIndex)
								newList[i] = Agent.agentSortingOrder[i];
							else if(i == newIndex)
								newList[i] = parentName;
							else
								newList[i] = Agent.agentSortingOrder[i-1];
									
						}
						Agent.agentSortingOrder = newList;
					}
					
					Agent.loadUISort(this);
				}
			}
		    
			System.out.println("ItemStateChange event triggered for: "+((CheckboxMenuItem) e.getSource()).getLabel());
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			System.err.println(exception.getMessage());
			//The ItemEvent was not triggered by a CheckboxMenuItem,  and the cast failed. Do nothing.
		}
	}

	Menu callFoldingMenu = new Menu("Call Folding ");
	CheckboxMenuItem autofoldCalls = new CheckboxMenuItem("Auto-Fold Calls (off)");
	MenuItem collapseCalls = new MenuItem("Collapse All Calls");
	MenuItem expandCalls = new MenuItem("Expand All Calls");
	Menu agentFoldingMenu = new Menu("Agent Folding ");
	CheckboxMenuItem autofoldAgents = new CheckboxMenuItem("Auto-Fold Agents (off)");
	MenuItem collapseAgents = new MenuItem("Collapse All Agents");
	MenuItem expandAgents = new MenuItem("Expand All Agents");

	MenuItem setAgentIdle = new MenuItem("Set Agent Idle ");
	MenuItem setAgentReleased = new MenuItem("Set Agent Released ");
	MenuItem spyOnAgent = new MenuItem("Spy ");
	MenuItem kickAgent = new MenuItem("Kick agent ");
	MenuItem hangupAgent = new MenuItem("Hangup Agent ");
	
	MenuItem dropCall = new MenuItem("Drop Call ");
	
	private PopupMenu loadMenuBar() 
	{
		Menu viewMenu, hm;
		Menu viewSub, viewSubSub;
		CheckboxMenuItem checkBoxMenu;
		  
		PopupMenu mb = new PopupMenu();

	    MenuItem mi;
	    // The File Menu...
//	    fm.add(mi = new MenuItem("Open", new MenuShortcut('O')));
//	    mi.addActionListener(this);
//	    fm.add(mi = new MenuItem("Close", new MenuShortcut('W')));
//	    mi.addActionListener(this);
//	    fm.addSeparator();
//	    fm.add(mi = new MenuItem("Print", new MenuShortcut('P')));
//	    mi.addActionListener(this);
//	    fm.addSeparator();

	    // The Options Menu...
	    viewMenu = new Menu("View ");
	    
	    viewSub = new Menu("Call Data Columns ");
		    (checkBoxMenu = new CheckboxMenuItem(Call.callState)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Call.callName)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Call.callData)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Call.callDNIS)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Call.callQueue)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Call.callDuration)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Call.callPriority)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		viewMenu.add(viewSub);
		
	    viewSub = new Menu("Agent Data Columns ");
		    (checkBoxMenu = new CheckboxMenuItem(Agent.agentState)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Agent.agentLogin)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Agent.agentSkills)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Agent.agentRelease)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		    (checkBoxMenu = new CheckboxMenuItem(Agent.agentDuration)).addItemListener(this);
		    checkBoxMenu.setState(true);
		    viewSub.add(checkBoxMenu);
		viewMenu.add(viewSub);
		viewMenu.addSeparator();
	    	Call.callSortingEnabledItem.addItemListener(this);
	    	Call.callSortingEnabledItem.setState(false);
	    	Call.callSortingEnabledItem.addItemListener(this);
	    	Call.callSortingSubmenu.add(Call.callSortingEnabledItem);
	    	Call.callSortingSubmenu.addSeparator();
	    	Call.callStateSortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callStateSortMenu);
	    	Call.setSortingMenu(Call.callStateSortMenu, 1, -1, this);
	    	Call.callNameSortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callNameSortMenu);
	    	Call.setSortingMenu(Call.callNameSortMenu, 1, -1, this);
	    	Call.callDataSortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callDataSortMenu);
	    	Call.setSortingMenu(Call.callDataSortMenu, 1, -1, this);
	    	Call.callDNISSortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callDNISSortMenu);
	    	Call.setSortingMenu(Call.callDNISSortMenu, 1, -1, this);
	    	Call.callQueueSortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callQueueSortMenu);
	    	Call.setSortingMenu(Call.callQueueSortMenu, 1, -1, this);
	    	Call.callDurationSortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callDurationSortMenu);
	    	Call.setSortingMenu(Call.callDurationSortMenu, 1, -1, this);
	    	Call.callPrioritySortMenu.setEnabled(false);
	    	Call.callSortingSubmenu.add(Call.callPrioritySortMenu);
	    	Call.setSortingMenu(Call.callPrioritySortMenu, 1, -1, this);
		viewMenu.add(Call.callSortingSubmenu);
		    Agent.agentSortingEnabledItem.addItemListener(this);
		    Agent.agentSortingEnabledItem.setState(false);
		    Agent.agentSortingSubmenu.add( Agent.agentSortingEnabledItem);
		    Agent.agentSortingEnabledItem.addItemListener(this);
		    Agent.agentSortingSubmenu.addSeparator();
		    Agent.agentStateSortMenu.setEnabled(false);
		    Agent.agentSortingSubmenu.add(Agent.agentStateSortMenu);
		    Agent.setSortingMenu(Agent.agentStateSortMenu, 1, -1, this);
		    Agent.agentLoginSortMenu.setEnabled(false);
		    Agent.agentSortingSubmenu.add(Agent.agentLoginSortMenu);
		    Agent.setSortingMenu(Agent.agentLoginSortMenu, 1, -1, this);
		    Agent.agentSkillsSortMenu.setEnabled(false);
		    Agent.agentSortingSubmenu.add(Agent.agentSkillsSortMenu);
		    Agent.setSortingMenu(Agent.agentSkillsSortMenu, 1, -1, this);
		    Agent.agentReleaseSortMenu.setEnabled(false);
		    Agent.agentSortingSubmenu.add(Agent.agentReleaseSortMenu);
		    Agent.setSortingMenu(Agent.agentReleaseSortMenu, 1, -1, this);
		    Agent.agentDurationSortMenu.setEnabled(false);
		    Agent.agentSortingSubmenu.add(Agent.agentDurationSortMenu);
		    Agent.setSortingMenu(Agent.agentDurationSortMenu, 1, -1, this);
		viewMenu.add(Agent.agentSortingSubmenu);

		viewMenu.addSeparator();

		autofoldCalls.addItemListener(this);
		callFoldingMenu.add(autofoldCalls);
		collapseCalls.addActionListener(this);
		expandCalls.addActionListener(this);
		callFoldingMenu.addSeparator();
		callFoldingMenu.add(expandCalls);
		callFoldingMenu.add(collapseCalls);

		viewMenu.add(callFoldingMenu);
		autofoldAgents.addItemListener(this);
		agentFoldingMenu.add(autofoldAgents);
		collapseAgents.addActionListener(this);
		expandAgents.addActionListener(this);
		agentFoldingMenu.addSeparator();
		agentFoldingMenu.add(expandAgents);
		agentFoldingMenu.add(collapseAgents);

		viewMenu.add(agentFoldingMenu);
		
	    mb.add(viewMenu);
	    mb.addSeparator();
	    


		setAgentIdle.setEnabled(false);
		setAgentIdle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/set_avail", "agent="+user.agentSelected.login);
					
					JSONObject json = new JSONObject(httpResponse);
					if(json.getString("success").equalsIgnoreCase("false"))
					{
						JOptionPane.showMessageDialog(frame, json.getString("message"));
					}
				}
				catch(Exception exception)
				{
					JOptionPane.showMessageDialog(frame, exception.getMessage()+"\n"+exception.getStackTrace());
				}
				user.dropDownEnabled = false;
			}
		});
	    mb.add(setAgentIdle);
	    
		setAgentReleased.setEnabled(false);
		setAgentReleased.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/set_released", "agent="+user.agentSelected.login);
					
					JSONObject json = new JSONObject(httpResponse);
					if(json.getString("success").equalsIgnoreCase("false"))
					{
						JOptionPane.showMessageDialog(frame, json.getString("message"));
					}
				}
				catch(Exception exception)
				{
					JOptionPane.showMessageDialog(frame, exception.getMessage()+"\n"+exception.getStackTrace());
				}
				user.dropDownEnabled = false;
			}
		});
	    mb.add(setAgentReleased);
	    
		spyOnAgent.setEnabled(false);
		spyOnAgent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/spy", "spy="+loginScreen.workingUsername+"&target="+user.agentSelected.login);
					
					JSONObject json = new JSONObject(httpResponse);
					if(json.getString("success").equalsIgnoreCase("false"))
					{
						JOptionPane.showMessageDialog(frame, json.getString("message"));
					}
				}
				catch(Exception exception)
				{
					JOptionPane.showMessageDialog(frame, exception.getMessage()+"\n"+exception.getStackTrace());
				}
				user.dropDownEnabled = false;
			}
		});
	    mb.add(spyOnAgent);
	    
		kickAgent.setEnabled(false);
		kickAgent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/kick_agent", "agent="+user.agentSelected.login);
					
					JSONObject json = new JSONObject(httpResponse);
					if(json.getString("success").equalsIgnoreCase("false"))
					{
						JOptionPane.showMessageDialog(frame, json.getString("message"));
					}
				}
				catch(Exception exception)
				{
					JOptionPane.showMessageDialog(frame, exception.getMessage()+"\n"+exception.getStackTrace());
				}
				user.dropDownEnabled = false;
			}
		});
	    mb.add(kickAgent);

	    hangupAgent.setEnabled(false);
	    hangupAgent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/hangup", "agent="+user.agentSelected.login);
					
					JSONObject json = new JSONObject(httpResponse);
					if(json.getString("success").equalsIgnoreCase("false"))
					{
						JOptionPane.showMessageDialog(frame, json.getString("message"));
					}
				}
				catch(Exception exception)
				{
					JOptionPane.showMessageDialog(frame, exception.getMessage()+"\n"+exception.getStackTrace());
				}
				user.dropDownEnabled = false;
			}
		});
	    mb.add(hangupAgent);
	    
	    mb.addSeparator();
	    
		dropCall.setEnabled(false);
		dropCall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					
					new Thread(new Runnable(){
						public void run() 
						{
							try
							{
								String httpResponse = null;
								if(user.callSelected.state == Call.ONCALL)
								{
									httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/hangup", "agent="+user.callSelected.agent.login);
								}
								else
								{
									httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/kick_call", "call_uuid="+user.callSelected.id);
								}
								
								JSONObject json = new JSONObject(httpResponse);
								if(json.getString("success").equalsIgnoreCase("false"))
								{
									JOptionPane.showMessageDialog(frame, json.getString("message"));
								}
							}
							catch(Exception exception)
							{
								JOptionPane.showMessageDialog(frame, exception.getMessage()+"\n"+exception.getStackTrace());
							}
						}
						
					}).start();
				}
				catch(Exception exception)
				{
				}
				user.dropDownEnabled = false;
			}
		});
	    mb.add(dropCall);

	    mb.addSeparator();
	    
	    mi = new MenuItem("Exit", new MenuShortcut('Q'));
	    mi.addActionListener(this);
	    mb.add(mi);
	    
        return mb;
	}

    private MenuItem makeMenuItem(String name)
    {
        MenuItem m = new MenuItem(name);
        m.addActionListener(this);
        return m;
    }
    
	double lastTime = System.nanoTime();
	double timeStep = 0;
	static int lastBestHit = -1;
    public void display(GLAutoDrawable gLDrawable) {
    	double thisTime = System.nanoTime();
    	timeStep = (thisTime-lastTime)/1E9;
        GL gl = gLDrawable.getGL();
        
    	if(dataView == CALL_FLOW_VIEW)
    	{
	        connection.acd.updateBuffer();
	        
	        drawCameraView(gl, false);
	        
	        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
	        
	        user.keyResponse(timeStep);
	        
	//        drawStars(gl);
	        
	        connection.acd.drawCallFlow(gl);
	
	        user.console.drawConsole(gl);
	
	        drawCameraView(gl, true);
	        connection.acd.drawCallFlow(gl);
	        user.console.drawConsole(gl);
	        gl.glFlush();
	        
	        drawCameraView(gl, false);
	        lastBestHit = processHits(gl.glRenderMode(GL.GL_RENDER), selectBuffer);
	        connection.acd.processHit(lastBestHit, gl);
    	}
    	else if(dataView == QUEUE_WEIGHT_VIEW)
    	{
	        drawCameraView(gl, false);
	        
	        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
	        
	        connection.acd.drawQueueWeight(gl);
	
	        user.console.drawConsole(gl);
    	}
        
        lastTime = thisTime;
        
        drawFPS(gl);
    }

	double fps_ClockLast, fps_ClockThis;
	int fps_Frames;
	
	void drawFPS(GL gl)
	{
		if(fps_ClockLast == 0)
			fps_ClockLast = System.nanoTime();
		
		if(fps_Frames == 100)
		{
			fps_Frames = 0;
			fps_ClockLast = System.nanoTime();
		}
		else
		{
			fps_ClockThis = System.nanoTime();
			fps_Frames++;
		}
		
		String fps = "0";

		if(fps_ClockThis-fps_ClockLast!=0)
			fps = "FPS: " + Math.round(fps_Frames/((fps_ClockThis-fps_ClockLast)/1E9));

		int width = (int) textRenderer.getBounds("FPS: 666").getWidth();
		
		gl.glColor3f(0,0,0);
		gl.glBegin(GL.GL_QUADS);
		drawSquare(gl, 2, 2, width+6, fontSize+3);
		gl.glEnd();
		
		gl.glColor3f(1,1,0);
		gl.glBegin(GL.GL_LINE_LOOP);
		drawSquare(gl, 2, 2, width+6, fontSize+3);
		gl.glEnd();
		

		textRenderer.setColor(1, 1, 1, 1);
		textRenderer.begin3DRendering();
		textRenderer.draw3D(fps, 5, 5, 0, 1);
		textRenderer.flush();
		textRenderer.end3DRendering();
	}
	
	void drawSquare(GL gl, int x, int y, int w, int h)
	{
		gl.glVertex3f(x, y, 0);
		gl.glVertex3f(x+w, y, 0);
		gl.glVertex3f(x+w, y+h, 0);
		gl.glVertex3f(x, y+h, 0);
	}
	
    public int processHits(int totalHits, IntBuffer buffer)
	{
    	float minZ = Float.MAX_VALUE;
    	int closestHit = -1;
    	
		int bufferOffset = 0;
		for (int i = 0; i < totalHits; i++)
		{
			float extraHits = buffer.get(bufferOffset); 
			bufferOffset++;
			
			float z1 = (float) (buffer.get(bufferOffset)& 0xffffffffL) / 0x7fffffff; 
			bufferOffset++;
			
//			float z2 = (float) (buffer.get(bufferOffset)& 0xffffffffL) / 0x7fffffff; 
			bufferOffset++;
			
			for (int j = 0; j < extraHits; j++)
			{
				int currentHit = buffer.get(bufferOffset);
				if (j == (extraHits-1) && minZ > z1)
				{
					closestHit = currentHit;
					minZ = z1;
				}
				else
				{
					//Hit already processed.
				}
				bufferOffset++;
			}
		}
		
		return closestHit;
		
    	/*
    	//Verbose example:
		System.out.println("---------------------------------");
		System.out.println(" HITS: " + hits);
		int offset = 0;
		int names;
		float z1, z2;
		for (int i=0;i<hits;i++)
		{
			System.out.println("- - - - - - - - - - - -");
			System.out.println(" hit: " + (i + 1));
			names = buffer.get(offset); offset++;
			z1 = (float) (buffer.get(offset)& 0xffffffffL) / 0x7fffffff; offset++;
			z2 = (float) (buffer.get(offset)& 0xffffffffL) / 0x7fffffff; offset++;
			System.out.println(" number of names: " + names);
			System.out.println(" z1: " + z1);
			System.out.println(" z2: " + z2);
			System.out.println(" names: ");
			
			for (int j=0;j<names;j++)
			{
				System.out.print(" " + buffer.get(offset)); 
				if (j==(names-1))
				{
					System.out.println("<-");
				}
				else
				{
					System.out.println();
				}
				offset++;
			}
			System.out.println("- - - - - - - - - - - -");
		}
		System.out.println("---------------------------------");
		*/
	}
    
	public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
    }

	int numStars = 10000;
	int[][] starPoints = null;
	float[][] starColors = null;
	float[] starOffsets = null;
	float[] starScalars = null;
	
	void loadStars()
	{
		if(RENDER_WIDTH == 0 || RENDER_HEIGHT == 0)
			return;
		
		starPoints = new int[numStars][3];
		starColors = new float[numStars][3];
		starOffsets = new float[numStars];
		starScalars = new float[numStars];
		Random rand = new Random(System.nanoTime());
		for(int i = 0; i < numStars; i++)
		{
			starPoints[i][0] = rand.nextInt()%RENDER_WIDTH;
			starPoints[i][1] = rand.nextInt()%RENDER_HEIGHT;
			starPoints[i][2] = 0;
			
			starColors[i][0] = rand.nextFloat();
			starColors[i][1] = rand.nextFloat();
			starColors[i][2] = rand.nextFloat();
			
			starScalars[i] = rand.nextFloat()*2;
		}
	}
	
	void drawStars(GL gl)
	{
		double time = System.nanoTime()/1E9;
		
		for(int i = 0; i < numStars; i++)
		{
			float sin = (float) (Math.sin(time*starScalars[i])+1)/2.0f;
			gl.glPointSize(sin*2+.1f);
			gl.glBegin(GL.GL_POINTS);
			gl.glColor3f(starColors[i][0]*sin, starColors[i][1]*sin, starColors[i][2]*sin);
			gl.glVertex3f(starPoints[i][0], starPoints[i][1], starPoints[i][2]);
			gl.glEnd();
		}
	}
	
    public void init(GLAutoDrawable gLDrawable) {
    	loadStars();
        GL gl = gLDrawable.getGL();
//        gl.glClearColor(.2f, .2f, .2f, 1); //Light grey
        gl.glClearColor(0,0,0, 1); //Black
        lastTime = System.nanoTime();
    }
 
    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
        GL gl = gLDrawable.getGL();
        RENDER_WIDTH = width;
        RENDER_HEIGHT = height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        
		user.console.width = (int) (RENDER_WIDTH/3.0);
		user.console.height = (int) (RENDER_HEIGHT/2.0);
		user.console.x = (int) 10;//(RENDER_WIDTH/2-RENDER_WIDTH/2.5/2.0);
		user.console.y = (int) 10;//(RENDER_HEIGHT - user.console.height/24 - user.console.height);
		user.console.scrollBarHeight = (user.console.y+user.console.height-user.console.fontHeight*2-user.console.padding)
								  -(user.console.y+user.console.padding+user.console.fontHeight*2);
		user.console.maxLength = user.console.width - user.console.padding*2;
		
		Call.setGridSize();
		Agent.setGridSize();
		StatsPanel.setGridSize();

		if(connection == null)
		{
			connection = new SocketConnection();
		}
		loadStars();
    }
    
    public static IntBuffer selectBuffer = null;
    public static int selectBufferSize = 32678;
	void drawCameraView(GL gl, boolean picking)
	{	
        if(picking)
        {
            gl.glViewport(0, 0, RENDER_WIDTH, RENDER_HEIGHT);
	        int[] viewPort = new int[4];
	        selectBuffer = ByteBuffer.allocateDirect(selectBufferSize * Integer.SIZE).order(ByteOrder.nativeOrder()).asIntBuffer();
	        gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort, 0);
	        gl.glSelectBuffer(selectBufferSize, selectBuffer);
	        gl.glRenderMode(GL.GL_SELECT);
	        gl.glInitNames();
        
        
	        gl.glMatrixMode(GL.GL_PROJECTION);
	        gl.glLoadIdentity();
	        glu.gluPickMatrix(User.lastMouseX, (double) viewPort[3] - User.lastMouseY, .1d, .1d, viewPort, 0);
	        gl.glOrtho(0, RENDER_WIDTH, 0, RENDER_HEIGHT, -100, 100);
	        gl.glMatrixMode(GL.GL_MODELVIEW);
	        gl.glLoadIdentity();
				
			glu.gluLookAt(user.coordinates[0],  	//Eye X
					  user.coordinates[1],			//Eye Y
					  0,		//Eye Z
					  user.coordinates[0],	//Look X
					  user.coordinates[1],	//Look Y
					  -3,	//Look Z
							0, 1, 0);
        }
		else
		{
	        gl.glViewport(0, 0, RENDER_WIDTH, RENDER_HEIGHT);
	        gl.glMatrixMode(GL.GL_PROJECTION);
	        gl.glLoadIdentity();
	        gl.glOrtho(0, RENDER_WIDTH, 0, RENDER_HEIGHT, -100, 100);
	        gl.glMatrixMode(GL.GL_MODELVIEW);
	        gl.glLoadIdentity();
				
			glu.gluLookAt(user.coordinates[0],  	//Eye X
					  user.coordinates[1],			//Eye Y
					  0,		//Eye Z
					  user.coordinates[0],	//Look X
					  user.coordinates[1],	//Look Y
					  -3,	//Look Z
							0, 1, 0);
		}
	}
	
    public static void main(String[] args) {
    	new GraphicalMain();
    }
}