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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.QNameUtils;
import org.deegree.feature.persistence.sql.xpath.MappableNameStep;
import org.deegree.feature.persistence.sql.xpath.MappableStep;
import org.deegree.feature.persistence.sql.xpath.MappedXPath;
import org.deegree.feature.persistence.sql.xpath.QueryFeatureTypeMapping;
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

    private final QueryFeatureTypeMapping ftMapping;

    private final Collection<QueryFeatureTypeMapping> ftMappings;

    /**
     * @param fs
     *            the associated feature {@link SQLFeatureStore}, never <code>null</code>
     * @param ftMapping
     *            the {@link FeatureTypeMapping}, never <code>null</code>
     */
    public SQLPropertyNameMapper( SQLFeatureStore fs, QueryFeatureTypeMapping ftMapping ) {
        this( fs, ftMapping, null );
    }

    /**
     * @param fs
     *            the associated feature {@link SQLFeatureStore}, never <code>null</code>
     * @param ftMappings
     *            a list of the {@link FeatureTypeMapping}s, never <code>null</code> or empty
     */
    public SQLPropertyNameMapper( SQLFeatureStore fs, Collection<QueryFeatureTypeMapping> ftMappings ) {
        this( fs, null, ftMappings );
    }

    private SQLPropertyNameMapper( SQLFeatureStore fs, QueryFeatureTypeMapping ftMapping,
                                   Collection<QueryFeatureTypeMapping> ftMappings ) {
        this.fs = fs;
        if ( ftMapping != null && ( ftMappings == null || ftMappings.isEmpty() ) ) {
            this.ftMapping = ftMapping;
            this.ftMappings = null;
        } else if ( ftMapping == null && ( ftMappings != null && ftMappings.size() == 1 ) ) {
            this.ftMapping = ftMappings.iterator().next();
            this.ftMappings = null;
        } else if ( ftMapping == null && ( ftMappings != null && ftMappings.size() > 1 ) ) {
            this.ftMapping = null;
            this.ftMappings = ftMappings;
        } else {
            throw new IllegalArgumentException( "At least one feature type mapping is required." );
        }
    }

    @Override
    public PropertyNameMapping getMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        if ( ftMapping != null || propName == null || propName.getAsText().isEmpty() )
            return new MappedXPath( fs, ftMapping, propName, aliasManager, false ).getPropertyNameMapping();
        QueryFeatureTypeMapping correspondingFtMapping = findCorrespondingMapping( propName );
        return new MappedXPath( fs, correspondingFtMapping, propName, aliasManager, false ).getPropertyNameMapping();

    }

    @Override
    public PropertyNameMapping getSpatialMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        if ( ftMapping != null || propName == null || propName.getAsText().isEmpty() )
            return new MappedXPath( fs, ftMapping, propName, aliasManager, true ).getPropertyNameMapping();
        QueryFeatureTypeMapping correspondingFtMapping = findCorrespondingMapping( propName );
        return new MappedXPath( fs, correspondingFtMapping, propName, aliasManager, true ).getPropertyNameMapping();
    }

    private QueryFeatureTypeMapping findCorrespondingMapping( ValueReference propName )
                            throws UnmappableException {
        List<MappableStep> steps = MappableStep.extractSteps( propName );
        if ( steps.size() > 1 && steps.get( 0 ) instanceof MappableNameStep ) {
            QName nodeName = ( (MappableNameStep) steps.get( 0 ) ).getNodeName();
            QueryFeatureTypeMapping ftMapping = findBestMatchingFtMapping( nodeName );
            if ( ftMapping != null )
                return ftMapping;
        }
        throw new UnmappableException( "Could not parse mapping " + propName );
    }

    private QueryFeatureTypeMapping findBestMatchingFtMapping( QName nodeName ) {
        List<QName> ftNames = new ArrayList<QName>();
        for ( QueryFeatureTypeMapping ftMapping : ftMappings ) {
            ftNames.add( ftMapping.getFeatureTypeMapping().getFeatureType() );
        }

        QName bestMatch = QNameUtils.findBestMatch( nodeName, ftNames );
        for ( QueryFeatureTypeMapping ftMapping : ftMappings ) {
            if ( ftMapping.getFeatureTypeMapping().getFeatureType().equals( bestMatch ) )
                return ftMapping;
        }

        // matching alias?
        for ( QueryFeatureTypeMapping ftMapping : ftMappings ) {
            String alias = ftMapping.getAlias();
            if ( nodeName.getLocalPart().equals( alias ) )
                return ftMapping;
        }
        return null;
    }

}