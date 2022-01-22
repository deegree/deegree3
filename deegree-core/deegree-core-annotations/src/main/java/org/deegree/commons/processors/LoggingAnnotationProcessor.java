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
package org.deegree.commons.processors;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
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
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.annotations.PackageLoggingNotes;
import org.deegree.commons.utils.io.RollbackPrintWriter;
import org.slf4j.Logger;

/**
 * <code>LoggingAnnotationProcessor</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @deprecated This class is deprecated as of version 3.4 of deegree.
 */

@SupportedAnnotationTypes(value = { "org.deegree.commons.annotations.PackageLoggingNotes",
                                   "org.deegree.commons.annotations.LoggingNotes" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({ "log4j.outputdir" })
@Deprecated
@AutoService(Processor.class)
public class LoggingAnnotationProcessor extends AbstractProcessor {

    private static final Logger LOG = getLogger( LoggingAnnotationProcessor.class );

    private String outDir;

    private int width = 120;

    @Override
    public void init( ProcessingEnvironment env ) {
        super.init( env );
        outDir = env.getOptions().get( "log4j.outputdir" );
        if ( outDir == null ) {
            outDir = System.getProperty( "java.io.tmpdir" ) + "/log4j/";
            LOG.info( "Outputting log4j snippets to '{}'.", outDir );
        }
        File parentFile = new File( outDir );
        if ( !parentFile.exists() && !parentFile.mkdirs() ) {
            LOG.warn( "Target directory could not be created: {}", parentFile );
        }
    }

    // breaks the lines at max width
    private String format( String str ) {
        StringBuilder res = new StringBuilder();
        outer: while ( str.length() > ( width - 3 ) ) {
            int len = 3;
            res.append( "<!-- " );
            while ( len < width && str.length() > 0 ) {
                int idx = str.indexOf( " " );
                if ( idx == -1 ) {
                    if ( len > 3 ) {
                        res.append( "\n<!-- " );
                    }
                    res.append( str );
                    str = "";
                } else {
                    if ( len + idx + 1 > width ) {
                        res.append( " -->\n" );
                        continue outer;
                    }
                    res.append(str.substring(0, idx + 1));
                    str = str.substring(0, idx + 1);
                    len += idx + 1;
                }
            }
            if ( !str.isEmpty() ) {
                res.append( " -->\n" );
            }
        }
        if ( !str.isEmpty() ) {
            res.append( "<!-- " ).append( str ).append( " -->\n" );
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

    private void block( String text, RollbackPrintWriter out, boolean big ) {
        if ( big ) {
            out.print( "<!-- " );
            for ( int i = 0; i < width - 2; ++i ) {
                out.print( "=" );
            }
            out.print(" -->");
            out.println();
        }
        int odd = text.length() % 2;
        int len = ( width - text.length() - 4 ) / 2;
        out.print( "<!-- " );
        for ( int i = 0; i < len; ++i ) {
            out.print( "=" );
        }
        out.print( " " + text + " " );
        for ( int i = 0; i < len + odd; ++i ) {
            out.print( "=" );
        }
        out.print(" -->");
        out.println();
        if ( big ) {
            out.print( "<!-- " );
            for ( int i = 0; i < width - 2; ++i ) {
                out.print( "=" );
            }
            out.print(" -->");
            out.println();
        }
        out.println();
    }

    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
        try {
            Tree tree = new Tree();

            for ( Element e : roundEnv.getRootElements() ) {
                find( e, tree );
            }

            if ( tree.children.isEmpty() ) {
                return true;
            }

            FileOutputStream fos = new FileOutputStream( new File( outDir, "error" ) );
            PrintWriter pw = new PrintWriter( new OutputStreamWriter( fos, "UTF-8" ) );
            RollbackPrintWriter out = new RollbackPrintWriter( pw );
            tree.print( out, "", true, false, false, false, false );
            out.close();
            pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( new File( outDir, "warn" ) ), "UTF-8" ) );
            out = new RollbackPrintWriter( pw );
            tree.print( out, "", false, true, false, false, false );
            out.close();
            pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( new File( outDir, "info" ) ), "UTF-8" ) );
            out = new RollbackPrintWriter( pw );
            tree.print( out, "", false, false, true, false, false );
            out.close();
            pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( new File( outDir, "debug" ) ), "UTF-8" ) );
            out = new RollbackPrintWriter( pw );
            tree.print( out, "", false, false, false, true, false );
            out.close();
            pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( new File( outDir, "trace" ) ), "UTF-8" ) );
            out = new RollbackPrintWriter( pw );
            tree.print( out, "", false, false, false, false, true );
            out.close();
            return true;
        } catch ( UnsupportedEncodingException | FileNotFoundException e ) {
            e.printStackTrace();
        }
        return false;
    }

    class Tree {
        PackageLoggingNotes pnotes;

        LoggingNotes notes;

        TreeMap<String, Tree> children = new TreeMap<>();

        void insert( String qname, PackageLoggingNotes pnotes, LoggingNotes notes ) {
            LinkedList<String> pkgs = new LinkedList<>( Arrays.asList( qname.split( "\\." ) ) );
            Tree node = this;
            while ( true ) {
                String next = pkgs.poll();
                Tree nextNode = node.children.get( next );
                if ( nextNode == null ) {
                    nextNode = new Tree();
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
                handleNotes( out, qname, error, warn, info, debug, trace );
            }
            if ( pnotes != null ) {
                handlePackageNotes( out, qname, error, warn, info, debug, trace );
            }
            for ( Entry<String, Tree> entry : children.entrySet() ) {
                entry.getValue().print( out, qname + ( qname.isEmpty() ? "" : "." ) + entry.getKey(), error, warn,
                        info, debug, trace );
            }
            out.rollback();
        }

        private void handleNotes( RollbackPrintWriter out, String qname, boolean error, boolean warn, boolean info,
                                  boolean debug, boolean trace ) {
            handleNote( out, error, notes.error(), qname, "error" );
            handleNote( out, warn, notes.warn(), qname, "warn" );
            handleNote( out, info, notes.info(), qname, "info" );
            handleNote( out, debug, notes.debug(), qname, "debug" );
            handleNote( out, trace, notes.trace(), qname, "trace" );
        }

        private void handlePackageNotes( RollbackPrintWriter out, String qname, boolean error, boolean warn,
                                         boolean info, boolean debug, boolean trace ) {
            String title = pnotes.title();

            boolean isSubsystem = qname.replaceAll( "[^.]", "" ).length() == 2;

            if ( !title.isEmpty() ) {
                block( title, out, isSubsystem );
            }

            handleNote( out, error, pnotes.error(), qname, "error" );
            handleNote( out, warn, pnotes.warn(), qname, "warn" );
            handleNote( out, info, pnotes.info(), qname, "info" );
            handleNote( out, debug, pnotes.debug(), qname, "debug" );
            handleNote( out, trace, pnotes.trace(), qname, "trace" );
        }

        private void handleNote( RollbackPrintWriter out, boolean level, String note, String qname, String levelName ) {
            if ( !note.isEmpty() && level ) {
                out.println( format( note ) );
                out.println("<!--");
                out.println("    <Logger name=\"" + qname + "\" level=\"" + levelName + "\">");
                out.println("        <AppenderRef ref=\"Console\"/>");
                out.println("    </Logger>");
                out.println("-->");
                out.println();
                out.flush();
            }
        }

    }

}
