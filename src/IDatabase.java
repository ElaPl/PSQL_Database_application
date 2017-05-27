
import java.sql.*;


public interface IDatabase {

	void open(String bd_name, String user, String password);
	void organizer(String secret, String newlogin, String newpassword);


	void close();
    
}
