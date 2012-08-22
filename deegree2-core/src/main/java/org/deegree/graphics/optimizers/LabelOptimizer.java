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
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.Theme;
import org.deegree.graphics.displayelements.DisplayElement;
import org.deegree.graphics.displayelements.Label;
import org.deegree.graphics.displayelements.LabelDisplayElement;
import org.deegree.graphics.displayelements.ScaledFeature;
import org.deegree.graphics.sld.TextSymbolizer;
import org.deegree.graphics.transformation.GeoTransform;

/**
 * Selects the optimal <code>Label</code>s (graphical representations generated from
 * <code>LabelDisplayElements</code>) with respect to the amount of overlapping.
 * <p>
 * The labeling and optimization approach uses ideas from papers by Ingo Petzold on automated label placement.
 * <p>
 * TODO: The handling of rotated labels is currently broken. Don't use rotated <code>LabelDisplayElement</code>s with
 * this optimizer at the moment!
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LabelOptimizer extends AbstractOptimizer {

    private static final ILogger LOG = LoggerFactory.getLogger( LabelOptimizer.class );

    // contains the LabelDisplayElements that are to be optimized
    private ArrayList<DisplayElement> displayElements = new ArrayList<DisplayElement>( 1000 );

    // contains the LabelChoices
    private ArrayList<LabelChoice> choices = new ArrayList<LabelChoice>( 1000 );

    // collision matrix of LabelChoices that may collide
    private boolean[][] candidates;

    /**
     * Creates a new instance of {@link LabelOptimizer} with no associated {@link Theme}s.
     */
    public LabelOptimizer() {
        // nothing to do
    }

    /**
     * Creates a new instance of {@link LabelOptimizer} for the given {@link Theme}s.
     *
     * @param themes
     */
    public LabelOptimizer( Theme[] themes ) {

        // collect all LabelDisplayElements from all Themes
        for ( int i = 0; i < themes.length; i++ ) {
            addTheme( themes[i] );
        }
    }

    @Override
    public void addTheme( Theme theme ) {
        if ( !themes.contains( theme ) ) {
            List<DisplayElement> themeElements = theme.getDisplayElements();
            for ( int i = 0; i < themeElements.size(); i++ ) {
                Object o = themeElements.get( i );
                if ( o instanceof LabelDisplayElement ) {
                    LabelDisplayElement element = (LabelDisplayElement) o;
                    TextSymbolizer symbolizer = (TextSymbolizer) element.getSymbolizer();
                    // only add element if "auto" is set
                    if ( symbolizer.getLabelPlacement() != null ) {
                        if ( symbolizer.getLabelPlacement().getPointPlacement() != null
                             && symbolizer.getLabelPlacement().getPointPlacement().isAuto() ) {
                            displayElements.add( (LabelDisplayElement) o );
                            // } else if (symbolizer.getLabelPlacement().getLinePlacement() != null)
                            // {
                            // displayElements.add (o);
                        }
                    }
                }
            }
            themes.add( theme );
        }
    }

    /**
     * Finds optimized {@link Label}s for the registered {@link LabelDisplayElement}s.
     *
     * @param g
     */
    public void optimize( Graphics2D g )
                            throws Exception {

        choices.clear();
        double scale = mapView.getScale( g );
        GeoTransform projection = mapView.getProjection();

        // used to signal the LabelDisplayElement that it should
        // not create Labels itself (in any case)
        Label[] dummyLabels = new Label[0];

        // collect LabelChoices for all LabelDisplayElements
        for ( int i = 0; i < displayElements.size(); i++ ) {
            LabelDisplayElement element = (LabelDisplayElement) displayElements.get( i );
            if ( !element.doesScaleConstraintApply( scale ) )
                continue;

            element.setLabels( dummyLabels );
            ( (ScaledFeature) element.getFeature() ).setScale( scale / mapView.getPixelSize() );
            choices.addAll( LabelChoiceFactory.createLabelChoices( element, g, projection ) );
        }

        buildCollisionMatrix();

        // do the magic
        try {
            anneal();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // sets the optimized labels to the LabelDisplayElements, so
        // they are considered when the DisplayElements are painted the next
        // time

        for ( int i = 0; i < choices.size(); i++ ) {
            LabelChoice choice = choices.get( i );
            choice.getElement().addLabel( choice.getSelectedLabel() );
        }
    }

    /**
     * Builds the collision matrix for all <code>LabelChoice</code>s.
     */
    private void buildCollisionMatrix() {
        long now = System.currentTimeMillis();
        candidates = new boolean[choices.size()][choices.size()];
        for ( int i = 0; i < choices.size(); i++ ) {
            LabelChoice choice1 = choices.get( i );
            for ( int j = i + 1; j < choices.size(); j++ ) {
                LabelChoice choice2 = choices.get( j );
                if ( choice1.intersects( choice2 ) ) {
                    candidates[i][j] = true;
                }
            }
        }
        LOG.logDebug( "Building of collision matrix took: " + ( System.currentTimeMillis() - now ) + " millis." );
    }

    /**
     * Performs "Simulated Annealing" on the {@link LabelChoice} combination.
     */
    private void anneal() {
        double currentValue = objectiveFunction();

        double temperature = 1.0;
        int counter = 0;
        int successCounter = 0;
        int failCounter = 0;

        int n = choices.size();

        LOG.logDebug( "Starting Annealing with value: " + currentValue );
        long now = System.currentTimeMillis();
        while ( counter <= 2500 && currentValue > ( n + 0.8 * 40 ) ) {

            counter++;
            if ( successCounter % 5 == 0 ) {
                temperature *= 0.9;
            }

            // choose one Label from one LabelChoice randomly
            int choiceIndex = (int) ( Math.random() * ( n - 1 ) + 0.5 );
            LabelChoice choice = choices.get( choiceIndex );
            int oldPos = choice.getSelected();
            choice.selectLabelRandomly();

            double value = objectiveFunction();

            // does the new placement imply an improvement?
            if ( value < currentValue ) {
                // yes -> keep it
                currentValue = value;
                successCounter++;
                failCounter = 0;
            } else {
                // no -> only keep it with a certain probability
                if ( Math.random() < temperature ) {
                    currentValue = value;
                    failCounter = 0;
                } else {
                    // change it back to the old placement
                    choice.setSelected( oldPos );
                    failCounter++;
                }
            }
        }
        LOG.logDebug( "Final value: " + currentValue );
        LOG.logDebug( "Annealing took: " + ( System.currentTimeMillis() - now ) + " millis." );
    }

    /**
     * Calculates the quality value for the currently selected combination of {@link Label}s.
     *
     * @return quality of currently selected combination of {@link Label}s
     */
    private double objectiveFunction() {
        float value = 0.0f;

        for ( int i = 0; i < choices.size(); i++ ) {
            LabelChoice choice1 = choices.get( i );
            Label label1 = choice1.getSelectedLabel();
            value += choice1.getQuality() + 1.0f;

            for ( int j = i + 1; j < choices.size(); j++ ) {
                if ( candidates[i][j] ) {
                    LabelChoice choice2 = choices.get( j );
                    Label label2 = choice2.getSelectedLabel();
                    if ( label1.intersects( label2 ) ) {
                        value += 40.0f;
                    }
                }
            }
        }
        return value;
    }
}
