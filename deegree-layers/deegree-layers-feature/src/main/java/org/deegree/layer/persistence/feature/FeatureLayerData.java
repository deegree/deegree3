//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.layer.persistence.feature;

import static org.deegree.gml.GMLVersion.GML_31;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.ThreadedFeatureInputStream;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Geometry;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureLayerData implements LayerData {

    private static final Logger LOG = getLogger( FeatureLayerData.class );

    private int maxFeatures;

    private final Style style;

    private FeatureXPathEvaluator evaluator;

    private final List<Query> queries;

    private final FeatureStore featureStore;

    public FeatureLayerData( List<Query> queries, FeatureStore featureStore, int maxFeatures, Style style ) {
        this.queries = queries;
        this.featureStore = featureStore;
        this.maxFeatures = maxFeatures;
        this.style = style;
        evaluator = new FeatureXPathEvaluator( GML_31 );
    }

    @Override
    public void render( RenderContext context ) {
        FeatureInputStream features = null;
        try {
            // TODO Should this always be done on this level? What about min and maxFill values?
            features = featureStore.query( queries.toArray( new Query[queries.size()] ) );
            features = new ThreadedFeatureInputStream( features, 100, 20 );
            int cnt = 0;

            Renderer renderer = context.getVectorRenderer();
            TextRenderer textRenderer = context.getTextRenderer();

            for ( Feature f : features ) {
                try {
                    LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evalds = style.evaluate( f, evaluator );
                    for ( Triple<Styling, LinkedList<Geometry>, String> evald : evalds ) {
                        if ( evald.first instanceof TextStyling ) {
                            textRenderer.render( (TextStyling) evald.first, evald.third, evald.second );
                        } else {
                            renderer.render( evald.first, evald.second );
                        }
                    }
                } catch ( Throwable e ) {
                    LOG.warn( "Unable to render feature, probably a curve had multiple/non-linear segments." );
                    LOG.warn( "Error message was: {}", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
                if ( maxFeatures > 0 && ++cnt == maxFeatures ) {
                    LOG.debug( "Reached max features of {} for layer '{}', stopping.", maxFeatures, this );
                    break;
                }
            }
        } catch ( FilterEvaluationException e ) {
            LOG.warn( "A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( Throwable e ) {
            LOG.warn( "Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( features != null ) {
                features.close();
            }
        }
    }

    @Override
    public FeatureCollection info() {
        // TODO Auto-generated method stub
        return null;
    }

}
