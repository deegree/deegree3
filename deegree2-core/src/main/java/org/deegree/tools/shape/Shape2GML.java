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

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
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
public class Shape2GML {

    private static final ILogger LOG = LoggerFactory.getLogger( Shape2GML.class );

    private String inFile = null;

    private String outFile = null;

    private FeatureCollection fc = null;

    /** Creates a new instance of Shape2GML */
    public Shape2GML( String inFile, String outFile ) {
        this.inFile = inFile;
        this.outFile = outFile;
    }

    private FeatureCollection read()
                            throws Exception {

        LOG.logInfo( "reading " + inFile + " ... " );

        // open shape file
        ShapeFile sf = new ShapeFile( inFile );

        int count = sf.getRecordNum();

        // create (empty feature collection)
        fc = FeatureFactory.createFeatureCollection( inFile, count );

        // load each feature from the shape file create a deegree feature
        // and add it to the feature collection
        for ( int i = 0; i < count; i++ ) {
            if ( i % 10 == 0 ) {
                System.out.print( "." );
            }
            Feature feat = sf.getFeatureByRecNo( i + 1 );
            // add to feature collection
            fc.add( feat );
        }
        System.out.println();

        sf.close();

        return fc;
    }

    private void write()
                            throws Exception {
        LOG.logInfo( "writing " + outFile + " ... " );
        FileOutputStream fos = new FileOutputStream( outFile );
        new GMLFeatureAdapter().export( fc, fos );
        fos.close();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main( String[] args )
                            throws Exception {

        if ( args == null || args.length < 2 ) {
            System.out.println( "Two arguments - input file and output file - are required!" );
            System.exit( 1 );
        }

        Shape2GML s2g = new Shape2GML( args[0], args[1] );
        s2g.read();
        s2g.write();

    }

}
