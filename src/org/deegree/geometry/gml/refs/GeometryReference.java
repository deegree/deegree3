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

import java.net.URL;

import org.deegree.commons.types.gml.StandardObjectProperties;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.feature.gml.GMLIdContext;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.uom.Unit;
import org.deegree.geometry.uom.ValueWithUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger( GMLIdContext.class );

    protected String href;

    private String gid;

    private T referencedGeometry;

    /** true: local xlink (#...), false: external xlink */
    protected boolean isLocal;

    private String baseURL;

    public GeometryReference( String href, String baseURL ) {
        this.href = href;
        this.baseURL = baseURL;
        int pos = href.lastIndexOf( '#' );
        if ( pos < 0 ) {
            isLocal = false;
        } else {
            isLocal = true;
            gid = href.substring( pos + 1 );
        }
    }

    /**
     * Returns whether the reference is document-local (xlink: #...) or remote.
     * 
     * @return true, if the reference is document-local, false otherwise
     */
    public boolean isLocal() {
        return isLocal;
    }

    public void resolve( T geometry ) {
        if ( this.referencedGeometry != null ) {
            String msg = "Internal error: Geometry reference (" + href + ") has already been resolved.";
            throw new RuntimeException( msg );
        }
        this.referencedGeometry = geometry;
    }

    protected T getGeometry() {
        if ( referencedGeometry == null ) {
            if ( isLocal ) {
                String msg = "Internal error: Reference to local geometry (" + href + ") has not been resolved.";
                throw new RuntimeException( msg );
            }
            LOG.info( "Trying to resolve reference to external geometry: '" + href + "', base system id: " + baseURL );
            GML311GeometryDecoder decoder = new GML311GeometryDecoder();
            try {
                URL resolvedURL = null;
                if (baseURL != null) {
                    resolvedURL = new URL (new URL(baseURL), href);
                } else {
                    resolvedURL = new URL( href );
                }
                XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( resolvedURL );
                xmlReader.nextTag();
                referencedGeometry = (T) decoder.parseAbstractGeometry( xmlReader, null );
                LOG.debug( "Read GML geometry: '" + referencedGeometry.getClass() + "'" );
                xmlReader.close();
            } catch ( Exception e ) {
                throw new RuntimeException( "Unable to resolve external geometry reference: " + e.getMessage() );
            }
        }
        return referencedGeometry;
    }

    public boolean contains( Geometry geometry ) {
        return getGeometry().contains( geometry );
    }

    public boolean crosses( Geometry geometry ) {
        return getGeometry().crosses( geometry );
    }

    public Geometry difference( Geometry geometry ) {
        return getGeometry().difference( geometry );
    }

    public ValueWithUnit distance( Geometry geometry, Unit requestedUnits ) {
        return getGeometry().distance( geometry, requestedUnits );
    }

    public boolean equals( Geometry geometry ) {
        return getGeometry().equals( geometry );
    }

    public Geometry getBuffer( ValueWithUnit distance ) {
        return getGeometry().getBuffer( distance );
    }

    public Geometry getConvexHull() {
        return getGeometry().getConvexHull();
    }

    public int getCoordinateDimension() {
        return getGeometry().getCoordinateDimension();
    }

    public CRS getCoordinateSystem() {
        return getGeometry().getCoordinateSystem();
    }

    public Envelope getEnvelope() {
        return getGeometry().getEnvelope();
    }

    public GeometryType getGeometryType() {
        return getGeometry().getGeometryType();
    }

    public String getId() {
        if ( isLocal ) {
            return gid;
        }
        return getGeometry().getId();
    }

    public PrecisionModel getPrecision() {
        return getGeometry().getPrecision();
    }

    public Geometry intersection( Geometry geometry ) {
        return getGeometry().intersection( geometry );
    }

    public boolean intersects( Geometry geometry ) {
        return getGeometry().intersects( geometry );
    }

    public boolean isDisjoint( Geometry geometry ) {
        return getGeometry().isDisjoint( geometry );
    }

    public boolean overlaps( Geometry geometry ) {
        return getGeometry().overlaps( geometry );
    }

    public boolean touches( Geometry geometry ) {
        return getGeometry().touches( geometry );
    }

    public boolean isBeyond( Geometry geometry, ValueWithUnit distance ) {
        return getGeometry().isBeyond( geometry, distance );
    }

    public boolean isWithin( Geometry geometry ) {
        return getGeometry().isWithin( geometry );
    }

    public boolean isWithinDistance( Geometry geometry, ValueWithUnit distance ) {
        return getGeometry().isWithinDistance( geometry, distance );
    }

    public Geometry union( Geometry geometry ) {
        return getGeometry().union( geometry );
    }

    @Override
    public com.vividsolutions.jts.geom.Geometry getJTSGeometry() {
        return getGeometry().getJTSGeometry();
    }

    @Override
    public StandardObjectProperties getStandardGMLProperties() {
        return getGeometry().getStandardGMLProperties();
    }

    @Override
    public void setStandardGMLProperties( StandardObjectProperties standardProps ) {
        getGeometry().setStandardGMLProperties( standardProps );
    }
}
