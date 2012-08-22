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
package org.deegree.model.feature;

import static org.deegree.framework.util.CollectionUtils.map;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.util.CollectionUtils.Mapper;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.MultiGeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfacePatch;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebServiceException;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 * Validator for feature instance (that have been constructed without schema information).
 * <p>
 * Validated features are assigned their respective feature types after successful validation.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Validator {

    private static final ILogger LOG = LoggerFactory.getLogger( Validator.class );

    private Set<Feature> inValidation = new HashSet<Feature>();

    private Map<QualifiedName, FeatureType> ftMap;

    /**
     * Constructs a new instance of <code>Validator</code> that will use the given map to lookup feature types by their
     * names.
     *
     * @param ftMap
     */
    public Validator( Map<QualifiedName, FeatureType> ftMap ) {
        this.ftMap = ftMap;
    }

    /**
     * Validates the given feature instance (and its subfeatures).
     * <p>
     * The feature instance is then assigned the corresponding {@link MappedFeatureType}. This also applies to its
     * subfeatures.
     *
     * @param feature
     *            feature instance to be validated
     * @throws OGCWebServiceException
     */
    public void validate( Feature feature )
                            throws OGCWebServiceException {

        if ( inValidation.contains( feature ) ) {
            return;
        }
        inValidation.add( feature );

        QualifiedName ftName = feature.getName();
        FeatureType ft = this.ftMap.get( ftName );
        if ( ft == null ) {
            String msg = Messages.format( "ERROR_FT_UNKNOWN", feature.getId(), ftName );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }

        int idx = 0;
        FeatureProperty[] properties = feature.getProperties();

        // remove GML properties if they are not defined for the feature type
        Set<QualifiedName> deleteGMLProps = new HashSet<QualifiedName>();
        for ( FeatureProperty featureProperty : properties ) {
            QualifiedName propName = featureProperty.getName();
            // GML namespace property?
            if ( CommonNamespaces.GMLNS.equals( propName.getNamespace() ) ) {
                // not defined in the feature type
                if ( ft.getProperty( propName ) == null ) {

                    deleteGMLProps.add( propName );
                }
            }
        }
        for ( QualifiedName propName : deleteGMLProps ) {
            LOG.logDebug( "Removing property '" + propName + "'." );
            feature.removeProperty( propName );
        }
        if ( deleteGMLProps.size() > 0 ) {
            properties = feature.getProperties();
        }

        PropertyType[] propertyTypes = ft.getProperties();

        if ( LOG.isDebug() ) {
            LOG.logDebug( "Validating properties", map( properties, new Mapper<QualifiedName, FeatureProperty>() {
                public QualifiedName apply( FeatureProperty u ) {
                    return u.getName();
                }

            } ) );
            LOG.logDebug( "Have property types", map( propertyTypes, new Mapper<QualifiedName, PropertyType>() {
                public QualifiedName apply( PropertyType u ) {
                    return u.getName();
                }
            } ) );
        }

        for ( int i = 0; i < propertyTypes.length; i++ ) {
            idx += validateProperties( feature, propertyTypes[i], properties, idx );
        }
        if ( idx != properties.length ) {
            String msg = Messages.format( "ERROR_FT_INVALID1", feature.getId(), ftName, properties[idx].getName() );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }

        feature.setFeatureType( ft );
    }

    /**
     * Validates that there is the correct amount of properties with the expected type in the given array of properties.
     *
     * @param feature
     * @param propertyType
     * @param properties
     * @param idx
     * @throws OGCWebServiceException
     */
    private int validateProperties( Feature feature, PropertyType propertyType, FeatureProperty[] properties, int idx )
                            throws OGCWebServiceException {
        int minOccurs = propertyType.getMinOccurs();
        int maxOccurs = propertyType.getMaxOccurs();
        QualifiedName propertyName = propertyType.getName();
        int count = 0;

        while ( idx + count < properties.length ) {
            if ( properties[idx + count].getName().equals( propertyName ) ) {
                validate( feature, properties[idx + count], propertyType );
                count++;
            } else {
                break;
            }
        }
        if ( count < minOccurs ) {
            if ( count == 0 ) {
                String msg = Messages.format( "ERROR_FT_INVALID2", feature.getId(), feature.getName(), propertyName );
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
            String msg = Messages.format( "ERROR_FT_INVALID3", feature.getId(), feature.getName(), propertyName,
                                          minOccurs, count );
            throw new OGCWebServiceException( this.getClass().getName(), msg );

        }
        if ( maxOccurs != -1 && count > maxOccurs ) {
            String msg = Messages.format( "ERROR_FT_INVALID4", feature.getId(), feature.getName(), propertyName,
                                          maxOccurs, count );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }
        return count;
    }

    /**
     * Validates that there is the correct amount of properties with the expected type in the given array of properties.
     *
     * @param feature
     * @param property
     * @param pt
     * @throws OGCWebServiceException
     */
    private void validate( Feature feature, FeatureProperty property, PropertyType pt )
                            throws OGCWebServiceException {

        Object value = property.getValue();
        if ( pt instanceof SimplePropertyType ) {
            if ( pt.getType() != Types.ANYTYPE ) {
                String s = value.toString();
                if ( value instanceof Date ) {
                    s = TimeTools.getISOFormattedTime( (Date) value );
                }
                Object newValue = validateSimpleProperty( feature, (SimplePropertyType) pt, s );
                property.setValue( newValue );
            }
        } else if ( pt instanceof GeometryPropertyType ) {
            if ( !( value instanceof Geometry ) ) {
                String msg = Messages.format( "ERROR_WRONG_PROPERTY_TYPE", pt.getName(), feature.getId(),
                                              "GeometryProperty", value.getClass().getName() );
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                // Geometry correctedGeometry =
                validateGeometryProperty( feature, (GeometryPropertyType) pt, (Geometry) value );
                // property.setValue( correctedGeometry );
            }
        } else if ( pt instanceof FeaturePropertyType ) {
            if ( !( ( value instanceof Feature ) || ( value instanceof URL ) ) ) {
                String msg = Messages.format( "ERROR_WRONG_PROPERTY_TYPE", pt.getName(), feature.getId(),
                                              "FeatureProperty", value.getClass().getName() );
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
            // only validate subfeature if it's not an external reference
            if ( value instanceof Feature ) {
                Feature subfeature = (Feature) value;
                // FeaturePropertyContent content = (FeaturePropertyContent) propertyType.getContents()
                // [0];
                // MappedFeatureType contentFT = content.getFeatureTypeReference().getFeatureType();

                // TODO: check that feature is a correct subsitution for the expected featuretype

                validate( subfeature );
            }
        } else if ( pt instanceof MultiGeometryPropertyType ) {
            throw new OGCWebServiceException( "Handling of MultiGeometryPropertyTypes not implemented "
                                              + "in validateProperty()." );
        } else {
            throw new OGCWebServiceException( "Internal error: Unhandled property type '" + pt.getClass()
                                              + "' encountered while validating property." );
        }
    }

    /**
     * Validates that the given string value can be converted to the type of the given {@link SimplePropertyType}.
     *
     * @param propertyType
     * @param s
     * @return corresponding <code>Object</code> for the string value
     * @throws OGCWebServiceException
     */
    private Object validateSimpleProperty( Feature feature, SimplePropertyType propertyType, String s )
                            throws OGCWebServiceException {

        int type = propertyType.getType();
        QualifiedName propertyName = propertyType.getName();

        Object value = null;
        if ( type == Types.NUMERIC || type == Types.DOUBLE ) {
            try {
                value = new Double( s );
            } catch ( NumberFormatException e ) {
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, feature.getId(), "Double" );
                throw new OGCWebServiceException( msg );
            }
        } else if ( type == Types.INTEGER ) {
            try {
                value = new Integer( s );
            } catch ( NumberFormatException e ) {
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, feature.getId(), "Integer" );
                throw new OGCWebServiceException( msg );
            }
        } else if ( type == Types.DECIMAL || type == Types.FLOAT ) {
            try {
                value = new Float( s );
            } catch ( NumberFormatException e ) {
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, feature.getId(), "Float" );
                throw new OGCWebServiceException( msg );
            }
        } else if ( type == Types.BOOLEAN ) {
            value = new Boolean( s );
        } else if ( type == Types.VARCHAR ) {
            value = s;
        } else if ( type == Types.DATE || type == Types.TIMESTAMP ) {
            try {
                value = TimeTools.createCalendar( s ).getTime();
            } catch ( NumberFormatException e ) {
                LOG.logDebug( "Stack trace: ", e );
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, feature.getId(), "Date" );
                throw new OGCWebServiceException( msg );
            }
        } else {
            String typeString = "" + type;
            try {
                typeString = Types.getTypeNameForSQLTypeCode( type );
            } catch ( UnknownTypeException e ) {
                LOG.logError( "No type name for code: " + type );
            }
            String msg = Messages.format( "ERROR_UNHANDLED_TYPE", "" + typeString );
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }
        return value;
    }

    private Geometry validateGeometryProperty( Feature feature, GeometryPropertyType pt, Geometry geometry ) {

        try {
            com.vividsolutions.jts.geom.Geometry jtsGeometry = JTSAdapter.export( geometry );
            if ( !jtsGeometry.isValid() ) {
                String msg = Messages.format( "GEOMETRY_NOT_VALID", pt.getName(), feature.getId() );
                LOG.logDebug( msg );
            } else if ( geometry instanceof Surface ) {
                geometry = validatePolygonOrientation( feature, pt, (Surface) geometry, (Polygon) jtsGeometry );
            } else if ( geometry instanceof MultiSurface ) {
                geometry = validateMultiPolygonOrientation( feature, pt, (MultiSurface) geometry,
                                                            (MultiPolygon) jtsGeometry );
            }
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
        }
        return geometry;
    }

    /**
     * Checks whether the outer boundary of the given {@link Surface} geometry has counter-clockwise orientation and
     * that the inner boundaries have clockwise orientation (as specified by ISO 19107 / GML).
     * <p>
     * Information on invalid orientations is logged.
     *
     * @param feature
     * @param pt
     * @param surface
     * @param polygon
     * @throws GeometryException
     */
    private Surface validatePolygonOrientation( Feature feature, GeometryPropertyType pt, Surface surface,
                                                Polygon polygon )
                            throws GeometryException {
        GeometryFactory factory = new GeometryFactory();
        CoordinateArraySequenceFactory coordSeqFactory = CoordinateArraySequenceFactory.instance();

        Coordinate[] outerCoords = polygon.getExteriorRing().getCoordinates();
        if ( !CGAlgorithms.isCCW( outerCoords ) ) {
            String msg = Messages.format( "OUTER_RING_NOT_CCW", pt.getName(), feature.getId() );
            LOG.logDebug( msg );
            CoordinateArrays.reverse( outerCoords );
        }
        LinearRing shell = new LinearRing( coordSeqFactory.create( outerCoords ), factory );

        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for ( int i = 0; i < polygon.getNumInteriorRing(); i++ ) {
            Coordinate[] innerCoords = polygon.getInteriorRingN( i ).getCoordinates();
            if ( CGAlgorithms.isCCW( innerCoords ) ) {
                String msg = Messages.format( "INNER_RING_NOT_CW", i, pt.getName(), feature.getId() );
                LOG.logDebug( msg );
                CoordinateArrays.reverse( innerCoords );
            }
            holes[i] = new LinearRing( coordSeqFactory.create( innerCoords ), factory );
        }
        Surface correctedSurface = (Surface) JTSAdapter.wrap( new Polygon( shell, holes, factory ) );
        SurfacePatch[] patches = new SurfacePatch[correctedSurface.getNumberOfSurfacePatches()];
        for ( int i = 0; i < patches.length; i++ ) {
            patches[i] = correctedSurface.getSurfacePatchAt( 0 );
        }
        return org.deegree.model.spatialschema.GeometryFactory.createSurface( patches, surface.getCoordinateSystem() );
    }

    /**
     * Checks whether the outer boundaries of the given {@link MultiSurface} members have counter-clockwise orientation
     * and that the inner boundaries have clockwise orientation (as specified by ISO 19107 / GML).
     * <p>
     * Information on invalid orientations is logged.
     *
     * @param feature
     * @param pt
     * @param multiSurface
     * @param multiPolygon
     * @throws GeometryException
     */
    private MultiSurface validateMultiPolygonOrientation( Feature feature, GeometryPropertyType pt,
                                                          MultiSurface multiSurface, MultiPolygon multiPolygon )
                            throws GeometryException {
        Surface[] surfaces = new Surface[multiPolygon.getNumGeometries()];
        for ( int i = 0; i < surfaces.length; i++ ) {
            surfaces[i] = validatePolygonOrientation( feature, pt, multiSurface.getSurfaceAt( i ),
                                                      (Polygon) multiPolygon.getGeometryN( i ) );
        }
        return org.deegree.model.spatialschema.GeometryFactory.createMultiSurface( surfaces,
                                                                                   multiSurface.getCoordinateSystem() );
    }
}
