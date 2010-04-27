<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%String title = request.getParameter( "title" );%>
<table width="200px" class="mainGUI">
  <tr>
    <td>
      <a href="javascript:incrementYaw(10)">
        <img src="../images/spin_ccw.gif" border="0"/>
      </a>
    </td>
    <td align="center">
      <b><%= title %></b><br/>
      (Current: <span id="yawTxtArea"></span>&deg;)
    </td>
    <td>
      <a href="javascript:incrementYaw(-10)">
        <img src="../images/spin_cw.gif" border="0"/>
      </a>
    </td>
  </tr>
</table>
