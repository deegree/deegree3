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

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.parseDouble;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.framework.xml.XMLTools.getElement;
import static org.deegree.model.spatialschema.GeometryFactory.createEnvelope;
import static org.deegree.ogcbase.CommonNamespaces.DEEGREEWMS;
import static org.deegree.ogcbase.CommonNamespaces.DGJDBC;
import static org.deegree.ogcbase.CommonNamespaces.XLNNS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;

import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <code>MapServer2DeegreeWMS</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MapServer2DeegreeWMS {

    private static final ILogger LOG = getLogger( MapServer2DeegreeWMS.class );

    private File mapFile;

    private Layer root = new Layer();

    private boolean featureTypes;

    private MapServer2DeegreeWMS( String name, boolean featureTypes ) {
        this.featureTypes = featureTypes;
        mapFile = new File( name );
        if ( !mapFile.exists() ) {
            LOG.logInfo( "The input file does not exist!" );
            return;
        }
    }

    private String arg( String line, String name ) {
        if ( line.trim().toLowerCase().startsWith( name.toLowerCase() ) ) {
            String arg = line.trim().substring( name.length() ).trim();
            return arg.startsWith( "\"" ) ? arg.substring( 1, arg.length() - 1 ) : arg;
        }
        return null;
    }

    private void readTree()
                            throws IOException {
        BufferedReader in = new BufferedReader( new FileReader( mapFile ) );

        boolean rootRead = false;
        Layer current = root;
        String currentConn = null;

        String s;
        while ( ( s = in.readLine() ) != null ) {

            if ( s.trim().equalsIgnoreCase( "layer" ) ) {
                rootRead = true;
                current = new Layer();
            }

            if ( current.name == null ) {
                current.name = arg( s, "name" );
            }
            if ( current.title == null ) {
                current.title = arg( s, "\"wms_title\"" );
            }

            if ( rootRead ) {
                String cat = arg( s, "\"wms_layer_group\"" );
                if ( cat != null ) {
                    Layer parent = root;
                    String[] cats = cat.substring( 1 ).split( "/" );
                    outer: for ( String c : cats ) {
                        for ( Layer l : parent.children ) {
                            if ( l.title.equals( c ) ) {
                                parent = l;
                                continue outer;
                            }
                        }
                        Layer l = new Layer();
                        l.title = c;
                        parent.children.add( l );
                        parent = l;
                    }

                    parent.children.add( current );
                }

                String conn = arg( s, "connection " );
                if ( conn != null ) {
                    currentConn = conn;
                }
                String connType = arg( s, "connectiontype" );
                if ( connType != null && connType.equalsIgnoreCase( "oraclespatial" ) ) {
                    current.oracleconn = currentConn;
                }
                if ( connType != null && connType.equalsIgnoreCase( "wms" ) ) {
                    current.remotewms = currentConn;
                }
                // TODO add more connection types
                String data = arg( s, "data" );
                if ( data != null ) {
                    current.data = data;
                }
                String srs = arg( s, "\"wms_srs\"" );
                if ( srs != null ) {
                    current.srs = srs;
                }
                String minscale = arg( s, "minscale" );
                if ( minscale != null ) {
                    current.minscale = minscale;
                }
                String maxscale = arg( s, "maxscale" );
                if ( maxscale != null ) {
                    current.maxscale = maxscale;
                }
                String bbox = arg( s, "\"wms_extent\"" );
                if ( bbox != null ) {
                    current.bbox = bbox;
                }
                String wmsname = arg( s, "\"wms_name\"" );
                if ( wmsname != null ) {
                    current.wmsname = wmsname;
                }
                String wmsversion = arg( s, "\"wms_server_version\"" );
                if ( wmsversion != null ) {
                    current.wmsversion = wmsversion;
                }
                String wmsformat = arg( s, "\"wms_format\"" );
                if ( wmsformat != null ) {
                    current.wmsformat = wmsformat;
                }
                String type = arg( s, "type" );
                if ( type != null ) {
                    current.raster = type.equalsIgnoreCase( "raster" );
                }
            } else {
                String extent = arg( s, "extent" );
                if ( extent != null ) {
                    current.bbox = extent;
                }
                String proj = arg( s, "\"init=" );
                if ( proj != null ) {
                    current.srs = proj.substring( 0, proj.length() - 1 );
                }
            }

        }
        root.print( 0 );
    }

    private void appendLayer( Element root, Layer layer )
                            throws InvalidParameterException, CRSTransformationException {
        root = appendElement( root, null, layer.raster ? "RasterLayer" : "Layer" );

        root.setAttribute( "queryable", "1" );

        if ( layer.name != null ) {
            appendElement( root, null, "Name", layer.name );
        }
        if ( layer.title != null ) {
            appendElement( root, null, "Title", layer.title );
        }
        if ( layer.srs != null && !layer.srs.equalsIgnoreCase( this.root.srs ) ) {
            appendElement( root, null, "SRS", this.root.srs.toUpperCase() );
        }
        if ( layer.srs != null ) {
            appendElement( root, null, "SRS", layer.srs.toUpperCase() );
        }
        if ( layer.bbox != null && layer.srs != null ) {
            String[] ss = layer.bbox.split( " " );
            double minx = Double.parseDouble( ss[0] );
            double miny = Double.parseDouble( ss[1] );
            double maxx = Double.parseDouble( ss[2] );
            double maxy = Double.parseDouble( ss[3] );
            try {
                Envelope bbox = createEnvelope( minx, miny, maxx, maxy, CRSFactory.create( layer.srs ) );
                Envelope wgs84bbox = new GeoTransformer( "EPSG:4326" ).transform( bbox, layer.srs, true );
                Element elem = appendElement( root, null, "LatLonBoundingBox" );
                elem.setAttribute( "minx", "" + wgs84bbox.getMin().getX() );
                elem.setAttribute( "miny", "" + wgs84bbox.getMin().getY() );
                elem.setAttribute( "maxx", "" + wgs84bbox.getMax().getX() );
                elem.setAttribute( "maxy", "" + wgs84bbox.getMax().getY() );

                elem = appendElement( root, null, "BoundingBox" );
                elem.setAttribute( "SRS", layer.srs );
                elem.setAttribute( "minx", "" + minx );
                elem.setAttribute( "miny", "" + miny );
                elem.setAttribute( "maxx", "" + maxx );
                elem.setAttribute( "maxy", "" + maxy );
            } catch ( UnknownCRSException e ) {
                LOG.logError( "Unknown error", e );
            }
        }

        Element sh = appendElement( root, null, "ScaleHint" );
        sh.setAttribute( "min", layer.minscale == null ? "0"
                                                      : Double.toString( ( parseDouble( layer.minscale ) / 0.00028 ) ) );
        sh.setAttribute( "max", layer.maxscale == null ? "" + MAX_VALUE
                                                      : Double.toString( ( parseDouble( layer.maxscale ) / 0.00028 ) ) );

        if ( layer.remotewms == null && layer != this.root && layer.name != null ) { // no style for remotewms layers
            Element style = appendElement( root, null, "Style" );
            appendElement( style, null, "Name", "default" );
            appendElement( style, null, "Title", "default" );
            appendElement( style, DEEGREEWMS, "StyleResource", "styles/" + layer.name + ".sld" );
        }

        if ( layer.name != null ) {
            Element ds = appendElement( root, DEEGREEWMS, "DataSource" );
            ds.setAttribute( "failOnException", "0" );
            ds.setAttribute( "queryable", "1" );
            if ( layer.oracleconn != null ) {
                String[] data = layer.data.split( " " );

                if ( featureTypes ) {
                    appendElement( ds, DEEGREEWMS, "Name", "app:" + data[2].trim().toUpperCase() );
                    appendElement( ds, DEEGREEWMS, "GeometryProperty", "app:" + data[0].toLowerCase() );
                } else {
                    appendElement( ds, DEEGREEWMS, "Name", layer.name );
                    appendElement( ds, DEEGREEWMS, "Type", "DATABASE" );
                    Element elem = appendElement( ds, DGJDBC, "JDBCConnection" );
                    appendElement( elem, DGJDBC, "Driver", "oracle.jdbc.driver.OracleDriver" );
                    String user = layer.oracleconn.substring( 0, layer.oracleconn.indexOf( "/" ) );
                    String pass = layer.oracleconn.substring( user.length() + 1, layer.oracleconn.indexOf( "@" ) );
                    String url = layer.oracleconn.substring( user.length() + pass.length() + 1 ); // keep the @
                    url = "jdbc:oracle:thin:" + url;
                    appendElement( elem, DGJDBC, "Url", url );
                    appendElement( elem, DGJDBC, "User", user );
                    appendElement( elem, DGJDBC, "Password", pass );
                    appendElement( ds, DEEGREEWMS, "GeometryField", data[0] );
                    appendElement( ds, DEEGREEWMS, "SQLTemplate", "select * " + layer.data.substring( data[0].length() )
                                                                  + " where " );
                    appendElement( ds, DEEGREEWMS, "NativeCRS", layer.srs );
                }
            }
            if ( layer.remotewms != null ) {
                appendElement( ds, DEEGREEWMS, "Name", layer.name );
                appendElement( ds, DEEGREEWMS, "Type", "REMOTEWMS" );
                Element elem = appendElement( ds, DEEGREEWMS, "OWSCapabilities" );
                elem = appendElement( elem, DEEGREEWMS, "OnlineResource" );
                elem.setAttributeNS( "http://www.w3.org/1999/xlink", "xlink:type", "simple" );
                elem.setAttributeNS( "http://www.w3.org/1999/xlink", "xlink:href",
                                     layer.remotewms + "?request=GetCapabilities&service=WMS&version="
                                                             + layer.wmsversion );
                elem = appendElement( ds, DEEGREEWMS, "FilterCondition" );
                appendElement( elem, DEEGREEWMS, "WMSRequest", "service=WMS&version=1.1.1&request=GetMap&format="
                                                               + layer.wmsformat
                                                               + "&transparent=true&exceptions=application/vnd.ogc."
                                                               + "se_inimage&styles=&layers=" + layer.wmsname + "&srs="
                                                               + layer.srs );
            }
            if ( layer.raster ) {
                appendElement( ds, DEEGREEWMS, "Name", layer.name );
                appendElement( ds, DEEGREEWMS, "Type", "LOCALWCS" );
                Element e = appendElement( ds, DEEGREEWMS, "OWSCapabilities" );
                e = appendElement( e, DEEGREEWMS, "OnlineResource" );
                e.setAttributeNS( XLNNS.toASCIIString(), "xlink:type", "simple" );
                e.setAttributeNS( XLNNS.toASCIIString(), "xlink:href", "LOCALWCS_configuration.xml" );
                e = appendElement( ds, DEEGREEWMS, "FilterCondition" );
                e = appendElement( e, DEEGREEWMS, "WCSRequest",
                                   "VERSION=1.0.0&TRANSPARENT=TRUE&FORMAT=png&EXCEPTIONS=application/vnd.ogc.se_xml&coverage="
                                                           + layer.name );
            }
        }

        for ( Layer l : layer.children ) {
            appendLayer( root, l );
        }
    }

    private void produceWMSConfig()
                            throws IOException, SAXException, XMLParsingException, InvalidParameterException,
                            CRSTransformationException, TransformerException {
        XMLFragment doc = new XMLFragment( MapServer2DeegreeWMS.class.getResource( "wms_configuration_template.xml" ) );
        Element capability = getElement( doc.getRootElement(), "/WMT_MS_Capabilities/Capability", null );
        appendLayer( capability, root );
        doc.prettyPrint( new FileWriter( "wms_configuration.xml" ) );
    }

    /**
     * @param args
     * @throws IOException
     * @throws CRSTransformationException
     * @throws XMLParsingException
     * @throws SAXException
     * @throws InvalidParameterException
     * @throws TransformerException
     */
    public static void main( String[] args )
                            throws IOException, InvalidParameterException, SAXException, XMLParsingException,
                            CRSTransformationException, TransformerException {
        if ( args.length < 1 ) {
            LOG.logInfo( "Usage:" );
            LOG.logInfo( "java -cp deegree2.jar org.deegree.tools.wms.MapServer2DeegreeWMS [options] <mapfile>" );
            LOG.logInfo( "Options:" );
            LOG.logInfo( " -f: generate feature type data sources instead of database data sources" );
            return;
        }

        if ( new File( "wms_configuration.xml" ).exists() ) {
            LOG.logInfo( "wms_configuration.xml already exists. Please remove it and run the tool again." );
            return;
        }

        String file = args.length > 1 ? args[1] : args[0];
        MapServer2DeegreeWMS conv;
        if ( args.length > 1 ) {
            conv = new MapServer2DeegreeWMS( file, args[0].equals( "-f" ) );
        } else {
            conv = new MapServer2DeegreeWMS( file, false );
        }
        conv.readTree();
        conv.produceWMSConfig();
    }

    static class Layer {
        String name, title, oracleconn, remotewms, bbox, srs, wmsname, wmsformat, data, wmsversion, minscale, maxscale;

        boolean raster;

        LinkedList<Layer> children = new LinkedList<Layer>();

        static void maybePrint( int indent, String name, String val ) {
            if ( val != null ) {
                for ( int i = 0; i < indent; ++i ) {
                    System.out.print( ' ' );
                }
                System.out.println( name + " " + val );
            }
        }

        void print( int indent ) {
            maybePrint( indent, "NAME", name );
            maybePrint( indent, "TITLE", title );
            maybePrint( indent, "ORACLE CONNECTION", oracleconn );
            maybePrint( indent, "REMOTEWMS", remotewms );
            maybePrint( indent, "REMOTEWMS NAME", wmsname );
            maybePrint( indent, "REMOTEWMS FORMAT", wmsformat );
            maybePrint( indent, "SRS", srs );
            maybePrint( indent, "BBOX", bbox );
            for ( Layer l : children ) {
                l.print( indent + 2 );
            }
        }
    }

}
