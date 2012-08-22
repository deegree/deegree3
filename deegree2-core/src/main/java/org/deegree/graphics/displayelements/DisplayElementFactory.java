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
package org.deegree.graphics.displayelements;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.sld.FeatureTypeStyle;
import org.deegree.graphics.sld.Geometry;
import org.deegree.graphics.sld.LineSymbolizer;
import org.deegree.graphics.sld.PointSymbolizer;
import org.deegree.graphics.sld.PolygonSymbolizer;
import org.deegree.graphics.sld.RasterSymbolizer;
import org.deegree.graphics.sld.Rule;
import org.deegree.graphics.sld.Symbolizer;
import org.deegree.graphics.sld.TextSymbolizer;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiGeometry;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiPrimitive;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * Factory class for the different kinds of {@link DisplayElement}s.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DisplayElementFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( DisplayElementFactory.class );

    /**
     * @param o
     * @param styles
     * @return the display elements
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws GeometryException
     * @throws PropertyPathResolvingException
     */
    public DisplayElement[] createDisplayElement( Object o, UserStyle[] styles, double pixelsize )
                            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
                            NoSuchMethodException, InvocationTargetException, GeometryException,
                            PropertyPathResolvingException {
        return createDisplayElement( o, styles, null, pixelsize );
    }

    /**
     * Returns the display elements for a {@link Feature} or {@link GridCoverage}.
     * 
     * @param o
     * @param styles
     * @param request
     * @return the display elements
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws GeometryException
     * 
     * @throws PropertyPathResolvingException
     */
    public DisplayElement[] createDisplayElement( Object o, UserStyle[] styles, GetMap request, double pixelsize )
                            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
                            NoSuchMethodException, InvocationTargetException, GeometryException,
                            PropertyPathResolvingException {

        ArrayList<DisplayElement> list = new ArrayList<DisplayElement>( 20 );

        if ( o instanceof Feature ) {
            Feature tmp = (Feature) o;

            List<Feature> features = splitFeature( tmp );
            for ( Feature feature : features ) {

                try {
                    String featureTypeName = feature.getFeatureType().getName().getPrefixedName();
                    String qfeatureTypeName = feature.getFeatureType().getName().getFormattedString();

                    for ( int i = 0; i < styles.length; i++ ) {

                        if ( styles[i] == null ) {
                            // create display element from default style
                            DisplayElement de = buildDisplayElement( feature );
                            if ( de != null ) {
                                list.add( de );
                            }
                        } else {
                            FeatureTypeStyle[] fts = styles[i].getFeatureTypeStyles();
                            for ( int k = 0; k < fts.length; k++ ) {
                                if ( fts[k].getFeatureTypeName() == null
                                     || featureTypeName.equals( fts[k].getFeatureTypeName() )
                                     || qfeatureTypeName.equals( fts[k].getFeatureTypeName() ) ) {
                                    Rule[] rules = fts[k].getRules();
                                    for ( int n = 0; n < rules.length; n++ ) {
                                        // does the filter rule apply?
                                        Filter filter = rules[n].getFilter();
                                        if ( filter != null ) {
                                            try {
                                                if ( !filter.evaluate( feature ) ) {
                                                    continue;
                                                }
                                            } catch ( FilterEvaluationException e ) {
                                                LOG.logDebug( "Error evaluating filter: ", e );
                                                continue;
                                            }
                                        }

                                        // Filter expression is true for this feature, so a
                                        // corresponding DisplayElement has to be added to the
                                        // list
                                        Symbolizer[] symbolizers = rules[n].getSymbolizers();

                                        for ( int u = 0; u < symbolizers.length; u++ ) {
                                            DisplayElement displayElement = buildDisplayElement( feature,
                                                                                                 symbolizers[u],
                                                                                                 pixelsize );
                                            if ( displayElement != null ) {
                                                list.add( displayElement );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch ( IncompatibleGeometryTypeException e ) {
                    LOG.logWarning( e.getLocalizedMessage() );
                    LOG.logDebug( "Stack trace", e );
                }
            }
        } else {
            for ( UserStyle style : styles ) {
                if ( style == null ) {
                    list.add( buildRasterDisplayElement( (GridCoverage) o, new RasterSymbolizer(), request ) );
                    continue;
                }
                for ( FeatureTypeStyle fts : style.getFeatureTypeStyles() ) {
                    for ( Rule rule : fts.getRules() ) {
                        for ( Symbolizer symbolizer : rule.getSymbolizers() ) {
                            list.add( buildRasterDisplayElement( (GridCoverage) o, (RasterSymbolizer) symbolizer,
                                                                 request ) );
                        }
                    }
                }
            }
        }

        DisplayElement[] de = new DisplayElement[list.size()];
        return list.toArray( de );
    }

    /**
     * splits a feature into n-Features if at least one of a features geometries are a {@link MultiGeometry}
     * 
     * @param feature
     * @return list of {@link Feature}
     */
    private List<Feature> splitFeature( Feature feature ) {
        List<Feature> list = new ArrayList<Feature>();
        FeatureType ft = feature.getFeatureType();
        PropertyType[] pt = ft.getProperties();

        for ( PropertyType type : pt ) {
            FeatureProperty[] fp = feature.getProperties( type.getName() );
            if ( fp == null ) {
                continue;
            }
            for ( FeatureProperty property : fp ) {
                if ( property == null ) {
                    continue;
                }
                Object value = property.getValue();
                if ( value instanceof MultiGeometry ) {
                    MultiGeometry mg = (MultiGeometry) value;
                    org.deegree.model.spatialschema.Geometry[] g = mg.getAll();
                    for ( org.deegree.model.spatialschema.Geometry geometry : g ) {
                        list.add( cloneFeature( feature, geometry, property.getName() ) );
                    }
                }
            }

        }
        if ( list.size() == 0 ) {
            list.add( feature );
        }

        return list;
    }

    private Feature cloneFeature( Feature feature, org.deegree.model.spatialschema.Geometry geometry, QualifiedName name ) {
        FeatureType ft = feature.getFeatureType();
        PropertyType[] pt = ft.getProperties();
        List<FeatureProperty> properties = new ArrayList<FeatureProperty>( pt.length );
        for ( PropertyType type : pt ) {
            if ( type.getName().equals( name ) ) {
                properties.add( FeatureFactory.createFeatureProperty( name, geometry ) );
            } else {
                FeatureProperty[] tmp = feature.getProperties( type.getName() );
                for ( FeatureProperty property : tmp ) {
                    properties.add( property );
                }
            }
        }
        FeatureProperty[] fp = properties.toArray( new FeatureProperty[properties.size()] );
        return FeatureFactory.createFeature( UUID.randomUUID().toString(), ft, fp );
    }

    /**
     * Builds a {@link DisplayElement} using the given {@link Feature} or {@link GridCoverage} and {@link Symbolizer}.
     * 
     * @param o
     *            contains the geometry or raster information (Feature or GridCoverage)
     * @param symbolizer
     *            contains the drawing (style) information and selects the geometry property of the <code>Feature</code>
     *            to be drawn
     * @throws IncompatibleGeometryTypeException
     *             if the selected geometry of the <code>Feature</code> is not compatible with the
     *             <code>Symbolizer</code>
     * @return constructed <code>DisplayElement</code>
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws GeometryException
     * @throws PropertyPathResolvingException
     */
    public static DisplayElement buildDisplayElement( Object o, Symbolizer symbolizer, double pixelsize )
                            throws IncompatibleGeometryTypeException, ClassNotFoundException, IllegalAccessException,
                            InstantiationException, NoSuchMethodException, InvocationTargetException,
                            GeometryException, PropertyPathResolvingException {
        DisplayElement displayElement = null;

        if ( o instanceof Feature ) {
            Feature feature = (Feature) o;

            // determine the geometry property to be used
            org.deegree.model.spatialschema.Geometry geometry = null;
            Geometry symbolizerGeometry = symbolizer.getGeometry();

            if ( symbolizerGeometry != null ) {
                FeatureProperty property = feature.getDefaultProperty( symbolizerGeometry.getPropertyPath() );
                if ( property != null ) {
                    geometry = (org.deegree.model.spatialschema.Geometry) property.getValue();
                }
            } else {
                geometry = feature.getDefaultGeometryPropertyValue();
            }

            // if the geometry is null, do not build a DisplayElement
            if ( geometry == null ) {
                return null;
            }
            if ( symbolizer instanceof PointSymbolizer ) {
                displayElement = buildPointDisplayElement( feature, geometry, (PointSymbolizer) symbolizer );
            } else if ( symbolizer instanceof LineSymbolizer ) {
                displayElement = buildLineStringDisplayElement( feature, geometry, (LineSymbolizer) symbolizer );
            } else if ( symbolizer instanceof PolygonSymbolizer ) {
                displayElement = buildPolygonDisplayElement( feature, geometry, (PolygonSymbolizer) symbolizer );
            } else if ( symbolizer instanceof TextSymbolizer ) {
                displayElement = buildLabelDisplayElement( feature, geometry, (TextSymbolizer) symbolizer, pixelsize );
            }
        } else {
            if ( symbolizer instanceof RasterSymbolizer ) {
                LOG.logDebug( "Building RasterDisplayElement" );
                displayElement = buildRasterDisplayElement( (GridCoverage) o, (RasterSymbolizer) symbolizer );
            }
        }

        return displayElement;
    }

    /**
     * Builds a {@link DisplayElement} using the given {@link Feature} or {@link GridCoverage} and the default
     * {@link Symbolizer}.
     * 
     * @param o
     *            contains the geometry or raster information (Feature or GridCoverage)
     * @throws IncompatibleGeometryTypeException
     *             if the selected geometry of the <code>Feature</code> is not compatible with the
     *             <code>Symbolizer</code>
     * @return constructed <code>DisplayElement</code>
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws GeometryException
     */
    public static DisplayElement buildDisplayElement( Object o )
                            throws IncompatibleGeometryTypeException, ClassNotFoundException, IllegalAccessException,
                            InstantiationException, NoSuchMethodException, InvocationTargetException, GeometryException {

        DisplayElement displayElement = null;

        if ( o instanceof GridCoverage ) {
            RasterSymbolizer symbolizer = new RasterSymbolizer();
            displayElement = buildRasterDisplayElement( (GridCoverage) o, symbolizer );
        } else {
            Feature feature = (Feature) o;
            // determine the geometry property to be used
            org.deegree.model.spatialschema.Geometry geoProperty = feature.getDefaultGeometryPropertyValue();

            // if the geometry property is null, do not build a DisplayElement
            if ( geoProperty == null ) {
                return null;
            }
            // PointSymbolizer
            if ( geoProperty instanceof Point || geoProperty instanceof MultiPoint ) {
                PointSymbolizer symbolizer = new PointSymbolizer();
                displayElement = buildPointDisplayElement( feature, geoProperty, symbolizer );
            } // LineSymbolizer
            else if ( geoProperty instanceof Curve || geoProperty instanceof MultiCurve ) {
                LineSymbolizer symbolizer = new LineSymbolizer();
                displayElement = buildLineStringDisplayElement( feature, geoProperty, symbolizer );
            } // PolygonSymbolizer
            else if ( geoProperty instanceof Surface || geoProperty instanceof MultiSurface ) {
                PolygonSymbolizer symbolizer = new PolygonSymbolizer();
                displayElement = buildPolygonDisplayElement( feature, geoProperty, symbolizer );
            } else {
                throw new IncompatibleGeometryTypeException( "not a valid geometry type" );
            }
        }

        return displayElement;
    }

    /**
     * Creates a {@link PointDisplayElement} using the given geometry and style information.
     * 
     * @param feature
     *            associated feature (source of the geometry information)
     * @param geom
     *            geometry information
     * @param sym
     *            style information
     * @return constructed <code>PointDisplayElement</code>
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static PointDisplayElement buildPointDisplayElement( Feature feature,
                                                                org.deegree.model.spatialschema.Geometry geom,
                                                                PointSymbolizer sym )
                            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
                            NoSuchMethodException, InvocationTargetException {

        // if the geometry is not a point geometry, the centroid(s) of the
        // geometry will be used
        PointDisplayElement displayElement = null;
        String className = sym.getResponsibleClass();
        Class<?> clss = Class.forName( className );
        Object[] values = new Object[] { feature, geom, sym };
        if ( geom instanceof Point ) {
            Class<?>[] param = new Class[] { Feature.class, Point.class, PointSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            displayElement = (PointDisplayElement) constructor.newInstance( values );
        } else if ( geom instanceof MultiPoint ) {
            Class<?>[] param = new Class[] { Feature.class, MultiPoint.class, PointSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            displayElement = (PointDisplayElement) constructor.newInstance( values );
        } else if ( geom instanceof MultiPrimitive ) {
            // Primitive[] primitives = ( (MultiPrimitive) geom ).getAllPrimitives();
            // Point[] centroids = new Point[primitives.length];
            Point[] centroids = new Point[1];
            centroids[0] = geom.getCentroid();

            // for ( int i = 0; i < primitives.length; i++ ) {
            // centroids[i] = primitives[i].getCentroid();
            // }

            try {
                Class<?>[] param = new Class[] { Feature.class, MultiPoint.class, PointSymbolizer.class };
                Constructor<?> constructor = clss.getConstructor( param );
                values[1] = GeometryFactory.createMultiPoint( centroids );
                displayElement = (PointDisplayElement) constructor.newInstance( values );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        } else {
            Class<?>[] param = new Class[] { Feature.class, Point.class, PointSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            values[1] = geom.getCentroid();
            displayElement = (PointDisplayElement) constructor.newInstance( values );
        }

        return displayElement;
    }

    /**
     * Creates a {@link LineStringDisplayElement} using the given geometry and style information.
     * 
     * @param feature
     *            associated feature (source of the geometry information)
     * @param geom
     *            geometry information
     * @param sym
     *            style information
     * @throws IncompatibleGeometryTypeException
     *             if the geometry property is not a <code>Curve</code> or <code>MultiCurve</code>
     * @return constructed <code>LineStringDisplayElement</code>
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws GeometryException
     */
    public static LineStringDisplayElement buildLineStringDisplayElement( Feature feature,
                                                                          org.deegree.model.spatialschema.Geometry geom,
                                                                          LineSymbolizer sym )
                            throws IncompatibleGeometryTypeException, ClassNotFoundException, IllegalAccessException,
                            InstantiationException, NoSuchMethodException, InvocationTargetException, GeometryException {
        LineStringDisplayElement displayElement = null;

        String className = sym.getResponsibleClass();
        Class<?> clss = Class.forName( className );
        Object[] values = new Object[] { feature, geom, sym };

        if ( geom instanceof Curve ) {
            Class<?>[] param = new Class[] { Feature.class, Curve.class, LineSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            displayElement = (LineStringDisplayElement) constructor.newInstance( values );
        } else if ( geom instanceof MultiCurve ) {
            Class<?>[] param = new Class[] { Feature.class, MultiCurve.class, LineSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            displayElement = (LineStringDisplayElement) constructor.newInstance( values );
        } else if ( geom instanceof Surface ) {
            // according to OGC SLD specification it is possible to assign a
            // LineSymbolizer to a polygon. To handle this the border of the
            // polygon will be transformed into a lines (curves)
            MultiCurve mc = surfaceToCurve( (Surface) geom );
            displayElement = buildLineStringDisplayElement( feature, mc, sym );
        } else if ( geom instanceof MultiSurface ) {
            // according to OGC SLD specification it is possible to assign a
            // LineSymbolizer to a multipolygon. To handle this the borders of the
            // multipolygons will be transformed into a lines (curves)
            MultiSurface ms = (MultiSurface) geom;
            List<Curve> list = new ArrayList<Curve>( 500 );
            for ( int i = 0; i < ms.getSize(); i++ ) {
                MultiCurve mc = surfaceToCurve( ms.getSurfaceAt( i ) );
                for ( int j = 0; j < mc.getSize(); j++ ) {
                    list.add( mc.getCurveAt( j ) );
                }
            }
            Curve[] curves = list.toArray( new Curve[list.size()] );
            MultiCurve mc = GeometryFactory.createMultiCurve( curves );
            displayElement = buildLineStringDisplayElement( feature, mc, sym );
        } else {
            throw new IncompatibleGeometryTypeException( "Tried to create a LineStringDisplayElement from a geometry "
                                                         + "with an incompatible / unsupported type: '"
                                                         + geom.getClass().getName() + "'!" );
        }

        return displayElement;
    }

    /**
     * Transforms a {@link Surface} into a {@link MultiCurve}.
     * 
     * @param geom
     * @return MultiCurve
     */
    private static MultiCurve surfaceToCurve( Surface geom )
                            throws GeometryException {
        List<Curve> list = new ArrayList<Curve>( 100 );
        int num = geom.getNumberOfSurfacePatches();
        for ( int i = 0; i < num; i++ ) {
            Position[] pos = geom.getSurfacePatchAt( i ).getExteriorRing();
            Curve curve = GeometryFactory.createCurve( pos, geom.getCoordinateSystem() );
            list.add( curve );
            Position[][] inn = geom.getSurfacePatchAt( i ).getInteriorRings();
            if ( inn != null ) {
                for ( int j = 0; j < inn.length; j++ ) {
                    curve = GeometryFactory.createCurve( inn[j], geom.getCoordinateSystem() );
                    list.add( curve );
                }
            }
        }
        Curve[] curves = list.toArray( new Curve[list.size()] );
        MultiCurve mc = GeometryFactory.createMultiCurve( curves );
        return mc;
    }

    /**
     * Creates a {@link PolygonDisplayElement} using the given geometry and style information.
     * 
     * @param feature
     *            associated feature (source of the geometry information)
     * @param geom
     *            geometry information
     * @param sym
     *            style information
     * @throws IncompatibleGeometryTypeException
     *             if the geometry property is not a <code>Surface</code> or <code>MultiSurface</code>
     * @return constructed <code>PolygonDisplayElement</code>
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static PolygonDisplayElement buildPolygonDisplayElement( Feature feature,
                                                                    org.deegree.model.spatialschema.Geometry geom,
                                                                    PolygonSymbolizer sym )
                            throws IncompatibleGeometryTypeException, ClassNotFoundException, IllegalAccessException,
                            InstantiationException, NoSuchMethodException, InvocationTargetException {
        PolygonDisplayElement displayElement = null;

        String className = sym.getResponsibleClass();
        Class<?> clss = Class.forName( className );
        Object[] values = new Object[] { feature, geom, sym };
        if ( geom instanceof Surface ) {
            Class<?>[] param = new Class[] { Feature.class, Surface.class, PolygonSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            displayElement = (PolygonDisplayElement) constructor.newInstance( values );
        } else if ( geom instanceof MultiSurface ) {
            Class<?>[] param = new Class[] { Feature.class, MultiSurface.class, PolygonSymbolizer.class };
            Constructor<?> constructor = clss.getConstructor( param );
            displayElement = (PolygonDisplayElement) constructor.newInstance( values );
        } else {
            String s = Messages.get( Locale.getDefault(), "INVALID_GEOM_TYPE_FOR_POLYGON_DE", geom.getClass().getName() );
            throw new IncompatibleGeometryTypeException( s );
        }

        return displayElement;
    }

    /**
     * Creates a {@link LabelDisplayElement} using the given geometry and style information.
     * 
     * @param feature
     *            associated feature (source of the geometry information and label caption)
     * @param geom
     *            geometry information
     * @param sym
     *            style information
     * @throws IncompatibleGeometryTypeException
     *             if the geometry property is not a <code>Point</code>, a <code>Surface</code> or
     *             <code>MultiSurface</code>
     * @return constructed <code>LabelDisplayElement</code>
     */
    public static LabelDisplayElement buildLabelDisplayElement( Feature feature,
                                                                org.deegree.model.spatialschema.Geometry geom,
                                                                TextSymbolizer sym, double pixelsize )
                            throws IncompatibleGeometryTypeException {

        LabelDisplayElement displayElement = null;

        if ( geom instanceof Point || geom instanceof MultiPoint || geom instanceof Surface
             || geom instanceof MultiSurface || geom instanceof Curve || geom instanceof MultiCurve ) {
            displayElement = new LabelDisplayElement( feature, geom, sym, pixelsize );
        } else {
            throw new IncompatibleGeometryTypeException( "Tried to create a LabelDisplayElement from a geometry with "
                                                         + "an incompatible / unsupported type: '"
                                                         + geom.getClass().getName() + "'!" );
        }

        return displayElement;
    }

    /**
     * Creates a {@link RasterDisplayElement} from the given {@link GridCoverage}.
     * 
     * @param gc
     *            grid coverage (source of the raster data)
     * @param sym
     *            raster symbolizer
     * 
     * @return RasterDisplayElement
     */
    public static RasterDisplayElement buildRasterDisplayElement( GridCoverage gc, RasterSymbolizer sym ) {
        return new RasterDisplayElement( gc, sym );
    }

    /**
     * @param gc
     * @param sym
     * @param request
     * @return the new raster display element
     */
    public static RasterDisplayElement buildRasterDisplayElement( GridCoverage gc, RasterSymbolizer sym, GetMap request ) {
        return new RasterDisplayElement( gc, sym, request );
    }

}
