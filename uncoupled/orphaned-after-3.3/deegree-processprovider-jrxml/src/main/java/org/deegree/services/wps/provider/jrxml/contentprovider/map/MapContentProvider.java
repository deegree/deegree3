/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.jrxml.contentprovider.map;

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.JASPERREPORTS_NS;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;
import static org.deegree.services.wps.provider.jrxml.contentprovider.map.RenderUtils.adjustSpan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.MapUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.JrxmlUtils;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.services.wps.provider.jrxml.contentprovider.AbstractJrxmlContentProvider;
import org.deegree.services.wps.provider.jrxml.jaxb.map.AbstractDatasourceType;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Center;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Detail;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Layer;
import org.deegree.services.wps.provider.jrxml.jaxb.map.WFSDatasource;
import org.deegree.services.wps.provider.jrxml.jaxb.map.WMSDatasource;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.PNGEncodeParam;

/**
 * Provides a map in the jrxml
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class MapContentProvider extends AbstractJrxmlContentProvider {

    public MapContentProvider( Workspace workspace ) {
        super( workspace );
    }

    private static final Logger LOG = LoggerFactory.getLogger( MapContentProvider.class );

    final static String SCHEMA = "http://www.deegree.org/processprovider/map";

    final static String MIME_TYPE = "text/xml";

    private static final String PARAM_PREFIX = "map";

    public static final double INCH2M = 0.0254;

    private enum SUFFIXES {

        LEGEND_SUFFIX( "_legend" ), MAP_SUFFIX( "_img" ), SCALE_SUFFIX( "_scale" ), LAYERLIST_SUFFIX( "_layerlist" ), SCALEBAR_SUFFIX(
                                                                                                                                      "_scalebar" );

        private final String text;

        private SUFFIXES( String text ) {
            this.text = text;
        }

    }

    @Override
    public void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescriptions,
                                                 List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {
        // for a wms, parameters starting with 'map' are important. three different types are supported:
        // * wmsXYZ_map -> as image parameter
        // * wmsXYZ_legend -> as imgage parameter
        // * wmsXYZ_layerList -> as frame key
        // where XYZ is a string which is the identifier of the process parameter.
        Map<String, Pair<Integer, Integer>> mapIds = new HashMap<String, Pair<Integer, Integer>>();
        for ( String parameterName : parameters.keySet() ) {
            if ( !handledParameters.contains( parameterName ) ) {
                if ( isMapParameter( parameterName ) ) {
                    String mapId = getIdentifierFromParameter( parameterName );
                    OMElement imgElement = jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                                    new XPath(
                                                                               ".//jasper:image[jasper:imageExpression/text()='$P{"
                                                                                                       + parameterName
                                                                                                       + "}']/jasper:reportElement",
                                                                               JrxmlUtils.nsContext ) );
                    if ( imgElement != null ) {
                        if ( parameterName.endsWith( SUFFIXES.MAP_SUFFIX.text ) ) {

                            if ( !mapIds.containsKey( mapId ) ) {
                                int width = jrxmlAdapter.getRequiredNodeAsInteger( imgElement, new XPath( "@width",
                                                                                                          nsContext ) );
                                int height = jrxmlAdapter.getRequiredNodeAsInteger( imgElement, new XPath( "@height",
                                                                                                           nsContext ) );
                                mapIds.put( mapId, new Pair<Integer, Integer>( width, height ) );
                            }
                        }
                    }
                    if ( !mapIds.containsKey( mapId ) ) {
                        mapIds.put( mapId, null );
                    }
                    // TODO: maybe a status information would be the better way?
                    // remove used parameter
                    handledParameters.add( parameterName );
                }
            }
        }

        for ( String mapId : mapIds.keySet() ) {
            LOG.debug( "Found map component with id " + mapId );
            ComplexInputDefinition comp = new ComplexInputDefinition();
            addInput( comp, parameterDescriptions, mapId, 1, 0 );
            ComplexFormatType format = new ComplexFormatType();
            format.setEncoding( "UTF-8" );
            format.setMimeType( MIME_TYPE );
            format.setSchema( SCHEMA );
            comp.setDefaultFormat( format );
            // TODO: Metadata (width/height)
            // Pair<Integer, Integer> mapDim = mapIds.get( mapId );
            // if ( mapDim != null ) {
            // System.out.println( mapDim.getFirst() );
            // System.out.println( mapDim.getSecond() );
            // try {
            // Metadata meta = new Metadata();
            // MapMetadata mm = new MapMetadata();
            // mm.setWidth( BigInteger.valueOf( mapDim.getFirst().longValue() ) );
            // mm.setHeight( BigInteger.valueOf( mapDim.getSecond().longValue() ) );
            // JAXBContext jc = JAXBContext.newInstance( MapMetadata.class );
            // Marshaller marshaller = jc.createMarshaller();
            // ByteArrayOutputStream os = new ByteArrayOutputStream();
            // XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( os );
            // marshaller.marshal( mm, writer );
            // os.close();
            // writer.close();
            // InputStream is = new ByteArrayInputStream( os.toByteArray() );
            // XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( is );
            // StAXOMBuilder b = new StAXOMBuilder( reader );
            // OMElement elem = b.getDocumentElement();
            // System.out.println( elem );
            // meta.setAny( elem );
            // comp.getMetadata().add( meta );
            // is.close();
            // reader.close();
            // } catch ( Exception e ) {
            // LOG.error( "Could not create map metadata", e );
            // }
            // }
            inputs.add( new JAXBElement<ComplexInputDefinition>( new QName( "ProcessInput" ),
                                                                 ComplexInputDefinition.class, comp ) );
        }

    }

    private String getIdentifierFromParameter( String parameter ) {
        if ( isMapParameter( parameter ) ) {
            for ( SUFFIXES suf : SUFFIXES.values() ) {
                if ( parameter.endsWith( suf.text ) ) {
                    parameter = parameter.substring( PARAM_PREFIX.length(), parameter.length() - suf.text.length() );
                }
            }
        }
        return parameter;
    }

    private String getParameterFromIdentifier( String mapId, SUFFIXES suffix ) {
        return PARAM_PREFIX + mapId + suffix.text;
    }

    private boolean isMapParameter( String imgParameter ) {
        boolean hasSuffix = false;
        for ( SUFFIXES suf : SUFFIXES.values() ) {
            if ( imgParameter.endsWith( suf.text ) ) {
                hasSuffix = true;
            }
        }
        return hasSuffix && imgParameter.startsWith( PARAM_PREFIX );
    }

    @Override
    public Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml,
                                                                          Map<String, Object> params,
                                                                          ProcessletInputs in,
                                                                          List<CodeType> processedIds,
                                                                          Map<String, String> parameters )
                            throws ProcessletException {
        for ( ProcessletInput input : in.getParameters() ) {
            if ( !processedIds.contains( input.getIdentifier() ) && input instanceof ComplexInput ) {
                ComplexInput complexIn = (ComplexInput) input;
                if ( SCHEMA.equals( complexIn.getSchema() ) && MIME_TYPE.equals( complexIn.getMimeType() ) ) {

                    String mapId = complexIn.getIdentifier().getCode();
                    LOG.debug( "Found input parameter " + mapId + " representing a map!" );

                    try {
                        // JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.wps.provider.jrxml.jaxb.map"
                        // );
                        JAXBContext jc = JAXBContext.newInstance( org.deegree.services.wps.provider.jrxml.jaxb.map.Map.class );
                        Unmarshaller unmarshaller = jc.createUnmarshaller();
                        org.deegree.services.wps.provider.jrxml.jaxb.map.Map map = (org.deegree.services.wps.provider.jrxml.jaxb.map.Map) unmarshaller.unmarshal( complexIn.getValueAsXMLStream() );

                        XMLAdapter jrxmlAdapter = new XMLAdapter( jrxml );
                        OMElement root = jrxmlAdapter.getRootElement();

                        List<OrderedDatasource<?>> datasources = anaylizeRequestOrder( map.getDatasources().getWMSDatasourceOrWFSDatasource() );

                        // MAP
                        int resolution = map.getResolution().intValue();
                        String mapKey = getParameterFromIdentifier( mapId, SUFFIXES.MAP_SUFFIX );
                        if ( parameters.containsKey( mapKey ) ) {
                            OMElement mapImgRep = jrxmlAdapter.getElement( root,
                                                                           new XPath(
                                                                                      ".//jasper:image[jasper:imageExpression/text()='$P{"
                                                                                                              + mapKey
                                                                                                              + "}']/jasper:reportElement",
                                                                                      nsContext ) );
                            if ( mapImgRep == null ) {
                                LOG.info( "Could not find image tag in the jrxml for map parameter '" + mapKey + "'" );
                                break;
                            }
                            int originalWidth = jrxmlAdapter.getRequiredNodeAsInteger( mapImgRep, new XPath( "@width",
                                                                                                             nsContext ) );
                            int originalHeight = jrxmlAdapter.getRequiredNodeAsInteger( mapImgRep,
                                                                                        new XPath( "@height", nsContext ) );

                            int width = adjustSpan( originalWidth, resolution );
                            int height = adjustSpan( originalHeight, resolution );

                            Envelope bbox = calculateBBox( map.getDetail(), mapId, width, height, resolution );

                            params.put( mapKey,
                                        prepareMap( datasources, parameters.get( mapKey ), originalWidth,
                                                    originalHeight, width, height, bbox, resolution ) );
                            // SCALE
                            double scale = RenderHelper.calcScaleWMS130( width, height, bbox,
                                                                         bbox.getCoordinateSystem(),
                                                                         MapUtils.DEFAULT_PIXEL_SIZE );
                            String scaleKey = getParameterFromIdentifier( mapId, SUFFIXES.SCALE_SUFFIX );
                            if ( parameters.containsKey( scaleKey ) ) {
                                params.put( scaleKey, convert( scale, parameters.get( scaleKey ) ) );
                            }

                            // SCALEBAR
                            String scalebarKey = getParameterFromIdentifier( mapId, SUFFIXES.SCALEBAR_SUFFIX );
                            if ( parameters.containsKey( scalebarKey ) ) {
                                prepareScaleBar( params, scalebarKey, jrxmlAdapter, datasources,
                                                 parameters.get( scalebarKey ), bbox, width );
                            }
                        }

                        // LAYERLIST
                        prepareLayerlist( getParameterFromIdentifier( mapId, SUFFIXES.LAYERLIST_SUFFIX ), jrxmlAdapter,
                                          map, datasources );

                        // LEGEND
                        String legendKey = getParameterFromIdentifier( mapId, SUFFIXES.LEGEND_SUFFIX );
                        if ( parameters.containsKey( legendKey ) ) {
                            params.put( legendKey,
                                        prepareLegend( legendKey, jrxmlAdapter, datasources,
                                                       parameters.get( legendKey ), resolution ) );
                        }

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            if ( LOG.isTraceEnabled() ) {
                                LOG.trace( "Adjusted jrxml: " + jrxmlAdapter.getRootElement() );
                            }
                            jrxmlAdapter.getRootElement().serialize( bos );
                            jrxml = new ByteArrayInputStream( bos.toByteArray() );
                        } catch ( XMLStreamException e ) {
                            throw new RuntimeException( e );
                        } finally {
                            IOUtils.closeQuietly( bos );
                        }

                    } catch ( JAXBException e ) {
                        String msg = "Could not parse map configuration from parameter '" + complexIn.getIdentifier()
                                     + "': " + e.getMessage();
                        LOG.debug( msg, e );
                        throw new ProcessletException( msg );
                    } catch ( IOException e ) {
                        String msg = "Could not read map configuration from parameter '" + complexIn.getIdentifier()
                                     + "': " + e.getMessage();
                        LOG.debug( msg, e );
                        throw new ProcessletException( msg );
                    } catch ( XMLStreamException e ) {
                        String msg = "Could not read map configuration as xml from parameter '"
                                     + complexIn.getIdentifier() + "': " + e.getMessage();
                        LOG.debug( msg, e );
                        throw new ProcessletException( msg );
                    }
                    processedIds.add( input.getIdentifier() );
                }
            }
        }
        return new Pair<InputStream, Boolean>( jrxml, false );
    }

    private Envelope calculateBBox( Detail detail, String id, int mapWidth, int mapHeight, int dpi )
                            throws ProcessletException {
        ICRS crs = CRSManager.getCRSRef( detail.getCrs() );
        Center center = detail.getCenter();
        Envelope bbox = null;
        if ( center != null ) {
            int scaleDenominator = center.getScaleDenominator().intValue();
            String[] coords = center.getValue().split( "," );
            double pixelSize = INCH2M / dpi;
            double w2 = ( scaleDenominator * pixelSize * mapWidth ) / 2d;
            double x1 = Double.parseDouble( coords[0] ) - w2;
            double x2 = Double.parseDouble( coords[0] ) + w2;
            w2 = ( scaleDenominator * pixelSize * mapHeight ) / 2d;
            double y1 = Double.parseDouble( coords[1] ) - w2;
            double y2 = Double.parseDouble( coords[1] ) + w2;
            bbox = new DefaultEnvelope( null, crs, null, new DefaultPoint( null, crs, null, new double[] { x1, y1 } ),
                                        new DefaultPoint( null, crs, null, new double[] { x2, y2 } ) );
        } else if ( detail.getBbox() != null ) {
            String[] coords = detail.getBbox().split( "," );
            double bboxXmin = Double.parseDouble( coords[0] );
            double bboxYmin = Double.parseDouble( coords[1] );
            double bboxXmax = Double.parseDouble( coords[2] );
            double bboxYmax = Double.parseDouble( coords[3] );
            double bboxWidth = bboxXmax - bboxXmin;
            double bboxHeight = bboxYmax - bboxYmin;
            double bboxAspectRatio = bboxWidth / bboxHeight;
            double printAspectRatio = ( (double) mapWidth ) / mapHeight;
            if ( bboxAspectRatio > printAspectRatio ) {
                double centerY = bboxYmin + ( ( bboxYmax - bboxYmin ) / 2d );
                double height = bboxWidth * ( 1.0 / printAspectRatio );
                double minY = centerY - ( height / 2d );
                double maxY = centerY + ( height / 2d );
                bbox = new DefaultEnvelope( null, crs, null, new DefaultPoint( null, crs, null, new double[] {
                                                                                                              bboxXmin,
                                                                                                              minY } ),
                                            new DefaultPoint( null, crs, null, new double[] { bboxXmax, maxY } ) );
            } else {
                double centerX = bboxXmin + ( ( bboxXmax - bboxXmin ) / 2d );
                double width = bboxHeight * printAspectRatio;
                double minX = centerX - ( width / 2d );
                double maxX = centerX + ( width / 2d );
                bbox = new DefaultEnvelope( null, crs, null, new DefaultPoint( null, crs, null,
                                                                               new double[] { minX, bboxYmin } ),
                                            new DefaultPoint( null, crs, null, new double[] { maxX, bboxYmax } ) );
            }
        } else {
            throw new ProcessletException(
                                           "Could not parse required parameter bbox or center for map component with id  '"
                                                                   + id + "'." );
        }
        LOG.debug( "requested and adjusted bbox: {}", bbox );
        return bbox;
    }

    private void prepareScaleBar( Map<String, Object> params, String scalebarKey, XMLAdapter jrxmlAdapter,
                                  List<OrderedDatasource<?>> datasources, String type, Envelope bbox, double mapWidth )
                            throws ProcessletException {

        OMElement sbRep = jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                   new XPath( ".//jasper:image[jasper:imageExpression/text()='$P{"
                                                              + scalebarKey + "}']/jasper:reportElement", nsContext ) );

        if ( sbRep != null ) {
            // TODO: rework this!
            LOG.debug( "Found scalebar with key '" + scalebarKey + "'." );
            int w = jrxmlAdapter.getRequiredNodeAsInteger( sbRep, new XPath( "@width", nsContext ) );
            int h = jrxmlAdapter.getRequiredNodeAsInteger( sbRep, new XPath( "@height", nsContext ) );
            BufferedImage img = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g = img.createGraphics();

            String fontName = null;
            int fontSize = 8;
            int desiredWidth = w - 30;
            // calculate scale bar max scale and size
            int length = 0;
            double lx = 0;
            double scale = 0;
            for ( int i = 0; i < 100; i++ ) {
                double k = 0;
                double dec = 30 * Math.pow( 10, i );
                for ( int j = 0; j < 9; j++ ) {
                    k += dec;
                    double tx = -k * ( mapWidth / bbox.getSpan0() );
                    if ( Math.abs( tx - lx ) < desiredWidth ) {
                        length = (int) Math.round( Math.abs( tx - lx ) );
                        scale = k;
                    } else {
                        break;
                    }
                }
            }
            // draw scale bar base line
            g.setStroke( new BasicStroke( ( desiredWidth + 30 ) / 250 ) );
            g.setColor( Color.black );
            g.drawLine( 10, 30, length + 10, 30 );
            double dx = length / 3d;
            double vdx = scale / 3;
            double div = 1;
            String uom = "m";
            if ( scale > 1000 ) {
                div = 1000;
                uom = "km";
            }
            // draw scale bar scales
            if ( fontName == null ) {
                fontName = "SANS SERIF";
            }
            g.setFont( new Font( fontName, Font.PLAIN, fontSize ) );
            DecimalFormat df = new DecimalFormat( "##.# " );
            DecimalFormat dfWithUom = new DecimalFormat( "##.# " + uom );
            for ( int i = 0; i < 4; i++ ) {
                String label = i < 3 ? df.format( ( vdx * i ) / div ) : dfWithUom.format( ( vdx * i ) / div );
                g.drawString( label, (int) Math.round( 10 + i * dx ) - 8, 10 );
                g.drawLine( (int) Math.round( 10 + i * dx ), 30, (int) Math.round( 10 + i * dx ), 20 );
            }
            for ( int i = 0; i < 7; i++ ) {
                g.drawLine( (int) Math.round( 10 + i * dx / 2d ), 30, (int) Math.round( 10 + i * dx / 2d ), 25 );
            }
            g.dispose();
            params.put( scalebarKey, convertImageToReportFormat( type, img ) );
            LOG.debug( "added scalebar" );
        }
    }

    private Object convert( double scale, String parameterType ) {
        if ( parameterType == null || "java.lang.String".equals( parameterType ) ) {
            return Double.toString( scale );
        } else if ( "java.lang.Integer".equals( parameterType ) ) {
            return new Double( scale ).intValue();
        } else if ( "java.lang.Float".equals( parameterType ) ) {
            return new Double( scale ).floatValue();
        } else if ( "java.lang.Long".equals( parameterType ) ) {
            return new Double( scale ).longValue();
        } else if ( "java.math.BigDecimal".equals( parameterType ) ) {
            return new BigDecimal( scale );
        } else if ( "java.lang.Short".equals( parameterType ) ) {
            return new Double( scale ).shortValue();
        } else if ( "java.lang.Byte".equals( parameterType ) ) {
            return new Double( scale ).byteValue();
        }
        return scale;
    }

    private Object prepareLegend( String legendKey, XMLAdapter jrxmlAdapter, List<OrderedDatasource<?>> datasources,
                                  String type, int resolution )
                            throws ProcessletException {

        if ( "net.sf.jasperreports.engine.JRRenderable".equals( type ) ) {
            return new LegendRenderable( datasources, resolution );
        } else {
            OMElement legendRE = jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                          new XPath(
                                                                     ".//jasper:image[jasper:imageExpression/text()='$P{"
                                                                                             + legendKey
                                                                                             + "}']/jasper:reportElement",
                                                                     nsContext ) );

            if ( legendRE != null ) {
                LOG.debug( "Found legend with key '" + legendKey + "'." );
                int width = jrxmlAdapter.getRequiredNodeAsInteger( legendRE, new XPath( "@width", nsContext ) );
                int height = jrxmlAdapter.getRequiredNodeAsInteger( legendRE, new XPath( "@height", nsContext ) );
                width = adjustSpan( width, resolution );
                height = adjustSpan( height, resolution );

                BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
                Graphics2D g = bi.createGraphics();
                // TODO: bgcolor?
                Color bg = Color.decode( "0xFFFFFF" );
                g.setColor( bg );
                g.fillRect( 0, 0, width, height );
                g.setColor( Color.BLACK );
                int k = 0;

                for ( int i = 0; i < datasources.size(); i++ ) {
                    if ( k > height ) {
                        LOG.warn( "The necessary legend size is larger than the available legend space." );
                    }
                    List<Pair<String, BufferedImage>> legends = datasources.get( i ).getLegends( width );
                    for ( Pair<String, BufferedImage> legend : legends ) {
                        BufferedImage img = legend.second;
                        if ( img != null ) {
                            if ( img.getWidth( null ) < 50 ) {
                                // it is assumed that no label is assigned
                                g.drawImage( img, 0, k, null );
                                g.drawString( legend.first, img.getWidth( null ) + 10, k + img.getHeight( null ) / 2 );
                            } else {
                                g.drawImage( img, 0, k, null );
                            }
                            k = k + img.getHeight( null ) + 10;
                        } else {
                            g.drawString( "- " + legend.first, 0, k + 10 );
                            k = k + 20;
                        }
                    }
                }
                g.dispose();
                return convertImageToReportFormat( type, bi );
            }
        }
        return null;
    }

    private void prepareLayerlist( String layerListKey, XMLAdapter jrxmlAdapter,
                                   org.deegree.services.wps.provider.jrxml.jaxb.map.Map map,
                                   List<OrderedDatasource<?>> datasources ) {

        OMElement layerListFrame = jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                            new XPath(
                                                                       ".//jasper:band/jasper:frame[jasper:reportElement/@key='"
                                                                                               + layerListKey + "']",
                                                                       nsContext ) );
        if ( layerListFrame != null ) {
            LOG.debug( "Found layer list with key '{}' to adjust.", layerListKey );
            List<OMElement> elements = jrxmlAdapter.getElements( layerListFrame, new XPath( "jasper:staticText",
                                                                                            nsContext ) );
            if ( elements.size() > 1 ) {
                OMElement grpTemplate = elements.get( 0 );
                OMElement fieldTemplate = elements.get( 1 );
                for ( OMElement element : elements ) {
                    element.detach();
                }
                XMLAdapter grpAdapter = new XMLAdapter( grpTemplate );
                int grpHeight = grpAdapter.getNodeAsInt( grpTemplate, new XPath( "jasper:reportElement/@height",
                                                                                 nsContext ), 15 );
                int grpY = grpAdapter.getNodeAsInt( grpTemplate, new XPath( "jasper:reportElement/@y", nsContext ), 0 );

                XMLAdapter fieldAdapter = new XMLAdapter( fieldTemplate );
                int fieldHeight = fieldAdapter.getNodeAsInt( fieldTemplate, new XPath( "jasper:reportElement/@height",
                                                                                       nsContext ), 15 );
                OMFactory factory = OMAbstractFactory.getOMFactory();
                // y + height * index
                int y = grpY;
                for ( OrderedDatasource<?> datasource : datasources ) {
                    for ( String datasourceKey : datasource.getLayerList().keySet() ) {
                        OMElement newGrp = createLayerEntry( grpTemplate, y, factory, datasourceKey );
                        layerListFrame.addChild( newGrp );
                        y += grpHeight;
                        for ( String layerName : datasource.getLayerList().get( datasourceKey ) ) {
                            OMElement newField = createLayerEntry( fieldTemplate, y, factory, layerName );
                            layerListFrame.addChild( newField );
                            y += fieldHeight;
                        }
                    }
                }
            } else {
                LOG.info( "layerlist frame with key '{}' must have at least two child elements", layerListKey );
            }
        } else {
            LOG.debug( "no layer list with key '{}' found.", layerListKey );
        }
    }

    private OMElement createLayerEntry( OMElement template, int y, OMFactory factory, String text ) {
        OMElement newGrp = template.cloneOMElement();
        OMElement e = newGrp.getFirstChildWithName( new QName( JASPERREPORTS_NS, "reportElement" ) );
        e.addAttribute( "y", Integer.toString( y ), null );
        e = newGrp.getFirstChildWithName( new QName( JASPERREPORTS_NS, "text" ) );
        // this does not work:
        // e.setText( layer );
        // it attaches the text, but does not replace
        e.getFirstOMChild().detach();
        e.addChild( factory.createOMText( e, text ) );
        return newGrp;
    }

    private Object prepareMap( List<OrderedDatasource<?>> datasources, String type, int originalWidth,
                               int originalHeight, int width, int height, Envelope bbox, int resolution )
                            throws ProcessletException {
        if ( "net.sf.jasperreports.engine.JRRenderable".equals( type ) ) {
            return new MapRenderable( datasources, bbox, resolution );
        } else {
            BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
            Graphics g = bi.getGraphics();
            for ( OrderedDatasource<?> datasource : datasources ) {
                BufferedImage image = datasource.getImage( width, height, bbox );
                if ( image != null ) {
                    g.drawImage( image, 0, 0, null );
                }
            }
            g.dispose();
            return convertImageToReportFormat( type, bi );
        }
    }

    private Object convertImageToReportFormat( String type, BufferedImage bi )
                            throws ProcessletException {
        if ( type != null && type.equals( "java.io.File" ) ) {
            return writeImage( bi );
        } else if ( type != null && type.equals( "java.net.URL" ) ) {
            try {
                return writeImage( bi ).toURI().toURL();
            } catch ( MalformedURLException e ) {
                LOG.error( "Could not create url: ", e );
            }
        } else if ( type != null && type.equals( "java.io.InputStream" ) ) {
            try {
                return new FileInputStream( writeImage( bi ) );
            } catch ( FileNotFoundException e ) {
                LOG.error( "Could not open stream: ", e );
            }
        } else if ( type != null && type.equals( "java.awt.Image" ) ) {
            return bi;
        }

        return writeImage( bi ).toString();
    }

    List<OrderedDatasource<?>> anaylizeRequestOrder( List<AbstractDatasourceType> datasources ) {

        List<OrderedDatasource<?>> orderedDatasources = new ArrayList<OrderedDatasource<?>>();
        for ( AbstractDatasourceType datasource : datasources ) {

            if ( datasource instanceof WMSDatasource ) {
                WMSDatasource wmsDatasource = (WMSDatasource) datasource;
                List<Layer> layers = wmsDatasource.getLayers().getLayer();
                int index = 0;
                for ( Layer layer : layers ) {
                    BigInteger position = layer.getPosition();
                    if ( position != null ) {
                        int intPos = position.intValue();

                        WMSOrderedDatasource contains = contains( orderedDatasources, wmsDatasource, intPos );
                        // splitten! wenn position zwischen zwei
                        if ( contains != null ) {
                            List<Layer> orderedLayers = contains.layers;
                            int i = 0;
                            for ( Layer orderedLayer : orderedLayers ) {
                                if ( orderedLayer.getPosition().intValue() < intPos ) {
                                    i++;
                                }
                            }
                            contains.layers.add( i, layer );
                            contains.min = contains.min < intPos ? contains.min : intPos;
                            contains.max = contains.max > intPos ? contains.max : intPos;
                        } else {
                            List<Layer> newLayerList = new ArrayList<Layer>();
                            newLayerList.add( layer );
                            int indexToAdd = getIndexToAdd( orderedDatasources, intPos );
                            orderedDatasources.add( indexToAdd, new WMSOrderedDatasource( wmsDatasource, newLayerList,
                                                                                          intPos, intPos ) );
                        }
                    } else {
                        WMSOrderedDatasource contains = contains( orderedDatasources, wmsDatasource );
                        if ( contains != null ) {
                            contains.layers.add( layer );
                        } else {
                            List<Layer> newLayerList = new ArrayList<Layer>();
                            newLayerList.add( layer );
                            orderedDatasources.add( new WMSOrderedDatasource( wmsDatasource, newLayerList ) );
                        }
                    }
                    index++;
                }
            } else if ( datasource instanceof WFSDatasource ) {
                WFSDatasource wfsDatasource = (WFSDatasource) datasource;
                WFSOrderedDatasource newDatasource = new WFSOrderedDatasource( wfsDatasource );
                if ( wfsDatasource.getPosition() != null ) {
                    int indexToAdd = getIndexToAdd( orderedDatasources, wfsDatasource.getPosition().intValue() );
                    orderedDatasources.add( indexToAdd, newDatasource );
                } else {
                    orderedDatasources.add( newDatasource );
                }

            }
        }
        return orderedDatasources;
    }

    private int getIndexToAdd( List<OrderedDatasource<?>> orderedDatasources, int intPos ) {
        int indexToAdd = 0;
        boolean isBetween = false;
        for ( OrderedDatasource<?> orderedDatasource : orderedDatasources ) {
            if ( orderedDatasource.min > 0 && intPos > orderedDatasource.min && intPos < orderedDatasource.max ) {
                indexToAdd++;
                isBetween = true;
                break;
            } else if ( orderedDatasource.min > 0 && intPos > orderedDatasource.max ) {
                indexToAdd++;
            } else {
                break;
            }
        }
        if ( isBetween ) {
            OrderedDatasource<?> dsToSplit = orderedDatasources.get( indexToAdd - 1 );
            if ( dsToSplit instanceof WMSOrderedDatasource ) {
                List<Layer> newLayerListFromSplit = new ArrayList<Layer>();
                int newMinPos = 0;
                int newMaxPos = 0;
                for ( Layer l : ( (WMSOrderedDatasource) dsToSplit ).layers ) {
                    if ( l.getPosition() != null && l.getPosition().intValue() > intPos ) {
                        newLayerListFromSplit.add( l );
                        newMinPos = Math.min( l.getPosition().intValue(), newMinPos );
                        newMaxPos = Math.max( l.getPosition().intValue(), newMaxPos );
                    }
                }
                for ( Layer lToRemove : newLayerListFromSplit ) {
                    ( (WMSOrderedDatasource) dsToSplit ).layers.remove( lToRemove );
                }
                orderedDatasources.add( indexToAdd, new WMSOrderedDatasource( (WMSDatasource) dsToSplit.datasource,
                                                                              newLayerListFromSplit, intPos, intPos ) );
            }
        }
        return indexToAdd;
    }

    private WMSOrderedDatasource contains( List<OrderedDatasource<?>> orderedDatasources, WMSDatasource datasource,
                                           int index ) {
        int i = 0;
        for ( OrderedDatasource<?> orderedDatasource : orderedDatasources ) {
            if ( orderedDatasource.datasource.equals( datasource ) ) {
                WMSOrderedDatasource wmsOrderedDatasource = (WMSOrderedDatasource) orderedDatasource;
                int maxBefore = Integer.MIN_VALUE;
                int minNext = Integer.MIN_VALUE;
                if ( i - 1 > 0 && i - 1 < orderedDatasources.size() ) {
                    maxBefore = orderedDatasources.get( i - 1 ).max;
                }
                if ( i + 1 < orderedDatasources.size() ) {
                    minNext = orderedDatasources.get( i + 1 ).min;
                }
                if ( index > maxBefore && ( minNext < 0 || index < minNext ) ) {
                    return wmsOrderedDatasource;
                }
            }
            i++;
        }
        return null;
    }

    private WMSOrderedDatasource contains( List<OrderedDatasource<?>> orderedDatasources, WMSDatasource datasource ) {
        for ( OrderedDatasource<?> orderedDatasource : orderedDatasources ) {
            if ( orderedDatasource.datasource.equals( datasource ) ) {
                return (WMSOrderedDatasource) orderedDatasource;
            }
        }
        return null;
    }

    private File writeImage( BufferedImage bi )
                            throws ProcessletException {
        FileOutputStream fos = null;
        try {
            File f = File.createTempFile( "image", ".png" );
            fos = new FileOutputStream( f );

            PNGEncodeParam encodeParam = PNGEncodeParam.getDefaultEncodeParam( bi );

            if ( encodeParam instanceof PNGEncodeParam.Palette ) {
                PNGEncodeParam.Palette p = (PNGEncodeParam.Palette) encodeParam;
                byte[] b = new byte[] { -127 };
                p.setPaletteTransparency( b );
            }
            com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder( "PNG", fos, encodeParam );
            encoder.encode( bi.getData(), bi.getColorModel() );
            LOG.debug( "Wrote image to file: {}", f.toString() );
            return f;
        } catch ( IOException e ) {
            String msg = "Could not write image to file: " + e.getMessage();
            LOG.debug( msg, e );
            throw new ProcessletException( msg );
        } finally {
            IOUtils.closeQuietly( fos );
        }
    }

}
