//$HeadURL$
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
package org.deegree.framework.xml;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deegree.ogcbase.CommonNamespaces;

/**
 * Implementation of Jaxen's (http://jaxen.codehaus.org) <code>NamespaceContext</code> interface.
 * <p>
 * NOTE: This should be used everywhere inside deegree, don't use
 * <code>org.jaxen.SimpleNamespaceContext</code> -- this prevents unnecessary binding to Jaxen.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class NamespaceContext implements org.jaxen.NamespaceContext {

    // keys: prefices (String), values: namespaces (URI)
    private Map<String, URI> namespaceMap = new HashMap<String, URI>();

    /**
     * Creates a new instance of <code>NamespaceContext</code> with only the prefix 'xmlns:' being
     * bound.
     */
    public NamespaceContext() {
        this.namespaceMap.put( CommonNamespaces.XMLNS_PREFIX, CommonNamespaces.XMLNS );
    }

    /**
     * Creates a new instance of <code>NamespaceContext</code> that contains all the bindings from
     * the argument <code>NamespaceContext</code>.
     *
     * @param nsContext
     *            bindings to copy
     */
    public NamespaceContext( NamespaceContext nsContext ) {
        this.namespaceMap = new HashMap<String, URI>( nsContext.namespaceMap );
    }

    /**
     * @return the namespace map of the context.
     */
    public Map<String, URI> getNamespaceMap() {
        return this.namespaceMap;
    }

    /**
     * registers a new prefix with an assigned namespace URI
     *
     * @param prefix
     * @param namespace
     */
    public void addNamespace( String prefix, URI namespace ) {
        this.namespaceMap.put( prefix, namespace );
    }

    /**
     * registeres all prexifes and assigned namespaces from another NamespaceContext. If a prefix
     * already exists in this NamespaceContext it will be ignored.
     *
     * @param nsc
     */
    public void addAll( NamespaceContext nsc ) {
        Map<String, URI> map = nsc.getNamespaceMap();
        Set<String> keys = map.keySet();
        for ( String key : keys ) {
            if ( this.namespaceMap.get( key ) == null ) {
                this.namespaceMap.put( key, map.get( key ) );
            }
        }
    }

    public String translateNamespacePrefixToUri( String prefix ) {
        URI namespaceURI = this.namespaceMap.get( prefix );
        return namespaceURI == null ? null : namespaceURI.toString();
    }

    /**
     * @param prefix to get the namespace for
     * @return the URI (namespace) associated with the given prefix, or <code>null</code> if it was not found.
     */
    public URI getURI( String prefix ) {
        return this.namespaceMap.get( prefix );
    }

    @Override
    public String toString() {
        return namespaceMap.toString();
    }
}
