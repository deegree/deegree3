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
package org.deegree.graphics.optimizers;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.displayelements.Label;
import org.deegree.graphics.displayelements.LabelDisplayElement;
import org.deegree.graphics.displayelements.LabelFactory;
import org.deegree.graphics.sld.LabelPlacement;
import org.deegree.graphics.sld.LinePlacement;
import org.deegree.graphics.sld.PointPlacement;
import org.deegree.graphics.sld.TextSymbolizer;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;

/**
 * Factory for {@link LabelChoice} objects.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LabelChoiceFactory {

    private static ILogger LOG = LoggerFactory.getLogger( LabelChoiceFactory.class );

    /**
     * Determines {@link LabelChoice} candidates for the given {@link LabelDisplayElement}.
     * 
     * @param element
     * @param g
     * @param projection
     * @return possible {@link LabelChoice}s
     */
    static ArrayList<LabelChoice> createLabelChoices( LabelDisplayElement element, Graphics2D g, GeoTransform projection ) {

        ArrayList<LabelChoice> choices = new ArrayList<LabelChoice>();

        try {
            Feature feature = element.getFeature();
            String caption = element.getLabel().evaluate( feature );

            // sanity check: empty labels are ignored
            if ( caption == null || caption.trim().equals( "" ) ) {
                return choices;
            }

            Geometry geometry = element.getGeometry();
            TextSymbolizer symbolizer = (TextSymbolizer) element.getSymbolizer();

            // gather font information
            org.deegree.graphics.sld.Font sldFont = symbolizer.getFont();
            java.awt.Font font = new java.awt.Font( sldFont.getFamily( feature ), sldFont.getStyle( feature )
                                                                                  | sldFont.getWeight( feature ),
                                                    sldFont.getSize( feature ) );
            g.setFont( font );
            FontRenderContext frc = g.getFontRenderContext();
            Rectangle2D bounds = font.getStringBounds( caption, frc );
            LineMetrics metrics = font.getLineMetrics( caption, frc );
            int w = (int) bounds.getWidth();
            int h = (int) bounds.getHeight();
            // int descent = (int) metrics.getDescent ();

            LabelPlacement lPlacement = symbolizer.getLabelPlacement();

            // element is associated to a point geometry
            if ( geometry instanceof Point ) {

                // get screen coordinates
                int[] coords = LabelFactory.calcScreenCoordinates( projection, geometry );
                int x = coords[0];
                int y = coords[1];

                // use placement information from SLD
                PointPlacement pPlacement = lPlacement.getPointPlacement();
                // double [] anchorPoint = pPlacement.getAnchorPoint( feature );
                double[] displacement = pPlacement.getDisplacement( feature );
                double rotation = pPlacement.getRotation( feature );

                Label[] labels = new Label[8];
                double opacity = symbolizer.getFill() == null ? 1 : symbolizer.getFill().getOpacity( feature );
                labels[0] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 0.0,
                                                                                                                0.0 },
                                                      new double[] { displacement[0], displacement[1] }, opacity );
                labels[1] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 0.0,
                                                                                                                1.0 },
                                                      new double[] { displacement[0], -displacement[1] }, opacity );
                labels[2] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 1.0,
                                                                                                                1.0 },
                                                      new double[] { -displacement[0], -displacement[1] }, opacity );
                labels[3] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 1.0,
                                                                                                                0.0 },
                                                      new double[] { -displacement[0], displacement[1] }, opacity );
                labels[4] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 0.0,
                                                                                                                0.5 },
                                                      new double[] { displacement[0], 0 }, opacity );
                labels[5] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 0.5,
                                                                                                                1.0 },
                                                      new double[] { 0, -displacement[1] }, opacity );
                labels[6] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 1.0,
                                                                                                                0.5 },
                                                      new double[] { -displacement[0], 0 }, opacity );
                labels[7] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                      symbolizer.getHalo(), x, y, w, h, rotation, new double[] { 0.5,
                                                                                                                0.0 },
                                                      new double[] { 0, displacement[1] }, opacity );
                float[] qualities = new float[] { 0.0f, 0.5f, 0.33f, 0.27f, 0.15f, 1.0f, 0.1f, 0.7f };
                choices.add( new LabelChoice( element, labels, qualities, 0, labels[1].getMaxX(), labels[1].getMaxY(),
                                              labels[3].getMinX(), labels[3].getMinY() ) );

                // element is associated to a polygon geometry
            } else if ( geometry instanceof Surface || geometry instanceof MultiSurface ) {

                // get screen coordinates
                int[] coords = LabelFactory.calcScreenCoordinates( projection, geometry );
                int x = coords[0];
                int y = coords[1];

                // use placement information from SLD
                PointPlacement pPlacement = lPlacement.getPointPlacement();
                // double [] anchorPoint = pPlacement.getAnchorPoint( feature );
                // double [] displacement = pPlacement.getDisplacement( feature );
                double rotation = pPlacement.getRotation( feature );

                // center label within the intersection of the screen surface and the polygon
                // geometry
                Surface screenSurface = GeometryFactory.createSurface( projection.getSourceRect(), null );
                Geometry intersection = null;

                try {
                    intersection = screenSurface.intersection( geometry );
                } catch ( Exception e ) {
                    LOG.logDebug( "no intersection could be calculated because objects are to small" );
                }

                if ( intersection != null && intersection.getCentroid() != null ) {
                    Position source = intersection.getCentroid().getPosition();
                    x = (int) ( projection.getDestX( source.getX() ) + 0.5 );
                    y = (int) ( projection.getDestY( source.getY() ) + 0.5 );
                    Label[] labels = new Label[3];
                    double opacity = 1;
                    if ( symbolizer.getFill() != null ) {
                        opacity = symbolizer.getFill().getOpacity( feature );
                    }
                    labels[0] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                          symbolizer.getHalo(), x, y, w, h, rotation,
                                                          new double[] { 0.5, 0.5 }, new double[] { 0, 0 }, opacity );
                    labels[1] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                          symbolizer.getHalo(), x, y, w, h, rotation,
                                                          new double[] { 0.5, 0.0 }, new double[] { 0, 0 }, opacity );
                    labels[2] = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics, feature,
                                                          symbolizer.getHalo(), x, y, w, h, rotation,
                                                          new double[] { 0.5, 1.0 }, new double[] { 0, 0 }, opacity );

                    float[] qualities = new float[] { 0.0f, 0.25f, 0.5f };
                    choices.add( new LabelChoice( element, labels, qualities, 0, labels[0].getMaxX(),
                                                  labels[2].getMaxY(), labels[0].getMinX(), labels[1].getMinY() ) );
                }

                // element is associated to a line geometry
            } else if ( geometry instanceof Curve || geometry instanceof MultiCurve ) {

                Surface screenSurface = GeometryFactory.createSurface( projection.getSourceRect(), null );
                Geometry intersection = screenSurface.intersection( geometry );

                if ( intersection != null ) {
                    ArrayList<LabelChoice> list = null;
                    if ( intersection instanceof Curve ) {
                        list = createLabelChoices( (Curve) intersection, element, g, projection );
                    } else if ( intersection instanceof MultiCurve ) {
                        list = createLabelChoices( (MultiCurve) intersection, element, g, projection );
                    } else {
                        throw new Exception( "Intersection produced unexpected geometry type: '"
                                             + intersection.getClass().getName() + "'!" );
                    }
                    choices = list;
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return choices;
    }

    /**
     * Determines {@link LabelChoice} candidates for the given {@link MultiCurve} where a label could be drawn.
     * <p>
     * Three candidates are generated:
     * <ul>
     * <li>on the line</li>
     * <li>above it</li>
     * <li>below</li>
     * </ul>
     * 
     * @param multiCurve
     * @param element
     * @param g
     * @param projection
     * @return ArrayList containing <tt>LabelChoice</tt>-objects
     * @throws FilterEvaluationException
     */
    static ArrayList<LabelChoice> createLabelChoices( MultiCurve multiCurve, LabelDisplayElement element, Graphics2D g,
                                                      GeoTransform projection )
                            throws FilterEvaluationException {

        ArrayList<LabelChoice> choices = new ArrayList<LabelChoice>( 1000 );
        for ( int i = 0; i < multiCurve.getSize(); i++ ) {
            Curve curve = multiCurve.getCurveAt( i );
            choices.addAll( createLabelChoices( curve, element, g, projection ) );
        }
        return choices;
    }

    /**
     * Determines <code>LabelChoice</code>s for the given <code>Curve</code> where a <code>Label</code> could be drawn.
     * <p>
     * Three candidates are generated:
     * <ul>
     * <li>on the line</li>
     * <li>above it</li>
     * <li>below</li>
     * </ul>
     * </li>
     * 
     * @param curve
     * @param element
     * @param g
     * @param projection
     * @return ArrayList containing <tt>LabelChoice</tt>-objects
     * @throws FilterEvaluationException
     */
    static ArrayList<LabelChoice> createLabelChoices( Curve curve, LabelDisplayElement element, Graphics2D g,
                                                      GeoTransform projection )
                            throws FilterEvaluationException {

        Feature feature = element.getFeature();

        // determine the placement type and parameters from the TextSymbolizer
        double perpendicularOffset = 0.0;
        int placementType = LinePlacement.TYPE_ABSOLUTE;
        double lineWidth = 3.0;
        int gap = 6;
        TextSymbolizer symbolizer = ( (TextSymbolizer) element.getSymbolizer() );
        if ( symbolizer.getLabelPlacement() != null ) {
            LinePlacement linePlacement = symbolizer.getLabelPlacement().getLinePlacement();
            if ( linePlacement != null ) {
                placementType = linePlacement.getPlacementType( element.getFeature() );
                perpendicularOffset = linePlacement.getPerpendicularOffset( element.getFeature() );
                lineWidth = linePlacement.getLineWidth( element.getFeature() );
                gap = linePlacement.getGap( element.getFeature() );
            }
        }

        // get width & height of the caption
        String caption = element.getLabel().evaluate( element.getFeature() );
        org.deegree.graphics.sld.Font sldFont = symbolizer.getFont();
        java.awt.Font font = new java.awt.Font( sldFont.getFamily( element.getFeature() ),
                                                sldFont.getStyle( element.getFeature() )
                                                                        | sldFont.getWeight( element.getFeature() ),
                                                sldFont.getSize( element.getFeature() ) );
        g.setFont( font );
        FontRenderContext frc = g.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds( caption, frc );
        LineMetrics metrics = font.getLineMetrics( caption, frc );
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        // get screen coordinates of the line
        int[][] pos = LabelFactory.calcScreenCoordinates( projection, curve );

        // ideal distance from the line
        double delta = height / 2.0 + lineWidth / 2.0;

        // walk along the linestring and "collect" possible label positions
        int w = (int) width;
        int lastX = pos[0][0];
        int lastY = pos[1][0];
        int count = pos[2][0];
        int boxStartX = lastX;
        int boxStartY = lastY;

        ArrayList<LabelChoice> choices = new ArrayList<LabelChoice>( 1000 );
        ArrayList<int[]> eCandidates = new ArrayList<int[]>( 100 );
        int i = 0;
        int kk = 0;
        while ( i < count && kk < 100 ) {
            kk++;
            int x = pos[0][i];
            int y = pos[1][i];

            // segment found where endpoint of label should be located?
            if ( LabelFactory.getDistance( boxStartX, boxStartY, x, y ) >= w ) {

                int[] p0 = new int[] { boxStartX, boxStartY };
                int[] p1 = new int[] { lastX, lastY };
                int[] p2 = new int[] { x, y };

                int[] p = LabelFactory.findPointWithDistance( p0, p1, p2, w );
                x = p[0];
                y = p[1];

                lastX = x;
                lastY = y;
                int boxEndX = x;
                int boxEndY = y;

                // does the linesegment run from right to left?
                if ( x <= boxStartX ) {
                    boxEndX = boxStartX;
                    boxEndY = boxStartY;
                    boxStartX = x;
                    boxStartY = y;
                    x = boxEndX;
                    y = boxEndY;
                }

                double rotation = LabelFactory.getRotation( boxStartX, boxStartY, x, y );
                double[] deviation = LabelFactory.calcDeviation( new int[] { boxStartX, boxStartY },
                                                                 new int[] { boxEndX, boxEndY }, eCandidates );

                switch ( placementType ) {
                case LinePlacement.TYPE_ABSOLUTE: {
                    double opacity = symbolizer.getFill() == null ? 1 : symbolizer.getFill().getOpacity( feature );
                    Label label = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics,
                                                            feature, symbolizer.getHalo(), boxStartX, boxStartY,
                                                            (int) width, (int) height, rotation, new double[] { 0.0,
                                                                                                               0.5 },
                                                            new double[] { ( w - width ) / 2, perpendicularOffset },
                                                            opacity );
                    choices.add( new LabelChoice( element, new Label[] { label }, new float[] { 0.0f }, 0,
                                                  label.getMaxX(), label.getMaxY(), label.getMinX(), label.getMinY() ) );
                    break;
                }
                case LinePlacement.TYPE_ABOVE: {
                    double opacity = symbolizer.getFill() == null ? 1 : symbolizer.getFill().getOpacity( feature );
                    Label upperLabel = LabelFactory.createLabel(
                                                                 caption,
                                                                 font,
                                                                 sldFont.getColor( feature ),
                                                                 metrics,
                                                                 feature,
                                                                 symbolizer.getHalo(),
                                                                 boxStartX,
                                                                 boxStartY,
                                                                 (int) width,
                                                                 (int) height,
                                                                 rotation,
                                                                 new double[] { 0.0, 0.5 },
                                                                 new double[] { ( w - width ) / 2, delta + deviation[0] },
                                                                 opacity );
                    choices.add( new LabelChoice( element, new Label[] { upperLabel }, new float[] { 0.0f }, 0,
                                                  upperLabel.getMaxX(), upperLabel.getMaxY(), upperLabel.getMinX(),
                                                  upperLabel.getMinY() ) );
                    break;
                }
                case LinePlacement.TYPE_BELOW: {
                    double opacity = symbolizer.getFill() == null ? 1 : symbolizer.getFill().getOpacity( feature );
                    Label lowerLabel = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics,
                                                                 feature, symbolizer.getHalo(), boxStartX, boxStartY,
                                                                 (int) width, (int) height, rotation,
                                                                 new double[] { 0.0, 0.5 },
                                                                 new double[] { ( w - width ) / 2,
                                                                               -delta - deviation[1] }, opacity );
                    choices.add( new LabelChoice( element, new Label[] { lowerLabel }, new float[] { 0.0f }, 0,
                                                  lowerLabel.getMaxX(), lowerLabel.getMaxY(), lowerLabel.getMinX(),
                                                  lowerLabel.getMinY() ) );
                    break;
                }
                case LinePlacement.TYPE_CENTER: {
                    double opacity = symbolizer.getFill() == null ? 1 : symbolizer.getFill().getOpacity( feature );
                    Label centerLabel = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics,
                                                                  feature, symbolizer.getHalo(), boxStartX, boxStartY,
                                                                  (int) width, (int) height, rotation,
                                                                  new double[] { 0.0, 0.5 },
                                                                  new double[] { ( w - width ) / 2, 0.0 }, opacity );
                    choices.add( new LabelChoice( element, new Label[] { centerLabel }, new float[] { 0.0f }, 0,
                                                  centerLabel.getMaxX(), centerLabel.getMaxY(), centerLabel.getMinX(),
                                                  centerLabel.getMinY() ) );
                    break;
                }
                case LinePlacement.TYPE_AUTO: {
                    double opacity = symbolizer.getFill() == null ? 1 : symbolizer.getFill().getOpacity( feature );
                    Label upperLabel = LabelFactory.createLabel(
                                                                 caption,
                                                                 font,
                                                                 sldFont.getColor( feature ),
                                                                 metrics,
                                                                 feature,
                                                                 symbolizer.getHalo(),
                                                                 boxStartX,
                                                                 boxStartY,
                                                                 (int) width,
                                                                 (int) height,
                                                                 rotation,
                                                                 new double[] { 0.0, 0.5 },
                                                                 new double[] { ( w - width ) / 2, delta + deviation[0] },
                                                                 opacity );
                    Label lowerLabel = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics,
                                                                 feature, symbolizer.getHalo(), boxStartX, boxStartY,
                                                                 (int) width, (int) height, rotation,
                                                                 new double[] { 0.0, 0.5 },
                                                                 new double[] { ( w - width ) / 2,
                                                                               -delta - deviation[1] }, opacity );
                    Label centerLabel = LabelFactory.createLabel( caption, font, sldFont.getColor( feature ), metrics,
                                                                  feature, symbolizer.getHalo(), boxStartX, boxStartY,
                                                                  (int) width, (int) height, rotation,
                                                                  new double[] { 0.0, 0.5 },
                                                                  new double[] { ( w - width ) / 2, 0.0 }, opacity );
                    choices.add( new LabelChoice( element, new Label[] { lowerLabel, upperLabel, centerLabel },
                                                  new float[] { 0.0f, 0.25f, 1.0f }, 0, centerLabel.getMaxX(),
                                                  lowerLabel.getMaxY(), centerLabel.getMinX(), upperLabel.getMinY() ) );
                    break;
                }
                default: {
                    assert false;
                }
                }

                boxStartX = lastX;
                boxStartY = lastY;
                eCandidates.clear();
            } else {
                eCandidates.add( new int[] { x, y } );
                lastX = x;
                lastY = y;
                i++;
            }
        }

        // pick LabelChoices on the linestring
        ArrayList<LabelChoice> pick = new ArrayList<LabelChoice>( choices.size() );
        int n = choices.size();
        for ( int j = n / 2; j < choices.size(); j += ( gap + 1 ) ) {
            pick.add( choices.get( j ) );
        }
        for ( int j = n / 2 - ( gap + 1 ); j > 0; j -= ( gap + 1 ) ) {
            pick.add( choices.get( j ) );
        }
        return pick;
    }
}
