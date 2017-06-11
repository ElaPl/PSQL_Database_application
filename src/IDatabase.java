import java.sql.Timestamp;
import java.sql.Date;

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
	JSONObject talk(String login, String password, String speakerlogin, String talk,
			String title, Timestamp start_timestamp, int room, int initial_evaluation, String eventname);
	JSONObject register_user_for_event(String login, String password, String event_name);
	JSONObject attendance(String login, String password, String talkID);
	JSONObject evaluation(String login, String password, String talkID, int rate);
	JSONObject reject(String login, String password, String talkID);
	JSONObject proposal(String login, String password, String talkID, String title, Timestamp start_timestamp );
	JSONObject friends(String loginFrom, String passwordFrom, String loginTo);
	JSONObject user_plan(String login, int limit);
	JSONObject day_plan(Date timestamp);
	JSONObject best_talks(Timestamp start_timestamp, Timestamp end_timestamp, int limit, int all);
	JSONObject most_popular_talks(Timestamp start_timestamp, Timestamp end_timestamp, int limit);
	JSONObject attended_talks(String login, String password);
	JSONObject abandoned_talks(String login, String password, int limit);
	JSONObject recently_added_talks(int limit);
	JSONObject rejected_talks(String login, String password);
	JSONObject proposals(String login, String password);
	JSONObject friends_talks(String login, String password, Timestamp _start_timestamp, Timestamp _end_timestamp, int _limit);
	JSONObject friends_events(String login, String password, String event);
	JSONObject import_configuration(String configPath);
	
	// return
	JSONObject status_not_impemented();
	JSONObject status_error();

	void close();
    
}
