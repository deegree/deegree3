//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-client/deegree-jsf-console/src/main/java/org/deegree/client/generic/ApplicationBean.java $
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
package org.deegree.console.util;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.utils.CollectionUtils.addAllUncontained;
import static org.deegree.services.controller.OGCFrontController.getServiceConfiguration;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.reflections.Reflections;
import org.reflections.serializers.Serializer;
import org.slf4j.Logger;

import com.google.common.base.Predicate;

/**
 * Encapsulates informations about the status of the deegree web services.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 29926 $, $Date: 2011-03-08 11:47:59 +0100 (Di, 08. Mär 2011) $
 */
@ManagedBean
@RequestScoped
public class ApplicationBean implements Serializable {

    private static final long serialVersionUID = 147824864885285227L;

    static final Logger LOG = getLogger( ApplicationBean.class );

    private String logo = DeegreeAALogoUtils.getAsString();

    private List<String> moduleInfos = new ArrayList<String>();

    private List<String> nameToController = new ArrayList<String>();

    private String baseVersion;

    List<String> loadedModules;

    public ApplicationBean() {
        for ( DeegreeModuleInfo info : DeegreeModuleInfo.getRegisteredModules() ) {
            if ( baseVersion == null ) {
                baseVersion = info.getVersion().getVersionNumber();
            }
            moduleInfos.add( info.toString() );
        }
        if ( getServiceConfiguration() != null && getServiceConfiguration().getServiceControllers() != null ) {
            for ( String key : getServiceConfiguration().getServiceControllers().keySet() ) {
                nameToController.add( key );
            }
        }

        final TreeMap<String, String> versions = new TreeMap<String, String>();
        final TreeMap<String, String> dates = new TreeMap<String, String>();
        final TreeMap<String, String> revs = new TreeMap<String, String>();
        final TreeMap<String, String> authors = new TreeMap<String, String>();

        loadedModules = new LinkedList<String>();

        final Reflections r = new Reflections( "org.deegree" );
        r.collect( "META-INF/maven", new Predicate<String>() {
            @Override
            public boolean apply( String input ) {
                return input.startsWith( "org.deegree" ) && input.endsWith( "pom.properties" );
            }
        }, new Serializer() {
            @Override
            public Reflections read( InputStream in ) {
                try {
                    Properties props = new Properties();
                    props.load( in );
                    String artId = props.getProperty( "artifactId" );
                    String version = props.getProperty( "version" );
                    versions.put( artId, version );
                } catch ( IOException e ) {
                    LOG.trace( "Stack trace: ", e );
                } finally {
                    closeQuietly( in );
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
        r.collect( "META-INF/deegree", new Predicate<String>() {
            @Override
            public boolean apply( String input ) {
                return input.endsWith( "buildinfo.properties" );
            }
        }, new Serializer() {
            @Override
            public Reflections read( InputStream in ) {
                try {
                    Properties props = new Properties();
                    props.load( in );
                    String artId = props.getProperty( "build.artifactId" );
                    String rev = props.getProperty( "build.svnrev" );
                    String by = props.getProperty( "build.by" );
                    String date = props.getProperty( "build.date" );
                    revs.put( artId, rev );
                    dates.put( artId, date );
                    authors.put( artId, by );
                } catch ( IOException e ) {
                    LOG.trace( "Stack trace: ", e );
                } finally {
                    closeQuietly( in );
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

        TreeSet<String> set = new TreeSet<String>();
        addAllUncontained( set, authors.keySet() );
        addAllUncontained( set, versions.keySet() );
        addAllUncontained( set, revs.keySet() );
        addAllUncontained( set, dates.keySet() );

        for ( String id : set ) {
            String by = authors.get( id );
            String ver = versions.get( id );
            String rev = revs.get( id );
            String date = dates.get( id );
            by = by == null ? "unknown" : by;
            ver = ver == null ? "unknown" : ver;
            rev = rev == null ? "unknown" : rev;
            date = date == null ? "unknown" : date;
            loadedModules.add( id + " " + ver + " by " + by + " at " + date );
        }

    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public String getLogo() {
        return logo;
    }

    public List<String> getModuleInfos() {
        return moduleInfos;
    }

    public List<String> getNameToController() {
        return nameToController;
    }

    public List<String> getLoadedModules() {
        return loadedModules;
    }
}