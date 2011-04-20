//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.csw.getrecords;

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ResultType;

/**
 * Encapsulates the method for parsing a {@Link GetRecords} XML request via Http-POST.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsXMLAdapter extends AbstractGetRecordsXMLAdapter {

    @Override
    protected GetRecords parseSubElements( OMElement holeRequest, ResultType resultType, int maxRecords,
                                           int startPosition, String outputFormat, String requestId, URI outputSchema,
                                           List<OMElement> getRecordsChildElements ) {
        boolean distributedSearch = false;
        int hopCount = -1;
        String responseHandler = null;

        Query query = null;
        for ( OMElement omElement : getRecordsChildElements ) {

            if ( !new QName( CSWConstants.CSW_202_NS, "DistributedSearch" ).equals( omElement.getQName() )
                 && !new QName( CSWConstants.CSW_202_NS, "ResponseHandler" ).equals( omElement.getQName() )
                 && !new QName( CSWConstants.CSW_202_NS, "Query" ).equals( omElement.getQName() ) ) {
                String msg = "Child element '" + omElement.getQName() + "' is not allowed.";
                throw new XMLParsingException( this, omElement, msg );
            }
            // optional
            if ( new QName( CSWConstants.CSW_202_NS, "DistributedSearch" ).equals( omElement.getQName() ) ) {
                if ( omElement.getText().equals( "true" ) ) {
                    distributedSearch = true;
                } else {
                    distributedSearch = false;
                }
                hopCount = getNodeAsInt( omElement, new XPath( "@hopCount", nsContext ), 2 );
            }
            // optional
            if ( new QName( CSWConstants.CSW_202_NS, "ResponseHandler" ).equals( omElement.getQName() ) ) {
                responseHandler = omElement.getText();
            }
            // mandatory
            query = parseQuery( omElement );
        }

        return new GetRecords( VERSION_202, nsContext, outputFormat, resultType, requestId, outputSchema,
                               startPosition, maxRecords, distributedSearch, hopCount, responseHandler, query,
                               holeRequest );
    }

}
