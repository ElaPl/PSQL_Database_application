import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Database  implements IDatabase {

	Connection connection = null;
	
	private String url = "jdbc:postgresql://localhost/";
	private String user;
	private String password;
	
	public Map<String, IDatabaseMethod> serviceMethodMap = new HashMap<String, IDatabaseMethod>();
	
	public void open(String bd_name, String user, String password) {
		try {
			Class.forName("org.postgresql.Driver");
			
			url = url + bd_name;
			this.user = user;
			this.password = password;
			try {
				connection = DriverManager.getConnection(url, user, password);
			}catch(SQLException e) {
				e.printStackTrace();
				System.err.println("Can't connect to database :\n" + e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");		
		
	}
	
	public void organizer(String secret, String newlogin, String newpassword) {
		try {
			Statement stmt = connection.createStatement();

	        String sql_command = "INSERT INTO Person (Login, user_password, role) "
        			+ "VALUES ('" + newlogin + "', '" + newpassword + "', 'o');";
	        stmt.executeUpdate(sql_command);
		}  catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("Adden new user : " + newlogin + " password: " + newpassword);
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
