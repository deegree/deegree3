//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.utils.log;

import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.deegree.commons.utils.io.RollbackPrintWriter;
import org.slf4j.Logger;

/**
 * <code>LoggingAnnotationProcessor</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */

@SupportedAnnotationTypes(value = { "org.deegree.commons.utils.log.PackageLoggingNotes",
                                   "org.deegree.commons.utils.log.LoggingNotes" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( { "log4j.outputfile", "width" })
public class LoggingAnnotationProcessor extends AbstractProcessor {

    private static final Logger LOG = getLogger( LoggingAnnotationProcessor.class );

    private String outFile;

    private int width;

    @Override
    public void init( ProcessingEnvironment env ) {
        super.init( env );
        outFile = env.getOptions().get( "log4j.outputfile" );
        String w = env.getOptions().get( "width" );
        width = w == null ? 120 : parseInt( w );
        if ( outFile == null ) {
            outFile = System.getProperty( "java.io.tmpdir" ) + "/log4j.snippet";
            LOG.info( "Outputting log4j snippet to '{}'.", outFile );
        }
    }

    // breaks the lines at max width
    String format( String str ) {
        StringBuilder res = new StringBuilder();
        outer: while ( str.length() > ( width - 3 ) ) {
            int len = 3;
            res.append( "## " );
            while ( len < width && str.length() > 0 ) {
                int idx = str.indexOf( " " );
                if ( idx == -1 ) {
                    if ( len > 3 ) {
                        res.append( "\n## " );
                    }
                    res.append( str );
                    str = "";
                } else {
                    if ( len + idx + 1 > width ) {
                        res.append( "\n" );
                        continue outer;
                    }
                    res.append( str.substring( 0, idx + 1 ) );
                    str = str.substring( idx + 1 );
                    len += idx + 1;
                }
            }
            if ( !str.isEmpty() ) {
                res.append( "\n" );
            }
        }
        if ( !str.isEmpty() ) {
            res.append( "## " + str );
        }
        return res.toString();
    }

    private void find( Element e, Tree root ) {
        LoggingNotes notes = e.getAnnotation( LoggingNotes.class );
        PackageLoggingNotes pnotes = e.getAnnotation( PackageLoggingNotes.class );
        // the #toString apparently yields the qname, is there another way?
        String qname = e.toString();

        if ( notes != null || pnotes != null ) {
            root.insert( qname, pnotes, notes );
        }

        for ( Element e2 : e.getEnclosedElements() ) {
            find( e2, root );
        }
    }

    void block( String text, RollbackPrintWriter out, boolean big ) {
        if ( big ) {
            out.print( "# " );
            for ( int i = 0; i < width - 2; ++i ) {
                out.print( "=" );
            }
            out.println();
        }
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
        if ( big ) {
            out.print( "# " );
            for ( int i = 0; i < width - 2; ++i ) {
                out.print( "=" );
            }
            out.println();
        }
        out.println();
    }

    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
        try {
            PrintWriter pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( outFile, true ), "UTF-8" ) );
            RollbackPrintWriter out = new RollbackPrintWriter( pw );

            Tree tree = new Tree();

            for ( Element e : roundEnv.getRootElements() ) {
                find( e, tree );
            }

            if ( tree.children.isEmpty() ) {
                return true;
            }

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

            // first run for errors, warnings, info
            block( "Errors, warnings and informational messages", out, true );
            out.flush();
            tree.print( out, "", true, true, true, false, false );
            // now get the debugs
            block( "Debugging messages, useful for in-depth debugging of e.g. service setups", out, true );
            out.flush();
            tree.print( out, "", false, false, false, true, false );
            // now for the hardcore devs
            block( "Tracing messages, for developers only", out, true );
            out.flush();
            tree.print( out, "", false, false, false, false, true );

            out.close();
            return true;
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
        return false;
    }

    class Tree {
        String segment;

        PackageLoggingNotes pnotes;

        LoggingNotes notes;

        TreeMap<String, Tree> children = new TreeMap<String, Tree>();

        void insert( String qname, PackageLoggingNotes pnotes, LoggingNotes notes ) {
            LinkedList<String> pkgs = new LinkedList<String>( Arrays.asList( qname.split( "\\." ) ) );
            Tree node = this;
            while ( true ) {
                String next = pkgs.poll();
                Tree nextNode = node.children.get( next );
                if ( nextNode == null ) {
                    nextNode = new Tree();
                    nextNode.segment = next;
                    node.children.put( next, nextNode );
                }
                node = nextNode;
                if ( pkgs.isEmpty() ) {
                    node.notes = notes;
                    node.pnotes = pnotes;
                    break;
                }
            }
        }

        void print( RollbackPrintWriter out, String qname, boolean error, boolean warn, boolean info, boolean debug,
                    boolean trace ) {
            if ( notes != null ) {
                if ( !notes.error().isEmpty() && error ) {
                    out.println( format( notes.error() ) );
                    out.println( "#log4j.logger." + qname + " = ERROR" );
                    out.println();
                    out.flush();
                }
                if ( !notes.warn().isEmpty() && warn ) {
                    out.println( format( notes.warn() ) );
                    out.println( "#log4j.logger." + qname + " = WARN" );
                    out.println();
                    out.flush();
                }
                if ( !notes.info().isEmpty() && info ) {
                    out.println( format( notes.info() ) );
                    out.println( "#log4j.logger." + qname + " = INFO" );
                    out.println();
                    out.flush();
                }
                if ( !notes.debug().isEmpty() && debug ) {
                    out.println( format( notes.debug() ) );
                    out.println( "#log4j.logger." + qname + " = DEBUG" );
                    out.println();
                    out.flush();
                }
                if ( !notes.trace().isEmpty() && trace ) {
                    out.println( format( notes.trace() ) );
                    out.println( "#log4j.logger." + qname + " = TRACE" );
                    out.println();
                    out.flush();
                }
            }
            if ( pnotes != null ) {
                String title = pnotes.title();

                boolean isSubsystem = qname.replaceAll( "[^\\.]", "" ).length() == 2;

                if ( !title.isEmpty() ) {
                    block( title, out, isSubsystem );
                }

                if ( !pnotes.error().isEmpty() && error ) {
                    out.println( format( pnotes.error() ) );
                    out.println( "#log4j.logger." + qname + " = ERROR" );
                    out.println();
                    out.flush();
                }
                if ( !pnotes.warn().isEmpty() && warn ) {
                    out.println( format( pnotes.warn() ) );
                    out.println( "#log4j.logger." + qname + " = WARN" );
                    out.println();
                    out.flush();
                }
                if ( !pnotes.info().isEmpty() && info ) {
                    out.println( format( pnotes.info() ) );
                    out.println( "#log4j.logger." + qname + " = INFO" );
                    out.println();
                    out.flush();
                }
                if ( !pnotes.debug().isEmpty() && debug ) {
                    out.println( format( pnotes.debug() ) );
                    out.println( "#log4j.logger." + qname + " = DEBUG" );
                    out.println();
                    out.flush();
                }
                if ( !pnotes.trace().isEmpty() && trace ) {
                    out.println( format( pnotes.trace() ) );
                    out.println( "#log4j.logger." + qname + " = TRACE" );
                    out.println();
                    out.flush();
                }
            }
            for ( Entry<String, Tree> entry : children.entrySet() ) {
                entry.getValue().print( out, qname + ( qname.isEmpty() ? "" : "." ) + entry.getKey(), error, warn,
                                        info, debug, trace );
            }
            out.rollback();
        }
    }

}
