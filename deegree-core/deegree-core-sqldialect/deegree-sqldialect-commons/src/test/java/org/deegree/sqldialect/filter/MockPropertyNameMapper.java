package org.deegree.sqldialect.filter;

import org.deegree.filter.expression.ValueReference;

/**
 * Mock implementation of {@link PropertyNameMapper} that maps any {@link ValueReference} to a column with the same
 * name, except for {@link ValueReference}s that start with "UNMAPPABLE_".
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus schneider</a>
 *
 * @since 3.4
 */
public class MockPropertyNameMapper implements PropertyNameMapper {

    @Override
    public PropertyNameMapping getSpatialMapping( ValueReference propName, TableAliasManager aliasManager ) {
        if ( isMappable( propName ) ) {
            return new PropertyNameMapping( null, null, propName.getAsQName().getLocalPart(), null );
        }
        return null;
    }

    @Override
    public PropertyNameMapping getMapping( ValueReference propName, TableAliasManager aliasManager ) {
        if ( isMappable( propName ) ) {
            return new PropertyNameMapping( null, null, propName.getAsQName().getLocalPart(), null );
        }
        return null;
    }

    private boolean isMappable( final ValueReference propName ) {
        return !propName.getAsQName().getLocalPart().startsWith( "UNMAPPABLE" );
    }

}
