<%-- $HeadURL: svn+ssh://hrubach@svn.wald.intevation.org/deegree/apps/services-template/trunk/web/error.jsp $ --%>
<!-- $Id: error.jsp 12467 2008-06-20 16:52:29Z jmays $ -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%
	if ( exception == null && request.getAttribute( "javax.servlet.jsp.jspException" ) != null ) {
        exception = (Exception) request.getAttribute( "javax.servlet.jsp.jspException" );
    }
    if ( exception == null ) {
        exception = new Exception( "Exception UNAVAILABLE: Tracing Stack..." );
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="expires" content="0" />
        <title>Error - <%=exception.getMessage()%></title>
        <link rel="stylesheet" href="./deegree.css" />
	</head>
	<body bgcolor="#FFFFFF" text="#000000" leftmargin="10" rightmargin="10" topmargin="0" 
		marginwidth="0" marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952">
		
	    <jsp:include page="header.jsp" flush="true" />
        <table align="center" width="75%" cellpadding="5" cellspacing="5">
            <tr>
                <td bgcolor="lightgrey" align="center">
                    <h1><font color="red">Error</font></h1>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <b>The following error has occured: <%=exception.getMessage() %></b>
                </td>
            </tr>
            <tr>
                <td>
                    <p>Sorry, your request cannot be completed. The server run into the 
                    following error:</p>
                    <h2>Stack Trace:</h2>
                    <pre>
                          <% exception.printStackTrace( ); %>
                    </pre>
                    <p>Please notify the administrator. Thank you.</p>
                </td>
            </tr>
        </table>
		<jsp:include page="footer.jsp" flush="true" />
	</body>
</html>
