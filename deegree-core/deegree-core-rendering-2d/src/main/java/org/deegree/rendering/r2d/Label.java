/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d;

import java.awt.Font;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;

import org.deegree.style.styling.TextStyling;

/**
 * A rectangular text label, ready to be drawn
 *
 * @author Florian Bingel
 */

public class Label {

	public TextLayout mLayout;

	public TextStyling mStyling;

	public Font mFont;

	public String mText;

	public Point2D.Double mOrigin;

	public Point2D.Double mDrawPosition;

	Label(TextLayout pLayout, TextStyling pStyling, Font pFont, String pText, Point2D.Double pOrigin,
			RendererContext context) {
		mLayout = pLayout;
		mStyling = pStyling;
		mFont = pFont;
		mText = pText;
		mOrigin = pOrigin;

		double ox = mOrigin.x + context.uomCalculator.considerUOM(mStyling.displacementX, mStyling.uom);
		double oy = mOrigin.y - context.uomCalculator.considerUOM(mStyling.displacementY, mStyling.uom);

		double px = ox - (mStyling.anchorPointX * mLayout.getBounds().getWidth());
		double py = oy + (mStyling.anchorPointY * mLayout.getBounds().getHeight());
		mDrawPosition = new Point2D.Double(px, py);

	}

	public TextLayout getLayout() {
		return mLayout;
	}

	public TextStyling getStyling() {
		return mStyling;
	}

	public Font getFont() {
		return mFont;
	}

	public String getText() {
		return mText;
	}

	public Point2D.Double getOrigin() {
		return mOrigin;
	}

	public Point2D.Double getDrawPosition() {
		return mDrawPosition;
	}

	public void setDrawPosition(Point2D.Double dp) {
		mDrawPosition = dp;
	}

}
