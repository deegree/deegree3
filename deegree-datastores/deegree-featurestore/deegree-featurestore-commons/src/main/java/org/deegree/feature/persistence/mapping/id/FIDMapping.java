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
package org.deegree.feature.persistence.mapping.id;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;

/**
 * Defines the mapping between feature ids and a relational model.
 * 
 * @see FeatureTypeMapping
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class FIDMapping {

    private final String prefix;

    private final String column;

    private final PrimitiveType pt;

    private final IDGenerator generator;

    /**
     * Creates a new {@link FIDMapping} instance.
     * 
     * @param prefix
     *            static prefix for all feature ids, must not be <code>null</code> (but can be empty)
     * @param column
     *            database column that the feature ids are mapped to, must not be <code>null</code>
     * @param pt
     *            type of the database column, must not be <code>null</code>
     * @param generator
     *            generator for determining new ids, must not be <code>null</code>
     */
    public FIDMapping( String prefix, String column, PrimitiveType pt, IDGenerator generator ) {
        this.prefix = prefix;
        this.column = column;
        this.pt = pt;
        this.generator = generator;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getColumn() {
        return column;
    }

    public PrimitiveType getColumnType() {
        return pt;
    }
}