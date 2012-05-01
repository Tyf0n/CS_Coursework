--CREATE TABLE experiment(
--       id INTEGER primary key,
--       runtime INTEGER,
--	 rundatetime varchar(127),
--	 numHosts INTEGER
--);


--CREATE TABLE experiment_hosts(
--       host_id INTEGER primary key,
--       experiment_id INTEGER,
--       host_position INTEGER
--);


--DROP TABLE IF EXISTS experiment_event;

--CREATE TABLE experiment_event(
--       event_id INTEGER primary key,
--       experiment_id INTEGER,
--       time_start REAL,
--       time_duration REAL,
--       event_type VARCHAR(64),
--       created_by_host_id INTEGER,
--       is_self_event INTEGER
--);

--CREATE TABLE experiment_summary(
--	summary_id INTEGER primary key,
--	experiment_id INTEGER,
--	active_hosts INTEGER,
--	packet_size INTEGER,
--	total_packets_sent INTEGER,
--	total_bits_sent INTEGER,
--	experiment_duration REAL,
--	average_transmission_delay REAL
--);

