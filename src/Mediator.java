import java.util.Iterator;
import java.util.Set;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Mediator {
	JSONParser myparser = new JSONParser();
	
	public void call(String command) {
		JSONObject jobject;
		
		try {
			jobject = (JSONObject)myparser.parse(command);
			Set<String> keys = jobject.keySet();			
			for (String key : keys) {
			    System.out.println(key);
			    ValueEnum enumval = ValueEnum.fromString(myString);
			    switch(key) {
			    	
			    	
			    }
			    
			}
			
			
		} catch (ParseException e) {
			System.out.println("Parse error");
		}
	}
}

/*
for(Iterator iterator = jobject.keySet().iterator(); iterator.hasNext();) {
String key = (String) iterator.next();
System.out.println("--> " + jobject.get(key));
}
*/