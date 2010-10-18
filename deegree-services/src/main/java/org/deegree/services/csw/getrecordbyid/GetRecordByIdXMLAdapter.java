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
package org.deegree.services.csw.getrecordbyid;

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.i18n.Messages;
import org.deegree.services.csw.AbstractCSWRequestXMLAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the method for parsing a {@link GetRecordById} XML request via Http-POST.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordByIdXMLAdapter extends AbstractCSWRequestXMLAdapter {
    private static final Logger LOG = LoggerFactory.getLogger( GetRecordByIdXMLAdapter.class );

    /**
     * Parses the {@link GetRecordById} XML request by deciding which version has to be parsed because of the requested
     * version.
     * 
     * @param requestVersion
     * @return {@link GetRecordById}
     */
    public GetRecordById parse( Version requestVersion ) {

        if ( requestVersion == null ) {
            requestVersion = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version",
                                                                                                    nsContext ) ) );
        }

        GetRecordById result = null;

        if ( VERSION_202.equals( requestVersion ) ) {
            result = parse202();
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", requestVersion, Version.getVersionsString( VERSION_202 ) );
            throw new InvalidParameterValueException( msg );
        }

        return result;
    }

    /**
     * Parses the {@link GetRecordById} request on the basis of CSW version 2.0.2
     * 
     * @return {@link GetRecordById}
     */
    @SuppressWarnings("unchecked")
    private GetRecordById parse202() {
        // outputFormat (optional)
        String outputFormat = getNodeAsString( rootElement, new XPath( "@outputFormat", nsContext ), "application/xml" );

        String elementSetNameString = getNodeAsString( rootElement, new XPath( "csw:ElementSetName", nsContext ),
                                                       ReturnableElement.summary.name() );

        ReturnableElement elementSetName = ReturnableElement.determineReturnableElement( elementSetNameString );

        String outputSchemaString = getNodeAsString( rootElement, new XPath( "@outputSchema", nsContext ),
                                                     "http://www.opengis.net/cat/csw/2.0.2" );
        URI outputSchema = URI.create( outputSchemaString );

        // elementName List<String>
        List<String> id = null;
        try {
            List<OMElement> idList = getRequiredNodes( rootElement, new XPath( "csw:Id", nsContext ) );
            LOG.debug( "idList: " + idList );
            id = new ArrayList<String>();
            for ( OMElement elem : idList ) {
                String idString = getNodeAsString( elem, new XPath( "text()", nsContext ), "" );
                id.add( idString );
            }
        } catch ( XMLParsingException e ) {
            String msg = "No ID provided, please check the mandatory element 'id'. ";
            LOG.info( msg );
            throw new MissingParameterException( msg );
        }

        return new GetRecordById( VERSION_202, outputFormat, elementSetName, outputSchema, id );
    }

}
