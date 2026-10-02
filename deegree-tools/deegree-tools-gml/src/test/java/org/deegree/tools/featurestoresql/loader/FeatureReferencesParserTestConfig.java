package org.deegree.tools.featurestoresql.loader;

import org.deegree.feature.Feature;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Configuration
@EnableBatchProcessing
public class FeatureReferencesParserTestConfig {

	@Autowired
	private JobRepository jobRepository;

	@StepScope
	@Bean
	public GmlReader gmlReader(@Value("#{jobParameters[pathToFile]}") String pathToFile) {
		GmlReader gmlReader = new GmlReader(null);
		gmlReader.setResource(new FileSystemResource(pathToFile));
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
			public void write(Chunk<? extends Feature> items) throws java.lang.Exception {
				System.out.println(items.toString());
			}
		};
	}

	@Bean
	public Step step(GmlReader gmlReader, FeatureReferencesParser featureReferencesParser, ItemWriter itemWriter,
			JdbcTransactionManager transactionManager) {
		return new StepBuilder("FeatureReferencesParserTestStep", jobRepository).<Feature, Feature>chunk(10)
			.transactionManager(transactionManager)
			.reader(gmlReader)
			.processor(featureReferencesParser)
			.writer(itemWriter)
			.build();
	}

	@Bean
	public Job job(Step step) throws Exception {
		return new JobBuilder("FeatureReferencesParserTestJob", jobRepository).incrementer(new RunIdIncrementer())
			.start(step)
			.build();
	}

	@Bean
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.HSQL)
			.addScript("/org/springframework/batch/core/schema-hsqldb.sql")
			.build();
	}

	@Bean
	public JdbcTransactionManager transactionManager(DataSource dataSource) {
		return new JdbcTransactionManager(dataSource);
	}

}
