//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.sqldialect.filter;

import java.util.ArrayList;
import java.util.List;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;

/**
 * Combines {@link PropertyNameMapper}s.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class CombinedPropertyNameMapper implements PropertyNameMapper {

    private final List<PropertyNameMapper> propertyNameMappers;

    /**
     * Instantiates an {@link CombinedPropertyNameMapper} without propertyNameMappers.
     */
    public CombinedPropertyNameMapper() {
        this( new ArrayList<PropertyNameMapper>() );
    }

    /**
     * Instantiates an {@link CombinedPropertyNameMapper} with a list of propertyNameMappers.
     * 
     * @param propertyNameMappers
     *            never <code>null</code>
     */
    public CombinedPropertyNameMapper( List<PropertyNameMapper> propertyNameMappers ) {
        this.propertyNameMappers = propertyNameMappers;
    }

    /**
     * @param mapperToAdd
     *            added to the list of combined propertyNameMappers, never <code>null</code>
     */
    public void addPropertyNameMapper( PropertyNameMapper mapperToAdd ) {
        propertyNameMappers.add( mapperToAdd );
    }

    @Override
    public PropertyNameMapping getMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        for ( PropertyNameMapper propertyNameMapper : propertyNameMappers ) {
            PropertyNameMapping mapping = propertyNameMapper.getMapping( propName, aliasManager );
            if ( mapping != null )
                return mapping;
        }
        return null;
    }

    @Override
    public PropertyNameMapping getSpatialMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        for ( PropertyNameMapper propertyNameMapper : propertyNameMappers ) {
            PropertyNameMapping mapping = propertyNameMapper.getSpatialMapping( propName, aliasManager );
            if ( mapping != null )
                return mapping;
        }
        return null;
    }

}