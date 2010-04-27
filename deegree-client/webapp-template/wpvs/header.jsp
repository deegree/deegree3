<%-- $HeadURL: svn+ssh://hrubach@svn.wald.intevation.org/deegree/apps/services-template/trunk/web/header.jsp $ --%>
<%-- $Id: header.jsp 12467 2008-06-20 16:52:29Z jmays $ --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.commons.version.*" %>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
	<tbody>
		<tr valign="top" bgColor="#fe5215">
			<td align="left" noWrap>
				<font size="2">&nbsp;&nbsp;<b>deegree Version: <%--=Version.getVersion()--%></b>&nbsp;</font>
			</td>
			<td align="right" noWrap>
				<font size="2"><a name="MENU"></a><b>[<a href="index.jsp">Home</a>]</b>&nbsp;&nbsp;</font>
			</td>
		</tr>
		<tr>
            <td align="center" colspan="2" height="100px">
            	<img src="images/logo-deegree.png" alt="deegree" />
            </td>
        </tr>
        <tr>
            <%
	            String mesg = "";
	            Object o = request.getParameter( "mesg" );
	            if ( o != null ) {
	                mesg = (String)o;
	            }
            %>
            <td colspan="2" align="center">
            	<h2><%= mesg %></h2>
            </td>
        </tr>
	</tbody>
</table>
