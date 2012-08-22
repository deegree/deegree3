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
package org.deegree.portal.standard.gazetteer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * Constructor expects a document formated like
 * 
 * <pre>
 *  &lt;Hierarchy name=&quot;MyGazetteer&quot; address=&quot;http://localhost/deegree-gaz/services&quot;&gt;
 *      &lt;Type name=&quot;Länder&quot; freeSearch=&quot;true&quot;&gt;
 *          &lt;FeatureType name=&quot;XYZ&quot; namespace=&quot;http://www.deegree.org/app&quot;/&gt;
 *          &lt;Type name=&quot;Kreise&quot; freeSearch=&quot;false&quot;&gt;
 *              &lt;FeatureType name=&quot;ABC&quot; namespace=&quot;http://www.deegree.org/app&quot;/&gt;
 *              &lt;Type name=&quot;Gemeinden&quot; freeSearch=&quot;true&quot;&gt;
 *                  &lt;FeatureType name=&quot;MNO&quot; namespace=&quot;http://www.deegree.org/app&quot;/&gt;
 *              &lt;/Type&gt;
 *          &lt;/Type&gt;
 *      &lt;/Type&gt;
 *  &lt;/Hierarchy&gt;
 * </pre>
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Hierarchy {

    private String gazetteerAddress;

    private String name;

    private HierarchyNode root;

    /**
     * <pre>
     * 
     * </pre>
     * 
     * @param xml
     * @throws XMLParsingException
     */
    public Hierarchy( XMLFragment xml ) throws XMLParsingException {
        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        Element node = xml.getRootElement();
        name = XMLTools.getAttrValue( node, null, "name", null );
        gazetteerAddress = XMLTools.getAttrValue( node, null, "address", null );
        node = (Element) XMLTools.getNode( node, "Type", CommonNamespaces.getNamespaceContext() );

        String nm = XMLTools.getAttrValue( node, null, "name", null );
        boolean freeSearch = "true".equalsIgnoreCase( XMLTools.getAttrValue( node, null, "freeSearch", null ) );
        boolean strictMode = true;
        boolean matchCase = true;
        if ( freeSearch ) {
            strictMode = "true".equalsIgnoreCase( XMLTools.getAttrValue( node, null, "strictMode", null ) );
            matchCase = "true".equalsIgnoreCase( XMLTools.getAttrValue( node, null, "matchCase", null ) );
        }
        Element ftnode = (Element) XMLTools.getNode( node, "FeatureType", nsc );
        String ftName = XMLTools.getAttrValue( ftnode, null, "name", null );
        String nsp = XMLTools.getAttrValue( ftnode, null, "namespace", null );
        Map<String, String> properties = readPropertyNames( nsc, ftnode );

        root = new HierarchyNode( new QualifiedName( ftName, URI.create( nsp ) ), properties, nm, freeSearch,
                                  strictMode, matchCase );
        HierarchyNode parent = root;

        node = (Element) XMLTools.getNode( node, "Type", CommonNamespaces.getNamespaceContext() );
        while ( node != null ) {
            nm = XMLTools.getAttrValue( node, null, "name", null );
            freeSearch = "true".equals( XMLTools.getAttrValue( node, null, "freeSearch", null ) );
            ftnode = (Element) XMLTools.getNode( node, "FeatureType", CommonNamespaces.getNamespaceContext() );
            ftName = XMLTools.getAttrValue( ftnode, null, "name", null );
            nsp = XMLTools.getAttrValue( ftnode, null, "namespace", null );
            properties = readPropertyNames( nsc, ftnode );
            HierarchyNode hn = new HierarchyNode( new QualifiedName( ftName, URI.create( nsp ) ), properties, nm,
                                                  freeSearch );
            parent.setChildNode( hn );
            parent = hn;
            node = (Element) XMLTools.getNode( node, "Type", CommonNamespaces.getNamespaceContext() );
        }
    }

    private Map<String, String> readPropertyNames( NamespaceContext nsc, Element ftnode )
                            throws XMLParsingException {
        String geogrIdPr = XMLTools.getRequiredNodeAsString( ftnode, "./GeographicIdentifier/@property", nsc );
        String altGeogrIdPr = XMLTools.getNodeAsString( ftnode, "./AlternativeGeographicIdentifier/@property", nsc,
                                                        null );
        String displayName = XMLTools.getNodeAsString( ftnode, "./DisplayName/@property", nsc, geogrIdPr );
        String parentIdPr = XMLTools.getNodeAsString( ftnode, "./ParentIdentifier/@property", nsc, null );
        String geoExtPr = XMLTools.getRequiredNodeAsString( ftnode, "./GeographicExtent/@property", nsc );
        String highlightGeometry = XMLTools.getNodeAsString( ftnode, "./HighlightGeometry/@property", nsc, geoExtPr );
        String posPr = XMLTools.getNodeAsString( ftnode, "./Position/@property", nsc, null );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "GeographicIdentifier", geogrIdPr );
        properties.put( "AlternativeGeographicIdentifier", altGeogrIdPr );
        properties.put( "ParentIdentifier", parentIdPr );
        properties.put( "GeographicExtent", geoExtPr );
        properties.put( "Position", posPr );
        properties.put( "DisplayName", displayName );
        properties.put( "HighlightGeometry", highlightGeometry );
        return properties;
    }

    /**
     * 
     * @return root node of a gazetteer hierarchy
     */
    public HierarchyNode getRoot() {
        return root;
    }

    /**
     * @return the gazetteerAddress
     */
    public String getGazetteerAddress() {
        return gazetteerAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     * @return parent node for {@link HierarchyNode} matching passed name
     */
    public HierarchyNode getParentOf( String name ) {
        HierarchyNode node = root;
        while ( node.getChildNode() != null ) {
            if ( node.getChildNode().equals( name ) ) {
                return node;
            }
            node = node.getChildNode();
        }
        return null;
    }

    /**
     * 
     * @param name
     * @return child node for {@link HierarchyNode} matching passed name
     */
    public HierarchyNode getChildOf( String name ) {
        HierarchyNode node = root;
        do {
            if ( node.equals( name ) ) {
                return node;
            }
            node = node.getChildNode();
        } while ( node.getChildNode() != null );
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

}
