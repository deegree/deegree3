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

package org.deegree.ogcwebservices.csw.discovery;

import static org.deegree.ogcbase.CommonNamespaces.CSW202NS;
import static org.deegree.ogcbase.CommonNamespaces.CSWNS;

import java.io.IOException;
import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Represents an XML GetRecordsResponse document of an OGC CSW 2.0 compliant service.
 * <p>
 * The &lt;GetRecordsResponse&gt; element is a container for the response of the GetRecords
 * operation. Three levels of detail may be contained in the response document.
 * <ul>
 * <li>The &lt;RequestId&gt; element may be used to correlate the response to a GetRecords request
 * for which a value was defined for the requestId attribute.
 * <li>&lt;SearchStatus&gt; element must be present and indicates the status of the response. The
 * status attribute is used to indicate the completion status of the GetRecords operation. Table 65
 * shows the possible values for the status attribute.
 * <li>The &lt;SearchResults&gt; element is a generic XML container for the actual response to a
 * GetRecords request. The content of the &lt;SearchResults&gt; element is the set of records
 * returned by the GetRecords operation. The actual records returned by the catalogue should
 * substitute for the element &lt;csw:AbstractRecord&gt.
 * </ul>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 *
 */
public class GetRecordsResultDocument extends XMLFragment {

    private static final long serialVersionUID = 2796229558893029054L;

    private static final String XML_TEMPLATE = "GetRecordsResponseTemplate.xml";

    /**
     *
     */
    public GetRecordsResultDocument() {
        // load later
    }

    /**
     * @param version
     */
    public GetRecordsResultDocument( String version ) {
        super( new QualifiedName( "csw", "GetRecordsResponse", version.equals( "2.0.2" ) ? CSW202NS : CSWNS ) );
    }

    /**
     * Extracts a <code>GetRecordsResult</code> representation of this object.
     *
     * @param request
     *
     * @return the actual GetRecordResult as a bean.
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     * @throws OGCWebServiceException
     */
    public GetRecordsResult parseGetRecordsResponse( GetRecords request )
                            throws MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException {
        try {
            String requestId = null;
            SearchStatus searchStatus = null;
            SearchResults searchResults = null;
            // '<csw:GetRecordsResponse>'-element (required)
            Node contextNode = XMLTools.getRequiredNode( getRootElement(), "self::csw:GetRecordsResponse", nsContext );

            // 'version'-attribute (optional)
            String version = XMLTools.getNodeAsString( contextNode, "@version", nsContext, GetRecords.DEFAULT_VERSION );

            // '<csw:RequestId>'-element (optional)
            requestId = XMLTools.getNodeAsString( contextNode, "csw:RequestId", nsContext, requestId );

            // '<csw:SearchStatus>'-element (required)
            String status = XMLTools.getRequiredNodeAsString( contextNode, "csw:SearchStatus/@status", nsContext );
            String timestamp = XMLTools.getNodeAsString( contextNode, "csw:SearchStatus/@timestamp", nsContext, null );
            searchStatus = new SearchStatus( status, timestamp );

            // '<csw:SearchResults>'-element (required)
            contextNode = XMLTools.getRequiredNode( contextNode, "csw:SearchResults", nsContext );

            // 'requestId'-attribute (optional)
            requestId = XMLTools.getNodeAsString( contextNode, "@requestId", nsContext, requestId );

            // 'resultSetId'-attribute (optional)
            String resultSetId = XMLTools.getNodeAsString( contextNode, "@resultSetId", nsContext, null );

            // 'elementSet'-attribute (optional)
            String elementSet = XMLTools.getNodeAsString( contextNode, "@elementSet", nsContext, null );

            // 'recordSchema'-attribute (optional)
            String recordSchema = XMLTools.getNodeAsString( contextNode, "@recordSchema", nsContext, null );

            // 'numberOfRecordsMatched'-attribute (required)
            int numberOfRecordsMatched = XMLTools.getRequiredNodeAsInt( contextNode, "@numberOfRecordsMatched",
                                                                        nsContext );

            // 'numberOfRecordsReturned'-attribute (required)
            int numberOfRecordsReturned = XMLTools.getRequiredNodeAsInt( contextNode, "@numberOfRecordsReturned",
                                                                         nsContext );

            // 'nextRecord'-attribute (required)
            int nextRecord = XMLTools.getRequiredNodeAsInt( contextNode, "@nextRecord", nsContext );

            // 'expires'-attribute (optional)
            String expires = XMLTools.getNodeAsString( contextNode, "@expires", nsContext, "null" );

            searchResults = new SearchResults( requestId, resultSetId, elementSet, recordSchema,
                                               numberOfRecordsReturned, numberOfRecordsMatched, nextRecord,
                                               contextNode, expires );
            return new GetRecordsResult( request, version, searchStatus, searchResults );
        } catch ( XMLParsingException e ) {
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new OGCWebServiceException( "GetRecordsResponseDocument", StringTools.stackTraceToString( e ), code );
        }

    }

    /**
     * creates an emtpy document as defined by the template
     *
     * @throws IOException
     *             if the template could not be found
     * @throws SAXException
     *             if an error occurs while creating the rootnode.
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = GetRecordsResultDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }
}
