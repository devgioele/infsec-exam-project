# Information Security Project 2021/2022

Made at the [Free University of Bolzano](https://unibz.it). 

## Setup

Install Java 11 or later.

Download [Tomcat 10.0.x](https://tomcat.apache.org/download-10.cgi) as zip file and remember where you place the 
extracted directory.

Install IntelliJ IDEA and open the project with IntelliJ IDEA. The community edition suffices.

Install the plugin [Smart Tomcat](https://plugins.jetbrains.com/plugin/9492-smart-tomcat).

Open IntelliJ IDEA's settings and go to `Tomcat Server`. Click on the top-left plus sign to add specify where your 
local Tomcat files are.

### Set up the SQL server

Run the container executing the following command from the project's root directory:
```sh
docker compose up -d
```

To connect to the SQL Server in the container and have an interactive shell:

```sh
docker exec -ti mssql bash -c '/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -d dev'
```

_Note: This uses the environment variable `SA_PASSWORD` of the container, not the one of your shell._

Enter the SQL statements specified in the file `create.sql`.

Quit the interactive shell.

## Run the web server

In the top right corner, click on the 'Run Tomcat 1' button to start the web server.
A terminal should open showing Tomcat's logs and the URL to visit.

## Useful SQL queries

### Listing all tables

```sql
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;
```

## Author

- Gioele De Vitti (17693)