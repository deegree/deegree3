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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.geotiff;

import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * <code>ImageReaderFactory</code>: an object factory for Apache commons-pool providing
 * file-based image reader.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 *
 */
public class ImageReaderFactory implements PooledObjectFactory<ImageReader> {

	private final File file;

	public ImageReaderFactory(File file) {
		this.file = file;
	}

	@Override
	public void destroyObject(PooledObject<ImageReader> pooledObject) throws Exception {
		ImageReader reader = (ImageReader) pooledObject;
		reader.dispose();
	}

	@Override
	public boolean validateObject(PooledObject<ImageReader> pooledObject) {
		// ImageReader reader = (ImageReader) o;
		// ImageInputStream iis = (ImageInputStream) reader.getInput();
		// unknown if we need something here, so far no readers have become invalid
		return true;
	}

	@Override
	public void activateObject(PooledObject<ImageReader> pooledObject) throws Exception {
		// nothing to do
	}

	@Override
	public void passivateObject(PooledObject<ImageReader> pooledObject) throws Exception {
		// nothing to do
	}

	@Override
	public PooledObject<ImageReader> makeObject() throws Exception {
		ImageInputStream iis = null;
		ImageReader reader = null;
		Iterator<ImageReader> readers = getImageReadersBySuffix("tiff");
		while (readers.hasNext() && !(reader instanceof TIFFImageReader)) {
			reader = readers.next();
		}
		iis = createImageInputStream(file);
		// already checked in provider
		reader.setInput(iis);
		return new DefaultPooledObject<ImageReader>(reader);
	}

}
