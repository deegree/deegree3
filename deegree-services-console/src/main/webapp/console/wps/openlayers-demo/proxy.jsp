<%@ page language="java"
    import="java.io.OutputStream,java.io.InputStream,org.apache.commons.httpclient.*,org.apache.commons.httpclient.methods.*,org.apache.commons.io.input.BoundedInputStream"
    pageEncoding="UTF-8"%><%

            HttpClient client = new HttpClient();
    
            // force character encoding to UTF-8
            request.setCharacterEncoding( "UTF-8" );

            //String url = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states";
            String url = request.getParameter( "url" );
            HttpMethodBase http = new GetMethod(url);
            
            long maxFileSize = 1024 * 1024;
 
            try {
                client.executeMethod( http );
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
                    InputStream is = new BoundedInputStream (http.getResponseBodyAsStream(), maxFileSize);
                    byte [] buffer = new byte [4096];
                    int bytesRead = 0;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    
                    os.flush();
                    is.close();
  
            } finally {
                http.releaseConnection();
            }
            
%>