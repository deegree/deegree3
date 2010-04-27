<%-- $HeadURL: svn+ssh://hrubach@svn.wald.intevation.org/deegree/apps/services-template/trunk/web/snoopy.jsp $ --%>
<%-- $Id: snoopy.jsp 12467 2008-06-20 16:52:29Z jmays $ --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Enumeration" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="expires" content="0">
        <title>snoopy</title>
        <link rel="stylesheet" href="./deegree.css" />
    </head>
    <body marginwidth="0" marginheight="0">
        <jsp:include page="header.jsp" flush="true"/>
        <h1 align="center">Status</h1>
        <table align="center" width="75%" cellpadding="5" cellspacing="5">
            <tr>
                <td width="12%">&nbsp;</td>
                <td>
                    <font size="4">
                        <b>Request Information:</b><br />
                        JSP Request Method: <%= request.getMethod() %>
                        <br />
                        Request URI: <%= request.getRequestURI() %>
                        <br />
                        Request Protocol: <%= request.getProtocol() %>
                        <br />
                        Servlet path: <%= request.getServletPath() %>
                        <br />
                        <% String path = request.getServletPath(); %>
                        Request Scheme: <%= request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path.substring(0,path.lastIndexOf('/') )+"/" %>
                        <br />
                        Path info: <%= request.getPathInfo() %>
                        <br />
                        Path translated: <%= request.getPathTranslated() %>
                        <br />
                        Query string: <%= request.getQueryString() %>
                        <br />
                        Content length: <%= request.getContentLength() %>
                        <br />
                        Content type: <%= request.getContentType() %>
                        <br />
                        Server name: <%= request.getServerName() %>
                        <br />
                        Server IP: <%=java.net.InetAddress.getByName( request.getServerName() ).getHostAddress() %>
                        <br />
                        Server port: <%= request.getServerPort() %>
                        <br />
                        Remote user: <%= request.getRemoteUser() %>
                        <br />
                        Remote address: <%= request.getRemoteAddr() %>
                        <br />
                        Remote host: <%= request.getRemoteHost() %>
                        <br />
                        Authorization scheme: <%= request.getAuthType() %>
                        <br />
                        Authenticated user: <%= request.getUserPrincipal() %>
                        <br />
                        <br /><b>Request Header:</b><br />
                        <%
                        Enumeration<?> e = request.getHeaderNames();
                        while ( e.hasMoreElements() ) {
                          String key = (String)e.nextElement();
                          out.println( key + " = " +request.getHeader( key ) + "<br />" );
                        }
                        
                        out.println( "<br /><b>Request Parameter:</b><br />");
                        e = request.getParameterNames();
                        while ( e.hasMoreElements() ) {
                          String key = (String)e.nextElement();
                          out.println( key + " = " + request.getParameter( key ) + "<br />");
                        }
                        
                        out.println( "<br /><b>Request Attributes:</b><br />");
                        e = request.getAttributeNames();
                        while (e.hasMoreElements() ) {
                          String key = (String)e.nextElement();
                          out.println( key + " = " + request.getAttribute( key ) + "<br />");
                        }
                        %>
                        
                        <br />
                        <b>Session Attributes:</b><br />
                        <%
                        StringBuffer _buf = new StringBuffer("SessionID : " + session.getId() + "<br />" );
                        e = session.getAttributeNames();
                        while ( e.hasMoreElements() ) {
                            String key = (String) e.nextElement();
                            if ( key != null ) {
                                Object value = session.getAttribute( key );
                                if ( value == null ) {
                                    value = new String( "null" );
                                }
                                _buf.append( key + " = " + value + "<br />" );
                            }
                        }
                        out.println( _buf.toString() );
                        
                        out.println( "<br /><b>Init Parameters:</b><br />");
                        _buf.delete( 0, _buf.length() );
                        // show page attributes
                        e = config.getInitParameterNames();
                        while ( e.hasMoreElements() ) {
                            String key = (String) e.nextElement();
                            _buf.append( key + " = " + config.getInitParameter( key ) + "<br />" );
                        }
                        out.println( _buf.toString() );
                        
                        out.println( "<br /><br /><b>Application Context:</b><br />");
                        
                        _buf.delete( 0, _buf.length() );
                        for (e = application.getAttributeNames(); e.hasMoreElements(); ) {
                            String key = e.nextElement().toString();
                            String val = application.getAttribute( key ).toString();
                            _buf.append( key + " = " + val + "<br />" );
                        }
                        out.println( _buf.toString() );
                        %>
                        <br /><br />
                        <b>System Properties:</b><br />
                        Server: <%=application.getServerInfo()%><br />
                        Total Memory: <%=Runtime.getRuntime().totalMemory()/1024%> Kilobyte<br />
                        Free Memory: <%=Runtime.getRuntime().freeMemory()/1024%> Kilobyte<br />
                        <%
                          java.util.Properties sysprops = System.getProperties();
                          _buf.delete( 0, _buf.length() );
                          for (e = sysprops.keys(); e.hasMoreElements() ; ) {
                            String key = e.nextElement().toString();
                            String value = sysprops.getProperty( key );
                            _buf.append( key + " : " + value + "<br />" );
                          }
                          out.println( _buf.toString() );
                        %>
                    </font>
                    <hr />
                    <font size="4">The browser you are using is <%= request.getHeader("User-Agent") %></font>
                    <hr />
                </td>
            </tr>
        </table>
        <jsp:include page="footer.jsp" flush="true"/>
    </body>
</html>
