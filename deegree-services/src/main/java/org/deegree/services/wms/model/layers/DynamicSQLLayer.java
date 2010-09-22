//$HeadURL$
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
package org.deegree.services.wms.model.layers;

import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.simplesql.SimpleSQLFeatureStore;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.spatial.Intersects;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.se.parser.PostgreSQLReader;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.wms.WMSException.InvalidDimensionValue;
import org.deegree.services.wms.WMSException.MissingDimensionValue;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.slf4j.Logger;

/**
 * <code>DynamicSQLLayer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces", debug = "logs information about missing/invalid symbol codes")
public class DynamicSQLLayer extends Layer {

    private static final Logger LOG = getLogger( DynamicSQLLayer.class );

    private final Collection<Integer> symbolCodes;

    private final QName symbolField;

    private PostgreSQLReader styles;

    private SimpleSQLFeatureStore datastore;

    /**
     * @param name
     * @param title
     * @param parent
     * @param datastore
     * @param styles
     * @param symbolCodes
     * @param symbolField
     */
    public DynamicSQLLayer( String name, String title, Layer parent, SimpleSQLFeatureStore datastore,
                            PostgreSQLReader styles, Collection<Integer> symbolCodes, String symbolField ) {
        super( name, title, parent );
        this.datastore = datastore;
        this.styles = styles;
        this.symbolCodes = symbolCodes;
        this.symbolField = symbolField == null ? null
                                              : new QName( datastore.getFeatureType().getName().getNamespaceURI(),
                                                           symbolField );
    }

    @Override
    public Envelope getBbox() {
        return datastore.getEnvelope( getFeatureType().getName() );
    }

    @Override
    public FeatureType getFeatureType() {
        return datastore.getFeatureType();
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        Envelope clickBox = fi.getClickBox();
        FeatureResultSet rs = null;
        try {
            GenericFeatureType ft = datastore.getFeatureType();
            PropertyName propName = new PropertyName( ft.getDefaultGeometryPropertyDeclaration().getName() );
            OperatorFilter fil = new OperatorFilter( new Intersects( propName, clickBox ) );
            rs = datastore.query( new Query( ft.getName(), clickBox, fil, -1, fi.getFeatureCount(), -1 ) );
            FeatureCollection col = rs.toCollection();
            return new Pair<FeatureCollection, LinkedList<String>>( col, new LinkedList<String>() );
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
        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

    @Override
    public LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        Java2DRenderer renderer = new Java2DRenderer( g, gm.getWidth(), gm.getHeight(), gm.getBoundingBox(),
                                                      gm.getPixelSize() );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        // TODO
        FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

        LinkedList<Style> defStyles = new LinkedList<Style>();
        for ( Integer code : symbolCodes ) {
            defStyles.add( styles.getStyle( code ) );
        }

        Integer maxFeats = gm.getMaxFeatures().get( this );
        int maxFeatures = maxFeats == null ? -1 : maxFeats;
        FeatureResultSet rs = null;
        final double resolution = gm.getResolution();
        try {
            rs = datastore.query( new Query( datastore.getFeatureType().getName(), gm.getBoundingBox(), null,
                                             round( gm.getScale() ), maxFeatures, resolution ) );

            for ( Feature f : rs ) {
                if ( style != null ) {
                    render( f, evaluator, style, renderer, textRenderer, gm.getScale(), resolution );
                }
                boolean painted = false;
                if ( symbolField != null ) {
                    Property[] props = f.getProperties( symbolField );
                    if ( props != null && props.length > 0 ) {
                        for ( Property p : props ) {
                            TypedObjectNode n = p.getValue();
                            if ( n instanceof PrimitiveValue ) {
                                String[] ss = ( (PrimitiveValue) n ).getAsText().split( "," );
                                for ( String s : ss ) {
                                    try {
                                        Style sty = styles.getStyle( Integer.parseInt( s ) );
                                        if ( sty != null ) {
                                            render( f, evaluator, sty, renderer, textRenderer, gm.getScale(),
                                                    resolution );
                                            painted = true;
                                        } else {
                                            LOG.debug( "Style with symbol code {} was not found in the database.", s );
                                        }
                                    } catch ( NumberFormatException e ) {
                                        LOG.debug( "Symbol code '{}' on a feature's property '{}' could not"
                                                   + " be parsed as integer.", s, symbolField.getLocalPart() );
                                        LOG.trace( "Stack trace:", e );
                                    }
                                }
                            }
                        }
                    } else {
                        LOG.debug( "The symbol field '{}' does not exist.", symbolField.getLocalPart() );
                    }
                }
                if ( !painted ) { // then use default (layer level) styles
                    for ( Style s : defStyles ) {
                        render( f, evaluator, s, renderer, textRenderer, gm.getScale(), resolution );
                        painted = true;
                    }
                }
                if ( !painted ) { // then use gray default style
                    render( f, evaluator, null, renderer, textRenderer, gm.getScale(), resolution );
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
        return new LinkedList<String>();
    }

    @Override
    public String toString() {
        return getName();
    }

}
