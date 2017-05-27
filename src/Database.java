import java.sql.*;
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
				return_status_error();
			}
		} catch (Exception e) {
			return_status_error();
		}
		JSONObject result = new JSONObject();
		result.put(status, status_ok);
		return result;
	}

	public JSONObject organizer(String secret, String newlogin,
			String newpassword) {
		try {
			Statement stmt = connection.createStatement();
			String sql_command = "INSERT INTO Person (Login, user_password, role) "
					+ "VALUES ('"
					+ newlogin
					+ "', '"
					+ newpassword
					+ "', 'o');";
			stmt.executeUpdate(sql_command);
		} catch (SQLException e) {
			return_status_error();
		}
		JSONObject result = new JSONObject();
		result.put(status, status_ok);
		return result;
	}

	public JSONObject return_status_not_impemented() {
		JSONObject result = new JSONObject();
		result.put(status, status_not_implemented);
		return result;
	}

	public JSONObject return_status_error() {
		JSONObject result = new JSONObject();
		result.put(status, status_error);
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
