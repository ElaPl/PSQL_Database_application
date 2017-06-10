 import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Database implements IDatabase {

	Connection connection = null;

	private String url = "jdbc:postgresql://localhost/";

	public JSONObject open(String bd_name, String user, String password) {
		try {
			Class.forName("org.postgresql.Driver");
			try {
				connection = DriverManager.getConnection(url + bd_name, user, password);
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
			CallableStatement func = connection.prepareCall("{ ? = call registerTalk( ?, ?, ?, ?, ?, ?, ?, ?, ? ) }");
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
		CallableStatement func = connection.prepareCall("{ ? = call registerUserForEvent( ?, ?, ? ) }");
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
		if (!result) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject attendance(String login, String password, int talkID) {
		boolean result;
		try {
		CallableStatement func = connection.prepareCall("{ ? = call attendance( ?, ?, ? ) }");
		func.registerOutParameter(1, Types.BOOLEAN);
		func.setString(2, login);
		func.setString(3, password);
		func.setInt(4, talkID);
		func.execute();
		result = func.getBoolean(1);
		func.close();
		}catch(SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject evaluation(String login, String password, int talkID, int rate) {
		boolean result;
		try {
		CallableStatement func = connection.prepareCall("{ ? = call evaluation( ?, ?, ?, ? ) }");
		func.registerOutParameter(1, Types.BOOLEAN);
		func.setString(2, login);
		func.setString(3, password);
		func.setInt(4, talkID);
		func.setInt(5, rate);
		func.execute();
		result = func.getBoolean(1);
		func.close();
		}catch(SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}
	
	public JSONObject reject(String login, String password, int talkID) {
		boolean result;
		try {
		CallableStatement func = connection.prepareCall("{ ? = call reject( ?, ?, ? ) }");
		func.registerOutParameter(1, Types.BOOLEAN);
		func.setString(2, login);
		func.setString(3, password);
		func.setInt(4, talkID);
		func.execute();
		result = func.getBoolean(1);
		func.close();
		}catch(SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject proposal(String login, String password, int talkID, String title, Timestamp start_timestamp ) {
		boolean result;
		try {
		CallableStatement func = connection.prepareCall("{ ? = call proposal( ?, ?, ?, ?, ? ) }");
		func.registerOutParameter(1, Types.BOOLEAN);
		func.setString(2, login);
		func.setString(3, password);
		func.setInt(4, talkID);
		func.setString(5, title);
		func.setTimestamp(6, start_timestamp);
		func.execute();
		result = func.getBoolean(1);
		func.close();
		}catch(SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject friends(String loginFrom, String passwordFrom, String loginTo) {
		boolean result;
		try {
		CallableStatement func = connection.prepareCall("{ ? = call friends( ?, ?, ?) }");
		func.registerOutParameter(1, Types.BOOLEAN);
		func.setString(2, loginFrom);
		func.setString(3, passwordFrom);
		func.setString(4, loginTo);
		func.execute();
		result = func.getBoolean(1);
		func.close();
		}catch(SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject user_plan(String login, int limit) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM user_plan(?, ?);");
			func.setString(1, login);
			func.setInt(2, limit);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("login", rs.getString("login"));
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}
	
	public JSONObject day_plan(java.sql.Date timestamp) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM day_plan(?);");
			func.setDate(1, timestamp);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}
	public JSONObject best_talks(Timestamp start_timestamp, Timestamp end_timestamp, int limit, int all) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM best_talks(?, ?, ?, ?);");
			func.setTimestamp(1, start_timestamp);
			func.setTimestamp(2, end_timestamp);
			func.setInt(3, limit);
			func.setInt(4, all);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}
	public JSONObject most_popular_talks(Timestamp start_timestamp, Timestamp end_timestamp, int limit) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM most_popular_talks(?, ?, ?);");
			func.setTimestamp(1, start_timestamp);
			func.setTimestamp(2, end_timestamp);
			func.setInt(3, limit);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}
	
	public JSONObject attended_talks(String login, String password) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM attended_talks(?, ?);");
			func.setString(1, login);
			func.setString(2, password);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}	
		
	public JSONObject abandoned_talks(String login, String password, int limit) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM abandoned_talks(?, ?, ?);");
			func.setString(1, login);
			func.setString(2, password);
			func.setInt(3, limit);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				row.put("number", rs.getInt("number"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}
	public JSONObject recently_added_talks(int limit) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM recently_added_talks(?);");
			func.setInt(3, limit);
			ResultSet rs = func.executeQuery();
	        JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row  = new JSONObject();
				row.put("talk_id", rs.getInt("talk_id"));
				row.put("speakerlogin", rs.getString("speakerlogin"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		}catch(SQLException e) {
			return status_error();
		}
	}

	public JSONObject status_OK_with_data(JSONArray rows) {
		JSONObject result = new JSONObject();
		result.put(status, status_ok);
		result.put("data", rows);
		return result;
	}
	
	public JSONObject status_not_impemented() {
		JSONObject result = new JSONObject();
		result.put(status, status_not_implemented);
		return result;
	}

	public JSONObject status_error() {
		JSONObject result = new JSONObject();
		result.put(status, status_error);
		System.out.println("Error");
		System.exit(0);
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
