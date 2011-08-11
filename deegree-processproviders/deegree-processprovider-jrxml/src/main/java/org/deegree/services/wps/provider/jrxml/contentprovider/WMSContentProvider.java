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

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.JASPERREPORTS_NS;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a map in the jrxml
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class WMSContentProvider implements JrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( WMSContentProvider.class );

    final static String SCHEMA = "http://schemaURL";

    final static String MIME_TYPE = "text/xml";

    @Override
    public void inspectInputParametersFromJrxml( List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 List<String> textParameters, List<String> imgParameters ) {
        // for a wms, parameters starting with map are important. three different types are supported:
        // * wmsXYZ_map -> as image parameter
        // * wmsXYZ_legend -> as imgage parameter
        // * wmsXYZ_layerList -> as frame key
        // where XYZ is a string which is the identifier of the process parameter.
        List<String> mapIds = new ArrayList<String>();
        List<String> idsToRemove = new ArrayList<String>();
        for ( String imgParameter : imgParameters ) {
            if ( isMapParameter( imgParameter ) ) {
                String mapId = getMapIdentifier( imgParameter );
                if ( !mapIds.contains( mapId ) ) {
                    mapIds.add( mapId );
                }
                // TODO: maybe a status information would be the better way?
                // remove used parameter
                idsToRemove.add( imgParameter );
            }
        }
        for ( String idToRemove : idsToRemove ) {
            imgParameters.remove( idToRemove );
        }

        for ( String mapId : mapIds ) {
            LOG.debug( "Found map component with id " + mapId );
            ComplexInputDefinition comp = new ComplexInputDefinition();
            comp.setTitle( getAsLanguageStringType( mapId ) );
            comp.setIdentifier( getAsCodeType( mapId ) );
            ComplexFormatType format = new ComplexFormatType();
            // TODO
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

    private String getMapIdentifier( String imgParameter ) {
        if ( isMapParameter( imgParameter ) ) {
            imgParameter = imgParameter.substring( 3 );
            if ( imgParameter.endsWith( "_legend" ) ) {
                imgParameter = imgParameter.substring( 0, imgParameter.length() - 7 );
            } else if ( imgParameter.endsWith( "_map" ) ) {
                imgParameter = imgParameter.substring( 0, imgParameter.length() - 4 );
            }
        }
        return imgParameter;
    }

    private boolean isMapParameter( String imgParameter ) {
        return imgParameter.startsWith( "wms" )
               && ( imgParameter.endsWith( "_legend" ) || imgParameter.endsWith( "_map" ) );
    }

    @Override
    public InputStream prepareJrxmlAndReadInputParameters( InputStream jrxml, Map<String, Object> params,
                                                           ProcessletInputs in, List<CodeType> processedIds ) {
        for ( ProcessletInput input : in.getParameters() ) {
            if ( !processedIds.contains( input ) && input instanceof ComplexInput ) {
                ComplexInput complexIn = (ComplexInput) input;
                if ( SCHEMA.equals( complexIn.getSchema() ) && MIME_TYPE.equals( complexIn.getMimeType() ) ) {
                    String mapUrl = null;
                    String legendUrl = null;
                    try {
                        InputStream valueAsBinaryStream = complexIn.getValueAsBinaryStream();
                        mapUrl = IOUtils.toString( valueAsBinaryStream );
                        legendUrl = mapUrl;
                    } catch ( IOException e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    String mapId = complexIn.getIdentifier().getCode();
                    LOG.debug( "Found input parameter " + mapId + " representing a map!" );
                    XMLAdapter jrxmlAdapter = new XMLAdapter( jrxml );
                    OMElement root = jrxmlAdapter.getRootElement();
                    String layerListKey = "wms" + mapId + "_layerList";
                    OMElement layerListFrame = jrxmlAdapter.getElement( root,
                                                                        new XPath(
                                                                                   "/jasper:jasperReport/jasper:detail/jasper:band/jasper:frame[jasper:reportElement/@key='"
                                                                                                           + layerListKey
                                                                                                           + "']",
                                                                                   nsContext ) );

                    List<String> layers = Arrays.asList( mapUrl.split( "/" ) );
                    if ( layerListFrame != null ) {
                        LOG.debug( "Found layer list with key '" + layerListKey + "' to adjust." );
                        List<OMElement> elements = jrxmlAdapter.getElements( layerListFrame,
                                                                             new XPath( "jasper:staticText", nsContext ) );
                        OMElement grpTemplate = elements.get( 0 );
                        // OMElement field = elements.get( 1 );
                        for ( OMElement element : elements ) {
                            element.detach();
                        }
                        XMLAdapter grpAdapter = new XMLAdapter( grpTemplate );
                        int grpHeight = grpAdapter.getNodeAsInt( grpTemplate,
                                                                 new XPath( "jasper:reportElement/@height", nsContext ),
                                                                 15 );
                        int y = grpAdapter.getNodeAsInt( grpTemplate,
                                                         new XPath( "jasper:reportElement/@y", nsContext ), 0 );
                        OMFactory factory = OMAbstractFactory.getOMFactory();
                        for ( String layer : layers ) {
                            OMElement newGrp = grpTemplate.cloneOMElement();
                            OMElement e = newGrp.getFirstChildWithName( new QName( JASPERREPORTS_NS, "reportElement" ) );
                            e.addAttribute( "y", Integer.toString( y + grpHeight * layers.indexOf( layer ) ), null );
                            e = newGrp.getFirstChildWithName( new QName( JASPERREPORTS_NS, "text" ) );
                            // this does not work:
                            // e.setText( layer );
                            // it attaches the text, but does not replace
                            e.getFirstOMChild().detach();
                            e.addChild( factory.createOMText( e, layer ) );
                            layerListFrame.addChild( newGrp );
                        }
                    } else {
                        LOG.debug( "no layer list with key '" + layerListKey + "' found." );
                    }
                    params.put( "wms" + mapId + "_map", mapUrl );
                    params.put( "wms" + mapId + "_legend", legendUrl );
                    processedIds.add( input.getIdentifier() );

                    // get input stream
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        jrxmlAdapter.getRootElement().serialize( bos );
                        jrxml = new ByteArrayInputStream( bos.toByteArray() );
                    } catch ( XMLStreamException e ) {
                        throw new RuntimeException( e );
                    } finally {
                        IOUtils.closeQuietly( bos );
                    }

                }
            }
        }
        return jrxml;
    }
}
