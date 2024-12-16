package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class FileSelect {
	
	private List<String> files = null;
	
	public List<String> getFiles(){
		return files;
	}
	
	FileSelect(List<String> s,CountDownLatch latch ){
		JFrame frame = new JFrame("Select file to download");
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        
        String[] t = s.toArray(new String[s.size()]);
        JList<String> jList = new JList<>(t);
        jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPanel = new JScrollPane(jList);

        JButton selectButton = new JButton("Select");
        JButton confirmButton = new JButton("Confirm");
        JLabel resultLabel = new JLabel("selected: ");
        
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<String> sv = jList.getSelectedValuesList();
                resultLabel.setText("selected: " + sv);
                
            }
        });
        
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	files = jList.getSelectedValuesList();
            	latch.countDown();
                frame.dispose();
            }
        });
        
        JPanel buttons = new JPanel();
        
        buttons.add(selectButton);
        buttons.add(confirmButton);

        
        frame.add(scrollPanel, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);
        frame.add(resultLabel, BorderLayout.NORTH);

        frame.setVisible(true);
	}
}
