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

import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a <FeatureId>element as defined in the FeatureId DTD. The
 * <FeatureId>element is used to encode the unique identifier for any feature instance. Within a
 * filter expression, the <FeatureId>is used as a reference to a particular feature instance.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureId {

    /**
     * The FeatureId's value.
     */
    private String value;

    /**
     * Constructs a new FeatureId.
     *
     * @param value
     */
    public FeatureId( String value ) {
        this.value = value;
    }

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively
     * calls other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     *
     * @return feature id
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static FeatureId buildFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name equals 'FeatureId'
        if ( !element.getLocalName().toLowerCase().equals( "featureid" ) )
            throw new FilterConstructionException( "Name of element does not equal 'FeatureId'!" );

        // determine the value of the FeatureId
        String fid = element.getAttribute( "fid" );
        if ( fid == null || "".equals( fid ) )
            throw new FilterConstructionException( "<FeatureId> requires 'fid'-attribute!" );

        return new FeatureId( fid );
    }

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively
     * calls other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     *
     * @return feature id
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static FeatureId buildGMLIdFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name equals 'GmlObjectId'
        if ( !element.getLocalName().equals( "GmlObjectId" ) )
            throw new FilterConstructionException( "Name of element does not equal 'GmlObjectId'!" );

        // determine the requested id
        String gmlId = element.getAttributeNS( CommonNamespaces.GMLNS.toString(), "id" );
        if ( gmlId == null || "".equals( gmlId ) )
            throw new FilterConstructionException( "<GmlObjectId> requires 'gml:id'-attribute!" );

        return new FeatureId( gmlId );
    }

    /**
     * Returns the feature id. A feature id is built from it's feature type name and it's id
     * separated by a ".". e.g. Road.A565
     *
     * @return feature id value
     */
    public String getValue() {
        return value;
    }

    /**
     * @see org.deegree.model.filterencoding.FeatureId#getValue()
     * @param value
     */
    public void setValue( String value ) {
        this.value = value;
    }

    /**
     * Produces a XML representation of this object.
     *
     * @return xml representation
     */
    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append( "<ogc:FeatureId fid=\"" ).append( value ).append( "\"/>" );
        return sb;
    }
}
