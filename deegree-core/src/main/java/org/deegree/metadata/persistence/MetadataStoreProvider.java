//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.metadata.persistence;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Implementations plug-in {@link MetadataStore}s.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface MetadataStoreProvider {

    /**
     * Returns the namespace for configurations documents that this provider handles.
     * 
     * @return the namespace for configurations documents, never <code>null</code>
     */
    public String getConfigNamespace();

    /**
     * Returns the URL for retrieving the configuration document schema.
     * 
     * @return the URL for retrieving the configuration document schema, may be <code>null</code>
     */
    public URL getConfigSchema();

    /**
     * Returns the URL for retrieving the configuration document template.
     * 
     * @return the URL for retrieving the configuration document template, may be <code>null</code>
     */
    public URL getConfigTemplate();

    /**
     * Creates a new {@link MetadataStore} instance from the given configuration document.
     * 
     * @param configURL
     *            location of the configuration document, must not be <code>null</code>
     * @return new feature store instance, configured, not initialized yet
     * @throws MetadataStore
     *             if the configuration contains an error or creation fails
     */
    public MetadataStore getMetadataStore( URL configURL )
                            throws MetadataStoreException;

    public String[] getCreateStatements( URL configURL )
                            throws UnsupportedEncodingException, IOException;

    public String[] getDropStatements( URL configURL )
                            throws UnsupportedEncodingException, IOException;
}