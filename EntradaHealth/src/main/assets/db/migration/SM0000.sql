CREATE TABLE IF NOT EXISTS schemaversion
(
  VersionID bigint NOT NULL PRIMARY KEY
);
INSERT INTO schemaversion VALUES (0);

CREATE TABLE IF NOT EXISTS Conversations
(
  ID varchar(255) NOT NULL PRIMARY KEY,
  PatientID bigint,
  OwnerID bigint NOT NULL,
  LastUpdated varchar(255) NOT NULL,
  LastMessage CLOB,
  RecipientIDs CLOB NOT NULL,
  UnreadMessagesCount bigint NOT NULL,
  Roomjid CLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS Messages
(
  ID varchar(255) NOT NULL PRIMARY KEY,
  Text CLOB,
  Type bigint NOT NULL,
  PatientID bigint,
  AuthorID bigint NOT NULL,
  ConversationID varchar(255) NOT NULL,
  AttachmentID varchar(225),
  SentDateTime varchar(255) NOT NULL,
  IsOutgoing boolean NOT NULL,
  IsRead boolean NOT NULL,
  IsDelivered boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS Buddies
(
  ID varchar(255) NOT NULL PRIMARY KEY,
  Username varchar(255) NOT NULL,
  IsFavorite boolean,
  FirstName varchar(255) NOT NULL,
  MI varchar(255),
  LastName varchar(255)
);

CREATE TABLE IF NOT EXISTS PendingInvites
(
  Username varchar(255),
  IsFavorite boolean,
  FirstName varchar(255) NOT NULL,
  MI varchar(255),
  LastName varchar(255)
);

CREATE TABLE IF NOT EXISTS patients
(  
  PatientID     bigint        NOT NULL  PRIMARY KEY,
  MRN           varchar_ignorecase(255)  NOT NULL,
  FirstName     varchar_ignorecase(255)  NOT NULL,
  MI            varchar_ignorecase(255)  NULL,
  LastName      varchar_ignorecase(255)  NOT NULL,
  DOB           varchar_ignorecase(255)  NULL,
  Gender        varchar_ignorecase(20)   NOT NULL,
  Address1      varchar_ignorecase(255)  NULL,
  Address2      varchar_ignorecase(255)  NULL,
  City          varchar_ignorecase(255)  NULL,
  State         varchar_ignorecase(255)  NULL,
  Zip         	varchar_ignorecase(255)  NULL,
  Phone1        varchar_ignorecase(255)  NULL,
  PrimaryCareProviderID	varchar_ignorecase(255)  NULL
);

CREATE TABLE IF NOT EXISTS tou
(
	UserId bigint PRIMARY KEY,
	TOUVersionNumber varchar,
	TOUAccepted boolean
);

