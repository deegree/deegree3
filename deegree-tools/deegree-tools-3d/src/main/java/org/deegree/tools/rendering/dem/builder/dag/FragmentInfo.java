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

package org.deegree.tools.rendering.dem.builder.dag;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.tools.rendering.dem.builder.MacroTriangle;

/**
 * TODO comment me
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FragmentInfo implements Serializable {

	private static final long serialVersionUID = -4663061794245767665L;

	public static final int SIZE = MeshFragment.SIZE;

	int id;

	public int locationCode;

	public float geometricError;

	public float minX, minY, minZ;

	public float maxX, maxY, maxZ;

	public long blobPosition;

	public int blobLength;

	public FragmentInfo(MacroTriangle patch, long blobPosition, int blobLength) {
		this.locationCode = Integer.parseInt("1" + patch.getLocationCode(), 2);
		this.geometricError = patch.geometryError;
		float[][] bbox = patch.getBBox();
		this.minX = bbox[0][0];
		this.minY = bbox[0][1];
		this.minZ = bbox[0][2];
		this.maxX = bbox[1][0];
		this.maxY = bbox[1][1];
		this.maxZ = bbox[1][2];
		this.blobPosition = blobPosition;
		this.blobLength = blobLength;
	}

	private String codeToString(int code) {
		String s = Integer.toString(code, 2);
		return s.substring(1);
	}

	public String toString() {
		String s = id + ": (";
		s += codeToString(locationCode) + "), error: " + geometricError + ", min=(" + minX + "," + minY + "," + minZ
				+ "), max=(" + maxX + "," + maxY + "," + maxZ + "), blob position [" + blobPosition + "-"
				+ (blobPosition + blobLength - 1) + "]";
		return s;
	}

	void append(ByteBuffer buffer) {
		MeshFragment.store(buffer, minX, minY, minZ, maxX, maxY, maxZ, geometricError, blobPosition, blobLength);
	}

	public float[][] getBBox() {
		// TODO Auto-generated method stub
		return new float[][] { new float[] { minX, minY, minZ }, new float[] { maxX, maxY, maxZ } };
	}

}
