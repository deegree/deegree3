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

package org.deegree.tools.wms;

import static java.lang.Integer.parseInt;
import static javax.imageio.ImageIO.read;
import static javax.imageio.ImageIO.write;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.model.coverage.grid.WorldFile.readWorldFile;
import static org.deegree.model.coverage.grid.WorldFile.TYPE.CENTER;
import static org.deegree.model.spatialschema.GeometryFactory.createEnvelope;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.StringPair;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.spatialschema.Envelope;

/**
 * <code>Paver</code> should really be run with Java6, since Java5 has severe problems with opening many (temporary)
 * files when using ImageIO, which are LEFT OPEN. Result is that you run out of file handles real fast, and all stops.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Paver implements Runnable {

    static final ILogger LOG = getLogger( Paver.class );

    private int number = 0;

    final Config config;

    final LinkedList<StringPair> requests = new LinkedList<StringPair>();

    boolean finished = false;

    private Paver( Config config ) {
        this.config = config;
        prepareRequests();
    }

    private void prepareRequests() {
        new Thread( new Runnable() {
            public void run() {
                if ( config.logfile != null ) {
                    LOG.logInfo( "Scanning logfile for errors..." );
                    int count = 0;
                    try {
                        BufferedReader in = new BufferedReader( new FileReader( config.logfile ) );
                        String s;
                        while ( ( s = in.readLine() ) != null ) {
                            if ( s.indexOf( "ERROR" ) != -1 ) {
                                String[] ss = s.split( "'" );
                                requests.addFirst( new StringPair( ss[1], ss[3] ) );
                                ++count;
                            }
                        }
                        in.close();
                    } catch ( FileNotFoundException e ) {
                        LOG.logError( "Logfile was not found." );
                        System.exit( 1 );
                    } catch ( IOException e ) {
                        LOG.logError( "Logfile could not be read: " + e.getLocalizedMessage() );
                        System.exit( 1 );
                    }

                    LOG.logInfo( "Found " + count + " errors." );
                    finished = true;
                    return;
                }

                int count = 0;
                for ( String name : config.dirsToLayers.keySet() ) {
                    String layers = config.dirsToLayers.get( name );
                    LOG.logInfo( "Preparing requests for directory " + name + " (layers '" + layers + "')" );

                    File dir = new File( name );
                    File[] dirs = dir.listFiles();

                    for ( File f : dirs ) {
                        if ( f.isDirectory() ) {
                            for ( File file : f.listFiles() ) {
                                if ( file.getName().endsWith( ".wld" ) ) {
                                    ++count;
                                    if ( count % 1000 == 0 ) {
                                        LOG.logInfo( "Read " + count + " world files." );
                                    }
                                    String base = file.toString().substring( 0, file.toString().length() - 4 );
                                    try {
                                        WorldFile wf = readWorldFile( base + ".png", CENTER );
                                        Envelope e = wf.getEnvelope();
                                        if ( config.bbox != null && !e.intersects( config.bbox ) ) {
                                            continue;
                                        }
                                        int width = (int) ( ( e.getWidth() + wf.getResx() ) / wf.getResx() );
                                        int height = (int) ( ( e.getHeight() + wf.getResy() ) / wf.getResy() );
                                        String req = config.request + "BBOX=" + e.getMin().getX() + ","
                                                     + e.getMin().getY() + ",";
                                        req += e.getMax().getX() + "," + e.getMax().getY() + "&WIDTH=" + width
                                               + "&HEIGHT=" + height + "&LAYERS=" + layers;
                                        requests.addFirst( new StringPair( base, req ) );
                                    } catch ( IOException e1 ) {
                                        LOG.logError( "Cannot read a world file: " + e1.getLocalizedMessage() + "("
                                                      + base + ")" );
                                    }
                                }
                            }
                        }
                    }
                }

                finished = true;

                LOG.logInfo( "Total number of requests is " + count );
            }
        } ).start();
    }

    private void sendRequests() {
        LOG.logInfo( "Sending unknown number of requests." );
        for ( int i = 0; i < config.numThreads; ++i ) {
            new Thread( this ).start();
        }

        while ( true ) {
            if ( finished && requests.isEmpty() ) {
                System.exit( 0 );
            }

            try {
                Thread.sleep( 1000 );
            } catch ( InterruptedException e ) {
                // shutting down.
            }
        }
    }

    public void run() {
        outer: while ( true ) {
            StringPair pair = null;
            synchronized ( requests ) {
                if ( !requests.isEmpty() ) {
                    pair = requests.poll();
                }
            }
            if ( pair != null ) {
                try {
                    LOG.logDebug( "Sending ", pair.second );
                    LOG.logDebug( "Storing at ", pair.first );
                    BufferedImage img = null;

                    while ( img == null ) {
                        try {
                            URLConnection conn = new URL( pair.second ).openConnection();
                            conn.setReadTimeout( 60000 );
                            conn.setConnectTimeout( 60000 );
                            conn.setUseCaches( false );
                            InputStream stream = conn.getInputStream();
                            img = read( stream );
                            stream.close();
                            if ( img == null ) {
                                LOG.logError( "Cannot parse map for file '" + pair.first + "' using request '"
                                              + pair.second + "'" );
                                ++number;
                                continue outer;
                            }
                        } catch ( IOException e ) {
                            LOG.logError( "Unable to retrieve map for '" + pair.first + "' from '" + pair.second
                                          + "': " + e.getLocalizedMessage() );
                            System.gc(); // maybe a file or two will get cleaned up...
                            try {
                                Thread.sleep( 60000 );
                            } catch ( InterruptedException e1 ) {
                                // then we're gone anyway
                            }
                        }
                    }
                    int num = ++number;
                    if ( num % 100 == 0 ) {
                        LOG.logInfo( "Currently processing number " + num );
                    }
                    write( img, "png", new File( pair.first + ".png" ) );
                } catch ( MalformedURLException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( IOException e ) {
                    LOG.logError( "Unable to retrieve map from " + pair.second, e );
                }
            } else {
                try {
                    Thread.sleep( 100 );
                    LOG.logInfo( "Waiting for requests list..." );
                } catch ( InterruptedException e ) {
                    // ok, so we shutdown
                }
            }
        }
    }

    private static void printUsage( String missing ) {
        System.out.println( "The " + missing + " parameter is missing." );
        System.out.println( "--request,-q: the basic request, without LAYERS, BBOX, WIDTH and HEIGHT parameters. Mandatory." );
        System.out.println( "--layers,-l: the layers to request plus tiling directory. Can occur multiple times. Mandatory." );
        System.out.println( "--bbox,-b: the bounding box to update. Optional, default is bbox of the raster tree." );
        System.out.println( "--num-threads,-n: the number of requests to send simultaneously. Optional, default is 1." );
        System.out.println( "--directory,-d: the directory where the raster trees reside. Optional, default is current directory." );
        System.out.println( "--fix, -f: a log file to scan for requests that failed. Specifying this causes the program to go into a different mode." );
        System.out.println( "Example: " );
        System.out.println( "Paver -q \"http://demo.deegree.org/deegree-wms/services?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&TRANSPARENT=TRUE&FORMAT=image/png&SRS=EPSG:26912&STYLES=\" -l dir1:Vegetation -l dir2:Lake,Roads" );
        System.out.println();
        System.exit( 1 );
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main( String[] args )
                            throws IOException {
        Config config = new Config();
        for ( int i = 0; i < args.length; ++i ) {
            if ( args[i].equals( "--bbox" ) || args[i].equals( "-b" ) ) {
                if ( i != args.length - 1 ) {
                    config.bbox = createEnvelope( args[++i], null );
                }
            } else if ( args[i].equals( "--request" ) || args[i].equals( "-q" ) ) {
                if ( i != args.length - 1 ) {
                    config.request = args[++i];
                    if ( !config.request.endsWith( "&" ) ) {
                        config.request += "&";
                    }
                }
            } else if ( args[i].equals( "--num-threads" ) || args[i].equals( "-n" ) ) {
                if ( i != args.length - 1 ) {
                    config.numThreads = parseInt( args[++i] );
                }
            } else if ( args[i].equals( "--directory" ) || args[i].equals( "-d" ) ) {
                if ( i != args.length - 1 ) {
                    config.dir = new File( args[++i] );
                    if ( !config.dir.exists() ) {
                        config.dir.mkdirs();
                    }
                }
            } else if ( args[i].equals( "--layers" ) || args[i].equals( "-l" ) ) {
                if ( i != args.length - 1 ) {
                    String[] param = args[++i].split( ":" );
                    config.dirsToLayers.put( param[0], param[1] );
                }
            } else if ( args[i].equals( "--fix" ) || args[i].equals( "-f" ) ) {
                if ( i != args.length - 1 ) {
                    config.logfile = args[++i];
                }
            }
        }

        if ( config.request == null && config.logfile == null ) {
            printUsage( "request" );
        }

        Paver paver = new Paver( config );
        paver.sendRequests();
    }

    static class Config {
        Envelope bbox;

        String request, logfile;

        int numThreads = 1;

        File dir = new File( "." );

        Map<String, String> dirsToLayers = new TreeMap<String, String>();
    }

}
