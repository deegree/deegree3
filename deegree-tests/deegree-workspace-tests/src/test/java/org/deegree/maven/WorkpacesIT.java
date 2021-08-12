/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.maven;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.deegree.maven.ithelper.ServiceIntegrationTestHelper;
import org.deegree.maven.ithelper.TestEnvironment;
import org.deegree.maven.utils.HttpUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
@RunWith(Parameterized.class)
public class WorkpacesIT {

    private static final Logger LOG = getLogger( WorkpacesIT.class );

    private final Path workspaceUnderTest;

    private static final TestEnvironment env = new TestEnvironment( System.getProperties() );

    public WorkpacesIT( Path workspaceUnderTest ) {
        this.workspaceUnderTest = workspaceUnderTest;
    }

    @Parameters
    public static List<Path> getParameters()
                    throws IOException {
        String workspaceDir = env.getWorkspaceDir();
        Path workspaces = Paths.get( workspaceDir );
        return Files.list( workspaces ).filter( p -> Files.isDirectory( p ) && Files.exists( p ) ).collect(
                        Collectors.toList() );
    }

    @Before
    public void restartWorkspace()
                    throws Exception {
        ServiceIntegrationTestHelper helper = new ServiceIntegrationTestHelper( env );
        String workspaceName = workspaceUnderTest.getFileName().toString();
        LOG.info( "Restart Workspace {}", workspaceName );
        try {
            HttpClient client = HttpUtils.getAuthenticatedHttpClient( env );
            String restartUrl = helper.createBaseURL() + "config/restart/" + workspaceName;
            LOG.info( "Sending against: " + restartUrl );
            HttpGet get = new HttpGet( restartUrl );
            HttpResponse resp = client.execute( get );
            String response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
            LOG.info( "Response after initial restart was: " + response );
        } catch ( IOException e ) {
            throw new Exception( "Could not test workspace " + workspaceName + ": "
                                 + e.getLocalizedMessage(), e );
        }
    }

    @Test
    public void execute()
                    throws Exception {
        try {
            String workspaceName = workspaceUnderTest.getFileName().toString();
            LOG.info( "Workspace under test {}", workspaceName );
            ServiceIntegrationTestHelper helper = new ServiceIntegrationTestHelper( env );
            Path services = workspaceUnderTest.resolve( "services" );
            if ( Files.exists( services ) ) {
                List<Path> serviceList = Files.list( services ).filter(
                                f -> isService( f ) ).collect( Collectors.toList() );
                for ( Path service : serviceList ) {
                    testService( helper, service );
                }
            }
        } catch ( NoClassDefFoundError e ) {
            LOG.warn( "Class not found, not performing any tests." );
        }
    }

    private void testService( ServiceIntegrationTestHelper helper, Path service )
                    throws Exception {
        String serviceName = service.getFileName().toString().toLowerCase();
        String serviceType = serviceName.substring( 0, 3 ).toUpperCase();
        LOG.info( "Service name: {}, service type: {}", serviceName, serviceType );
        helper.testCapabilities( serviceType );
        helper.testLayers( serviceType );
        LOG.info( "All maps can be requested." );
    }

    private boolean isService( Path f ) {
        String fileName = f.getFileName().toString();
        return fileName.endsWith( ".xml" ) && !"wmts.xml".equals( fileName ) && !"main.xml".equals( fileName )
               && !fileName.endsWith( "metadata.xml" );
    }

}

