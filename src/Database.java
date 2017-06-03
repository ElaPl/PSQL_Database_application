 import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Date;

import org.json.simple.JSONObject;

public class Database implements IDatabase {

	Connection connection = null;

	private String url = "jdbc:postgresql://localhost/";
	private String user;
	private String password;

	public JSONObject open(String bd_name, String user, String password) {
		try {
			Class.forName("org.postgresql.Driver");

			url = url + bd_name;
			this.user = user;
			this.password = password;
			try {
				connection = DriverManager.getConnection(url, user, password);
			} catch (SQLException e) {
				return status_error();
			}
		} catch (Exception e) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject organizer(String secret, String newlogin, String newpassword) {
		if (!secret.equals("d8578edf8458ce06fbc5bb76a58c5ca4")) {
			return status_error();
		}
		try{
			CallableStatement func = connection.prepareCall("{ ? = call addOrganizer( ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, newlogin);
			func.setString(3, newpassword);
			func.execute();
			Boolean done = func.getBoolean(1);
			if (!done) {
				return status_error();
			}
			func.close();
		}catch(SQLException e) {
			return status_error();
		}
		return status_ok();

	}

	public JSONObject event(String login, String password, String event_name, Timestamp start_time,
			Timestamp end_time) {
		try{
			CallableStatement func = connection.prepareCall("{ ? = call addEvent( ?, ?, ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, event_name);
			func.setTimestamp(5, start_time);
			func.setTimestamp(6, end_time);
			func.execute();
			Boolean done = func.getBoolean(1);
			if (!done) {
				return status_error();
			}
			func.close();
		}catch(SQLException e) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject user(String login, String password, String newLogin, String newPassword) {
		try{
			CallableStatement func = connection.prepareCall("{ ? = call addUser( ?, ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, newLogin);
			func.setString(5, newPassword);
			func.execute();
			Boolean done = func.getBoolean(1);
			if (!done) {
				return status_error();
			}
			func.close();
		}catch(SQLException e) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject talk(String login, String password, String speakerlogin, int talk_id,
			String title, Timestamp start_timestamp, int room, int initial_evaluation, String eventname) {
			try {
			CallableStatement func = connection.prepareCall("{ ? = call registerTalk( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, speakerlogin);
			func.setInt(5, talk_id);
			func.setString(6, title);
			func.setTimestamp(7, start_timestamp);
			func.setInt(8, room);
			func.setInt(9, initial_evaluation);
			func.setString(10, eventname);
			func.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
			func.execute();
			Boolean done = func.getBoolean(1);
			func.close();
			if (!done) {
				return status_error();
			}
			} catch(SQLException e) {
				return status_error();
			}
			return status_ok();
	}
	public JSONObject register_user_for_event(String login, String password, String event_name) {
		boolean result;
		try {
		CallableStatement func = connection.prepareCall("{ ? = call registerUserOnEvent( ?, ?, ? ) }");
		func.registerOutParameter(1, Types.BOOLEAN);
		func.setString(2, login);
		func.setString(3, password);
		func.setString(4, event_name);
		func.execute();
		result = func.getBoolean(1);
		func.close();
		}catch(SQLException e) {
			return status_error();
		}
		if (result) {
			return status_ok();
		}
		else {
			return status_error();
		}
	}
	
	public JSONObject status_not_impemented() {
		JSONObject result = new JSONObject();
		result.put(status, status_not_implemented);
		return result;
	}

	public JSONObject status_error() {
		JSONObject result = new JSONObject();
		result.put(status, status_error);
		return result;
	}

	public JSONObject status_ok() {
		JSONObject result = new JSONObject();
		result.put(status, status_ok);
		return result;
	}

	/*
	 * public void event(String login, String password, String eventname, String
	 * ) { try { Statement stmt = connection.createStatement();
	 * 
	 * String sql_command = "INSERT INTO Person (Login, user_password, role) " +
	 * "VALUES ('" + newlogin + "', '" + newpassword + "', 'o');";
	 * stmt.executeUpdate(sql_command); } catch (SQLException e) {
	 * e.printStackTrace(); System.err.println(e.getClass().getName() + ": " +
	 * e.getMessage()); } System.out.println("Adden new user : " + newlogin +
	 * " password: " + newpassword); }
	 */
	/*
	 * (*O) event <login> <password> <eventname> <start_timestamp>
	 * <end_timestamp> // rejestracja wydarzenia, napis <eventname> jest
	 * unikalny
	 */

	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Closed database successfully");
	}
}
