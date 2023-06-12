/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2022 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.sqldialect.oracle.sdo;

/**
 * Holder for a native representation of Oracle Geometries in plain Java types
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class SDOGeometry {

	public int gtype;

	public int srid;

	public double point[];

	public int[] elem_info;

	public double[] ordinates;

	public SDOGeometry(int gtype, int srid, double point[], int[] elem_info, double[] ordinates) {
		this.gtype = gtype;
		this.srid = srid;
		this.point = point;
		this.elem_info = elem_info;
		this.ordinates = ordinates;
	}

	public SDOGeometry() {

	}

}
