package org.deegree.tools.featurestoresql.config;

import org.deegree.feature.types.AppSchema;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the FeatureStoreLoader.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Configuration
@EnableBatchProcessing
public class FeatureStoreLoaderConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @StepScope
    @Bean
    public LoadParameter parseJobParameter( @Value("#{jobParameters[schemaUrl]}") String schemaUrl,
                                            @Value("#{jobParameters[format]}") String format,
                                            @Value("#{jobParameters[srid]}") String srid,
                                            @Value("#{jobParameters[idtype]}") String idtype,
                                            @Value("#{jobParameters[mapping]}") String mapping,
                                            @Value("#{jobParameters[dialect]}") String dialect,
                                            @Value("#{jobParameters[cycledepth]}") String depth,
                                            @Value("#{jobParameters[listOfPropertiesWithPrimitiveHref]}") String listOfPropertiesWithPrimitiveHref,
                                            @Value("#{jobParameters[referenceData]}") String referenceData ) {
        return new LoadParameterBuilder().setSchemaUrl( schemaUrl ).setFormat( format ).setSrid( srid ).setIdType(
                        idtype ).setMappingType( mapping ).setDialect( dialect ).setDepth(
                        depth ).setListOfPropertiesWithPrimitiveHref(
                        listOfPropertiesWithPrimitiveHref ).setReferenceData( referenceData ).build();
    }

    @StepScope
    @Bean
    public AppSchemaReader appSchemaReader( LoadParameter loadParameter ) {
        return new AppSchemaReader( loadParameter.getSchemaUrl() );
    }

    @StepScope
    @Bean
    public FeatureStoreConfigWriter featureStoreConfigWriter( LoadParameter loadParameter ) {
        return new FeatureStoreConfigWriter( loadParameter );
    }

    @Bean
    public Step step( AppSchemaReader appSchemaReader, FeatureStoreConfigWriter featureStoreConfigWriter ) {
        return stepBuilderFactory.get( "featureStoreConfigLoaderStep" ).<AppSchema, AppSchema> chunk( 1 ).reader( appSchemaReader ).writer( featureStoreConfigWriter ).build();
    }

    @Bean
    public Job job( Step step ) {
        return jobBuilderFactory.get( "featureStoreConfigLoaderJob" ).incrementer( new RunIdIncrementer() ).start( step ).build();
    }

}