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
package org.deegree.services.wps.provider.sextante;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.ValueRepresentation;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.PrecisionModel;

import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

/**
 * The {@link VectorLayerAdapter} has methods to create a {@link IVectorLayer} from a {@link Geometry}, <br> {@link Feature}
 * or {@link FeatureCollection} and methods to create a {@link Geometry}, {@link Feature} or {@link FeatureCollection}
 * from a {@link IVectorLayer} .
 * 
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class VectorLayerAdapter {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( VectorLayerAdapter.class );

    /**
     * Creates an {@link IVectorLayer} from a {@link FeatureCollection}.
     * 
     * @param c
     *            The {@link FeatureCollection} must contain simple {@link Feature}s, this means that a {@link Feature}
     *            has only one geometry and only properties with different names. If a {@link Feature} has more
     *            geometries, they would be merged. If a {@link Feature} has more properties with the same name, it will
     *            be used the first. Some cases can't handled, when the {@link Feature}s has different
     *            {@link FeatureType}s or contains other {@link Feature}s.
     * 
     * @return An {@link IVectorLayer} with {@link IFeature}s. The {@link IFeature}s contains only one
     *         {@link com.vividsolutions.jts.geom.Geometry} and only properties with different names.
     */
    public static IVectorLayer createVectorLayer( FeatureCollection c ) {

        // parameters for vector layer
        LinkedList<IFeature> features = new LinkedList<IFeature>();
        Field[] vectorLayerPropertyDeclarations = null;
        String crs = null;

        Iterator<Feature> it = c.iterator();
        if ( it.hasNext() ) {

            // get feature type
            Feature f = it.next();
            FeatureType fType = f.getType();

            // get crs
            crs = determineCRS( f );

            // get property declarations
            vectorLayerPropertyDeclarations = createPropertyDeclarationsForVectorLayer( fType );

            // traverse all features
            for ( Feature feature : c ) {

                if ( feature.getType().equals( fType ) ) {// check feature type

                    // LOG.info( "FEATURE: " + feature.getId() );

                    // create properties
                    Object[] values = createPropertiesForVectorLayerGeometry( feature, vectorLayerPropertyDeclarations );

                    // create geometry
                    com.vividsolutions.jts.geom.Geometry geom = createJTSGeometry( feature );

                    // add feature
                    features.add( new FeatureImpl( geom, values ) );

                } else {
                    LOG.error( "Feature with id '" + feature.getId()
                               + "' have an other feature type as the others (not supported)." );
                    // TODO throw Exception
                }

            }
        }

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "FeatureCollectionLayer", crs, vectorLayerPropertyDeclarations );

        // add features to the layer
        for ( IFeature f : features ) {
            layer.addFeature( f );
        }

        return layer;
    }

    /**
     * Creates an {@link IVectorLayer} from a {@link Feature}.
     * 
     * @param f
     *            The {@link Feature} must be simple, this means that a {@link Feature} has only one geometry and only
     *            properties with different names. If a {@link Feature} has more geometries, they would be merged. If a
     *            {@link Feature} has more properties with the same name, it will be used the first. A case can't
     *            handled, if the {@link Feature} contains other {@link Feature}s. If the {@link Feature} is a
     *            {@link FeatureCollection}, it can handled.
     * 
     * @return An {@link IVectorLayer} with a {@link IFeature}. The {@link IFeature} contains only one
     *         {@link com.vividsolutions.jts.geom.Geometry} and only properties with different names.
     */
    public static IVectorLayer createVectorLayer( Feature f ) {

        if ( f instanceof FeatureCollection ) {
            return createVectorLayer( (FeatureCollection) f );
        } else {
            // get crs
            String crs = determineCRS( f );

            // create property declarations
            Field[] propertiyDeclarations = createPropertyDeclarationsForVectorLayer( f.getType() );

            // create properties
            Object[] properties = createPropertiesForVectorLayerGeometry( f, propertiyDeclarations );

            // create geometry
            com.vividsolutions.jts.geom.Geometry geom = createJTSGeometry( f );

            // create vector layer
            VectorLayerImpl layer = new VectorLayerImpl( "FeatureLayer", crs, propertiyDeclarations );
            layer.addFeature( new FeatureImpl( geom, properties ) );

            return layer;
        }
    }

    /**
     * Creates an {@link IVectorLayer} from a {@link Geometry}.
     * 
     * @param g
     *            If the {@link Geometry} isn't simple, it will be linearized.
     * 
     * @return An {@link IVectorLayer} with a {@link IFeature}. The {@link IFeature} contains only one
     *         {@link com.vividsolutions.jts.geom.Geometry} and no properties.
     */
    public static IVectorLayer createVectorLayer( Geometry g ) {

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "GeometryLayer", g.getCoordinateSystem().getName() );

        // add geometry to layer
        layer.addFeature( createJTSGeometry( g ), null );

        return layer;
    }

    /**
     * Creates a {@link FeatureCollection} from an {@link IVectorLayer}.
     * 
     * TODO more details.
     * 
     * @param l
     *            Every {@link IVectorLayer}.
     * 
     * @return {@link FeatureCollection}
     * 
     * @throws IteratorException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static FeatureCollection createFeatureCollection( IVectorLayer l )
                            throws IteratorException, IllegalArgumentException, InstantiationException,
                            IllegalAccessException {

        // list of features
        LinkedList<Feature> features = new LinkedList<Feature>();

        IFeatureIterator it = l.iterator();

        int idCounter = 0;

        while ( it.hasNext() ) {
            features.add( createFeature( it.next(), "SextanteFeature" + ++idCounter, l ) );
        }

        // create feature collection
        GenericFeatureCollection coll = new GenericFeatureCollection( "SextanteFeatureCollection", features );

        return coll;
    }

    /**
     * Creates a {@link Feature} from an {@link IVectorLayer}.
     * 
     * TODO more details.
     * 
     * @param l
     *            {@link IVectorLayer}
     * 
     * @return feature
     * 
     * @throws IteratorException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Feature createFeature( IVectorLayer l )
                            throws IteratorException, IllegalArgumentException, InstantiationException,
                            IllegalAccessException {
        // feature
        Feature f;

        if ( l.getShapesCount() > 1 ) { // more features
            f = createFeatureCollection( l );

        } else {

            IFeatureIterator it = l.iterator();
            if ( it.hasNext() ) {// one feature
                f = createFeature( it.next(), "SextantFeature1", l );

            } else {// no feature
                f = null;
            }
        }

        return f;
    }

    /**
     * Creates a {@link Feature} from an {@link IFeature}.
     * 
     * @param f
     *            {@link IFeature}
     * @param id
     *            {@link IFeature} id
     * @param l
     *            {@link IVectorLayer}
     * 
     * @return {@link Feature}
     * 
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Feature createFeature( IFeature f, String id, IVectorLayer l )
                            throws IllegalArgumentException, InstantiationException, IllegalAccessException {

        // feature
        Feature feature;

        // create property declarations
        LinkedList<PropertyType> propDecls = new LinkedList<PropertyType>();
        Object[] propObjs = f.getRecord().getValues();
        // create simple properties
        for ( int i = 0; i < l.getFieldCount(); i++ ) {
            String[] name = l.getFieldName( i ).replace( "{", "" ).split( "}" );

            // determine element name
            QName probName;
            if ( name.length >= 2 )
                probName = new QName( name[1] );
            else
                probName = new QName( l.getFieldName( i ).replace( " ", "" ) );

            // modify value
            Object value = propObjs[i];
            if ( value instanceof Integer )// PrimitiveType only support BigInteger
                value = new BigInteger( value.toString() );

            // create property type
            SimplePropertyType spt = new SimplePropertyType( probName, 1, 1,
                                                             PrimitiveType.determinePrimitiveType( value ), false,
                                                             new LinkedList<PropertyType>() );

            propDecls.add( spt );
        }

        // create simple geometry
        GeometryPropertyType gpt = new GeometryPropertyType( new QName( "geom" ), 1, 1, GeometryType.MULTI_GEOMETRY,
                                                             CoordinateDimension.DIM_2, false,
                                                             new LinkedList<PropertyType>(), ValueRepresentation.INLINE );
        propDecls.add( gpt );

        // creatre feature type
        GenericFeatureType fty = new GenericFeatureType( new QName( "SextanteFeature" ), propDecls, false );

        // create properties
        LinkedList<Property> props = new LinkedList<Property>();
        Iterator<PropertyType> it = propDecls.iterator();
        for ( int i = 0; i < propObjs.length; i++ ) {
            if ( it.hasNext() ) {

                Object value = propObjs[i];
                if ( value instanceof Integer ) // PrimitiveType only support BigInteger
                    value = new BigInteger( value.toString() );

                // GenericProperty gp = new GenericProperty( it.next(), new PrimitiveValue( propObjs[i] ) );
                SimpleProperty sp = new SimpleProperty( (SimplePropertyType) it.next(), value.toString(),
                                                        PrimitiveType.determinePrimitiveType( value ) );

                props.add( sp );
            }
        }
        if ( it.hasNext() ) {

            Geometry geom = createGeometry( f.getGeometry() );

            if ( geom != null ) {
                GenericProperty gp = new GenericProperty( it.next(), geom );
                props.add( gp );

            }

        }

        // create feature
        feature = new GenericFeature( fty, id, props, GMLVersion.GML_31 );

        return feature;
    }

    /**
     * Creates a {@link Geometry} from an {@link IVectorLayer}.
     * 
     * @param l
     *            Every {@link IVectorLayer}.
     * 
     * @return Returns a {@link Geometry}. If the {@link IVectorLayer} contains more than one {@link IFeature}, they
     *         will be merged to a MultiGeometry.
     * 
     * @throws IteratorException
     */
    public static Geometry createGeometry( IVectorLayer l )
                            throws IteratorException {

        // JTS geometry
        com.vividsolutions.jts.geom.Geometry gJTS;

        IFeatureIterator it = l.iterator();

        if ( l.getShapesCount() == 1 ) { // only one shape in layer
            gJTS = it.next().getGeometry();

        } else { // more shapes in layer

            com.vividsolutions.jts.geom.GeometryFactory gFactoryJTS = new com.vividsolutions.jts.geom.GeometryFactory(
                                                                                                                       new PrecisionModel() );

            // create a JTS geometry array and skip emty geometries
            LinkedList<com.vividsolutions.jts.geom.Geometry> geomList = new LinkedList<com.vividsolutions.jts.geom.Geometry>();
            while ( it.hasNext() ) {
                com.vividsolutions.jts.geom.Geometry geom = it.next().getGeometry();
                if ( !geom.isEmpty() ) {
                    geomList.add( geom );
                }
            }

            // create a JTS geometry collection
            gJTS = gFactoryJTS.createGeometryCollection( geomList.toArray( new com.vividsolutions.jts.geom.Geometry[geomList.size()] ) );
        }

        // create a deegree geometry
        Geometry g = createGeometry( gJTS );

        return g;
    }

    /**
     * Creates a CRS name from a {@link Feature}.
     * 
     * @param f
     *            {@link Feature}
     * @return CRS name.
     */
    private static String determineCRS( Feature f ) {

        String crs = null;
        Property[] geoms = f.getGeometryProperties();
        if ( geoms.length > 0 ) {
            Geometry g = (Geometry) geoms[0].getValue();
            crs = g.getCoordinateSystem().getName();
        }

        return crs;
    }

    /**
     * Creates {@link com.vividsolutions.jts.geom.Geometry} from {@link Feature}.
     * 
     * @param f
     *            {@link Feature}
     * @return Returns a {@link com.vividsolutions.jts.geom.Geometry}. If the {@link Feature} contains more than one
     *         geometry property, they will be merged to a MultiGeometry.
     */
    private static com.vividsolutions.jts.geom.Geometry createJTSGeometry( Feature f ) {

        Property[] fGeometries = f.getGeometryProperties();
        com.vividsolutions.jts.geom.Geometry geom;

        if ( fGeometries.length == 1 ) { // only one geometry

            geom = createJTSGeometry( (Geometry) fGeometries[0].getValue() );

        } else { // more geometries

            geom = createJTSGeometry( (Geometry) fGeometries[0].getValue() );

            LOG.warn( "Feature '" + f.getId() + "' has many geometries, only the first is in use." );

            // merge all geometries
            // com.vividsolutions.jts.geom.GeometryFactory gFactoryJTS = new
            // com.vividsolutions.jts.geom.GeometryFactory(
            // new PrecisionModel() );
            //
            // // create jts geometry array
            // com.vividsolutions.jts.geom.Geometry[] geoms = new
            // com.vividsolutions.jts.geom.Geometry[fGeometries.length];
            // for ( int i = 0; i < fGeometries.length; i++ ) {
            // geoms[i] = createJTSGeometry( (Geometry) fGeometries[i].getValue() );
            // }
            //
            // // create a JTS geometry collection
            // geom = gFactoryJTS.createGeometryCollection( geoms );

        }

        return geom;
    }

    /**
     * Creates {@link com.vividsolutions.jts.geom.Geometry} from {@link Geometry} .
     * 
     * @param g
     *            {@link Geometry}
     * @return Returns a {@link com.vividsolutions.jts.geom.Geometry}. If the {@link Geometry} isn't simple, it will be
     *         linearized.
     */
    private static com.vividsolutions.jts.geom.Geometry createJTSGeometry( Geometry g ) {

        // create jts geometry
        AbstractDefaultGeometry gAbst = (AbstractDefaultGeometry) g;
        com.vividsolutions.jts.geom.Geometry gJTS = gAbst.getJTSGeometry();

        return gJTS;
    }

    /**
     * Creates {@link Geometry} from {@link com.vividsolutions.jts.geom.Geometry} .
     * 
     * @param gJTS
     *            {@link com.vividsolutions.jts.geom.Geometry}
     * @return {@link Geometry} or <code>null</code> if the given geometry is an empty collection.
     */
    private static Geometry createGeometry( com.vividsolutions.jts.geom.Geometry gJTS ) {

        // default deegree geometry to create a deegree geometry from JTS geometry
        GeometryFactory gFactory = new GeometryFactory();
        AbstractDefaultGeometry gDefault = (AbstractDefaultGeometry) gFactory.createPoint( null, 0, 0, CRS.EPSG_4326 );

        Geometry g = gDefault.createFromJTS( gJTS );

        return g;
    }

    /**
     * Creates a property declaration of a {@link IVectorLayer}.
     * 
     * @param type
     *            - {@link FeatureType}
     * @return The property declaration of a {@link IVectorLayer} as a {@link Field} array.
     */
    private static Field[] createPropertyDeclarationsForVectorLayer( FeatureType type ) {

        // LOG.info( "FEATURE TYP: " + type.getName().toString() );

        // list of property declaration for vector layer
        LinkedList<Field> vectoLayerPropertyDeclarations = new LinkedList<Field>();

        // get property declarations
        List<PropertyType> propertyDeclarations = type.getPropertyDeclarations();

        for ( PropertyType pt : propertyDeclarations ) {

            // handle only SimplePropertyType
            if ( pt instanceof SimplePropertyType ) {
                SimplePropertyType spt = (SimplePropertyType) pt;

                // name
                QName pName = spt.getName();

                // class
                // Class<?> pClass = spt.getPrimitiveType().getValueClass();
                Class<?> pClass = String.class;

                // notice name and class as field
                vectoLayerPropertyDeclarations.add( new Field( pName.toString(), pClass ) );

                // LOG.info( "  PROPERTY: " + pName + ":   " + pClass );
            }
        }

        // create property declaration array
        int i = 0;
        Field[] fields = new Field[vectoLayerPropertyDeclarations.size()];
        for ( Field field : vectoLayerPropertyDeclarations ) {
            fields[i] = field;
            i++;
        }

        return fields;
    }

    /**
     * Creates properties for a {@link com.vividsolutions.jts.geom.Geometry} of the {@link IVectorLayer}. <br>
     * If a {@link Feature} has more properties with the same name, it will be used the first.
     * 
     * @param f
     *            {@link Feature}
     * 
     * @param propertyDeclarations
     *            Property declaration as {@link Field} array.
     * 
     * @return {@link IFeature} properties as object array.
     */
    private static Object[] createPropertiesForVectorLayerGeometry( Feature f, Field[] propertyDeclarations ) {

        Object[] geomProperties;

        if ( propertyDeclarations != null ) {
            geomProperties = new Object[propertyDeclarations.length];

            for ( int i = 0; i < propertyDeclarations.length; i++ ) {

                String[] name = propertyDeclarations[i].getName().replace( "{", "" ).split( "}" );

                if ( name.length >= 2 ) {

                    QName probName = new QName( name[0], name[1] );

                    // propterties by name
                    Property[] properties = f.getProperties( probName );

                    // notice only the first
                    if ( properties.length >= 1 ) {

                        if ( properties.length > 1 ) {
                            LOG.warn( "Multiple occurrence of property {}, using only first one for IVectorLayer.",
                                      probName );
                        }

                        TypedObjectNode probNode = properties[0].getValue();

                        if ( probNode instanceof PrimitiveValue ) {
                            // geomProperties[i] = ( (PrimitiveValue) properties[0].getValue() );
                            geomProperties[i] = ( (PrimitiveValue) properties[0].getValue() ).getAsText();
                        } else {
                            LOG.warn( "Property '" + properties[0].getName() + "' is not supported." );
                            geomProperties[i] = null;
                        }

                    } else {
                        geomProperties[i] = null;
                    }

                    // LOG.info( "  PROPERTY: " + propertyDeclarations[i].getName() + ": " + geomProperties[i] );
                }
            }

        } else { // if names=null
            geomProperties = new Object[] {};
        }

        return geomProperties;
    }
}
