//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.configuration;

import static org.deegree.model.crs.projections.ProjectionUtils.EPS11;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSConfiguration</code> creates, instantiates and supplies a configured CRS-Provider. Because only one
 * crs-configuration is needed inside the JVM, this implementation uses a singleton pattern.
 * <p>
 * The configuration will try to read the file: crs_providers.properties. It uses following strategie to load this file,
 * first the root directory (e.g. '/' or WEB-INF/classes ) will be searched. If no file was found there, it will try to
 * load from the package. The properties file must denote a property with name 'CRS_PROVIDER' followed by a '=' and a
 * fully qualified name denoting the class (an instance of CRSProvider) which should be available in the classpath. This
 * class must have an empty constructor.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class CRSConfiguration {
    private static Logger LOG = LoggerFactory.getLogger( CRSConfiguration.class );

    private CRSProvider provider;

    //
    private final static String PROVIDER_CONFIG = "crs_providers.properties";

    private static CRSConfiguration CONFIG = null;

    /**
     * @param provider
     *            to get the CRS's from.
     */
    private CRSConfiguration( CRSProvider provider ) {
        this.provider = provider;
    }

    /**
     * Creates or returns an instance of the CRSConfiguration by reading the DEFAULT property configured in the
     * 'crs_providers.properties'. If no key is given (or no string could be loaded), the {@link DeegreeCRSProvider}
     * will be used.
     * 
     * @return an instance of a CRS-Configuration with the configured CRSProvider.
     * @throws CRSConfigurationException
     *             if --anything-- went wrong while instantiating the CRSProvider.
     */
    public synchronized static CRSConfiguration getCRSConfiguration()
                            throws CRSConfigurationException {
        if ( CONFIG != null ) {
            return CONFIG;
        }
        CRSProvider provider = null;

        LOG.debug( "Trying to load configured CRS provider from configuration (/crs_providers.properties)." );
        InputStream is = CRSConfiguration.class.getResourceAsStream( "/" + PROVIDER_CONFIG );
        if ( is == null ) {
            LOG.debug( "Trying to load configured CRS provider from configuration (org.deegree.model.crs.configuration.crs_providers.properties)." );
            is = CRSConfiguration.class.getResourceAsStream( PROVIDER_CONFIG );
        }
        if ( is == null ) {
            LOG.warn( Messages.getMessage( "CRS_CONFIG_NO_PROVIDER_DEFS_FOUND", PROVIDER_CONFIG ) );
            // create the standard deegree-crs-provider.
            provider = new DeegreeCRSProvider( null );
        } else {
            Properties props = new Properties();
            try {
                props.load( is );
            } catch ( IOException e ) {
                LOG.error( e.getMessage(), e );
            } finally {
                try {
                    is.close();
                } catch ( IOException e ) {
                    // no output if the stream can't be closed, just leave it as it is.
                }
            }

            String className = props.getProperty( "CRS_PROVIDER" );
            if ( className == null || "".equals( className.trim() ) ) {
                LOG.warn( Messages.getMessage( "CRS_CONFIG_NO_PROVIDER_FOUND", PROVIDER_CONFIG ) );
                provider = new DeegreeCRSProvider( null );
            } else if ( "org.deegree.model.crs.configuration.DeegreeCRSProvider".equals( className ) ) {
                LOG.debug( "The configured CRS provider is a Deegree CRSProvider with name: " + className );
                provider = new DeegreeCRSProvider( null );
            } else {
                // use reflection to instantiate the configured provider.
                try {
                    LOG.debug( "Trying to load configured CRS provider from classname: " + className );
                    provider = (CRSProvider) Class.forName( className ).newInstance();
                } catch ( InstantiationException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ) );
                } catch ( IllegalAccessException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( ClassNotFoundException e ) {

                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } finally {
                    if ( provider == null ) {
                        LOG.info( "The configured class: " + className
                                     + " was not created. Trying to create a deegree-crs-provider" );
                        provider = new DeegreeCRSProvider( null );
                    }
                }
            }
        }
        CONFIG = new CRSConfiguration( provider );
        return CONFIG;

    }

    /**
     * export the given file to the deegree-crs format.
     * 
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            outputHelp();
        }
        Map<String, String> params = new HashMap<String, String>( 5 );
        for ( int i = 0; i < args.length; i++ ) {
            String arg = args[i];
            if ( arg != null && !"".equals( arg.trim() ) ) {
                arg = arg.trim();
                if ( arg.equalsIgnoreCase( "-?" ) || arg.equalsIgnoreCase( "-h" ) ) {
                    outputHelp();
                } else {
                    if ( i + 1 < args.length ) {
                        String val = args[++i];
                        if ( val != null && !"".equals( val.trim() ) ) {
                            params.put( arg, val.trim() );
                        } else {
                            System.out.println( "Invalid value for parameter: " + arg );
                        }
                    } else {
                        System.out.println( "No value for parameter: " + arg );
                    }
                }
            }
        }
        String inFormat = params.get( "-inFormat" );
        if ( inFormat == null || "".equals( inFormat.trim() ) ) {
            System.out.println( "No input format (inFormat) defined, setting to proj4" );
            inFormat = "proj4";
        }
        String inFile = params.get( "-inFile" );
        if ( inFile == null || "".equals( inFile.trim() ) ) {
            System.out.println( "No input file set, exiting\n" );
            outputHelp();
            System.exit( 1 );
        }
        File inputFile = new File( inFile );

        String outFile = params.get( "-outFile" );
        String outFormat = params.get( "-outFormat" );
        if ( outFormat == null || "".equals( outFormat.trim() ) ) {
            System.out.println( "No output format (outFormat) defined, setting to deegree" );
            outFormat = "deegree";
        }

        String veri = params.get( "-verify" );
        boolean verify = ( veri != null && !"".equals( inFile.trim() ) );

        CRSProvider in = new PROJ4CRSProvider( inputFile );
        if ( "deegree".equalsIgnoreCase( inFormat ) ) {
            try {
                in = new DeegreeCRSProvider( inputFile );
            } catch ( CRSConfigurationException e ) {
                e.printStackTrace();
            }
        }

        CRSProvider out = new DeegreeCRSProvider();
        if ( "proj4".equalsIgnoreCase( outFormat ) ) {
            out = new PROJ4CRSProvider();
        }

        try {
            List<CoordinateSystem> allSystems = in.getAvailableCRSs();
            if ( verify ) {
                out = new DeegreeCRSProvider( null );
                List<CoordinateSystem> notExported = new LinkedList<CoordinateSystem>();
                for ( CoordinateSystem inCRS : allSystems ) {
                    if ( inCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                        String id = inCRS.getIdentifier();
                        CoordinateSystem outCRS = out.getCRSByID( id );
                        // System.out.print( "Getting crs: " + id + " and projection: " +
                        // ((ProjectedCRS)inCRS).getProjection().getDeegreeSpecificName() );
                        if ( outCRS != null && outCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                            // System.out.println( "... [SUCCESS] to retrieve from deegree-config
                            // with projection: " +
                            // ((ProjectedCRS)outCRS).getProjection().getDeegreeSpecificName() );
                            Projection inProj = ( (ProjectedCRS) inCRS ).getProjection();
                            Projection outProj = ( (ProjectedCRS) outCRS ).getProjection();
                            if ( Math.abs( inProj.getProjectionLatitude() - outProj.getProjectionLatitude() ) > EPS11 ) {
                                System.out.println( "For the projection with id: " + id
                                                    + " the projectionLatitude differs:\n in ("
                                                    + ( (ProjectedCRS) inCRS ).getProjection().getName() + "): "
                                                    + Math.toDegrees( inProj.getProjectionLatitude() ) + "\nout("
                                                    + ( (ProjectedCRS) outCRS ).getProjection().getName()
                                                    + " with id: "
                                                    + ( (ProjectedCRS) outCRS ).getProjection().getName() + "): "
                                                    + Math.toDegrees( outProj.getProjectionLatitude() ) );
                            }
                        } else {
                            notExported.add( inCRS );
                            System.out.println( id + " [FAILED] to retrieve from deegree-config." );
                        }
                    }
                }
                if ( notExported.size() > 0 ) {
                    StringBuilder sb = new StringBuilder( notExported.size() * 2000 );
                    out.export( sb, allSystems );
                    if ( outFile != null && !"".equals( outFile.trim() ) ) {
                        File outputFile = new File( outFile );
                        BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) );
                        writer.write( sb.toString() );
                        writer.flush();
                        writer.close();
                    } else {
                        System.out.println( sb.toString() );
                    }
                }
            } else {

                StringBuilder sb = new StringBuilder( allSystems.size() * 2000 );
                out.export( sb, allSystems );
                if ( outFile != null && !"".equals( outFile.trim() ) ) {
                    File outputFile = new File( outFile );
                    BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) );
                    writer.write( sb.toString() );
                    writer.flush();
                    writer.close();
                } else {
                    System.out.println( sb.toString() );
                }
            }
        } catch ( CRSConfigurationException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // CRSConfiguration config = new CRSConfiguration(
    }

    private static void outputHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append( "The CRSConfiguration program can be used to create a deegree-crs-configuration, from other crs definition-formats. Following parameters are supported:\n" );
        sb.append( "-inFile the /path/to/crs-definitions-file\n" );
        sb.append( "-inFormat the format of the input file, valid values are proj4(default),deegree \n" );
        sb.append( "-outFormat the format of the output file, valid values are deegree (default)\n" );
        sb.append( "-outFile the /path/to/the/output/file or standard output if not supplied.\n" );
        sb.append( "[-verify] checks the projection parameters of the inFormat against the deegree configuration.\n" );
        sb.append( "-?|-h output this text\n" );
        sb.append( "example usage: java -cp deegree.jar org.deegree.model.crs.configuration.CRSConfiguration -inFormat 'proj4' -inFile '/home/proj4/nad/epsg' -outFormat 'deegree' -outFile '/home/deegree/crs-definitions.xml'\n" );
        System.out.println( sb.toString() );
        System.exit( 1 );
    }

    /**
     * @return the crs provider.
     */
    public final CRSProvider getProvider() {
        return provider;
    }
}
