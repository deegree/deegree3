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
/*
 Copyright 2006 Jerry Huxtable

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.deegree.rendering.r2d.strokes;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static org.deegree.commons.utils.math.MathUtils.round;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.deegree.style.styling.components.Halo;
import org.deegree.style.styling.components.LinePlacement;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.UomCalculator;

/**
 * <code>HaloStroke</code> drawing a halo around a text.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class HaloStroke extends TextStroke {

	private final Halo halo;

	private final UOM uom;

	private final UomCalculator uomCalculator;

	public HaloStroke(String text, Font font, LinePlacement linePlacement, Halo halo, UOM uom,
			UomCalculator uomCalculator) {
		super(text, font, linePlacement);
		this.halo = halo;
		this.uom = uom;
		this.uomCalculator = uomCalculator;
	}

	@Override
	protected void appendShape(GeneralPath result, Shape transformedShape) {
		BasicStroke stroke = new BasicStroke(round(2 * uomCalculator.considerUOM(halo.radius, uom)), CAP_BUTT,
				JOIN_ROUND);

		Shape haloShape = stroke.createStrokedShape(transformedShape);
		result.append(haloShape, false);
	}

}