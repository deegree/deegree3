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
package org.deegree.model.coverage.raster.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.io.shpapi.shape_new.Shape;
import org.deegree.io.shpapi.shape_new.ShapeFileReader;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;

import org.deegree.model.legacy.GeometryConverter;

/**
 * This class reads and saves shapefiles with tile index.
 * 
 * A tileindex is a mapping between an envelope and a name of a file that covers this envelope.
 * 
 * @see TileIndex
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * 
 */
public class TileIndexReader {

    final static String LOCATION_FIELD_NAME = "LOCATION";

    final static String GEOMETRY_FIELD_NAME = "GEOM";

    private static Log log = LogFactory.getLog( TileIndexReader.class );

    /**
     * This static method reads a shapefile with a tileindex.
     * 
     * @param tileindex
     *            filename of the tileindex shape file
     * @return TileIndex with filename and envelope for each Tile
     * @throws IOException
     */
    public static TileIndex readTileIndex( String tileindex )
                            throws IOException {
        TileIndex result = new TileIndex();

        ShapeFileReader reader = new ShapeFileReader( tileindex );

        DBaseFile db = reader.getTables();

        String basedir = new File( tileindex ).getParent() + File.separator;
        try {
            for ( Shape shape : reader.read().getShapes() ) {
                db.nextRecord();
                String filename = db.getColumn( LOCATION_FIELD_NAME );
                Envelope env = GeometryConverter.fromLegacy( shape.getEnvelope() );
                result.add( basedir + filename, env );
            }
        } catch ( DBaseException e ) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * This static method creates a new tile index shapefile.
     * 
     * @param index
     *            TileIndex with filename and envelope for each tile
     * @param basename
     *            basename of the output shape file (i.e. without .shp)
     * @throws Exception
     */
    public static void saveTileIndex( TileIndex index, String basename )
                            throws IOException {

        FeatureCollection col = FeatureFactory.createFeatureCollection( "", 100 );

        PropertyType locationType = FeatureFactory.createSimplePropertyType( new QualifiedName( LOCATION_FIELD_NAME ),
                                                                             Types.VARCHAR, false );

        PropertyType geomType = FeatureFactory.createSimplePropertyType( new QualifiedName( GEOMETRY_FIELD_NAME ),
                                                                         Types.GEOMETRY, false );

        FeatureType tileType = FeatureFactory.createFeatureType( new QualifiedName( "tile" ), false,
                                                                 new PropertyType[] { locationType, geomType } );

        for ( String tile : index ) {
            try {
                FeatureProperty[] content = new FeatureProperty[2];
                Envelope tileEnv = index.getEnvelope( tile );
                content[0] = FeatureFactory.createFeatureProperty( new QualifiedName( LOCATION_FIELD_NAME ), tile );
                Geometry geom = GeometryConverter.toLegacyGeometry( tileEnv );
                content[1] = FeatureFactory.createFeatureProperty( new QualifiedName( GEOMETRY_FIELD_NAME ), geom );
                Feature feature = FeatureFactory.createFeature( tile, tileType, content );
                col.add( feature );
            } catch ( GeometryException e ) {
                e.printStackTrace();
            }
        }

        if ( col.size() == 0 ) {
            log.warn( "TileIndex is empty. Shapefile will not be written." );
        } else {
            try {
                ShapeFile shapes = new ShapeFile( basename, "rw" );
                shapes.writeShape( col );
                shapes.close();
                // ShapeFileWriter writer = new ShapeFileWriter( shapes );
                // writer.write( basename );
            } catch ( Exception e ) {
                e.printStackTrace();
                throw new IOException();
            }
        }
    }

}
