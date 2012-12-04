//$HeadURL$
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.maven;

import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.get;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.deegree.maven.ithelper.ServiceIntegrationTestHelper;
import org.deegree.maven.utils.HttpUtils;

/**
 * @goal test-workspaces
 * @phase integration-test
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WorkspaceITMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        Set<?> artifacts = project.getDependencyArtifacts();
        Set<Artifact> workspaces = new HashSet<Artifact>();
        for ( Object o : artifacts ) {
            Artifact a = (Artifact) o;
            if ( a.getType() != null && a.getType().equals( "deegree-workspace" ) ) {
                workspaces.add( a );
            }
        }

        ServiceIntegrationTestHelper helper = new ServiceIntegrationTestHelper( project, getLog() );
        for ( Artifact a : workspaces ) {
            getLog().info( "Testing workspace " + a.getArtifactId() );

            String url = helper.createBaseURL() + "config/upload/iut.zip";
            File file = a.getFile();
            try {
                HttpClient client = HttpUtils.getAuthenticatedHttpClient( helper );

                getLog().info( "Sending against: " + helper.createBaseURL() + "config/delete/iut" );
                HttpGet get = new HttpGet( helper.createBaseURL() + "config/delete/iut" );
                HttpResponse resp = client.execute( get );
                String response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
                getLog().info( "Response after initially deleting iut was: " + response );

                getLog().info( "Sending against: " + helper.createBaseURL() + "config/restart" );
                get = new HttpGet( helper.createBaseURL() + "config/restart" );
                resp = client.execute( get );
                response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
                getLog().info( "Response after initial restart was: " + response );

                getLog().info( "Sending against: " + url );
                HttpPost post = new HttpPost( url );
                post.setEntity( new FileEntity( file, null ) );
                resp = client.execute( post );
                response = EntityUtils.toString( resp.getEntity() ).trim();
                getLog().info( "Response after uploading was: " + response );

                getLog().info( "Sending against: " + helper.createBaseURL() + "config/restart/iut" );
                get = new HttpGet( helper.createBaseURL() + "config/restart/iut" );
                resp = client.execute( get );
                response = EntityUtils.toString( resp.getEntity(), "UTF-8" ).trim();
                getLog().info( "Response after starting workspace was: " + response );
            } catch ( IOException e ) {
                throw new MojoFailureException( "Could not test workspace " + a.getArtifactId() + ": "
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
                    getLog().info( "All maps can be requested." );
                }
                String response = get( UTF8STRING, helper.createBaseURL() + "config/delete/iut", null, "deegree",
                                       "deegree" ).trim();
                getLog().info( "Response after finally deleting iut was: " + response );
            } catch ( IOException e ) {
                getLog().debug( e );
                throw new MojoFailureException( "Could not test workspace " + a.getArtifactId() + ": "
                                                + e.getLocalizedMessage(), e );
            }
        }
    }

}
