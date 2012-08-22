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
package org.deegree.tools.srs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;

/**
 * A utility program to inform the callee about the availability (-isAvailable param) of a certain crs or to retrieve
 * all available crs's from the deegree crs configuration.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SRSInfo {
    private final CRSProvider provider;

    /**
     * constructor creating a crs provider
     */
    SRSInfo() {
        CRSConfiguration config = CRSConfiguration.getCRSConfiguration();
        provider = config.getProvider();
        if ( provider == null ) {
            System.out.println( "Could not retrieve a deegree crs-provider instance, this may not be, please make sure your deegree installation uses a correct crs-configuration. Exiting!" );
        }
    }

    /**
     * returns true if the the passed SRS is available in deegree
     * 
     * @param srs
     * @return <code>true</code> if the the passed SRS is available in deegree
     */
    private boolean isAvailable( String srs ) {
        return provider.getCRSByID( srs ) != null;
    }

    /**
     * @return a list of crs's with following layout 1) crsid[0], crsid[1] ... etc.
     */
    private List<String> getAll( boolean verify, File exportFile ) {

        List<String> allCRSs = new ArrayList<String>();
        if ( verify ) {
            List<CoordinateSystem> avCRS = provider.getAvailableCRSs();
            if ( avCRS != null && avCRS.size() > 0 ) {
                for ( CoordinateSystem crs : avCRS ) {
                    String[] ids = crs.getIdentifiers();
                    if ( ids != null ) {
                        StringBuilder sb = new StringBuilder( 300 );
                        for ( int i = 0; i < ids.length; ++i ) {
                            sb.append( ids[i] );
                            if ( i + 1 < ids.length ) {
                                sb.append( ", " );
                            }
                        }
                        allCRSs.add( sb.toString() );
                    }
                }
            }
            if ( exportFile != null ) {
                StringBuilder out = new StringBuilder( 20000000 );
                provider.export( out, avCRS );
                try {
                    BufferedWriter bw = new BufferedWriter( new FileWriter( exportFile ) );
                    bw.write( out.toString() );
                    bw.flush();
                    bw.close();
                } catch ( IOException e ) {
                    System.out.println( e );
                }
            }
        } else {
            allCRSs = provider.getAvailableCRSIds();
            }
        Collections.sort( allCRSs );
        return allCRSs;
    }

    /**
     * @param args
     *            following parameters are supported:
     *            <ul>
     *            <li>[-isAvailable srsName]</li>
     *            <li>[-file outputfile]</li>
     *            <li>[-verify]</li>
     *            </ul>
     */
    public static void main( String[] args ) {

        SRSInfo srsinfo = new SRSInfo();

        HashMap<String, String> params = new HashMap<String, String>();
        boolean verify = false;
        for ( int i = 0; i < args.length; i++ ) {
            String firstArgument = args[i];
            if ( firstArgument != null && !"".equals( firstArgument.trim() ) ) {
                firstArgument = firstArgument.trim();
                if ( firstArgument.equalsIgnoreCase( "-?" ) || firstArgument.equalsIgnoreCase( "-h" ) ) {
                    outputHelp();
                } else {
                    if ( "-verify".equalsIgnoreCase( firstArgument ) ) {
                        verify = true;
                    } else {
                        if ( i + 1 < args.length ) {
                            String val = args[++i];
                            if ( val != null && !"".equals( val.trim() ) ) {
                                params.put( firstArgument, val.trim() );
                            } else {
                                System.out.println( "Invalid value for parameter: " + firstArgument );
                            }
                        } else {
                            System.out.println( "No value for parameter: " + firstArgument );
                        }
                    }
                }
            }
        }
        String availableCRS = params.get( "-isAvailable" );
        if ( availableCRS != null && !"".equals( availableCRS.trim() ) ) {
            System.out.println( "Coordinates System: " + availableCRS + " is "
                                + ( ( srsinfo.isAvailable( availableCRS.trim() ) ) ? "" : "not " )
                                + "available in deegree" );
        } else {
            File exportFile = null;// new File( "/dev/shm/crs-configuration.xml" );
            List<String> availableCRSs = srsinfo.getAll( verify, exportFile );
            if ( availableCRSs != null && availableCRSs.size() > 0 ) {
                String file = params.get( "-file" );
                if ( file != null && !"".equals( file.trim() ) ) {
                    File f = new File( file );
                    boolean overwrite = true;
                    if ( f.exists() ) {
                        System.out.print( "The file: " + file + " already exsists, overwrite ([y]/n): " );
                        BufferedReader read = new BufferedReader( new InputStreamReader( System.in ) );
                        String s = "n";
                        try {
                            s = read.readLine();
                        } catch ( IOException e ) {
                            // nottin.
                        }
                        if ( s != null && !"".equals( s.trim() ) && !"y".equalsIgnoreCase( s.trim() ) ) {
                            overwrite = false;
                        }

                    }
                    if ( overwrite ) {
                        System.out.println( "Writing to file: " + f.getAbsoluteFile() );
                        try {
                            FileWriter fw = new FileWriter( f );

                            int count = 1;
                            for ( String crs : availableCRSs ) {
                                fw.write( ( count++ ) + ")" + crs + "\n" );
                                // fw.write( crs );
                            }
                            fw.close();
                        } catch ( IOException e ) {
                            System.out.println( "An exception occurred while trying to write to file: "
                                                + f.getAbsoluteFile() + "\n message:\n" + e.getMessage() );
                            e.printStackTrace();
                        }

                        System.exit( 1 );
                    } else {
                        System.out.println( "Not overwriting file: " + f.getAbsoluteFile()
                                            + ", outputting to standard out." );
                    }
                } else {
                    System.out.println( "No File given (-file param) writing to standard out." );
                }
                int count = 1;
                for ( String crs : availableCRSs ) {
                    System.out.println( ( count++ ) + ")" + crs );
                }
            } else {
                System.out.println( "No Coordinate Systems configured, this is very strange!" );
            }
        }
    }

    private static void outputHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append( "The SRSInfo program can be used to output all available crs's configured in deegree and\n" );
        sb.append( "will give you an affirmation on an available crs.\n\n" );
        sb.append( "Following parameters are supported:\n" );
        sb.append( "[-isAvailable] crs_id -- will give you an affirmation if the given crs is available.\n" );
        sb.append( "[-file] if [-isAvailable] is not given, the -file parameter can be used to write all configured crs_s to, if not given standard out will be used.\n" );
        sb.append( "[-verify] if [-isAvailable] is not given, the -verify flag can be used to verify if the provider can create all configured crs_s, thus verifying if the configuration is correct.\n" );
        sb.append( "-?|-h output this text\n" );
        System.out.println( sb.toString() );
        System.exit( 1 );
    }
}
