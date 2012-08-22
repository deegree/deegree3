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

package org.deegree.ogcwebservices.csw.discovery;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DescribeRecordDocument_2_0_2 extends DescribeRecordDocument {

    private static final long serialVersionUID = -1840952437026263106L;

    private static final ILogger LOG = LoggerFactory.getLogger( DescribeRecordDocument_2_0_2.class );

    private static final String XML_TEMPLATE = "DescribeRecord2.0.2Template.xml";

    @Override
    public DescribeRecord parse( String id )
                            throws MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException {

        String version;
        Map<String, String> vendorSpecificParameter = null;
        Map<String, URI> namespaceMappings = null;
        String[] typeNames = null;
        String outputFormat = "application/xml";
        URI schemaLanguage = null;

        try {
            // '<csw202:DescribeRecord>'-element (required)
            Node contextNode = XMLTools.getRequiredNode( this.getRootElement(), "self::csw202:DescribeRecord",
                                                         nsContext );

            // 'service'-attribute (required, must be CSW)
            String service = XMLTools.getRequiredNodeAsString( contextNode, "@service", nsContext );
            if ( !service.equals( "CSW" ) ) {
                ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
                throw new InvalidParameterValueException( "DescribeRecords", "'service' must be 'CSW'", code );
            }

            // 'version'-attribute (required)
            version = XMLTools.getRequiredNodeAsString( contextNode, "@version", nsContext );
            if ( !"2.0.2".equals( version ) ) {
                throw new OGCWebServiceException( "DescribeRecordDocument_2_0_2",
                                                  Messages.getMessage( "CSW_NOT_SUPPORTED_VERSION",
                                                                       GetRecords.DEFAULT_VERSION, "2.0.2", version ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            // 'outputFormat'-attribute (optional, default="text/xml")
            outputFormat = XMLTools.getNodeAsString( contextNode, "@outputFormat", nsContext, outputFormat );

            // 'schemaLanguage'-attribute (optional,
            // default="http://www.w3.org/XML/Schema")
            String schemaLanguageString = XMLTools.getNodeAsString( contextNode, "@schemaLanguage", nsContext,
                                                                    "http://www.w3.org/XML/Schema" );
            try {
                schemaLanguage = new URI( schemaLanguageString );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( "Value '" + schemaLanguageString + "' for attribute 'schemaLanguage' "
                                               + "is invalid. Must denote a valid URI." );
            }

            // 'csw202:TypeName' elements
            List<Node> nl = XMLTools.getNodes( contextNode, "csw202:TypeName", nsContext );
            typeNames = new String[nl.size()];
            for ( int i = 0; i < typeNames.length; i++ ) {
                typeNames[i] = parseTypeName( (Element) nl.get( i ) );
            }

            // vendorspecific attributes; required by deegree rights management
            vendorSpecificParameter = parseDRMParams( this.getRootElement() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new OGCWebServiceException( "CatalogGetCapabilities", StringTools.stackTraceToString( e ), code );
        }

        return new DescribeRecord( id, version, vendorSpecificParameter, namespaceMappings, typeNames, outputFormat,
                                   schemaLanguage );
    }

    @Override
    /*
     * (non-Javadoc)
     *
     * @see org.deegree.framework.xml.XMLFragment#createEmptyDocument()
     */
    void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = GetRecordsDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

}
