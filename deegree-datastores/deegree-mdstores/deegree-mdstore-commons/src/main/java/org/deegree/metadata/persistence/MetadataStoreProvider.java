//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/persistence/MetadataStoreProvider.java $
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

import org.deegree.metadata.MetadataRecord;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.standard.AbstractResourceProvider;

/**
 * Implementations plug-in {@link MetadataStore}s.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: lbuesching $
 * 
 * @version $Revision: 30800 $, $Date: 2011-05-12 16:49:44 +0200 (Do, 12. Mai 2011) $
 */
public abstract class MetadataStoreProvider extends AbstractResourceProvider<MetadataStore<? extends MetadataRecord>> {

    /**
     * Requests a list of sql statements to setup the database required for a {@link MetadataStore} implementation.
     * 
     * @param dbType
     *            never <code>null</code>
     * @return a list of sql statements to setup the database, may be empty but never <code>null</code>
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public abstract String[] getCreateStatements( SQLDialect dbType )
                            throws UnsupportedEncodingException, IOException;

    /**
     * Requests a list of sql statements to reset the database required for a {@link MetadataStore} implementation.
     * 
     * @param dbType
     *            never <code>null</code>
     * @return a list of sql statements to reset the database, may be empty but never <code>null</code>
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public abstract String[] getDropStatements( SQLDialect dbType )
                            throws UnsupportedEncodingException, IOException;

}
