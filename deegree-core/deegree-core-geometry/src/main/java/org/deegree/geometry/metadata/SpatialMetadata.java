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
package org.deegree.geometry.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.primitive.Point;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class SpatialMetadata {

	private static final SimpleGeometryFactory GEOM_FACTORY = new SimpleGeometryFactory();

	private Envelope envelope;

	private List<ICRS> coordinateSystems;

	/**
	 * Instantiates an empty SpatialMetadata instance.
	 */
	public SpatialMetadata() {
		this(null, Collections.<ICRS>emptyList());
	}

	/**
	 * Instantiates an SpatialMetadata instance with envelope and coordinate systems.
	 * @param envelope may be <code>null</code>
	 * @param coordinateSystems may be empty but never <code>null</code>
	 */
	public SpatialMetadata(Envelope envelope, List<ICRS> coordinateSystems) {
		this.envelope = envelope;
		this.coordinateSystems = coordinateSystems;
	}

	/**
	 * Instantiates an SpatialMetadata from another SpatialMetadata instance.
	 * @param spatialMetadata may be <code>null</code>
	 **/
	public SpatialMetadata(SpatialMetadata spatialMetadata) {
		if (spatialMetadata != null) {
			this.envelope = copyEnvelope(spatialMetadata.envelope);
			this.coordinateSystems = new ArrayList<ICRS>();
			if (spatialMetadata.coordinateSystems != null)
				this.coordinateSystems.addAll(spatialMetadata.coordinateSystems);
		}
		else {
			this.envelope = null;
			this.coordinateSystems = Collections.emptyList();
		}
	}

	/**
	 * @return the envelope may be <code>null</code>
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

	/**
	 * @param envelope the envelope to set, may be <code>null</code>
	 */
	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	/**
	 * @return the coordinateSystems, never <code>null</code>
	 */
	public List<ICRS> getCoordinateSystems() {
		return coordinateSystems;
	}

	/**
	 * @param coordinateSystems the coordinateSystems to set, never <code>null</code>
	 */
	public void setCoordinateSystems(List<ICRS> coordinateSystems) {
		this.coordinateSystems = coordinateSystems;
	}

	/**
	 * Merge the passed SpatialMetadata into this SpatialMetadata and returns the merged
	 * SpatialMetadata, this and the passed are not changed!
	 * @param spatialMetadataToMerge SpatialMetadata to merge, never <code>null</code>
	 */
	public SpatialMetadata merge(SpatialMetadata spatialMetadataToMerge) {
		if (spatialMetadataToMerge == null)
			return new SpatialMetadata(this);

		List<ICRS> newCoordinateSystems = new ArrayList<ICRS>();
		if (this.coordinateSystems != null) {
			newCoordinateSystems.addAll(this.coordinateSystems);
		}
		if (spatialMetadataToMerge.getCoordinateSystems() != null) {
			for (ICRS crsToMerge : spatialMetadataToMerge.getCoordinateSystems()) {
				if (!newCoordinateSystems.contains(crsToMerge)) {
					newCoordinateSystems.add(crsToMerge);
				}
			}
		}
		Envelope newEnvelope = copyEnvelope(this.envelope);
		if (spatialMetadataToMerge.getEnvelope() != null) {
			if (newEnvelope == null)
				newEnvelope = copyEnvelope(spatialMetadataToMerge.getEnvelope());
			else
				newEnvelope = newEnvelope.merge(spatialMetadataToMerge.getEnvelope());
		}
		return new SpatialMetadata(newEnvelope, newCoordinateSystems);
	}

	@Override
	public String toString() {
		return "SpatialMetadata [envelope=" + envelope + ", coordinateSystems=" + coordinateSystems + "]";
	}

	private Envelope copyEnvelope(Envelope envelopeToCopy) {
		if (envelopeToCopy != null) {
			Point min = envelopeToCopy.getMin();
			Point max = envelopeToCopy.getMax();
			ICRS crs = envelopeToCopy.getCoordinateSystem();
			return GEOM_FACTORY.createEnvelope(min.get0(), min.get1(), max.get0(), max.get1(), crs);
		}
		return null;
	}

}