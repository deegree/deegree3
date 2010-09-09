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

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.provider.sextante.Field;
import org.deegree.services.wps.provider.sextante.VectorLayerAdapter;
import org.deegree.services.wps.provider.sextante.OutputFactoryExt;
import org.deegree.services.wps.provider.sextante.VectorLayerImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.vectorTools.centroids.CentroidsAlgorithm;

/**
 * Tests the functionality of the IVectorLayerAdapter class.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class VectorLayerAdapterTest {

    private static Logger LOG = LoggerFactory.getLogger( VectorLayerAdapterTest.class );

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
                IVectorLayer layer = VectorLayerAdapter.createVectorLayer( gIn );

                // create geometry
                Geometry gOut = VectorLayerAdapter.createGeometry( layer );

                // check geometry
                Assert.assertTrue( gIn.equals( gOut ) );
            }
        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
            Assert.fail( t.getLocalizedMessage() );
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

                IVectorLayer layer = VectorLayerAdapter.createVectorLayer( f );
                Feature fOut = VectorLayerAdapter.createFeature( layer );

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

        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
            Assert.fail( t.getLocalizedMessage() );
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
                IVectorLayer layer = VectorLayerAdapter.createVectorLayer( fcIn );

                // create feature collection
                FeatureCollection fcOut = VectorLayerAdapter.createFeatureCollection( layer );

                checkFeatureCollection( fcIn, fcOut );

            }
        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
            Assert.fail( t.getLocalizedMessage() );
        }
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

        LinkedList<ExampleData> data = ExampleData.getAllFeatureCollections();
        for ( ExampleData dataFc : data ) {
            LOG.info( dataFc.toString() );

            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( dataFc.getGMLVersion(),
                                                                                     dataFc.getURL() );

            FeatureCollection fc = gmlStreamReader.readFeatureCollection();
            colls.add( fc );
        }

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

        LinkedList<Feature> features = new LinkedList<Feature>();

        LinkedList<ExampleData> data = ExampleData.getAllFeatureCollections();
        for ( ExampleData dataFc : data ) {
            LOG.info( dataFc.toString() );

            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( dataFc.getGMLVersion(),
                                                                                     dataFc.getURL() );

            FeatureCollection fc = gmlStreamReader.readFeatureCollection();

            for ( Feature feature : fc ) {
                features.add( feature );
            }
        }

        return features;
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

        LinkedList<ExampleData> data = ExampleData.getAllGeometryies();
        for ( ExampleData dataGeom : data ) {
            LOG.info( dataGeom.toString() );

            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( dataGeom.getGMLVersion(),
                                                                                     dataGeom.getURL() );
            Geometry g = gmlStreamReader.readGeometry();
            geoms.add( g );
        }

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

                    // check value content (only the first property with the same name)
                    TypedObjectNode valueIn = fIn.getProperties( sptIn.getName() )[0].getValue();
                    TypedObjectNode valueOut = fOut.getProperties( sptOut.getName() )[0].getValue();
                    if ( valueIn instanceof PrimitiveValue && valueOut instanceof PrimitiveValue ) {
                        Assert.assertTrue( ( (PrimitiveValue) valueIn ).getAsText().equals(
                                                                                            ( (PrimitiveValue) valueIn ).getAsText() ) );
                    } else {
                        Assert.fail();
                    }

                }
            }

            // check property was found
            Assert.assertTrue( found );
            found = false;

        }

        // check geometry content (only first geometry)
        TypedObjectNode geomIn = fIn.getGeometryProperties()[0].getValue();
        TypedObjectNode geomOut = fOut.getGeometryProperties()[0].getValue();
        
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

    // @Test
    public void simpleAlgorithmExample() {
        try {
            // geometry
            GeometryFactory geomFactory = new GeometryFactory();
            Coordinate[] coords = new Coordinate[3];
            coords[0] = new Coordinate( 49, 50 );
            coords[1] = new Coordinate( 100, 100 );
            coords[2] = new Coordinate( 150, 149 );
            com.vividsolutions.jts.geom.Geometry geom = geomFactory.createMultiPoint( coords );

            // initialize SEXTANTE
            Sextante.initialize();

            // create vector layer with input data
            IVectorLayer inputLayer = new VectorLayerImpl();
            inputLayer.addFeature( geom, null );

            // create algorithm
            GeoAlgorithm alg = new CentroidsAlgorithm();

            // commit input data to algorithm
            ParametersSet inputParams = alg.getParameters();
            inputParams.getParameter( CentroidsAlgorithm.LAYER ).setParameterValue( inputLayer );

            // execute algorithm
            alg.execute( null, new OutputFactoryExt() );

            // create vector layer with output data
            OutputObjectsSet outputParams = alg.getOutputObjects();
            Output output = outputParams.getOutput( CentroidsAlgorithm.RESULT );
            IVectorLayer outputLayer = (IVectorLayer) output.getOutputObject();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
