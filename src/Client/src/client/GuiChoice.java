package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GuiChoice {
	
	static boolean Download;
	
	static boolean is_Download() {
		return Download;
	}
	
	GuiChoice(CountDownLatch latch){
		JFrame frame = new JFrame("Login");
    	frame.setSize(300, 200);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    	frame.setLayout(new BorderLayout());
        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        
        frame.add(buttonPanel);
        
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Download = false;
                frame.dispose();
                latch.countDown();
            }
        });
        
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Download = true;
                frame.dispose();
                latch.countDown();
            }
        });
        
        
        
        frame.setVisible(true);
	}
}
