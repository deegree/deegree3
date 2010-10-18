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

import static java.lang.System.currentTimeMillis;

import java.io.File;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.RequestLogger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoggingHttpResponseWrapper extends HttpServletResponseWrapper {

    private File requestLog;

    private boolean successfulOnly;

    private String kvp;

    private boolean exceptionSent;

    private long entryTime;

    private boolean logged;

    private RequestLogger logger;

    private Credentials creds;

    private String address;

    /**
     * If XML request should possibly be logged.
     * 
     * @param address
     * 
     * @param response
     * @param requestLog
     * @param successfulOnly
     * @param entryTime
     * @param creds
     * @param logger
     */
    public LoggingHttpResponseWrapper( String address, HttpServletResponse response, File requestLog,
                                       boolean successfulOnly, long entryTime, Credentials creds, RequestLogger logger ) {
        super( response );
        this.address = address;
        this.requestLog = requestLog;
        this.successfulOnly = successfulOnly;
        this.entryTime = entryTime;
        this.creds = creds;
        this.logger = logger;
    }

    /**
     * If kvp request should possibly be logged.
     * 
     * @param response
     * @param kvp
     * @param successfulOnly
     * @param entryTime
     * @param creds
     * @param logger
     */
    public LoggingHttpResponseWrapper( HttpServletResponse response, String kvp, boolean successfulOnly,
                                       long entryTime, Credentials creds, RequestLogger logger ) {
        super( response );
        this.kvp = kvp;
        this.successfulOnly = successfulOnly;
        this.entryTime = entryTime;
        this.creds = creds;
        this.logger = logger;
    }

    /**
     * If no logging is done.
     * 
     * @param response
     */
    public LoggingHttpResponseWrapper( HttpServletResponse response ) {
        super( response );
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
        if ( exceptionSent && successfulOnly && requestLog != null ) {
            logger.logXML( address, requestLog, entryTime, currentTimeMillis(), creds );
        }
    }

}
