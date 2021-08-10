//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/test/trunk/src/main/java/org/deegree/test/cite/CiteWrapper.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

public class CiteWrapper {

    private static final Logger LOG = getLogger( CiteWrapper.class );

    private final String citeScript;

    private final PrintStream oldSysOut;

    private final PrintStream oldSysErr;

    private String out;

    private String err;

    private String baseUrl;

    private String paramName;

    private String getCapsPath;

    public CiteWrapper( String citeScript, String paramName, String getCapsPath ) {
        this.citeScript = citeScript;
        this.oldSysOut = System.out;
        this.oldSysErr = System.err;
        this.paramName = paramName;
        this.getCapsPath = getCapsPath;

        if (System.getProperty("serviceUrl") == null || System.getProperty("serviceUrl").isEmpty() ) {
            this.baseUrl = "http://localhost:8080/deegree-compliance-tests/services";
            LOG.debug("Using default URL: '" + this.baseUrl + "'");
        } else {
            LOG.debug("Retrieving serviceUrl from context '" + System.getProperty("serviceUrl") + "'");
            this.baseUrl = System.getProperty("serviceUrl");
        }
        LOG.info("Running CITE wrapper with " + baseUrl);
    }

    public void execute()
                            throws Exception {
        String[] args = new String[] { "-cmd=-mode=test", "-mode=test", "-source=" + citeScript, "-workdir=/tmp", "@"+paramName+"="+baseUrl+"/"+getCapsPath };
        LOG.debug("using args "+ Arrays.toString(args));
        ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
        ByteArrayOutputStream sysErr = new ByteArrayOutputStream();
        try {
            System.setOut( new PrintStream( sysOut ) );
            System.setErr( new PrintStream( sysErr ) );
            // TODO what about the build path?
            com.occamlab.te.Test.main( args );
        } catch ( Exception e ) {
            throw e;
        } finally {
            out = sysOut.toString( "UTF-8" );
            err = sysErr.toString( "UTF-8" );
            System.setOut( oldSysOut );
            System.setErr( oldSysErr );
        }
    }

    public String getOutput() {
        return out;
    }

    public String getError() {
        return err;
    }
}
