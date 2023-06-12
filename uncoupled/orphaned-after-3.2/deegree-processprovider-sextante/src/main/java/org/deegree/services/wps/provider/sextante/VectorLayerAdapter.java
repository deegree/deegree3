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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.ValueRepresentation;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.PrecisionModel;

import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

/**
 * The {@link VectorLayerAdapter} has methods to create a {@link IVectorLayer} from a {@link Geometry}, <br>
 * {@link Feature} or {@link FeatureCollection} and methods to create a {@link Geometry}, {@link Feature} or
 * {@link FeatureCollection} from a {@link IVectorLayer} .
 * 
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class VectorLayerAdapter {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( VectorLayerAdapter.class );

    public static final String APP_NS = "http://www.deegree.org/sextante";

    public static final String APP_PREFIX = "st";

    /**
     * Creates an {@link IVectorLayer} from a {@link FeatureCollection}.
     * 
     * @param c
     *            The {@link FeatureCollection} must contain simple {@link Feature}s, this means that a {@link Feature}
     *            has only one geometry and only properties with different names. If a {@link Feature} has more
     *            geometries, it will be used the first. If a {@link Feature} has more properties with the same name, it
     *            will be used the first. Some cases can't handled, if the {@link Feature}s contains other
     *            {@link Feature}s. A {@link Feature} without geometry will be skipped.
     * 
     * @return An {@link IVectorLayer} with {@link IFeature}s. The {@link IFeature}s contains only one
     *         {@link com.vividsolutions.jts.geom.Geometry} and only properties with different names.
     */
    public static VectorLayerImpl createVectorLayer( FeatureCollection c ) {

        // parameters for vector layer
        LinkedList<IFeature> features = new LinkedList<IFeature>();
        Field[] vectorLayerPropertyDeclarations = null;
        String crs = null;

        Iterator<Feature> it = c.iterator();
        if ( it.hasNext() ) {

            // get feature type
            Feature firstFeature = it.next();

            // get crs
            crs = determineCRS( firstFeature );

            // get property declarations
            vectorLayerPropertyDeclarations = determinePropertyDeclarationsForVectorLayer( c );

            // traverse all features
            for ( Feature feature : c ) {

                // create properties
                Object[] values = determinePropertiesForVectorLayerGeometry( feature, vectorLayerPropertyDeclarations );

                // create geometry
                com.vividsolutions.jts.geom.Geometry geom = createJTSGeometryFromFeature( feature );

                if ( geom != null ) {
                    // add feature
                    features.add( new FeatureImpl( geom, values ) );
                } else {
                    LOG.warn( "Feature '" + feature.getId() + "' was skipped." );
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
     *            properties with different names. If a {@link Feature} has more geometries, it will be used the first.
     *            If a {@link Feature} has more properties with the same name, it will be used the first. A case can't
     *            handled, if the {@link Feature} contains other {@link Feature}s. If the {@link Feature} is a
     *            {@link FeatureCollection}, it can handled. A {@link Feature} without geometry will be skipped.
     * 
     * @return An {@link IVectorLayer} with a {@link IFeature}. The {@link IFeature} contains only one
     *         {@link com.vividsolutions.jts.geom.Geometry} and only properties with different names.
     */
    public static VectorLayerImpl createVectorLayer( Feature f ) {

        if ( f instanceof FeatureCollection ) {
            return createVectorLayer( (FeatureCollection) f );
        } else {
            // get crs
            String crs = determineCRS( f );

            // create property declarations
            Field[] propertiyDeclarations = determinePropertyDeclarationsForVectorLayer( f );

            // create properties
            Object[] properties = determinePropertiesForVectorLayerGeometry( f, propertiyDeclarations );

            // create geometry
            com.vividsolutions.jts.geom.Geometry geom = createJTSGeometryFromFeature( f );

            // create vector layer
            VectorLayerImpl layer = new VectorLayerImpl( "FeatureLayer", crs, propertiyDeclarations );

            if ( geom != null ) {
                layer.addFeature( new FeatureImpl( geom, properties ) );
            } else {
                LOG.warn( "Feature '" + f.getId() + "' was skipped." );
            }

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
    public static VectorLayerImpl createVectorLayer( Geometry g ) {

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "GeometryLayer", g.getCoordinateSystem().getAlias() );

        // add geometry to layer
        layer.addFeature( createJTSGeometryFromGeometry( g ), null );

        return layer;
    }

    /**
     * Creates a {@link FeatureCollection} from an {@link IVectorLayer}.
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
    public static GenericFeatureCollection createFeatureCollection( IVectorLayer l )
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

            // remove multigeometries with one geometry
            if ( gJTS.getNumGeometries() == 1 )
                gJTS = gJTS.getGeometryN( 0 );

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
        Geometry g = createGeometryFromJTSGeometry( gJTS, l.getCRS().toString() );

        return g;
    }

    /**
     * Creates {@link com.vividsolutions.jts.geom.Geometry} from {@link Feature}.
     * 
     * @param f
     *            {@link Feature}
     * @return Returns a {@link com.vividsolutions.jts.geom.Geometry}. If the {@link Feature} contains more than one
     *         geometry property, they will be merged to a MultiGeometry.
     */
    private static com.vividsolutions.jts.geom.Geometry createJTSGeometryFromFeature( Feature f ) {

        List<Property> fGeometries = f.getGeometryProperties();
        com.vividsolutions.jts.geom.Geometry geom;

        if ( fGeometries.size() == 1 ) { // only one geometry
            geom = createJTSGeometryFromGeometry( (Geometry) fGeometries.get( 0 ).getValue() );
        } else { // more geometries

            if ( fGeometries.size() != 0 ) { // feature with more than one geometry
                geom = createJTSGeometryFromGeometry( (Geometry) fGeometries.get( 0 ).getValue() );

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

            } else { // feature without geometry
                LOG.warn( "Feature '" + f.getId() + "' has no geometries." );
                geom = null;
            }

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
    private static com.vividsolutions.jts.geom.Geometry createJTSGeometryFromGeometry( Geometry g ) {

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
    private static Geometry createGeometryFromJTSGeometry( com.vividsolutions.jts.geom.Geometry gJTS, String crsName ) {

        // default deegree geometry to create a deegree geometry from JTS geometry
        GeometryFactory gFactory = new GeometryFactory();
        AbstractDefaultGeometry gDefault = (AbstractDefaultGeometry) gFactory.createPoint( null,
                                                                                           0,
                                                                                           0,
                                                                                           CRSManager.getCRSRef( crsName ) );

        Geometry g = gDefault.createFromJTS( gJTS, CRSManager.getCRSRef( crsName ) );

        return g;
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
    private static GenericFeature createFeature( IFeature f, String id, IVectorLayer l )
                            throws IllegalArgumentException, InstantiationException, IllegalAccessException {

        // feature
        GenericFeature feature;

        // create property declarations
        LinkedList<PropertyType> propDecls = new LinkedList<PropertyType>();
        Object[] propObjs = f.getRecord().getValues();

        // create simple properties types
        for ( int i = 0; i < l.getFieldCount(); i++ ) {

            // determine element name
            QName probName;
            if ( l instanceof VectorLayerImpl ) {
                Field field = ( (VectorLayerImpl) l ).getField( i );
                probName = field.getQName();
            } else {
                probName = new QName( APP_NS, l.getFieldName( i ), APP_PREFIX );
            }

            // modify value
            Object value = propObjs[i];
            if ( value != null ) { // value is not null
                value = modifyPropertyValue( value );
            } else {// value is null
                value = determinePropertyNullValue( l.getFieldType( i ) );
            }

            // create property type
            SimplePropertyType spt = new SimplePropertyType( probName, 1, 1, BaseType.valueOf( value ), null,
                                                             new LinkedList<PropertyType>() );

            propDecls.add( spt );
        }

        // create simple geometry
        GeometryPropertyType gpt = new GeometryPropertyType( new QName( APP_NS, "the_geom", APP_PREFIX ), 1, 1, null,
                                                             new LinkedList<PropertyType>(),
                                                             GeometryType.MULTI_GEOMETRY, CoordinateDimension.DIM_2,
                                                             ValueRepresentation.INLINE );
        propDecls.add( gpt );

        // creatre feature type
        GenericFeatureType fty = new GenericFeatureType( new QName( APP_NS, "SextanteFeature", APP_PREFIX ), propDecls,
                                                         false );

        // create properties
        LinkedList<Property> props = new LinkedList<Property>();
        Iterator<PropertyType> it = propDecls.iterator();
        for ( int i = 0; i < propObjs.length; i++ ) {

            // create simple properties
            if ( it.hasNext() ) {

                // modify value
                Object value = propObjs[i];
                if ( value != null ) { // value is not null
                    value = modifyPropertyValue( value );
                } else {// value is null
                    value = determinePropertyNullValue( l.getFieldType( i ) );
                }

                // GenericProperty gp = new GenericProperty( it.next(), new PrimitiveValue( propObjs[i] ) );
                SimpleProperty sp = new SimpleProperty( (SimplePropertyType) it.next(), value.toString() );

                props.add( sp );
            }
        }

        // create geometric properties
        if ( it.hasNext() ) {
            Geometry geom = createGeometryFromJTSGeometry( f.getGeometry(), l.getCRS().toString() );

            if ( geom != null ) {
                GenericProperty gp = new GenericProperty( it.next(), geom );
                props.add( gp );

            }
        }

        // create feature
        feature = new GenericFeature( fty, id, props, null );

        return feature;
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
        List<Property> geoms = f.getGeometryProperties();
        if ( !geoms.isEmpty() ) {
            Geometry g = (Geometry) geoms.get( 0 ).getValue();
            crs = g.getCoordinateSystem().getAlias();
        }

        return crs;
    }

    /**
     * Creates a property declaration of a {@link IVectorLayer}.
     * 
     * @param f
     *            {@link Feature}
     * @return The property declaration of a {@link IVectorLayer} as a {@link Field} array.
     */
    private static Field[] determinePropertyDeclarationsForVectorLayer( Feature f ) {

        // feature type
        FeatureType type = f.getType();

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
                Class<?> pClass = spt.getPrimitiveType().getBaseType().getValueClass();
                // Class<?> pClass = String.class;

                // notice name and class as field
                vectoLayerPropertyDeclarations.add( new Field( pName, pClass ) );

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
     * Creates a property declaration of a {@link IVectorLayer}.
     * 
     * @param c
     *            {@link FeatureCollection}
     * @return The property declaration of a {@link IVectorLayer} as a {@link Field} array.
     */
    private static Field[] determinePropertyDeclarationsForVectorLayer( FeatureCollection c ) {

        HashMap<String, Field> properties = new HashMap<String, Field>();

        Iterator<Feature> it = c.iterator();
        while ( it.hasNext() ) {

            // get feature type
            Feature f = it.next();

            // if feature has geometries
            if ( !f.getGeometryProperties().isEmpty() ) {

                FeatureType fType = f.getType();

                // get property declarations
                List<PropertyType> propertyDeclarations = fType.getPropertyDeclarations();

                for ( PropertyType pt : propertyDeclarations ) {

                    // handle only SimplePropertyType
                    if ( pt instanceof SimplePropertyType ) {
                        SimplePropertyType spt = (SimplePropertyType) pt;

                        // name
                        QName pName = spt.getName();

                        // class
                        Class<?> pClass = spt.getPrimitiveType().getBaseType().getValueClass();
                        // Class<?> pClass = String.class;

                        // notice name and field
                        properties.put( pName.getLocalPart(), new Field( pName, pClass ) );
                    }
                }
            }
        }

        // create fields array
        Field[] vectoLayerPropertyDeclarations = new Field[properties.size()];
        Set<String> names = properties.keySet();
        int i = 0;
        for ( String name : names ) {
            vectoLayerPropertyDeclarations[i++] = properties.get( name );
        }

        return vectoLayerPropertyDeclarations;
    }

    /**
     * This method determines a property value with 0 by {@link Class} object. Only use this method if the property
     * value is null.
     * 
     * @param valueClass
     *            {@link Class} of property value.
     * @return Property value with 0.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Object determinePropertyNullValue( Class<?> valueClass )
                            throws InstantiationException, IllegalAccessException {

        Object value = null;

        if ( valueClass.equals( BigDecimal.class ) ) {
            value = new BigDecimal( 0 );
        } else {
            if ( valueClass.equals( BigInteger.class ) ) {
                value = new BigInteger( "0" );
            } else {
                value = valueClass.newInstance();
            }
        }

        return value;
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
    private static Object[] determinePropertiesForVectorLayerGeometry( Feature f, Field[] propertyDeclarations ) {

        // property values of a IFeature
        Object[] geomProperties;

        if ( propertyDeclarations != null ) {
            geomProperties = new Object[propertyDeclarations.length];

            for ( int i = 0; i < propertyDeclarations.length; i++ ) {

                // determine property value by name
                List<Property> allProperties = f.getProperties();
                LinkedList<Property> propertyByName = new LinkedList<Property>();
                for ( int j = 0; j < allProperties.size(); j++ ) {
                    Property prop = allProperties.get( j );
                    if ( prop.getName().equals( propertyDeclarations[i].getQName() ) )
                        propertyByName.add( prop );
                }

                // notice only the first
                if ( propertyByName.size() >= 1 ) {

                    Property prop = propertyByName.getFirst();

                    if ( propertyByName.size() > 1 ) {
                        LOG.warn( "Multiple occurrence of property {}, using only first one for IVectorLayer.",
                                  prop.getName().getLocalPart() );
                    }

                    TypedObjectNode probNode = prop.getValue();

                    if ( probNode instanceof PrimitiveValue ) {
                        geomProperties[i] = ( (PrimitiveValue) probNode ).getValue();
                        // geomProperties[i] = ( (PrimitiveValue) properties[0].getValue() ).getAsText();
                    } else {
                        LOG.error( "Property '" + prop.getName() + "' is not supported." );
                        geomProperties[i] = null;
                    }

                } else {
                    geomProperties[i] = null;
                }

            }

        } else { // if propertyDeclaration = null
            geomProperties = new Object[] {}; // no property values
        }

        return geomProperties;
    }

    /**
     * This method modifies a property value of a {@link IVectorLayer} if it is not compatible to {@link BaseType}.
     * 
     * @param value
     *            Property value of a {@link IVectorLayer}.
     * 
     * @return Modified property value. It's compatible to {@link BaseType}.
     */
    private static Object modifyPropertyValue( Object value ) {
        Object newValue = value;

        if ( value instanceof Integer )// PrimitiveType only support BigInteger
            newValue = new BigInteger( value.toString() );
        else if ( value instanceof Long )// PrimitiveType only support Double
            newValue = new Double( value.toString() );

        return newValue;
    }

}
