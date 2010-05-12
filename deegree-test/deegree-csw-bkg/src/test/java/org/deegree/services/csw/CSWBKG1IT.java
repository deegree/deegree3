/**
 * 
 */
package org.deegree.services.csw;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wraps the execution of the CSW BKG 1 TestSuite as a JUnit-test.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@RunWith(Parameterized.class)
public class CSWBKG1IT {

    private String testLabel = "CSW_BKG1";

    private String resultSnippet;

    public CSWBKG1IT( String testLabel, String resultSnippet ) {
        this.testLabel = testLabel;
        this.resultSnippet = resultSnippet;
    }

    @Parameters
    public static Collection getResultSnippets()
                            throws Exception {

        URL url = CSWBKG1IT.class.getResource( "/bkg1/ctl" );
        String file = new File( url.toURI() ).getAbsolutePath();
        System.out.println("file: "+ file);

       
        CiteWrapper wrapper = new CiteWrapper( file );
        try {
            wrapper.execute();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        String out = wrapper.getOutput();
        String err = wrapper.getError();

        System.out.println( out );
        if ( !err.isEmpty() ) {
            System.out.println( "Standard error messages: " + err );
        }

        return getResultSnippets( out );
    }

    private static Collection getResultSnippets( String out )
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
        throw new RuntimeException( "Error parsing CITE result log." );
    }

    @Test
    public void singleTest() {
        if ( resultSnippet.contains( "Failed" ) ) {
            throw new RuntimeException( "Test '" + testLabel + "' failed." );
        }
    }
}
