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

package org.deegree.owscommon_1_1_0;

import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsString;
import static org.deegree.framework.xml.XMLTools.getNodesAsStringList;
import static org.deegree.framework.xml.XMLTools.getRequiredElements;
import static org.deegree.ogcbase.CommonNamespaces.XLINK_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.w3c.dom.Element;

/**
 * <code>ManifestDocument</code> supplies methods for the parsing of a manifest type in ows 1.1.0.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class ManifestDocument extends CommonsDocument {
    /**
     *
     */
    private static final long serialVersionUID = -7314461220886459179L;

    private static ILogger LOG = LoggerFactory.getLogger( ManifestDocument.class );

    /**
     * @param manifestElement
     *            to parse the data from
     * @return the manifest bean or <code>null</code> if the given element is <code>null</code>.
     * @throws XMLParsingException
     *             if a parsing error occurred.
     */
    public Manifest parseManifestType( Element manifestElement )
                            throws XMLParsingException {
        if ( manifestElement == null ) {
            return null;
        }
        BasicIdentification basicManifestIdentification = parseBasicIdentificationType( manifestElement );
        List<Element> referenceGroupElements = getRequiredElements( manifestElement, PRE_OWS + "ReferenceGroup",
                                                                    nsContext );
        List<ReferenceGroup> referenceGroup = new ArrayList<ReferenceGroup>();
        for ( Element elem : referenceGroupElements ) {
            ReferenceGroup tmp = parseReferenceGroup( elem );
            if ( tmp != null ) {
                referenceGroup.add( tmp );
            }
        }
        return new Manifest( basicManifestIdentification, referenceGroup );
    }

    /**
     * @param referenceGroup
     *            to be parsed
     * @return the bean representation or <code>null</code> if the given element is <code>null</code>.
     * @throws XMLParsingException
     *             if the element could not be parsed.
     */
    protected ReferenceGroup parseReferenceGroup( Element referenceGroup )
                            throws XMLParsingException {
        if ( referenceGroup == null ) {
            return null;
        }
        BasicIdentification basicRGId = parseBasicIdentificationType( referenceGroup );
        List<Element> referenceElements = getElements( referenceGroup, PRE_OWS + "Reference", nsContext );
        List<Reference> references = new ArrayList<Reference>();
        if ( referenceElements == null || referenceElements.size() == 0 ) {
            LOG.logWarning( Messages.getMessage( "WCTS_REFERENCE_SUBSTITION_NOT_SUPPORTED" ) );
        } else {
            for ( Element refElement : referenceElements ) {
                Reference tmp = parseReferenceType( refElement );
                if ( tmp != null ) {
                    references.add( tmp );
                }
            }
        }
        return new ReferenceGroup( basicRGId, references );

    }

    /**
     * @param referenceElement
     *            to be parsed.
     * @return the bean representation or <code>null</code> if the given element is <code>null</code>.
     * @throws XMLParsingException
     *             if the element could not be parsed.
     */
    protected Reference parseReferenceType( Element referenceElement )
                            throws XMLParsingException {
        if ( referenceElement == null ) {
            return null;
        }
        String hrefAttribute = referenceElement.getAttributeNS( XLNNS.toASCIIString(), "href" );
        if ( hrefAttribute == null || "".equals( hrefAttribute.trim() ) ) {
            throw new XMLParsingException( Messages.getMessage( "OWS_MISSING_REQUIRED_ATTRIBUTE", XLINK_PREFIX
                                                                                                  + ":href",
                                                                PRE_OWS + "Reference" ) );
        }
        String roleAttribute = referenceElement.getAttributeNS( XLNNS.toASCIIString(), "role" );
        String typeAttribute = referenceElement.getAttribute( "type" );
        Pair<String, String> identifier = parseIdentifier( referenceElement );
        List<String> abstracts = getNodesAsStringList( referenceElement, PRE_OWS + "Abstract", nsContext );
        String format = getNodeAsString( referenceElement, PRE_OWS + "Format", nsContext, null );
        List<Metadata> metadatas = parseMetadatas( getElements( referenceElement, PRE_OWS + "Metadata", nsContext ) );
        return new Reference( hrefAttribute, roleAttribute, typeAttribute, identifier, abstracts, format, metadatas );
    }

}
