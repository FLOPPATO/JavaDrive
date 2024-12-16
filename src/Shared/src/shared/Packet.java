package shared;


import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Stack;

public class Packet implements Serializable{
	
	private static String salt = "-*88PPsale";
	
	static public String SHA256(String pass) {
		String Sha;
		try {
			MessageDigest digest;
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest((pass+salt).getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
		    for (byte b : hash)
		        hexString.append(String.format("%02x", b));
			Sha = hexString.toString();
		} catch (Exception e) { e.printStackTrace(); Sha = null; }
		return Sha;
	}
	
	private String USER,PASS;
	private Stack<String> file_names;
	private boolean newu;
	
	public String getFILEname() {
		return file_names.pop();
	}
	
	public void addFILE(String file_name) {
		file_names.add(file_name);
	}
	
	public Packet(){
		 USER = PASS = null;
		 file_names = new Stack<>();
		 newu = false;
	}

	public void setUSER(String USER) {
		if(this.USER == null)this.USER = USER;
	}
	
	public String getUSER() {
		return USER;
	}
	
	public void setPASS(String PASS) {
		if(this.PASS == null)this.PASS = PASS;
	}
	
	public String getPASS() {
		return PASS;
	}
	
	public void newUSER() {
		newu = true;
	}
	
	public boolean isNEW() {
		return newu;
	}
	
	
	
}
