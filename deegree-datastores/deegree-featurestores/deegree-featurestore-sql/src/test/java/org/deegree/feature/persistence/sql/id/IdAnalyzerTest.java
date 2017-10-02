/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2017 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit GmbH -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.sql.id;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IdAnalyzerTest {

    private static final String FEATURE_NAME = "NAME";
    private static final String PREFIX = "p";

    private IdAnalyzer idAnalyzer;

    @Before
    public void setUp() throws Exception {
        idAnalyzer = new IdAnalyzer( createMappedAppSchemaMock() );
    }

    @Test
    public void usingSingleLetterPrefixResultsToCorrectFeatureName() throws Exception {
        IdAnalysis analysis = idAnalyzer.analyze( PREFIX + FEATURE_NAME );
        assertEquals( analysis.getIdKernels()[0], FEATURE_NAME );
    }

    @Test
    public void usingDoubleLetterPrefixResultsToCorrectFeatureName() throws Exception {
        IdAnalysis analysis = idAnalyzer.analyze( PREFIX + PREFIX + FEATURE_NAME );
        assertEquals( analysis.getIdKernels()[0], FEATURE_NAME );
    }

    private MappedAppSchema createMappedAppSchemaMock() {
        QName featureA = new QName( "A" );
        QName featureB = new QName( "B" );
        return new MappedAppSchema(
                new FeatureType[]{
                        new GenericFeatureType( featureA, Collections.<PropertyType>emptyList(), false ),
                        new GenericFeatureType( featureB, Collections.<PropertyType>emptyList(), false )
                },
                Collections.<FeatureType, FeatureType>emptyMap(),
                Collections.<String, String>emptyMap(),
                null,
                new FeatureTypeMapping[]{
                        createFeatureTypeMapping( featureA, PREFIX ),
                        createFeatureTypeMapping( featureB, ( PREFIX + PREFIX ) )
                },
                null,
                null,
                null,
                true,
                null,
                Collections.<GMLObjectType>emptyList(),
                Collections.<GMLObjectType, GMLObjectType>emptyMap()
        );
    }

    private FeatureTypeMapping createFeatureTypeMapping( QName featureName, String prefix ) {
        return new FeatureTypeMapping( featureName,
                new TableName( "TABLE_" + featureName.getLocalPart() + "_IDENTIFIER" ),
                new FIDMapping( prefix, ",", Collections.<Pair<SQLIdentifier, BaseType>>emptyList(), null ),
                Collections.<Mapping>emptyList() );
    }

}