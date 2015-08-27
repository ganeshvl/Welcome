ALTER TABLE patients
  ADD (Address1      varchar_ignorecase(255)  NULL,
			  Address2      varchar_ignorecase(255)  NULL,
			  City          varchar_ignorecase(255)  NULL,
			  State         varchar_ignorecase(255)  NULL,
			  Zip         	varchar_ignorecase(255)  NULL,
			  Phone1         varchar_ignorecase(255)  NULL);

			  
CREATE TABLE IF NOT EXISTS express_notes_tags
(
	ID	INT auto_increment PRIMARY KEY,
  JobTypeID     bigint        NOT NULL  ,
  Name          varchar_ignorecase(255)  NOT NULL,
  Required          boolean     NULL
);