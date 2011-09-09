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

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.JrxmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataTableContentProvider implements JrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( DataTableContentProvider.class );

    static final String SCHEMA = "http://www.deegree.org/processprovider/table";

    final static String MIME_TYPE = "text/xml";

    static final String TABLE_PREFIX = "xml";

    static final String DETAIL_SUFFIX = "DetailEntry";

    static final String HEADER_SUFFIX = "HeaderEntry";

    private static final NamespaceBindings nsContext;
    static {
        nsContext = JrxmlUtils.nsContext.addNamespace( "tbl", SCHEMA );
    }

    @Override
    public void inspectInputParametersFromJrxml( List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
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
            comp.setTitle( getAsLanguageStringType( tableId ) );
            comp.setIdentifier( getAsCodeType( tableId ) );
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

    private String getIdentifierFromParameter( String fieldName ) {
        return fieldName.substring( TABLE_PREFIX.length(), fieldName.indexOf( "_" ) );
    }

    private boolean isTableParameter( String paramName ) {
        return paramName != null ? paramName.matches( TABLE_PREFIX + "[a-zA-Z0-9]*_(" + DETAIL_SUFFIX + "|"
                                                      + HEADER_SUFFIX + ")1" ) : false;
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

                    String tableId = complexIn.getIdentifier().getCode();
                    LOG.debug( "Found input parameter " + tableId + " representing a xml datasource!" );
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        // TODO: read number of headerentries
                        // XMLAdapter tableAdapter = new XMLAdapter( complexIn.getValueAsXMLStream() );
                        // List<OMElement> elements = tableAdapter.getElements( tableAdapter.getRootElement(),
                        // new XPath(
                        // "/tbl:XMLDataSource/tbl:Header/tbl:HeaderEntry",
                        // nsContext ) );
                        int numberOfEntries = 4; // elements.size();
                        if ( numberOfEntries > 1 ) {
                            XMLAdapter jrxmlAdapter = new XMLAdapter( jrxml );
                            OMElement root = jrxmlAdapter.getRootElement();
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
                            if ( LOG.isDebugEnabled() ) {
                                LOG.debug( "Adjusted jrxml: " + root );
                            }
                            // reset xml
                            root.serialize( bos );
                            jrxml = new ByteArrayInputStream( bos.toByteArray() );
                        }
                        // add complete input xml
                        InputStream xmlIs = complexIn.getValueAsBinaryStream();
                        Document document = JRXmlUtils.parse( xmlIs );
                        params.put( JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document );
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

        return jrxml;
    }

    private void setText( OMFactory factory, OMElement txtElement, String text ) {
        // this does not work:
        // e.setText( layer );
        // it attaches the text, but does not replace
        txtElement.getFirstOMChild().detach();
        txtElement.addChild( factory.createOMText( txtElement, text ) );
    }
}
