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

package org.deegree.ogcwebservices.wfs.operation;

import static java.lang.Integer.parseInt;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.XMLTools.getRequiredNodeAsString;
import static org.deegree.i18n.Messages.get;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.w3c.dom.Element;

/**
 * <code>GetGmlObject</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetGmlObject extends AbstractWFSRequest {

    private static final long serialVersionUID = 1050888585238239416L;

    private static final NamespaceContext nsContext = getNamespaceContext();

    private static final ILogger LOG = getLogger( GetGmlObject.class );

    private int depth;

    private int expiry;

    private String objectId;

    private GetGmlObject( int depth, int expiry, String objectId, String version, String id, String handle,
                          Map<String, String> vendorSpecificParameter ) {
        super( version, id, handle, vendorSpecificParameter );
        this.depth = depth;
        this.expiry = expiry;
        this.objectId = objectId;
    }

    /**
     * @param map
     * @return a new request object
     * @throws MissingParameterValueException
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     */
    public static GetGmlObject create( Map<String, String> map )
                            throws InconsistentRequestException, MissingParameterValueException,
                            InvalidParameterValueException {
        checkServiceParameter( map );

        String id = map.get( "ID" );
        String version = checkVersionParameter( map );

        String depth = map.get( "TRAVERSEXLINKDEPTH" );
        if ( depth == null || depth.length() == 0 ) {
            throw new MissingParameterValueException( "traversexlinkdepth", get( "WFS_MISSING_PARAMETER_VALUE",
                                                                                 "TRAVERSEXLINKDEPTH" ) );
        }
        int idepth;
        try {
            idepth = parseInt( depth );
        } catch ( NumberFormatException nfe ) {
            throw new InvalidParameterValueException( "traversexlinkdepth", get( "WFS_INVALID_PARAMETER_VALUE",
                                                                                 "TRAVERSEXLINKDEPTH", depth ) );
        }

        String expiry = map.get( "TRAVERSEXLINKEXPIRY" );
        int iexpiry = -1;
        if ( expiry != null && expiry.length() > 0 ) {
            try {
                iexpiry = parseInt( expiry );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException( "traversexlinkexpiry", get( "WFS_INVALID_PARAMETER_VALUE",
                                                                                      "TRAVERSEXLINKEXPIRY", expiry ) );
            }
        }

        String objectId = map.get( "GMLOBJECTID" );
        if ( objectId == null || objectId.length() == 0 ) {
            throw new MissingParameterValueException( "gmlobjectid", get( "WFS_MISSING_PARAMETER_VALUE", "GMLOBJECTID" ) );
        }

        return new GetGmlObject( idepth, iexpiry, objectId, version, id, null, map );
    }

    /**
     * Output format parameter is ignored by this method (it will always be GML3 anyway).
     *
     * @param id
     * @param root
     * @return a new request
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest create( String id, Element root )
                            throws OGCWebServiceException {
        try {
            String objectId = getRequiredNodeAsString( root, "ogc:GmlObjectId/@gml:id", nsContext );
            String version = root.getAttribute( "version" );

            String depth = root.getAttribute( "traverseXlinkDepth" );
            if ( depth == null || depth.length() == 0 ) {
                throw new MissingParameterValueException( "traversexlinkdepth", get( "WFS_MISSING_PARAMETER_VALUE",
                                                                                     "TRAVERSEXLINKDEPTH" ) );
            }
            int idepth;
            try {
                idepth = parseInt( depth );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException( "traversexlinkdepth", get( "WFS_INVALID_PARAMETER_VALUE",
                                                                                     "TRAVERSEXLINKDEPTH", depth ) );
            }

            String expiry = root.getAttribute( "traverseXlinkExpiry" );
            int iexpiry = -1;
            if ( expiry != null && expiry.length() > 0 ) {
                try {
                    iexpiry = parseInt( expiry );
                } catch ( NumberFormatException nfe ) {
                    throw new InvalidParameterValueException( "traversexlinkexpiry",
                                                              get( "WFS_INVALID_PARAMETER_VALUE",
                                                                   "TRAVERSEXLINKEXPIRY", expiry ) );
                }
            }

            return new GetGmlObject( idepth, iexpiry, objectId, version, id, null, null );
        } catch ( XMLParsingException e ) {
            LOG.logDebug( "Stack trace: ", e );
            throw new OGCWebServiceException( "getgmlobject", get( "WFS_REQUEST_NOT_PARSED", e.getMessage() ) );
        }
    }

    /**
     * @return the TRAVERSEXLINKDEPTH parameter value
     */
    public int getXLinkDepth() {
        return depth;
    }

    /**
     * @return the TRAVERSEXLINKEXPIRY parameter value, or -1 if not set
     */
    public int getXLinkExpiry() {
        return expiry;
    }

    /**
     * @return the GMLOBJECTID parameter value
     */
    public String getObjectId() {
        return objectId;
    }

}
