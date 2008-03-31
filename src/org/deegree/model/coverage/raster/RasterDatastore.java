//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wfs.operation.Query;

import org.deegree.model.legacy.GeometryConverter;

public class RasterDatastore implements TileContainer {

    Datastore datastore;

    String baseDir;

    ComplexFilter currentFilter;

    Envelope envelope;

    RasterEnvelope rasterEnvelope;

    enum ComplexRasterType {
        TILED, MULTIRANGED, COMBINED
    }

    ComplexRasterType type;

    private static final URI DEEGREEAPP = CommonNamespaces.buildNSURI( "http://www.deegree.org/app" );

    Log log = LogFactory.getLog( RasterDatastore.class );

    RasterDatastore( Datastore datastore, ComplexFilter filter, Envelope subset, RasterEnvelope rasterEnv,
                     String baseDir, ComplexRasterType type ) {
        this.datastore = datastore;
        this.currentFilter = filter;
        this.envelope = subset;
        this.rasterEnvelope = rasterEnv;
        this.baseDir = baseDir;
        this.type = type;
    }

    public RasterDatastore( Datastore datastore, String baseDir ) {
        this.datastore = datastore;
        this.currentFilter = new ComplexFilter( OperationDefines.AND );
        this.baseDir = baseDir;
        this.type = ComplexRasterType.TILED;
    }

    public RasterDatastore getSubset( ComplexFilter filter ) {
        // TODO: check for spatial filter and update spatialSubset?
        ComplexFilter combinedFilter = new ComplexFilter( currentFilter, filter, OperationDefines.AND );
        return new RasterDatastore( this.datastore, combinedFilter, envelope, rasterEnvelope, baseDir, type );
    }

    public RasterDatastore getSubset( Envelope env ) {
        Geometry envGeom = null;
        try {
            envGeom = GeometryConverter.toLegacyGeometry( env );
        } catch ( GeometryException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PropertyName geomProperty = new PropertyName( new QualifiedName( "app:geom", DEEGREEAPP ) );
        ComplexFilter spatialFilter = new ComplexFilter( new SpatialOperation( OperationDefines.BBOX, geomProperty,
                                                                               envGeom ) );
        ComplexFilter combinedFilter = new ComplexFilter( currentFilter, spatialFilter, OperationDefines.AND );

        return new RasterDatastore( datastore, combinedFilter, env, rasterEnvelope.createSubEnvelope( env ), baseDir,
                                    type );
    }

    public AbstractRaster getAbstractRaster() {
        if ( type == ComplexRasterType.TILED ) {
            TileContainer container = new MemoryTileContainer( getTiles() );
            return new TiledRaster( container ).getSubset( envelope );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns absolut pathname for feature property LOCATION.
     */
    private String getLocationProperty( Feature feature ) {
        QualifiedName locationQN = new QualifiedName( "app:location", DEEGREEAPP );
        FeatureProperty locationProp = feature.getProperties( locationQN )[0];

        String location = (String) locationProp.getValue();

        return baseDir + File.separator + location;
    }

    public Envelope getEnvelope() {
        if ( envelope != null ) {
            return envelope;
        } else {
            log.debug( "calculating envelope" );
            // TODO: create a query for bbox
            ComplexFilter nullFilter = new ComplexFilter( OperationDefines.AND );
            QualifiedName tileTypeName = new QualifiedName( "app:Tile", DEEGREEAPP );
            MappedFeatureType featureType = datastore.getFeatureType( tileTypeName );
            MappedFeatureType[] rootFts = new MappedFeatureType[] { featureType };
            Query query = Query.create( tileTypeName, nullFilter );

            Envelope result;
            try {
                FeatureCollection fc = datastore.performQuery( query, rootFts );
                result = GeometryConverter.fromLegacy( fc.getBoundedBy() );
            } catch ( Exception e ) {
                log.warn( "accessing dummy Envelope" );
                result = GeometryFactoryCreator.getInstance().getGeometryFactory().createEnvelope(
                                                                                                   new double[] {
                                                                                                                 Double.MAX_VALUE,
                                                                                                                 Double.MAX_VALUE },
                                                                                                   new double[] {
                                                                                                                 Double.MIN_VALUE,
                                                                                                                 Double.MIN_VALUE },
                                                                                                   0.0001, null );
            }
            return result;
        }
    }

    public RasterEnvelope getRasterEnvelope() {
        if ( rasterEnvelope != null ) {
            return rasterEnvelope;
        } else {
            log.debug( "calculating raster envelope" );
            ComplexFilter nullFilter = new ComplexFilter( OperationDefines.AND );
            QualifiedName tileTypeName = new QualifiedName( "app:Tile", DEEGREEAPP );
            MappedFeatureType featureType = datastore.getFeatureType( tileTypeName );
            MappedFeatureType[] rootFts = new MappedFeatureType[] { featureType };
            Query query = Query.create( tileTypeName, nullFilter );

            query.setMaxFeatures( 1 );
            try {
                FeatureCollection fc = datastore.performQuery( query, rootFts );
                String filename = getLocationProperty( fc.getFeature( 0 ) );
                RasterEnvelope tmpEnv = RasterFactory.createRasterFromFile( filename ).getRasterEnvelope();
                rasterEnvelope = tmpEnv.createSubEnvelope( getEnvelope() );
            } catch ( Exception e ) {
                log.warn( "accessing dummy RasterEnvelope" );
                rasterEnvelope = new RasterEnvelope( getEnvelope(), 1000, 1000 );
            }
            return rasterEnvelope;
        }
    }

    public List<AbstractRaster> getTiles() {
        List<AbstractRaster> tiles = new ArrayList<AbstractRaster>();

        QualifiedName tileTypeName = null;

        tileTypeName = new QualifiedName( "app:Tile", DEEGREEAPP );

        MappedFeatureType featureType = datastore.getFeatureType( tileTypeName );
        MappedFeatureType[] rootFts = new MappedFeatureType[] { featureType };
        Query query = Query.create( tileTypeName, currentFilter );

        try {
            FeatureCollection fc = datastore.performQuery( query, rootFts );
            Iterator<Feature> iter = fc.iterator();
            while ( iter.hasNext() ) {
                String filename = getLocationProperty( iter.next() );
                log.debug( "filter match for " + filename );
                AbstractRaster raster = RasterFactory.createRasterFromFile( filename );
                tiles.add( raster );
            }
        } catch ( DatastoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tiles;
    }

    public List<AbstractRaster> getTiles( Envelope env ) {
        return getSubset( env ).getTiles();
    }
}
