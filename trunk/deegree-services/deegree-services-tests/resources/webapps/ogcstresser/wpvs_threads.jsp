<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Step 3: How many threads/requests?</title>
<style type="text/css">

	body {
		font-size: 16px;
		font-family: sans-serif;
	}
	
	body table {
		padding: 5px;
		border-collapse: separate;
		border-spacing: 20px;
	}
	
	#step3-form table {
		padding: 5px;
		border-collapse: separate;
		border-spacing: 5px;
	}

</style>
</head>
<body>

<%@ include file="menu.html" %>

<div id="step3-form">	
	<table>
		<tr><td>
			<form method=GET action="Controller"> 
			<input type="hidden" name="action" value="step3">
			<table>
				<tr><td>number of threads: </td><td><input type="text" name=threadNo> <br /></td></tr>
				<tr><td>number of requests per thread: </td><td><input type="text" name=requestNo> <br /></td></tr>
				<tr><td /><td><input type="checkbox" name=imgornot value="displayimg">display thumbnail images (for all requests)<br /></td></tr>
				<tr><td></td><td><input type=Submit value="Submit" name="step3"></td></tr>
			</table>
			</form>
		</td></tr>
	</table>
</div>

</body>
</html>