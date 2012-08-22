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
package org.deegree.model.filterencoding;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract superclass representing <code>Filter</code> elements (as defined in the Filter DTD). A
 * <code>Filter</code> element either consists of (one or more) FeatureId-elements or one
 * operation-element. This is reflected in the two implementations FeatureFilter and ComplexFilter.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class AbstractFilter implements Filter {

    /**
     * Given a DOM-fragment, a corresponding Filter-object is built. This method recursively calls
     * other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     * @return corresponding Filter-object
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     * @deprecated use the 1.0.0 filter encoding aware method instead.
     */
    @Deprecated
    public static Filter buildFromDOM( Element element )
                            throws FilterConstructionException {
        return buildFromDOM( element, false );
    }

    /**
     * Given a DOM-fragment, a corresponding Filter-object is built. This method recursively calls
     * other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     * @param useVersion_1_0_0
     *            if the filter encoding 1_0_0 should be used.
     * @return corresponding Filter-object
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Filter buildFromDOM( Element element, boolean useVersion_1_0_0 )
                            throws FilterConstructionException {
        Filter filter = null;

        // check if root element's name equals 'filter'
        if ( !element.getLocalName().equals( "Filter" ) ) {
            throw new FilterConstructionException( "Name of element does not equal 'Filter'!" );
        }

        // determine type of Filter (FeatureFilter / ComplexFilter)
        Element firstElement = null;
        NodeList children = element.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
                firstElement = (Element) children.item( i );
                break;
            }
        }
        if ( firstElement == null ) {
            throw new FilterConstructionException( "Filter node is empty!" );
        }

        if ( firstElement.getLocalName().equals( "FeatureId" ) ) {
            // must be a FeatureFilter
            FeatureFilter fFilter = new FeatureFilter();
            children = element.getChildNodes();
            for ( int i = 0; i < children.getLength(); i++ ) {
                if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
                    Element fid = (Element) children.item( i );
                    if ( !fid.getLocalName().equals( "FeatureId" ) )
                        throw new FilterConstructionException( "Unexpected element encountered: " + fid.getLocalName() );
                    fFilter.addFeatureId( FeatureId.buildFromDOM( fid ) );
                }
            }
            filter = fFilter;
        } else if ( firstElement.getLocalName().equals( "GmlObjectId" ) ) {
            // must be a FeatureFilter
            FeatureFilter fFilter = new FeatureFilter();
            children = element.getChildNodes();
            for ( int i = 0; i < children.getLength(); i++ ) {
                if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
                    Element fid = (Element) children.item( i );
                    if ( !fid.getLocalName().equals( "GmlObjectId" ) )
                        throw new FilterConstructionException( "Unexpected element encountered: " + fid.getLocalName() );
                    fFilter.addFeatureId( FeatureId.buildGMLIdFromDOM( fid ) );
                }
            }
            filter = fFilter;
        } else {
            // must be a ComplexFilter
            children = element.getChildNodes();
            boolean justOne = false;
            for ( int i = 0; i < children.getLength(); i++ ) {
                if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
                    Element operator = (Element) children.item( i );
                    if ( justOne )
                        throw new FilterConstructionException( "Unexpected element encountered: "
                                                               + operator.getLocalName() );
                    ComplexFilter cFilter = new ComplexFilter( AbstractOperation.buildFromDOM( operator,
                                                                                               useVersion_1_0_0 ) );
                    filter = cFilter;
                    justOne = true;
                }
            }
        }
        return filter;
    }
}
