<%@ page language="java" pageEncoding="UTF-8" import="java.io.*,javax.faces.context.FacesContext, org.apache.commons.io.*"%><%
    // PLEASE NOTE:
    //
    // Do *not* add anything (header, whitespace, etc.) in front or after the JSP
    // code in this file -- it will cause Servlet#getOutputStream() to be called more than
    // once and thus the execution will fail!!!

    InputStream is = null;
    OutputStream os = null;
    try {
        String mimeType = request.getParameter( "mt" );
        String fileName = request.getParameter( "file" );

        File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
        File file = new File( tmpDir, fileName );
        if ( FileUtils.directoryContains( tmpDir, file ) && file.exists() ) {
            response.setContentType( mimeType );
            is = new FileInputStream( file );
            os = response.getOutputStream();
            IOUtils.copy( is, os );
        } else {
            throw new Exception( "Unable to download requested file: " + fileName );
        }
    } catch ( Exception e ) {
        e.printStackTrace();
        throw new Exception( "Exception while downloading file", e );
    } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
    }
%>