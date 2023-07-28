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
package org.deegree.feature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.timeslice.TimeSlice;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for common {@link Feature} implementations.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public abstract class AbstractFeature implements Feature {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractFeature.class);

	/** Feature id */
	protected String fid;

	/** Feature type */
	protected final FeatureType ft;

	private Envelope envelope;

	private boolean envelopeCalculated = false;

	private ExtraProps extraProps;

	/**
	 * Creates a new {@link AbstractFeature} instance.
	 * @param fid feature id or <code>null</code> if the feature is anonymous (discouraged
	 * for most use cases)
	 * @param ft feature type, must not be <code>null</code>
	 * @param extraProps extra properties, may be <code>null</code>
	 */
	protected AbstractFeature(String fid, FeatureType ft, ExtraProps extraProps) {
		this.fid = fid;
		this.ft = ft;
		this.extraProps = extraProps;
	}

	@Override
	public String getId() {
		return fid;
	}

	@Override
	public void setId(String fid) {
		this.fid = fid;
	}

	@Override
	public QName getName() {
		return ft.getName();
	}

	@Override
	public FeatureType getType() {
		return ft;
	}

	@Override
	public Envelope getEnvelope() {
		if (!envelopeCalculated) {
			envelope = calcEnvelope();
		}
		return envelope;
	}

	@Override
	public void setEnvelope(Envelope env) {
		this.envelope = env;
		envelopeCalculated = true;
	}

	/**
	 * Helper method for calculating the envelope of a feature.
	 * @return envelope of all geometry properties of the feature
	 */
	@Override
	public Envelope calcEnvelope() {
		Envelope featureBBox = null;
		for (Property prop : this.getProperties()) {
			featureBBox = mergeEnvelope(prop, featureBBox);
		}
		if (getExtraProperties() != null) {
			for (Property prop : getExtraProperties().getProperties()) {
				featureBBox = mergeEnvelope(prop, featureBBox);
			}
		}
		if (featureBBox == null) {
			Set<Feature> visited = new HashSet<Feature>();
			try {
				for (Property prop : this.getProperties()) {
					if (prop.getValue() instanceof Feature) {
						Feature f = (Feature) prop.getValue();
						if (visited.contains(f)) {
							continue;
						}
						visited.add(f);

						try {
							for (Property p2 : f.getProperties()) {
								featureBBox = mergeEnvelope(p2, featureBBox);
							}
						}
						catch (ReferenceResolvingException e) {
							LOG.debug("Could not resolve properties when calculating envelope: {}",
									e.getLocalizedMessage());
						}
					}
				}
			}
			catch (ReferenceResolvingException e) {
				LOG.debug("Could not resolve properties when calculating envelope: {}", e.getLocalizedMessage());
			}
		}
		return featureBBox;
	}

	private Envelope mergeEnvelope(final TypedObjectNode node, Envelope env) {
		if (node instanceof Property) {
			Property prop = (Property) node;
			if (prop.getValue() != null) {
				env = mergeEnvelope(prop.getValue(), env);
			}
		}
		else if (node instanceof Geometry) {
			Geometry g = (Geometry) node;
			Envelope gEnv = g.getEnvelope();
			// TODO this is to skip one-dimensional bounding boxes...
			if (gEnv.getCoordinateDimension() > 1) {
				if (env != null) {
					env = env.merge(gEnv);
				}
				else {
					env = gEnv;
				}
			}
			else {
				LOG.warn("Encountered one-dimensional bbox. Ignoring for feature envelope.");
			}
		}
		else if (node instanceof TimeSlice) {
			final TimeSlice timeSlice = (TimeSlice) node;
			for (final Property prop : timeSlice.getProperties()) {
				final Envelope propEnvelope = mergeEnvelope(prop, env);
				env = mergeEnvelope(env, propEnvelope);
			}
		}
		else if (node instanceof ElementNode) {
			// e.g. INSPIRE Address geometry
			ElementNode xml = (ElementNode) node;
			List<TypedObjectNode> children = xml.getChildren();
			if (children != null) {
				for (TypedObjectNode child : children) {
					env = mergeEnvelope(child, env);
				}
			}
		}
		return env;
	}

	private Envelope mergeEnvelope(final Envelope existing, final Envelope additional) {
		if (existing == null) {
			return additional;
		}
		if (additional == null || additional.getCoordinateDimension() < 2) {
			return existing;
		}
		return existing.merge(additional);
	}

	@Override
	public ExtraProps getExtraProperties() {
		return extraProps;
	}

	@Override
	public void setExtraProperties(ExtraProps extraProps) {
		this.extraProps = extraProps;
	}

}