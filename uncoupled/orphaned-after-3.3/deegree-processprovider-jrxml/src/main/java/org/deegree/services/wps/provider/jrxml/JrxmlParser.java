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
package org.deegree.services.wps.provider.jrxml;

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.provider.jrxml.contentprovider.JrxmlContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Parser for jrxml files
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class JrxmlParser {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlParser.class );

    private List<String> globalParameters = new ArrayList<String>();

    JrxmlParser() {
    }

    /**
     * @param globalParameters
     */
    JrxmlParser( List<String> globalParameters ) {
        this.globalParameters = globalParameters;
    }

    /**
     * @param processId
     *            the id of the process, never <code>null</code>
     * @param name
     *            the name of the process
     * @param string
     * @param jrxmlAdapter
     *            the adapter encapsulating the jrxml, never <code>null</code>
     * @param contentProviders
     *            a list of {@link JrxmlContentProvider}s, never <code>null</code>
     * @param parameterDescriptions
     * @return
     */
    public Pair<ProcessDefinition, Map<String, String>> parse( String processId, String name, String description,
                                                               XMLAdapter jrxmlAdapter,
                                                               List<JrxmlContentProvider> contentProviders,
                                                               Map<String, ParameterDescription> parameterDescriptions ) {

        OMElement root = jrxmlAdapter.getRootElement();
        String processName = jrxmlAdapter.getNodeAsString( root, new XPath( "/jasper:jasperReport/@name", nsContext ),
                                                           name );

        Map<String, String> parameters = new HashMap<String, String>();
        List<OMElement> paramElements = jrxmlAdapter.getElements( root,
                                                                  new XPath( "/jasper:jasperReport/jasper:parameter",
                                                                             nsContext ) );
        for ( OMElement paramElement : paramElements ) {
            String paramName = paramElement.getAttributeValue( new QName( "name" ) );
            String paramType = paramElement.getAttributeValue( new QName( "class" ) );
            LOG.debug( "Found parameter '{}', type {}", paramName, paramType );
            parameters.put( paramName, paramType );
        }

        InputParameters inputParams = new InputParameters();
        List<JAXBElement<? extends ProcessletInputDefinition>> processInput = inputParams.getProcessInput();
        List<String> handledParameters = new ArrayList<String>();
        handledParameters.addAll( globalParameters );
        for ( JrxmlContentProvider contentProvider : contentProviders ) {
            contentProvider.inspectInputParametersFromJrxml( parameterDescriptions, processInput, jrxmlAdapter,
                                                             parameters, handledParameters );
        }

        OutputParameters outputParams = new OutputParameters();
        ComplexOutputDefinition output = new ComplexOutputDefinition();
        output.setTitle( getAsLanguageStringType( "report" ) );
        output.setIdentifier( getAsCodeType( "report" ) );
        ComplexFormatType format = new ComplexFormatType();
        format.setMimeType( JrxmlUtils.OUTPUT_MIME_TYPES.PDF.getMimeType() );
        output.setDefaultFormat( format );

        // ComplexFormatType html = new ComplexFormatType();
        // html.setMimeType( JrxmlUtils.OUTPUT_MIME_TYPES.HTML.getMimeType() );
        // output.getOtherFormats().add( html );

        outputParams.getProcessOutput().add( new JAXBElement<ComplexOutputDefinition>( new QName( "report" ),
                                                                                       ComplexOutputDefinition.class,
                                                                                       output ) );
        ProcessDefinition processDefinition = new ProcessDefinition();

        processDefinition.setConfigVersion( "0.5.0" );
        processDefinition.setProcessVersion( "1.0.0" );
        processDefinition.setStatusSupported( false );
        processDefinition.setStoreSupported( true );

        processDefinition.setIdentifier( getAsCodeType( processId ) );
        processDefinition.setTitle( getAsLanguageStringType( processName ) );
        if ( description != null )
            processDefinition.setAbstract( getAsLanguageStringType( description ) );
        processDefinition.setInputParameters( inputParams );
        processDefinition.setOutputParameters( outputParams );
        return new Pair<ProcessDefinition, Map<String, String>>( processDefinition, parameters );
    }

}
