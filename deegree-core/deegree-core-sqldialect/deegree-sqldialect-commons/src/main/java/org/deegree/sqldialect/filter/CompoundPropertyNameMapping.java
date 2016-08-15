package org.deegree.sqldialect.filter;

import org.deegree.filter.expression.ValueReference;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a list of {@link PropertyNameMapping}s (sub elements of a CompoundMapping).
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class CompoundPropertyNameMapping {

    private final Map<QName, PropertyNameMapping> propertyNameMappings = new HashMap<QName, PropertyNameMapping>();

    /**
     * Instantiates a {@link CompoundPropertyNameMapping} without {@link PropertyNameMapping}s, add them with {@link #addPropertyNameMapper(QName, PropertyNameMapping)}
     */
    public CompoundPropertyNameMapping() {
    }

    /**
     * Adda a {@link PropertyNameMapping}.
     *
     * @param propertyNameMappingName the name of the {@link PropertyNameMapping} to add, never <code>null</code>
     * @param propertyNameMapping     the {@link PropertyNameMapping} to add, never <code>null</code>
     */
    public void addPropertyNameMapper( QName propertyNameMappingName, PropertyNameMapping propertyNameMapping ) {
        propertyNameMappings.put( propertyNameMappingName, propertyNameMapping );
    }

    /**
     * @return the {@link PropertyNameMapping}s for this name, <code>null</code> if not available.
     */
    public PropertyNameMapping getPropertyNameMappings( QName propertyNameMappingName ) {
        return propertyNameMappings.get( propertyNameMappingName );
    }

}