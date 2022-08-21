-- One-liners for an easy pasting into the database shell

CREATE TABLE [user] (name nvarchar(50) NOT NULL, surname nvarchar(50) NOT NULL, email nvarchar(50) NOT NULL, password nvarchar(50) NOT NULL, CONSTRAINT user_PK PRIMARY KEY (email));

CREATE TABLE email (sender nvarchar(50) NOT NULL, receiver nvarchar(50) NOT NULL, subject ntext NULL, body ntext NOT NULL, signature ntext, [time] datetime2(3) NOT NULL, CONSTRAINT email_FK FOREIGN KEY (sender) REFERENCES [user] (email), CONSTRAINT email_FK_1 FOREIGN KEY (receiver) REFERENCES [user](email));

-- Assuming that the bit length of the keys is 4096 and that they are stored in base 64,
-- at most ceil((4096+1)/log2(64)) = 683 digits are required. Since base 64 encoding works with groups of 4, the required digits are 684.
CREATE TABLE public_key (email nvarchar(50) PRIMARY KEY, modulus varchar(684) NOT NULL, exponent varchar(684) NOT NULL, CONSTRAINT email_FK2 FOREIGN KEY (email) REFERENCES [user](email));