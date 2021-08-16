//$HeadURL$
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
package org.deegree.workspace.standard;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to deegree module metadata (e.g. Maven artifact identifier and build information).
 * The information is extracted from <code>META-INF/MANIFEST.MF</code> on the classpath:
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * 
 * @since 3.4
 */
public final class ModuleInfo implements Comparable<ModuleInfo> {

    private static final Logger LOG = LoggerFactory.getLogger( ModuleInfo.class );

    private final String artifactId;

    private final String version;

    private final String scmRevision;

    private final String buildDate;

    private final String buildBy;

    private ModuleInfo( String artifactId, String version, String scmRevision, String buildDate, String buildBy ) {
        this.artifactId = artifactId;
        this.version = version;
        this.scmRevision = scmRevision;
        this.buildDate = buildDate;
        this.buildBy = buildBy;
    }

    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the module's version.
     * 
     * @return the version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the {@link ModuleInfo}s for the deegree modules on the given classpathes.
     * 
     * @param classpathURLs
     *            classpath urls, must not be <code>null</code>
     * @return module infos, never <code>null</code>, but can be empty (if no deegree module information is present on
     *         the given classpathes)
     */
    public static Collection<ModuleInfo> extractModulesInfo( Collection<URL> classpathURLs ) {
        SortedSet<ModuleInfo> modules = new TreeSet<ModuleInfo>();
        for ( URL classpathURL : classpathURLs ) {
            if ( classpathURL.getFile().toLowerCase().endsWith( ".zip" ) ) {
                continue;
            }
            try {
                ModuleInfo moduleInfo = extractModuleInfo( classpathURL );
                if ( moduleInfo != null ) {
                    modules.add( moduleInfo );
                }
            } catch ( Throwable e ) {
                LOG.warn( "Could not extract module info from {}.", classpathURL );
            }
        }
        return modules;
    }

    /**
     * Returns the {@link ModuleInfo} for the deegree module on the given classpath.
     * 
     * @param classpathURL
     *            classpath url, must not be <code>null</code>
     * @return module info or <code>null</code> (if the module does not have file META-INF/MANIFEST.MF)
     * @throws IOException
     *             if accessing <code>META-INF/MANIFEST.MF</code> fails
     */
    public static ModuleInfo extractModuleInfo( URL classpathURL )
                            throws IOException {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder = builder.setUrls( classpathURL );
        builder = builder.setScanners( new ResourcesScanner() );
        Reflections r = new Reflections( builder );

        Set<String> resources = r.getResources( Pattern.compile( "(MANIFEST\\.MF)" ) );
        if ( !resources.isEmpty() ) {
            URLClassLoader classLoader = new URLClassLoader( new URL[] { classpathURL }, null );
            String resourcePath = resources.iterator().next();
            InputStream buildInfoStream = null;
            try {
                Properties props = new Properties();
                buildInfoStream = classLoader.getResourceAsStream( resourcePath );
                props.load( buildInfoStream );
                String buildArtifactId = props.getProperty( "deegree-build-artifactId",
                                                            props.getProperty( "build.artifactId" ) );
                if ( buildArtifactId == null ) {
                    // skipping because this jar is not from deegree
                    return null;
                }
                String buildBy = props.getProperty( "deegree-build-by", props.getProperty( "build.by" ) );
                String buildDate = props.getProperty( "deegree-build-date", props.getProperty( "build.date" ) );
                String buildRev = props.getProperty( "deegree-build-rev", props.getProperty( "build.svnrev" ) );
                String version = retrieveVersion( props, r, classLoader, buildArtifactId, classpathURL );
                return new ModuleInfo( buildArtifactId, version, buildRev, buildDate, buildBy );
            } finally {
                closeQuietly( buildInfoStream );
            }
        }
        return null;
    }

    @Override
    public int compareTo( ModuleInfo that ) {
        return toString().compareTo( that.toString() );
    }

    @Override
    public boolean equals( Object o ) {
        if ( o instanceof ModuleInfo ) {
            return this.toString().equals( o.toString() );
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( artifactId );
        if ( version != null ) {
            sb.append( "-" );
            sb.append( version );
        }
        sb.append( " (git commit " );
        sb.append( scmRevision );
        sb.append( " build@" );
        sb.append( buildDate );
        sb.append( " by " );
        sb.append( buildBy );
        sb.append( ")" );
        return sb.toString();
    }

    private static String retrieveVersion( Properties props, Reflections r, URLClassLoader classLoader,
                                           String buildArtifactId, URL classpathURL )
                    throws IOException {
        String version = props.getProperty( "Implementation-Version" );
        if ( version != null ) {
            return version;
        }
        Set<String> resources = r.getResources( Pattern.compile( "pom\\.properties" ) );
        InputStream pomInputStream = null;
        if ( !resources.isEmpty() ) {
            String resourcePath = resources.iterator().next();
            try {
                props = new Properties();
                pomInputStream = classLoader.findResource( resourcePath ).openStream();
                props.load( pomInputStream );
                String pomArtifactId = props.getProperty( "artifactId" );
                if ( !pomArtifactId.equals( buildArtifactId ) ) {
                    LOG.warn( "ArtifactId mismatch for module on path: {} (MANIFEST.MF/buildinfo.properties vs. pom.properties).",
                              classpathURL );
                }
                return props.getProperty( "version" );
            } finally {
                closeQuietly( pomInputStream );
            }
        }
        return null;
    }

}
