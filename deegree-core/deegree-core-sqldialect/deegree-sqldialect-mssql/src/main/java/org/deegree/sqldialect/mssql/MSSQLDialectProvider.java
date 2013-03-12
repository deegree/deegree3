//$HeadURL: svn+ssh://goerke@criador:2222/srv/svn/deegree-intern/trunk/latlon-sqldialect-mssql/src/main/java/de/latlon/deegree/sqldialect/mssql/MSSQLDialectProvider.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.sqldialect.mssql;

import static org.deegree.commons.jdbc.ConnectionManager.Type.MSSQL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SQLDialectProvider;

/**
 * {@link SQLDialectProvider} for Microsoft SQL databases.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: 295 $, $Date: 2011-06-09 16:48:47 +0200 (Do, 09 Jun 2011) $
 */
public class MSSQLDialectProvider implements SQLDialectProvider {

    public Type getSupportedType() {
        return MSSQL;
    }

    @Override
    public SQLDialect create( String connId, DeegreeWorkspace ws )
                            throws ResourceInitException {
        return new MSSQLDialect();
    }
}
