package org.deegree.services.wmts;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public abstract class AbstractWmtsSimilarityIT {

    protected final String request;

    protected final String resourceName;

    public AbstractWmtsSimilarityIT( String resourceName, String baseDir )
                    throws IOException {
        this.resourceName = resourceName;
        this.request = IOUtils.toString(
                        WmtsGetFeatureInfoSimilarityIT.class.getResourceAsStream(
                                        baseDir + "/" + resourceName + ".kvp" ) );
    }

    protected String createRequest() {
        StringBuilder sb = new StringBuilder();
        sb.append( "http://localhost:" );
        sb.append( System.getProperty( "portnumber", "8080" ) );
        sb.append( "/deegree-wmts-tests/services" );
        sb.append( request );
        return sb.toString();
    }

}
