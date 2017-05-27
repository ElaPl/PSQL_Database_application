import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
				return db.return_status_not_impemented();
			}
		});
		
		MethodMap.put("organizer", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobc) {
				if (isObjString(jobc.get("secret")) && isObjString(jobc.get("newlogin")) && isObjString(jobc.get("newpassword"))) {
					return db.organizer((String) jobc.get("secret"),
							(String) jobc.get("newlogin"),
							(String) jobc.get("newpassword"));	
				}
				return db.return_status_not_impemented();
				
			}
		});
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
					JSONObject res = new JSONObject();
					res.put(db.status, db.status_not_implemented);
					print_result(res);
				}
			}

		} catch (ParseException e) {
			JSONObject res = new JSONObject();
			res.put(db.status, db.status_not_implemented);
			print_result(res);
		}
	}
	private void print_result(JSONObject jobc) {
		System.out.println(jobc);
	}
}

/*
 * for(Iterator iterator = jobject.keySet().iterator(); iterator.hasNext();) {
 * String key = (String) iterator.next(); System.out.println("--> " +
 * jobject.get(key)); }
 */