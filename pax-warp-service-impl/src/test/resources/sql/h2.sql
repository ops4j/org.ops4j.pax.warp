CREATE TABLE numbers (   id int NOT NULL,   i8 tinyint NOT NULL,   i16 smallint NOT NULL,   i32 int NOT NULL,   i64 bigint NOT NULL,   d decimal NOT NULL ); 
CREATE TABLE strings (   id varchar(255) NOT NULL,   c4 char(4) NOT NULL,   c254 char(254) NOT NULL,   v4 varchar(4) NOT NULL,   v255 varchar(255) NOT NULL,   t text NOT NULL,   enabled boolean NOT NULL ); 
CREATE TABLE numbers_strings (   number_id int NOT NULL,   string_id varchar(255) NOT NULL ); 
ALTER TABLE numbers ADD PRIMARY KEY (id); 
ALTER TABLE strings ADD PRIMARY KEY (id); 
ALTER TABLE numbers_strings ADD PRIMARY KEY (number_id, string_id); 
ALTER TABLE numbers_strings ADD CONSTRAINT FK_NUMBERS_STRINGS_NUMBERS   FOREIGN KEY (number_id) REFERENCES numbers (id); 
ALTER TABLE numbers_strings ADD CONSTRAINT FK_NUMBERS_STRINGS_STRINGS   FOREIGN KEY (string_id) REFERENCES strings (id);  
