<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
            String directions = request.getParameter( "directionlabels" );
            String[] dirs = directions.split( "," );
%>
<table cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="5" align="center"><%=dirs[0]%><br />
    <a href="javascript:changePOI('UP')"> <img src="../images/north_direction2.gif" border="0" /> </a></td>
  </tr>
  <tr height="40px">
    <td width="42px" align="right" valign="middle"><%=dirs[3]%>&nbsp;</td>
    <td width="42px" align="left"><a href="javascript:changePOI('LEFT')"> <img
      src="../images/west_direction2.gif" align="middle" border="0" /> </a></td>
    <td width="42px" align="center" width="50px">&nbsp;</td>
    <td width="42px" align="right"><a href="javascript:changePOI('RIGHT')"> <img
      src="../images/east_direction2.gif" align="middle" border="0" /> </a></td>
    <td width="42px" align="left" valign="middle">&nbsp;<%=dirs[1]%></td>
  </tr>
  <tr>
    <td colspan="5" align="center"><a href="javascript:changePOI('DOWN')"> <img
      src="../images/south_direction2.gif" border="0" /> </a> <br />
    <%=dirs[2]%></td>
  </tr>
</table>
