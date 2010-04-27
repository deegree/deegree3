<%-- $HeadURL: svn+ssh://hrubach@svn.wald.intevation.org/deegree/apps/services-template/trunk/web/footer.jsp $ --%>
<%-- $Id: footer.jsp 12467 2008-06-20 16:52:29Z jmays $ --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.commons.version.*"%>
<table align="left" border="0" cellPadding="0" cellSpacing="0" width="100%">
    <tr bgcolor="#FFFF00" align="center">
        <td colspan="2">
            <font size="1">
                <b>Version <%--=Version.getVersionName() --%></b>
                <!-- <b>$Revision: <%--=Version.getSvnRevision() --%>$ - Version <%--=Version.getVersion() --%></b> -->
            </font>
        </td>
    </tr>
    <tr bgcolor="#fe5215">
        <td align="center" noWrap>
            <b>
                more info at <a href="http://www.deegree.org/">http://www.deegree.org</a>.
                Copyright by <a href="http://www.lat-lon.de">lat/lon GmbH</a>
                and <a href="http://aggis.uni-bonn.de">Bonn University</a>
            </b>
        </td>
        <td align="right" noWrap>
            <a href="#MENU"><b>[&nbsp;^&nbsp;]</b></a>&nbsp;&nbsp;
        </td>
    </tr>
</table>