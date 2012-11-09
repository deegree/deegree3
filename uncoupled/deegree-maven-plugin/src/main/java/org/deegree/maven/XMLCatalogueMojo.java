//$HeadURL: svn+ssh://aschmitz@deegree.wald.intevation.de/deegree/deegree3/trunk/uncoupled/deegree-maven-plugin/src/main/java/org/deegree/maven/EclipseProjectLinker.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.maven.utils.ClasspathHelper.addDependenciesToClasspath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

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
 * @goal generate-jaxb-catalog
 * @phase generate-resources
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 31419 $, $Date: 2011-08-02 17:42:17 +0200 (Tue, 02 Aug 2011) $
 */
public class XMLCatalogueMojo extends AbstractMojo {

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

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        File target = new File( project.getBasedir(), "target" );
        target.mkdirs();
        target = new File( target, "deegree.xmlcatalog" );

        PrintStream catalogOut = null;
        try {
            catalogOut = new PrintStream( new FileOutputStream( target ) );
            final PrintStream catalog = catalogOut;

            addDependenciesToClasspath( project, artifactResolver, artifactFactory, metadataSource, localRepository );

            final XMLInputFactory fac = XMLInputFactory.newInstance();
            final Reflections r = new Reflections( "/META-INF/schemas/" );

            class CurrentState {
                String location;
            }

            final CurrentState state = new CurrentState();

            r.collect( "META-INF/schemas", new Predicate<String>() {
                @Override
                public boolean apply( String input ) {
                    state.location = input;
                    return input != null && input.endsWith( ".xsd" );
                }
            }, new Serializer() {
                @Override
                public Reflections read( InputStream in ) {
                    try {
                        XMLStreamReader reader = fac.createXMLStreamReader( in );
                        nextElement( reader );
                        String location = "classpath:META-INF/schemas/" + state.location;
                        String ns = reader.getAttributeValue( null, "targetNamespace" );
                        catalog.println( "PUBLIC \"" + ns + "\" \"" + location + "\"" );
                    } catch ( Throwable e ) {
                        getLog().error( e );
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
        } catch ( Throwable t ) {
            throw new MojoFailureException( "Creating xml catalog failed: " + t.getLocalizedMessage() );
        } finally {
            closeQuietly( catalogOut );
        }
    }

}
