package server;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.Properties;


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
	
	public int GetMaxConn() {
		return Integer.valueOf(config.getProperty("connection.max"));
	}
	
	public int GetPort() {
		return Integer.valueOf(config.getProperty("server.port"));
	}
	
	public String GetDir() {
		return config.getProperty("server.dir");
	}
	
	public int GetMaxAttempts() {
		return Integer.valueOf(config.getProperty("server.attempts"));
	}
}

public class Server {
	

	static Config config_file = new Config("config.properties");

	
	static final int MAX_CONN = config_file.GetMaxConn();
	static final int port = config_file.GetPort();
	static final String upload_dir = config_file.GetDir();
	static final int MAX_ATTEMPTS = config_file.GetMaxAttempts();
	private static Semaphore Client_max = new Semaphore(MAX_CONN);
	
	public static void main(String[] args) {
		
        ServerSocket server;
        handler.setDir(upload_dir);
        handler.maxAtt(MAX_ATTEMPTS);
        
        try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        
        try {
            while (true) {
                Socket clientSocket = server.accept();
                if (Auth.isAlive() && Client_max.tryAcquire() ) {
                    new Thread(new handler(clientSocket,Client_max)).start();
                    System.out.println("connection accepted" + clientSocket);
                } else {
                    System.out.println("connection refused " + clientSocket);
                    new Thread(() -> {
            			try {
            				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
							out.write(5);
							out.flush();
							out.close();
							clientSocket.close();
						} catch (Exception e) {e.printStackTrace();}
                    }).start();
                }
            }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

}
