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
package org.deegree.ogcwebservices.csw.capabilities;

import java.util.HashMap;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.OGCDocument;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.w3c.dom.Element;

/**
 * Parser for "csw:GetCapabilities" requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class CatalogueGetCapabilitiesDocument extends OGCDocument {

    private static final long serialVersionUID = -7155778875151820291L;

    private static ILogger LOG = LoggerFactory.getLogger( CatalogueGetCapabilitiesDocument.class );

    /**
     * Parses the underlying document into a <code>GetCapabilities</code> request object.
     *
     * @param id
     * @return corresponding <code>GetCapabilities</code> object
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     * @throws InvalidParameterValueException
     */
    public CatalogueGetCapabilities parse( String id )
                            throws XMLParsingException, InvalidParameterValueException {

        String version = CSWPropertiesAccess.getString( "DEFAULTVERSION" );
        String service = null;
        String updateSeq = null;
        String[] acceptVersions = null;
        String[] acceptFormats = null;
        String[] sections = null;
        Element root = this.getRootElement();

        // 'service'-attribute (required, must be CSW)
        service = XMLTools.getAttrValue( root, null, "service", null );
        if ( service == null ) {
            throw new XMLParsingException( "Mandatory attribute 'service' is missing" );
        } else if ( !service.equals( "CSW" ) ) {
            throw new XMLParsingException( "Attribute 'service' must be 'CSW'" );
        }
        // 'updateSequence'-attribute (optional)
        updateSeq = XMLTools.getAttrValue( root, null, "updateSequence", null );

        // '<csw:AcceptVersions>'-element (optional)
        Element acceptVersionsElement = XMLTools.getChildElement( "AcceptVersions", CommonNamespaces.CSWNS, root );
        if ( acceptVersionsElement != null ) {
            acceptVersions = XMLTools.getRequiredNodesAsStrings( acceptVersionsElement, "csw:Version", nsContext );
            version = CatalogueGetCapabilities.validateVersion( acceptVersions );
            if ( version == null ) {
                throw new InvalidParameterValueException( Messages.get( "CSW_UNSUPPORTED_VERSION" ) );
            }
        }
        LOG.logInfo( "process with version:", version );

        // '<csw:AcceptFormats>'-element (optional)
        Element acceptFormatsElement = XMLTools.getChildElement( "AcceptFormats", CommonNamespaces.CSWNS, root );
        if ( acceptFormatsElement != null ) {
            ElementList formatsList = XMLTools.getChildElements( "OutputFormat", CommonNamespaces.CSWNS,
                                                                 acceptFormatsElement );
            acceptFormats = new String[formatsList.getLength()];
            for ( int i = 0; i < acceptFormats.length; i++ ) {
                acceptFormats[i] = XMLTools.getStringValue( formatsList.item( i ) );
            }
        }

        // '<csw:Sections>'-element (optional)
        Element sectionsElement = XMLTools.getChildElement( "Sections", CommonNamespaces.CSWNS, root );
        if ( sectionsElement != null ) {
            ElementList sectionList = XMLTools.getChildElements( "Section", CommonNamespaces.CSWNS, sectionsElement );
            sections = new String[sectionList.getLength()];
            for ( int i = 0; i < sections.length; i++ ) {
                sections[i] = XMLTools.getStringValue( sectionList.item( i ) );
            }
        }

        return new CatalogueGetCapabilities( id, updateSeq, version, acceptVersions, acceptFormats, sections,
                                             new HashMap<String, String>() );
    }

}
