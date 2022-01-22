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

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
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
import org.springframework.core.io.PathResource;

/**
 * Configuration of the GMLLoader.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Configuration
@EnableBatchProcessing
public class GmlLoaderConfiguration {

    private static final Logger LOG = getLogger( GmlLoaderConfiguration.class );

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Summary summary() {
        return new Summary();
    }

    @Bean
    public ReportWriter reportWriter( Summary summary ) {
        Path outputFile = Paths.get( "GmlLoader.log" );
        return new ReportWriter( summary, outputFile );
    }

    @StepScope
    @Bean
    public TransactionHandler transactionHandler( SQLFeatureStore sqlFeatureStore, Summary summary ) {
        return new TransactionHandler( sqlFeatureStore, summary );
    }

    @StepScope
    @Bean
    public GmlReader gmlReader( SQLFeatureStore sqlFeatureStore,
                                @Value("#{jobParameters[pathToFile]}") String pathToFile,
                                @Value("#{jobParameters[disabledResources]}") String disabledResources ) {
        GmlReader gmlReader = new GmlReader( sqlFeatureStore );
        gmlReader.setResource( new PathResource( pathToFile ) );
        gmlReader.setDisabledResources( parseDisabledResources( disabledResources ) );
        return gmlReader;
    }

    @StepScope
    @Bean
    public FeatureReferencesParser featureReferencesParser() {
        return new FeatureReferencesParser();
    }

    @StepScope
    @Bean
    public FeatureStoreWriter featureStoreWriter( SQLFeatureStore sqlFeatureStore, Summary summary ) {
        return new FeatureStoreWriter( sqlFeatureStore, summary );
    }

    @StepScope
    @Bean
    public SQLFeatureStore sqlFeatureStore( @Value("#{jobParameters[workspaceName]}") String workspaceName,
                                            @Value("#{jobParameters[sqlFeatureStoreId]}") String sqlFeatureStoreId )
                            throws Exception {
        DeegreeWorkspace workspace = DeegreeWorkspace.getInstance( workspaceName );
        workspace.initAll();
        LOG.info( "deegree workspace directory: [" + workspace.getLocation() + "] initialized" );
        Workspace newWorkspace = workspace.getNewWorkspace();
        SQLFeatureStore featureStore = (SQLFeatureStore) newWorkspace.getResource( FeatureStoreProvider.class,
                                                                                   sqlFeatureStoreId );
        LOG.info( "SQLFeatureStore: [" + sqlFeatureStoreId + "] requested." );
        if ( featureStore == null )
            throw new IllegalArgumentException( "SQLFeatureStore with ID " + sqlFeatureStoreId + " in workspace "
                                                + workspaceName
                                                + " does not exist or could not be initialised successful." );
        return featureStore;
    }

    @Bean
    public Step step( TransactionHandler transactionHandler, GmlReader gmlReader,
                            FeatureReferencesParser featureReferencesParser, FeatureStoreWriter featureStoreWriter ) {
        return stepBuilderFactory.get( "gmlLoaderStep" ).<Feature, Feature> chunk( 10 ).reader( gmlReader ).processor( featureReferencesParser ).writer( featureStoreWriter ).listener( transactionHandler ).build();
    }

    @Bean
    public Job job( Step step, ReportWriter reportWriter ) {
        return jobBuilderFactory.get( "gmlLoaderJob" ).incrementer( new RunIdIncrementer() ).start( step ).listener(
                        reportWriter ).build();
    }

    private List<String> parseDisabledResources( String disabledResources ) {
        List<String> patterns = new ArrayList<>();
        if ( disabledResources != null ) {
            String[] split = disabledResources.split( "," );
            for ( String resource : split ) {
                String pattern = resource.trim();
                if ( !pattern.isEmpty() )
                    patterns.add( pattern );
            }
        }
        return patterns;
    }

}
