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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.process.jaxb.java.AllowedValues;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition.DataType;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.ProcessletOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process to get a preview about the report.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class JrxmlTemplateProcess extends AbstractJrxmlWPSProcess {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlTemplateProcess.class );

    private static final String parameterId = "processId";

    final String text = "template";

    private final Map<String, URL> idToTemplateId;

    public JrxmlTemplateProcess( Map<String, URL> idToTemplateId ) {
        this.idToTemplateId = idToTemplateId;
    }

    @Override
    public ProcessDefinition getDescription() {
        InputParameters inputParams = new InputParameters();
        List<JAXBElement<? extends ProcessletInputDefinition>> processInput = inputParams.getProcessInput();

        LiteralInputDefinition lit = new LiteralInputDefinition();
        lit.setIdentifier( getAsCodeType( parameterId ) );
        lit.setTitle( getAsLanguageStringType( "processId" ) );
        lit.setMaxOccurs( BigInteger.valueOf( 1 ) );
        lit.setMinOccurs( BigInteger.valueOf( 1 ) );

        DataType dataType = new DataType();
        dataType.setValue( "string" );
        dataType.setReference( "http://www.w3.org/2001/XMLSchema.xsd#~string" );
        lit.setDataType( dataType );

        AllowedValues avs = new AllowedValues();
        for ( String processId : idToTemplateId.keySet() ) {
            if ( idToTemplateId.get( processId ) != null ) {
                if ( idToTemplateId.get( processId ).getFile().endsWith( ".png" ) ) {
                    avs.getValueOrRange().add( processId );
                } else {
                    LOG.info( "Template for process with id {} can not be handled: format is not yet supported. Supported format is imagepng.",
                              processId );
                }
            }
        }
        lit.setAllowedValues( avs );
        processInput.add( new JAXBElement<LiteralInputDefinition>( new QName( "ProcessInput" ),
                                                                   LiteralInputDefinition.class, lit ) );

        OutputParameters outputParams = new OutputParameters();
        ComplexOutputDefinition output = new ComplexOutputDefinition();
        output.setTitle( getAsLanguageStringType( text ) );
        output.setIdentifier( getAsCodeType( text ) );
        ComplexFormatType format = new ComplexFormatType();
        // TODO: mime type image
        format.setMimeType( "image/png" );
        output.setDefaultFormat( format );

        outputParams.getProcessOutput().add( new JAXBElement<ComplexOutputDefinition>( new QName( text ),
                                                                                       ComplexOutputDefinition.class,
                                                                                       output ) );

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setConfigVersion( "0.5.0" );
        processDefinition.setProcessVersion( "1.0.0" );
        processDefinition.setStatusSupported( false );
        processDefinition.setStoreSupported( true );

        processDefinition.setIdentifier( getAsCodeType( "TEMPLATE" ) );
        processDefinition.setInputParameters( inputParams );
        processDefinition.setOutputParameters( outputParams );
        return processDefinition;
    }

    @Override
    public Processlet getProcesslet() {
        return new Processlet() {

            @Override
            public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                                    throws ProcessletException {
                ProcessletInput input = in.getParameter( parameterId );
                if ( input != null ) {
                    LiteralInput li = (LiteralInput) input;
                    String processId = li.getValue();
                    URL url = idToTemplateId.get( processId );
                    if ( url != null ) {
                        ProcessletOutput output = out.getParameter( text );
                        ComplexOutput co = (ComplexOutput) output;
                        OutputStream os = co.getBinaryOutputStream();
                        try {
                            InputStream is = url.openStream();
                            byte[] buffer = new byte[8];
                            int len;
                            while ( ( len = is.read( buffer ) ) != -1 ) {
                                os.write( buffer, 0, len );
                            }
                        } catch ( IOException e ) {
                            String msg = "Could not pass image template to outputs stream";
                            LOG.error( msg, e );
                            throw new ProcessletException( msg );
                        }
                    }
                }

            }

            @Override
            public void init() {
            }

            @Override
            public void destroy() {
            }
        };
    }

    @Override
    public ExceptionCustomizer getExceptionCustomizer() {
        return null;
    }

}
