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

package org.deegree.cs.configuration.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.persistence.AbstractCRSStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>XMLFileResource</code> is an {@link OMElement} based adapter for an xml file.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public abstract class XMLFileResource extends XMLAdapter implements XMLResource {

	private static Logger LOG = LoggerFactory.getLogger(XMLFileResource.class);

	private AbstractCRSStore provider = null;

	/**
	 * @param provider to use for the reverse lookup of coordinate systems, required
	 * @param requiredRootLocalName check for the root elements localname, may be
	 * <code>null</code>
	 * @param requiredNamespace check for the root elements namespace, may be
	 * <code>null</code>
	 */
	public XMLFileResource(AbstractCRSStore provider, URL file, String requiredRootLocalName,
			String requiredNamespace) {
		if (provider == null) {
			throw new NullPointerException("The provider is null, this may not be.");
		}
		if (file == null) {
			throw new NullPointerException(
					"The CRS_FILE property was not set, this resolver can not function without a file. ");
		}
		InputStream is = null;
		try {
			LOG.debug("Trying to load configuration from file: " + file);
			is = file.openStream();

			load(is);
			if (getRootElement() == null) {
				throw new NullPointerException("The file: " + file + " does not contain a root element. ");
			}
			if (requiredRootLocalName != null && !"".equals(requiredRootLocalName)) {
				if (!requiredRootLocalName.equalsIgnoreCase(getRootElement().getLocalName())) {
					throw new IllegalArgumentException("The local name of the root element of the given file is not: "
							+ requiredRootLocalName + " aborting.");
				}
			}
			if (requiredNamespace != null) {
				if (!requiredNamespace.equals(getRootElement().getNamespace().getNamespaceURI())) {
					throw new IllegalArgumentException(
							"The root element of the given file is not in the required namespace: " + requiredNamespace
									+ " aborting.");
				}
			}

		}
		catch (IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new IllegalArgumentException(
					"File: " + file + " is an invalid xml file resource because: " + e.getLocalizedMessage());
		}
		// rb: no way to close the stream (yet?).
		// finally {
		// if ( is != null ) {
		// try {
		// is.close();
		// } catch ( IOException e ) {
		// LOG.debug( "Could not close inputstream: " + e.getLocalizedMessage(), e );
		// LOG.debug( "Could not close inputstream: " + e.getLocalizedMessage() );
		// }
		// }
		// }
		this.provider = provider;
	}

	/**
	 * @param provider to be used for callback.
	 * @param rootElement
	 */
	public XMLFileResource(AbstractCRSStore provider, OMElement rootElement) {
		super(rootElement);
		this.provider = provider;
	}

	/**
	 * @return the provider used for reversed look ups, will never be <code>null</code>
	 */
	public AbstractCRSStore getProvider() {
		return provider;
	}

}
