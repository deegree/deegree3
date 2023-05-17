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
package org.deegree.rendering.r2d.context;

import static org.deegree.rendering.r2d.RenderHelper.calcScaleWMS130;

import java.awt.Color;
import java.util.Map;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.ImageSerializer;
import org.deegree.rendering.r2d.RenderHelper;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class RenderingInfo {

	private String format;

	private int width, height;

	private boolean transparent;

	private Color bgcolor;

	private Envelope envelope;

	private double pixelSize;

	// for GetFeatureInfo
	private int x, y, featureCount;

	private Map<String, String> parameters;

	private ImageSerializer serializer;

	public RenderingInfo(String format, int width, int height, boolean transparent, Color bgcolor, Envelope envelope,
			double pixelSize, Map<String, String> parameters) {
		this(format, width, height, transparent, bgcolor, envelope, pixelSize, parameters, null);
	}

	public RenderingInfo(String format, int width, int height, boolean transparent, Color bgcolor, Envelope envelope,
			double pixelSize, Map<String, String> parameters, ImageSerializer serializer) {
		this.format = format;
		this.width = width;
		this.height = height;
		this.transparent = transparent;
		this.bgcolor = bgcolor;
		this.envelope = envelope;
		this.pixelSize = pixelSize;
		this.parameters = parameters;
		this.serializer = serializer;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean getTransparent() {
		return transparent;
	}

	public Color getBgColor() {
		return bgcolor;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public double getPixelSize() {
		return pixelSize;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the featureCount
	 */
	public int getFeatureCount() {
		return featureCount;
	}

	/**
	 * @param featureCount the featureCount to set
	 */
	public void setFeatureCount(int featureCount) {
		this.featureCount = featureCount;
	}

	/**
	 * @return the original request parameters, if set
	 */
	public Map<String, String> getParameterMap() {
		return parameters;
	}

	public double getScale() {
		return calcScaleWMS130(width, height, envelope, envelope.getCoordinateSystem(), pixelSize);
	}

	public double getResolution() {
		return RenderHelper.calcResolution(envelope, width, height);
	}

	public ImageSerializer getSerializer() {
		return serializer;
	}

}
