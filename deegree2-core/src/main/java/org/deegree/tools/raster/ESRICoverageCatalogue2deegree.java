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
package org.deegree.tools.raster;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.ecwapi.ECWReader;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

import com.sun.media.jai.codec.FileSeekableStream;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ESRICoverageCatalogue2deegree {

    private static final String TEMPLATE = "esri_imagecatalogue2deegree_template.xml";

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private String inFile, outFile, crs, pathToFiles;

    /**
     * 
     * @param inFile
     * @param crs
     * @param outFile
     * @param pathToFiles
     */
    public ESRICoverageCatalogue2deegree( String inFile, String crs, String outFile, String pathToFiles ) {
        if ( inFile.toLowerCase().endsWith( ".dbf" ) ) {
            // ensure that input file name has no extension
            inFile = inFile.substring( 0, inFile.length() - 4 );
        }
        this.inFile = inFile;
        this.crs = crs;
        this.outFile = outFile;
        this.pathToFiles = pathToFiles;
        if ( !pathToFiles.endsWith( "/" ) && !pathToFiles.endsWith( "\\" ) ) {
            this.pathToFiles += "/";
        }
    }

    /**
     * 
     * @throws Exception
     */
    public void execute()
                            throws Exception {
        XMLFragment xml = new XMLFragment( ESRICoverageCatalogue2deegree.class.getResource( TEMPLATE ) );
        Element parent = (Element) XMLTools.getRequiredNode( xml.getRootElement(), "./deegreewcs:Resolution", nsContext );

        DBaseFile dbf = new DBaseFile( inFile );
        int count = dbf.getRecordNum();
        URI namespace = dbf.getFeatureType().getNameSpace();
        QualifiedName image = new QualifiedName( "IMAGE", namespace );
        QualifiedName xmin = new QualifiedName( "XMIN", namespace );
        QualifiedName ymin = new QualifiedName( "YMIN", namespace );
        QualifiedName xmax = new QualifiedName( "XMAX", namespace );
        QualifiedName ymax = new QualifiedName( "YMAX", namespace );
        double xmin_ = 9E99;
        double ymin_ = 9E99;
        double xmax_ = -9E99;
        double ymax_ = -9E99;
        CoordinateSystem srs = CRSFactory.create( crs );
        for ( int i = 0; i < count; i++ ) {
            Feature feature = dbf.getFRow( i + 1 );
            String fileName = (String) feature.getProperties( image )[0].getValue();
            File file = new File( fileName );
            if ( !file.isAbsolute() ) {
                fileName = pathToFiles + fileName;
            }
            double minx = ( (Number) feature.getProperties( xmin )[0].getValue() ).doubleValue();
            if ( minx < xmin_ ) {
                xmin_ = minx;
            }
            double miny = ( (Number) feature.getProperties( ymin )[0].getValue() ).doubleValue();
            if ( miny < ymin_ ) {
                ymin_ = miny;
            }
            double maxx = ( (Number) feature.getProperties( xmax )[0].getValue() ).doubleValue();
            if ( maxx > xmax_ ) {
                xmax_ = maxx;
            }
            double maxy = ( (Number) feature.getProperties( ymax )[0].getValue() ).doubleValue();
            if ( maxy > ymax_ ) {
                ymax_ = maxy;
            }
            int width = 0;
            int height = 0;
            if ( fileName.toLowerCase().endsWith( ".ecw" ) ) {
                ECWReader ecw = new ECWReader( fileName );
                width = ecw.getWidth();
                height = ecw.getHeight();
            } else {
                FileSeekableStream fss = new FileSeekableStream( fileName );
                RenderedOp rop = JAI.create( "stream", fss );
                width = ( (Integer) rop.getProperty( "image_width" ) ).intValue();
                height = ( (Integer) rop.getProperty( "image_height" ) ).intValue();
                fss.close();
            }
            Element elFile = XMLTools.appendElement( parent, CommonNamespaces.DEEGREEWCS, "File" );
            elFile.setAttribute( "width", Integer.toString( width ) );
            elFile.setAttribute( "height", Integer.toString( height ) );
            Element elName = XMLTools.appendElement( elFile, CommonNamespaces.DEEGREEWCS, "Name" );
            XMLTools.setNodeValue( elName, fileName );
            Element elEnv = XMLTools.appendElement( elFile, CommonNamespaces.GMLNS, "Envelope" );
            elEnv.setAttribute( "srsName", "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#" + srs.getLocalName() );
            Element elCoord = XMLTools.appendElement( elEnv, CommonNamespaces.GMLNS, "coordinates" );
            XMLTools.setNodeValue( elCoord, minx + "," + miny + " " + maxx + "," + maxy );
        }
        System.out.println( "Envelope in " + crs );
        System.out.println( xmin_ + "," + ymin_ + "," + xmax_ + "," + ymax_ );
        System.out.println();
        
        Envelope env = GeometryFactory.createEnvelope( xmin_, ymin_, xmax_, ymax_, srs );
        GeoTransformer gt = new GeoTransformer( "EPSG:4326" );
        env = gt.transform( env, srs );

        System.out.println( "Envelope in EPSG:4326 (WGS84)" );
        System.out.println( env.getMin().getX() + "," + env.getMin().getY() + "," + env.getMax().getX() + ","
                            + env.getMax().getY() );

        FileWriter fw = new FileWriter( outFile );
        fw.write( xml.getAsPrettyString() );
        fw.close();
    }

    /**
     * @param args
     */
    public static void main( String[] args )
                            throws Exception {
        Map<String, String> param = new HashMap<String, String>();
        for ( int i = 0; i < args.length; i += 2 ) {
            param.put( args[i], args[i + 1] );
        }

        String inFile = parameterCheck( param, "-inFile" );
        String crs = parameterCheck( param, "-crs" );
        String outFile = parameterCheck( param, "-outFile" );
        String pathToFiles = param.get( "-pathToFiles" );
        ESRICoverageCatalogue2deegree esri = new ESRICoverageCatalogue2deegree( inFile, crs, outFile, pathToFiles );
        esri.execute();
    }

    private static String parameterCheck( Map<String, String> param, String name ) {
        String p = param.get( name );
        if ( p == null ) {
            System.out.println( "parameter " + name + " is missing" );
            System.exit( 1 );
        }
        return p;
    }

}
