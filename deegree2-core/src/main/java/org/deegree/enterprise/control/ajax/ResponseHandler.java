package org.deegree.enterprise.control.ajax;

import java.io.IOException;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.stringtree.json.JSONWriter;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class ResponseHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( ResponseHandler.class );

    private HttpServletResponse response;

    private OutputStream os;

    private XSLTDocument xslt;

    /**
     * 
     * @param response
     */
    ResponseHandler( HttpServletResponse response ) {
        this.response = response;
    }

    private void openStream()
                            throws IOException {
        if ( os == null ) {
            os = response.getOutputStream();
        }
    }

    /**
     * sets content type
     * 
     * @param contentType
     */
    public void setContentType( String contentType ) {
        response.setContentType( contentType );
    }

    /**
     * sets locale/character encoding
     * 
     * @param locale
     */
    public void setLocale( Locale locale ) {
        response.setLocale( locale );
    }

    /**
     * sets content length
     * 
     * @param length
     */
    public void setContentLength( int length ) {
        response.setContentLength( length );
    }

    /**
     * sets an xslt script that will be used if a XML document will be written; - {@link #writeAndClose(XMLFragment)}
     * 
     * @param xslt
     */
    void setXSLT( XSLTDocument xslt ) {
        this.xslt = xslt;
    }

    /**
     * write a string result back to the client and closes the output stream. 'text/plain; charset=utf-8' will be used
     * as default if no content-type has been set.
     * 
     * @param value
     * @throws IOException
     */
    public void writeAndClose( String value )
                            throws IOException {
        if ( response.getContentType() == null ) {
            response.setContentType( "text/plain; charset=" + CharsetUtils.getSystemCharset() );
        }
        openStream();
        byte[] b = value.getBytes();
        setContentLength( b.length );
        os.write( b );
        os.flush();
        os.close();
    }

    /**
     * write a XML result back to the client (if a xslt script has been set the document will be transformed first) and
     * closes the output stream.<br>
     * 'text/plain; charset=utf-8' will be used as default if no content-type has been set.
     * 
     * @param value
     * @throws IOException
     */
    public void writeAndClose( XMLFragment value )
                            throws IOException {
        if ( response.getContentType() == null ) {
            response.setContentType( "text/plain; charset=" + CharsetUtils.getSystemCharset() );
        }
        openStream();

        if ( xslt != null ) {
            Source xmlSource = new DOMSource( value.getRootElement() );
            Source xslSource = new DOMSource( xslt.getRootElement() );
            try {
                XSLTDocument.transform( xmlSource, xslSource, new StreamResult( os ), null, null );
            } catch ( TransformerException e ) {
                LOG.logError( e.getMessage(), e );
                throw new IOException( e.getMessage() );
            }
        } else {
            value.write( os );
        }
        os.flush();
        os.close();
    }

    /**
     * 'application/json; charset=$deafultCharset$' will be used as default if no content-type has been set.
     * 
     * @param emitClassName
     * @param value
     * @throws IOException
     */
    public void writeAndClose( boolean emitClassName, Object value )
                            throws IOException {

        if ( response.getContentType() == null ) {
            response.setContentType( "application/json; charset=" + Charset.defaultCharset().displayName() );
        }
        value.getClass().getModifiers();
        JSONWriter writer = new JSONWriter( emitClassName );
        writeAndClose( writer.write( value ) );
    }
    
    /**
     * 
     * @return original HttpServletResponse object
     */
    public HttpServletResponse getHttpServletResponse() {
        return response;
    }

}
