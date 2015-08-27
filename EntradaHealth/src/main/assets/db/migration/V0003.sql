ALTER TABLE patients
  ADD (PrimaryCareProviderID	bigint         NULL);

CREATE TABLE IF NOT EXISTS physicians
(
	ReferringID		bigint      NOT NULL  PRIMARY KEY,
	PhysicianID		varchar_ignorecase(255)  NOT NULL,
	ClinicID		bigint        NOT NULL,
	FirstName     	varchar_ignorecase(255)  NOT NULL,
  	MI            	varchar_ignorecase(255)  NULL,
  	LastName      	varchar_ignorecase(255)  NOT NULL,
  	DOB           	varchar_ignorecase(255)  NULL,
  	Gender        	varchar_ignorecase(20)   NOT NULL,
  	Address1      	varchar_ignorecase(255)  NULL,
	Address2      	varchar_ignorecase(255)  NULL,
	City          	varchar_ignorecase(255)  NULL,
	State         	varchar_ignorecase(255)  NULL,
	Zip           	varchar_ignorecase(255)  NULL,
	Phone1        	varchar_ignorecase(255)  NULL
);

CREATE TABLE IF NOT EXISTS referring_physicians
(
	ReferringID		bigint      NOT NULL  PRIMARY KEY,
	JobID			bigint      NOT NULL
	
);

