<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="stylesheet" href="style.css" type="text/css" />
	
	<title>Home page</title>
</head>
<body>
	<nav class="header">
	  <div class="box">
	  	<div>
			<img src="images/email_icon.jpg" align="left" />
			<p>E-MAIL CLIENT
				<br><% out.println(request.getAttribute("email")); %>
			</p>
	  	</div>
	  	<div id="right">
	  	    <form action="LogoutServlet" method="post">
                <input type="submit" name="logout" value="Logout" />
            </form>
        </div>
	  </div>
	</nav>
	
	<div class="grid-container">
	    <div class="menu">
	        <div class="btn">
	            New Mail
	            <a href="NewMailServlet?email=<%= request.getAttribute("email") %>">
	                <span class="btn-link"></span>
	            </a>
	        </div>
	        <div class="btn">
	            Inbox
	            <a href="InboxServlet?email=<%= request.getAttribute("email") %>">
	                <span class="btn-link"></span>
	            </a>
	        </div>
	        <div class="btn">
	            Sent
	            <a href="SentEmailsServlet?email=<%= request.getAttribute("email") %>">
	                <span class="btn-link"></span>
	            </a>
	        </div>
	    </div>
		<div class="content">
		    <%= request.getAttribute("content")!=null ? request.getAttribute("content") : "" %>
		</div>
	</div>
</body>
</html>