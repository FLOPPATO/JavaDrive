package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EmptyStackException;
import java.util.concurrent.Semaphore;
import java.io.File;
import java.io.FileInputStream;

import shared.Packet;

public class handler implements Runnable {

	Socket socket;
	Semaphore Client_max;
	
	handler(Socket socket,Semaphore Client_max){
		this.socket = socket;
		this.Client_max = Client_max;
	}
	
	ObjectInputStream obj_in;
	
	OutputStream raw_data_out;
	InputStream raw_data_in;
	
	PrintWriter string_writer;
	BufferedReader string_reader;
	
	DataOutputStream message_out;
	DataInputStream message_in;
	
	private void cleanup() {
    	try {
			if(obj_in!=null)obj_in.close();
			if(raw_data_out!=null)raw_data_out.close();
			if(raw_data_in!=null)raw_data_in.close();
			if(string_writer!=null)string_writer.close();
			if(string_reader!=null)string_reader.close();
			if(message_out!=null)message_out.close();
			if(message_in!=null)message_out.close();
			
			if(socket!=null)socket.close();
			Client_max.release();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    static String upload_dir;
    
    static public void setDir(String dir) {
    	upload_dir = dir;
    }
    
    static int MAX_ATTEMPTS;
    
    static public void maxAtt(int att) {
    	MAX_ATTEMPTS = att;
    }
    
    
    static final int SERVER_READY  		 =  0;  //server is ready
    static final int TOO_MANY_ATTEMPTS   =	1;  //kick user, too many attempts
    static final int LOG_IN				 =  2;  //user log-in success
    static final int NEW_USER	 		 =  3;  //user registered 
    static final int WRONG_CRED	  		 =  4;  //wrong credentials
    static final int FULL_CAPACITY 		 =  5;  //full capacity(see server.java)
    static final int SERVER_CLOSE  		 =  6;  //server closing socket
    static final int FILE_RECIVED  		 =  99; //file is received
    
    static final int SEE_FILE            =  7;  //notify server to send file names
    static final int WAIT_FILE           =  8;  //notify server to wait for files to upload
	
	public void run() {
        try {
        	obj_in = new ObjectInputStream(socket.getInputStream());
        	
        	raw_data_in = socket.getInputStream();
        	raw_data_out = socket.getOutputStream();
        	
        	string_writer = new PrintWriter(socket.getOutputStream(), true);
        	string_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	
        	message_out = new DataOutputStream(socket.getOutputStream());
        	message_in = new DataInputStream(socket.getInputStream());
        	
        	int attempts = 0;
        	message_out.write(SERVER_READY);
        	message_out.flush();
            int last_answer;
            do {
            	System.out.println("att" + ++attempts);
		        Packet client = (Packet) obj_in.readObject();
		        if(client.isNEW()){
		        	if(Auth.addUser(client.getUSER(), client.getPASS()))
		        		message_out.write(last_answer = NEW_USER); // ( last_answer = 3 ) = 3
		        	else 
		        		message_out.write(last_answer = WRONG_CRED);
		        }else {
		        	if(Auth.login(client.getUSER(), client.getPASS())) 
		        		message_out.write(last_answer = LOG_IN);
		        	else 
		        		message_out.write(last_answer = WRONG_CRED);
		        }
		        message_out.flush();
	        }while(attempts != MAX_ATTEMPTS && last_answer == WRONG_CRED);
            
            if(attempts == 3 && last_answer == WRONG_CRED) {
            	message_out.write(TOO_MANY_ATTEMPTS);
            	message_out.flush();
            	cleanup();
            	throw new Exception(socket + "too many attempts");
            }
            
        	/*TODO:
    			TD1
    		
    	
        	 */
//--------------------------------------SELECT OPTION---------------------
            
            while(true) {
	            int ans = message_in.read();
	            Packet user = (Packet) obj_in.readObject();
	        	if(ans == WAIT_FILE) {
	            	download_multiple(user); 
	            	/*sequence:
	            	 * receive buffer size(kB)
	            	 * for each file:
	            	 * 		receive size
	            	 * 		receive file data
	            	 * 		send confirm receive(99)
	            	 * 		next file
	            	 * 
	            	 */
	        	}else if(ans == SEE_FILE) {
	        		send_fileNames(user);
	            	/*sequence:
	            	 * send the user's file count
	            	 * for each file:
	            	 * 		send file name
	            	 */
	        		
	        		upload_multiple(user);
	            	/*sequence:
	            	 * wait for buffer size(kB)
	            	 * wait for file count
	            	 * for each file:
	            	 * 		wait for file name
	            	 * 		write file length
	            	 * 		send raw file data
	            	 * 		read FILE_RECIVED for confirmation (99)
	            	 */
	        	}
            }
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			cleanup();
		}
	}
	
	private void upload_multiple(Packet g) {
		try {
			String dir = upload_dir + g.getUSER() + "\\";
			System.out.println(121);
			Long buffer_size = message_in.readLong();
			System.out.println(buffer_size);
			int file_remaining = message_in.read();
			System.out.println(file_remaining);
			while(file_remaining-- != 0) {
				System.out.println(file_remaining);
				String filename = string_reader.readLine();
				File toSend = new File(dir + filename);
				System.out.println(121);
				message_out.writeLong(toSend.length());
				message_out.flush();
				
				FileInputStream fileread = new FileInputStream(toSend);
				
	            byte[] buffer = new byte[(int) (buffer_size * 1024)];
	            int bytesRead;
	            
				while ((bytesRead = fileread.read(buffer)) > 0) {
					raw_data_out.write(buffer, 0, bytesRead);
				}
				fileread.close();
				
				if(message_in.read() != FILE_RECIVED)
					throw new Exception("file non ricevuti");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void send_fileNames(Packet g) {
		try {
			File path = new File(upload_dir + g.getUSER());
			
			File[] files = path.listFiles();
			System.out.println("coonf");
			message_out.write(files.length);
			message_out.flush();
			if(files != null)
				for(File file : files) {
					string_writer.println(file.getName());
					System.out.println(file.getName());
				}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean download_multiple(Packet g) {
		try {
			Long buf_size = message_in.readLong();
	    	String name;
	    	try {
	        	Path ph = Paths.get(upload_dir+g.getUSER());
	        	try {
	        	    Files.createDirectories(ph);
	        	} catch (IOException e) {}
	    		while((name = g.getFILEname()) != null) {
	    			
	    			Long file_size = message_in.readLong();
	    			System.out.println("!!!!!" + file_size);
	
	                System.out.println(ph.toAbsolutePath().toString()+"\\"+name);
	            	
	            	FileOutputStream file_scrittura = new FileOutputStream(ph.toAbsolutePath().toString()+"\\"+name);
	
					byte[] buffer = new byte[(int) (buf_size * 1024)];
					int bytesRead;
					
					System.out.println("Fatto a");
					while (file_size > 0) {
						bytesRead = raw_data_in.read(buffer, 0, 
								(int) Math.min(buffer.length, file_size)); //lunghezza massima o rimanente
						file_scrittura.write(buffer, 0, bytesRead);
						file_size -= bytesRead;
					}
					file_scrittura.close();
					System.out.println("Fatto b");
					
					message_out.write(FILE_RECIVED);
					message_out.flush();
	    		}
	    	}catch(EmptyStackException e) {
	        	System.out.println("finitI");
	        }
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
    	
	}
	
}
