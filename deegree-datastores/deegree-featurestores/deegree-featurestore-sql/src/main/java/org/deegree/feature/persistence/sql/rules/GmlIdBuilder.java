package org.deegree.feature.persistence.sql.rules;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;

import java.sql.SQLException;
import java.util.List;

/**
 * Builder for gml:ids.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlIdBuilder {

    /**
     * Creates a gml:Id for a feature from a list of id values and a version.
     *
     * @param ftMapping      the configured mapping for the feature type, never <code>null</code>
     * @param idColumnValues a list of id values, never <code>null</code> or empty (must contain at least one value)
     * @param version        an optional version to append to the gml:id, the version will be ignored if <= 0
     * @return a valid gml id, never <code>null</code>
     * @throws SQLException
     */
    public String buildGmlId( FeatureTypeMapping ftMapping, List<Object> idColumnValues, int version )
                            throws SQLException {
        List<Pair<SQLIdentifier, BaseType>> fidColumns = ftMapping.getFidMapping().getColumns();

        StringBuilder gmlId = new StringBuilder();
        gmlId.append( ftMapping.getFidMapping().getPrefix() );
        gmlId.append( idColumnValues.get( 0 ) );
        for ( int i = 1; i < fidColumns.size(); i++ ) {
            gmlId.append( ftMapping.getFidMapping().getDelimiter() );
            gmlId.append( idColumnValues.get( i ) );
        }
        if ( version > 0 )
            gmlId.append( "_version" ).append( version );
        return gmlId.toString();
    }

}
