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
package org.deegree.maven.utils;

import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static org.apache.maven.project.artifact.MavenMetadataSource.createArtifacts;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ClasspathHelper {

    /**
     * @param project
     * @param artifactResolver
     * @param artifactFactory
     * @param metadataSource
     * @param localRepository
     * @param type
     * @return a list of all (possibly transitive) artifacts of the given type
     * @throws InvalidDependencyVersionException
     * @throws ArtifactResolutionException
     * @throws ArtifactNotFoundException
     */
    public static Set<?> getDependencyArtifacts( MavenProject project, ArtifactResolver artifactResolver,
                                                 ArtifactFactory artifactFactory,
                                                 ArtifactMetadataSource metadataSource,
                                                 ArtifactRepository localRepository, final String type,
                                                 boolean transitively )
                            throws InvalidDependencyVersionException, ArtifactResolutionException,
                            ArtifactNotFoundException {

        List<?> dependencies = project.getDependencies();

        Set<Artifact> dependencyArtifacts = createArtifacts( artifactFactory, dependencies, null, new ArtifactFilter() {
            @Override
            public boolean include( Artifact artifact ) {
                return artifact != null && artifact.getType() != null && artifact.getType().equals( type );
            }
        }, null );

        ArtifactResolutionResult result;
        Artifact mainArtifact = project.getArtifact();

        result = artifactResolver.resolveTransitively( dependencyArtifacts, mainArtifact, EMPTY_MAP, localRepository,
                                                       project.getRemoteArtifactRepositories(), metadataSource, null,
                                                       EMPTY_LIST );

        if ( transitively ) {
            return result.getArtifacts();
        }

        LinkedHashSet<Artifact> set = new LinkedHashSet<Artifact>();
        if ( mainArtifact.getType() != null && mainArtifact.getType().equals( type ) ) {
            set.add( mainArtifact );
        }
        set.addAll( dependencyArtifacts );

        return set;
    }

    private static Set<?> resolveDeps( MavenProject project, ArtifactResolver artifactResolver,
                                       ArtifactFactory artifactFactory, ArtifactMetadataSource metadataSource,
                                       ArtifactRepository localRepository )
                            throws InvalidDependencyVersionException, ArtifactResolutionException,
                            ArtifactNotFoundException {

        List<?> dependencies = project.getDependencies();

        Set<Artifact> dependencyArtifacts = createArtifacts( artifactFactory, dependencies, null, null, null );

        dependencyArtifacts.add( project.getArtifact() );

        ArtifactResolutionResult result = artifactResolver.resolveTransitively( dependencyArtifacts,
                                                                                project.getArtifact(),
                                                                                EMPTY_MAP,
                                                                                localRepository,
                                                                                project.getRemoteArtifactRepositories(),
                                                                                metadataSource, null, EMPTY_LIST );

        return result.getArtifacts();
    }

    public static void addDependenciesToClasspath( MavenProject project, ArtifactResolver artifactResolver,
                                                   ArtifactFactory artifactFactory,
                                                   ArtifactMetadataSource metadataSource,
                                                   ArtifactRepository localRepository )
                            throws MojoExecutionException {
        try {
            Set<?> artifacts = resolveDeps( project, artifactResolver, artifactFactory, metadataSource, localRepository );
            final URL[] urls = new URL[artifacts.size()];
            Iterator<?> itor = artifacts.iterator();
            int i = 0;
            while ( itor.hasNext() ) {
                urls[i++] = ( (Artifact) itor.next() ).getFile().toURI().toURL();
            }

            doPrivileged( new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    URLClassLoader cl = new URLClassLoader( urls, currentThread().getContextClassLoader() );
                    currentThread().setContextClassLoader( cl );
                    return null;
                }
            } );

        } catch ( Throwable e ) {
            throw new MojoExecutionException( e.getLocalizedMessage(), e );
        }
    }

}
