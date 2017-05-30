import java.sql.Timestamp;

import org.json.simple.JSONObject;


public interface IDatabase {

	// Standard output format
	public final String status = "status";
	public final String status_ok = "OK";
	public final String status_not_implemented = "NOT IMPLEMENTED";
	public final String status_error = "ERROR";
	
	
	JSONObject open(String bd_name, String user, String password);
	JSONObject organizer(String secret, String newlogin, String newpassword);
	JSONObject event(String login, String password, String event_name, Timestamp start_time, Timestamp end_time);
	JSONObject user(String login, String password, String newLogin, String newPassword);

	// return
	JSONObject status_not_impemented();
	JSONObject status_error();

	void close();
    
}
