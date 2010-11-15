<%@ page language="java" pageEncoding="UTF-8" import="java.io.*"%>
<%
    // PLEASE NOTE:
    //
    // Do *not* add anything (header, whitespace, etc.) in front or after the JSP
    // code in this file -- it will cause Servlet#getOutputStream() to be called more than
    // once and thus the execution will fail!!!

    String responseId = request.getParameter( "responseid" );
    if ( responseId == null ) {
        throw new Exception( "No 'responseid' parameter specified." );
    }

    try {
        File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );

        File responseMimeType = new File( "tmpDir", "response_" + responseId + "_mimetype" );
        BufferedReader reader = new BufferedReader( new FileReader( responseMimeType ) );
        String mimeType = reader.readLine();
        reader.close();
        response.setContentType( mimeType );

        File responseFile = new File( "tmpDir", "response_" + responseId );
        InputStream is = new FileInputStream( responseFile );
        OutputStream os = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
            os.write( buffer, 0, bytesRead );
        }
        os.flush();
        is.close();
    } catch ( Exception e ) {
        throw new Exception( "Unable to perform download of response with id " + responseId + "': "
                             + e.getMessage() );
    }
%>