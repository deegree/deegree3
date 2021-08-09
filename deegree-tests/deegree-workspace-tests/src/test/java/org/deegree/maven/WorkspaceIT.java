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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.util.EntityUtils;
import org.deegree.maven.ithelper.ServiceIntegrationTestHelper;
import org.deegree.maven.ithelper.TestEnvironment;
import org.deegree.maven.utils.HttpUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.get;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WorkspaceIT {

    private static final Logger LOG = getLogger( WorkspaceIT.class );

    private TestEnvironment env = new TestEnvironment( System.getProperties() );

    public void execute() throws Exception {
        List<File> workspaces = new ArrayList<File>();

        ServiceIntegrationTestHelper helper = new ServiceIntegrationTestHelper( env );

        for ( File workspace : workspaces ) {
            LOG.info( "Testing workspace " + workspace.getAbsolutePath() );

            String url = helper.createBaseURL() + "config/upload/iut.zip";
            File file = workspace; // TODO x-x---x-xx
            try {
                HttpClient client = HttpUtils.getAuthenticatedHttpClient( env );

                LOG.info( "Sending against: " + helper.createBaseURL() + "config/delete/iut" );
                HttpGet get = new HttpGet( helper.createBaseURL() + "config/delete/iut" );
                HttpResponse resp = client.execute( get );
                String response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
                LOG.info( "Response after initially deleting iut was: " + response );

                LOG.info( "Sending against: " + helper.createBaseURL() + "config/restart" );
                get = new HttpGet( helper.createBaseURL() + "config/restart" );
                resp = client.execute( get );
                response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
                LOG.info( "Response after initial restart was: " + response );

                LOG.info( "Sending against: " + url );
                HttpPost post = new HttpPost( url );
                post.setEntity( new FileEntity( file, ContentType.APPLICATION_OCTET_STREAM ) );
                resp = client.execute( post );
                response = EntityUtils.toString( resp.getEntity() ).trim();
                LOG.info( "Response after uploading was: " + response );

                LOG.info( "Sending against: " + helper.createBaseURL() + "config/restart/iut" );
                get = new HttpGet( helper.createBaseURL() + "config/restart/iut" );
                resp = client.execute( get );
                response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
                LOG.info( "Response after starting workspace was: " + response );
            } catch ( IOException e ) {
                throw new Exception( "Could not test workspace " + workspace.getName() + ": "
                        + e.getLocalizedMessage(), e );
            }

            try {
                String s = get( UTF8STRING, helper.createBaseURL() + "config/list/iut/services/", null, "deegree",
                        "deegree" );
                String[] services = s.split( "\\s" );

                for ( String srv : services ) {
                    String nm = new File( srv ).getName().toLowerCase();
                    if ( nm.length() != 7 ) {
                        continue;
                    }
                    String service = nm.substring( 0, 3 ).toUpperCase();
                    helper.testCapabilities( service );
                    helper.testLayers( service );
                    LOG.info( "All maps can be requested." );
                }
                String response = get( UTF8STRING, helper.createBaseURL() + "config/delete/iut", null, "deegree",
                        "deegree" ).trim();
                LOG.info( "Response after finally deleting iut was: " + response );
            } catch ( Exception e ) {
                LOG.error( e.getLocalizedMessage(), e );
                throw new Exception( "Could not test workspace " + workspace.getName() + ": "
                        + e.getLocalizedMessage(), e );
            }
        }
    }

}

