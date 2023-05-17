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

package org.deegree.cs.refs.coordinatesystem;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IDatum;
import org.deegree.cs.components.IGeodeticDatum;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.CRS.CRSType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.refs.CRSResourceRef;
import org.deegree.cs.transformations.Transformation;
import org.slf4j.Logger;

/**
 * Represents a {@link CRSRef} that is not necessarily resolved or resolvable.
 * <p>
 * Their are two aspects that this class takes care of:
 * <nl>
 * <li>In most use cases, coordinate reference system are identified using strings (such
 * as 'EPSG:4326'). However, there are multiple equivalent ways to encode coordinate
 * reference system identifications (another one would be 'urn:ogc:def:crs:EPSG::4326').
 * By using this class to represent a CRS, the original spelling is maintained.</li>
 * <li>A coordinate reference system may be specified which is not known to the
 * {@link CRSStore}. However, for some operations this is not a necessarily a problem,
 * e.g. a GML document may be read and transformed into Feature and Geometry objects.</li>
 * </nl>
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
public class CRSRef extends CRSResourceRef<ICRS> implements Serializable, ICRS {

	private static final long serialVersionUID = -2387578425336244509L;

	private static final Logger LOG = getLogger(CRSRef.class);

	/**
	 * Flag indicating, if the axis order should be swapped to x/y (EAST/NORTH;
	 * WEST/SOUTH) or the defined axis order is used
	 */
	private boolean forceXY;

	/**
	 * Creates a new {@link CRSRef} instance.
	 * @param resolver used for resolving the reference, must not be <code>null</code>
	 * @param uri the object's uri, must not be <code>null</code>
	 * @param baseURL base URL for resolving the uri, may be <code>null</code> (no
	 * resolving of relative URLs)
	 */
	public CRSRef(ReferenceResolver resolver, String uri, String baseURL) {
		super(resolver, uri, baseURL);
	}

	/**
	 * Creates a new {@link CRSRef} instance with a coordinate reference system name.
	 * @param resolver used for resolving the reference, must not be <code>null</code>
	 * @param uri the object's uri, must not be <code>null</code>
	 * @param baseURL base URL for resolving the uri, may be <code>null</code> (no
	 * resolving of relative URLs)
	 * @param forceXY true if the axis order of the coordinate system should be x/y
	 * (EAST/NORTH; WEST/SOUTH); false id the defined axis order should be taken
	 */
	public CRSRef(ReferenceResolver resolver, String uri, String baseURL, boolean forceXY) {
		this(resolver, uri, baseURL);
		this.forceXY = forceXY;
	}

	/**
	 * Returns the string that identifies the {@link CRSRef} which is the URI of the
	 * Reference.
	 * @return the string that identifies the coordinate reference system
	 */
	@Override
	public String getName() {
		return getURI();
	}

	/**
	 * @return the alias of a CRS reference is the id
	 */
	@Override
	public String getAlias() {
		return getURI();
	}

	@Override
	public boolean equals(Object obj) {
		try {
			if (getReferencedObject() != null) {
				return getReferencedObject().equals(obj);
			}
		}
		catch (ReferenceResolvingException e) {
			LOG.debug("CRS reference could not be resolved: {}", e.getLocalizedMessage());
		}
		return getURI().equals(obj);
	}

	@Override
	public int hashCode() {
		try {
			if (getReferencedObject() != null) {
				return getReferencedObject().hashCode();
			}
		}
		catch (ReferenceResolvingException e) {
			LOG.debug("CRS reference could not be resolved: {}", e.getLocalizedMessage());
		}
		return getURI().hashCode();
	}

	@Override
	public String getAreaOfUse() {
		return getReferencedObject().getAreaOfUse();
	}

	@Override
	public String toString() {
		return "{uri=" + getURI() + ", resolved=" + isResolved() + "}";
	}

	public IAxis[] getAxis() {
		return getReferencedObject().getAxis();
	}

	public IGeodeticDatum getGeodeticDatum() {
		return getReferencedObject().getGeodeticDatum();
	}

	public IDatum getDatum() {
		return getReferencedObject().getDatum();
	}

	public IUnit[] getUnits() {
		return getReferencedObject().getUnits();
	}

	public boolean hasDirectTransformation(ICRS targetCRS) {
		return getReferencedObject().hasDirectTransformation(targetCRS);
	}

	public Transformation getDirectTransformation(ICRS targetCRS) {
		return getReferencedObject().getDirectTransformation(targetCRS);
	}

	public Point3d convertToAxis(Point3d coordinates, IUnit[] units, boolean invert) {
		return getReferencedObject().convertToAxis(coordinates, units, invert);
	}

	public List<Transformation> getTransformations() {
		return getReferencedObject().getTransformations();
	}

	public int getEasting() {
		return getReferencedObject().getEasting();
	}

	public int getNorthing() {
		return getReferencedObject().getNorthing();
	}

	public double[] getValidDomain() {
		return getReferencedObject().getValidDomain();
	}

	@Override
	public double[] getAreaOfUseBBox() {
		return getReferencedObject().getAreaOfUseBBox();
	}

	public int getDimension() {
		return getReferencedObject().getDimension();
	}

	public CRSType getType() {
		return getReferencedObject().getType();
	}

	public boolean isXYForced() {
		return forceXY;
	}

	@Override
	public ICRS getReferencedObject() throws ReferenceResolvingException {
		// ensure that getReferenced object returns a concrete CRS instance!
		ICRS referencedObject = super.getReferencedObject();
		if (referencedObject instanceof CRSRef) {
			return ((CRSRef) referencedObject).getReferencedObject();
		}
		return referencedObject;
	}

	@Override
	public boolean equalsWithFlippedAxis(Object other) {
		return getReferencedObject().equalsWithFlippedAxis(other);
	}

}
