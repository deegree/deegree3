//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.controller.utils;

import org.apache.logging.log4j.Logger;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.RequestLogger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.currentTimeMillis;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoggingHttpResponseWrapper extends HttpServletResponseWrapper {

    private static final Logger LOG = getLogger( LoggingHttpResponseWrapper.class );

    private File requestLog;

    private boolean successfulOnly;

    private String kvp;

    private boolean exceptionSent;

    private long entryTime;

    private boolean logged;

    private RequestLogger logger;

    private Credentials creds;

    private String address;

    private InputStream request;

    /**
     * If XML request should possibly be logged.
     * 
     * @param address
     * 
     * @param response
     * @param requestLog
     * @param successfulOnly
     * @param creds
     * @param logger
     * @param request
     *            is closed before logging is initiated if not null
     */
    public LoggingHttpResponseWrapper( String address, HttpServletResponse response, File requestLog,
                                       boolean successfulOnly, RequestLogger logger, InputStream request ) {
        super( response );
        this.address = address;
        this.requestLog = requestLog;
        this.successfulOnly = successfulOnly;
        this.logger = logger;
        this.request = request;
        this.entryTime = System.currentTimeMillis();
    }

    /**
     * If kvp request should possibly be logged.
     * 
     * @param response
     * @param kvp
     * @param successfulOnly
     * @param entryTime
     * @param logger
     * @param request
     *            is closed before logging is initiated if not null
     */
    public LoggingHttpResponseWrapper( HttpServletResponse response, String kvp, boolean successfulOnly,
                                       RequestLogger logger, InputStream request ) {
        super( response );
        this.kvp = kvp;
        this.successfulOnly = successfulOnly;
        this.logger = logger;
        this.request = request;
        this.entryTime = System.currentTimeMillis();
    }

    public void setCredentials( Credentials creds ) {
        this.creds = creds;
    }

    /**
     * 
     */
    public void setExceptionSent() {
        exceptionSent = true;
    }

    /**
     * kvp: possibly writes the entry into the log, XML/SOAP: possibly deletes the logged request file
     */
    public void finalizeLogging() {
        if ( logged ) {
            return;
        }
        logged = true;
        if ( !exceptionSent || !successfulOnly ) {
            if ( kvp != null ) {
                logger.logKVP( address, kvp, entryTime, currentTimeMillis(), creds );
            }
        }
        if ( request != null ) {
            try {
                request.close();
            } catch ( IOException e ) {
                LOG.trace( "Stack trace:", e );
            }
        }
        if ( ( !successfulOnly || !exceptionSent ) && requestLog != null ) {
            logger.logXML( address, requestLog, entryTime, currentTimeMillis(), creds );
        }
        if ( requestLog != null ) {
            if ( !requestLog.delete() ) {
                LOG.warn( "Could not delete temporary file {}.", requestLog );
            }
        }
    }
}
