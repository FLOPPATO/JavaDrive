package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import shared.Packet;

class Config {
	
	private Properties config;
	
	Config(String file_name){
		try {
			config = new Properties();
			FileInputStream input = new FileInputStream(file_name);
			config.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String GetAddress() {
		return config.getProperty("server.address");
	}
	
	public int GetPort() {
		return Integer.valueOf(config.getProperty("server.port"));
	}
	
	public Long GetBufSize() {
		return Long.valueOf(config.getProperty("buffer.size"));
	}
	
	public String GetDowloadDir() {
		return config.getProperty("dowload.dir");
	}
	
	
}

public class Client {
	
	static Config configs = new Config("config.properties");
	
	static final String serverAddress = configs.GetAddress();
	static final int port = configs.GetPort();
	static final Long kbBUFFER = configs.GetBufSize();
	static final String download_dir = configs.GetDowloadDir();
	
	static Socket socket;
	
	static ObjectOutputStream obj_out;
	
    static OutputStream raw_data_out;
    static InputStream raw_data_in;
    
    static PrintWriter string_writer;
    static BufferedReader string_reader;
    
    static DataOutputStream message_out;
    static DataInputStream message_in;
    
	
	static private void cleanup() {
    	try {
			if(obj_out!=null)obj_out.close();
			if(raw_data_out!=null)raw_data_out.close();
			if(raw_data_in!=null)raw_data_in.close();
			if(string_writer!=null)string_writer.close();
			if(string_reader!=null)string_reader.close();
			if(message_out!=null)message_out.close();
			if(message_in!=null)message_out.close();
			
			if(socket!=null)socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
    
	public static void main(String[] args) {
        try {
	        socket = new Socket(serverAddress, port);
	        obj_out = new ObjectOutputStream(socket.getOutputStream());
	        raw_data_out = socket.getOutputStream();
	        raw_data_in = socket.getInputStream();
	        string_writer = new PrintWriter(socket.getOutputStream(), true);
	        string_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	message_out = new DataOutputStream(socket.getOutputStream());
        	message_in = new DataInputStream(socket.getInputStream());
        	
	        int answer;
	        Packet saved = null;
	        System.out.print("1");
	        int Gui_mess = answer = message_in.read();
	        System.out.print("2");
	        
	        if(answer == SERVER_READY) do  {
	            Packet auth = new Packet();
	            
				{
					CountDownLatch latch = new CountDownLatch(1);
					new GuiLogin(auth,latch,Gui_mess);
					try {
						latch.await();
					}catch (Exception e) {}
					Gui_mess = 0;
				}
				
				System.out.println("sending");
				obj_out.writeObject(auth);
				obj_out.flush();
				System.out.println("sended");
				
				answer = message_in.read();
				saved = auth;
				
				System.out.println("switching: " + answer);
				
				switch(answer) {
					case TOO_MANY_ATTEMPTS: {
						Gui_mess = TOO_MANY_ATTEMPTS;
						throw new Exception("too many attempt");
					}
					case LOG_IN	: {
						System.out.println("welcome back");
						Gui_mess = LOG_IN;
						break;
					}
					case NEW_USER: {
						System.out.println("welcome");
						Gui_mess = NEW_USER;
						break;
					}
					case WRONG_CRED: {
						System.out.println("wrong");
						Gui_mess = WRONG_CRED;
						break;
					}
					case FULL_CAPACITY: {
						Gui_mess = FULL_CAPACITY;
						throw new Exception("server full capacity");
					}
					case SERVER_CLOSE: {
						Gui_mess = SERVER_CLOSE;
						throw new Exception("server close/unknow reason");
					}
					default: {
						System.out.println("unknown");
						break;
					}
				}
				System.out.println();
	        }while( answer != LOG_IN && answer != NEW_USER );
	        
	        else if(answer != SERVER_READY) {
	        	System.out.print("server not available");
	        	throw new Exception("server not available");
	        }
	        
        	
        	/*TODO:
        		add menu for upload/download (TD1)
        		
        	
        	*/
	        
	        
	        if(answer == LOG_IN || answer == NEW_USER) {
	        	while(true) {
			        boolean Download;
			        
					{
						CountDownLatch latch = new CountDownLatch(1);
						GuiChoice t = new GuiChoice(latch);
						try {
							latch.await();
						}catch (Exception e) {}
						Download = t.is_Download();
					}
					
		        	if(Download) {
		        		Dowload_With_gui(saved);
		        	}
		        	else {
		        		Upload_With_gui(saved);
		        	}
	        	}
	        }
	        
	        
        }catch(Exception e) {
        	e.printStackTrace();
        }finally {
			cleanup();
		}
        
	}
	
	
	static void Dowload_With_gui(Packet saved) {
		try {
			message_out.write(SEE_FILE);
			message_out.flush();
			
			List<String> file_uploaded = Ask_files(saved);
			{
				CountDownLatch latch = new CountDownLatch(1);
				FileSelect t = new FileSelect(file_uploaded,latch);
				try {
					latch.await();
				}catch (Exception e) {}
				file_uploaded = t.getFiles();
			}
			
			System.out.println(file_uploaded.size());
			Ask_dowload(saved,file_uploaded);
			System.out.println(12);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void Ask_dowload(Packet saved,List<String> file_uploaded) { 
		try {
			
			message_out.writeLong(kbBUFFER);
			message_out.flush();
			message_out.write(file_uploaded.size());
			message_out.flush();
			for(String file : file_uploaded) {
				string_writer.println(file);
        		System.out.println(121);
				Long file_size  = message_in.readLong();
        		System.out.println(121);
				
				
	        	try {
	        	    Files.createDirectories(Paths.get(download_dir));
	        	} catch (IOException e) {}
	        	
	        	FileOutputStream file_scrittura = new FileOutputStream(download_dir + file);
				
				byte[] buffer = new byte[(int) (kbBUFFER * 1024)];
				int bytesRead;
				
				System.out.println("Fatto a");
				while (file_size > 0) {
					bytesRead = raw_data_in.read(buffer, 0, 
							(int) Math.min(buffer.length, file_size)); //lunghezza massima o rimanente
					file_scrittura.write(buffer, 0, bytesRead);
					file_size -= bytesRead;
				}
				file_scrittura.close();
				
				message_out.write(FILE_RECIVED);
				message_out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static List<String> Ask_files(Packet saved) {
		List<String> file_uploaded = new ArrayList<>();
		
		try {


			
	        Packet file_to_send = new Packet();
	        file_to_send.setUSER(saved.getUSER());
	        file_to_send.setPASS(saved.getPASS());
	        obj_out.writeObject(file_to_send);
	        obj_out.flush();
			
			String file_name;
			System.out.println("coonf");
			int nFiles = message_in.read();
			System.out.println("!!!"+nFiles);
			while(nFiles-- != 0) {
				file_name = string_reader.readLine();
				System.out.println(file_name);
				file_uploaded.add(file_name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return file_uploaded;
		
	}
	
	static void Upload_With_gui(Packet saved) {
		try {
			message_out.write(WAIT_FILE);
			message_out.flush();
			
	        Packet file_to_send = new Packet();
	        //vuole per forza un nuovo oggetto
	        
	        Stack<File> tmp;
	        file_to_send.setUSER(saved.getUSER());
	        file_to_send.setPASS(saved.getPASS());
	        {
				CountDownLatch latch = new CountDownLatch(1);
				GuiFile t = new GuiFile(latch);
				try {
					latch.await();
				}catch (Exception e) {}
				tmp = t.getP();
				for(int i = 0; i < tmp.size(); i++)
					file_to_send.addFILE((new File(tmp.elementAt(i).getName()).toString()));
			}
	        
	
	        obj_out.writeObject(file_to_send);
	        obj_out.flush();
	        
	        try {
	        	File file;
	        	message_out.writeLong(kbBUFFER);
	        	message_out.flush();
		        while((file = tmp.pop()) != null) {
		        	message_out.writeLong(file.length());
		        	System.out.println(file.length());
		        	message_out.flush();
		            FileInputStream fileread = new FileInputStream(file);
		            byte[] buffer = new byte[(int) (kbBUFFER * 1024)];
		            int bytesRead;
		            
					while ((bytesRead = fileread.read(buffer)) > 0) {
						raw_data_out.write(buffer, 0, bytesRead);
					}
					fileread.close();
					
					if(message_in.read() != FILE_RECIVED)
						throw new Exception("file non ricevuti");
		        }
	        }catch(EmptyStackException e) {
	        	System.out.println("finitI");//100%
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
