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

package org.deegree.tools.rendering.dem.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.SQLException;

import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.tools.rendering.dem.builder.dag.FragmentInfo;

/**
 * Manages the storing of {@link MacroTriangle}-based patches during the generation of an
 * {@link MultiresolutionMesh}-dataset (that is performed by the
 * {@link DEMDatasetGenerator}).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PatchManager {

	private int levels;

	private Blob targetBlob;

	// current offset in blob
	private long currentPos = 0;

	// contains the patch positions for all location codes
	private FragmentInfo[] patchesByLocationCode;

	private PatchManager(int levels, FragmentInfo[] patchesByLocationCode) {
		this.levels = levels;
		this.patchesByLocationCode = patchesByLocationCode;
	}

	PatchManager(int levels, Blob targetBlob) {
		this.levels = levels;
		this.targetBlob = targetBlob;
		this.patchesByLocationCode = new FragmentInfo[(2 << levels) - 2];
		System.out.println("Creating new triangle manager for " + levels + " levels. Reserved "
				+ patchesByLocationCode.length + " offsets.");
	}

	public int getLevels() {
		return levels;
	}

	public synchronized void storePatch(MacroTriangle patch, ByteBuffer rawTileBytes) throws SQLException {

		byte[] bytes = new byte[rawTileBytes.capacity()];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		rawTileBytes.rewind();
		buffer.put(rawTileBytes);
		buffer.rewind();

		String locationCode = patch.getLocationCode();
		// System.out.print("Storing macro triangle '" + locationCode
		// + "' in blob and registering patch info...");
		FragmentInfo pos = new FragmentInfo(patch, currentPos, rawTileBytes.capacity());
		patchesByLocationCode[getArrayPosForLocationCode(locationCode)] = pos;
		targetBlob.setBytes(pos.blobPosition + 1, bytes);
		currentPos += pos.blobLength;
		// System.out.println("done.");
	}

	private int getArrayPosForLocationCode(String locationCode) {
		return Integer.parseInt("1" + locationCode, 2) - 2;
	}

	public FragmentInfo getPatchByLocationCode(int locationCode) {
		return patchesByLocationCode[locationCode - 2];
	}

	public void saveToFile(File file) throws FileNotFoundException, IOException {
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
		os.writeInt(levels);
		os.writeObject(patchesByLocationCode);
		os.close();
	}

	public static PatchManager restoreFromFile(File file)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
		int levels = is.readInt();
		FragmentInfo[] patchInfo = (FragmentInfo[]) is.readObject();
		is.close();
		return new PatchManager(levels, patchInfo);
	}

	public String toString() {
		String s = "- Total number of patches: " + patchesByLocationCode.length;
		s += "\n- Number of patches on level 0: " + (patchesByLocationCode.length + 2) / 2;
		return s;
	}

}
