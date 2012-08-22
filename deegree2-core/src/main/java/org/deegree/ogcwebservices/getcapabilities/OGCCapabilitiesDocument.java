// $HeadURL$
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
package org.deegree.ogcwebservices.getcapabilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.OGCDocument;
import org.deegree.ogcwebservices.MetadataLink;
import org.deegree.ogcwebservices.MetadataType;
import org.w3c.dom.Element;

/**
 * Most basic capabilities document for any OGC service instance.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class OGCCapabilitiesDocument extends OGCDocument {

    private static final long serialVersionUID = -1704967654756261989L;
    protected static final URI OGCNS = CommonNamespaces.OGCNS;

    /**
     * Returns the value of the version attribute of the capabilities document.
     *
     * @return the value of the version attribute of the capabilities document.
     */
    public String parseVersion() {
        return getRootElement().getAttribute( "version" );
    }

    /**
     * Returns the value of the updateSequence attribute of the capabilities document.
     *
     * @return the value of the updateSequence attribute of the capabilities document.
     */
    public String parseUpdateSequence() {
        String s = getRootElement().getAttribute( "updateSequence" );
        if ( s == null || s.trim().length() == 0 ) {
            return "0";
        }
        return s;
    }

    /**
     * Creates a <tt>MetadataLink</tt> instance from the passed element.
     *
     * @param element
     * @return created <tt>MetadataLink</tt>
     * @throws XMLParsingException
     */
    protected MetadataLink parseMetadataLink( Element element )
                            throws XMLParsingException {
        if ( element == null )
            return null;

        try {
            URL reference = new URL( XMLTools.getAttrValue( element, "xlink:href" ) );
            String title = XMLTools.getAttrValue( element, "xlink:title" );
            URI about = new URI( XMLTools.getAttrValue( element, null, "about", null ) );
            String tmp = XMLTools.getAttrValue( element, null, "metadataType", null );
            MetadataType metadataType = new MetadataType( tmp );
            return new MetadataLink( reference, title, about, metadataType );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "Couldn't parse metadataLink reference\n"
                                           + StringTools.stackTraceToString( e ) );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "Couldn't parse metadataLink about\n" + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the document
     * @throws InvalidCapabilitiesException
     */
    public abstract OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException;

    /**
     * Creates an <code>Address</code> instance from the passed element.
     *
     * @param element
     *            Address-element
     * @param namespaceURI
     *            namespace-prefix of all elements
     * @return the new instance
     */
    protected Address parseAddress( Element element, URI namespaceURI ) {
        ElementList el = XMLTools.getChildElements( "DeliveryPoint", namespaceURI, element );
        String[] deliveryPoint = new String[el.getLength()];
        for ( int i = 0; i < deliveryPoint.length; i++ ) {
            deliveryPoint[i] = XMLTools.getStringValue( el.item( i ) );
        }
        String city = XMLTools.getStringValue( "City", namespaceURI, element, null );
        String adminArea = XMLTools.getStringValue( "AdministrativeArea", namespaceURI, element, null );
        String postalCode = XMLTools.getStringValue( "PostalCode", namespaceURI, element, null );
        String country = XMLTools.getStringValue( "Country", namespaceURI, element, null );
        el = XMLTools.getChildElements( "ElectronicMailAddress", namespaceURI, element );
        String[] eMailAddresses = new String[el.getLength()];
        for ( int i = 0; i < eMailAddresses.length; i++ ) {
            eMailAddresses[i] = XMLTools.getStringValue( el.item( i ) );
        }
        return new Address( adminArea, city, country, deliveryPoint, eMailAddresses, postalCode );
    }

    /**
     * Creates a <tt>Phone</tt> instance from the passed element.
     *
     * @param element
     *            Phone-element
     * @param namespaceURI
     *            URI that all elements must have
     * @return the new instance
     */
    protected Phone parsePhone( Element element, URI namespaceURI ) {

        // 'Voice'-elements (optional)
        ElementList el = XMLTools.getChildElements( "Voice", namespaceURI, element );
        String[] voiceNumbers = new String[el.getLength()];
        for ( int i = 0; i < voiceNumbers.length; i++ ) {
            voiceNumbers[i] = XMLTools.getStringValue( el.item( i ) );
        }

        // 'Facsimile'-elements (optional)
        el = XMLTools.getChildElements( "Facsimile", namespaceURI, element );
        String[] facsimileNumbers = new String[el.getLength()];
        for ( int i = 0; i < facsimileNumbers.length; i++ ) {
            facsimileNumbers[i] = XMLTools.getStringValue( el.item( i ) );
        }
        return new Phone( facsimileNumbers, null, null, voiceNumbers );
    }

}
