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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.serializers.Serializer;

import com.google.common.base.Predicate;

/**
 * @goal assemble-log4j
 * @phase generate-resources
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Log4jMojo extends AbstractMojo {

    private int width = 120;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private void block( String text, PrintWriter out ) {
        out.print( "# " );
        for ( int i = 0; i < width - 2; ++i ) {
            out.print( "=" );
        }
        out.println();
        int odd = text.length() % 2;
        int len = ( width - text.length() - 4 ) / 2;
        out.print( "# " );
        for ( int i = 0; i < len; ++i ) {
            out.print( "=" );
        }
        out.print( " " + text + " " );
        for ( int i = 0; i < len + odd; ++i ) {
            out.print( "=" );
        }
        out.println();
        out.print( "# " );
        for ( int i = 0; i < width - 2; ++i ) {
            out.print( "=" );
        }
        out.println();
        out.println();
    }

    private void collect( final Reflections r, final String type, String msg, final PrintWriter out ) {
        block( msg, out );
        r.collect( "META-INF/deegree/log4j", new Predicate<String>() {
            @Override
            public boolean apply( String input ) {
                return input != null && input.equals( type );
            }
        }, new Serializer() {
            @Override
            public Reflections read( InputStream in ) {
                try {
                    copy( in, out );
                } catch ( IOException e ) {
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
    }

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        if ( new File( project.getBasedir(), "src/main/resources/log4j.properties" ).exists() ) {
            getLog().info( "Skipping generation of log4j.properties as it already exists in src/main/resources." );
            return;
        }
        final Reflections r = new Reflections( "org.deegree" );
        // to work around stupid initialization compiler error (hey, it's defined to be null if not 'initialized'!)
        PrintWriter o = null;
        final PrintWriter out;
        try {
            File outFile = new File( project.getBasedir(), "target/generated-resources/log4j.properties" );
            if ( !outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs() ) {
                throw new MojoFailureException( "Could not create parent directory: " + outFile.getParentFile() );
            }
            out = new PrintWriter( new OutputStreamWriter( new FileOutputStream( outFile ), "UTF-8" ) );

            out.println( "# by default, only log to stdout" );
            out.println( "log4j.rootLogger=INFO, stdout" );
            out.println( "log4j.appender.stdout=org.apache.log4j.ConsoleAppender" );
            out.println( "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout" );
            out.println( "log4j.appender.stdout.layout.ConversionPattern=[%d{HH:mm:ss}] %5p: [%c{1}] %m%n" );
            out.println();
            out.println( "# The log level for all classes that are not configured below." );
            out.println( "log4j.logger.org.deegree=INFO" );
            out.println();
            out.println( "# automatically generated output follows" );
            out.println();

            o = out;

            collect( r, "error", "Severe error messages", out );
            collect( r, "warn", "Important warning messages", out );
            collect( r, "info", "Informational messages", out );
            collect( r, "debug", "Debugging messages, useful for in-depth debugging of e.g. service setups", out );
            collect( r, "trace", "Tracing messages, for developers only", out );
        } catch ( FileNotFoundException e ) {
            getLog().error( e );
        } catch ( UnsupportedEncodingException e ) {
            getLog().error( e );
        } finally {
            closeQuietly( o );
        }
    }

}
