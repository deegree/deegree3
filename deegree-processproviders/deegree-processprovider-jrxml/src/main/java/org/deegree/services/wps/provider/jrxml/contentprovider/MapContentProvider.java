//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.JASPERREPORTS_NS;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;

import java.awt.Color;
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
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.protocol.wfs.client.WFSClient;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.remoteows.wms.WMSClient111;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.JrxmlUtils;
import org.deegree.services.wps.provider.jrxml.jaxb.map.AbstractDatasourceType;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Layer;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Style;
import org.deegree.services.wps.provider.jrxml.jaxb.map.WFSDatasource;
import org.deegree.services.wps.provider.jrxml.jaxb.map.WMSDatasource;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.styling.Styling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;

/**
 * Provides a map in the jrxml
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MapContentProvider implements JrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( MapContentProvider.class );

    final static String SCHEMA = "http://www.deegree.org/processprovider/map";

    final static String MIME_TYPE = "text/xml";

    private static final String PARAM_PREFIX = "map";

    private enum SUFFIXES {

        LEGEND_SUFFIX( "_legend" ), MAP_SUFFIX( "_img" ), SCALE_SUFFIX( "_scale" ), LAYERLIST_SUFFIX( "_layerlist" ), SCALEBAR_SUFFIX(
                                                                                                                                      "_scalebar" );

        private final String text;

        private SUFFIXES( String text ) {
            this.text = text;
        }

        private String getText() {
            return text;
        }

    }

    @Override
    public void inspectInputParametersFromJrxml( List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {
        // for a wms, parameters starting with 'map' are important. three different types are supported:
        // * wmsXYZ_map -> as image parameter
        // * wmsXYZ_legend -> as imgage parameter
        // * wmsXYZ_layerList -> as frame key
        // where XYZ is a string which is the identifier of the process parameter.
        List<String> mapIds = new ArrayList<String>();
        for ( String parameterName : parameters.keySet() ) {
            if ( !handledParameters.contains( parameterName ) ) {
                if ( jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                              new XPath( ".//jasper:image/jasper:imageExpression[text()='$P{"
                                                         + parameterName + "}']", JrxmlUtils.nsContext ) ) != null
                     || jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                 new XPath(
                                                            ".//jasper:textField/jasper:textFieldExpression[text()='$P{"
                                                                                    + parameterName + "}']",
                                                            JrxmlUtils.nsContext ) ) != null ) {
                    if ( isMapParameter( parameterName ) ) {
                        String mapId = getIdentifierFromParameter( parameterName );
                        if ( !mapIds.contains( mapId ) ) {
                            mapIds.add( mapId );
                        }
                        // TODO: maybe a status information would be the better way?
                        // remove used parameter
                        handledParameters.add( parameterName );
                    }
                }
            }
        }

        for ( String mapId : mapIds ) {
            LOG.debug( "Found map component with id " + mapId );
            ComplexInputDefinition comp = new ComplexInputDefinition();
            comp.setTitle( getAsLanguageStringType( mapId ) );
            comp.setIdentifier( getAsCodeType( mapId ) );
            ComplexFormatType format = new ComplexFormatType();
            format.setEncoding( "UTF-8" );
            format.setMimeType( MIME_TYPE );
            format.setSchema( SCHEMA );
            comp.setDefaultFormat( format );
            comp.setMaxOccurs( BigInteger.valueOf( 1 ) );
            comp.setMinOccurs( BigInteger.valueOf( 0 ) );
            inputs.add( new JAXBElement<ComplexInputDefinition>( new QName( "ProcessInput" ),
                                                                 ComplexInputDefinition.class, comp ) );
        }

    }

    private String getIdentifierFromParameter( String parameter ) {
        if ( isMapParameter( parameter ) ) {
            for ( SUFFIXES suf : SUFFIXES.values() ) {
                if ( parameter.endsWith( suf.getText() ) ) {
                    parameter = parameter.substring( PARAM_PREFIX.length(), parameter.length() - suf.getText().length() );
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
            if ( imgParameter.endsWith( suf.getText() ) ) {
                hasSuffix = true;
            }
        }
        return hasSuffix && imgParameter.startsWith( PARAM_PREFIX );
    }

    @Override
    public InputStream prepareJrxmlAndReadInputParameters( InputStream jrxml, Map<String, Object> params,
                                                           ProcessletInputs in, List<CodeType> processedIds,
                                                           Map<String, String> parameters )
                            throws ProcessletException {
        for ( ProcessletInput input : in.getParameters() ) {
            if ( !processedIds.contains( input ) && input instanceof ComplexInput ) {
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
                            int width = jrxmlAdapter.getRequiredNodeAsInteger( mapImgRep, new XPath( "@width",
                                                                                                     nsContext ) );
                            int height = jrxmlAdapter.getRequiredNodeAsInteger( mapImgRep, new XPath( "@height",
                                                                                                      nsContext ) );

                            String[] coords = map.getDetail().getBbox().split( "," );
                            ICRS crs = CRSManager.getCRSRef( map.getDetail().getCrs() );
                            Envelope bbox = new DefaultEnvelope(
                                                                 null,
                                                                 crs,
                                                                 null,
                                                                 new DefaultPoint(
                                                                                   null,
                                                                                   crs,
                                                                                   null,
                                                                                   new double[] {
                                                                                                 Double.parseDouble( coords[0] ),
                                                                                                 Double.parseDouble( coords[1] ) } ),
                                                                 new DefaultPoint(
                                                                                   null,
                                                                                   crs,
                                                                                   null,
                                                                                   new double[] {
                                                                                                 Double.parseDouble( coords[2] ),
                                                                                                 Double.parseDouble( coords[3] ) } ) );

                            params.put( mapKey,
                                        prepareMap( datasources, parameters.get( mapKey ), width, height, bbox, crs ) );

                            // SCALE
                            double scale = Utils.calcScaleWMS130( width, height, bbox, crs );
                            String scaleKey = getParameterFromIdentifier( mapId, SUFFIXES.SCALE_SUFFIX );
                            if ( parameters.containsKey( scaleKey ) ) {
                                params.put( scaleKey, convert( scale, parameters.get( scaleKey ) ) );
                            }

                            // SCALEBAR
                            String scalebarKey = getParameterFromIdentifier( mapId, SUFFIXES.SCALEBAR_SUFFIX );
                            if ( parameters.containsKey( scalebarKey ) ) {
                                prepareScaleBar( scalebarKey, jrxmlAdapter, datasources, parameters.get( scalebarKey ),
                                                 scale );
                            }
                        }

                        // LAYERLIST
                        prepareLayerlist( getParameterFromIdentifier( mapId, SUFFIXES.LAYERLIST_SUFFIX ), jrxmlAdapter,
                                          map, datasources );

                        // LEGEND
                        String legendKey = getParameterFromIdentifier( mapId, SUFFIXES.LEGEND_SUFFIX );
                        if ( parameters.containsKey( legendKey ) ) {
                            params.put( legendKey,
                                        prepareLegend( legendKey, jrxmlAdapter, datasources, parameters.get( legendKey ) ) );
                        }

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            if ( LOG.isDebugEnabled() ) {
                                LOG.debug( "Adjusted jrxml: " + jrxmlAdapter.getRootElement() );
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
        return jrxml;
    }

    private void prepareScaleBar( String scalebarKey, XMLAdapter jrxmlAdapter, List<OrderedDatasource<?>> datasources,
                                  String string, double scale ) {

        OMElement sbRep = jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                   new XPath( ".//jasper:image[jasper:imageExpression/text()='$P{"
                                                              + scalebarKey + "}']/jasper:reportElement", nsContext ) );

        if ( sbRep != null ) {
            // int w = jrxmlAdapter.getRequiredNodeAsInteger( sbRep, new XPath( "@width", nsContext ) );
            // int h = jrxmlAdapter.getRequiredNodeAsInteger( sbRep, new XPath( "@height", nsContext ) );
            // BufferedImage img = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
            // Graphics2D g = img.createGraphics();
            // g.dispose();
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
                                  String type )
                            throws ProcessletException {

        OMElement legendRE = jrxmlAdapter.getElement( jrxmlAdapter.getRootElement(),
                                                      new XPath( ".//jasper:image[jasper:imageExpression/text()='$P{"
                                                                 + legendKey + "}']/jasper:reportElement", nsContext ) );

        if ( legendRE != null ) {
            LOG.debug( "Found legend with key '" + legendKey + "'." );
            int width = jrxmlAdapter.getRequiredNodeAsInteger( legendRE, new XPath( "@width", nsContext ) );
            int height = jrxmlAdapter.getRequiredNodeAsInteger( legendRE, new XPath( "@height", nsContext ) );
            // TODO: bgcolor?
            Color bg = Color.decode( "0xFFFFFF" );
            BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
            Graphics g = bi.getGraphics();
            g.setColor( bg );
            g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
            g.setColor( Color.BLACK );
            int k = 0;

            for ( int i = 0; i < datasources.size(); i++ ) {
                if ( k > bi.getHeight() ) {
                    LOG.warn( "The necessary legend size is larger than the available legend space." );
                }
                // TODO: height must be
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

    private Object prepareMap( List<OrderedDatasource<?>> datasources, String type, int width, int height,
                               Envelope bbox, ICRS crs )
                            throws ProcessletException {
        BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
        Graphics g = bi.getGraphics();

        for ( OrderedDatasource<?> datasource : datasources ) {
            BufferedImage image = datasource.getImage( width, height, bbox, crs );
            if ( image != null ) {
                g.drawImage( image, 0, 0, null );
            }
        }
        g.dispose();

        return convertImageToReportFormat( type, bi );
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

    abstract class OrderedDatasource<T extends AbstractDatasourceType> {
        // OrderedDatasource
        // abstract String getRequest( int width, int height, String bbox, String crs );

        int min = Integer.MIN_VALUE;

        int max = Integer.MIN_VALUE;

        final T datasource;

        public OrderedDatasource( T datasource ) {
            this.datasource = datasource;
        }

        public abstract List<Pair<String, BufferedImage>> getLegends( int width )
                                throws ProcessletException;

        public OrderedDatasource( T datasource, int min, int max ) {
            this.datasource = datasource;
            this.min = min;
            this.max = max;
        }

        abstract BufferedImage getImage( int width, int height, Envelope bbox, ICRS crs )
                                throws ProcessletException;

        abstract Map<String, List<String>> getLayerList();

        protected org.deegree.style.se.unevaluated.Style getStyle( Style dsStyle )
                                throws MalformedURLException, FactoryConfigurationError, XMLStreamException,
                                IOException {
            XMLStreamReader reader = null;
            if ( dsStyle == null || dsStyle.getNamedStyle() != null ) {
                return null;
            } else if ( dsStyle.getExternalStyle() != null ) {
                URL ex = new URL( dsStyle.getExternalStyle() );
                XMLInputFactory fac = XMLInputFactory.newInstance();
                reader = fac.createXMLStreamReader( ex.openStream() );
            } else if ( dsStyle.getEmbeddedStyle() != null ) {
                XMLInputFactory fac = XMLInputFactory.newInstance();
                reader = fac.createXMLStreamReader( new DOMSource( dsStyle.getEmbeddedStyle() ) );
                nextElement( reader );
                nextElement( reader );
            }

            SymbologyParser symbologyParser = SymbologyParser.INSTANCE;
            return symbologyParser.parse( reader );
        }

        protected BufferedImage getLegendImg( org.deegree.style.se.unevaluated.Style style, int width ) {
            Legends legends = new Legends();
            Pair<Integer, Integer> legendSize = legends.getLegendSize( style );
            if ( legendSize.first < width )
                width = legendSize.first;
            BufferedImage img = new BufferedImage( width, legendSize.second, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g = img.createGraphics();
            g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
            g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON );
            legends.paintLegend( style, width, legendSize.second, g );
            g.dispose();
            return img;
        }
    }

    class WMSOrderedDatasource extends OrderedDatasource<WMSDatasource> {

        List<Layer> layers;

        public WMSOrderedDatasource( WMSDatasource datasource, List<Layer> layers ) {
            super( datasource );
            this.layers = layers;
        }

        public WMSOrderedDatasource( WMSDatasource datasource, List<Layer> layers, int min, int max ) {
            super( datasource, min, max );
            this.layers = layers;
        }

        @Override
        BufferedImage getImage( int width, int height, Envelope bbox, ICRS crs )
                                throws ProcessletException {
            try {
                String user = datasource.getAuthentification() != null ? datasource.getAuthentification().getUser()
                                                                      : null;
                String passw = datasource.getAuthentification() != null ? datasource.getAuthentification().getPassword()
                                                                       : null;
                // TODO: version
                if ( !"1.1.1".equals( datasource.getVersion() ) ) {
                    throw new ProcessletException( "WMS version " + datasource.getVersion()
                                                   + " is not yet supported. Supported values are: 1.1.1" );
                }
                String capUrl = datasource.getUrl() + "?request=GetCapabilities&service=WMS&version=1.1.1";
                WMSClient111 wmsClient = new WMSClient111( new URL( capUrl ), 5, 60, user, passw );
                List<String> layerNames = new ArrayList<String>();
                for ( Layer l : layers ) {
                    layerNames.add( l.getName() );
                }

                // TODO: styles!
                GetMap gm = new GetMap( layerNames, width, height, bbox, crs, "image/png", true );
                Pair<BufferedImage, String> map = wmsClient.getMap( gm, null, 60, false );
                if ( map.first == null )
                    LOG.debug( map.second );
                return map.first;
            } catch ( MalformedURLException e ) {
                String msg = "could not reslove wms url " + datasource.getUrl() + "!";
                LOG.error( msg, e );
                throw new ProcessletException( msg );
            } catch ( IOException e ) {
                String msg = "could not get map!";
                LOG.error( msg, e );
                throw new ProcessletException( msg );
            }
        }

        private BufferedImage loadImage( String request )
                                throws IOException {
            LOG.debug( "try to load image from request " + request );
            InputStream is = null;
            SeekableStream fss = null;
            try {
                URLConnection conn = new URL( request ).openConnection();
                is = conn.getInputStream();

                if ( LOG.isDebugEnabled() ) {
                    File logFile = File.createTempFile( "loadedImg", "response" );
                    OutputStream logStream = new FileOutputStream( logFile );

                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ( ( read = is.read( buffer ) ) != -1 ) {
                        logStream.write( buffer, 0, read );
                    }
                    logStream.close();

                    is = new FileInputStream( logFile );
                    LOG.debug( "response can be found at " + logFile );
                }

                try {
                    fss = new MemoryCacheSeekableStream( is );
                    RenderedOp ro = JAI.create( "stream", fss );
                    return ro.getAsBufferedImage();
                } catch ( Exception e ) {
                    LOG.info( "could not load image!" );
                    return null;
                }
            } finally {
                IOUtils.closeQuietly( fss );
                IOUtils.closeQuietly( is );
            }
        }

        @Override
        Map<String, List<String>> getLayerList() {
            HashMap<String, List<String>> layerList = new HashMap<String, List<String>>();
            ArrayList<String> layerNames = new ArrayList<String>( layers.size() );
            for ( Layer layer : layers ) {
                layerNames.add( layer.getTitle() != null ? layer.getTitle() : layer.getName() );
            }
            layerList.put( datasource.getName(), layerNames );
            return layerList;
        }

        @Override
        public List<Pair<String, BufferedImage>> getLegends( int width )
                                throws ProcessletException {
            List<Pair<String, BufferedImage>> legends = new ArrayList<Pair<String, BufferedImage>>();
            for ( Layer layer : layers ) {
                String layerName = layer.getName();
                try {
                    BufferedImage bi = null;
                    org.deegree.style.se.unevaluated.Style style = getStyle( layer.getStyle() );
                    if ( style != null ) {
                        bi = getLegendImg( style, width );
                    } else {
                        String legendUrl = getLegendUrl( layer );
                        LOG.debug( "Try to load legend image from WMS {}: ", legendUrl );
                        try {
                            bi = loadImage( legendUrl );
                        } catch ( IOException e ) {
                            String msg = "Could not create legend for layer: " + layerName;
                            LOG.error( msg );
                            throw new ProcessletException( msg );
                        }
                    }
                    legends.add( new Pair<String, BufferedImage>( layerName, bi ) );
                } catch ( Exception e ) {
                    String dsName = datasource.getName() != null ? datasource.getName() : datasource.getUrl();
                    LOG.info( "Could not create legend image for datasource '" + dsName + "', layer " + layerName + ".",
                              e );
                }
            }
            return legends;
        }

        String getLegendUrl( Layer layer ) {
            String url = datasource.getUrl();
            StringBuilder sb = new StringBuilder();
            sb.append( url );
            if ( !url.endsWith( "?" ) )
                sb.append( '?' );
            sb.append( "REQUEST=GetLegendGraphic&" );
            sb.append( "SERVICE=WMS&" );
            sb.append( "VERSION=" ).append( datasource.getVersion() ).append( '&' );
            sb.append( "LAYER=" ).append( layer.getName() ).append( '&' );
            sb.append( "TRANSPARENT=" ).append( "TRUE" ).append( '&' );
            sb.append( "FORMAT=" ).append( "image/png" );
            // .append( '&' );
            // STYLES=
            return sb.toString();
        }
    }

    class WFSOrderedDatasource extends OrderedDatasource<WFSDatasource> {

        public WFSOrderedDatasource( WFSDatasource datasource ) {
            super( datasource );
        }

        public WFSOrderedDatasource( WFSDatasource datasource, int pos ) {
            super( datasource, pos, pos );
        }

        @Override
        BufferedImage getImage( int width, int height, Envelope bbox, ICRS crs )
                                throws ProcessletException {
            try {
                String capURL = datasource.getUrl() + "?service=WFS&request=GetCapabilities&version="
                                + datasource.getVersion();
                WFSClient wfsClient = new WFSClient( new URL( capURL ) );

                StreamFeatureCollection features = wfsClient.getFeatures( new QName(
                                                                                     datasource.getFeatureType().getValue(),
                                                                                     datasource.getFeatureType().getValue() ) );

                BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
                Graphics2D g = image.createGraphics();
                Java2DRenderer renderer = new Java2DRenderer( g, width, height, bbox /* pixelSize */);

                // TODO
                // XMLInputFactory fac = XMLInputFactory.newInstance();
                // XMLStreamReader reader = fac.createXMLStreamReader( new DOMSource( datasource.getFilter() ) );
                // nextElement( reader );
                // nextElement( reader );
                // Filter filter = Filter110XMLDecoder.parse( reader );
                // reader.close();

                org.deegree.style.se.unevaluated.Style style = getStyle( datasource.getStyle() );
                if ( style != null && features != null ) {
                    Feature feature = null;
                    while ( ( feature = features.read() ) != null ) {
                        LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evaluate = style.evaluate( feature,
                                                                                                             new FeatureXPathEvaluator(
                                                                                                                                        GMLVersion.GML_32 ) );
                        for ( Triple<Styling, LinkedList<Geometry>, String> triple : evaluate ) {
                            for ( Geometry geom : triple.second ) {
                                renderer.render( triple.first, geom );
                            }
                        }
                    }
                }
                g.dispose();
                return image;
            } catch ( Exception e ) {
                String msg = "could nor create image from wfs datasource " + datasource.getName() + ":  "
                             + e.getMessage();
                LOG.error( msg, e );
                throw new ProcessletException( msg );
            }
        }

        @Override
        Map<String, List<String>> getLayerList() {
            HashMap<String, List<String>> layerList = new HashMap<String, List<String>>();
            layerList.put( datasource.getName(), new ArrayList<String>() );
            return layerList;
        }

        @Override
        public List<Pair<String, BufferedImage>> getLegends( int width ) {
            ArrayList<Pair<String, BufferedImage>> legends = new ArrayList<Pair<String, BufferedImage>>();

            BufferedImage legend = null;
            String name = datasource.getName() != null ? datasource.getName() : datasource.getUrl();
            try {
                org.deegree.style.se.unevaluated.Style style = getStyle( datasource.getStyle() );
                if ( style != null ) {
                    legend = getLegendImg( style, width );
                }
            } catch ( Exception e ) {
                LOG.debug( "Could not create legend for wfs datasource '{}': {}", name, e.getMessage() );
            }
            legends.add( new Pair<String, BufferedImage>( name, legend ) );
            return legends;
        }

    }

    private File writeImage( BufferedImage bi )
                            throws ProcessletException {
        FileOutputStream fos = null;
        try {
            File f = File.createTempFile( "createdMap", ".png" );
            fos = new FileOutputStream( f );

            PNGEncodeParam encodeParam = PNGEncodeParam.getDefaultEncodeParam( bi );

            if ( encodeParam instanceof PNGEncodeParam.Palette ) {
                PNGEncodeParam.Palette p = (PNGEncodeParam.Palette) encodeParam;
                byte[] b = new byte[] { -127 };
                p.setPaletteTransparency( b );
            }
            com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder( "PNG", fos, encodeParam );
            encoder.encode( bi.getData(), bi.getColorModel() );
            LOG.debug( "Wrote map to file: {}", f.toString() );
            return f;
        } catch ( IOException e ) {
            String msg = "Could not write map to file: " + e.getMessage();
            LOG.debug( msg, e );
            throw new ProcessletException( msg );
        } finally {
            IOUtils.closeQuietly( fos );
        }
    }

}
