//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sql.idgenerator;

import java.sql.Connection;
import java.util.Properties;

import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.idgenerator.IdGenerationException;
import org.deegree.io.datastore.idgenerator.IdGenerator;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.io.datastore.sql.transaction.SQLTransaction;

/**
 * Feature id generator that produces successive (+1) values and retrieves its start value from the
 * specified table value (the maximum stored in the field).
 * <p>
 * Please note that aborted transactions will also increase ids, so feature ids may be skipped.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DBMaxIdGenerator extends IdGenerator {

    private String tableName;

    private String columnName;

    // initialized when #getNewId() is called the first time
    private int lastId = -1;

    /**
     * Creates a new <code>DBMaxIdGenerator</code> instance.
     * <p>
     * Supported configuration parameters: <table>
     * <tr>
     * <th>Name</th>
     * <th>optional?</th>
     * <th>Usage</th>
     * </tr>
     * <tr>
     * <td>table</td>
     * <td>no</td>
     * <td>name of the table where the id field is stored</td>
     * </tr>
     * <tr>
     * <td>column</td>
     * <td>no</td>
     * <td>name of the id field</td>
     * </tr>
     * </table>
     * 
     * @param params
     *            configuration parameters
     * @throws IdGenerationException
     */
    public DBMaxIdGenerator( Properties params ) throws IdGenerationException {
        super( params );
        this.tableName = params.getProperty( "table" );
        this.columnName = params.getProperty( "column" );
        if ( this.tableName == null || this.columnName == null ) {
            String msg = "DBMaxIdGenerator requires 'table' and 'column' parameters.";
            throw new IdGenerationException( msg );
        }
    }

    /**
     * Returns a new primary key.
     * 
     * @param ta
     *            datastore transaction (context)
     * @return a new primary key.
     * @throws IdGenerationException
     *             if the generation of the id could not be performed
     */
    @Override
    public Object getNewId( DatastoreTransaction ta )
                            throws IdGenerationException {

        if ( this.lastId == -1 ) {
            initLastId( ta );
        }

        return ++this.lastId;
    }

    /**
     * Initialized the lastId field with the maximum value stored in the specified table field.
     * 
     * @param ta
     *            datastore transaction (context)
     * @throws IdGenerationException
     */
    private void initLastId( DatastoreTransaction ta )
                            throws IdGenerationException {

        if ( !( ta instanceof SQLTransaction ) ) {
            String msg = "DBMaxIdGenerator can only be used with SQL based datastores.";
            throw new IllegalArgumentException( msg );
        }

        try {
            AbstractSQLDatastore ds = (AbstractSQLDatastore) ta.getDatastore();
            Connection conn = ( (SQLTransaction) ta ).getConnection();
            this.lastId = ds.getMaxValue( conn, this.tableName, this.columnName );
        } catch ( DatastoreException e ) {
            throw new IdGenerationException( e.getMessage(), e );
        }
    }

    /**
     * Returns a new id for a feature of the given type.
     * 
     * @param ft
     *            (mapped) feature type
     * @return a new feature id.
     * @throws IdGenerationException
     *             if the generation of the id could not be performed
     */
    @Override
    public FeatureId getNewId( MappedFeatureType ft, DatastoreTransaction ta )
                            throws IdGenerationException {

        MappedGMLId fidDefinition = ft.getGMLId();
        if ( fidDefinition.getKeySize() != 1 ) {
            String msg = "Cannot generate feature ids that are mapped to more than one column.";
            throw new IdGenerationException( msg );
        }

        if ( this.lastId == -1 ) {
            initLastId( ta );
        }

        FeatureId fid = new FeatureId( ft, new Object[] { ++this.lastId } );
        return fid;
    }
}
