//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wmts.client;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.parsing.FeatureInfoParser;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpResponse;
import org.deegree.protocol.wmts.ops.GetFeatureInfo;

/**
 * The server response to a GetFeatureInfo request.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetFeatureInfoResponse {

    private final OwsHttpResponse rawResponse;

    private GetFeatureInfo request;

    GetFeatureInfoResponse( OwsHttpResponse rawResponse, GetFeatureInfo request ) {
        this.rawResponse = rawResponse;
        this.request = request;
    }

    public FeatureCollection getFeatures()
                            throws OWSException, OWSExceptionReport {
        try {
            return FeatureInfoParser.parseAsFeatureCollection( rawResponse.getAsXMLStream(), request.getLayer() );
        } catch ( XMLStreamException e ) {
            throw new OWSException( "Remote WMTS response was not recognized as feature collection: "
                                    + e.getLocalizedMessage(), e, OWSException.NO_APPLICABLE_CODE );
        }
    }

    /**
     * Provides access to the raw server response.
     * 
     * @return the raw server response, never <code>null</code>
     */
    public OwsHttpResponse getAsRawResponse() {
        return rawResponse;
    }

    /**
     * 
     * @throws IOException
     */
    public void close()
                            throws IOException {
        rawResponse.close();
    }

}
