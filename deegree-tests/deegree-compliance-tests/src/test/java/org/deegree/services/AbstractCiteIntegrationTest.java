package org.deegree.services;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract class for CITE test suite runner as JUnit tests.
 *
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 *
 * @version 1.0
 * @since 3.4
 *
 */
public abstract class AbstractCiteIntegrationTest {

    protected static final Logger LOG = getLogger( AbstractCiteIntegrationTest.class );

    protected static Collection getResultSnippets(String testResources, String paramName, String getCapsPath)
            throws IOException, URISyntaxException {

        URL url = AbstractCiteIntegrationTest.class.getResource( testResources );
        String file = new File( url.toURI() ).getAbsolutePath();

        CiteWrapper wrapper = new CiteWrapper( file, paramName, getCapsPath);
        try {
            wrapper.execute();
        } catch ( Exception e ) {
            LOG.error( e.getMessage(), e );
            e.printStackTrace( System.err );
        }
        String out = wrapper.getOutput();
        String err = wrapper.getError();

        System.out.println( out );
        if ( !err.isEmpty() ) {
            System.out.println( "Standard error messages: " + err );
        }

        return parseResultSnippets( out );
    }

    private static Collection parseResultSnippets( String out )
            throws IOException {

        List resultSnippets = new ArrayList();

        BufferedReader reader = new BufferedReader( new StringReader( out ) );
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            lines.add( line );
        }

        int currentLine = 0;
        while ( currentLine < lines.size() ) {
            String trimmed = lines.get( currentLine++ ).trim();
            if ( trimmed.startsWith( "Testing" ) && !trimmed.startsWith( "Testing suite" ) ) {
                String s = trimmed.substring( 8 );
                String caseId = s.substring( 0, s.indexOf( ' ' ) );
                String result = findCorrespondingResult( lines, currentLine, caseId );
                resultSnippets.add( new Object[] { caseId, result } );
            }
        }
        return resultSnippets;
    }

    private static String findCorrespondingResult( List<String> lines, int currentLine, String caseId ) {
        while ( currentLine < lines.size() ) {
            String trimmed = lines.get( currentLine++ ).trim();
            if ( trimmed.startsWith( "Test " + caseId ) ) {
                return trimmed;
            }
        }
        throw new RuntimeException( "Error parsing CITE result log for test id: " + caseId );
    }

    protected String resultSnippet;

}
