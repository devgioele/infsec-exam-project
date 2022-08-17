-- One-liners for an easy pasting into the database shell

CREATE TABLE [user] (name nvarchar(50)  NOT NULL,surname nvarchar(50) NOT NULL,email nvarchar(50) NOT NULL,password nvarchar(50) NOT NULL,CONSTRAINT user_PK PRIMARY KEY (email));

CREATE TABLE mail (sender nvarchar(50) NOT NULL,receiver nvarchar(50) NOT NULL,signature text, subject nvarchar(100)
NULL,body ntext NOT NULL,[time] datetime2(3) NOT NULL,CONSTRAINT mail_FK FOREIGN KEY (sender) REFERENCES [user](email),CONSTRAINT mail_FK_1 FOREIGN KEY (receiver) REFERENCES [user](email));