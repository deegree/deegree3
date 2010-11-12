<%@ page language="java"
	import="java.io.OutputStream,java.io.InputStream,java.util.Enumeration,org.apache.commons.httpclient.*,org.apache.commons.httpclient.methods.*"
	pageEncoding="UTF-8"%><%

            // PLEASE NOTE:
            //
            // Do *not* add anything (header, whitespace, etc.) in front or after the JSP
            // code in this file -- it will cause Servlet#getOutputStream() to be called more than
            // once and thus the execution will fail!!!
	
            // force character encoding to UTF-8
            request.setCharacterEncoding( "UTF-8" );

            // KVP or XML request?
            boolean isXML = request.getParameter( "GCXML" ) != null ? true : false;

            // GCMethod (GET / POST)
            String method = request.getParameter( "GCMethod" );
            boolean isGET = true;
            if ( method != null && method.equals( "POST" ) ) {
                isGET = false;
            }

            // GCAction (address of service to contact)
            String action = request.getParameter( "GCAction" );
            HttpClient client = new HttpClient();

            HttpMethodBase http = null;

            if ( isXML ) {
                http = new PostMethod( action );
                String xml = request.getParameter( "GCXML" );
                ( (PostMethod) http ).setRequestEntity( new StringRequestEntity( xml, "text/xml",
                                                                                 "UTF-8" ) );
            } else {
                StringBuffer sb = new StringBuffer( 200 );
                Enumeration<?> iterator = request.getParameterNames();
                boolean first = true;
                while ( iterator.hasMoreElements() ) {
                    String param = (String) iterator.nextElement();
                    if ( !param.startsWith( "GC" ) ) {
                        if ( first ) {
                            first = false;
                            sb.append( "?" );
                        } else {
                            sb.append( "&" );
                        }
                        sb.append( param ).append( "=" ).append( request.getParameter( param ) );
                    }
                }
                if ( isGET ) {
                    http = new GetMethod( action + sb.toString() );
                } else {
                    http = new PostMethod( action );
                    ( (PostMethod) http ).setRequestEntity( new StringRequestEntity( sb.toString() ) );
                }
            }

            try {
                client.executeMethod( http );
                // HttpStatus check not needed, we want to see exceptionReports, etc
                //if ( http.getStatusCode() == HttpStatus.SC_OK ) {  
                    if ( http.getResponseHeader( "Content-Type" ) != null ) {
                        String contentType = http.getResponseHeader( "Content-Type" ).getValue();
                        if ( contentType.contains( "xml" ) ) {
                            contentType = "text/xml";
                        }
                        response.setContentType( contentType );        
                    } else {
                        response.setContentType( "text/plain" );
                    }
                    
                    OutputStream os = response.getOutputStream();                     
                    InputStream is = http.getResponseBodyAsStream();
                    byte [] buffer = new byte [4096];
                    int bytesRead = 0;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                    
                //} else {
                //    response.setContentType( "text/plain" );
                //    out.write( "Unexpected failure: " + http.getStatusLine().toString() );
                //}
            } finally {
                http.releaseConnection();
            }
%>