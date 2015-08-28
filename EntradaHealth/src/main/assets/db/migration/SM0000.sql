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

CREATE TABLE IF NOT EXISTS Patient
(  
  Address1 varchar(255) NOT NULL,
  City varchar(255) NOT NULL,
  MRN varchar(255) NOT NULL,
  Address2 varchar(255) NOT NULL,
  PrimaryCareProviderID varchar(255) NOT NULL,
  ID bigint NOT NULL PRIMARY KEY,
  State varchar(255) NOT NULL,
  ClinicID varchar(255) NOT NULL,
  DOB varchar(255) NOT NULL,
  FirstName varchar(255) NOT NULL,
  AlternateID varchar(255) NOT NULL,
  Gender varchar(255) NOT NULL,
  MI varchar(255) NOT NULL,
  Zip varchar(255) NOT NULL,
  Phone1 varchar(255) NOT NULL,
  LastName varchar(255) NOT NULL,
  Suffix varchar(255) NOT NULL,
);

CREATE TABLE IF NOT EXISTS tou
(
	UserId bigint PRIMARY KEY,
	TOUVersionNumber varchar,
	TOUAccepted boolean
);

