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

package org.deegree.geometry.gml.refs;

import org.deegree.commons.types.gml.StandardGMLObjectProps;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLObjectResolver;

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
public class GeometryReference<T extends Geometry> implements Geometry {

    private final GMLObjectResolver resolver;

    protected final String uri;
    
    protected final String baseURL;

    private T geometry;

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
    public GeometryReference( GMLObjectResolver resolver, String uri, String baseURL ) {
        this.resolver = resolver;
        this.uri = uri;
        this.baseURL = baseURL;
    }

    /**
     * Returns the URI of the feature.
     * 
     * @return the URI of the feature, never <code>null</code>
     */
    public String getURI() {
        return uri;
    }

    /**
     * Returns whether the URI is local, i.e. if it starts with the <code>#</code> character.
     * 
     * @return true, if the URI is local, false otherwise
     */
    public boolean isLocal() {
        return uri.startsWith( "#" );
    }

    /**
     * Returns the referenced {@link Geometry} instance (may trigger resolving and fetching it).
     * 
     * @return the referenced {@link Geometry} instance
     */
    public T getReferencedGeometry() {
        if ( this.geometry == null ) {
            geometry = (T) resolver.getGeometry( uri, baseURL );
        }
        return geometry;
    }    

    @Override
    public boolean contains( Geometry geometry ) {
        return getReferencedGeometry().contains( geometry );
    }

    @Override
    public boolean crosses( Geometry geometry ) {
        return getReferencedGeometry().crosses( geometry );
    }

    @Override
    public Geometry getDifference( Geometry geometry ) {
        return getReferencedGeometry().getDifference( geometry );
    }

    @Override
    public Measure getDistance( Geometry geometry, Unit requestedUnits ) {
        return getReferencedGeometry().getDistance( geometry, requestedUnits );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        return getReferencedGeometry().equals( geometry );
    }

    @Override
    public Geometry getBuffer( Measure distance ) {
        return getReferencedGeometry().getBuffer( distance );
    }

    @Override
    public Geometry getConvexHull() {
        return getReferencedGeometry().getConvexHull();
    }

    @Override
    public int getCoordinateDimension() {
        return getReferencedGeometry().getCoordinateDimension();
    }

    @Override
    public CRS getCoordinateSystem() {
        return getReferencedGeometry().getCoordinateSystem();
    }

    @Override
    public Envelope getEnvelope() {
        return getReferencedGeometry().getEnvelope();
    }

    @Override
    public GeometryType getGeometryType() {
        return getReferencedGeometry().getGeometryType();
    }

    @Override
    public String getId() {
        return getReferencedGeometry().getId();
    }

    @Override
    public PrecisionModel getPrecision() {
        return getReferencedGeometry().getPrecision();
    }

    @Override
    public Geometry getIntersection( Geometry geometry ) {
        return getReferencedGeometry().getIntersection( geometry );
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        return getReferencedGeometry().intersects( geometry );
    }

    @Override
    public boolean isDisjoint( Geometry geometry ) {
        return getReferencedGeometry().isDisjoint( geometry );
    }

    @Override
    public boolean overlaps( Geometry geometry ) {
        return getReferencedGeometry().overlaps( geometry );
    }

    @Override
    public boolean touches( Geometry geometry ) {
        return getReferencedGeometry().touches( geometry );
    }

    @Override
    public boolean isBeyond( Geometry geometry, Measure distance ) {
        return getReferencedGeometry().isBeyond( geometry, distance );
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        return getReferencedGeometry().isWithin( geometry );
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, Measure distance ) {
        return getReferencedGeometry().isWithinDistance( geometry, distance );
    }

    @Override
    public Geometry getUnion( Geometry geometry ) {
        return getReferencedGeometry().getUnion( geometry );
    }

    @Override
    public StandardGMLObjectProps getAttachedProperties() {
        return getReferencedGeometry().getAttachedProperties();
    }

    @Override
    public void setAttachedProperties( StandardGMLObjectProps standardProps ) {
        getReferencedGeometry().setAttachedProperties( standardProps );
    }

    @Override
    public Point getCentroid() {
        return getReferencedGeometry().getCentroid();
    }

    @Override
    public void setCoordinateSystem( CRS crs ) {
        getReferencedGeometry().setCoordinateSystem( crs );
    }

    @Override
    public void setId( String id ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrecision( PrecisionModel pm ) {
        getReferencedGeometry().setPrecision( pm );
    }
}
