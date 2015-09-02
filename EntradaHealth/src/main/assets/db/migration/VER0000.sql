CREATE TABLE IF NOT EXISTS schemaversion
(
  VersionID bigint NOT NULL PRIMARY KEY
);
INSERT INTO schemaversion VALUES (0);

CREATE TABLE IF NOT EXISTS EUser
(
	Name varchar(255) NOT NULL PRIMARY KEY,
	Password varchar(255) NOT NULL,
	Environment varchar(255) NOT NULL,
	CurrentDictator varchar(255) NOT NULL,	
	IsCurrent boolean NOT NULL,
	QBUserName varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Dictator
(
	DictID bigint NOT NULL PRIMARY KEY,
	Name varchar(255) NOT NULL,
	Clinic varchar(255) NOT NULL,
	Username varchar(255) NOT NULL,
	IsCurrent boolean NOT NULL,
);
