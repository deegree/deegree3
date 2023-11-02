/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * Copyright (C) 2022 grit graphische Informationstechnik Beratungsgesellschaft mbH
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

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configuration of the GMLLoader.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
@Configuration
@EnableBatchProcessing
public class GmlLoaderConfiguration {

	private static final Logger LOG = getLogger(GmlLoaderConfiguration.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@JobScope
	@Bean
	public Summary summary(@Value("#{jobParameters[reportWriteStatistics] ?: false}") boolean reportWriteStatistics) {
		Summary summary = new Summary();

		if (reportWriteStatistics) {
			summary.setStatistics(new FeatureStatistics());
		}

		return summary;
	}

	@JobScope
	@Bean
	public ReportWriter reportWriter(Summary summary,
			@Value("#{jobParameters['reportFile'] ?: 'GmlLoader.log'}") String fileName) {
		Path outputFile = Paths.get(fileName);
		return new ReportWriter(summary, outputFile);
	}

	@StepScope
	@Bean
	public AbstractItemStreamItemReader<Feature> gmlReader(SQLFeatureStore sqlFeatureStore,
			@Value("#{jobParameters[pathToFile]}") String pathToFile,
			@Value("#{jobParameters[pathToList]}") String pathToList,
			@Value("#{jobParameters[disabledResources]}") String disabledResources) {
		GmlReader gmlReader = new GmlReader(sqlFeatureStore);
		gmlReader.setDisabledResources(parseDisabledResources(disabledResources));
		if (pathToFile != null && pathToList != null) {
			// error
			throw new IllegalArgumentException("Specify file to read or file with list of files only!");
		}
		else if (pathToFile != null) {
			gmlReader.setResource(new PathResource(pathToFile));
			return gmlReader;
		}
		else {
			MultiResourceItemReader<Feature> reader = new MultiResourceItemReader<Feature>();
			reader.setDelegate(gmlReader);
			List<Resource> resources;
			try {
				resources = Files.lines(Paths.get(pathToList)) //
					.filter(Objects::nonNull) //
					.filter(line -> !line.startsWith("#")) //
					.filter(line -> !line.trim().isEmpty()) //
					.map(PathResource::new) //
					.peek(pr -> {
						if (!pr.exists()) {
							final String msg = "The file " + pr.getDescription() + " in the list '" + pathToList
									+ "' does not exist!";
							throw new IllegalArgumentException(msg);
						}
						else if (!pr.isReadable()) {
							final String msg = "The file " + pr.getDescription() + " in the list '" + pathToList
									+ "' is not readable!";
							throw new IllegalArgumentException(msg);
						}
					})
					.collect(Collectors.toList());

				reader.setResources(resources.toArray(new Resource[resources.size()]));
				return reader;
			}
			catch (IllegalArgumentException iex) {
				throw iex;
			}
			catch (IOException ex) {
				throw new IllegalArgumentException("Could not read file list.", ex);
			}
		}
	}

	@StepScope
	@Bean
	public FeatureReferencesParser featureReferencesParser() {
		return new FeatureReferencesParser();
	}

	@StepScope
	@Bean
	public SQLFeatureStore sqlFeatureStore(@Value("#{jobParameters[workspaceName]}") String workspaceName,
			@Value("#{jobParameters[sqlFeatureStoreId]}") String sqlFeatureStoreId) throws Exception {
		DeegreeWorkspace workspace = DeegreeWorkspace.getInstance(workspaceName);
		workspace.initAll();
		LOG.info("deegree workspace directory: [" + workspace.getLocation() + "] initialized");
		Workspace newWorkspace = workspace.getNewWorkspace();
		SQLFeatureStore featureStore = (SQLFeatureStore) newWorkspace.getResource(FeatureStoreProvider.class,
				sqlFeatureStoreId);
		LOG.info("SQLFeatureStore: [" + sqlFeatureStoreId + "] requested.");
		if (featureStore == null)
			throw new IllegalArgumentException("SQLFeatureStore with ID " + sqlFeatureStoreId + " in workspace "
					+ workspaceName + " does not exist or could not be initialised successful.");
		return featureStore;
	}

	@StepScope
	@Bean
	public ItemWriter<Feature> featureStoreWriter(SQLFeatureStore sqlFeatureStore, Summary summary,
			@Value("#{jobParameters['dryRun'] ?: false}") boolean dryRun) {
		if (dryRun)
			return new NullWriter();
		return new FeatureStoreWriter(sqlFeatureStore, summary);
	}

	@StepScope
	@Bean
	public StepExecutionListener referenceCheckListener(SQLFeatureStore sqlFeatureStore, Summary summary,
			@Value("#{jobParameters['dryRun'] ?: false}") boolean dryRun) {
		if (dryRun)
			return new DryRunReferenceCheck(summary);
		return new TransactionHandler(sqlFeatureStore, summary);
	}

	@JobScope
	@Bean
	public Step step(StepExecutionListener referenceCheckListener, AbstractItemStreamItemReader<Feature> gmlReader,
			FeatureReferencesParser featureReferencesParser, ItemWriter<Feature> featureStoreWriter,
			@Value("#{jobParameters['chunkSize']}") Integer chunkSize,
			@Value("#{jobParameters['skipReferenceCheck'] ?: false}") boolean skipReferenceCheck) {
		int chunk = chunkSize != null && chunkSize.intValue() > 10 ? chunkSize.intValue() : 10;
		SimpleStepBuilder<Feature, Feature> builder = stepBuilderFactory.get("gmlLoaderStep")
			.<Feature, Feature>chunk(chunk);
		builder.reader(gmlReader);
		if (skipReferenceCheck) {
			LOG.warn("The feature reference check will be skipped.");
		}
		else {
			builder.processor(featureReferencesParser);
		}
		return builder.writer(featureStoreWriter).listener(referenceCheckListener).build();
	}

	@Bean
	public Job job(Step step, ReportWriter reportWriter) {
		return jobBuilderFactory.get("gmlLoaderJob")
			.incrementer(new RunIdIncrementer())
			.start(step)
			.listener(reportWriter)
			.build();
	}

	private List<String> parseDisabledResources(String disabledResources) {
		List<String> patterns = new ArrayList<>();
		if (disabledResources != null) {
			String[] split = disabledResources.split(",");
			for (String resource : split) {
				String pattern = resource.trim();
				if (!pattern.isEmpty())
					patterns.add(pattern);
			}
		}
		return patterns;
	}

}
