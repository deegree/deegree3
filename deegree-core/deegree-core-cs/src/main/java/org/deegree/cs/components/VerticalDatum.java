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

package org.deegree.cs.components;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CRSResource;

/**
 * A <code>VerticalDatum</code> is a datum which only has one axis. It is used for
 * vertical measurements.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class VerticalDatum extends Datum implements IVerticalDatum {

	/**
	 * @param id of this datum.
	 */
	public VerticalDatum(CRSResource id) {
		super(id);
	}

	/**
	 * @param codes
	 * @param names
	 * @param versions
	 * @param descriptions
	 * @param areasOfUse
	 */
	public VerticalDatum(CRSCodeType[] codes, String[] names, String[] versions, String[] descriptions,
			String[] areasOfUse) {
		this(new CRSIdentifiable(codes, names, versions, descriptions, areasOfUse));
	}

	/**
	 * @param code
	 * @param name
	 * @param version
	 * @param description
	 * @param areaOfUse
	 */
	public VerticalDatum(CRSCodeType code, String name, String version, String description, String areaOfUse) {
		this(new CRSCodeType[] { code }, new String[] { name }, new String[] { version }, new String[] { description },
				new String[] { areaOfUse });
	}

	/**
	 * @param code
	 */
	public VerticalDatum(CRSCodeType code) {
		this(new CRSCodeType[] { code }, null, null, null, null);
	}

}
