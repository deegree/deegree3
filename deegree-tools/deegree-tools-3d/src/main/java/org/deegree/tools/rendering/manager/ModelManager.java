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

package org.deegree.tools.rendering.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.deegree.commons.index.PositionableModel;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;

/**
 * The <code>ModelManager</code> class defines a simple interface for inserting, deleting
 * and updating different kind of data models into the WPVS.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @param <FB> The type of the filebackend.
 *
 */
public abstract class ModelManager<FB extends PositionableModel> {

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ModelManager.class);

	/**
	 * A backend using a database
	 */
	private final ModelBackend<?> modelBackend;

	/**
	 * The place to store textures.
	 */
	protected final File textureDir;

	/**
	 * The translation to the origin of the WPVS scene.
	 */
	protected final double[] wpvsTranslationVector;

	/**
	 * @param textureDir
	 * @param modelBackend
	 * @param wpvsTranslationVector 2d vector to the origin of the wpvs scene
	 */
	protected ModelManager(String textureDir, ModelBackend<?> modelBackend, double[] wpvsTranslationVector) {
		if (textureDir != null) {
			File f = new File(textureDir);
			if (!f.exists()) {
				LOG.warn("Given texture directory does not exist, not checking referenes in billboards.");
				f = null;
			}
			this.textureDir = f;
		}
		else {
			this.textureDir = null;
		}

		this.modelBackend = modelBackend;
		this.wpvsTranslationVector = wpvsTranslationVector;

	}

	/**
	 * Imports the data from the given file.
	 * @param uuid the file in an expected format.
	 * @param objectType to be deleted
	 * @param qualityLevel the qualitylevel to be deleted, if -1 the object will be
	 * deleted from the backend.
	 * @param sqlWhere defining a where clause on elements to be deleted.
	 * @return the number of imported objects
	 * @throws IOException
	 *
	 */
	public BackendResult delete(String uuid, Type objectType, int qualityLevel, String sqlWhere) throws IOException {
		return modelBackend.delete(uuid, objectType, qualityLevel, sqlWhere);
	}

	/**
	 * Imports the data from the given file.
	 * @param f the file in an expected format.
	 * @param commandLine program parameters only necessary for a given instance object
	 * will be available in the commandline.
	 * @return the number of imported objects
	 * @throws FileNotFoundException
	 * @throws IOException
	 *
	 */
	public abstract BackendResult importFromFile(File f, CommandLine commandLine)
			throws FileNotFoundException, IOException;

	/**
	 * @return the modelBackend
	 */
	public final ModelBackend<?> getDbBackend() {
		return modelBackend;
	}

	/**
	 * @return the textureDir
	 */
	public final File getTextureDir() {
		return textureDir;
	}

	/**
	 * @throws IOException
	 *
	 */
	public void flush() throws IOException {
		modelBackend.flush();
	}

}
