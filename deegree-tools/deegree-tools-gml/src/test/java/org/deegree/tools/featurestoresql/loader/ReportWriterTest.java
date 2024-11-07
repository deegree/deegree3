package org.deegree.tools.featurestoresql.loader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ReportWriterTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void test() throws IOException {
		Summary summary = new Summary();
		ReportWriter reportWriter = new ReportWriter(summary, tmpFolder.newFile().toPath());

		JobExecution jobExecution = mock(JobExecution.class);
		StepExecution stepExecution = mock(StepExecution.class);
		when(jobExecution.getStepExecutions()).thenReturn(Collections.singletonList(stepExecution));
		when(stepExecution.getStartTime()).thenReturn(LocalDateTime.now().minusDays(1));
		when(stepExecution.getEndTime()).thenReturn(LocalDateTime.now().minusDays(1));
		when(stepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

		reportWriter.afterJob(jobExecution);
	}

}
