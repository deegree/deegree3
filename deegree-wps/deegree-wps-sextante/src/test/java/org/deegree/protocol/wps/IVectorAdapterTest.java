//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.provider.IVectorLayerAdapter;
import org.junit.Test;

import es.unex.sextante.dataObjects.IVectorLayer;

/**
 * Tests the functionality of the IVectorLayerAdapter class.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class IVectorAdapterTest {

    /**
     * Tests the IVectorAdapter with geometries.
     * 
     */
    @Test
    public void testGeometries() {
        try {
            LinkedList<Geometry> geoms = readGeometries();

            for ( Geometry gIn : geoms ) {

                // create vector layer
                IVectorLayer layer = IVectorLayerAdapter.createVectorLayer( gIn );

                // create geometry
                Geometry gOut = IVectorLayerAdapter.createGeometry( layer );

                // check geometry
                Assert.assertTrue( gIn.equals( gOut ) );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the IVectorAdapter with features.
     * 
     */
    @Test
    public void testFeatures() {
        try {

            LinkedList<Feature> fs = readFeatures();

            for ( Feature f : fs ) {

                IVectorLayer layer = IVectorLayerAdapter.createVectorLayer( f );
                Feature fOut = IVectorLayerAdapter.createFeature( layer );

                if ( f instanceof FeatureCollection ) {

                    FeatureCollection fcIn = (FeatureCollection) f;

                    if ( fcIn.size() > 1 ) {// more features

                        // check instance
                        Assert.assertTrue( fOut instanceof FeatureCollection );

                        // check feature collection
                        checkFeatureCollection( fcIn, (FeatureCollection) fOut );

                    } else {// one feature

                        // check instance
                        Assert.assertTrue( !( fOut instanceof FeatureCollection ) );

                        // check feature
                        Feature fModifyIn = fcIn.iterator().next();
                        checkFeature( fModifyIn, fOut );

                    }

                } else {

                    // check feature
                    checkFeature( f, fOut );
                }

            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the IVectorAdapter with feature collections.
     * 
     */
    @Test
    public void testFeatureCollections() {

        try {
            LinkedList<FeatureCollection> fcs = readFeatureCollections();
            for ( FeatureCollection fcIn : fcs ) {

                // create vector layer
                IVectorLayer layer = IVectorLayerAdapter.createVectorLayer( fcIn );

                // create feature collection
                FeatureCollection fcOut = IVectorLayerAdapter.createFeatureCollection( layer );

                checkFeatureCollection( fcIn, fcOut );

            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a feature collection.
     * 
     * @param filename
     *            - filename of a feature collection file in the resource dictionary.
     * @return feature collection
     * @throws Exception
     */
    private static FeatureCollection readFeatureCollection( String filename )
                            throws Exception {

        URL url = IVectorAdapterTest.class.getResource( filename );
        GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, url );
        FeatureCollection fc = gmlStreamReader.readFeatureCollection();
        return fc;
    }

    /**
     * Returns a list of feature collections with different geometry types.
     * 
     * @return - list of feature collections
     * @throws Exception
     */
    private static LinkedList<FeatureCollection> readFeatureCollections()
                            throws Exception {

        LinkedList<FeatureCollection> colls = new LinkedList<FeatureCollection>();
        colls.add( readFeatureCollection( "GML31_FeatureCollection_Deegree.xml" ) );
        colls.add( readFeatureCollection( "GML31_FeatureCollection_GeoServer.xml" ) );

        return colls;
    }

    /**
     * Returns a list of features with different geometry types.
     * 
     * @return list of features
     * @throws Exception
     */
    private static LinkedList<Feature> readFeatures()
                            throws Exception {

        LinkedList<Feature> fs = new LinkedList<Feature>();
        FeatureCollection fc1 = readFeatureCollection( "GML31_FeatureCollection_Deegree.xml" );
        FeatureCollection fc2 = readFeatureCollection( "GML31_FeatureCollection_GeoServer.xml" );
        fs.add( fc2 );

        Iterator<Feature> it = fc1.iterator();
        for ( Feature f : fc1 ) {
            fs.add( f );
        }

        return fs;
    }

    /**
     * Returns a geometry.
     * 
     * @param type
     *            - geometry type
     * @return geometry
     * @throws Exception
     */
    private static Geometry readGeometry( GeometryType type )
                            throws Exception {

        File geom = null;

        if ( type.equals( GeometryType.POINT ) )
            geom = new File( IVectorAdapterTest.class.getResource( "GML31_Point.xml" ).getPath() );
        else if ( type.equals( GeometryType.LINE_STRING ) )
            geom = new File( IVectorAdapterTest.class.getResource( "GML31_LineString.xml" ).getPath() );
        else if ( type.equals( GeometryType.POLYGON ) )
            geom = new File( IVectorAdapterTest.class.getResource( "GML31_Polygon.xml" ).getPath() );
        else if ( type.equals( GeometryType.MULTI_POINT ) )
            geom = new File( IVectorAdapterTest.class.getResource( "GML31_MultiPoint.xml" ).getPath() );
        else if ( type.equals( GeometryType.MULTI_LINE_STRING ) )
            geom = new File( IVectorAdapterTest.class.getResource( "GML31_MultiLineString.xml" ).getPath() );
        else if ( type.equals( GeometryType.MULTI_POLYGON ) )
            geom = new File( IVectorAdapterTest.class.getResource( "GML31_MultiPolygon.xml" ).getPath() );

        GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31,
                                                                                 geom.toURI().toURL() );

        Geometry g = gmlStreamReader.readGeometry();

        return g;
    }

    /**
     * Returns a list of geometries with different types.
     * 
     * @return list of geometries
     * @throws Exception
     */
    private static LinkedList<Geometry> readGeometries()
                            throws Exception {

        LinkedList<Geometry> geoms = new LinkedList<Geometry>();
        geoms.add( readGeometry( GeometryType.POINT ) );
        geoms.add( readGeometry( GeometryType.LINE_STRING ) );
        geoms.add( readGeometry( GeometryType.POLYGON ) );
        geoms.add( readGeometry( GeometryType.MULTI_POINT ) );
        geoms.add( readGeometry( GeometryType.MULTI_LINE_STRING ) );
        geoms.add( readGeometry( GeometryType.MULTI_POLYGON ) );

        return geoms;
    }

    /**
     * Checks two features of equality properties.
     * 
     * @param fIn
     *            - input feature
     * @param fOut
     *            - ouput feature
     */
    private void checkFeature( Feature fIn, Feature fOut ) {

        // collect simple properties
        LinkedList<SimplePropertyType> sptsIn = new LinkedList<SimplePropertyType>();
        LinkedList<SimplePropertyType> sptsOut = new LinkedList<SimplePropertyType>();

        // properties (input)
        List<PropertyType> proDeclIn = fIn.getType().getPropertyDeclarations();
        Iterator<PropertyType> itInProDecl = proDeclIn.iterator();
        while ( itInProDecl.hasNext() ) {
            PropertyType ptIn = itInProDecl.next();
            // simple properties
            if ( ptIn instanceof SimplePropertyType ) {
                sptsIn.add( (SimplePropertyType) ptIn );
            }
        }

        // properties (output)
        List<PropertyType> proDeclOut = fOut.getType().getPropertyDeclarations();
        Iterator<PropertyType> itOutProDecl = proDeclOut.iterator();
        while ( itOutProDecl.hasNext() ) {
            PropertyType ptOut = itOutProDecl.next();
            // simple properties
            if ( ptOut instanceof SimplePropertyType ) {
                sptsOut.add( (SimplePropertyType) ptOut );
            }
        }

        // check simple property size
        Assert.assertTrue( sptsIn.size() == sptsOut.size() );

        for ( SimplePropertyType sptIn : sptsIn ) {
            boolean found = false;

            for ( SimplePropertyType sptOut : sptsOut ) {
                if ( sptIn.getName().equals( sptOut.getName() ) ) {
                    found = true;

                    // check value content
                    Assert.assertTrue( fIn.getProperties( sptIn.getName() )[0].getValue().toString().equals(
                                                                                                             fOut.getProperties( sptOut.getName() )[0].getValue().toString() ) );
                }
            }

            // check property was found
            Assert.assertTrue( found );
            found = false;

        }

        // check geometry content (only first geometry)
        Assert.assertTrue( fIn.getGeometryProperties()[0].getValue().toString().equals(
                                                                                        fOut.getGeometryProperties()[0].getValue().toString() ) );

    }

    /**
     * Checks two feature collections of equality properties.
     * 
     * @param fcIn
     *            - input feature collection
     * @param fcOut
     *            - ouput feature collection
     */
    private void checkFeatureCollection( FeatureCollection fcIn, FeatureCollection fcOut ) {

        // check size
        Assert.assertTrue( fcIn.size() == fcOut.size() );

        // check features
        Iterator<Feature> itIn = fcIn.iterator();
        Iterator<Feature> itOut = fcOut.iterator();
        while ( itIn.hasNext() && itOut.hasNext() ) {
            Feature fIn = itIn.next();
            Feature fOut = itOut.next();
            checkFeature( fIn, fOut );
        }
    }

}
