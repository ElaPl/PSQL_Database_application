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
import static java.lang.Math.toIntExact;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mediator {
	private JSONParser myparser = new JSONParser();
	private IDatabase db = new Database();
	Map<String, IDatabaseMethod> MethodMap = new HashMap<String, IDatabaseMethod>();
	static DateUtil dateUtil = new DateUtil();

	Mediator() {
		MethodMap.put("open", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobc) {
				return db.open((String) jobc.get("baza"), (String) jobc.get("login"), (String) jobc.get("password"));
			}
		});

		MethodMap.put("organizer", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobc) {
				if (isObjString(jobc.get("secret")) && isObjString(jobc.get("newlogin"))
						&& isObjString(jobc.get("newpassword"))) {
					return db.organizer((String) jobc.get("secret"), (String) jobc.get("newlogin"),
							(String) jobc.get("newpassword"));
				}
				return db.status_not_impemented();
			}
		});
		MethodMap.put("event", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.event((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("eventname"),
						convertStringToTimestamp((String) jobject.get("start_timestamp")),
						convertStringToTimestamp((String) jobject.get("end_timestamp")));

			}
		});
		MethodMap.put("user", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.user((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("newlogin"), (String) jobject.get("newpassword"));
			}
		});

		MethodMap.put("talk", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.talk((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("speakerlogin"), (String) jobject.get("talk"),
						(String) jobject.get("title"), convertStringToTimestamp((String) jobject.get("start_timestamp")),
						toInt(jobject.get("room")), toInt(jobject.get("initial_evaluation")),
						(String) jobject.get("eventname"));
			}
		});
		MethodMap.put("register_user_for_event", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.register_user_for_event((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("eventname"));
			}
		});
		MethodMap.put("attendance", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.attendance((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("talk"));
			}
		});
		MethodMap.put("evaluation", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.evaluation((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("talk"), toIntExact((Long)jobject.get("rating")));
			}
		});
		MethodMap.put("reject", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.reject((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("talk"));
			}
		});
		MethodMap.put("proposal", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.proposal((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("talk"), (String) jobject.get("title"),
						convertStringToTimestamp((String) jobject.get("start_timestamp")));
			}
		});
		MethodMap.put("friends", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.friends((String) jobject.get("login1"), (String) jobject.get("password"),
						(String) jobject.get("login2"));
			}
		});
		MethodMap.put("user_plan", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.user_plan((String) jobject.get("login"), toInt(jobject.get("limit")));
			}
		});
		
		MethodMap.put("day_plan", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.day_plan(convertStringToDate((String) jobject.get("timestamp")));
			}
		});
		
		MethodMap.put("best_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.best_talks(convertStringToTimestamp((String) jobject.get("start_timestamp")),
						convertStringToTimestamp((String) jobject.get("end_timestamp")),
						toInt(jobject.get("limit")), toInt(jobject.get("all")));
			}
		});
		
		MethodMap.put("most_popular_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.most_popular_talks(convertStringToTimestamp((String) jobject.get("start_timestamp")),
						convertStringToTimestamp((String) jobject.get("end_timestamp")),
						toInt(jobject.get("limit")));
			}
		});
		
		MethodMap.put("attended_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.attended_talks((String) jobject.get("login"), (String) jobject.get("password"));
			}
		});
		
		MethodMap.put("abandoned_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.abandoned_talks((String) jobject.get("login"), (String) jobject.get("password"),
						toInt(jobject.get("number")));
			}
		});
		
		MethodMap.put("recently_added_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.recently_added_talks(toInt(jobject.get("limit")));
			}
		});
		MethodMap.put("rejected_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.rejected_talks((String) jobject.get("login"), (String) jobject.get("password"));
			}
		});
		MethodMap.put("proposals", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.proposals((String) jobject.get("login"), (String) jobject.get("password"));
			}
		});
		MethodMap.put("friends_talks", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.friends_talks((String) jobject.get("login"), (String) jobject.get("password"),
						convertStringToTimestamp((String) jobject.get("start_timestamp")), convertStringToTimestamp((String) jobject.get("end_timestamp")),
						toInt(jobject.get("limit")));
			}
		});
		MethodMap.put("friends_events", new IDatabaseMethod() {
			public JSONObject execute(JSONObject jobject) {
				return db.friends_events((String) jobject.get("login"), (String) jobject.get("password"),
						(String) jobject.get("eventname"));
			}
		});
	}

	private int toInt(Object obj) {
		int result;
		try {
			result = Integer.parseInt((String) obj);
		}catch (Exception e) {
			result = toIntExact((Long) obj);
		}
		return result;
	}
	
	private Date convertStringToDate(String str_date) {
		java.util.Date date = dateUtil.stringToDate(str_date);
		return new Date(date.getTime());
	}
	
	private Timestamp convertStringToTimestamp(String str_date) {
		java.util.Date date = dateUtil.stringToDate(str_date);
		return new Timestamp(date.getTime());
	}

	private Timestamp get_timestamp(String str) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Timestamp timestamp = null;
		try {
			System.out.println(str);
			Date parsedDate = (Date) dateFormat.parse(str);
			System.out.println(str);
			timestamp = new java.sql.Timestamp(parsedDate.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Can not parse json date to java date");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return timestamp;
	}

	private boolean isObjString(Object obj) {
		if (obj instanceof String) {
			return true;
		}
		return false;
	}

	public void execute_file(String path) {
		String line = "";
		
		try {
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			if ((line = bufferedReader.readLine()) != null) {
				open_database(line);
			}
			while ((line = bufferedReader.readLine()) != null) {
				//System.out.println(line);
				call(line);
			}
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file ");
		} catch (IOException ex) {
			System.out.println("Error reading file");
		}
	}

	public void open_database(String command) {
		JSONObject jobject;

		try {
			jobject = (JSONObject) myparser.parse(command);
			Set<String> keys = jobject.keySet();
			for (String key : keys) {
				final IDatabaseMethod databaseeMethod = MethodMap.get(key);
				if (databaseeMethod != null) {
					JSONObject ob = databaseeMethod.execute((JSONObject) jobject.get(key));
					if ((String) ob.get("status") == "OK") {
						JSONObject ob2 = db.import_configuration("config/config.sql");
						print_result(ob2);
						if ((String) ob2.get("status") != "OK") {
							System.exit(0);
						}
					}else {
						print_result(db.status_error());
						System.exit(0);
					}
				} else {
					print_result(db.status_not_impemented());
					System.exit(0);
				}
			}

		} catch (ParseException e) {
			print_result(db.status_error());
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
				}
			}

		} catch (ParseException e) {
			print_result(db.status_error());
		}
	}

	private void print_result(JSONObject jobc) {
		System.out.println(jobc);
	}
}