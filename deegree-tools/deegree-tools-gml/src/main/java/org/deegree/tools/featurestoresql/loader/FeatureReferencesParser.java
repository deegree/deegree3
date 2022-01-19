/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.Feature;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.gml.reference.FeatureReference;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureReferencesParser implements ItemProcessor<Feature, Feature> {

    public static final String FEATURE_IDS = "FeatureIds";

    public static final String REFERENCE_IDS = "ReferenceIds";

    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Override
    public Feature process( Feature feature )
                            throws Exception {
        String featureId = feature.getId();
        List<String> references = new ArrayList<>();

        List<Property> properties = feature.getProperties();
        for ( Property property : properties ) {
            if ( property.getType() instanceof FeaturePropertyType ) {
                TypedObjectNode href = property.getValue();
                if ( href != null && href instanceof FeatureReference ) {
                    String hrefValue = ( (FeatureReference) href ).getURI();
                    if ( hrefValue.startsWith( "#" ) )
                        references.add( hrefValue );
                }
            }
        }

        putInContext( featureId, references );
        return feature;
    }

    private void putInContext( String featureId, List<String> references ) {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        if ( !executionContext.containsKey( FEATURE_IDS ) )
            executionContext.put( FEATURE_IDS, new ArrayList<String>() );
        ( (List<String>) executionContext.get( FEATURE_IDS ) ).add( featureId );

        if ( !executionContext.containsKey( REFERENCE_IDS ) )
            executionContext.put( REFERENCE_IDS, new ArrayList<String>() );
        ( (List<String>) executionContext.get( REFERENCE_IDS ) ).addAll( references );
    }

}
