<%@ page language="java" pageEncoding="UTF-8" import="java.io.*,javax.faces.context.FacesContext"%>
<%
    // PLEASE NOTE:
    //
    // Do *not* add anything (header, whitespace, etc.) in front or after the JSP
    // code in this file -- it will cause Servlet#getOutputStream() to be called more than
    // once and thus the execution will fail!!!

    try {
        String mimeType = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get( "mt" );
        File file = (File) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get( "file" );
        response.setContentType( mimeType );
        InputStream is = new FileInputStream( file );
        OutputStream os = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
            os.write( buffer, 0, bytesRead );
        }
        os.flush();
        is.close();
    } catch ( Exception e ) {
        throw new Exception( "Unable to perform download: " + e.getMessage() );
    }
%>