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
package org.deegree.feature.persistence.postgis;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.mapping.MappedApplicationSchema;

/**
 * Handles the creation of DDL (DataDefinitionLanguage) scripts for the {@link PostGISFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISDDLCreator {

    private final MappedApplicationSchema schema;

    /**
     * @param schema
     */
    public PostGISDDLCreator( MappedApplicationSchema schema ) {
        this.schema = schema;
    }

    /**
     * @return
     */
    public String[] getDDL() {
        List<String> ddl = new ArrayList<String>();

        // create feature_type table
        ddl.add( "CREATE TABLE feature_types (id smallint PRIMARY KEY, qname text NOT NULL)" );
        ddl.add( "COMMENT ON TABLE feature_types IS 'Ids and bboxes of concrete feature types'" );
        ddl.add( "SELECT ADDGEOMETRYCOLUMN('public', 'feature_types','bbox','-1','GEOMETRY',2);" );

        // populate feature_type table
        for ( short ftId = 0; ftId < schema.getFts(); ftId++ ) {
            QName ftName = schema.getFtName( ftId );
            ddl.add( "INSERT INTO feature_types (id,qname) VALUES (" + ftId + ",'" + ftName + "')" );
        }

        // create gml_objects table
        ddl.add( "CREATE TABLE gml_objects (id SERIAL PRIMARY KEY, "
                 + "gml_id text UNIQUE NOT NULL, ft_type smallint REFERENCES feature_types, binary_object bytea)" );
        ddl.add( "COMMENT ON TABLE gml_objects IS 'All objects (features and geometries)'" );
        ddl.add( "SELECT ADDGEOMETRYCOLUMN('public', 'gml_objects','gml_bounded_by','-1','GEOMETRY',2)" );
        ddl.add( "ALTER TABLE gml_objects ADD CONSTRAINT gml_objects_geochk CHECK (isvalid(gml_bounded_by))" );
        ddl.add( "CREATE INDEX gml_objects_sidx ON gml_objects USING GIST (gml_bounded_by GIST_GEOMETRY_OPS)" );
        // ddl.add( "CREATE TABLE gml_names (gml_object_id integer REFERENCES gml_objects,"
        // + "name text NOT NULL,codespace text,prop_idx smallint NOT NULL)" );

        return ddl.toArray( new String[ddl.size()] );
    }
}
