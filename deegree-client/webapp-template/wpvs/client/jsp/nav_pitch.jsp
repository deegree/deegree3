<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
int[] pitch = { 5, 10, 15, 20, 25, 30, 40, 60, 80 };

String title = request.getParameter( "title" );

%>

<script type="text/javascript">
<!--
	function handleSetPitch(id, p) {
		//document.getElementById( '5dg' ).style.background = '#bdbdd9';
		//document.getElementById( '10dg' ).style.background = '#bdbdd9';
		//document.getElementById( '15dg' ).style.background = '#bdbdd9';
		//document.getElementById( '20dg' ).style.background = '#bdbdd9';
		//document.getElementById( '25dg' ).style.background = '#bdbdd9';
		//document.getElementById( '30dg' ).style.background = '#bdbdd9';
		//document.getElementById( '40dg' ).style.background = '#bdbdd9';
		//document.getElementById( '60dg' ).style.background = '#bdbdd9';
		//document.getElementById( '80dg' ).style.background = '#bdbdd9';
		//document.getElementById( id ).style.background = '#FF6666';
        document.getElementById( '5dg' ).className = 'distance';
        document.getElementById( '10dg' ).className = 'distance';
        document.getElementById( '15dg' ).className = 'distance';
        document.getElementById( '20dg' ).className = 'distance';
        document.getElementById( '25dg' ).className = 'distance';
        document.getElementById( '30dg' ).className = 'distance';
        document.getElementById( '40dg' ).className = 'distance';
        document.getElementById( '60dg' ).className = 'distance';
        document.getElementById( '80dg' ).className = 'distance';
        document.getElementById( id ).className = 'distanceselected';		
		setPitch( p );
	}
//-->
</script>

<table>
    <tr>
    <td>
    <b class="mainGUI"><%= title %></b>
    <br/>
    <a id="5dg" class="distance" href="javascript:handleSetPitch( '5dg', 5 );">&nbsp;5°&nbsp;</a>
    <a id="10dg" class="distance" href="javascript:handleSetPitch( '10dg', 10 );">&nbsp;10°&nbsp;</a>
    <a id="15dg" class="distance" href="javascript:handleSetPitch( '15dg', 15 );">&nbsp;15°&nbsp;</a>
    <a id="20dg" class="distance" href="javascript:handleSetPitch( '20dg', 20 );">&nbsp;20°&nbsp;</a>
    <a id="25dg" class="distance" href="javascript:handleSetPitch( '25dg', 25 );">&nbsp;25°&nbsp;</a>
    <a id="30dg" class="distance" href="javascript:handleSetPitch( '30dg', 30 );">&nbsp;30°&nbsp;</a>
    <a id="40dg" class="distance" href="javascript:handleSetPitch( '40dg', 40 );">&nbsp;40°&nbsp;</a>
    <a id="60dg" class="distance" href="javascript:handleSetPitch( '60dg', 60 );">&nbsp;60°&nbsp;</a>
    <a id="80dg" class="distance" href="javascript:handleSetPitch( '80dg', 80 );">&nbsp;80°&nbsp;</a>
    </td>
    </tr>
</table>
    