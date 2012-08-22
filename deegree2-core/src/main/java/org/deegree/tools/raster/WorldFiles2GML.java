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
package org.deegree.tools.raster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.StringTools;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.tools.shape.GML2Shape_new;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WorldFiles2GML {

    private File[] files;

    private WorldFile.TYPE wft;

    /**
     * @param files
     * @param wft
     */
    public WorldFiles2GML( File[] files, WorldFile.TYPE wft ) {
        this.files = files;
        this.wft = wft;
    }

    private FeatureType createFeatureType()
                            throws URISyntaxException {

        PropertyType[] ftps = new PropertyType[2];

        QualifiedName qn = new QualifiedName( "app", "name", new URI( "http://www.deegree.org/app" ) );
        ftps[0] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, true );

        qn = new QualifiedName( "app", "geom", new URI( "http://www.deegree.org/app" ) );
        ftps[1] = FeatureFactory.createSimplePropertyType( qn, Types.GEOMETRY, true );

        qn = new QualifiedName( "app", "WorldFiles", new URI( "http://www.deegree.org/app" ) );
        return FeatureFactory.createFeatureType( qn, false, ftps );
    }

    /**
     * @return a feature collection
     * @throws IOException
     * @throws GeometryException
     * @throws URISyntaxException
     */
    public FeatureCollection perform()
                            throws IOException, GeometryException, URISyntaxException {
        FeatureType ft = createFeatureType();
        FeatureCollection fc = FeatureFactory.createFeatureCollection( "FC", 200 );
        for ( int i = 0; i < files.length; i++ ) {
            WorldFile wf = WorldFile.readWorldFile( files[i].getAbsolutePath(), wft );
            Geometry geom = GeometryFactory.createSurface( wf.getEnvelope(), null );
            FeatureProperty[] fps = new FeatureProperty[2];

            QualifiedName qn = new QualifiedName( "app", "name", new URI( "http://www.deegree.org/app" ) );
            fps[0] = FeatureFactory.createFeatureProperty( qn, files[i].getAbsoluteFile() );
            qn = new QualifiedName( "app", "geom", new URI( "http://www.deegree.org/app" ) );
            fps[1] = FeatureFactory.createFeatureProperty( qn, geom );
            fc.add( FeatureFactory.createFeature( "id" + i, ft, fps ) );
        }
        return fc;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties props = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            props.put( args[i], args[i + 1] );
        }

        WorldFile.TYPE wft = WorldFile.TYPE.CENTER;
        if ( "outer".equals( props.get( "-type" ) ) ) {
            wft = WorldFile.TYPE.OUTER;
        }
        System.out.println( wft );
        List<String> ext = StringTools.toList( props.getProperty( "-ext" ), ",", true );
        ConvenienceFileFilter ff = new ConvenienceFileFilter( ext, true );
        File dir = new File( props.getProperty( "-rootDir" ) );
        File[] files = dir.listFiles( ff );
        WorldFiles2GML wf2g = new WorldFiles2GML( files, wft );
        FeatureCollection fc = wf2g.perform();

        GMLFeatureAdapter ada = new GMLFeatureAdapter( true );
        FileOutputStream fos = new FileOutputStream( dir.getAbsolutePath() + "/gml.xml" );
        ada.export( fc, fos );
        fos.close();

        GML2Shape_new rws = new GML2Shape_new( dir.getAbsolutePath() + "/gml.xml", dir.getAbsolutePath() + "/gml" );
        rws.read();
        rws.write();

        System.out.println( "finished" );

    }

}
