import org.json.simple.JSONObject;


public interface IDatabase {

	// Standard output format
	public final String status = "status";
	public final String status_ok = "OK";
	public final String status_not_implemented = "NOT IMPLEMENTED";
	public final String status_error = "ERROR";
	
	
	JSONObject open(String bd_name, String user, String password);
	JSONObject organizer(String secret, String newlogin, String newpassword);
	JSONObject return_status_not_impemented();
	JSONObject return_status_error();

	void close();
    
}
