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
package org.deegree.commons.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Modifiable prefix to namespace (and namespace to prefix) mappings for dealing with
 * qualified names and XPath expressions.
 * <p>
 * Implements both the <code>org.jaxen.NamespaceContext</code> and the
 * <code>javax.xml.namespace.NamespaceContext</code> interfaces.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class NamespaceBindings implements org.jaxen.NamespaceContext, javax.xml.namespace.NamespaceContext {

	// key: prefix, value: namespace
	private final Map<String, String> prefixToNs = new HashMap<String, String>();

	// key: namespace, value: prefix
	private final Map<String, String> nsToPrefix = new HashMap<String, String>();

	/**
	 * Creates a new instance of {@link NamespaceContext} with no bindings.
	 */
	public NamespaceBindings() {
		prefixToNs.put(CommonNamespaces.XMLNSNS_PREFIX, CommonNamespaces.XMLNSNS);
		nsToPrefix.put(CommonNamespaces.XMLNSNS, CommonNamespaces.XMLNSNS_PREFIX);
	}

	/**
	 * Creates a new instance of {@link NamespaceContext} that contains all the bindings
	 * from the argument context.
	 * @param nsContext bindings to copy, must not be <code>null</code>
	 */
	public NamespaceBindings(NamespaceBindings nsContext) {
		this();

		prefixToNs.putAll(nsContext.prefixToNs);
		nsToPrefix.putAll(nsContext.nsToPrefix);
	}

	/**
	 * Creates a new instance of {@link NamespaceContext} that contains all the bindings
	 * from the given XML namespace context.
	 * @param nsContext bindings to copy, must not be <code>null</code>
	 */
	public NamespaceBindings(javax.xml.namespace.NamespaceContext nsContext, Collection<String> prefixes) {
		this();

		for (String prefix : prefixes) {
			String ns = nsContext.getNamespaceURI(prefix);
			prefixToNs.put(prefix, ns);
			nsToPrefix.put(ns, prefix);
		}
	}

	/**
	 * Registers a new prefix with an assigned namespace URI.
	 * @param prefix prefix, must not be <code>null</code>
	 * @param namespace namespace, may be <code>null</code>
	 * @return this: new XPath(..., new NamespaceContext().addNamespace(...)
	 */
	public NamespaceBindings addNamespace(String prefix, String namespace) {
		prefixToNs.put(prefix, namespace);
		nsToPrefix.put(namespace, prefix);
		return this;
	}

	/**
	 * Returns the namespace mapping for the given prefix (Jaxen method).
	 * <p>
	 * Taken from the Jaxen Javadoc: In XPath, there is no such thing as a 'default
	 * namespace'. The empty prefix always resolves to the empty namespace URI. This
	 * method should return null for the empty prefix. Similarly, the prefix "xml" always
	 * resolves to the URI "http://www.w3.org/XML/1998/namespace".
	 * </p>
	 * @param prefix prefix, may be <code>null</code>
	 * @return namespace uri, may be <code>null</code> (unbound)
	 */
	@Override
	public String translateNamespacePrefixToUri(String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			return null;
		}
		return prefixToNs.get(prefix);
	}

	/**
	 * Returns all bound namespaces.
	 * @return bound namespaces, never <code>null</code>
	 */
	public Iterator<String> getNamespaceURIs() {
		return nsToPrefix.keySet().iterator();
	}

	@Override
	public String getNamespaceURI(String prefix) {
		String ns = prefixToNs.get(prefix);
		if (ns == null) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		}
		return ns;
	}

	@Override
	public String getPrefix(String ns) {
		return nsToPrefix.get(ns);
	}

	@Override
	public Iterator<String> getPrefixes(String ns) {
		return Collections.singletonList(nsToPrefix.get(ns)).iterator();
	}

	/**
	 * Returns all bound prefixes.
	 * @return bound prefixes, never <code>null</code>
	 */
	public Iterator<String> getPrefixes() {
		return prefixToNs.keySet().iterator();
	}

	@Override
	public String toString() {
		return prefixToNs.toString();
	}

}