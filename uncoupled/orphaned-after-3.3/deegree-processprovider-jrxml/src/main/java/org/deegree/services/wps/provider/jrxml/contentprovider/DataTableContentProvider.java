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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.JrxmlUtils;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.workspace.Workspace;
import org.jaxen.dom.DOMXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Handles XML Datasources, fields must have a prefix 'xml' and suffix 'DetailEntry' and/or 'HeaderEntry'
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class DataTableContentProvider extends AbstractJrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( DataTableContentProvider.class );

    public static final String SCHEMA = "http://www.deegree.org/processprovider/table";

    public final static String MIME_TYPE = "text/xml";

    static final String TABLE_PREFIX = "xml";

    static final String DETAIL_SUFFIX = "DetailEntry";

    static final String HEADER_SUFFIX = "HeaderEntry";

    private static final NamespaceBindings nsContext;

    private String datasourceParameterName = JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT;

    // TODO: multiple datasources/xmlDaraTables!
    // datasource - xmldataTable report parameter must be asigned
    private String tableId;

    static {
        nsContext = JrxmlUtils.nsContext.addNamespace( "tbl", SCHEMA );
    }

    public DataTableContentProvider( Workspace workspace ) {
        super( workspace );
    }

    public DataTableContentProvider( Workspace workspace, String datasourceParameterName ) {
        super( workspace );
        if ( datasourceParameterName != null )
            this.datasourceParameterName = datasourceParameterName;
    }

    @Override
    public void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescriptions,
                                                 List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {
        List<String> tableIds = new ArrayList<String>();

        List<OMElement> fieldElements = jrxmlAdapter.getElements( jrxmlAdapter.getRootElement(),
                                                                  new XPath( "/jasper:jasperReport/jasper:field",
                                                                             nsContext ) );
        for ( OMElement fieldElement : fieldElements ) {
            String fieldName = fieldElement.getAttributeValue( new QName( "name" ) );
            if ( isTableParameter( fieldName ) ) {
                String identifier = getIdentifierFromParameter( fieldName );
                if ( !tableIds.contains( identifier ) ) {
                    tableIds.add( identifier );
                }
            }
        }

        for ( String tableId : tableIds ) {
            LOG.debug( "Found table component with id " + tableId );
            ComplexInputDefinition comp = new ComplexInputDefinition();
            addInput( comp, parameterDescriptions, tableId, 1, 0 );
            ComplexFormatType format = new ComplexFormatType();
            format.setEncoding( "UTF-8" );
            format.setMimeType( MIME_TYPE );
            format.setSchema( SCHEMA );
            comp.setDefaultFormat( format );
            inputs.add( new JAXBElement<ComplexInputDefinition>( new QName( "ProcessInput" ),
                                                                 ComplexInputDefinition.class, comp ) );
            this.tableId = tableId;
        }

    }

    private String getIdentifierFromParameter( String fieldName ) {
        return fieldName.substring( TABLE_PREFIX.length(), fieldName.indexOf( "_" ) );
    }

    private boolean isTableParameter( String paramName ) {
        return paramName != null ? paramName.matches( TABLE_PREFIX + "[a-zA-Z0-9]*_(" + DETAIL_SUFFIX + "|"
                                                      + HEADER_SUFFIX + ")1" ) : false;
    }

    @Override
    public Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml,
                                                                          Map<String, Object> params,
                                                                          ProcessletInputs in,
                                                                          List<CodeType> processedIds,
                                                                          Map<String, String> parameters )
                            throws ProcessletException {
        boolean hasDatasourceInserted = false;
        for ( ProcessletInput input : in.getParameters() ) {
            if ( !processedIds.contains( input.getIdentifier() ) && input instanceof ComplexInput ) {
                ComplexInput complexIn = (ComplexInput) input;
                if ( SCHEMA.equals( complexIn.getSchema() ) && MIME_TYPE.equals( complexIn.getMimeType() ) ) {

                    String tableId = complexIn.getIdentifier().getCode();
                    if ( tableId.equals( this.tableId ) ) {
                        LOG.debug( "Found input parameter " + tableId + " representing a xml datasource!" );
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {

                            XMLAdapter jrxmlAdapter = new XMLAdapter( jrxml );
                            OMElement root = jrxmlAdapter.getRootElement();

                            String query = jrxmlAdapter.getNodeAsString( root,
                                                                         new XPath(
                                                                                    "/jasper:jasperReport/jasper:queryString",
                                                                                    nsContext ), "." ).trim();

                            XMLStreamReader tableAsStream = complexIn.getValueAsXMLStream();
                            Document document = XMLStreamUtils.getAsDocument( tableAsStream );

                            DOMXPath xpath = new DOMXPath( "tbl:Header/tbl:HeaderEntry" );
                            xpath.addNamespace( "tbl", SCHEMA );
                            int numberOfEntries = xpath.selectNodes( document.getDocumentElement() ).size();
                            if ( numberOfEntries > 1 ) {
                                List<OMElement> fieldElements = jrxmlAdapter.getElements( root,
                                                                                          new XPath(
                                                                                                     "/jasper:jasperReport/jasper:field",
                                                                                                     nsContext ) );

                                OMFactory factory = OMAbstractFactory.getOMFactory();
                                for ( OMElement fieldElement : fieldElements ) {
                                    String fieldName = fieldElement.getAttributeValue( new QName( "name" ) );
                                    if ( isTableParameter( fieldName )
                                         && tableId.equals( getIdentifierFromParameter( fieldName ) ) ) {

                                        OMElement textFieldElement = jrxmlAdapter.getElement( root,
                                                                                              new XPath(
                                                                                                         ".//jasper:textField[jasper:textFieldExpression/text()='$F{"
                                                                                                                                 + fieldName
                                                                                                                                 + "}']",
                                                                                                         nsContext ) );
                                        for ( int i = 2; i < numberOfEntries + 1; i++ ) {
                                            OMElement newFieldElement = fieldElement.cloneOMElement();
                                            String newFieldName = fieldName.substring( 0, fieldName.length() - 1 ) + i;
                                            newFieldElement.addAttribute( "name", newFieldName, null );
                                            OMElement fieldDesc = jrxmlAdapter.getElement( newFieldElement,
                                                                                           new XPath(
                                                                                                      "jasper:fieldDescription",
                                                                                                      nsContext ) );
                                            String text = fieldDesc.getText();
                                            text = text.replace( "[1]", "[" + i + "]" );
                                            setText( factory, fieldDesc, text );
                                            fieldElement.insertSiblingAfter( newFieldElement );

                                            // reference

                                            if ( textFieldElement != null ) {
                                                int width = jrxmlAdapter.getRequiredNodeAsInteger( textFieldElement,
                                                                                                   new XPath(
                                                                                                              "jasper:reportElement/@width",
                                                                                                              nsContext ) );
                                                int x = jrxmlAdapter.getRequiredNodeAsInteger( textFieldElement,
                                                                                               new XPath(
                                                                                                          "jasper:reportElement/@x",
                                                                                                          nsContext ) );
                                                OMElement newDetailTextField = textFieldElement.cloneOMElement();
                                                jrxmlAdapter.getElement( newDetailTextField,
                                                                         new XPath( "jasper:reportElement", nsContext ) ).addAttribute( "x",
                                                                                                                                        Integer.toString( x
                                                                                                                                                          + width
                                                                                                                                                          * ( i - 1 ) ),
                                                                                                                                        null );
                                                OMElement newTextFieldExpr = jrxmlAdapter.getElement( newDetailTextField,
                                                                                                      new XPath(
                                                                                                                 "jasper:textFieldExpression",
                                                                                                                 nsContext ) );
                                                setText( factory, newTextFieldExpr, "$F{" + newFieldName + "}" );
                                                textFieldElement.insertSiblingAfter( newDetailTextField );
                                            }
                                        }

                                    }
                                }
                                if ( LOG.isTraceEnabled() ) {
                                    LOG.trace( "Adjusted jrxml: " + root );
                                }
                            }

                            // reset xml
                            root.serialize( bos );
                            jrxml = new ByteArrayInputStream( bos.toByteArray() );

                            // // add complete input xml
                            // OMNodeEx e ;
                            // OMElement document = tableAdapter.getRootElement();
                            // DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            // Document doc = builder.parse( tableAdapter.getRootElement() );
                            // TODO: reload should not be required!
                            if ( parameters.containsKey( datasourceParameterName )
                                 && "net.sf.jasperreports.engine.JRDataSource".equals( parameters.get( datasourceParameterName ) ) ) {
                                InputStream is = getAsInputStream( complexIn.getValueAsXMLStream() );
                                params.put( datasourceParameterName, new JRXmlDataSource( is, query ) );
                            } else {
                                Document doc = getAsDocument( complexIn.getValueAsXMLStream() );
                                params.put( datasourceParameterName, doc );
                            }
                            hasDatasourceInserted = true;
                        } catch ( Exception e ) {
                            String msg = "Could not process data table content: " + e.getMessage();
                            LOG.error( msg, e );
                            throw new ProcessletException( msg );
                        } finally {
                            IOUtils.closeQuietly( bos );
                        }
                        processedIds.add( complexIn.getIdentifier() );
                    }
                }
            }
        }
        return new Pair<InputStream, Boolean>( jrxml, hasDatasourceInserted );
    }

    private static Document getAsDocument( XMLStreamReader xmlStreamReader )
                            throws XMLStreamException, FactoryConfigurationError, ParserConfigurationException,
                            SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        StreamBufferStore store = new StreamBufferStore();
        XMLStreamWriter xmlWriter = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( store );
            XMLAdapter.writeElement( xmlWriter, xmlStreamReader );
        } finally {
            if ( xmlWriter != null ) {
                try {
                    xmlWriter.close();
                } catch ( XMLStreamException e ) {
                    LOG.error( "Unable to close xmlwriter." );
                }
            }
        }
        Document doc = builder.parse( store.getInputStream() );
        store.close();
        return doc;
    }

    private static InputStream getAsInputStream( XMLStreamReader xmlStreamReader )
                            throws XMLStreamException, FactoryConfigurationError, ParserConfigurationException,
                            SAXException, IOException {
        StreamBufferStore store = new StreamBufferStore();
        XMLStreamWriter xmlWriter = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( store );
            XMLAdapter.writeElement( xmlWriter, xmlStreamReader );
        } finally {
            if ( xmlWriter != null ) {
                try {
                    xmlWriter.close();
                } catch ( XMLStreamException e ) {
                    LOG.error( "Unable to close xmlwriter." );
                }
            }
        }
        return store.getInputStream();
    }

    void setTableId( String tableId ) {
        this.tableId = tableId;
    }

    private void setText( OMFactory factory, OMElement txtElement, String text ) {
        // this does not work:
        // e.setText( layer );
        // it attaches the text, but does not replace
        txtElement.getFirstOMChild().detach();
        txtElement.addChild( factory.createOMText( txtElement, text ) );
    }
}
