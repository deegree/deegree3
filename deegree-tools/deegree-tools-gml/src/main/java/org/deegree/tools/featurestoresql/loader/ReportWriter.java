package org.deegree.tools.featurestoresql.loader;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ReportWriter extends JobExecutionListenerSupport {

    private static final Logger LOG = getLogger( ReportWriter.class );

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" );

    private final Summary summary;

    private final Path outFile;

    /**
     * @param summary
     *                 with the results of import, never <code>null</code>
     * @param outFile
     *                 to write the output, never <code>null</code>
     */
    public ReportWriter( Summary summary, Path outFile ) {
        this.summary = summary;
        this.outFile = outFile;
    }

    @Override
    public void afterJob( JobExecution jobExecution ) {
        StepExecution stepExecution = getStepExecution( jobExecution );

        ExitStatus exitStatus = stepExecution.getExitStatus();
        try ( PrintWriter writer = new PrintWriter( outFile.toFile() ) ) {
            writer.println( "Start: " + getStartTime( stepExecution ) );
            writer.println( "Time needed: " + getTimeNeeded( stepExecution ) );

            if ( ExitStatus.FAILED.getExitCode().equals( exitStatus.getExitCode() ) ) {
                if ( summary.hasUnresolvableReferences() ) {
                    writer.println( "Failed cause of Unresolvable references: " );
                    for ( String unresolvableReference : summary.getUnresolvableReferences() )
                        writer.println( "     - " + unresolvableReference );
                } else if ( summary.isCommitFailed() ) {
                    writer.println( "Commit failed." );
                } else {
                    writer.println( "Failure: " + exitStatus.getExitDescription() );
                }
                writer.println( "Status: FAILED" );
            } else if ( ExitStatus.COMPLETED.getExitCode().equals( exitStatus.getExitCode() ) ) {
                writer.println( "Number of processed features: " + summary.getNumberOfFeatures() );
                writer.println( "Status: SUCCESS" );
            } else {
                writer.println( "Status: " + exitStatus );
            }
        } catch ( IOException e ) {
            LOG.warn( "Report could not be created: {}", e.getMessage() );
        }
    }

    private StepExecution getStepExecution( JobExecution jobExecution ) {
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        return stepExecutions.iterator().next();
    }

    private String getTimeNeeded( StepExecution stepExecution ) {
        if ( stepExecution != null && stepExecution.getStartTime() != null ) {
            long millis = new Date().getTime() - stepExecution.getStartTime().getTime();
            long hours = MILLISECONDS.toHours( millis );
            long minutes = MILLISECONDS.toMinutes( millis );
            long seconds = MILLISECONDS.toSeconds( millis );
            if ( hours > 0 )
                return String.format( "%02d h, %02d min, %02d sec", hours, minutes - HOURS.toMinutes( hours ),
                                      seconds - MINUTES.toSeconds( minutes ) );
            else
                return String.format( "%02d min, %02d sec", minutes, seconds - MINUTES.toSeconds( minutes ) );
        }
        return "UNKNOWN";

    }

    private String getStartTime( StepExecution stepExecution ) {
        if ( stepExecution != null && stepExecution.getStartTime() != null )
            return DATE_FORMAT.format( stepExecution.getStartTime() );
        return "UNKNOWN";
    }

    private String getEndTime( StepExecution stepExecution ) {
        if ( stepExecution != null && stepExecution.getEndTime() != null )
            return DATE_FORMAT.format( stepExecution.getEndTime() );
        return "UNKNOWN";
    }

}