//$HeadURL:
/*----------------------------------------------------------------------------
 This file is part of advregistry3 project

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 
 53177 Bonn
 Germany
 web: http://lat-lon.de/
 EMail: info@lat-lon.de
 ----------------------------------------------------------------------------*/
package org.deegree.client.core.filter;

import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Ensure that the encoding of the request is correct! Use the init parameter with name 'encoding' or 'UTF-8' as
 * default.
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class EncodingFilter implements Filter {

    private static final Logger LOG = getLogger( EncodingFilter.class );

    private String encoding = "UTF-8";

    @Override
    public void init( FilterConfig filterConfig )
                            throws ServletException {
        String enc = filterConfig.getInitParameter( "encoding" );
        LOG.debug( "Init paramater 'encoding' is set to: " + enc );
        if ( enc != null && enc.length() > 0 )
            encoding = enc;
    }

    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {
        if ( request.getCharacterEncoding() == null )
            request.setCharacterEncoding( encoding );
        chain.doFilter( request, response );
    }

    @Override
    public void destroy() {
        // nothing to do here
    }

}
