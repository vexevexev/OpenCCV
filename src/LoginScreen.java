import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JOptionPane;

import JSON.JSONObject;


public class LoginScreen implements ActionListener
{
	Frame frame = new Frame("Contact Center Visualizer (Login)");
	Label instructions = new Label("Please enter your OpenACD credentials...");
	Label username = new Label("Username ");
	TextField usernameField = new TextField(15);
	
	Label password = new Label("Password ");
	TextField passwordField = new TextField(15);
	
	Label domain = new Label("Domain   ");
	TextField domainField = new TextField(15);
	
	Button submitButton = new Button("Submit");
	
	boolean authenticated = false;
	
	public String workingUsername;
	public String workingPassword;
	
	public LoginScreen()
	{
		passwordField.setEchoChar('*');
		
		instructions.setBackground(new Color(.75f,.75f,.75f));
		username.setBackground(new Color(.75f,.75f,.75f));
		password.setBackground(new Color(.75f,.75f,.75f));
		frame.setBackground(new Color(.75f, .75f, .75f));
		
		frame.setLayout(new BorderLayout());
		frame.add(instructions, BorderLayout.NORTH);
		Container container = new Container();
		container.setLayout(new BorderLayout());
		Container container2 = new Container();
		Font instructionFont = new Font(Font.MONOSPACED ,Font.BOLD, 14);
		instructions.setFont(instructionFont);
		username.setFont(instructionFont);
		password.setFont(instructionFont);
		domain.setFont(instructionFont);
		
		container2.setLayout(new FlowLayout());
		container2.add(instructions);
		container.add(container2, BorderLayout.NORTH);
		container2 = new Container();
		container2.setLayout(new FlowLayout());
		container2.add(username);
//		container.add(container2, BorderLayout.WEST);
//		container2 = new Container();
//		container2.setLayout(new FlowLayout());
		container2.add(usernameField);
		container.add(container2, BorderLayout.CENTER);
		frame.add(container, BorderLayout.NORTH);

		container = new Container();
		container.setLayout(new BorderLayout());
		container2 = new Container();
		container2.setLayout(new FlowLayout());
		container2.add(password);
//		container.add(container2, BorderLayout.WEST);
//		container2 = new Container();
//		container2.setLayout(new FlowLayout());
		container2.add(passwordField);
		container.add(container2, BorderLayout.NORTH);
		container2 = new Container();
		container2.setLayout(new FlowLayout());
		container2.add(domain);
		container2.add(domainField);
		container.add(container2, BorderLayout.CENTER);
		frame.add(container, BorderLayout.CENTER);
		container = new Container();
		container.setLayout(new FlowLayout());
		submitButton.setSize(frame.getWidth()/3, submitButton.getHeight());
		submitButton.addActionListener(this);
		container.add(submitButton);
		frame.add(container, BorderLayout.SOUTH);
		frame.pack();
		
		frame.setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-frame.getWidth()/2), 
						  (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-frame.getHeight()/2));
		
		frame.setResizable(false);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				attemptLogout();
				System.exit(0);
			}
		});
		
		KeyAdapter enterKeyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if(attemptLogin())
						authenticated = true;
				}
			}
		};
		usernameField.addKeyListener(enterKeyListener);
		passwordField.addKeyListener(enterKeyListener);
		domainField.addKeyListener(enterKeyListener);
	}
	
	public void authenticate()
	{
		try 
		{
			frame.setVisible(true);
			while(true)
			{
				if(!authenticated)
						Thread.sleep(100);
				else
				{
					frame.setVisible(false);
					return;
				}
			}
		}
		catch (InterruptedException e) 
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	
	
	public void actionPerformed(ActionEvent e) 
	{
		try
		{
			Button button = (Button) e.getSource();
			if(button.getLabel().equalsIgnoreCase(submitButton.getLabel()))
			{
				if(attemptLogin())
					authenticated = true;
			}
		}
		catch(Exception exception)
		{
			//Do nothing.
		}
	}
	
	private boolean attemptLogin()
	{
		try
		{
			usernameField.setEnabled(false);
			passwordField.setEnabled(false);
			workingUsername = usernameField.getText();
			workingPassword = passwordField.getText();
			String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/login", "agent="+usernameField.getText()+"&password="+passwordField.getText()+"&domain="+domainField.getText());
			usernameField.setEnabled(true);
			passwordField.setEnabled(true);
			
			JSONObject json = new JSONObject(httpResponse);
			if(json.getString("success").equalsIgnoreCase("false"))
			{
				JOptionPane.showMessageDialog(frame, json.getString("message"));
				return false;
			}
			
			return true;
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(frame, e.getMessage()+"\n"+e.getStackTrace());
			usernameField.setEnabled(true);
			passwordField.setEnabled(true);
			return false;
		}
	}
	
	public boolean attemptLogout()
	{
		try
		{
			String httpResponse = LoginScreen.excutePost("http://"+GraphicalMain.connection.host+":8383/logout", "agent="+workingUsername);
			JSONObject json = new JSONObject(httpResponse);
			if(json.getString("success").equalsIgnoreCase("false"))
				return false;
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

	public static String excutePost(String targetURL, String urlParameters) throws Exception
	{
		URL url;
		HttpURLConnection connection = null;
		//Create connection
		url = new URL(targetURL);
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", 
		 "application/x-www-form-urlencoded");
					
		connection.setRequestProperty("Content-Length", "" + 
		 Integer.toString(urlParameters.getBytes().length));
		connection.setRequestProperty("Content-Language", "en-US");
					
		connection.setUseCaches (false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
	
		//Send request
		DataOutputStream wr = new DataOutputStream (
		connection.getOutputStream ());
		wr.writeBytes (urlParameters);
		wr.flush ();
		wr.close ();
	
		//Get Response	
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
		while((line = rd.readLine()) != null) 
		{
			response.append(line);
			response.append('\r');
		}
		rd.close();
		if(connection != null) 
		{
			connection.disconnect(); 
		}
		return response.toString();
	}
}
