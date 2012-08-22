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
package org.deegree.graphics.sld;

import java.util.List;

import org.deegree.framework.xml.Marshallable;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;

/**
 * The Geometry element is optional and if it is absent then the default geometry property of the feature type that is
 * used in the containing FeatureStyleType is used. The precise meaning of default geometry property is
 * system-dependent. Most frequently, feature types will have only a single geometry property.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */

public class Geometry implements Marshallable {

    private org.deegree.model.spatialschema.Geometry geometry = null;

    private PropertyPath propertyPath = null;

    /**
     * @param propertyPath
     * @param geometry
     */
    public Geometry( PropertyPath propertyPath, org.deegree.model.spatialschema.Geometry geometry ) {
        this.propertyPath = propertyPath;
        this.geometry = geometry;
    }

    /**
     * returns xpath information of the geometry property
     * 
     * @return xpath information of the geometry property
     */
    public PropertyPath getPropertyPath() {
        return propertyPath;
    }

    /**
     * In principle, a fixed geometry could be defined using GML or operators could be defined for computing a geometry
     * from references or literals. This enbales the calling client to submitt the geometry to be rendered by the WMS
     * directly. (This is not part of the SLD XML-schema)
     * 
     * @return the Geometry
     * 
     */
    public org.deegree.model.spatialschema.Geometry getGeometry() {
        return geometry;
    }

    /**
     * exports the content of the Geometry as XML formated String
     * 
     * @return xml representation of the Geometry
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<Geometry>" );
        if ( propertyPath != null ) {
            int c = propertyPath.getSteps();
            List<PropertyPathStep> list = propertyPath.getAllSteps();
            sb.append( "<ogc:PropertyName" );
            for ( int i = 0; i < c; i++ ) {
                if ( list.get( i ).getPropertyName().getNamespace() != null ) {
                    sb.append( " xmlns:" ).append( list.get( i ).getPropertyName().getPrefix() );
                    sb.append( "='" ).append( list.get( i ).getPropertyName().getNamespace().toASCIIString() );
                    sb.append( "'" );
                }
            }
            sb.append( ">" );
            sb.append( propertyPath.getAsString() );
            sb.append( "</ogc:PropertyName>" );
        } else {
            try {
                sb.append( GMLGeometryAdapter.export( geometry ) );
            } catch ( GeometryException e ) {
                e.printStackTrace();
            }
        }

        sb.append( "</Geometry>" );

        return sb.toString();
    }
}
