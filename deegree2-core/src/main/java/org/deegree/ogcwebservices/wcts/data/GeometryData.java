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

package org.deegree.ogcwebservices.wcts.data;

import java.util.ArrayList;
import java.util.List;

import org.deegree.crs.transformations.Transformation;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * <code>GeometryData</code> encapsulates a list of geometries which can be transformed using the
 * {@link #doTransform(Transformation, boolean)} or {@link #doTransform(CoordinateSystem, CoordinateSystem, boolean)}
 * methods.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class GeometryData extends TransformableData<Geometry> {
    private List<Geometry> sourceGeometries;

    private final List<Geometry> transformedGeometries;

    private static ILogger LOG = LoggerFactory.getLogger( GeometryData.class );

    /**
     * Creates a data instance which handles geometries.
     *
     * @param transformableData
     *            to transform
     * @throws IllegalArgumentException
     *             if either one of the crs's are <code>null</code>.
     */
    public GeometryData( List<Geometry> transformableData ) throws IllegalArgumentException {

        if ( transformableData == null ) {
            transformableData = new ArrayList<Geometry>();
        }
        this.sourceGeometries = transformableData;
        transformedGeometries = new ArrayList<Geometry>( this.sourceGeometries.size() );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wcts.operation.TransformableData#doTransform(boolean)
     */
    @Override
    public void doTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, boolean enableLogging )
                            throws OGCWebServiceException {
        LOG.logDebug( "Trying to transform the geometries with default transformation." );
        GeoTransformer transformer = getGeotransformer( targetCRS );
        doTransform( sourceCRS, transformer, enableLogging );
    }

    @Override
    public void doTransform( Transformation transformation, boolean enableLogging )
                            throws OGCWebServiceException {
        LOG.logDebug( "Trying to transform the geometries with a transformation." );
        GeoTransformer transformer = getGeotransformer( transformation );
        doTransform( CRSFactory.create( transformation.getSourceCRS() ), transformer, enableLogging );
    }

    private void doTransform( CoordinateSystem sourceCRS, GeoTransformer transformer, @SuppressWarnings("unused")
    boolean enableLogging )
                            throws OGCWebServiceException {
        for ( Geometry geom : sourceGeometries ) {
            try {
                if ( !sourceCRS.equals( geom.getCoordinateSystem() ) ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage( "WCTS_MISMATCHING_CRS_DEFINITIONS",
                                                                           sourceCRS.getIdentifier(),
                                                                           geom.getCoordinateSystem().getIdentifier() ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
                }
                LOG.logDebug( "Transforming geometry: " + geom );
                transformedGeometries.add( transformer.transform( geom ) );
            } catch ( IllegalArgumentException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( CRSTransformationException e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wcts.operation.TransformableData#getResult()
     */
    @Override
    public List<Geometry> getTransformedData() {
        return transformedGeometries;
    }

    /**
     * Try to create a Geometry from a given String, put the result into a GeometryData object.
     *
     * @param sourceCRS
     *            in which the data is referenced
     * @param wkt
     *            to create the geometry from.
     * @return a geometry data or <code>null</code> if the wkt could not be parsed.
     * @throws OGCWebServiceException
     *             if run into an exception (thrown by the {@link WKTAdapter} any other then a GeometryException.
     */
    public static GeometryData parseGeometryData( CoordinateSystem sourceCRS, String wkt )
                            throws OGCWebServiceException {
        if ( wkt == null ) {
            return null;
        }
        GeometryData result = null;
        try {
            Geometry parsedGeom = WKTAdapter.wrap( wkt, sourceCRS );
            if ( parsedGeom != null ) {
                LOG.logDebug( "The geomety is of type: ", parsedGeom );
                List<Geometry> pg = new ArrayList<Geometry>();
                pg.add( parsedGeom );
                result = new GeometryData( pg );
            }
        } catch ( GeometryException e ) {
            LOG.logDebug( "No parsable geometry found:  ", e.getLocalizedMessage() );
        } catch ( Exception e ) {
            LOG.logError( e );
            throw new OGCWebServiceException( e.getLocalizedMessage(), ExceptionCode.INVALIDPARAMETERVALUE );
        }
        return result;
    }

}
