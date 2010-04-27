<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
String title = request.getParameter( "title" );
%>

<script type="text/javascript">
<!--
	function handleSetStep(id, s) {
		document.getElementById( 's1' ).className = 'distance';
		document.getElementById( 's2' ).className = 'distance';		
        document.getElementById( 's3' ).className = 'distance';
        document.getElementById( 's4' ).className = 'distance';
        document.getElementById( 's5' ).className = 'distance';
        document.getElementById( id ).className = 'distanceselected';
        
		setStep( s );		
	}
//-->
</script>

<table>
	<tr>
	    <td>
	    <b class="mainGUI"><%= title %></b>
	    <br/>
	    <a id="s1" class="distance" href="javascript:handleSetStep( 's1', 0.1 );">&nbsp;<b>1</b>&nbsp;</a>
	    <a id="s2" class="distance" href="javascript:handleSetStep( 's2', 0.2 );">&nbsp;<b>2</b>&nbsp;</a>
		<a id="s3" class="distance" href="javascript:handleSetStep( 's3', 0.3 );">&nbsp;<b>3</b>&nbsp;</a>
		<a id="s4" class="distance" href="javascript:handleSetStep( 's4', 0.4 );">&nbsp;<b>4</b>&nbsp;</a>
		<a id="s5" class="distance" href="javascript:handleSetStep( 's5', 0.5 );">&nbsp;<b>5</b>&nbsp;</a>
	    </td>
    </tr>
</table>
