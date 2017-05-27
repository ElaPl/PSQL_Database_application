import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;


public interface IDatabaseMethod {
	JSONObject execute(JSONObject jobject);
}
