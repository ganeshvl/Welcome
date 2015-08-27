CREATE TABLE IF NOT EXISTS encounters
(
  EncounterID   bigint        NOT NULL PRIMARY KEY,
  ApptDate      varchar_ignorecase(255)  NOT NULL,
  PatientID     bigint        NOT NULL,
  Attending     varchar(255)  NULL
);


CREATE TABLE IF NOT EXISTS job_types
(
  JobTypeID     bigint        NOT NULL  PRIMARY KEY,
  Name          varchar_ignorecase(255)  NOT NULL,
  DisableGenericJobs varchar_ignorecase(5)  NOT NULL
);

CREATE TABLE IF NOT EXISTS jobs
(
  JobID         bigint      NOT NULL  PRIMARY KEY,
  JobNumber     varchar_ignorecase(255) NOT NULL,
  EncounterID   bigint      NOT NULL,
  JobTypeID     bigint      NOT NULL,
  Stat          boolean     NULL,
  LocalFlags    int         NOT NULL
);


CREATE TABLE IF NOT EXISTS dictations
(
  DictationID     bigint                  NOT NULL PRIMARY KEY,
  JobID           bigint                  NOT NULL,
  DictationTypeID bigint                  NOT NULL,
  DictatorID      bigint                  NOT NULL,
  QueueID         bigint                  NULL,
  Status          bigint                  NOT NULL,
  Duration        bigint                  NOT NULL,
  MachineName     varchar_ignorecase(255) NULL,
  Filename        varchar_ignorecase(255) NULL,
  ClientVersion   varchar_ignorecase(255) NULL,
);


CREATE TABLE IF NOT EXISTS patients
(
  PatientID     bigint        NOT NULL  PRIMARY KEY,
  MRN           varchar_ignorecase(255)  NOT NULL,
  FirstName     varchar_ignorecase(255)  NOT NULL,
  MI            varchar_ignorecase(255)  NULL,
  LastName      varchar_ignorecase(255)  NOT NULL,
  DOB           varchar_ignorecase(255)  NULL,
  Gender        varchar_ignorecase(20)   NOT NULL
  );

CREATE TABLE IF NOT EXISTS queues
(
  QueueID       bigint        NOT NULL  PRIMARY KEY,
  Name          varchar_ignorecase(255)  NOT NULL,
  Description   varchar_ignorecase(255)  NULL
);

CREATE TABLE IF NOT EXISTS groupqueues
(
	GroupID INT,
	Name varchar_ignorecase(255)  NOT NULL
	
	
);

CREATE TABLE IF NOT EXISTS favoritegroupname
(
	GroupID INT auto_increment PRIMARY KEY,
	GroupName          varchar_ignorecase(255)
  
);

ALTER TABLE groupqueues 
   ADD FOREIGN KEY (GroupID) REFERENCES favoritegroupname (GroupID);

CREATE TABLE IF NOT EXISTS schemaversion
(
  VersionID bigint NOT NULL PRIMARY KEY
);
INSERT INTO schemaversion VALUES (0);
CREATE TABLE IF NOT EXISTS extras
(
  RequirePassCode int NOT NULL
  
);



--INSERT INTO favoritegroupname(GroupName) VALUES ('Test');

INSERT INTO extras VALUES (0);


