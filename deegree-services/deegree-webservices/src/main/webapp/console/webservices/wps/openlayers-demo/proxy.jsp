<%@page import="java.net.URL"%><%@ page language="java"
    import="java.io.OutputStream,java.io.InputStream,org.apache.commons.io.input.BoundedInputStream"
    pageEncoding="UTF-8"%><%

            // force character encoding to UTF-8
            request.setCharacterEncoding( "UTF-8" );

            //String url = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states";
            String urlStr = request.getParameter( "url" );
            URL url = new URL(urlStr);
            
            long maxFileSize = 1024 * 1024 * 2;

             String contentType ="text/xml;charset=UTF-8"; //text/xml; subtype=gml/2.1.2;charset=UTF-8
             response.setContentType( contentType );        

             OutputStream os = response.getOutputStream();                     
             InputStream is = new BoundedInputStream (url.openStream(), maxFileSize);
             byte [] buffer = new byte [4096];
             int bytesRead = 0;
             while ((bytesRead = is.read(buffer)) != -1) {
               os.write(buffer, 0, bytesRead);
             }
                    
             os.flush();
             is.close();
        
%>