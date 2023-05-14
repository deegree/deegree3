/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.rendering.r2d.labelplacement;

import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import org.deegree.rendering.r2d.Label;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.RendererContext;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.style.utils.UomCalculator;

/**
 * <code>Automatic Label Placement, based on org.deegree.graphics.optimizers.LabelOptimizer from deegree2 </code>
 *
 * Selects an approximate optimal <code>Label</code>s position distribution with respect
 * to the amount of overlapping.
 * <p>
 * The labeling and optimization approach uses ideas from papers by Ingo Petzold on
 * automated label placement.
 * <p>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author Florian Bingel
 */

public class AutoLabelPlacement {

	private static final Logger LOG = getLogger(AutoLabelPlacement.class);

	private ArrayList<PointLabelPositionOptions> labelPositionsList;

	// collision matrix of PointLabelPositionOptions that may overlap
	private boolean[][] collisionMatrix;

	float placementQuality = 0.0f;

	int intersectionQuality = 0;

	/**
	 * Finds optimized {@link Label} positions for all Labels in the List. Labels should
	 * have {@link TextStyling}.auto set to true
	 * @param labelList List of Labels to optimize
	 */
	public AutoLabelPlacement(List<Label> labelList, Renderer renderer) throws Exception {

		UomCalculator uomCalculator = ((Java2DRenderer) renderer).rendererContext.uomCalculator;

		labelPositionsList = new ArrayList<PointLabelPositionOptions>();
		for (Label l : labelList) {
			if (l.getStyling().auto)
				labelPositionsList.add(new PointLabelPositionOptions(l, uomCalculator));
		}

		LOG.debug("Added " + labelPositionsList.size() + " Labels of " + labelList.size() + " to auto placement");

		if (labelPositionsList.size() > 1) {
			buildCollisionMatrix();

			// do the magic
			try {
				anneal();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			// Update labels with new position
			for (PointLabelPositionOptions l : labelPositionsList) {
				l.updateLabelPosition();
			}
		}
	}

	/**
	 * Performs "Simulated Annealing" on the array of {@link PointLabelPositionOptions}.
	 */
	private void anneal() {

		objectiveFunction();
		float currentQuality = placementQuality + intersectionQuality;

		double temperature = 1.0;
		int counter = 0;
		int successCounter = 0;
		// int failCounter = 0;
		java.util.Random rand = new java.util.Random();

		int n = labelPositionsList.size();

		LOG.debug("Starting Annealing with value: " + currentQuality + ", trying to reach: " + (n + 0.8 * 40));
		long now = System.currentTimeMillis();

		while (counter <= 2500 && currentQuality > (n + 0.8 * 40)) {

			counter++;
			if (successCounter == 5) {
				successCounter = 0;
				temperature *= 0.9;
			}

			// choose one Label from the list randomly
			int choiceIndex = rand.nextInt(n);
			// int choiceIndex = (int) ( Math.random() * ( n - 1 ) + 0.5 );
			PointLabelPositionOptions choice = labelPositionsList.get(choiceIndex);

			int oldPos = choice.getSelectedIndex();
			int oldIntersectionQuality = intersectionQuality;
			float oldPlacementQuality = placementQuality;

			updateChoiceAndQuality(choice, choiceIndex);

			float quality = placementQuality + intersectionQuality;

			// does the new placement imply an improvement?
			if (quality < currentQuality) {
				// yes -> keep it
				currentQuality = quality;
				successCounter++;
				// failCounter = 0;
			}
			else {
				// no -> only keep it with a certain probability
				if (Math.random() < temperature) {
					currentQuality = quality;
					// failCounter = 0;
				}
				else {
					// change it back to the old placement
					choice.select(oldPos);
					intersectionQuality = oldIntersectionQuality;
					placementQuality = oldPlacementQuality;
					// failCounter++;
				}
			}
		}

		long duration = System.currentTimeMillis() - now;

		LOG.debug("Final value: " + currentQuality + ", needed " + counter + " iterations");
		LOG.debug("Annealing took: " + duration + " ms, ( " + (int) ((double) duration / (double) counter * 1000)
				+ " µs per iteration  )");
	}

	/**
	 * Builds the collision matrix for all <code>PointLabelPositionOptions</code>.
	 */
	private void buildCollisionMatrix() {

		long now = System.currentTimeMillis();
		collisionMatrix = new boolean[labelPositionsList.size()][labelPositionsList.size()];

		for (int i = 0; i < labelPositionsList.size(); i++) {
			PointLabelPositionOptions choice1 = labelPositionsList.get(i);

			for (int j = i + 1; j < labelPositionsList.size(); j++) {
				PointLabelPositionOptions choice2 = labelPositionsList.get(j);
				collisionMatrix[i][j] = choice1.intersectsAny(choice2);
			}

		}

		LOG.debug("Building of collision matrix took: " + (System.currentTimeMillis() - now) + " millis.");
	}

	/**
	 * Updates the quality value for the currently selected combination of {@link Label}s
	 * and lets the label select a new random position
	 * @param changedLabel the label which is chosen to be changed
	 * @param choice the index of the choice in the labelPositionsList
	 */
	private void updateChoiceAndQuality(PointLabelPositionOptions changedLabel, int choice) {

		int changedLabelIntersectionQuality = 0;

		// calculate, how much the (to bee) changedLabel does contribute to the
		// intersectionQuality
		for (int i = 0; i < choice; i++) {
			PointLabelPositionOptions choice1 = labelPositionsList.get(i);
			if (collisionMatrix[i][choice] && (changedLabel.intersectsSelection(choice1))) {
				changedLabelIntersectionQuality += 40;
			}
		}

		for (int i = choice + 1; i < labelPositionsList.size(); i++) {
			PointLabelPositionOptions choice1 = labelPositionsList.get(i);
			if (collisionMatrix[choice][i] && (changedLabel.intersectsSelection(choice1))) {
				changedLabelIntersectionQuality += 40;
			}
		}

		// subtract the qualities of the label which is going to be changed
		intersectionQuality -= changedLabelIntersectionQuality;
		placementQuality -= changedLabel.getQuality();

		// select a new position randomly
		changedLabel.selectLabelPositionRandomly();

		// calculate, how much the changedLabel does contribute to the intersectionQuality
		changedLabelIntersectionQuality = 0;
		for (int i = 0; i < choice; i++) {
			PointLabelPositionOptions choice1 = labelPositionsList.get(i);
			if (collisionMatrix[i][choice] && (changedLabel.intersectsSelection(choice1))) {
				changedLabelIntersectionQuality += 40;
			}
		}

		for (int i = choice + 1; i < labelPositionsList.size(); i++) {
			PointLabelPositionOptions choice1 = labelPositionsList.get(i);
			if (collisionMatrix[choice][i] && (changedLabel.intersectsSelection(choice1))) {
				changedLabelIntersectionQuality += 40;
			}
		}

		// add the new qualities
		intersectionQuality += changedLabelIntersectionQuality;
		placementQuality += changedLabel.getQuality();
	}

	/**
	 * Calculates the initial quality values for the currently selected combination of
	 * {@link Label}s.
	 *
	 */
	private void objectiveFunction() {

		placementQuality = 0.0f;
		intersectionQuality = 0;

		for (int i = 0; i < labelPositionsList.size(); i++) {
			PointLabelPositionOptions choice1 = labelPositionsList.get(i);
			// placementQuality += choice1.getQuality() + 1.0f;
			placementQuality += choice1.getQuality();

			for (int j = i + 1; j < labelPositionsList.size(); j++) {
				if (collisionMatrix[i][j]) {
					PointLabelPositionOptions choice2 = labelPositionsList.get(j);
					if (choice1.intersectsSelection(choice2)) {
						intersectionQuality += 40;
					}
				}
			}
		}
	}

}
