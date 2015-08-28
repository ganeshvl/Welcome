DROP TABLE referring_physicians;

CREATE TABLE IF NOT EXISTS referring_physicians
(
	ID 	bigint auto_increment PRIMARY KEY,
	ReferringID  bigint      NOT NULL,
 	JobID   bigint      NOT NULL
	
);

CREATE TABLE IF NOT EXISTS ResourceNames
(
	ResourceID varchar(255) NOT NULL PRIMARY KEY,
	ResourceName varchar(255) NOT NULL,
	
);

CREATE TABLE IF NOT EXISTS Schedule
(
	ScheduleID bigint NOT NULL PRIMARY KEY,
	AppointmentStatus bigint NOT NULL,
	PatientID bigint NOT NULL,
	JobId varchar(255),
	ReasonName varchar(255) NOT NULL,
	ResourceID varchar(255) NOT NULL,
	AppointmentDate varchar(255) NOT NULL,
);
