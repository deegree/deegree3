//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql;

import org.deegree.feature.persistence.sql.xpath.MappedXPath;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;

/**
 * {@link PropertyNameMapper} for the {@link SQLFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLPropertyNameMapper implements PropertyNameMapper {

    private final SQLFeatureStore fs;

    private final FeatureTypeMapping ftMapping;

    private final boolean handleStrict;

    public SQLPropertyNameMapper( SQLFeatureStore fs, FeatureTypeMapping ftMapping, boolean handleStrict ) {
        this.fs = fs;
        this.ftMapping = ftMapping;
        this.handleStrict = handleStrict;
    }

    @Override
    public PropertyNameMapping getMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        return new MappedXPath( fs, ftMapping, propName, aliasManager, false, handleStrict ).getPropertyNameMapping();
    }

    @Override
    public PropertyNameMapping getSpatialMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        return new MappedXPath( fs, ftMapping, propName, aliasManager, true, handleStrict ).getPropertyNameMapping();
    }
}
