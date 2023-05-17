/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceIdentifier;

/**
 * Memory based resource location.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class IncorporealResourceLocation<T extends Resource> extends DefaultResourceLocation<T> {

	private byte[] bytes;

	/**
	 * @param bytes never <code>null</code>
	 * @param id never <code>null</code>
	 */
	public IncorporealResourceLocation(byte[] bytes, ResourceIdentifier<T> id) {
		super(null, id);
		this.bytes = bytes;
	}

	@Override
	public InputStream getAsStream() {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public InputStream resolve(String path) {
		return null;
	}

	@Override
	public File resolveToFile(String path) {
		return null;
	}

	@Override
	public URL resolveToUrl(String path) {
		return null;
	}

	@Override
	public void deactivate() {
		// ignore
	}

	@Override
	public void activate() {
		// ignore
	}

	@Override
	public void setContent(InputStream in) {
		try {
			bytes = IOUtils.toByteArray(in);
		}
		catch (IOException e) {
			throw new ResourceException(e.getLocalizedMessage(), e);
		}
	}

}
