//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.cs.persistence;

import java.net.URL;
import java.util.ServiceLoader;

import org.deegree.commons.config.ResourceProvider;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.workspace.Workspace;

/**
 * Base interface for all {@link CRSStoreProvider} which must have an default constructor to be loaded by the
 * {@link ServiceLoader}.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public interface CRSStoreProvider extends ResourceProvider {

    /**
     * Creates a new {@link CRSStore} instance from the given configuration document.
     * 
     * @param configURL
     *            location of the configuration document, must not be <code>null</code>
     * @param workspace
     * @return new crs store instance, configured, not initialized yet
     */
    public CRSStore getCRSStore( URL configURL, Workspace workspace )
                            throws CRSStoreException;

}
