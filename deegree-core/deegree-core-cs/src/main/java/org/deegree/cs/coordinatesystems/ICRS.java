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
package org.deegree.cs.coordinatesystems;

import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRSResource;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IDatum;
import org.deegree.cs.components.IGeodeticDatum;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.CRS.CRSType;
import org.deegree.cs.transformations.Transformation;

/**
 * Interface describing a general CoordinateSytem
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface ICRS extends CRSResource {

	/**
	 * @return the dimension of this CRS.
	 */
	int getDimension();

	/**
	 * @return one of the *_CRS types defined in this class.
	 */
	CRSType getType();

	/**
	 * @return (all) axis' in their defined order.
	 */
	IAxis[] getAxis();

	/**
	 * @return the usedDatum or <code>null</code> if the datum was not a Geodetic one.
	 */
	IGeodeticDatum getGeodeticDatum();

	/**
	 * @return the datum of this coordinate system.
	 */
	IDatum getDatum();

	/**
	 * @return the units of all axis of the ICoordinateSystem.
	 */
	IUnit[] getUnits();

	/**
	 * @param targetCRS to get the alternative Transformation for.
	 * @return true if this crs has an alternative transformation for the given
	 * ICoordinateSystem, false otherwise.
	 */
	boolean hasDirectTransformation(ICRS targetCRS);

	/**
	 * @param targetCRS to get the alternative transformation for.
	 * @return the transformation associated with the given crs, <code>null</code>
	 * otherwise.
	 */
	Transformation getDirectTransformation(ICRS targetCRS);

	/**
	 * @return the polynomial transformations.
	 */
	List<Transformation> getTransformations();

	/**
	 * Return the axis index associated with an easting value, if the axis could not be
	 * determined {@link Axis#AO_OTHER} 0 will be returned.
	 * @return the index of the axis which represents the easting/westing component of a
	 * coordinate tuple.
	 */
	int getEasting();

	/**
	 * Return the axis index associated with a northing value, if the axis could not be
	 * determined (e.g not is {@link Axis#AO_NORTH} {@link Axis#AO_SOUTH} or
	 * {@link Axis#AO_UP} or {@link Axis#AO_DOWN}) 1 will be returned.
	 * @return the index of the axis which represents the easting/westing component of a
	 * coordinate tuple.
	 */
	int getNorthing();

	/**
	 * Returns the approximate domain of validity of this coordinate system. The returned
	 * array will contain the values in the appropriate coordinate system and with the
	 * appropriate axis order.
	 * @return the real world coordinates of the domain of validity of this crs, or
	 * <code>null</code> if the valid domain could not be determined
	 */
	double[] getValidDomain();

	/**
	 * Converts the given coordinates in given to the unit of the respective axis.
	 * @param coordinates to convert to.
	 * @param units in which the coordinates were given.
	 * @param invert if the operation should be inverted, e.g. the coordinates are given
	 * in the axis units and should be converted to the given units.
	 * @return the converted coordinates.
	 */
	public Point3d convertToAxis(Point3d coordinates, IUnit[] units, boolean invert);

	/**
	 * @return an alias of the crs
	 */
	public String getAlias();

	/**
	 * TODO: this methode should become redundant with the reworked identifer concept!
	 * @param other
	 * @return true, if this and other are from the same type differes only in a flipped
	 * axis order.
	 */
	boolean equalsWithFlippedAxis(Object other);

}
