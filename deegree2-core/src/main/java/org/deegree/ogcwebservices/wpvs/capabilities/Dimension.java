//$HeadURL$
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

package org.deegree.ogcwebservices.wpvs.capabilities;

/**
 * TODO this class is an extended copy of org.deegree.ogcwebservices.wms.capabilities.Dimension.
 * the wms version should be moved up and this one should be a specialisation.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Dimension {

    private String name;

	private String units;

	private String unitSymbol;

	private String default_;

	private Boolean multipleValues;

	private Boolean nearestValue;

	private Boolean current;

	private String value;

	/**
     * Creates a new dimension object from the given parameters.
     *
     * @param name
     * @param units
     * @param unitSymbol
     * @param default_
     * @param multipleValues
     * @param nearestValue
     * @param current
     * @param value
     */
    public Dimension( String name, String units, String unitSymbol, String default_,
    				  Boolean multipleValues, Boolean nearestValue, Boolean current, String value ) {

    	this.name = name;
		this.units = units;
		this.unitSymbol = unitSymbol;
		this.default_ = default_;
		this.multipleValues = multipleValues;
		this.nearestValue = nearestValue;
		this.current = current;
		this.value = value;

    }

	/**
	 * @return Returns the current.
	 */
	public Boolean getCurrent() {
		return current;
	}

	/**
	 * @return Returns the default_.
	 */
	public String getDefault() {
		return default_;
	}

	/**
	 * @return Returns the multipleValues.
	 */
	public Boolean getMultipleValues() {
		return multipleValues;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the nearestValue.
	 */
	public Boolean getNearestValue() {
		return nearestValue;
	}

	/**
	 * @return Returns the units.
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @return Returns the unitSymbol.
	 */
	public String getUnitSymbol() {
		return unitSymbol;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

}
