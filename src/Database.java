import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Database implements IDatabase {

	Connection connection = null;

	private String url = "jdbc:postgresql://localhost/";

	public JSONObject open(String bd_name, String user, String password) {
		System.out.println("Try to open database");
		try {
			Class.forName("org.postgresql.Driver");
			try {
				connection = DriverManager.getConnection(url + bd_name, user, password);
			} catch (Exception e) {
				System.out.println("Error connection");
				return status_error();
			}
		} catch (Exception e) {
			System.out.println("Error connection");
			return status_error();
		}
		return status_ok();
	}

	public JSONObject import_configuration(String configPath) {
		try {
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, "person_rated_talk", null);
			if (!tables.next()) {
				try (BufferedReader br = new BufferedReader(new FileReader(configPath))) {
			    //Now read line bye line
			    String thisLine, sqlQuery;
			    try {
			        sqlQuery = "";
			        while ((thisLine = br.readLine()) != null) 
			        {
			            //Skip comments and empty lines
			            if(thisLine.length() > 0 && thisLine.charAt(0) == '-' || thisLine.length() == 0 ) 
			                continue;
			            sqlQuery = sqlQuery + thisLine + " " ;
			            //If one command complete
			           
			            if(((sqlQuery.charAt(sqlQuery.length() - 2) == ';') && !(sqlQuery.substring(0, 26).equals("CREATE OR REPLACE FUNCTION")))
			            		|| (sqlQuery.substring(sqlQuery.length() - 9,sqlQuery.length() - 1).equals("plpgsql;"))) {
			            	sqlQuery = sqlQuery.substring(0, sqlQuery.length()-2);
			           // 	System.out.println(sqlQuery);

			            //    sqlQuery = sqlQuery.replace(';' , ' '); //Remove the ; since jdbc complains			                
			                try {
			                	Statement stmt = connection.createStatement();
			                	stmt.execute(sqlQuery);
				                sqlQuery = "";
			                }
			                catch(SQLException ex) {
			                	return status_error();
			                }
			                catch(Exception ex) {
			                	return status_error();
			                }
			                sqlQuery = "";
			            }   
			        }
				}catch(Exception ex) {
			    	return status_error();
			    }
			} catch(Exception ex) {
		    	return status_error();
		    }
			}
		}catch (Exception e) {
				return status_error();
			}
			return status_ok();
	}

	public JSONObject organizer(String secret, String newlogin, String newpassword) {
		if (!secret.equals("d8578edf8458ce06fbc5bb76a58c5ca4")) {
			return status_error();
		}
		try {
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
		} catch (SQLException e) {
			return status_error();
		}
		return status_ok();

	}

	public JSONObject event(String login, String password, String event_name, Timestamp start_time,
			Timestamp end_time) {
		try {
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
		} catch (SQLException e) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject user(String login, String password, String newLogin, String newPassword) {
		try {
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
		} catch (SQLException e) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject talk(String login, String password, String speakerlogin, String talk_id, String title,
			Timestamp start_timestamp, int room, int initial_evaluation, String eventname) {
		try {
			CallableStatement func = connection
					.prepareCall("{ ? = call registerTalk( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, speakerlogin);
			func.setString(5, talk_id);
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
		} catch (SQLException e) {
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
		} catch (SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject attendance(String login, String password, String talkID) {
		boolean result;
		try {
			CallableStatement func = connection.prepareCall("{ ? = call attendance( ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, talkID);
			func.execute();
			result = func.getBoolean(1);
			func.close();
		} catch (SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject evaluation(String login, String password, String talkID, int rate) {
		boolean result;
		try {
			CallableStatement func = connection.prepareCall("{ ? = call evaluation( ?, ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, talkID);
			func.setInt(5, rate);
			func.execute();
			result = func.getBoolean(1);
			func.close();
		} catch (SQLException e) {
			return status_error();
		}
		if (!result) {
			System.out.println(rate);
			return status_error();
		}
		return status_ok();
	}

	public JSONObject reject(String login, String password, String talkID) {
		boolean result;
		try {
			CallableStatement func = connection.prepareCall("{ ? = call reject( ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, talkID);
			func.execute();
			result = func.getBoolean(1);
			func.close();
		} catch (SQLException e) {
			return status_error();
		}
		if (!result) {
			return status_error();
		}
		return status_ok();
	}

	public JSONObject proposal(String login, String password, String talkID, String title, Timestamp start_timestamp) {
		boolean result;
		try {
			CallableStatement func = connection.prepareCall("{ ? = call proposal( ?, ?, ?, ?, ?, ? ) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, login);
			func.setString(3, password);
			func.setString(4, talkID);
			func.setString(5, title);
			func.setTimestamp(6, start_timestamp);
			func.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

			func.execute();
			result = func.getBoolean(1);
			func.close();
		} catch (SQLException e) {
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
			CallableStatement func = connection.prepareCall("{ ? = call addFriend( ?, ?, ?) }");
			func.registerOutParameter(1, Types.BOOLEAN);
			func.setString(2, loginFrom);
			func.setString(3, passwordFrom);
			func.setString(4, loginTo);
			func.execute();
			result = func.getBoolean(1);
			func.close();
		} catch (SQLException e) {
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
				JSONObject row = new JSONObject();
				row.put("login", rs.getString("login"));
				row.put("talk", rs.getString("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
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
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
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
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
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
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
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
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
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
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				row.put("number", rs.getInt("number"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
			return status_error();
		}
	}

	public JSONObject recently_added_talks(int _limit) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM recently_added_talks(?);");
			func.setInt(1, _limit);
			ResultSet rs = func.executeQuery();
			JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("speakerlogin", rs.getString("login"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
			return status_error();
		}
	}

	public JSONObject rejected_talks(String login, String password) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM rejected_talks(?, ?);");
			func.setString(1, login);
			func.setString(2, password);
			ResultSet rs = func.executeQuery();
			JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("speakerlogin", rs.getString("login"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
			return status_error();
		}
	}

	public JSONObject proposals(String login, String password) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM proposals(?, ?);");
			func.setString(1, login);
			func.setString(2, password);
			ResultSet rs = func.executeQuery();
			JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("speakerlogin", rs.getString("login"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
			return status_error();
		}
	}

	public JSONObject friends_talks(String login, String password, Timestamp start_timestamp, Timestamp end_timestamp,
			int limit) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM friends_talks(?, ?, ?, ?, ?);");
			func.setString(1, login);
			func.setString(2, password);
			func.setTimestamp(3, start_timestamp);
			func.setTimestamp(4, end_timestamp);
			func.setInt(5, limit);
			ResultSet rs = func.executeQuery();
			JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row = new JSONObject();
				row.put("talk", rs.getString("talk_id"));
				row.put("speakerlogin", rs.getString("login"));
				row.put("start_timestamp", rs.getTimestamp("start_timestamp"));
				row.put("title", rs.getString("title"));
				row.put("room", rs.getInt("room"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
			return status_error();
		}
	}

	public JSONObject friends_events(String login, String password, String event) {
		try {
			CallableStatement func = connection.prepareCall("SELECT * FROM friends_events(?, ?, ?);");
			func.setString(1, login);
			func.setString(2, password);
			func.setString(3, event);
			ResultSet rs = func.executeQuery();
			JSONArray list = new JSONArray();
			while (rs.next()) {
				JSONObject row = new JSONObject();
				row.put("login", rs.getString("login"));
				row.put("event", rs.getString("event"));
				row.put("friendlogin", rs.getString("friendlogin"));
				list.add(row);
			}
			return status_OK_with_data(list);
		} catch (SQLException e) {
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
		return result;
	}

	public JSONObject status_ok() {
		JSONObject result = new JSONObject();
		result.put(status, status_ok);
		return result;
	}

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
