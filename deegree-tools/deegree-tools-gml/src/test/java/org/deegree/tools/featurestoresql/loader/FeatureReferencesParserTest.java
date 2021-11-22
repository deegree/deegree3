package org.deegree.tools.featurestoresql.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { FeatureReferencesParserTestConfig.class })
public class FeatureReferencesParserTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void testProcess_cadastralparcels()
                            throws Exception {
        JobParameters jobParameters = createJobParameters( "cadastralparcels.xml" );
        ExecutionContext executionContext = new ExecutionContext();
        JobExecution jobExecution = jobLauncherTestUtils.launchStep( "FeatureReferencesParserTestStep", jobParameters,
                                                                     executionContext );

        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        assertThat( stepExecutions.size(), is( 1 ) );

        ExecutionContext stepExecutionContext = stepExecutions.iterator().next().getExecutionContext();

        List<String> featureIds = (List<String>) stepExecutionContext.get( FeatureReferencesParser.FEATURE_IDS );
        assertThat( featureIds, is( notNullValue() ) );
        assertThat( featureIds.size(), is( 5 ) );

        List<String> referenceIds = (List<String>) stepExecutionContext.get( FeatureReferencesParser.REFERENCE_IDS );
        assertThat( referenceIds, is( notNullValue() ) );
        assertThat( referenceIds.size(), is( 0 ) );
    }

    @Test
    public void testProcess_cadastralzonings()
                            throws Exception {
        JobParameters jobParameters = createJobParameters( "cadastralzonings.xml" );
        ExecutionContext executionContext = new ExecutionContext();
        JobExecution jobExecution = jobLauncherTestUtils.launchStep( "FeatureReferencesParserTestStep", jobParameters,
                                                                     executionContext );

        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        assertThat( stepExecutions.size(), is( 1 ) );

        ExecutionContext stepExecutionContext = stepExecutions.iterator().next().getExecutionContext();

        List<String> featureIds = (List<String>) stepExecutionContext.get( FeatureReferencesParser.FEATURE_IDS );
        assertThat( featureIds, is( notNullValue() ) );
        assertThat( featureIds.size(), is( 2 ) );

        List<String> referenceIds = (List<String>) stepExecutionContext.get( FeatureReferencesParser.REFERENCE_IDS );
        assertThat( referenceIds, is( notNullValue() ) );
        assertThat( referenceIds.size(), is( 1 ) );
    }

    private JobParameters createJobParameters( String resourceName )
                            throws IOException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        InputStream resourceAsStream = FeatureReferencesParserTest.class.getResourceAsStream( resourceName );
        File file = File.createTempFile( "FeatureReferencesParserTest", ".xml" );
        FileOutputStream fos = new FileOutputStream( file );
        IOUtils.copy( resourceAsStream, fos );
        String pathToFile = file.toString();
        fos.close();
        return jobParametersBuilder.addString( "pathToFile", pathToFile ).addDate( "date", new Date() ).toJobParameters();
    }

}