/**
 * 
 */
package org.deegree.services.csw;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_PREFIX;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.services.CiteWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wraps the execution of the CSW BKG 1 TestSuite as a JUnit-test.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: lbuesching $
 * 
 * @version $Revision: 32799 $, $Date: 2012-02-06 10:14:01 +0100 (Mon, 06 Feb 2012) $
 */
@RunWith(Parameterized.class)
public class CSWBKG1IT {

    private String testLabel = "CSW_BKG1";

    private String resultSnippet;

    private final boolean isCSWAvailable;
    
    public CSWBKG1IT( String testLabel, String resultSnippet, boolean isCSWAvailable) {
        this.testLabel = testLabel;
        this.resultSnippet = resultSnippet;
        this.isCSWAvailable = isCSWAvailable;
    }

    @Parameters
    public static Collection<Object[]> getResultSnippets()
                            throws Exception {
        boolean cswAvailable = isCSWAvailable();
        if ( !cswAvailable ) {
            List<Object[]> resultSnippets = new ArrayList<Object[]>();
            resultSnippets.add( new Object[] { null, null, false } );
            return resultSnippets;
        }

        URL url = CSWBKG1IT.class.getResource( "/bkg1/ctl/" );
        String file = new File( url.toURI() ).getAbsolutePath();
        System.out.println( "file: " + file );

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

    private static boolean isCSWAvailable() {
        String url = System.getProperty( "serviceUrl" );
        System.out.println("Check if CSW to test with URL '" + url + "' is available!");
        try {
            InputStream postBody = CSWBKG1IT.class.getResourceAsStream( "testGetRecordsRequest.xml" );
            XMLAdapter resp = HttpUtils.post( HttpUtils.XML, url, postBody, null );
            NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();
            nsContext.addNamespace( CSW_202_PREFIX, CSWConstants.CSW_202_NS );
            OMElement element = resp.getElement( resp.getRootElement(), new XPath( "/" + CSW_202_PREFIX
                                                                                   + ":GetRecordsResponse/"
                                                                                   + CSW_202_PREFIX + ":SearchResults",
                                                                                   nsContext ) );
            if ( element != null ) {
                return true;
            }
        } catch ( Exception e ) {
            System.out.println( "Could not check CSW, assume CSW is not available: " + e.getMessage() );
        }
        return false;
    }

    private static Collection<Object[]> getResultSnippets( String out )
                            throws IOException {
        List<Object[]> resultSnippets = new ArrayList<Object[]>();

        BufferedReader reader = new BufferedReader( new StringReader( out ) );
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            lines.add( line );
        }

        int currentLine = 0;
        while ( currentLine < lines.size() ) {
            String trimmed = lines.get( currentLine++ ).trim();
            if ( trimmed.startsWith( "Testing ctl" ) || trimmed.startsWith( "Testing csw" ) ) {
                String s = trimmed.substring( 8 );
                String caseId = s.substring( 0, s.indexOf( ' ' ) );
                String result = findCorrespondingResult( lines, currentLine, caseId );
                resultSnippets.add( new Object[] { caseId, result, true } );
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
        assumeTrue( isCSWAvailable );
        if ( resultSnippet.contains( "Failed" ) ) {
            throw new RuntimeException( "Test '" + testLabel + "' failed." );
        }
    }
}
