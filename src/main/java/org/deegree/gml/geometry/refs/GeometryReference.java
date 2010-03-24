//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.gml.geometry.refs;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.GMLReference;
import org.deegree.gml.props.GMLStdProps;

/**
 * Represents a reference to the GML representation of a geometry, which is usually expressed using an
 * <code>xlink:href</code> attribute in GML (may be document-local or remote).
 * 
 * @param <T>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeometryReference<T extends Geometry> extends GMLReference<T> implements Geometry {

    /**
     * Creates a new {@link GeometryReference} instance.
     * 
     * @param resolver
     *            used for resolving the reference, must not be <code>null</code>
     * @param uri
     *            the geometry's uri, must not be <code>null</code>
     * @param baseURL
     *            base URL for resolving the uri, may be <code>null</code> (no resolving of relative URLs)
     */
    public GeometryReference( GMLReferenceResolver resolver, String uri, String baseURL ) {
        super (resolver, uri, baseURL);
    }

    @Override
    public boolean contains( Geometry geometry ) {
        return getReferencedObject().contains( geometry );
    }

    @Override
    public boolean crosses( Geometry geometry ) {
        return getReferencedObject().crosses( geometry );
    }

    @Override
    public Geometry getDifference( Geometry geometry ) {
        return getReferencedObject().getDifference( geometry );
    }

    @Override
    public Measure getDistance( Geometry geometry, Unit requestedUnits ) {
        return getReferencedObject().getDistance( geometry, requestedUnits );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        return getReferencedObject().equals( geometry );
    }

    @Override
    public Geometry getBuffer( Measure distance ) {
        return getReferencedObject().getBuffer( distance );
    }

    @Override
    public Geometry getConvexHull() {
        return getReferencedObject().getConvexHull();
    }

    @Override
    public int getCoordinateDimension() {
        return getReferencedObject().getCoordinateDimension();
    }

    @Override
    public CRS getCoordinateSystem() {
        return getReferencedObject().getCoordinateSystem();
    }

    @Override
    public Envelope getEnvelope() {
        return getReferencedObject().getEnvelope();
    }

    @Override
    public GeometryType getGeometryType() {
        return getReferencedObject().getGeometryType();
    }

    @Override
    public String getId() {
        return getReferencedObject().getId();
    }

    @Override
    public PrecisionModel getPrecision() {
        return getReferencedObject().getPrecision();
    }

    @Override
    public Geometry getIntersection( Geometry geometry ) {
        return getReferencedObject().getIntersection( geometry );
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        return getReferencedObject().intersects( geometry );
    }

    @Override
    public boolean isDisjoint( Geometry geometry ) {
        return getReferencedObject().isDisjoint( geometry );
    }

    @Override
    public boolean overlaps( Geometry geometry ) {
        return getReferencedObject().overlaps( geometry );
    }

    @Override
    public boolean touches( Geometry geometry ) {
        return getReferencedObject().touches( geometry );
    }

    @Override
    public boolean isBeyond( Geometry geometry, Measure distance ) {
        return getReferencedObject().isBeyond( geometry, distance );
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        return getReferencedObject().isWithin( geometry );
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, Measure distance ) {
        return getReferencedObject().isWithinDistance( geometry, distance );
    }

    @Override
    public Geometry getUnion( Geometry geometry ) {
        return getReferencedObject().getUnion( geometry );
    }

    @Override
    public GMLStdProps getGMLProperties() {
        return getReferencedObject().getGMLProperties();
    }

    @Override
    public void setGMLProperties( GMLStdProps standardProps ) {
        getReferencedObject().setGMLProperties( standardProps );
    }

    @Override
    public Point getCentroid() {
        return getReferencedObject().getCentroid();
    }

    @Override
    public void setCoordinateSystem( CRS crs ) {
        getReferencedObject().setCoordinateSystem( crs );
    }

    @Override
    public void setId( String id ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrecision( PrecisionModel pm ) {
        getReferencedObject().setPrecision( pm );
    }
}
