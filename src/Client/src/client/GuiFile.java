package client;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

public class GuiFile{
	
	private Stack<File> Paths;
	
	public Stack<File> getP(){
		return Paths;
	}
	
	GuiFile(CountDownLatch latch){
		Paths = new Stack<>();
        JFrame frame = new JFrame("Upload");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        JLabel dropArea = new JLabel("Drop file here", SwingConstants.CENTER);
        dropArea.setFont(new Font("Arial", Font.BOLD, 16));
        dropArea.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        frame.add(dropArea, BorderLayout.CENTER);
        dropArea.setOpaque(true);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        
        new DropTarget(dropArea, 
        new DropTargetListener() {
        		public void dragEnter(DropTargetDragEvent x) {
        			dropArea.setBackground(new Color(51, 153, 255));
	            }
	
	            public void dragOver(DropTargetDragEvent x) {} 
	
	            public void dropActionChanged(DropTargetDragEvent x) {}
	
	            public void dragExit(DropTargetEvent x) {
	                dropArea.setText("Drop file here");
	                dropArea.setBackground(new Color(255, 255, 255));
	            }
	
	            public void drop(DropTargetDropEvent EV) {
	            	dropArea.setBackground(new Color(255, 255, 255));
	                try {
	                    EV.acceptDrop(DnDConstants.ACTION_COPY);
	                    Transferable transferable = EV.getTransferable();
	                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
	                    for (int i = 0; i < files.size(); i++) {
	                    	File tmp = files.get(i);
	                    	if(!Paths.contains(tmp.getAbsolutePath()))
	                    		Paths.push(tmp);
	                    }
	                    
	                    String text = "<html><b>Files:</b><br>";
	                    for (int i = 0; i < Paths.size(); i++) {
	                    	text+=(Paths.elementAt(i)+"<br>");
	                    }
	                    dropArea.setText(text+"</html>");
	                    
	                    if(buttonPanel.getComponentCount() == 0) {
	                        JButton button = new JButton("Send");
	                        button.setSize(100, 30);
	                        button.addActionListener(new ActionListener() {
	                            public void actionPerformed(ActionEvent e) {
	                                JOptionPane.showMessageDialog(frame, "Sended");
	                                latch.countDown();
	                                frame.dispose();
	                            }
	                        });
	                        buttonPanel.add(button);
	                        frame.add(buttonPanel, BorderLayout.SOUTH);
	                    }
	                } catch (Exception e) {
	                    dropArea.setText("Errore");
	                    e.printStackTrace();
	                }
	            }
        });
        
        frame.setVisible(true);
		
	}		
}
