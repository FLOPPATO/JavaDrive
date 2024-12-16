package client;

import javax.swing.*;
import shared.Packet;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CountDownLatch;

public class GuiLogin {
		int error;
		
	    GuiLogin(Packet p,CountDownLatch latch,int Gui_code) {
	    	JFrame frame = new JFrame("Login");
	    	frame.setSize(300, 200);
	    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	    	frame.setLayout(new BorderLayout());

	        JPanel loginPanel = new JPanel();
	        loginPanel.setLayout(new GridLayout(3, 2));

	        JLabel usernameLabel = new JLabel("Username:");
	        JTextField usernameField = new JTextField();
	        JLabel passwordLabel = new JLabel("Password:");
	        JPasswordField passwordField = new JPasswordField();
	        
	        String Gui_error = null;
	        switch(Gui_code) {
	        	case 0:
	        		Gui_error = null;
	        		break;
	        	case 1:
	        		Gui_error = "too many attempts";
	        		Gui_code *= - 1;
	        		break;
	        	case 2:
	        		Gui_error = "logged in";
	        		break;
	        	case 3:
	        		Gui_error = "registred";
	        		break;
	        	case 4:
	        		Gui_error = "wrong user/password";
	        		Gui_code *= - 1;
	        		break;
	        	case 5:
	        		Gui_error = "server is congested";
	        		Gui_code *= - 1;
	        		break;
	        }
	        
	        JLabel messageLabel = new JLabel(Gui_error, JLabel.CENTER);
	        messageLabel.setForeground(Gui_code > 0 ?Color.GREEN:Color.RED);
	        
	        loginPanel.add(usernameLabel);
	        loginPanel.add(usernameField);
	        loginPanel.add(passwordLabel);
	        loginPanel.add(passwordField);
	        
	        JPanel buttonPanel = new JPanel();
	        buttonPanel.setLayout(new FlowLayout());
	        
	        JButton loginButton = new JButton("Login");
	        JButton registerButton = new JButton("Register");
	        
	        buttonPanel.add(loginButton);
	        buttonPanel.add(registerButton);
	        
	        frame.add(loginPanel);
	        frame.add(buttonPanel,BorderLayout.SOUTH);
	        frame.add(messageLabel,BorderLayout.NORTH);

	        
	        loginButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                String username = usernameField.getText();
	                String password = new String(passwordField.getPassword());
	                p.setUSER(username);
	                p.setPASS(Packet.SHA256(password));
	                frame.dispose();
	                latch.countDown();
	            }
	        });
	        
	        registerButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                String username = usernameField.getText();
	                String password = new String(passwordField.getPassword());
	                p.setUSER(username);
	                p.setPASS(Packet.SHA256(password));
	                p.newUSER();
	                frame.dispose();
	                latch.countDown();
	            }
	        });
	        frame.setVisible(true);
	    }
}
