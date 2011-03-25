<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="org.deegree.protocol.wpvs.client.WPVSClient, java.net.URL,java.net.MalformedURLException,java.util.List"
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
	
	#menu table {
		padding: 5px;
		border-collapse: separate;
		border-spacing: 20px;
	}	
			
	#smallfont {
		font-size: 13px;
	}
	
	table.one-row-table {
		padding: 0px;
		border-collapse: separate;
		border-spacing: 20px 10px;
	}

	tr.odd {	
		background-color: #C7C7C7;
	}
	
	tr.even {
		background-color: #EBEBEB;
	}	
	
</style>
	<title>Step 2: Specify getView parameters</title>
</head>


<body>
<%@ include file="menu.html" %>
<div id="ParameterSelection">
<form action="Controller" method=GET>
	<table>
	<tr class="odd"><td>
	<table class="one-row-table">

	<%
	String capab = (String) request.getSession().getAttribute( "capab" );
	WPVSClient client = new WPVSClient( new URL( capab ) );
	List<String> datasets = client.getQueryableDatasets(); 
	%>

	<tr>
	<td>Datasets </td>
	<td><select multiple size="<%= datasets.size() %>" name="datasets">
	<%
	for ( String data: datasets ) {
	    out.print( "<option>" + data + "</option>" );
	}
	%>
	</select></td>

	<% 
	List<String> elevModels = client.getElevationModels(); 
	%>

	<td>Elevation Model <select name="elevModel">
	<%
	for ( String model: elevModels ) {
	    out.println( "<option>" + model + "</option>" );
	}
	%>
	</select></td>

	<td>CRS <input type="text" name="crs" size="12" value="EPSG:31466">
	</td>
	</tr>
	</table>
	</td></tr>
	
	<tr class="even"><td>
	<table class="one-row-table">
	<tr>
	<td>Point-of-interest</td>
	<td><div id="smallfont">x <input type="text" name="poiX" size="6"></div></td>
	<td><div id="smallfont">y <input type="text" name="poiY" size="6"></div></td>
	<td><div id="smallfont">z <input type="text" name="poiZ" size="6"></div></td>
	</tr>
	</table>
	</td></tr>

	<tr class="odd"><td>
	<table class="one-row-table">
	<tr>
	<td>Orientation</td>
	<td><div id="smallfont">pitch <input type="text" name="pitch" size="3"></div></td>
	<td><div id="smallfont">yaw <input type="text" name="yaw" size="3"></div></td>
	<td><div id="smallfont">roll <input type="text" name="roll" size="3"></div></td>
	<td><div id="smallfont">distance <input type="text" name="distance" size="7"></div></td>
	</tr>
	</table>
	</td></tr>

	<tr class="even"><td>
	<table class="one-row-table">
	<tr>	
	<td>Viewing threshold</td>
	<td><div id="smallfont">angle-of-view <input type="text" name="aov" size="3"></div></td>
	<td><div id="smallfont">far-clipping-plane <input type="text" name="clipping" size="7"></div></td>
	</tr>
	</table>
	</td></tr>

	<tr class="odd"><td>
	<table class="one-row-table">
	<tr>
	<td>Image parameters</td>
	<td><div id="smallfont">width <input type="text" name="width" size="5"></div></td>
	<td><div id="smallfont">height <input type="text" name="height" size="5"></div></td>
	<td><div id="smallfont">scale <input type="text" name="scale" size="5"></div></td>
	</tr>
	</table>
	</td></tr>

	<tr><td>
	<table class="one-row-table">
	<tr>
	<td><input type="submit" value="Submit" name="step2"></td>
	</tr>
	</table>
	</td></tr>
	</table>
</form>
</div>

</body>

</html>