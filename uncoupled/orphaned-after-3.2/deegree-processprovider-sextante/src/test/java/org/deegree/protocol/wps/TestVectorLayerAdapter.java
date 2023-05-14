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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.services.wps.provider.sextante.VectorLayerAdapter;
import org.deegree.services.wps.provider.sextante.VectorLayerImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.unex.sextante.dataObjects.IVectorLayer;

/**
 * This class tests the functionality of the {@link VectorLayerAdapter}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class TestVectorLayerAdapter {

    private static Logger LOG = LoggerFactory.getLogger( TestVectorLayerAdapter.class );

    // enabled/disabled all tests
    private static final boolean ENABLED = false;

    /**
     * Tests the {@link VectorLayerAdapter} to convert geometries to {@link VectorLayerImpl} and back. <br>
     * Compares the input geometry with the output geometry.
     */
    @Test
    public void testGeometries() {
        if ( ENABLED ) {

            try {

                // read geometries
                LinkedList<Geometry> geoms = readGeometries();

                // traverse all geometries
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
    }

    /**
     * Tests the {@link VectorLayerAdapter} to convert features to {@link VectorLayerImpl} and back. <br>
     * Compares the input geometry with the output geometry and the input properties with the output properties.
     */
    @Test
    public void testFeatures() {
        if ( ENABLED ) {
            try {

                // read features
                LinkedList<Feature> fs = readFeatures();

                // traverse all features
                for ( Feature f : fs ) {

                    IVectorLayer layer = VectorLayerAdapter.createVectorLayer( f );
                    Feature fOut = VectorLayerAdapter.createFeature( layer );

                    // if feature is a feature collection
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

                    } else { // if feature is a feature

                        // check feature
                        checkFeature( f, fOut );
                    }

                }

            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                Assert.fail( t.getLocalizedMessage() );
            }
        }
    }

    /**
     * Tests the {@link VectorLayerAdapter} to convert feature collections to {@link VectorLayerImpl} and back. <br>
     * Compares the input geometry with the output geometry and the input properties with the output properties of every
     * feature.
     */
    @Test
    public void testFeatureCollections() {
        if ( ENABLED ) {
            try {
                // read feature collections
                LinkedList<FeatureCollection> fcs = readFeatureCollections();

                // traverse all feature collections
                for ( FeatureCollection fcIn : fcs ) {

                    // create vector layer
                    IVectorLayer layer = VectorLayerAdapter.createVectorLayer( fcIn );

                    // create feature collection
                    FeatureCollection fcOut = VectorLayerAdapter.createFeatureCollection( layer );

                    // check feature collection
                    checkFeatureCollection( fcIn, fcOut );

                }
            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                Assert.fail( t.getLocalizedMessage() );
            }
        }
    }

    /**
     * Returns a list of feature collections with different geometry types.
     * 
     * @return List of feature collections.
     * @throws Exception
     */
    private static LinkedList<FeatureCollection> readFeatureCollections()
                            throws Exception {

        LinkedList<FeatureCollection> colls = new LinkedList<FeatureCollection>();

        LinkedList<VectorExampleData> data = VectorExampleData.getAllFeatureCollections();

        for ( VectorExampleData dataFc : data ) {
            // read file
            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( dataFc.getGMLVersion(),
                                                                                     dataFc.getURL() );
            FeatureCollection fc = gmlStreamReader.readFeatureCollection();

            // notice collection
            colls.add( fc );
        }

        return colls;
    }

    /**
     * Returns a list of features with different geometry types.
     * 
     * @return List of features.
     * @throws Exception
     */
    private static LinkedList<Feature> readFeatures()
                            throws Exception {

        LinkedList<Feature> features = new LinkedList<Feature>();

        LinkedList<VectorExampleData> data = VectorExampleData.getAllFeatureCollections();
        for ( VectorExampleData dataFc : data ) {

            // read file
            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( dataFc.getGMLVersion(),
                                                                                     dataFc.getURL() );
            FeatureCollection fc = gmlStreamReader.readFeatureCollection();

            // notice features
            for ( Feature feature : fc ) {
                features.add( feature );
            }
        }

        return features;
    }

    /**
     * Returns a list of geometries with different types.
     * 
     * @return List of geometries.
     * @throws Exception
     */
    private static LinkedList<Geometry> readGeometries()
                            throws Exception {

        LinkedList<Geometry> geoms = new LinkedList<Geometry>();

        LinkedList<VectorExampleData> data = VectorExampleData.getAllGeometries();
        for ( VectorExampleData dataGeom : data ) {
            // read file
            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( dataGeom.getGMLVersion(),
                                                                                     dataGeom.getURL() );
            Geometry g = gmlStreamReader.readGeometry();

            // notice geometry
            geoms.add( g );
        }

        return geoms;
    }

    /**
     * Checks two features of the same properties and geometry.
     * 
     * @param fIn
     *            Input feature.
     * @param fOut
     *            Ouput feature.
     */
    private void checkFeature( Feature fIn, Feature fOut ) {

        // check only if geometries available
        if ( !fIn.getGeometryProperties().isEmpty() && !fOut.getGeometryProperties().isEmpty() ) {

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

                        // check property name, namespace and prefix
                        Assert.assertTrue( sptIn.getName().equals( sptOut.getName() ) );

                        // check value content (only the first property with the same name)
                        List<Property> valuesIn = fIn.getProperties( sptIn.getName() );
                        List<Property> valuesOut = fOut.getProperties( sptOut.getName() );

                        if ( valuesIn.size() > 0 ) { // check if input value available
                            TypedObjectNode valueIn = valuesIn.get( 0 ).getValue();

                            if ( valuesOut.size() > 0 ) {// check if output value available
                                TypedObjectNode valueOut = valuesOut.get( 0 ).getValue();

                                // check values
                                if ( valueIn instanceof PrimitiveValue && valueOut instanceof PrimitiveValue ) {
                                    Assert.assertTrue( ( (PrimitiveValue) valueIn ).getAsText().equals( ( (PrimitiveValue) valueIn ).getAsText() ) );
                                } else {
                                    Assert.fail();
                                }
                            }
                        } else { // no input value, check output value
                            if ( valuesOut.size() > 0 ) {// check if output value available
                                TypedObjectNode valueOut = valuesOut.get( 0 ).getValue();
                                if ( valueOut instanceof PrimitiveValue ) {
                                    String val = ( (PrimitiveValue) valueOut ).getAsText();
                                    Assert.assertTrue( val.equals( "0" ) || val.equals( "0.0" ) || val.equals( "null" )
                                                       || val.equals( "" ) );
                                } else {
                                    Assert.fail();
                                }
                            }
                        }

                    }
                }

                // check property was found
                Assert.assertTrue( found );
                found = false;

            }

            // check geometry content (only first geometry)
            TypedObjectNode geomIn = fIn.getGeometryProperties().get( 0 ).getValue();
            TypedObjectNode geomOut = fOut.getGeometryProperties().get( 0 ).getValue();
            Assert.assertTrue( geomIn.toString().equals( geomOut.toString() ) );
        }
    }

    /**
     * Checks two feature collections of number of features and the same properties and geometry of every feature.
     * 
     * @param fcIn
     *            - Input feature collection.
     * @param fcOut
     *            - Ouput feature collection.
     */
    private void checkFeatureCollection( FeatureCollection fcIn, FeatureCollection fcOut ) {

        // check features
        Iterator<Feature> itIn = fcIn.iterator();
        Iterator<Feature> itOut = fcOut.iterator();
        while ( itIn.hasNext() && itOut.hasNext() ) {
            Feature fIn = getNextFeatureWithGeometry( itIn ); // skip features without geometry
            Feature fOut = itOut.next();
            checkFeature( fIn, fOut );
        }

    }

    /**
     * The method returns the next {@link Feature} which contains a {@link Geometry}. {@link Feature}s without
     * {@link Geometry} would skipped.
     * 
     * @param it
     *            - {@link Iterator} for iterate over features.
     * @return {@link Feature} with geometry
     */
    private Feature getNextFeatureWithGeometry( Iterator<Feature> it ) {
        Feature f = it.next();
        if ( !f.getGeometryProperties().isEmpty() )
            return f;
        else
            return getNextFeatureWithGeometry( it );
    }
}
