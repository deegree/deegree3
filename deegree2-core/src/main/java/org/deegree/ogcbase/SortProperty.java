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
package org.deegree.ogcbase;

import static org.deegree.ogcbase.PropertyPathFactory.createPropertyPath;

import java.net.URI;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Java incarnation of the <code>SortPropertyType</code> defined in <code>sort.xsd</code> from the Filter Encoding
 * Specification.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SortProperty {

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    // property that will act as sort criterion
    private PropertyPath sortProperty;

    // true -> ascending, false -> descending
    private boolean sortOrder;

    private SortProperty( PropertyPath sortProperty, boolean sortOrder ) {
        this.sortProperty = sortProperty;
        this.sortOrder = sortOrder;
    }

    /**
     * Returns the property that will act as sort criterion.
     * 
     * @return the property that will act as sort criterion
     */
    public PropertyPath getSortProperty() {
        return this.sortProperty;
    }

    /**
     * Returns the sort order.
     * 
     * @return true, if the sort order is ascending, false if it is descending
     */
    public boolean getSortOrder() {
        return this.sortOrder;
    }

    /**
     * Creates a new <code>SortProperty</code> instance.
     * 
     * @param sortProperty
     * @param sortOrderString
     *            must be "ASC" or "DESC"
     * @return SortProperty instance
     * @throws InvalidParameterValueException
     *             if sortOrderString is not "ASC" or "DESC"
     */
    public static SortProperty create( PropertyPath sortProperty, String sortOrderString )
                            throws InvalidParameterValueException {
        boolean sortOrder;
        if ( "DESC".equals( sortOrderString ) ) {
            sortOrder = false;
        } else if ( "ASC".equals( sortOrderString ) ) {
            sortOrder = true;
        } else {
            String msg = "Invalid value '" + sortOrderString
                         + "' for 'SortOrder'. The only possible values are 'DESC' or 'ASC'";
            throw new InvalidParameterValueException( msg );
        }
        return new SortProperty( sortProperty, sortOrder );
    }

    /**
     * Creates an array of <code>SortProperty</code> instances from the giving string encoding (as described in OGC
     * 04-021r2, page 136).
     * 
     * @param sortBy
     * @param nscontext
     * @return array of <code>SortProperty</code> instances
     */
    public static SortProperty[] create( String sortBy, Map<String, URI> nscontext ) {

        SortProperty[] sortProperties = null;

        if ( sortBy != null ) {
            String[] parts = sortBy.split( "," );
            sortProperties = new SortProperty[parts.length];
            for ( int i = 0; i < parts.length; i++ ) {
                boolean sortOrder = false;

                String name;

                if ( parts[i].endsWith( " A" ) ) {
                    sortOrder = true;
                    name = parts[i].substring( 0, parts[i].length() - 2 );
                } else if ( parts[i].endsWith( " D" ) ) {
                    sortOrder = false;
                    name = parts[i].substring( 0, parts[i].length() - 2 );
                } else {
                    sortOrder = true; // it's the default
                    name = parts[i];
                }

                String[] nameps = name.split( ":" );

                PropertyPath pp;
                if ( nameps.length >= 2 ) {
                    pp = createPropertyPath( new QualifiedName( nameps[0], nameps[1], nscontext.get( nameps[0] ) ) );
                } else {
                    pp = createPropertyPath( new QualifiedName( name ) );
                }
                sortProperties[i] = new SortProperty( pp, sortOrder );
            }
        }
        return sortProperties;
    }

    /**
     * Parses the given <code>SortProperty</code> element.
     * 
     * @param element
     *            'ogc:SortProperty'-element
     * @return corresponding <code>SortProperty</code> instance
     * @throws XMLParsingException
     */
    public static SortProperty create( Element element )
                            throws XMLParsingException {

        Node node = XMLTools.getRequiredNode( element, "ogc:PropertyName/text()", nsContext );
        PropertyPath propertyName = OGCDocument.parsePropertyPath( (Text) node );
        String sortOrderString = XMLTools.getNodeAsString( element, "ogc:SortOrder/text()", nsContext, "ASC" );
        boolean sortOrder;
        if ( "ASC".equals( sortOrderString ) ) {
            sortOrder = true;
        } else if ( "DESC".equals( sortOrderString ) ) {
            sortOrder = false;
        } else {
            String msg = "Invalid value ('" + sortOrderString + "') for 'SortOrder'. The only "
                         + "valid values are 'ASC' or 'DESC'.";
            throw new XMLParsingException( msg );
        }

        SortProperty sortProperty = new SortProperty( propertyName, sortOrder );
        return sortProperty;
    }
}
