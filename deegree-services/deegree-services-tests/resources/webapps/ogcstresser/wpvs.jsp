<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="org.deegree.protocol.wpvs.client.WPVSClient"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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
	
	#step1-form table {
		padding: 5px;
		border-collapse: separate;
		border-spacing: 5px;
	}

</style>
<title>Step 1: Introduce getCapabilities URL</title>
</head>
<body>

<%@ include file="menu.html" %>

<div id="step1-form">	
	<table>
		<tr><td>
			<form method=GET action="Controller"> 
			<table>
				<tr><td><b>getCapabilities URL</b>:</td><td><input type="text" name=capabilities size="80"></td></tr>
				<tr><td></td><td><input type=Submit value="Submit" name="step1"></td></tr>
			</table>
			</form>
		</td></tr>
	</table>
</div>

</body>
</html>