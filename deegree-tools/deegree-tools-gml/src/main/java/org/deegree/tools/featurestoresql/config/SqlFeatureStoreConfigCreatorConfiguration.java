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
package org.deegree.tools.featurestoresql.config;

import org.deegree.feature.types.AppSchema;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;

/**
 * Configuration of the FeatureStoreLoader.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Configuration
@EnableBatchProcessing
public class SqlFeatureStoreConfigCreatorConfiguration {

	@StepScope
	@Bean
	public LoadParameter parseJobParameter(@Value("#{jobParameters[schemaUrl]}") String schemaUrl,
			@Value("#{jobParameters[format]}") String format, @Value("#{jobParameters[srid]}") String srid,
			@Value("#{jobParameters[idtype]}") String idtype, @Value("#{jobParameters[mapping]}") String mapping,
			@Value("#{jobParameters[dialect]}") String dialect, @Value("#{jobParameters[dbschema]}") String dbSchema,
			@Value("#{jobParameters[cycledepth]}") String depth,
			@Value("#{jobParameters[listOfPropertiesWithPrimitiveHref]}") String listOfPropertiesWithPrimitiveHref,
			@Value("#{jobParameters[referenceData]}") String referenceData,
			@Value("#{jobParameters[useRefDataProps]}") String useRefDataProps) {
		return new LoadParameterBuilder().setSchemaUrl(schemaUrl)
			.setFormat(format)
			.setSrid(srid)
			.setIdType(idtype)
			.setMappingType(mapping)
			.setDialect(dialect)
			.setDbSchema(dbSchema)
			.setDepth(depth)
			.setListOfPropertiesWithPrimitiveHref(listOfPropertiesWithPrimitiveHref)
			.setReferenceData(referenceData)
			.setuseRefDataProps(useRefDataProps)
			.build();
	}

	@StepScope
	@Bean
	public AppSchemaReader appSchemaReader(LoadParameter loadParameter) {
		return new AppSchemaReader(loadParameter.getSchemaUrl());
	}

	@StepScope
	@Bean
	public FeatureStoreConfigWriter featureStoreConfigWriter(LoadParameter loadParameter) {
		return new FeatureStoreConfigWriter(loadParameter);
	}

	@Bean
	public Step step(AppSchemaReader appSchemaReader, FeatureStoreConfigWriter featureStoreConfigWriter,
			JobRepository jobRepository, JdbcTransactionManager transactionManager) {
		StepBuilder stepBuilder = new StepBuilder("featureStoreConfigLoaderStep", jobRepository);
		return new SimpleStepBuilder<AppSchema, AppSchema>(stepBuilder).<AppSchema, AppSchema>chunk(1)
			.transactionManager(transactionManager)
			.reader(appSchemaReader)
			.writer(featureStoreConfigWriter)
			.build();
	}

	@Bean
	public Job job(Step step, JobRepository jobRepository) {
		return new JobBuilder("featureStoreConfigLoaderJob", jobRepository).incrementer(new RunIdIncrementer())
			.start(step)
			.build();
	}

}