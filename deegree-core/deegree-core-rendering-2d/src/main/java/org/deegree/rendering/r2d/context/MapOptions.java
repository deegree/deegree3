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
package org.deegree.rendering.r2d.context;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class MapOptions {

	private Quality quality;

	private Interpolation interpol;

	private Antialias antialias;

	private int maxFeatures;

	private int featureInfoRadius;

	private boolean opaque;

	private Integer featureInfoDecimalPlaces;

	private MapOptions(Quality quality, Interpolation interpol, Antialias antialias, int maxFeatures,
			int featureInfoRadius, boolean opaque, Integer featureInfoDecimalPlaces) {
		this.quality = quality;
		this.interpol = interpol;
		this.antialias = antialias;
		this.maxFeatures = maxFeatures;
		this.featureInfoRadius = featureInfoRadius;
		this.opaque = opaque;
		this.featureInfoDecimalPlaces = featureInfoDecimalPlaces;
	}

	/**
	 * @return the quality
	 */
	public Quality getQuality() {
		return quality;
	}

	/**
	 * @param quality the quality to set
	 */
	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	/**
	 * @return the interpol
	 */
	public Interpolation getInterpolation() {
		return interpol;
	}

	/**
	 * @param interpol the interpol to set
	 */
	public void setInterpolation(Interpolation interpol) {
		this.interpol = interpol;
	}

	/**
	 * @return the antialias
	 */
	public Antialias getAntialias() {
		return antialias;
	}

	/**
	 * @param antialias the antialias to set
	 */
	public void setAntialias(Antialias antialias) {
		this.antialias = antialias;
	}

	/**
	 * @return the maxFeatures
	 */
	public int getMaxFeatures() {
		return maxFeatures;
	}

	/**
	 * @param maxFeatures the maxFeatures to set
	 */
	public void setMaxFeatures(int maxFeatures) {
		this.maxFeatures = maxFeatures;
	}

	/**
	 * @return the featureInfoRadius, a value < 1 means default, 0 means disabled and > 0
	 * for the radius
	 */
	public int getFeatureInfoRadius() {
		return featureInfoRadius;
	}

	/**
	 * @param featureInfoRadius the featureInfoRadius to set, a value < 1 means default, 0
	 * means disabled and > 0 for the radius
	 */
	public void setFeatureInfoRadius(int featureInfoRadius) {
		this.featureInfoRadius = featureInfoRadius;
	}

	/**
	 * @return if layer is opaque
	 */
	public boolean isOpaque() {
		return opaque;
	}

	/**
	 * @param opaque set if layer is opaque
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * @return featureInfoDecimalPlaces, a non <code>null</code> positive value defines
	 * the requested number of digits after the decimal point to be used for numeric
	 * values, if this feature is available
	 */
	public Integer getFeatureInfoDecimalPlaces() {
		return featureInfoDecimalPlaces;
	}

	/**
	 * @param featureInfoDecimalPlaces the featureInfoDecimalPlaces to set, a non
	 * <code>null</code> positive value defines the requested number of digits after the
	 * decimal point to be used for numeric values, if this feature is available
	 */
	public void setFeatureInfoDecimalPlaces(Integer featureInfoDecimalPlaces) {
		this.featureInfoDecimalPlaces = featureInfoDecimalPlaces;
	}

	/**
	 * <code>Quality</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static enum Quality {

		/***/
		LOW,
		/***/
		NORMAL,
		/***/
		HIGH

	}

	/**
	 * <code>Interpolation</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static enum Interpolation {

		/***/
		NEARESTNEIGHBOR,
		/***/
		NEARESTNEIGHBOUR,
		/***/
		BILINEAR,
		/***/
		BICUBIC

	}

	/**
	 * <code>Antialias</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static enum Antialias {

		/***/
		IMAGE,
		/***/
		TEXT,
		/***/
		BOTH,
		/***/
		NONE

	}

	public interface MapOptionsSetter<T> {

		void setOption(String layer, T value);

	}

	public static MapOptionsSetter<Quality> getQualitySetter(final MapOptionsMaps options) {
		return new MapOptionsSetter<Quality>() {
			@Override
			public void setOption(String layer, Quality value) {
				options.setQuality(layer, value);
			}
		};
	}

	public static MapOptionsSetter<Antialias> getAntialiasSetter(final MapOptionsMaps options) {
		return new MapOptionsSetter<Antialias>() {
			@Override
			public void setOption(String layer, Antialias value) {
				options.setAntialias(layer, value);
			}
		};
	}

	public static MapOptionsSetter<Interpolation> getInterpolationSetter(final MapOptionsMaps options) {
		return new MapOptionsSetter<Interpolation>() {
			@Override
			public void setOption(String layer, Interpolation value) {
				options.setInterpolation(layer, value);
			}
		};
	}

	public interface MapOptionsGetter<T> {

		T getOption(String layer);

	}

	public static MapOptionsGetter<Quality> getQualityGetter(final MapOptionsMaps options) {
		return new MapOptionsGetter<Quality>() {
			@Override
			public Quality getOption(String layer) {
				return options.getQuality(layer);
			}
		};
	}

	public static MapOptionsGetter<Antialias> getAntialiasGetter(final MapOptionsMaps options) {
		return new MapOptionsGetter<Antialias>() {
			@Override
			public Antialias getOption(String layer) {
				return options.getAntialias(layer);
			}
		};
	}

	public static MapOptionsGetter<Interpolation> getInterpolationGetter(final MapOptionsMaps options) {
		return new MapOptionsGetter<Interpolation>() {
			@Override
			public Interpolation getOption(String layer) {
				return options.getInterpolation(layer);
			}
		};
	}

	public static class Builder {

		private Quality quality;

		private Interpolation interpolation;

		private Antialias antialias;

		private int maxFeatures = -1;

		private int featureInfoRadius = -1;

		private boolean opaque;

		private Integer featureInfoDecimalPlaces;

		/**
		 * @param quality the quality to set
		 */
		public Builder quality(Quality quality) {
			this.quality = quality;
			return this;
		}

		/**
		 * @param interpolation the interpolation to set
		 */
		public Builder interpolation(Interpolation interpolation) {
			this.interpolation = interpolation;
			return this;
		}

		/**
		 * @param antialias the antialias to set
		 */
		public Builder antialias(Antialias antialias) {
			this.antialias = antialias;
			return this;
		}

		/**
		 * @param maxFeatures the maxFeatures to set
		 */
		public Builder maxFeatures(int maxFeatures) {
			this.maxFeatures = maxFeatures;
			return this;
		}

		/**
		 * @param featureInfoRadius the featureInfoRadius to set, a value < 1 means
		 * default, 0 means disabled and > 0 for the radius
		 */
		public Builder featureInfoRadius(int featureInfoRadius) {
			this.featureInfoRadius = featureInfoRadius;
			return this;
		}

		/**
		 * @param opaque set if layer is opaque
		 */
		public Builder opaque(boolean opaque) {
			this.opaque = opaque;
			return this;
		}

		/**
		 * @param featureInfoDecimalPlaces the featureInfoDecimalPlaces to set, a non
		 * <code>null</code> positive value defines the requested number of digits after
		 * the decimal point to be used for numeric values, if this feature is available
		 */
		public Builder featureInfoDecimalPlaces(Integer featureInfoDecimalPlaces) {
			this.featureInfoDecimalPlaces = featureInfoDecimalPlaces;
			return this;
		}

		public MapOptions build() {
			return new MapOptions(quality, interpolation, antialias, maxFeatures, featureInfoRadius, opaque,
					featureInfoDecimalPlaces);
		}

	}

}
