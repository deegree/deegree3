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
package org.deegree.services.wps.provider;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.PrecisionModel;

import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class IVectorLayerAdapter {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( IVectorLayerAdapter.class );

    private static FeatureCollection readFeatureCollection()
                            throws Exception {
        URL url = new URL(
                           "http://demo.deegree.org/deegree-wfs/services?service=WFS&version=1.1.0&request=GetFeature&typename=app:Country&namespace=xmlns%28app=http://www.deegree.org/app%29&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1" );
        GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, url );
        FeatureCollection fc = gmlStreamReader.readFeatureCollection();
        return fc;
    }

    @Test
    public void testToCreateVectorLayerFromFeatureCollection() {

        try {
            IVectorLayer layer = createVectorLayer( readFeatureCollection() );

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a IVectorLayer from a org.deegree.feature.FeatureCollection.
     * 
     * @param c
     *            feature collection
     * @return vector layer
     */
    public static IVectorLayer createVectorLayer( FeatureCollection c ) {

        Iterator<Feature> it = c.iterator();
        if ( it.hasNext() ) {

            // get feature type
            FeatureType fType = it.next().getType();

            // get property declarations
            LinkedList<Field> fSextantePropDecl = new LinkedList<Field>();
            List<PropertyType> fPropertyDeclarations = fType.getPropertyDeclarations();
            for ( PropertyType pt : fPropertyDeclarations ) {

                // handle only SimplePropertyType
                if ( pt instanceof SimplePropertyType ) {
                    SimplePropertyType spt = (SimplePropertyType) pt;

                    // name
                    String pName = spt.getName().toString();

                    // class
                    Class<?> pClass = spt.getPrimitiveType().getValueClass();

                    // notice name and class as field
                    fSextantePropDecl.add( new Field( pName, pClass ) );
                    LOG.info( "SimpleProperties: " + pName + "   " + pClass );
                }
            }

            // traverse all features
            for ( Feature f : c ) {

                if ( f.getType().equals( fType ) ) {// check feature type

                    LOG.info( "ID: " + f.getId() );

                    Property[] fGeometries = f.getGeometryProperties();

                    if ( fGeometries.length == 1 ) { // only one geometry

                    } else { // more geometries

                    }

                    // AbstractDefaultGeometry geom = (AbstractDefaultGeometry) dfd[0].getValue();
                    // FeatureImpl fSextante = new FeatureImpl( geom.getJTSGeometry(), null );

                    Property[] props = f.getProperties();
                    for ( int i = 0; i < props.length; i++ ) {
                        if ( props[i] instanceof SimplePropertyType ) {
                            
                            
                            
                            LOG.info( "       " + props[i].getName().getLocalPart() + "(" + props[i].getValue() + ")" );
                        }
                    }

                } else {
                    LOG.error( "Features have different feature types." );
                    // TODO throw Exception
                }

            }
        }

        VectorLayerImpl layer = new VectorLayerImpl( "FeatureCollectionLayer", null, null );

        return null;
    }

    /**
     * Creates a IVectorLayer from a org.deegree.feature.Feature.
     * 
     * @param f
     *            - feature
     * @return vector layer
     */
    public static IVectorLayer createVectorLayer( Feature f ) {
        return null;
    }

    /**
     * Creates a IVectorLayer from a org.deegree.geometry.Geometry.
     * 
     * @param g
     *            - geometry
     * @return vector layer
     */
    public static IVectorLayer createVectorLayer( Geometry g ) {

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "GeometryLayer", g.getCoordinateSystem().getName() );

        // create jts geometry
        AbstractDefaultGeometry gAbst = (AbstractDefaultGeometry) g;
        com.vividsolutions.jts.geom.Geometry gJTS = gAbst.getJTSGeometry();

        // add geometry to layer
        layer.addFeature( gJTS, null );

        return layer;
    }

    /**
     * Creates a org.deegree.feature.FeatureCollection from a vector layer.
     * 
     * @param l
     *            - vector layer
     * @return feature collection
     */
    public static FeatureCollection createFeatureCollection( IVectorLayer l ) {
        return null;
    }

    /**
     * Creates a org.deegree.feature.Feature from a vector layer.
     * 
     * @param l
     *            - vector layer
     * @return feature
     */
    public static Feature createFeature( IVectorLayer l ) {
        return null;
    }

    /**
     * Creates a org.deegree.geometry.Geometry from a vector layer.
     * 
     * @param l
     *            - vector layer
     * @return geometry
     * @throws IteratorException
     */
    public static Geometry createGeometry( IVectorLayer l )
                            throws IteratorException {

        // default deegree geometry to create a deegree geometry from JTS geometry
        GeometryFactory gFactory = new GeometryFactory();
        AbstractDefaultGeometry gDefault = (AbstractDefaultGeometry) gFactory.createPoint(
                                                                                           "GeometryFromIVectorLayerAdapter",
                                                                                           0, 0, CRS.EPSG_4326 );
        // JTS geometry
        com.vividsolutions.jts.geom.Geometry gJTS = null;

        IFeatureIterator it = l.iterator();

        if ( l.getShapesCount() == 1 ) { // only one shape in layer
            gJTS = it.next().getGeometry();

        } else { // more shapes in layer

            com.vividsolutions.jts.geom.GeometryFactory gFactoryJTS = new com.vividsolutions.jts.geom.GeometryFactory(
                                                                                                                       new PrecisionModel() );

            // create a JTS geometry array
            com.vividsolutions.jts.geom.Geometry[] gArrayJTS = new com.vividsolutions.jts.geom.Geometry[l.getShapesCount()];
            int k = 0;
            while ( it.hasNext() ) {
                gArrayJTS[k] = it.next().getGeometry();
                k++;
            }

            // create a JTS geometry collection
            gJTS = gFactoryJTS.createGeometryCollection( gArrayJTS );
        }

        // create a deegree geometry
        Geometry g = gDefault.createFromJTS( gJTS );

        return g;
    }

}
