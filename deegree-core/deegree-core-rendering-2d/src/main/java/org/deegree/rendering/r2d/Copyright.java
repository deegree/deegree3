/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.rendering.r2d;

import java.awt.image.BufferedImage;

/**
 * Encapsulates a copyright image or text.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Copyright {

	private final String copyrightText;

	private final BufferedImage copyrightImage;

	private final int offsetX;

	private final int offsetY;

	/**
	 * @param copyrightText the text of the copyright, may be <code>null</code> if an
	 * copyrightImage is not
	 * @param copyrightImage the text of the copyright, may be <code>null</code> if an
	 * copyrightImage is not
	 * @param offsetX the offset to draw the copyright from the left of the image to the
	 * left of the copyright
	 * @param offsetY the offset to draw the copyright from the bottom of the image to the
	 * bottom of the copyright
	 */
	public Copyright(String copyrightText, BufferedImage copyrightImage, int offsetX, int offsetY) {
		this.copyrightText = copyrightText;
		this.copyrightImage = copyrightImage;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/**
	 * @return the text of the copyright, may be <code>null</code> if an copyrightImage is
	 * not
	 */
	public String getCopyrightText() {
		return copyrightText;
	}

	/**
	 * @return the text of the copyright, may be <code>null</code> if an copyrightImage is
	 * not
	 */
	public BufferedImage getCopyrightImage() {
		return copyrightImage;
	}

	/**
	 * @return the offset to draw the copyright from the left of the image to the left of
	 * the copyright
	 */
	public int getOffsetX() {
		return offsetX;
	}

	/**
	 * @return the offset to draw the copyright from the bottom of the image to the bottom
	 * of the copyright
	 */
	public int getOffsetY() {
		return offsetY;
	}

}