<%@ page language="java" pageEncoding="UTF-8" import="java.io.*,javax.faces.context.FacesContext, org.apache.commons.io.IOUtils"%><%
    // PLEASE NOTE:
    //
    // Do *not* add anything (header, whitespace, etc.) in front or after the JSP
    // code in this file -- it will cause Servlet#getOutputStream() to be called more than
    // once and thus the execution will fail!!!

    InputStream is = null;
    OutputStream os = null;
    try {
        String mimeType = request.getParameter("mt");
        File file = new File ( new File (System.getProperty("java.io.tmpdir")),request.getParameter("file") );
        response.setContentType( mimeType );
        is = new FileInputStream( file );
        os = response.getOutputStream();
        IOUtils.copy(is, os);
    } catch ( Exception e ) {
        e.printStackTrace();
        throw new Exception( "Unable to perform download: " + e.getMessage() );
    } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
    }
%>