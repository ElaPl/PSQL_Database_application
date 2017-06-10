DROP TABLE IF EXISTS Person, Event, Friends, Talk, Person_rated_talk, Person_sign_up_talk, Person_sign_up_event,Person_took_part_talk ;
DROP TYPE IF EXISTS user_plan_type CASCADE ;
DROP TYPE IF EXISTS lecture_list_type CASCADE ;
DROP TYPE IF EXISTS lecture_list_with_participants_number_type CASCADE ;
DROP TYPE IF EXISTS signet_for_event_type CASCADE ;

CREATE TYPE lecture_list_type AS 
(talk_id int,
 start_timestamp timestamp,
 title text,
 room int );

CREATE TYPE lecture_list_with_participants_number_type AS 
(talk_id int,
 start_timestamp timestamp,
 title text,
 room int,
 number int );

CREATE TYPE user_plan_type AS 
(login text,
 talk_id int,
 start_timestamp timestamp,
 title text,
 room int );

CREATE TYPE signet_for_event_type AS 
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
	talk_id INTEGER PRIMARY KEY,
	title TEXT NOT NULL,
	event_name TEXT REFERENCES Event(name),
	speaker_login TEXT NOT NULL REFERENCES Person (login),
	room INTEGER,
	start_timestamp TIMESTAMP,
	status CHAR(1) NOT NULL CHECK (status = 'a' or status = 'r' or status = 'w'),
	add_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Person_rated_talk (
	login TEXT NOT NULL REFERENCES Person (login), 
	talk_id INTEGER NOT NULL REFERENCES Talk (talk_id) ON DELETE CASCADE,
	rate INTEGER NOT NULL CHECK(rate >= 0 AND rate <= 10)
);

CREATE TABLE IF NOT EXISTS Person_sign_up_talk (
	login TEXT NOT NULL REFERENCES Person (login), 
	talk_id INTEGER NOT NULL REFERENCES Talk (talk_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Person_sign_up_event (
	login TEXT NOT NULL REFERENCES Person (login), 
	event_name TEXT NOT NULL REFERENCES Event (name)
);

CREATE TABLE IF NOT EXISTS Person_took_part_talk (
	login TEXT NOT NULL REFERENCES Person (login), 
	talk_id INTEGER NOT NULL REFERENCES Talk (talk_id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION IsOrganizer(text, text ) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * 
			FROM person p
			WHERE p.login = $1 AND p.user_password = $2 AND p.role = 'o'))
		THEN
			RETURN FALSE;
		END IF;
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION IsUser(text, text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * 
			FROM person p
			WHERE p.login = $1 AND p.user_password = $2 AND p.role = 'u'))
		THEN
			RETURN FALSE;
		END IF;
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION addOrganizer(newlogin TEXT, newpasssword TEXT) RETURNS BOOLEAN AS
$BODY$
BEGIN
	INSERT INTO Person (login, user_password, role) VALUES ($1, $2, 'o');
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION addEvent(login TEXT, passsword TEXT, event_name TEXT, start_time Timestamp, end_time Timestamp) RETURNS BOOLEAN AS
$BODY$
	BEGIN IF (NOT isOrganizer(login, passsword)) THEN RETURN FALSE; END IF;
	INSERT INTO Event (name, start_timestamp, end_timestamp) VALUES ($3, $4, $5);
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;
	
CREATE OR REPLACE FUNCTION addUser(login TEXT, passsword TEXT, newLogin TEXT, newPassword TEXT) RETURNS BOOLEAN AS
$BODY$
BEGIN 
	IF (NOT isOrganizer(login, passsword)) THEN RETURN FALSE; END IF;
	INSERT INTO Person (login, user_password, role) VALUES ($3, $4, 'u');
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION registerTalk (login TEXT, password TEXT, speakerlogin TEXT, talkID INT, title TEXT, start_timestamp TIMESTAMP, room INT, initial_evaluation INT, eventname TEXT) RETURNS BOOLEAN AS 
$BODY$
DECLARE 
	curr_event event;
	status char(1);
	this_talk talk;
	eventNameToInsert text;
BEGIN
	IF (NOT isOrganizer(login, password)) THEN raise notice '_not organizer'; RETURN FALSE; END IF;
	IF NOT EXISTS (
		SELECT * FROM person p WHERE p.login LIKE speakerlogin AND p.role LIKE 'u') THEN
		raise notice '_user _not exist';
		RETURN FALSE;
	END IF;
	IF (NOT (eventname LIKE '')) THEN
		SELECT * INTO curr_event FROM event WHERE event.name LIKE eventname;
		IF (curr_event IS NULL) THEN  raise notice 'event _not exist'; RETURN FALSE; END IF;
		IF (NOT start_timestamp BETWEEN curr_event.start_timestamp AND curr_event.end_timestamp) THEN 	raise notice 'Wrong _timestamp'; RETURN FALSE; END IF;
	END IF;
	SELECT * INTO this_talk FROM talk WHERE this_talk.talk_id = talkID;
	IF (NOT (this_talk IS NULL)) THEN
		DELETE FROM talk t WHERE t.talk_id = talkID;
		DELETE FROM Person_rated_talk pr WHERE pr.login = login AND pr.talk_id = talkID;
	END IF;	
	IF (eventname LIKE '') THEN eventNameToInsert = NULL; ELSE eventNameToInsert = eventname; END IF;
	INSERT INTO talk(talk_id, title, event_name, speaker_login, room, start_timestamp, status) VALUES (talkID, title, eventNameToInsert, speakerlogin, room, start_timestamp, 'a'	);
	INSERT INTO Person_rated_talk (login, talk_id, rate) VALUES (login, talkID, initial_evaluation);
	RETURN TRUE;
END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION registerUserForEvent(text, text, text) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT (IsUser($1,$2))) THEN
			RETURN FALSE;
		ELSEIF (EXISTS (
			SELECT * FROM person_sign_up_event pe WHERE pe.login = $1 AND pe.event_name = $3)) 
				THEN RETURN FALSE; END IF;
		INSERT INTO person_sign_up_event (login, event_name) VALUES ($1, $3);
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION attendance(text, text, int) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT (IsUser($1,$2))) THEN
			RETURN FALSE;
		ELSEIF (EXISTS (
			SELECT * FROM person_took_part_talk pe WHERE pe.login = $1 AND pe.talk_id = $3)) 
				THEN RETURN FALSE; END IF;
		INSERT INTO person_took_part_talk (login, talk_id) VALUES ($1, $3);
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION evaluation(login text, password text, talkID int, rating int) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * FROM person p WHERE p.login = $1 AND p.user_password = $2)) THEN RETURN FALSE;
		ELSEIF (EXISTS (
			SELECT * FROM person_rated_talk pe WHERE pe.login = $1 AND pe.talk_id = $3)) 
				THEN RETURN FALSE; END IF;
		INSERT INTO person_rated_talk (login, talk_id, rate) VALUES ($1, $3, $4);
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION reject(login text, password text, talkID int) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT (IsOrganizer(login, password))) THEN RETURN FALSE; END IF;

		DELETE FROM talk t WHERE t.talk_id = talkID;
		RETURN TRUE;
	END
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proposal(_login text, _password text, talkID int, title TEXT, start_timestamp TIMESTAMP) RETURNS BOOLEAN AS 
$BODY$
	BEGIN
		IF (NOT EXISTS (
			SELECT * FROM person p WHERE p.login = _login AND p.user_password = _password)) THEN RETURN FALSE; END IF;
		INSERT INTO talk(talk_id, title, event_name, speaker_login, room, start_timestamp, status) VALUES (talkID, title, NULL, _login, NULL, start_timestamp, 'w');
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
	 WHERE p.login=_login
	 ORDER BY t.start_timestamp ASC
	 LIMIT ALL;	
 ELSE
	RETURN QUERY
 	SELECT 
		p.login, t.talk_id, t.start_timestamp, t.title, t.room
	 FROM person_sign_up_event p 
		JOIN talk t USING (event_name)
	 WHERE p.login=_login
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
	WHERE t.start_timestamp::date = require_date
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
		WHERE t.start_timestamp BETWEEN _start_timestamp AND _end_timestamp
		ORDER BY tt.average DESC;
	ELSE 
		RETURN QUERY
		SELECT t.talk_id, t.start_timestamp, t.title, t.room
		FROM temp_table tt
			JOIN talk t USING (talk_id)
		WHERE t.start_timestamp BETWEEN _start_timestamp AND _end_timestamp
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
		SELECT DISTINCT pt.talk_id, count(pt.login) as "participants_number"
		FROM person_took_part_talk pt
			JOIN talk t USING (talk_id)
		WHERE t.start_timestamp BETWEEN _start_timestamp AND _end_timestamp
		GROUP BY pt.talk_id;
 IF (_limit = 0) THEN 	
	 RETURN QUERY
	 SELECT
		t.talk_id, t.start_timestamp, t.title, t.room
	 FROM temp_table_most_pupular_talks tt
		JOIN talk t USING (talk_id)
	 ORDER BY tt.participants_number DESC;	
 ELSE
	RETURN QUERY
	 SELECT
		t.talk_id, t.start_timestamp, t.title, t.room
	 FROM temp_table_most_pupular_talks tt
		JOIN talk t USING (talk_id)
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
	signed_up_event my_type;
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
						WHERE ptpt.login LIKE _login AND ptpt.talk_id = t.talk_id);
	ELSE
	SELECT
		t.talk_id, t.start_timestamp, t.title, t.room, signed
	FROM talk t
		JOIN person_sign_up_event pt USING (event_name)
		JOIN temp_table_abandoned_talks USING (event_name)
	WHERE pt.login = _login AND NOT EXISTS (SELECT *
						FROM person_took_part_talk ptpt
						WHERE ptpt.login LIKE _login AND ptpt.talk_id = t.talk_id)
	LIMIT _limit;
	END IF; 
	DROP TABLE temp_table_abandoned_talks;
END
$X$ LANGUAGE plpgsql;





