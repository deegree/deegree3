package org.deegree.tools.featurestoresql.loader;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureReferenceCheckerTest {

    private FeatureReferenceChecker featureReferenceChecker = new FeatureReferenceChecker();

    @Test
    public void testCheckReferences() {
        List<String> featureIds = asList( "123", "456", "789" );
        ;
        List<String> references = asList( "123" );
        FeatureReferenceCheckResult result = featureReferenceChecker.checkReferences( featureIds, references );

        assertThat( result.isValid(), is( true ) );
        assertThat( result.getUnresolvableReferences().size(), is( 0 ) );
    }

    @Test
    public void testCheckReferences_unresolvableReferences() {
        List<String> featureIds = asList( "123", "456", "789" );
        List<String> references = asList( "123", "#abc", "ghi" );
        FeatureReferenceCheckResult result = featureReferenceChecker.checkReferences( featureIds, references );

        assertThat( result.isValid(), is( false ) );
        assertThat( result.getUnresolvableReferences().size(), is( 2 ) );
        assertThat( result.getUnresolvableReferences(), hasItem( "abc" ) );
        assertThat( result.getUnresolvableReferences(), hasItem( "ghi" ) );
    }

    @Test
    public void testCheckReferences_emptyLists() {
        List<String> featureIds = new ArrayList<>();
        List<String> references = new ArrayList<>();
        FeatureReferenceCheckResult result = featureReferenceChecker.checkReferences( featureIds, references );

        assertThat( result.isValid(), is( true ) );
    }

    @Test
    public void testCheckReferences_nullLists() {
        List<String> featureIds = null;
        List<String> references = null;
        FeatureReferenceCheckResult result = featureReferenceChecker.checkReferences( featureIds, references );

        assertThat( result.isValid(), is( true ) );
    }

    @Test
    public void testCheckReferences_nullFeatureIds() {
        List<String> featureIds = null;
        List<String> references = asList( "#abc", "123" );
        FeatureReferenceCheckResult result = featureReferenceChecker.checkReferences( featureIds, references );

        assertThat( result.isValid(), is( false ) );
        assertThat( result.getUnresolvableReferences().size(), is( 2 ) );
        assertThat( result.getUnresolvableReferences(), hasItem( "abc" ) );
        assertThat( result.getUnresolvableReferences(), hasItem( "123" ) );
    }
}