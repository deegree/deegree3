//$HeadURL$
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
package org.deegree.feature.persistence.postgis;

import org.deegree.commons.tom.sql.SQLDialectHelper;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISDialect implements SQLDialectHelper {

    public String getDefaultSchema() {
        return "public";
    }

    public String stringPlus() {
        return "||";
    }

    public String stringIndex( String pattern, String string ) {
        return "POSITION(" + pattern + " IN " + string + ")";
    }

    public String cast( String expr, String type ) {
        return expr + "::" + type;
    }

    public String geometryMetadata( String dbSchema, String table, String column ) {
        return "SELECT coord_dimension,srid,type FROM public.geometry_columns WHERE f_table_schema='"
               + dbSchema.toLowerCase() + "' AND f_table_name='" + table.toLowerCase() + "' AND f_geometry_column='"
               + column.toLowerCase() + "'";
    }
}