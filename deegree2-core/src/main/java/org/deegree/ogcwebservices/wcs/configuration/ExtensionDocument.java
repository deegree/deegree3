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
package org.deegree.ogcwebservices.wcs.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.values.Interval;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.InvalidGMLException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class creates a class representation of the Extension section of a deegree WCS coverage offering (coverage
 * configuration) element. the extension section contains informations about data access/sources for different
 * resolutions and ranges.<BR>
 * an extension section must contain at least one Resolution element but can contains as much as the user may defined. A
 * resoluton contains a access informations for data and the ranges the data are assigned to. because of this it is
 * possible that more than one Resoultion element with same resolution range but with different other ranges (e.g. time
 * or elevation)
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 1.1
 */
public class ExtensionDocument {

    private static final ILogger LOG = LoggerFactory.getLogger( ExtensionDocument.class );

    private static URI GMLNS = CommonNamespaces.GMLNS;

    private static URI DGRNS = CommonNamespaces.DEEGREEWCS;

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    private URL systemId = null;

    private Element root = null;

    /**
     * constructing the ExtensionBuilder by passing the root element of a deegree WCS CoverageOffering Extension.
     * 
     * @param root
     * @param systemId
     */
    public ExtensionDocument( Element root, URL systemId ) {
        this.root = root;
        this.systemId = systemId;
    }

    /**
     * returns the content of the Extension element of te deegree WCS coverage description (configuration document). the
     * extension section contains informations about data access/sources for different resolutions and ranges.<BR>
     * an extension section must contain at least one Resolution element but can contains as much as the user may
     * defined. A resoluton contains a access informations for data and the ranges the data are assigned to. because of
     * this it is possible that more than one Resoultion element with same resolution range but with different other
     * ranges (e.g. time or elevation)
     * 
     * @return content of the Extension element of te deegree WCS coverage description
     * @throws InvalidCVExtensionException
     * @throws UnknownCVExtensionException
     * @throws InvalidParameterValueException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public Extension getExtension()
                            throws InvalidCVExtensionException, UnknownCVExtensionException,
                            InvalidParameterValueException, InvalidGMLException, UnknownCRSException {
        Extension extension = null;
        try {
            String type = XMLTools.getRequiredNodeAsString( root, "./@type", nsc );
            double offset = XMLTools.getNodeAsDouble( root, "./@offset", nsc, 0 );
            double scaleFactor = XMLTools.getNodeAsDouble( root, "./@scaleFactor", nsc, 1 );
            ElementList el = XMLTools.getChildElements( "Resolution", DGRNS, root );
            Resolution[] resolutions = getResolutions( type, el );
            extension = new DefaultExtension( type, resolutions, offset, scaleFactor );
        } catch ( XMLParsingException e ) {
            throw new InvalidCVExtensionException( StringTools.stackTraceToString( e ) );
        }
        return extension;
    }

    /**
     * returns the resolutions definitions within the Extension element of the deegree WCS coverage offering. Each
     * resoultion contains access description for its data and an optional description of the ranges the data are valid
     * for.
     * 
     * @param type
     * @param el
     * @return resolutions definitions within the Extension element of the deegree WCS coverage offering
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     * @throws UnknownCRSException
     */
    private Resolution[] getResolutions( String type, ElementList el )
                            throws XMLParsingException, InvalidParameterValueException, InvalidGMLException,
                            UnknownCRSException {
        Resolution[] resolutions = new Resolution[el.getLength()];
        for ( int i = 0; i < resolutions.length; i++ ) {
            resolutions[i] = getResolution( type, el.item( i ) );
        }
        return resolutions;
    }

    /**
     * creates an instance of <tt>Resoltuion</tt> from the passed <tt>Element</tt> and the type of the coverage source.
     * Valid values for type are:
     * <ul>
     * <li>shapeIndexed
     * <li>nameIndexed
     * <li>file
     * </ul>
     * if an unknown typed is passed an <tt>InvalidParameterValueException</tt> will be thrown
     * 
     * @param type
     * @param element
     * @return created Resoltuion
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private Resolution getResolution( String type, Element element )
                            throws XMLParsingException, InvalidParameterValueException, InvalidGMLException,
                            UnknownCRSException {
        String tmp = XMLTools.getRequiredAttrValue( "min", null, element );
        double min = Double.parseDouble( tmp );
        tmp = XMLTools.getRequiredAttrValue( "max", null, element );
        double max = Double.parseDouble( tmp );
        // ElementList el = XMLTools.getChildElements( "Range", DGRNS, element );
        NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
        List<Node> el = XMLTools.getNodes( element, "deegreewcs:Range", nsContext );
        Range[] ranges = getRanges( el );
        Resolution resolution = null;

        if ( type.equals( Extension.SHAPEINDEXED ) ) {
            // TODO
            // enable more than one shape
            Element elem = XMLTools.getRequiredElement( element, "deegreewcs:Shape", nsContext );
            Shape shape = getShape( elem );
            resolution = new ShapeResolution( min, max, ranges, shape );
        } else if ( type.equals( Extension.NAMEINDEXED ) ) {
            ElementList ell = XMLTools.getChildElements( "Directory", DGRNS, element );
            Directory[] dirs = new Directory[ell.getLength()];
            for ( int i = 0; i < dirs.length; i++ ) {
                dirs[i] = getDirectory( ell.item( i ) );
            }
            resolution = new DirectoryResolution( min, max, ranges, dirs );
        } else if ( type.equals( Extension.FILEBASED ) ) {
            ElementList ell = XMLTools.getChildElements( "File", DGRNS, element );
            org.deegree.ogcwebservices.wcs.configuration.File[] files = new org.deegree.ogcwebservices.wcs.configuration.File[ell.getLength()];
            for ( int i = 0; i < files.length; i++ ) {
                files[i] = getFile( ell.item( i ) );
            }
            resolution = new FileResolution( min, max, ranges, files );
        } else if ( type.equals( Extension.ORACLEGEORASTER ) ) {
            resolution = getOracleGeoRasterResolution( element, min, max, ranges );

        } else if ( type.equals( Extension.DATABASEINDEXED ) ) {
            resolution = getDatabaseIndexed( element, min, max, ranges );
        } else if ( type.equals( Extension.SCRIPTBASED ) ) {
            resolution = getScriptBased( element, min, max, ranges );
        } else {
            String msg = StringTools.concat( 200, "type: ", type, " not known ", "by the deegree WCS" );
            throw new InvalidParameterValueException( msg );
        }
        return resolution;
    }

    private Resolution getScriptBased( Element element, double min, double max, Range[] ranges )
                            throws XMLParsingException, InvalidParameterValueException {

        XMLFragment xml = new XMLFragment( element );
        try {
            xml.setSystemId( systemId.toExternalForm() );
        } catch ( MalformedURLException e ) {
            // should never happen because systemID is read from a valid URL
        }

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        String xpath = "deegreewcs:Script/deegreewcs:Name";
        String script = XMLTools.getRequiredNodeAsString( element, xpath, nsc );

        xpath = "deegreewcs:Script/deegreewcs:Parameter";
        List<Node> list = XMLTools.getNodes( element, xpath, nsc );
        List<String> parameter = new ArrayList<String>( list.size() );
        for ( Node node : list ) {
            parameter.add( XMLTools.getStringValue( node ) );
        }
        xpath = "deegreewcs:Script/deegreewcs:ResultFormat";
        String resultFormat = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        xpath = "deegreewcs:Script/deegreewcs:StorageLocation";
        String storageLocation = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        try {
            storageLocation = new File( xml.resolve( storageLocation ).getFile() ).getAbsolutePath();
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            throw new InvalidParameterValueException( e.getMessage(), e );
        }
        return new ScriptResolution( min, max, ranges, script, parameter, resultFormat, storageLocation );
    }

    /**
     * creates a <tt>DatabaseResolution</tt> object from the passed element
     * 
     * @param element
     * @param min
     * @param max
     * @param ranges
     * @return the resolution
     * @throws XMLParsingException
     */
    private Resolution getDatabaseIndexed( Element element, double min, double max, Range[] ranges )
                            throws XMLParsingException {

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        String xpath = "deegreewcs:Database/dgjdbc:JDBCConnection";
        Node node = XMLTools.getRequiredNode( element, xpath, nsc );
        IODocument io = new IODocument( (Element) node );
        JDBCConnection jdbc = io.parseJDBCConnection();
        xpath = "deegreewcs:Database/deegreewcs:Table/text()";
        String table = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        xpath = "deegreewcs:Database/deegreewcs:DataRootDirectory/text()";
        String rootDir = XMLTools.getNodeAsString( element, xpath, nsc, "./" );
        File dir = new File( rootDir );
        if ( !dir.isAbsolute() ) {
            XMLFragment xml = new XMLFragment();
            xml.setRootElement( root );
            xml.setSystemId( systemId );
            try {
                URL url = xml.resolve( rootDir );
                dir = new File( url.toURI() );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
        return new DatabaseResolution( min, max, ranges, jdbc, table, dir.getAbsolutePath() );
    }

    /**
     * creates a <tt>OracleGeoRasterResolution</tt> object from the passed element
     * 
     * @param element
     * @param min
     * @param max
     * @param ranges
     * @return the Resolution
     * @throws XMLParsingException
     */
    private Resolution getOracleGeoRasterResolution( Element element, double min, double max, Range[] ranges )
                            throws XMLParsingException {
        Resolution resolution;
        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        String xpath = "deegreewcs:OracleGeoRaster/dgjdbc:JDBCConnection";
        Node node = XMLTools.getRequiredNode( element, xpath, nsc );
        IODocument io = new IODocument( (Element) node );
        JDBCConnection jdbc = io.parseJDBCConnection();
        xpath = "deegreewcs:OracleGeoRaster/deegreewcs:Table/text()";
        String table = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        xpath = "deegreewcs:OracleGeoRaster/deegreewcs:RDTTable/text()";
        String rdtTable = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        xpath = "deegreewcs:OracleGeoRaster/deegreewcs:Column/text()";
        String column = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        xpath = "deegreewcs:OracleGeoRaster/deegreewcs:Identification/text()";
        String identification = XMLTools.getRequiredNodeAsString( element, xpath, nsc );
        xpath = "deegreewcs:OracleGeoRaster/deegreewcs:Level/text()";
        int level = XMLTools.getNodeAsInt( element, xpath, nsc, 1 );
        resolution = new OracleGeoRasterResolution( min, max, ranges, jdbc, table, rdtTable, column, identification,
                                                    level );
        return resolution;
    }

    /**
     * creates a <tt>Shape</tt> object from the passed element
     * 
     * @param element
     * @return created Shape
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private Shape getShape( Element element )
                            throws XMLParsingException, UnknownCRSException {
        String tilePoperty = XMLTools.getRequiredAttrValue( "tileProperty", null, element );
        String directoryProperty = XMLTools.getRequiredAttrValue( "directoryProperty", null, element );
        String srsName = XMLTools.getRequiredAttrValue( "srsName", null, element );
        CoordinateSystem crs = CRSFactory.create( srsName );
        String rootFileName = XMLTools.getStringValue( element );
        rootFileName = rootFileName.trim();
        XMLFragment xml = new XMLFragment();
        xml.setRootElement( root );
        xml.setSystemId( systemId );
        java.io.File file = null;
        try {
            URL url = xml.resolve( rootFileName + ".shp" );
            file = new java.io.File( url.toURI() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
        rootFileName = file.getAbsolutePath();
        rootFileName = rootFileName.substring( 0, rootFileName.lastIndexOf( "." ) );
        return new Shape( crs, rootFileName, tilePoperty, directoryProperty );
    }

    /**
     * creates a <tt>File</tt> object from the passed Element that describes the extensions and locations of the
     * coverages assigned to a <tt>Resolution</tt>
     * 
     * @param element
     * @return <tt>File</tt> object from the passed Element that describes the extensions and locations of the coverages
     *         assigned to a <tt>Resolution</tt>
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private org.deegree.ogcwebservices.wcs.configuration.File getFile( Element element )
                            throws XMLParsingException, InvalidGMLException, UnknownCRSException {
        String name = XMLTools.getRequiredStringValue( "Name", DGRNS, element );
        XMLFragment xml = new XMLFragment();
        xml.setRootElement( element );
        xml.setSystemId( systemId );
        File file = new File( name );
        try {
            URL url = xml.resolve( name );
            file = new java.io.File( url.toURI() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            e.printStackTrace();
        }
        name = file.getAbsolutePath();

        Element elem = XMLTools.getRequiredChildElement( "Envelope", GMLNS, element );
        Envelope envelope = GMLGeometryAdapter.wrapBox( elem, null );
        String srs = XMLTools.getRequiredAttrValue( "srsName", null, elem );

        String[] tmp = StringTools.toArray( srs, "#", false );
        // just a heuristic because it is not guarranteed that the URL
        // in the srsName attribute can be solved
        if ( srs.toLowerCase().indexOf( "epsg" ) > -1 ) {
            srs = "EPSG:" + tmp[1];
        } else {
            srs = "CRS:" + tmp[1];
        }
        if ( tmp[1].equals( "0" ) ) {
            srs = null;
        }

        CoordinateSystem crs = CRSFactory.create( srs );
        return new org.deegree.ogcwebservices.wcs.configuration.File( crs, name, envelope );
    }

    /**
     * creates a <tt>Directory</tt> object from the passed Elememt that describes the extensions and locations of the
     * coverages assigned to a <tt>Resolution</tt>
     * 
     * @param element
     * @return <tt>Directory</tt> object from the passed Elememt that describes the extensions and locations of the
     *         coverages assigned to a <tt>Resolution</tt>
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private Directory getDirectory( Element element )
                            throws XMLParsingException, InvalidGMLException, UnknownCRSException {
        // get valid file extension for this directory
        String temp = XMLTools.getRequiredAttrValue( "extensions", null, element );
        String[] extensions = StringTools.toArray( temp, ",;", true );
        // get the width and height (in pixels) af the tiles in this directory
        temp = XMLTools.getRequiredAttrValue( "tileWidth", null, element );
        double tileWidth = 0;
        try {
            tileWidth = Double.parseDouble( temp );
        } catch ( Exception e ) {
            throw new XMLParsingException( "tileWidth attribute isn't a number" );
        }
        double tileHeight = 0;
        try {
            tileHeight = Double.parseDouble( temp );
        } catch ( Exception e ) {
            throw new XMLParsingException( "tileHeight attribute isn't a number" );
        }
        // get the directroy name
        String name = XMLTools.getRequiredStringValue( "Name", DGRNS, element );
        XMLFragment xml = new XMLFragment();
        xml.setRootElement( element );
        xml.setSystemId( systemId );
        try {
            // resolve name if relative
            name = xml.resolve( name ).toExternalForm();
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "invalid file name/path: " + name );
        }
        // get the bounding envelope of all tiles in the directory
        Element elem = XMLTools.getRequiredChildElement( "Envelope", GMLNS, element );
        Envelope envelope = GMLGeometryAdapter.wrapBox( elem, null );
        String srs = XMLTools.getRequiredAttrValue( "srsName", null, elem );
        if ( srs != null ) {
            String[] tmp = StringTools.toArray( srs, "#", false );
            // just a heuristic because it is not guarranteed that the URL
            // in the srsName attribute can be solved
            if ( srs.toLowerCase().indexOf( "epsg" ) > -1 ) {
                srs = "EPSG:" + tmp[1];
            } else {
                srs = "CRS:" + tmp[1];
            }
            if ( tmp[1].equals( "0" ) )
                srs = null;
        }
        CoordinateSystem crs = CRSFactory.create( srs );
        return new GridDirectory( name, envelope, crs, extensions, tileWidth, tileHeight );
    }

    /**
     * creates an array of <tt>Ranges</tt> from the passed element list
     * 
     * @param el
     * @return created array of <tt>Ranges</tt>
     * @throws XMLParsingException
     */
    private Range[] getRanges( List<Node> el )
                            throws XMLParsingException {
        Range[] ranges = new Range[el.size()];
        for ( int i = 0; i < ranges.length; i++ ) {
            ranges[i] = getRange( (Element) el.get( i ) );
        }
        return ranges;
    }

    /**
     * creates a <tt>Range</tt> object from the passed element
     * 
     * @param element
     * @return created <tt>Range</tt>
     * @throws XMLParsingException
     */
    private Range getRange( Element element )
                            throws XMLParsingException {
        String name = XMLTools.getRequiredStringValue( "Name", DGRNS, element );
        ElementList el = XMLTools.getChildElements( "Axis", DGRNS, element );
        Axis[] axis = getAxis( el );
        return new Range( name, axis );
    }

    /**
     * creates an array of <tt>Axis</tt> objects from the passed element list
     * 
     * @param el
     * @return created array of <tt>Axis</tt>
     * @throws XMLParsingException
     */
    private Axis[] getAxis( ElementList el )
                            throws XMLParsingException {
        Axis[] axis = new Axis[el.getLength()];
        for ( int i = 0; i < axis.length; i++ ) {
            axis[i] = getAxis( el.item( i ) );
        }
        return axis;
    }

    /**
     * creates an <tt>Axis</tt> object from the passed element. The <tt>Interval</tt> included in the <tt>Axis</tt>
     * doesn't have a resolution because it isn't required.
     * 
     * @param element
     * @return created <tt>Axis</tt>
     * @throws XMLParsingException
     */
    private Axis getAxis( Element element )
                            throws XMLParsingException {
        try {
            String name = XMLTools.getRequiredStringValue( "Name", DGRNS, element );
            Element elem = XMLTools.getRequiredChildElement( "Interval", DGRNS, element );
            String tmp = XMLTools.getRequiredStringValue( "min", DGRNS, elem );
            TypedLiteral min = new TypedLiteral( tmp, new URI( "xs:double" ) );
            tmp = XMLTools.getRequiredStringValue( "max", DGRNS, elem );
            TypedLiteral max = new TypedLiteral( tmp, new URI( "xs:double" ) );
            Interval interval = new Interval( min, max, null, null, null );
            return new Axis( name, interval );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( e.getMessage() );
        }
    }

}
