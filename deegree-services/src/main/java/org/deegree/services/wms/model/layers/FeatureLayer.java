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

package org.deegree.services.wms.model.layers;

import static java.lang.System.currentTimeMillis;
import static org.deegree.commons.utils.CollectionUtils.clearNulls;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.commons.utils.time.DateUtils.formatISO8601Date;
import static org.deegree.commons.utils.time.DateUtils.formatISO8601DateWOMS;
import static org.deegree.cs.CRSCodeType.getUndefined;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.services.wms.model.Dimension.formatDimensionValueList;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.query.ThreadedResultSet;
import org.deegree.feature.persistence.shape.ShapeFeatureStore;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Intersects;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.dims.DimensionInterval;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.wms.WMSException.InvalidDimensionValue;
import org.deegree.services.wms.WMSException.MissingDimensionValue;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.model.Dimension;
import org.slf4j.Logger;

/**
 * <code>ShapefileLayer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(error = "logs the error when a dynamic layer could not be loaded", warn = "logs information about errors when accessing the data store", debug = "logs when a query is done against the data stores", trace = "logs stack traces")
public class FeatureLayer extends Layer {

    private static final Logger LOG = getLogger( FeatureLayer.class );

    private FeatureStore datastore;

    /**
     * @param layer
     * @param parent
     * @param adapter
     * @throws IOException
     * @throws MalformedURLException
     * @throws FileNotFoundException
     */
    public FeatureLayer( AbstractLayerType layer, Layer parent, XMLAdapter adapter ) throws FileNotFoundException,
                            MalformedURLException, IOException {
        super( layer, parent );
        datastore = FeatureStoreManager.get( layer.getFeatureStoreId() );
    }

    /**
     * Used for extensions.
     * 
     * @param name
     * @param title
     * @param parent
     */
    public FeatureLayer( String name, String title, Layer parent ) {
        super( name, title, parent );
    }

    /**
     * @param name
     * @param title
     * @param parent
     * @param file
     * @throws IOException
     * @throws FileNotFoundException
     */
    public FeatureLayer( String name, String title, Layer parent, String file ) throws FileNotFoundException,
                            IOException {
        super( name, title, parent );
        // TODO what about the charset here?
        datastore = new ShapeFeatureStore( file, null, null, null, null, true, null );
        try {
            datastore.init();
        } catch ( FeatureStoreException e ) {
            LOG.error( "Layer could not be loaded, because the error '{}' occurred while loading the shape file.",
                       e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        if ( !datastore.isAvailable() ) {
            LOG.error( "Layer could not be loaded, because the feature store is not available." );
        }
        CRS crs = datastore.getStorageSRS();
        if ( crs != null ) {
            try {
                LinkedList<CRS> ss = getSrs();
                if ( !ss.contains( crs ) && !crs.getWrappedCRS().getCode().equals( getUndefined() ) ) {
                    ss.addFirst( crs );
                }
            } catch ( UnknownCRSException e ) {
                LOG.warn( "SRS '{}' of shape datastore '{}' is not known.", crs.getName(), file );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    @Override
    public Envelope getBbox() {
        if ( datastore == null || !datastore.isAvailable() ) {
            return null;
        }

        // always use up-to-date envelope
        Envelope bbox = null;
        ApplicationSchema schema = datastore.getSchema();

        for ( FeatureType t : schema.getFeatureTypes( null, false, false ) ) {
            Envelope thisBox = null;
            try {
                thisBox = datastore.getEnvelope( t.getName() );
            } catch ( FeatureStoreException e ) {
                LOG.error( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
            }
            if ( bbox == null ) {
                bbox = thisBox;
            } else {
                if ( thisBox != null ) {
                    bbox = bbox.merge( thisBox );
                }
            }
        }
        return bbox;
    }

    @Override
    public void close() {
        if ( datastore != null ) {
            datastore.destroy();
        }
    }

    /**
     * @return the underlying feature store
     */
    public FeatureStore getDataStore() {
        return datastore;
    }

    /**
     * @param style
     * @param gm
     * @param queries
     * @return a list of dimension warnings
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    public LinkedList<String> collectQueries( Style style, final GetMap gm, LinkedList<Query> queries )
                            throws MissingDimensionValue, InvalidDimensionValue {
        final Envelope bbox = gm.getBoundingBox();

        final Pair<Filter, LinkedList<String>> dimFilter = getDimensionFilter( gm.getDimensions() );
        final Filter filter = gm.getFilterForLayer( this.getName(), dimFilter == null ? null : dimFilter.first, style );
        if ( style != null ) {
            QName ftName = style.getFeatureType();
            if ( ftName != null && datastore.getSchema().getFeatureType( ftName ) == null ) {
                LOG.warn( "FeatureType '" + ftName + "' from style is not known to the FeatureStore." );
                return new LinkedList<String>();
            }
        }

        QName featureType = style == null ? null : style.getFeatureType();
        Integer maxFeats = gm.getMaxFeatures().get( this );
        final int maxFeatures = maxFeats == null ? -1 : maxFeats;
        if ( featureType == null && datastore != null ) {
            queries.addAll( map( datastore.getSchema().getFeatureTypes( null, false, false ),
                                 new Mapper<Query, FeatureType>() {
                                     public Query apply( FeatureType u ) {
                                         return new Query( u.getName(), bbox, filter, round( gm.getScale() ),
                                                           maxFeatures, gm.getResolution() );
                                     }
                                 } ) );
        } else {
            Query query = new Query( featureType, bbox, filter, round( gm.getScale() ), maxFeatures, gm.getResolution() );
            queries.add( query );
        }
        return dimFilter == null ? new LinkedList<String>() : dimFilter.second;
    }

    @Override
    public LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        LinkedList<Query> queries = new LinkedList<Query>();
        LinkedList<String> warnings = collectQueries( style, gm, queries );

        Java2DRenderer renderer = new Java2DRenderer( g, gm.getWidth(), gm.getHeight(), gm.getBoundingBox(),
                                                      gm.getPixelSize() );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        // TODO
        FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

        if ( queries.isEmpty() ) {
            LOG.warn( "No queries were generated. Is the configuration correct?" );
            return warnings;
        }

        FeatureResultSet rs = null;
        try {
            rs = datastore.query( queries.toArray( new Query[queries.size()] ) );
            // TODO Should this always be done on this level? What about min and maxFill values?
            rs = new ThreadedResultSet( rs, 100, 20 );
            Integer maxFeats = gm.getMaxFeatures().get( this );
            int max = maxFeats == null ? -1 : maxFeats;
            int cnt = 0;
            double resolution = gm.getResolution();
            if ( !gm.getCoordinateSystem().equals( datastore.getStorageSRS() ) ) {
                try {
                    Envelope b = new GeometryTransformer( datastore.getStorageSRS() ).transform( gm.getBoundingBox() );
                    resolution = Utils.calcResolution( b, gm.getWidth(), gm.getHeight() );
                } catch ( IllegalArgumentException e ) {
                    LOG.warn( "Calculating the resolution failed: '{}'", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                } catch ( TransformationException e ) {
                    LOG.warn( "Calculating the resolution failed: '{}'", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                } catch ( UnknownCRSException e ) {
                    LOG.warn( "Calculating the resolution failed: '{}'", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            for ( Feature f : rs ) {
                try {
                    render( f, evaluator, style, renderer, textRenderer, gm.getScale(), resolution );
                } catch ( IllegalArgumentException e ) {
                    LOG.warn( "Unable to render feature, probably a curve had multiple/non-linear segments." );
                    LOG.trace( "Stack trace:", e );
                }
                if ( max > 0 && ++cnt == max ) {
                    LOG.debug( "Reached max features of {} for layer '{}', stopping.", max, this );
                    break;
                }
            }
        } catch ( FilterEvaluationException e ) {
            LOG.warn( "A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( FeatureStoreException e ) {
            LOG.warn( "Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( rs != null ) {
                rs.close();
            }
        }
        return warnings;
    }

    @Override
    public boolean isAvailable() {
        if ( datastore == null ) {
            LOG.debug( "Layer '{}' is not available, since its data store could not be loaded.", getName() );
        } else if ( !datastore.isAvailable() ) {
            LOG.debug( "Layer '{}' is not available, since its data store is unavailable.", getName() );
        }
        return datastore != null && datastore.isAvailable();
    }

    static OperatorFilter buildFilter( Operator operator, FeatureType u, Envelope clickBox ) {
        if ( u == null ) {
            if ( operator == null ) {
                return null;
            }
            return new OperatorFilter( operator );
        }
        LinkedList<Operator> list = new LinkedList<Operator>();
        for ( PropertyType pt : u.getPropertyDeclarations() ) {
            if ( pt instanceof GeometryPropertyType
                 && ( ( (GeometryPropertyType) pt ).getCoordinateDimension() == DIM_2 || ( (GeometryPropertyType) pt ).getCoordinateDimension() == DIM_2_OR_3 ) ) {
                list.add( new And( new BBOX( new PropertyName( pt.getName() ), clickBox ),
                                   new Intersects( new PropertyName( pt.getName() ), clickBox ) ) );
            }
        }
        if ( list.size() > 1 ) {
            Or or = new Or( list.toArray( new Operator[list.size()] ) );
            if ( operator == null ) {
                return new OperatorFilter( or );
            }
            return new OperatorFilter( new And( or, operator ) );
        }
        if ( !list.isEmpty() ) {
            if ( operator == null ) {
                return new OperatorFilter( list.get( 0 ) );
            }
            return new OperatorFilter( new And( list.get( 0 ), operator ) );
        }
        if ( operator == null ) {
            return null;
        }
        return new OperatorFilter( operator );
    }

    private FeatureCollection clearDuplicates( FeatureResultSet rs ) {
        FeatureCollection col = new GenericFeatureCollection();
        for ( Feature f : rs ) {
            if ( !col.contains( f ) ) {
                col.add( f );
            }
        }
        return col;
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( final GetFeatureInfo fi, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {

        try {
            final Pair<Filter, LinkedList<String>> dimFilter = getDimensionFilter( fi.getDimensions() );
            // TODO need the style here for filters, scale constraints etc.
            final Envelope clickBox = fi.getClickBox();
            final Operator operator = dimFilter == null ? null : ( (OperatorFilter) dimFilter.first ).getOperator();

            QName featureType = style == null ? null : style.getFeatureType();

            LOG.debug( "Querying the feature store(s)..." );

            FeatureCollection col;
            if ( featureType == null ) {
                List<Query> queries = clearNulls( map( datastore.getSchema().getFeatureTypes( null, false, false ),
                                                       new Mapper<Query, FeatureType>() {
                                                           public Query apply( FeatureType u ) {
                                                               if ( u.getDefaultGeometryPropertyDeclaration() == null ) {
                                                                   return null;
                                                               }
                                                               return new Query( u.getName(), clickBox,
                                                                                 buildFilter( operator, u, clickBox ),
                                                                                 -1, fi.getFeatureCount(), -1 );
                                                           }
                                                       } ) );
                col = clearDuplicates( datastore.query( queries.toArray( new Query[queries.size()] ) ) );
            } else {
                FeatureType ft = datastore.getSchema().getFeatureType( featureType );
                if ( ft.getDefaultGeometryPropertyDeclaration() == null ) {
                    return new Pair<FeatureCollection, LinkedList<String>>( new GenericFeatureCollection(),
                                                                            new LinkedList<String>() );
                }
                Query query = new Query( featureType, clickBox, buildFilter( operator, ft, clickBox ), -1,
                                         fi.getFeatureCount(), -1 );
                col = clearDuplicates( datastore.query( query ) );
            }

            LOG.debug( "Finished querying the feature store(s)." );

            return new Pair<FeatureCollection, LinkedList<String>>( col, dimFilter == null ? new LinkedList<String>()
                                                                                          : dimFilter.second );
        } catch ( FilterEvaluationException e ) {
            LOG.warn( "A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( FeatureStoreException e ) {
            LOG.warn( "Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }

        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

    /**
     * @param dims
     * @return a filter or null, if no dimensions have been requested
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    protected Pair<Filter, LinkedList<String>> getDimensionFilter( HashMap<String, List<?>> dims )
                            throws MissingDimensionValue, InvalidDimensionValue {

        LinkedList<String> warnings = new LinkedList<String>();
        LinkedList<Operator> ops = new LinkedList<Operator>();

        if ( time != null ) {
            final PropertyName property = new PropertyName( time.getPropertyName() );

            List<?> vals = dims.get( "time" );

            if ( vals == null ) {
                vals = time.getDefaultValue();
                if ( vals == null ) {
                    throw new MissingDimensionValue( "time" );
                }
                String defVal = formatDimensionValueList( vals, true );

                warnings.add( "99 Default value used: time=" + defVal + " ISO8601" );
            }

            Operator[] os = new Operator[vals.size()];
            int i = 0;
            for ( Object o : vals ) {
                if ( !time.getNearestValue() && !time.isValid( o ) ) {
                    throw new InvalidDimensionValue( "time", o instanceof Date ? formatISO8601DateWOMS( (Date) o )
                                                                              : o.toString() );
                }
                Date theVal = null;
                if ( o instanceof DimensionInterval<?, ?, ?> ) {
                    DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
                    final String min = formatISO8601DateWOMS( (Date) iv.min );
                    final String max = formatISO8601DateWOMS( (Date) iv.max );
                    os[i++] = new PropertyIsBetween( property, new Literal<PrimitiveValue>( min ),
                                                     new Literal<PrimitiveValue>( max ), false );
                } else if ( o.toString().equalsIgnoreCase( "current" ) ) {
                    if ( !time.getCurrent() ) {
                        throw new InvalidDimensionValue( "time", "current" );
                    }
                    theVal = new Date( currentTimeMillis() );
                } else if ( o instanceof Date ) {
                    theVal = (Date) o;
                }
                if ( theVal != null ) {
                    if ( time.getNearestValue() ) {
                        Object nearest = time.getNearestValue( theVal );
                        if ( !nearest.equals( theVal ) ) {
                            theVal = (Date) nearest;
                            warnings.add( "99 Nearest value used: time=" + formatISO8601DateWOMS( theVal ) + " "
                                          + time.getUnits() );
                        }
                    }
                    Literal<PrimitiveValue> lit = new Literal<PrimitiveValue>( formatISO8601DateWOMS( theVal ) );
                    os[i++] = new PropertyIsEqualTo( property, lit, false );
                }
            }
            if ( os.length > 1 ) {
                if ( !time.getMultipleValues() ) {
                    throw new InvalidDimensionValue( "time", vals.toString() );
                }
                try {
                    ops.add( new Or( os ) );
                } catch ( Exception e ) {
                    // will not happen, look at the if condition
                }
            } else {
                ops.add( os[0] );
            }
        }

        for ( String name : dimensions.keySet() ) {
            Dimension<Object> dim = dimensions.get( name );
            final PropertyName property = new PropertyName( dim.getPropertyName() );

            List<?> vals = dims.get( name );

            if ( vals == null ) {
                vals = dim.getDefaultValue();
                if ( vals == null ) {
                    throw new MissingDimensionValue( name );
                }
                String units = dim.getUnits();
                if ( name.equals( "elevation" ) ) {
                    warnings.add( "99 Default value used: elevation=" + formatDimensionValueList( vals, false ) + " "
                                  + ( units == null ? "m" : units ) );
                } else if ( name.equals( "time" ) ) {
                    warnings.add( "99 Default value used: time=" + formatDimensionValueList( vals, true ) + " "
                                  + ( units == null ? "ISO8601" : units ) );
                } else {
                    warnings.add( "99 Default value used: DIM_" + name + "=" + formatDimensionValueList( vals, false )
                                  + " " + units );
                }
            }

            Operator[] os = new Operator[vals.size()];
            int i = 0;

            for ( Object o : vals ) {

                if ( !dim.getNearestValue() && !dim.isValid( o ) ) {
                    throw new InvalidDimensionValue( name, o.toString() );
                }

                if ( o instanceof DimensionInterval<?, ?, ?> ) {
                    DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
                    final String min;
                    if ( iv.min instanceof Date ) {
                        min = formatISO8601Date( (Date) iv.min );
                    } else {
                        min = ( (Number) iv.min ).toString();
                    }
                    final String max;
                    if ( iv.max instanceof Date ) {
                        max = formatISO8601Date( (Date) iv.max );
                    } else if ( iv.max instanceof String ) {
                        max = formatISO8601Date( new Date() );
                    } else {
                        max = ( (Number) iv.max ).toString();
                    }
                    os[i++] = new PropertyIsBetween( property, new Literal<PrimitiveValue>( min ),
                                                     new Literal<PrimitiveValue>( max ), false );
                } else {
                    if ( dim.getNearestValue() ) {
                        Object nearest = dim.getNearestValue( o );
                        if ( !nearest.equals( o ) ) {
                            o = nearest;
                            if ( "elevation".equals( name ) ) {
                                warnings.add( "99 Nearest value used: elevation=" + o + " " + dim.getUnits() );
                            } else {
                                warnings.add( "99 Nearest value used: DIM_" + name + "=" + o + " " + dim.getUnits() );
                            }
                        }
                    }
                    os[i++] = new PropertyIsEqualTo( new PropertyName( dim.getPropertyName() ),
                                                     new Literal<PrimitiveValue>( o.toString() ), false );
                }
            }
            if ( os.length > 1 ) {
                if ( !dim.getMultipleValues() ) {
                    throw new InvalidDimensionValue( "elevation", vals.toString() );
                }
                ops.add( new Or( os ) );
            } else {
                ops.add( os[0] );
            }
        }

        if ( ops.isEmpty() ) {
            return null;
        }
        if ( ops.size() > 1 ) {
            final OperatorFilter filter = new OperatorFilter( new And( ops.toArray( new Operator[ops.size()] ) ) );
            return new Pair<Filter, LinkedList<String>>( filter, warnings );
        }
        return new Pair<Filter, LinkedList<String>>( new OperatorFilter( ops.get( 0 ) ), warnings );
    }

    @Override
    public FeatureType getFeatureType() {
        return datastore.getSchema().getFeatureTypes()[0];
    }

    @Override
    public String toString() {
        return getName();
    }

}
