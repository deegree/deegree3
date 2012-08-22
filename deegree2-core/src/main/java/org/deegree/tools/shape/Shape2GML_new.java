// $HeadURL$
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
package org.deegree.tools.shape;

import java.io.FileOutputStream;
import java.io.IOException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileReader;
import org.deegree.io.shpapi.shape_new.ShapeGeometryException;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.GMLFeatureAdapter;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 1.1
 */
public class Shape2GML_new {

    private static final ILogger LOG = LoggerFactory.getLogger( Shape2GML_new.class );

    private String inFile = null;

    private String outFile = null;

    /**
     * Creates a new instance of Shape2GML
     *
     * @param inFile
     * @param outFile
     */
    public Shape2GML_new( String inFile, String outFile ) {
        if ( inFile.endsWith( ".shp" ) ) {
            inFile = inFile.substring( 0, inFile.lastIndexOf( "." ) );
        }
        this.inFile = inFile;
        this.outFile = outFile;
    }

    private FeatureCollection read()
                            throws IOException, DBaseException {

        LOG.logInfo( "Reading " + inFile + " ... " );

        // open shape file
        ShapeFileReader reader = new ShapeFileReader( inFile );
        ShapeFile sf = reader.read();

        return sf.getFeatureCollection();
    }

    private void write( FeatureCollection fc )
                            throws IOException, FeatureException {
        LOG.logInfo( "Writing " + outFile + " ... " );
        FileOutputStream fos = new FileOutputStream( outFile );
        new GMLFeatureAdapter().export( fc, fos );
        fos.close();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main( String[] args ) {

        try {
            if ( args == null || args.length < 2 ) {
                System.out.println( "Usage: java -cp ... ...Shape2GML_new <inputfile basename> <outputfile>" );
                System.exit( 1 );
            }

            Shape2GML_new s2g = new Shape2GML_new( args[0], args[1] );
            s2g.write( s2g.read() );
            LOG.logInfo( "Done." );
        } catch ( IOException e ) {
            e.printStackTrace();
            LOG.logError( "An IO error occured.", e );
        } catch ( FeatureException e ) {
            e.printStackTrace();
            LOG.logError( "Features could not be created.", e );
        } catch ( ShapeGeometryException e ) {
            e.printStackTrace();
            LOG.logError( "Some error occured while converting Geometries.", e );
        } catch ( DBaseException e ) {
            e.printStackTrace();
            LOG.logError( "The .dbf could not be read.", e );
        }
    }

}
