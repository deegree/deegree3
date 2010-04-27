<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
String title = request.getParameter( "title" );
%>

<script type="text/javascript">
<!--
	function handleSetDistance(id, d) {
		//document.getElementById( 'd25m' ).style.background = '#bdbdd9';
		//document.getElementById( 'd50m' ).style.background = '#bdbdd9';
		//document.getElementById( 'd100m' ).style.background = '#bdbdd9';
		//document.getElementById( 'd200m' ).style.background = '#bdbdd9';		
		//document.getElementById( 'd500m' ).style.background = '#bdbdd9';
		//document.getElementById( 'd1km' ).style.background = '#bdbdd9';
		//document.getElementById( 'd2.5km' ).style.background = '#bdbdd9';
		//document.getElementById( 'd5km' ).style.background = '#bdbdd9';
        //document.getElementById( 'd10km' ).style.background = '#bdbdd9';
		//document.getElementById( id ).style.background = '#FF6666';
		document.getElementById( 'd5m' ).className = 'distance';
		document.getElementById( 'd10m' ).className = 'distance';		
        document.getElementById( 'd25m' ).className = 'distance';
        document.getElementById( 'd50m' ).className = 'distance';
        document.getElementById( 'd100m' ).className = 'distance';
        document.getElementById( 'd200m' ).className = 'distance';
        document.getElementById( 'd500m' ).className = 'distance';
        document.getElementById( 'd1km' ).className = 'distance';
        document.getElementById( 'd2.5km' ).className = 'distance';
        document.getElementById( 'd5km' ).className = 'distance';
        document.getElementById( id ).className = 'distanceselected';
        
		setDistance( d );		
	}
//-->
</script>

<table>
	<tr>
	    <td>
	    <b class="mainGUI"><%= title %></b>
	    <br/>
	    <a id="d5m" class="distance" href="javascript:handleSetDistance( 'd5m', 5 );">&nbsp;5m&nbsp;</a>
	    <a id="d10m" class="distance" href="javascript:handleSetDistance( 'd10m', 10 );">&nbsp;10m&nbsp;</a>
		<a id="d25m" class="distance" href="javascript:handleSetDistance( 'd25m', 25 );">&nbsp;25m&nbsp;</a>
		<a id="d50m" class="distance" href="javascript:handleSetDistance( 'd50m', 50 );">&nbsp;50m&nbsp;</a>
		<a id="d100m" class="distance" href="javascript:handleSetDistance( 'd100m', 100 );">&nbsp;100m&nbsp;</a>
		<a id="d200m" class="distance" href="javascript:handleSetDistance( 'd200m', 200 );">&nbsp;200m&nbsp;</a>
		<a id="d500m" class="distance" href="javascript:handleSetDistance( 'd500m', 500 );">&nbsp;500m&nbsp;</a>
		<a id="d1km" class="distance" href="javascript:handleSetDistance( 'd1km', 1000 );">&nbsp;1km&nbsp;</a>
		<a id="d2.5km" class="distance" href="javascript:handleSetDistance( 'd2.5km', 2500 );">&nbsp;2.5km&nbsp;</a>
		<a id="d5km" class="distance" href="javascript:handleSetDistance( 'd5km', 5000 );">&nbsp;5km&nbsp;</a>
		<!-- a id="d10km" class="distance" href="javascript:handleSetDistance( 'd10km', 10000 );">&nbsp;10km&nbsp;</a-->			    
	    </td>
    </tr>
</table>
    