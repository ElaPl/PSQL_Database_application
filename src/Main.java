//import java.sql.Connection;
//import java.sql.DriverManager;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import java.sql.*;
import java.text.ParseException;
import java.util.*;

public class Main {
	public static void main(String args[]) {

		IDatabase db = new Database();

		String line = null;
		try {
			FileReader fileReader = new FileReader("inputs/input.json");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			Mediator mediator = new Mediator();
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
				mediator.call(line);
			}
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file ");
		} catch (IOException ex) {
			System.out.println("Error reading file");
		}
		
		/*
		 * db.open("bd_proj", "ela", "123"); db.organizer("lalala", "ela",
		 * "secret_password"); db.close();
		 */
	}
}