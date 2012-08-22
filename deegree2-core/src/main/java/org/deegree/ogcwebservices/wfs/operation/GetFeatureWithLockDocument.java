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

import static org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest.FORMAT_GML2_WFS100;
import static org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest.FORMAT_GML3;

import java.util.List;
import java.util.Map;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.ogcwebservices.wfs.operation.LockFeature.ALL_SOME_TYPE;
import org.w3c.dom.Element;

/**
 * Parser for "wfs:GetFeatureWithLock" requests.
 * <p>
 * Identical to "wfs:GetFeature" requests, except for two additional attributes in the root element (and the name of the
 * root element, of course).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetFeatureWithLockDocument extends GetFeatureDocument {

    private static final long serialVersionUID = -5865330735585920611L;

    /**
     * Parses the underlying document into a <code>GetFeatureWithLock</code> request object.
     *
     * @param id
     * @return corresponding <code>GetFeatureWithLock</code> object
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    @Override
    public GetFeatureWithLock parse( String id )
                            throws XMLParsingException, InvalidParameterValueException {

        checkServiceAttribute();
        String version = checkVersionAttribute();

        boolean useVersion_1_0_0 = "1.0.0".equals( version );

        Element root = this.getRootElement();
        String handle = XMLTools.getNodeAsString( root, "@handle", nsContext, null );
        String outputFormat = XMLTools.getNodeAsString( root, "@outputFormat", nsContext,
                                                        useVersion_1_0_0 ? FORMAT_GML2_WFS100 : FORMAT_GML3 );

        int maxFeatures = XMLTools.getNodeAsInt( root, "@maxFeatures", nsContext, -1 );
        int startPosition = XMLTools.getNodeAsInt( root, "@startPosition", nsContext, 1 );
        if ( startPosition < 1 ) {
            String msg = Messages.getMessage( "WFS_INVALID_STARTPOSITION", Integer.toString( startPosition ) );
            throw new XMLParsingException( msg );
        }

        int traverseXLinkDepth = XMLTools.getNodeAsInt( root, "@traverseXLinkDepth", nsContext, -1 );
        int traverseXLinkExpiry = XMLTools.getNodeAsInt( root, "@traverseXLinkExpiry", nsContext, -1 );

        String resultTypeString = XMLTools.getNodeAsString( root, "@resultType", nsContext, "results" );
        RESULT_TYPE resultType;
        if ( "results".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.RESULTS;
        } else if ( "hits".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.HITS;
        } else {
            String msg = Messages.getMessage( "WFS_INVALID_RESULT_TYPE", resultTypeString );
            throw new XMLParsingException( msg );
        }

        List<Element> nl = XMLTools.getRequiredElements( root, "wfs:Query", nsContext );
        Query[] queries = new Query[nl.size()];
        for ( int i = 0; i < queries.length; i++ ) {
            queries[i] = parseQuery( nl.get( i ), useVersion_1_0_0 );
        }

        // vendorspecific attributes; required by deegree rights management
        Map<String, String> vendorSpecificParams = parseDRMParams( root );

        long expiry = LockFeatureDocument.parseExpiry( root );

        String lockActionString = XMLTools.getNodeAsString( root, "@lockAction", nsContext, "ALL" );
        ALL_SOME_TYPE lockAction = ALL_SOME_TYPE.ALL;
        try {
            lockAction = LockFeature.validateLockAction( lockActionString );
        } catch ( InvalidParameterValueException e ) {
            throw new XMLParsingException( e.getMessage() );
        }

        GetFeatureWithLock req = new GetFeatureWithLock( version, id, handle, resultType, outputFormat, maxFeatures,
                                                         startPosition, traverseXLinkDepth, traverseXLinkExpiry,
                                                         queries, vendorSpecificParams, expiry, lockAction );
        return req;
    }
}
