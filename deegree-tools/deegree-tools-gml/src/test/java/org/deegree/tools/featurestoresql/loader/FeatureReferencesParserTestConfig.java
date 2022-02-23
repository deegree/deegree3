package org.deegree.tools.featurestoresql.loader;

import org.deegree.feature.Feature;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Configuration
@EnableBatchProcessing
public class FeatureReferencesParserTestConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }

    @StepScope
    @Bean
    public GmlReader gmlReader( @Value("#{jobParameters[pathToFile]}") String pathToFile ) {
        GmlReader gmlReader = new GmlReader( null );
        gmlReader.setResource( new PathResource( pathToFile ) );
        return gmlReader;
    }

    @StepScope
    @Bean
    public FeatureReferencesParser featureReferencesParser() {
        return new FeatureReferencesParser();
    }

    @Bean
    public ItemWriter itemWriter() {
        return new ItemWriter<Feature>() {
            public void write(java.util.List<? extends Feature> items) throws java.lang.Exception {
                System.out.println(items.toString());
            }
        };
    }

    @Bean
    public Step step( GmlReader gmlReader, FeatureReferencesParser featureReferencesParser, ItemWriter itemWriter ) {
        return stepBuilderFactory.get( "FeatureReferencesParserTestStep" ).<Feature, Feature> chunk( 10 ).reader( gmlReader ).processor( featureReferencesParser ).writer( itemWriter ).build();
    }

    @Bean
    public Job job( Step step )
                            throws Exception {
        return jobBuilderFactory.get( "FeatureReferencesParserTestJob" ).incrementer( new RunIdIncrementer() ).start( step ).build();
    }
}
