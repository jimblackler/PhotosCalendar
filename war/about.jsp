<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="net.jimblackler.picacal.Authenticate"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Photos Calendar</title>
<link rel="stylesheet" type="text/css" href="style.css" />
<style type="text/css">
h1 {
	font-size: 150%;
	padding-left: 60px;
	padding-top: 10px;
	padding-bottom: 10px;
	padding-top: 10px;
}

li {
	font-size: large;
	padding-top: 20px;
	padding-bottom: 20px;
	line-height: 200%;
	padding-top: 20px;
}

li img.illustrate {
	position: absolute;
	left: 540px;
}

li img.arrow {
	float: right;
	position: relative;
	left: 50px;
}
</style>
</head>
<%
  String authenticateUrl = Authenticate.getAuthUrl(request);
%>
<body>
	<h1>
		<a href="<%=authenticateUrl%>"><img src="go0.png" alt="View now"
			style="vertical-align: -24px" onmouseover="this.src='go1.png'"
			onmouseout="this.src='go0.png'"></a> or learn how it works...
	</h1>

	<div style="width: 470px">
		<ol>
			<li style="min-height: 170px;"><img class="illustrate" src="step1.png"><b>You're
					redirected to the Picasa website, and asked for access to your data.</b> This includes
				pictures from your Google+ posts.
				<div>
					<img class="arrow" src="arrow.png">
				</div></li>
			<li style="min-height: 140px;"><img class="illustrate" src="step2.png"><b>Google
					Calendar prompts you to add the Photo Calendar layer.</b> The numbers on the URL are unique
				to you.
				<div>
					<img class="arrow" src="arrow.png">
				</div></li>
			<li style="min-height: 200px;"><img class="illustrate" src="step3.png"><b>The
					calendar appears in Other Calendars</b>, but the events may not appear for about a minute.
				If you see an error message, restart the process from the beginning.
				<div>
					<img class="arrow" src="arrow.png">
				</div></li>
			<li style="min-height: 140px;"><img class="illustrate" src="step4.png"><b>You
					can now see links to your Picasa photos alongside your other Calendar events.</b> Remember
				to look back from 'today'; all of your photos were taken in the past!
				<div>
					<img class="arrow" src="arrow.png">
				</div></li>
			<li style="min-height: 140px;"><img class="illustrate" src="step5.png"><b>To
					view the actual photos, click the event, 'More details', then the link in the
					description.</b>
				<div>
					<img class="arrow" src="arrow.png">
				</div></li>
		</ol>
	</div>
	<h1>
		<a href="<%=authenticateUrl%>"><img src="go0.png" alt="View now"
			style="vertical-align: -24px" onmouseover="this.src='go1.png'"
			onmouseout="this.src='go0.png'"></a>
	</h1>
	<h1>More questions</h1>
	<ul>
		<li><b>How do I remove the calendar?</b>
			<p>In Google Calendar, click on Calendar Settings, the Calendars tab, and find the
				calendar in Other Calendars. Click 'unsubscribe'.</p>
			<p>
				If you like, you can also revoke the permission you granted in <a
					href="https://accounts.google.com/b/0/IssuedAuthSubTokens">My Google Accounts</a>.
			</p></li>
		<li><b>How secure is it?</b>
			<p>The app generates a very long URL (web address) for your calendar data. Only if
				you decide to share the URL will anyone be able to see your photo calendar. Even then,
				they will only be able to see the picture names, times and links. Having the calendar
				URL adds no permissions to view photos.</p></li>
		<li><b>Does the app expose any Google Calendar data?</b>
			<p>
				No. The app cannot read any data from Calendar. It just adds an <em>extra</em> calendar
				to your list of calendars.
			</p></li>
	</ul>

	<script src="timezones.js" type="text/javascript"></script>
	<script src="script.js" type="text/javascript"></script>
</body>
</html>