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
package org.deegree.filter.spatial;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.Feature;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

/**
 * {@link SpatialOperator} that checks for the intersection of the two geometry operands' envelopes.
 * <p>
 * Note that the first (@link {@link Expression}) argument may be <code>omitted</code>: <br/>
 * <br/>
 * From Filter Encoding Implementation Specification 1.1: <i>If the optional &lt;PropertyName&gt; element is not
 * specified, the calling service must determine which spatial property is the spatial key and apply the BBOX operator
 * accordingly. For feature types that has a single spatial property, this is a trivial matter. For feature types that
 * have multiple spatial properties, the calling service either knows which spatial property is the spatial key or the
 * calling service generates an exception indicating that the feature contains multiple spatial properties and the
 * <propertyName> element must be specified.</i>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class BBOX extends SpatialOperator {

    private final Envelope param2;

    /**
     * Creates a new {@link BBOX} instance which uses the default geometry property and the specified bounding box.
     * 
     * @param param2
     *            bounding box argument for intersection testing, never <code>null</code>
     */
    public BBOX( Envelope param2 ) {
        this( null, param2 );
    }

    /**
     * Creates a new {@link BBOX} instance which uses the specified geometry property and bounding box.
     * 
     * @param param1
     * @param param2
     *            bounding box argument for intersection testing, never <code>null</code>
     */
    public BBOX( Expression param1, Envelope param2 ) {
        super( param1 );
        this.param2 = param2;
    }

    /**
     * Returns the name of the property to be tested for intersection.
     * 
     * @return the name of the property, may be <code>null</code> (implies that the default geometry property of the
     *         object should be used)
     * @deprecated use {@link #getParam1()} instead
     */
    @Override
    public ValueReference getPropName() {
        return (ValueReference) propName;
    }

    /**
     * Returns the envelope which is tested for intersection.
     * 
     * @return the envelope, never <code>null</code>
     */
    public Envelope getBoundingBox() {
        return param2;
    }

    @Override
    public <T> boolean evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException {

        // handle the BBOX-specific case that the property name can be empty
        ValueReference propName = this.getPropName();
        if ( propName == null ) {
            if ( obj instanceof Feature ) {
                GeometryPropertyType pt = ( (Feature) obj ).getType().getDefaultGeometryPropertyDeclaration();
                if ( pt != null ) {
                    propName = new ValueReference( pt.getName() );
                }
            }
        }
        if ( propName != null ) {
            for ( TypedObjectNode paramValue : propName.evaluate( obj, xpathEvaluator ) ) {
                Geometry param1Value = checkGeometryOrNull( paramValue );
                if ( param1Value != null ) {
                    Envelope transformedBBox = (Envelope) getCompatibleGeometry( param1Value, param2 );
                    return transformedBBox.intersects( param1Value );
                }
            }
        } else if ( obj instanceof Feature ) {
            Feature f = (Feature) obj;
            if ( f.getGMLProperties() != null && f.getGMLProperties().getBoundedBy() != null ) {
                Envelope transformedBBox = (Envelope) getCompatibleGeometry( f.getGMLProperties().getBoundedBy(),
                                                                             param2 );
                return transformedBBox.intersects( f.getGMLProperties().getBoundedBy() );
            }
        }
        return false;
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-BBOX\n";
        s += indent + propName + "\n";
        s += indent + param2;
        return s;
    }

    @Override
    public Object[] getParams() {
        return new Object[] { propName, param2 };
    }
}