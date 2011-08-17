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

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.deegree.maven.utils.ClasspathHelper.addDependenciesToClasspath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.serializers.Serializer;

import com.google.common.base.Predicate;

/**
 * @goal assemble-console
 * @phase generate-resources
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ConsoleMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * 
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * 
     * @component
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * 
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    String input;

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {

        addDependenciesToClasspath( project, artifactResolver, artifactFactory, metadataSource, localRepository );

        final File dir = new File( project.getBasedir(), "src/main/webapp/console" );
        if ( !dir.isDirectory() && !dir.mkdirs() ) {
            throw new MojoExecutionException( "Could not create src/main/webapp/console directory." );
        }

        final Reflections r = new Reflections( "/META-INF/deegree" );

        // let's just hope it's always apply -> read -> apply -> read
        r.collect( "META-INF/deegree/console", new Predicate<String>() {
            @Override
            public boolean apply( String input ) {
                ConsoleMojo.this.input = input;
                return input != null && !input.trim().isEmpty();
            }
        }, new Serializer() {
            @Override
            public Reflections read( InputStream in ) {
                try {
                    File newDir = new File( dir, input );
                    if ( in.available() == 0 ) {
                        if ( !newDir.isDirectory() && !newDir.mkdirs() ) {
                            getLog().warn( "Could not create console directory " + input );
                        }
                    } else {
                        copyInputStreamToFile( in, newDir );
                    }
                } catch ( IOException e ) {
                    getLog().warn( "Could not copy console file " + input, e );
                }
                return r;
            }

            @Override
            public File save( Reflections reflections, String filename ) {
                return null;
            }

            @Override
            public String toString( Reflections reflections ) {
                return null;
            }
        } );
    }

}
