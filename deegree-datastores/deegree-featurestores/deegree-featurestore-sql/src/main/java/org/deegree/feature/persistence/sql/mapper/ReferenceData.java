package org.deegree.feature.persistence.sql.mapper;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface ReferenceData {

    /**
     * @param featureTypeName
     *                 the name of the feature type, never <code>null</code>
     * @param xpath
     *                 the steps describing the path to the feature, may be empty. but never <code>null</code>
     * @return <code>true</code> if the property identified by the path occurs one or zero times, <code>false</code> otherwise
     */
    boolean hasZeroOrOneProperty( QName featureTypeName, List<QName> xpath );

    /**
     * @param featureTypeName
     *                 the name of the feature type, never <code>null</code>
     * @return <code>true</code> if the feature type with this name should be mapped, <code>false</code> otherwise
     */
    boolean shouldFeatureTypeMapped( QName featureTypeName );

}