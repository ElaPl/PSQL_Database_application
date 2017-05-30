import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mediator {
	private JSONParser myparser = new JSONParser();
	private IDatabase db = new Database();	
	Map<String, IDatabaseMethod> MethodMap = new HashMap<String, IDatabaseMethod>();

	Mediator() {
		MethodMap.put("open", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobc) {
				if (isObjString(jobc.get("baza")) && isObjString(jobc.get("login")) && isObjString(jobc.get("password"))) {
					return db.open((String) jobc.get("baza"), (String) jobc.get("login"),
							(String) jobc.get("password"));
				}
				return db.status_not_impemented();
			}
		});
		
		MethodMap.put("organizer", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobc) {
				if (isObjString(jobc.get("secret")) && isObjString(jobc.get("newlogin")) && isObjString(jobc.get("newpassword"))) {
					return db.organizer((String) jobc.get("secret"),
							(String) jobc.get("newlogin"),
							(String) jobc.get("newpassword"));	
				}
				return db.status_not_impemented();	
			}
		});
		MethodMap.put("event", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.event((String) jobject.get("login"),(String) jobject.get("password"),
						(String) jobject.get("eventname"),  
						Timestamp.valueOf((String)jobject.get("start_timestamp")),
						Timestamp.valueOf((String)jobject.get("start_timestamp")));
			
			}
		});
		MethodMap.put("user", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.user((String) jobject.get("login"),(String) jobject.get("password"),
								(String) jobject.get("newlogin"),(String) jobject.get("newpassword"));
			
			}
		});
	}
	private Timestamp get_timestamp (String str)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Timestamp timestamp = null;
		try{
			System.out.println(str);
	    Date parsedDate = (Date) dateFormat.parse(str);
		System.out.println(str);
	    timestamp = new java.sql.Timestamp(parsedDate.getTime());
		}
	    catch(Exception e) {
	    	 e.printStackTrace();
	    	 System.out.println("Can not parse json date to java date");
	    	 System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	         }
	    return timestamp;
	}
	
	private boolean isObjString(Object obj) {
		if(obj instanceof String){
			return true;
		}
		return false;
	}
	
	public void execute_file(String path) {
		String line = "";
		try {
			FileReader fileReader = new FileReader("inputs/input.json");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				call(line);
			}
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file ");
		} catch (IOException ex) {
			System.out.println("Error reading file");
		}
	}

	public void call(String command) {
		JSONObject jobject;

		try {
			jobject = (JSONObject) myparser.parse(command);
			Set<String> keys = jobject.keySet();
			for (String key : keys) {
				final IDatabaseMethod databaseeMethod = MethodMap.get(key);
				if (databaseeMethod != null) {
					print_result(databaseeMethod.execute((JSONObject) jobject.get(key)));
				} else {
					print_result(db.status_not_impemented());
					System.out.println("Mediator: Taka funkcja nie istnieje");
				}
			}

		} catch (ParseException e) {
			print_result(db.status_error());
			System.out.println("Mediator: Parse error");
		}
	}
	private void print_result(JSONObject jobc) {
		System.out.println(jobc);
	}
}