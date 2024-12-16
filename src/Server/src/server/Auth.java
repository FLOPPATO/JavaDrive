package server;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

class Db_Config {
	
	private Properties config;
	
	Db_Config(String file_name){
		try {
			config = new Properties();
			FileInputStream input = new FileInputStream(file_name);
			config.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String GetUrl() {
		return config.getProperty("database.url");
	}
	
	public String GetUser() {
		return config.getProperty("database.username");
	}
	
	public String GetPass() {
		System.out.println(config.getProperty("database.password"));
		return config.getProperty("database.password");
		
	}
}

public class Auth {
	
	static Db_Config config = new Db_Config("config.properties");
	
    static final String URL = config.GetUrl();
    static final String USER = config.GetUser();
    static final String PASS = config.GetPass();
	
	static public boolean addUser(String username, String Hashed_password) {
        boolean rc = true;
        try {
        	Connection connection = null;
        	connection = DriverManager.getConnection(URL, USER, PASS);
            String querySQL= "SELECT * FROM users where username = ?";
            PreparedStatement st = connection.prepareStatement(querySQL);
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if(rs.next()) {
	            rc = true; //username is not primary_key
            }else {
                String createSQL = "INSERT INTO users (username,hash) values (?,?)";
                PreparedStatement stcreate = connection.prepareStatement(createSQL);
                stcreate.setString(1, username);
                stcreate.setString(2, Hashed_password);
                stcreate.execute();
            }
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            rc = false;
        }
        return rc;
	}
	
	static public boolean login(String username, String Hashed_password) {
        boolean rc = true;

        try {
        	Connection connection = null;
            connection = DriverManager.getConnection(URL, USER, PASS);
            String querySQL= "SELECT * FROM users where username = ? and hash = ?";
            PreparedStatement st = connection.prepareStatement(querySQL);
            st.setString(1, username);
            st.setString(2, Hashed_password);
            ResultSet rs = st.executeQuery();
            if(rs.next()) {
	            System.out.println("USER: " + rs.getString("username") + " welcome");
            }else {
	            rc = false;
            }
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            rc = false;
        }
        return rc;
	}
	
	static public boolean isAlive() {
	    boolean rc = false;
        try {
        	Connection connection = null;
			connection = DriverManager.getConnection(URL, USER, "");
			PreparedStatement st = connection.prepareStatement("SELECT 1");
			rc = st.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return rc;
	}
	
}
