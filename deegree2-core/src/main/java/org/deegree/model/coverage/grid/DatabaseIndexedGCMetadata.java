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

package org.deegree.model.coverage.grid;

import org.deegree.io.JDBCConnection;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DatabaseIndexedGCMetadata {

    private JDBCConnection jdbc;

    private float scale;

    private String table;

    private String rootDir;

    private boolean dataInDB = false;

    /**
     * @param jdbc
     * @param scale
     * @param table
     * @param rootDir
     * @param dataInDB
     */
    public DatabaseIndexedGCMetadata( JDBCConnection jdbc, float scale, String table, String rootDir, boolean dataInDB ) {
        this.jdbc = jdbc;
        this.scale = scale;
        this.table = table;
        this.dataInDB = dataInDB;
        this.rootDir = rootDir;
    }

    /**
     * @return the dataInDB
     */
    public boolean areDataStoredInDB() {
        return dataInDB;
    }

    /**
     * @return the jdbc
     */
    public JDBCConnection getJDBCConnection() {
        return jdbc;
    }

    /**
     * @return the resolution
     */
    public float getScale() {
        return scale;
    }

    /**
     * @return the table
     */
    public String getTable() {
        return table;
    }

    /**
     * @return the rootDir
     */
    public String getRootDir() {
        return rootDir;
    }

}
