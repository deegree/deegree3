//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.ogcwebservices.wass.common;

import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.w3c.dom.Element;

/**
 * Encapsulated data: CloseSession element
 *
 * Namespace: http://www.gdi-nrw.de/session
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class CloseSession extends AbstractRequest {

    private static final long serialVersionUID = 1447551772226950443L;

    /**
     * The logger enhances the quality and simplicity of Debugging within the deegree2 framework
     */
    private static final ILogger LOG = LoggerFactory.getLogger( CloseSession.class );

    private String sessionID = null;

    /**
     * Constructs new one from the given values.
     *
     * @param id the request id
     * @param service
     * @param version
     * @param sessionID
     */
    public CloseSession(  String id, String service, String version, String sessionID ) {
        super( id, version, service, "CloseSession" );
        this.sessionID = sessionID;
    }

    /**
     * Constructs new one from the given key-value-pairs.
     * @param id the request id
     * @param kvp
     *            the map
     */
    public CloseSession( String id, Map<String, String> kvp ) {
        super( id, kvp );
        sessionID = kvp.get( "SESSIONID" );
    }

    /**
     * @return Returns the sessionID.
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * @param id
     * @param documentElement
     * @return a new instance of this class
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest create( String id, Element documentElement ) throws OGCWebServiceException {
        try {
            return new SessionOperationsDocument( ).parseCloseSession( id, documentElement );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
    }

    /**
     * @param id
     * @param kvp
     * @return a new instance of this class
     */
    public static OGCWebServiceRequest create( String id, Map<String, String> kvp ) {
        return new CloseSession( id, kvp );
    }

}
