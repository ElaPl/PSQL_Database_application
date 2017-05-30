import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
		try {
			Statement stmt = connection.createStatement();
			String sql_command = "INSERT INTO Person (Login, user_password, role) " + "VALUES ('" + newlogin + "', '"
					+ newpassword + "', 'o');";
			stmt.executeUpdate(sql_command);
		} catch (SQLException e) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject event(String login, String password, String event_name, Timestamp start_time,
			Timestamp end_time) {
		try{
			CallableStatement func = connection.prepareCall("{ ? = call IsOrganizer( ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.execute();
			Boolean isOrganizer = func.getBoolean(1);
			func.close();
			if (!isOrganizer) {
				return status_error();
			}
			String statement = "INSERT INTO event (name, start_timestamp, end_timestamp) VALUES ( ?, ?, ?);";
			PreparedStatement stmt = connection.prepareStatement(statement);
			stmt.setString(1, event_name);
			stmt.setTimestamp(2, start_time);
			stmt.setTimestamp(3, end_time);
			stmt.execute();
		} catch (SQLException e) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject user(String login, String password, String newLogin, String newPassword) {
		try{
			CallableStatement func = connection.prepareCall("{ ? = call IsOrganizer( ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.execute();
			Boolean isOrganizer = func.getBoolean(1);
			func.close();
			if (!isOrganizer) {
				return status_error();
			}
			String statement = "INSERT INTO Person (login, user_password, role) VALUES ( ?, ?, 'u');";
			PreparedStatement stmt = connection.prepareStatement(statement);
			stmt.setString(1, newLogin);
			stmt.setString(2, newPassword);
			stmt.execute();
		} catch (SQLException e) {
			return status_error();
		}
		return status_ok();
	}
	public JSONObject talk(String login, String password, String speakerlogin, int talk
			String title, Timestamp start_timestamp, int room, int initial_evaluation, String eventname) {
			
		
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
