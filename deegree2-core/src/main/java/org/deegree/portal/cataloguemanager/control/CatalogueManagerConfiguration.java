//$HeadURL: 
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.portal.cataloguemanager.control;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CatalogueManagerConfiguration extends XMLFragment {

    private static final long serialVersionUID = -4330651238416870990L;

    private String catalogueURL;

    private Map<String, String> templateURL = new HashMap<String, String>();

    private String metadataSchema;

    private Map<String, String> xPathes = new HashMap<String, String>();

    private List<SpatialExtent> spatialExtents;

    private List<String> searchableProperties = new ArrayList<String>();

    private List<String> dateProperties = new ArrayList<String>();

    private List<String> searchableCSW = new ArrayList<String>();

    private List<RespPartyConfiguration> respParties;

    private char[] ignoreCharacters;

    private int stepSize;

    private URL briefHTMLXSL;

    private URL fullHTMLXSL;

    private URL pdfXSL;

    private URL linkageXSL;
    
    private String templateDirectory = "WEB-INF/conf/cataloguemanager/templates";

    private static NamespaceContext nsContext = null;
    static {
        if ( nsContext == null ) {
            nsContext = CommonNamespaces.getNamespaceContext();
            nsContext.addNamespace( "md", URI.create( "http://www.deegree.org/cataloguemanager" ) );
        }
    }

    /**
     * @param url
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     */
    CatalogueManagerConfiguration( URL url ) throws IOException, SAXException, XMLParsingException {
        super( url );
        parseConfiguration();
    }

    /**
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    private void parseConfiguration()
                            throws XMLParsingException, MalformedURLException {
        catalogueURL = XMLTools.getRequiredNodeAsString( getRootElement(),
                                                         "./md:CatalogueService/md:onlineResource/@xlink:href",
                                                         nsContext );
        metadataSchema = XMLTools.getRequiredNodeAsString( getRootElement(), "./md:CatalogueService/md:metadataSchema",
                                                           nsContext );
        List<Element> list = XMLTools.getElements( getRootElement(), "./md:Templates/md:Template", nsContext );
        for ( Element element : list ) {
            String level = XMLTools.getRequiredNodeAsString( element, "./@hierarchyLevel", nsContext );
            String template = XMLTools.getRequiredNodeAsString( element, "./md:onlineResource/@xlink:href", nsContext );
            templateURL.put( level, template );
        }
        list = XMLTools.getElements( getRootElement(), "./md:MD_Elements/md:element", nsContext );
        for ( Element element : list ) {
            String name = XMLTools.getRequiredNodeAsString( element, "./@name", nsContext );
            String value = XMLTools.getRequiredNodeAsString( element, "./text()", nsContext );
            xPathes.put( name, value );
        }

        list = XMLTools.getElements( getRootElement(), "./md:BoundingBoxes/md:BBOX", nsContext );
        spatialExtents = new ArrayList<SpatialExtent>( list.size() );
        for ( Element element : list ) {
            String id = XMLTools.getRequiredNodeAsString( element, "./@id", nsContext );
            String name = XMLTools.getRequiredNodeAsString( element, "./@name", nsContext );
            String bbox = XMLTools.getStringValue( element );
            SpatialExtent se = new SpatialExtent();
            se.setBbox( bbox );
            se.setId( id );
            se.setName( name );
            spatialExtents.add( se );
        }
        parseResponsibleParties();
        parseSearchSection();
    }

    /**
     * @throws XMLParsingException
     * 
     */
    private void parseResponsibleParties()
                            throws XMLParsingException {
        List<Element> list = XMLTools.getElements( getRootElement(), "./md:ResponsibleParties/md:ResponsibleParty",
                                                   nsContext );
        respParties = new ArrayList<RespPartyConfiguration>( list.size() );
        for ( Element el : list ) {
            RespPartyConfiguration respParty = new RespPartyConfiguration();
            respParty.setDisplayName( XMLTools.getRequiredNodeAsString( el, "md:DisplayName", nsContext ) );
            respParty.setIndividualName( XMLTools.getRequiredNodeAsString( el, "md:IndividualName", nsContext ) );
            respParty.setOrganisationName( XMLTools.getRequiredNodeAsString( el, "md:OrganisationName", nsContext ) );
            respParty.setStreet( XMLTools.getRequiredNodeAsString( el, "md:Street", nsContext ) );
            respParty.setCity( XMLTools.getRequiredNodeAsString( el, "md:City", nsContext ) );
            respParty.setCountry( XMLTools.getRequiredNodeAsString( el, "md:Country", nsContext ) );
            respParty.setPostalCode( XMLTools.getRequiredNodeAsString( el, "md:PostalCode", nsContext ) );
            respParty.setVoice( XMLTools.getRequiredNodeAsString( el, "md:Voice", nsContext ) );
            respParty.setFacsimile( XMLTools.getNodeAsString( el, "md:Facsimile", nsContext, "" ) );
            respParty.setEmail( XMLTools.getRequiredNodeAsString( el, "md:EMail", nsContext ) );
            respParties.add( respParty );
        }
    }

    /**
     * @throws XMLParsingException
     * @throws MalformedURLException
     * 
     */
    private void parseSearchSection()
                            throws XMLParsingException, MalformedURLException {
        String xpath = "md:Search/md:searchableProperties/md:Property";
        List<Element> elements = XMLTools.getRequiredElements( getRootElement(), xpath, nsContext );
        for ( Element element : elements ) {
            String s = '{' + element.getAttribute( "namespace" ) + "}:" + element.getAttribute( "name" );
            searchableProperties.add( s );
        }
        xpath = "md:Search/md:dateProperties/md:Property";
        elements = XMLTools.getRequiredElements( getRootElement(), xpath, nsContext );
        for ( Element element : elements ) {
            String s = '{' + element.getAttribute( "namespace" ) + "}:" + element.getAttribute( "name" );
            dateProperties.add( s );
        }
        xpath = "md:Search/md:ignoreCharacters";
        String s = XMLTools.getNodeAsString( getRootElement(), xpath, nsContext, "" );
        ignoreCharacters = new char[s.length()];
        for ( int i = 0; i < s.length(); i++ ) {
            ignoreCharacters[i] = s.charAt( i );
        }
        xpath = "md:Search/md:stepSize";
        stepSize = XMLTools.getRequiredNodeAsInt( getRootElement(), xpath, nsContext );

        xpath = "md:Search/md:searchableCSW/md:CSW";
        elements = XMLTools.getElements( getRootElement(), xpath, nsContext );
        for ( Element element : elements ) {
            searchableCSW.add( element.getAttribute( "xlink:href" ) );
        }

        xpath = "md:Search/md:briefHTMLFormat/@xlink:href";
        s = XMLTools.getRequiredNodeAsString( getRootElement(), xpath, nsContext );
        briefHTMLXSL = resolve( s );

        xpath = "md:Search/md:fullHTMLFormat/@xlink:href";
        s = XMLTools.getRequiredNodeAsString( getRootElement(), xpath, nsContext );
        fullHTMLXSL = resolve( s );

        xpath = "md:Search/md:pdfFormat/@xlink:href";
        s = XMLTools.getNodeAsString( getRootElement(), xpath, nsContext, null );
        if ( s != null ) {
            pdfXSL = resolve( s );
        }

        xpath = "md:Search/md:linkageFormat/@xlink:href";
        s = XMLTools.getNodeAsString( getRootElement(), xpath, nsContext, null );
        if ( s != null ) {
            linkageXSL = resolve( s );
        }
    }

    /**
     * 
     * @return list of available responsible parties
     */
    List<RespPartyConfiguration> getResponsibleParties() {
        return respParties;
    }

    /**
     * 
     * @return base URL to CSW
     */
    String getCatalogueURL() {
        return catalogueURL;
    }

    /**
     * 
     * @return metadata schema editing is performed on
     */
    String getMetadataSchema() {
        return metadataSchema;
    }

    /**
     * 
     * @param hierarchyLevel
     * @return URL to metadata template depending on hierarchy level
     */
    String getTemplateURL( String hierarchyLevel ) {
        return templateURL.get( hierarchyLevel );
    }

    /**
     * 
     * @param name
     * @return xPath assigned to the passed name
     */
    String getXPath( String name ) {
        return xPathes.get( name );
    }

    /**
     * 
     * @return list of names of all available xpath expressions
     */
    List<String> getXPathNames() {
        List<String> list = new ArrayList<String>( 50 );
        Iterator<String> iterator = xPathes.keySet().iterator();
        while ( iterator.hasNext() ) {
            list.add( iterator.next() );
        }
        return list;
    }

    /**
     * 
     * @return available spatial extents
     */
    List<SpatialExtent> getSpatialExtents() {
        return spatialExtents;
    }

    public List<String> getSearchableCSW() {
        return searchableCSW;
    }

    public List<String> getSearchableProperties() {
        return searchableProperties;
    }

    public List<String> getDateProperties() {
        return dateProperties;
    }

    public char[] getIgnoreCharacters() {
        return ignoreCharacters;
    }

    public int getStepSize() {
        return stepSize;
    }

    /**
     * @return the briefHTMLXSL
     */
    public URL getBriefHTMLXSL() {
        return briefHTMLXSL;
    }

    /**
     * @return the fullHTMLXSL
     */
    public URL getFullHTMLXSL() {
        return fullHTMLXSL;
    }

    /**
     * @return the pdfXSL
     */
    public URL getPdfXSL() {
        return pdfXSL;
    }

    /**
     * @return the linkageXSL
     */
    public URL getLinkageXSL() {
        return linkageXSL;
    }
    
    /**
     * 
     * @return relative path of the directory where available metadata templates can be found
     */
    public String getTemplateDirectory() {
        return templateDirectory;
    }

}
