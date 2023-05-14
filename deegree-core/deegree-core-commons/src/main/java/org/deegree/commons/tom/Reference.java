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

package org.deegree.commons.tom;

/**
 * Represents a lazy reference to an {@link Object}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @param <T> type of the referenced object
 */
public class Reference<T extends Object> implements Object {

	private final ReferenceResolver resolver;

	private String uri;

	private final String baseURL;

	private T object;

	protected ReferenceResolvingException exception;

	/**
	 * Creates a new {@link Reference} instance.
	 * @param resolver used for resolving the reference, must not be <code>null</code>
	 * @param uri the object's uri, must not be <code>null</code>
	 * @param baseURL base URL for resolving the uri, may be <code>null</code> (no
	 * resolving of relative URLs)
	 */
	public Reference(ReferenceResolver resolver, String uri, String baseURL) {
		this.resolver = resolver;
		this.uri = uri;
		// if ( isLocal() ) {
		// id = uri.substring( 1 );
		// }
		this.baseURL = baseURL;
	}

	/**
	 * Returns the resolver.
	 * @return the resolver, never <code>null</code>
	 */
	public ReferenceResolver getResolver() {
		return resolver;
	}

	/**
	 * Returns the URI of the object.
	 * @return the URI of the object, never <code>null</code>
	 */
	public String getURI() {
		return uri;
	}

	/**
	 * Sets the URI of the object.
	 * @param uri URI of the object, must not be <code>null</code>
	 */
	public void setURI(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns whether the reference has been resolved.
	 * @return true, if the reference has been resolved, false otherwise
	 */
	public boolean isResolved() {
		return object != null;
	}

	/**
	 * Returns whether the reference has been resolved and is an internal reference.
	 * @return <code>true</code> if the reference is resolved is an internal reference,
	 * <code>false</code> if the reference has not been resolved or is not internal
	 */
	public boolean isInternalResolved() {
		return false;
	}

	// TODO can we get rid of this method?
	public boolean isLocal() {
		return uri.startsWith("#");
	}

	/**
	 * Returns the base URL for resolving the uri.
	 * @return base URL for resolving the uri, may be <code>null</code> (no resolving of
	 * relative URLs)
	 */
	public String getBaseURL() {
		return baseURL;
	}

	/**
	 * Sets the referenced object.
	 * @param object the referenced object, may be <code>null</code>
	 */
	public void resolve(T object) {
		this.object = object;
	}

	/**
	 * Returns the referenced {@link Object} instance (may trigger resolving and fetching
	 * it).
	 * @return the referenced {@link Object} instance
	 * @throws ReferenceResolvingException if the reference cannot be resolved
	 */
	@SuppressWarnings("unchecked")
	public synchronized T getReferencedObject() throws ReferenceResolvingException {
		if (exception != null) {
			throw exception;
		}
		if (object == null) {
			try {
				object = (T) resolver.getObject(uri, baseURL);
			}
			catch (ReferenceResolvingException e) {
				throw exception = e;
			}
			if (object == null) {
				String msg = "Unable to resolve reference to '" + uri + "'.";
				throw exception = new ReferenceResolvingException(msg);
			}
		}
		return object;
	}

	@Override
	public String getId() {
		if (object != null) {
			return object.getId();
		}
		if (isLocal()) {
			return uri.substring(1);
		}
		return getReferencedObject().getId();
	}

}