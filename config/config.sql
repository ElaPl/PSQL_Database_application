DROP TABLE IF EXISTS Person, Event, Friends, Talk, Person_rated_talk, Person_sign_up_talk, Person_sign_up_event,Person_took_part_talk ;
DROP TYPE IF EXISTS user_plan_type CASCADE ;
DROP TYPE IF EXISTS lecture_list_type CASCADE ;
DROP TYPE IF EXISTS lecture_list_with_participants_number_type CASCADE ;
DROP TYPE IF EXISTS signed_for_event_type CASCADE ;
DROP TYPE IF EXISTS talk_without_room_type CASCADE ;

CREATE TYPE lecture_list_type AS 
(talk_id text,
 start_timestamp timestamp,
 title text,
 room int );

CREATE TYPE lecture_list_with_participants_number_type AS 
(talk_id text,
 start_timestamp timestamp,
 title text,
 room int,
 number int );

CREATE TYPE user_plan_type AS 
(login text,
 talk_id text,
 start_timestamp timestamp,
 title text,
 room int );

CREATE TYPE talk_without_room_type AS 
(talk_id text,
 login text,
 start_timestamp timestamp,
 title text);

CREATE TYPE signed_for_event_type AS 
(event_name text,
 participants int);


CREATE TABLE IF NOT EXISTS Person (
	login TEXT PRIMARY KEY, 
	user_password VARCHAR(20) NOT NULL,
	role CHAR(1) NOT NULL CHECK (Role = 'u' or Role = 'o')	
);

CREATE TABLE IF NOT EXISTS Event (
	name TEXT PRIMARY KEY, 
	start_timestamp TIMESTAMP NOT NULL,
	end_timestamp TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS Friends (
	loginFrom TEXT NOT NULL REFERENCES Person (login), 
	loginTo TEXT NOT NULL REFERENCES Person (login)
);

CREATE TABLE IF NOT EXISTS Talk (	
	talk_id TEXT PRIMARY KEY,
	title TEXT NOT NULL,
	event_name TEXT REFERENCES Event(name),
	speaker_login TEXT NOT NULL REFERENCES Person (login),
	room INTEGER,
	start_timestamp TIMESTAMP,
	status CHAR(1) NOT NULL CHECK (status = 'a' or status = 'r' or status = 'w'),
	add_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS Person_rated_talk (
	login TEXT NOT NULL REFERENCES Person (login), 
	talk_id TEXT NOT NULL REFERENCES Talk (talk_id) ON DELETE CASCADE,
	rate INTEGER NOT NULL CHECK(rate >= 0 AND rate <= 10)
);

CREATE TABLE IF NOT EXISTS Person_sign_up_talk (
	login TEXT NOT NULL REFERENCES Person (login), 
	talk_id TEXT NOT NULL REFERENCES Talk (talk_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Person_sign_up_event (
	login TEXT NOT NULL REFERENCES Person (login), 
	event_name TEXT NOT NULL REFERENCES Event (name)
);

CREATE TABLE IF NOT EXISTS Person_took_part_talk (
	login TEXT NOT NULL REFERENCES Person (login), 
	talk_id TEXT NOT NULL REFERENCES Talk (talk_id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION IsOrganizer(_login text, _password text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * 
			FROM person p
			WHERE p.login = _login AND p.user_password = _password AND p.role = 'o'))
		THEN
			RETURN FALSE;
		END IF;
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION IsUser(_login text, _password text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * 
			FROM person p
			WHERE p.login = _login AND p.user_password = _password AND p.role = 'u'))
		THEN
			RETURN FALSE;
		END IF;
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION addOrganizer(_newlogin TEXT, _newpasssword TEXT) RETURNS BOOLEAN AS
$BODY$
BEGIN
	INSERT INTO Person (login, user_password, role) VALUES (_newlogin, _newpasssword, 'o');
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION addEvent(_login TEXT, _passsword TEXT, _event_name TEXT, _start_time Timestamp, _end_time Timestamp) RETURNS BOOLEAN AS
$BODY$
	BEGIN IF (NOT isOrganizer(_login, _passsword)) THEN RETURN FALSE; END IF;
	INSERT INTO Event (name, start_timestamp, end_timestamp) VALUES (_event_name, _start_time, _end_time);
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;
	
CREATE OR REPLACE FUNCTION addUser(_login TEXT, _passsword TEXT, _newLogin TEXT, _newPassword TEXT) RETURNS BOOLEAN AS
$BODY$
BEGIN 
	IF (NOT isOrganizer(_login, _passsword)) THEN RETURN FALSE; END IF;
	INSERT INTO Person (login, user_password, role) VALUES (_newLogin, _newPassword, 'u');
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION registerTalk (_login TEXT, _password TEXT, _speakerlogin TEXT, _talkID TEXT, title TEXT, start_timestamp TIMESTAMP, room INT, initial_evaluation INT, eventname TEXT, _add_time TIMESTAMP) RETURNS BOOLEAN AS 
$BODY$
DECLARE 
	curr_event event;
	status char(1);
	this_talk talk;
	eventNameToInsert text;
	prev_add_time timestamp;
BEGIN
	IF (NOT isOrganizer(_login, _password)) THEN raise notice '_not organizer'; RETURN FALSE; END IF;
	IF (NOT (eventname LIKE '')) THEN
		SELECT * INTO curr_event FROM event WHERE event.name LIKE eventname;
		IF (curr_event IS NULL) THEN  raise notice 'event _not exit'; RETURN FALSE; END IF;
		IF (NOT start_timestamp BETWEEN curr_event.start_timestamp AND curr_event.end_timestamp) THEN 	raise notice 'Wrong _timestamp'; RETURN FALSE; END IF;
	END IF;
	SELECT * INTO this_talk FROM talk WHERE talk_id = _talkID;
	IF (NOT (this_talk IS NULL)) THEN
		_add_time = this_talk.add_time;
		DELETE FROM talk t WHERE t.talk_id = _talkID;
		DELETE FROM Person_rated_talk pr WHERE pr.login = _login AND pr.talk_id = _talkID;
	END IF;	
	IF (eventname LIKE '') THEN eventNameToInsert = NULL; ELSE eventNameToInsert = eventname; END IF;
	INSERT INTO talk(talk_id, title, event_name, speaker_login, room, start_timestamp, status, add_time) VALUES (_talkID, title, eventNameToInsert, _speakerlogin, room, start_timestamp, 'a', _add_time);
	INSERT INTO Person_rated_talk (login, talk_id, rate) VALUES (_login, _talkID, initial_evaluation);
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION registerUserForEvent(_login text, _password text, _event_name text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT (IsUser(_login,_password))) THEN
			RETURN FALSE;
		ELSEIF (EXISTS (
			SELECT * FROM person_sign_up_event pe WHERE pe.login = _login AND pe.event_name = _event_name)) 
				THEN RETURN TRUE; END IF;
		INSERT INTO person_sign_up_event (login, event_name) VALUES (_login, _event_name);
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION attendance(_login text, _password text, _talk_id text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT (IsUser(_login, _password))) THEN
			RETURN FALSE;
		ELSEIF (NOT EXISTS (SELECT t.talk_id FROM talk t WHERE t.talk_id = _talk_id AND t.status = 'a')) THEN RETURN FALSE;
		ELSEIF (EXISTS (
			SELECT * FROM person_took_part_talk pe WHERE pe.login = _login AND pe.talk_id = _talk_id)) 
				THEN RETURN TRUE; END IF;
		INSERT INTO person_took_part_talk (login, talk_id) VALUES (_login, _talk_id);
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION evaluation(_login text, _password text, _talkID text, _rate int) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT (IsUser(_login, _password))) THEN RETURN FALSE; END IF;
		IF (EXISTS (SELECT * FROM person_rated_talk pe WHERE pe.login = _login AND pe.talk_id = _talkID)) 
				THEN DELETE FROM person_rated_talk pe WHERE pe.login = _login AND pe.talk_id = _talkID; END IF;
		INSERT INTO person_rated_talk (login, talk_id, rate) VALUES (_login, _talkID, _rate);
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION reject(_login text, _password text, _talkID text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (select talk_id from talk where talk_id = _talkID)) THEN RAISE EXCEPTION 'Talk not exist'; END IF;
		IF (IsOrganizer(_login, _password)) THEN 
			UPDATE talk SET status = 'r' WHERE talk_id = _talkID;
			RETURN TRUE;
		ELSEIF ((IsUser (_login, _password)) AND (EXISTS (SELECT tt.talk_id, tt.speaker_login FROM talk tt WHERE tt.talk_id = _talkID AND tt.speaker_login LIKE _login))) THEN
			UPDATE talk SET status = 'r' WHERE talk_id = _talkID;
			RETURN TRUE;
		ELSE RETURN FALSE;	
		END IF;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proposal(_login text, _password text, talkID text, title TEXT, start_timestamp TIMESTAMP, _add_time TIMESTAMP) RETURNS BOOLEAN AS 
$BODY$
BEGIN
	IF (NOT EXISTS (
		SELECT * FROM person p WHERE p.login = _login AND p.user_password = _password)) THEN RETURN FALSE; END IF;
	INSERT INTO talk(talk_id, title, event_name, speaker_login, room, start_timestamp, status, add_time) VALUES (talkID, title, NULL, _login, NULL, start_timestamp, 'w', _add_time);
	RETURN TRUE;	
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION friends(login1 text, password1 text, login2 TEXT) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * FROM person p WHERE p.login = login1 AND p.user_password = password1)) THEN  raise notice 'not exist'; RETURN FALSE; END IF;
		INSERT INTO friends(loginfrom, loginto) VALUES (login1, login2);
		RETURN TRUE;	
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION user_plan(_login text, _limit int) RETURNS SETOF user_plan_type AS
$X$
DECLARE 
	result user_plan_type;
BEGIN
 IF (_limit = 0) THEN 	
	RETURN QUERY
	 SELECT
		p.login, t.talk_id, t.start_timestamp, t.title, t.room
	 FROM person_sign_up_event p 
		JOIN talk t USING (event_name)
	 WHERE p.login=_login AND t.status = 'a'
	 ORDER BY t.start_timestamp ASC
	 LIMIT ALL;	
 ELSE
	RETURN QUERY
 	SELECT 
		p.login, t.talk_id, t.start_timestamp, t.title, t.room
	 FROM person_sign_up_event p 
		JOIN talk t USING (event_name)
	 WHERE p.login=_login AND t.status = 'a'
	 ORDER BY t.start_timestamp ASC
	 LIMIT _limit;
 END IF;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION day_plan(require_date date) RETURNS SETOF lecture_list_type AS
$X$
BEGIN
	RETURN QUERY
	SELECT 
		t.talk_id, t.start_timestamp, t.title, t.room
	FROM talk t
	WHERE t.start_timestamp::date = require_date AND t.status = 'a'
	ORDER BY t.room ASC, t.start_timestamp ASC;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION best_talks(_start_timestamp timestamp, _end_timestamp timestamp, _limit int, _all int) RETURNS SETOF lecture_list_type AS
$X$
BEGIN
	IF (_all = 1) THEN
 		CREATE TEMP TABLE IF NOT EXISTS temp_table AS
		SELECT 
			t.talk_id, (sum(t.rate)/count(t.login)) as "average"
		FROM person_rated_talk t
		GROUP BY t.talk_id;
	ELSE 
 		CREATE TEMP TABLE IF NOT EXISTS temp_table AS
		SELECT 
			t.talk_id, (sum(t.rate)/count(t.login)) as "average"
		FROM person_rated_talk t
			JOIN person_took_part_talk USING (talk_id)
		GROUP BY t.talk_id;
	END IF;
	IF (_limit = 0 ) THEN
		RETURN QUERY
		SELECT t.talk_id, t.start_timestamp, t.title, t.room
		FROM temp_table tt
			JOIN talk t USING (talk_id)
		WHERE (t.start_timestamp BETWEEN _start_timestamp AND _end_timestamp) AND t.status != 'r'
		ORDER BY tt.average DESC;
	ELSE 
		RETURN QUERY
		SELECT t.talk_id, t.start_timestamp, t.title, t.room
		FROM temp_table tt
			JOIN talk t USING (talk_id)
		WHERE t.start_timestamp BETWEEN _start_timestamp AND _end_timestamp AND t.status != 'r'
		ORDER BY tt.average DESC
		LIMIT _limit;
	END IF;
	DROP TABLE temp_table;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION most_popular_talks(_start_timestamp timestamp, _end_timestamp timestamp, _limit int) RETURNS SETOF lecture_list_type AS
$X$
BEGIN
CREATE TEMP TABLE IF NOT EXISTS temp_table_most_pupular_talks AS
		SELECT DISTINCT t.talk_id, count(pt.login) as "participants_number"
		FROM talk t
			LEFT JOIN person_took_part_talk pt USING (talk_id)
		WHERE t.start_timestamp BETWEEN _start_timestamp AND _end_timestamp
		GROUP BY t.talk_id;
 IF (_limit = 0) THEN 	
	 RETURN QUERY
	 SELECT
		t.talk_id, t.start_timestamp, t.title, t.room
	 FROM temp_table_most_pupular_talks tt
		JOIN talk t USING (talk_id)
	 WHERE t.status != 'r'
	 ORDER BY tt.participants_number DESC;	
 ELSE
	RETURN QUERY
	 SELECT
		t.talk_id, t.start_timestamp, t.title, t.room
	 FROM temp_table_most_pupular_talks tt
		JOIN talk t USING (talk_id)
	 WHERE t.status != 'r'
	 ORDER BY tt.participants_number DESC
	 LIMIT _limit;
 END IF;
 DROP TABLE temp_table_most_pupular_talks;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION attended_talks(_login text, _password text) RETURNS SETOF lecture_list_type AS
$X$
BEGIN
	IF (NOT EXISTS (
			SELECT * FROM person p WHERE p.login = _login AND p.user_password = _password)) THEN RAISE EXCEPTION 'User not exists'; END IF;
	RETURN QUERY
	SELECT DISTINCT
		t.talk_id, t.start_timestamp, t.title, t.room
	FROM talk t
		JOIN person_took_part_talk pt USING (talk_id)
	WHERE pt.login = _login;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION abandoned_talks(_login text, _password text, _limit int) RETURNS SETOF lecture_list_with_participants_number_type AS
$X$
DECLARE 
	signed_up_event signed_for_event_type;
BEGIN
	IF (NOT EXISTS (
			SELECT * FROM person p WHERE p.login = _login AND p.user_password = _password)) THEN RAISE EXCEPTION 'User not exists'; END IF;
	
	CREATE TEMP TABLE IF NOT EXISTS temp_table_abandoned_talks AS
	SELECT event_name::text, count(login)::int as "signed" 
		FROM person_sign_up_event 
		GROUP BY event_name;
	IF (_limit = 0 ) THEN
	RETURN QUERY	
	SELECT
		t.talk_id, t.start_timestamp, t.title, t.room, signed
	FROM talk t
		JOIN person_sign_up_event pt USING (event_name)
		JOIN temp_table_abandoned_talks USING (event_name)
	WHERE pt.login = _login AND NOT EXISTS (SELECT *
						FROM person_took_part_talk ptpt
						WHERE ptpt.login LIKE _login AND ptpt.talk_id = t.talk_id)
	ORDER BY signed DESC;
	ELSE
	SELECT
		t.talk_id, t.start_timestamp, t.title, t.room, signed
	FROM talk t
		JOIN person_sign_up_event pt USING (event_name)
		JOIN temp_table_abandoned_talks USING (event_name)
	WHERE pt.login = _login AND NOT EXISTS (SELECT *
						FROM person_took_part_talk ptpt
						WHERE ptpt.login LIKE _login AND ptpt.talk_id = t.talk_id)
	ORDER BY signed DESC
	LIMIT _limit;
	END IF; 
	DROP TABLE temp_table_abandoned_talks;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION recently_added_talks(_limit int) RETURNS SETOF user_plan_type AS
$X$
BEGIN
	IF (_limit = 0) THEN
		RETURN QUERY
		SELECT 
			t.speaker_login::text, t.talk_id::int, t.start_timestamp::timestamp, t.title::text, t.room::int
		FROM talk t
		ORDER BY t.add_time DESC;
	ELSE 
		RETURN QUERY
		SELECT 
			t.talk_id, t.speaker_login, t.start_timestamp, t.title, t.room
		FROM talk t
		ORDER BY t.add_time DESC
		LIMIT _limit;
	END IF;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION rejected_talks(_login text, _password text) RETURNS SETOF talk_without_room_type AS
$X$
BEGIN
	IF (IsOrganizer(_login, _password)) THEN
		RETURN QUERY
		SELECT 
			t.talk_id, t.speaker_login, t.start_timestamp, t.title
		FROM talk t
		WHERE t.status = 'r' 
		ORDER BY t.add_time ASC;
	ELSEIF (IsUser(_login, _password)) THEN
		RETURN QUERY
		SELECT 
			t.talk_id, t.speaker_login, t.start_timestamp, t.title
		FROM talk t
		WHERE t.status = 'r' AND t.speaker_login = _login
		ORDER BY t.add_time ASC;
	ELSE 	
		RAISE EXCEPTION 'Wrong login or password';
	END IF;
END
$X$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proposals(_login text, _password text) RETURNS SETOF talk_without_room_type AS
$X$
BEGIN
	IF (IsOrganizer(_login, _password)) THEN
		RETURN QUERY
		SELECT 
			t.talk_id, t.speaker_login, t.start_timestamp, t.title
		FROM talk t
		WHERE t.status = 'w' 
		ORDER BY t.add_time ASC;
	ELSE 	
		RAISE EXCEPTION 'Wrong login or password';
	END IF;
END
$X$ LANGUAGE plpgsql;

