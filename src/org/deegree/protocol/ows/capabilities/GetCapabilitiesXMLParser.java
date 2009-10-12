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
package org.deegree.protocol.ows.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.OWSCommonXMLAdapter;

/**
 * Parser for OWS/OGC GetCapabilities requests (XML).
 * <p>
 * Handles GetCapabilities documents that are compliant to the following specifications:
 * <ul>
 * <li>OWS Common 1.0.0</li>
 * <li>OWS Common 1.1.0</li>
 * </ul>
 * </p>
 * <p>
 * Evaluates the <code>language</code> attribute for requests to multilingual services according to OWS Common change
 * request OGC 08-016r2. This is used by the WPS Specification 1.0.0.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GetCapabilitiesXMLParser extends OWSCommonXMLAdapter {

    /**
     * Create a new {@link GetCapabilitiesXMLParser} without an XML source.
     */
    public GetCapabilitiesXMLParser() {
        //
    }

    /**
     * Create a new {@link GetCapabilitiesXMLParser} for the given root element.
     * 
     * @param rootElement
     *            the root element of the GetCapabilities request
     */
    public GetCapabilitiesXMLParser( OMElement rootElement ) {
        this.setRootElement( rootElement );
    }

    /**
     * Parses an OWS 1.0.0 <code>GetCapabilitiesType</code> into a {@link GetCapabilities} object.
     * 
     * @return <code>GetCapabilities</code> object corresponding to the input document
     * @throws XMLParsingException
     *             if the document contains syntactic or semantic errors
     */
    public GetCapabilities parse100()
                            throws XMLParsingException {

        // @updateSequence (optional)
        String updateSequence = rootElement.getAttributeValue( new QName( "updateSequence" ) );

        // ows:AcceptVersions (optional)
        String[] versions = getNodesAsStrings( rootElement, new XPath( "ows:AcceptVersions/ows:Version/text()",
                                                                       nsContext ) );

        // ows110:Sections (optional)
        List<String> sections = parseSections( OWS_PREFIX );

        // ows:AcceptFormats (optional)
        List<OMElement> formatElements = getElements( rootElement, new XPath( "ows:AcceptFormats/ows:OutputFormat",
                                                                              nsContext ) );
        List<String> formats = new ArrayList<String>( formatElements.size() );
        for ( OMElement formatElement : formatElements ) {
            formats.add( formatElement.getText() );
        }

        // @language (optional)
        List<String> languages = null;
        String languageString = rootElement.getAttributeValue( new QName( "language" ) );
        if ( languageString != null ) {
            languages = Arrays.asList( languageString.split( "," ) );
        }

        return new GetCapabilities( Arrays.asList( versions ), sections, formats, updateSequence, languages );
    }

    /**
     * Parses an OWS 1.1.0 <code>GetCapabilitiesType</code> into a {@link GetCapabilities} object.
     * 
     * @return <code>GetCapabilities</code> object corresponding to the input document
     * @throws XMLParsingException
     *             if the document contains syntactic or semantic errors
     */
    public GetCapabilities parse110()
                            throws XMLParsingException {

        // @updateSequence (optional)
        String updateSequence = rootElement.getAttributeValue( new QName( "updateSequence" ) );

        // ows110:AcceptVersions (optional)
        String[] versions = getNodesAsStrings( rootElement, new XPath( "ows110:AcceptVersions/ows110:Version/text()",
                                                                       nsContext ) );

        // ows110:Sections (optional)
        List<String> sections = parseSections( OWS110_PREFIX );

        // ows110:AcceptFormats (optional)
        List<OMElement> formatElements = getElements( rootElement,
                                                      new XPath( "ows110:AcceptFormats/ows110:OutputFormat", nsContext ) );
        List<String> formats = new ArrayList<String>( formatElements.size() );
        for ( OMElement formatElement : formatElements ) {
            formats.add( formatElement.getText() );
        }

        // @language (optional)
        List<String> languages = null;
        String languageString = rootElement.getAttributeValue( new QName( "language" ) );
        if ( languageString != null ) {
            languages = Arrays.asList( languageString.split( "," ) );
        }

        return new GetCapabilities( Arrays.asList( versions ), sections, formats, updateSequence, languages );
    }

    /**
     * @return all parsed Sections
     */
    private List<String> parseSections( String nsPrefix ) {
        // The spec defines that all sections shall be returned if the element is omitted,
        // but only the required sections if the client sends an empty sections element.
        // Therefore we set All explicit if the element is omitted.
        OMElement sectionsElement = getElement( rootElement, new XPath( nsPrefix + ":Sections", nsContext ) );
        List<OMElement> sectionElements = getElements( rootElement, new XPath( nsPrefix + ":Sections/" + nsPrefix
                                                                               + ":Section", nsContext ) );
        List<String> sections = new ArrayList<String>( sectionElements.size() );
        if ( sectionsElement == null && sectionElements.size() == 0 ) {
            sections.add( "All" );
        } else {
            for ( OMElement sectionElement : sectionElements ) {
                sections.add( sectionElement.getText() );
            }
        }
        return sections;
    }
}
