//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services;

import java.net.URL;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.services.controller.OGCFrontController;

/**
 * Implementations provide OWS implementations for plugging into the {@link OGCFrontController}.
 * 
 * {@link OWS}
 * {@link OGCFrontController}
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public interface OWSProvider {

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
     * Creates a new {@link FeatureStore} instance from the given configuration document.
     * 
     * @param configURL
     *            location of the configuration document, must not be <code>null</code>
     * @return new feature store instance, configured, not initialized yet
     * @throws FeatureStoreException
     *             if the configuration contains an error or creation fails
     */
    public FeatureStore getFeatureStore( URL configURL )
                            throws FeatureStoreException;
}