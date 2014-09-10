//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/TransactionHelper.java $
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
package org.deegree.metadata.iso.persistence.sql;

import java.util.List;

import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.deegree.sqldialect.SQLDialect;

/**
 * Provides access to the sql service manager.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ServiceManager {

    /**
     * A read only service requesting the sql backend.
     * 
     * @param dialect
     *            never <code>null</code>
     * @param queryables
     *            may be empty but never <code>null</code>
     * @return never <code>null</code>
     */
    QueryService getQueryService( SQLDialect dialect, List<Queryable> queryables );

    /**
     * A service with transactional access to the sql backend.
     * 
     * @param dialect
     *            never <code>null</code>
     * @param queryables
     *            may be empty but never <code>null</code>
     * @param anyTextConfig
     * @return never <code>null</code>
     */
    TransactionService getTransactionService( SQLDialect dialect, List<Queryable> queryables, AnyText anyTextConfig );

}