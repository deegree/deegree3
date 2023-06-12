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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.input.ReferencedComplexInput;
import org.deegree.services.wps.provider.jrxml.JrxmlUtils;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.workspace.Workspace;

/**
 * A {@link JrxmlContentProvider} for image parameters
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class ImageContentProvider extends AbstractJrxmlContentProvider {

    public ImageContentProvider( Workspace workspace ) {
        super( workspace );
    }

    @Override
    public void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescriptions,
                                                 List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {
        OMElement root = jrxmlAdapter.getRootElement();
        for ( String parameterName : parameters.keySet() ) {
            if ( !handledParameters.contains( parameterName )
                 && jrxmlAdapter.getElement( root, new XPath( ".//jasper:image/jasper:imageExpression[text()='$P{"
                                                              + parameterName + "}']", JrxmlUtils.nsContext ) ) != null ) {

                ComplexInputDefinition comp = new ComplexInputDefinition();
                addInput( comp, parameterDescriptions, parameterName, 1, 0 );
                ComplexFormatType defaultFormat = new ComplexFormatType();
                defaultFormat.setMimeType( "image/png" );
                comp.setDefaultFormat( defaultFormat );
                inputs.add( new JAXBElement<ComplexInputDefinition>( new QName( "ProcessInput" ),
                                                                     ComplexInputDefinition.class, comp ) );
                handledParameters.add( parameterName );
            }
        }
    }

    @Override
    public Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml,
                                                                          Map<String, Object> params,
                                                                          ProcessletInputs in,
                                                                          List<CodeType> processedIds,
                                                                          Map<String, String> parameters )
                            throws ProcessletException {
        for ( ProcessletInput parameter : in.getParameters() ) {
            if ( !processedIds.contains( parameter.getIdentifier() ) ) {
                if ( parameter instanceof ComplexInput ) {
                    String parameterType = parameters.get( parameter.getIdentifier().getCode() );
                    Object value = null;
                    if ( parameter instanceof ReferencedComplexInput
                         && ( parameterType == null || parameterType.equals( "java.lang.String" )
                              || parameterType.equals( "java.io.File" ) || parameterType.equals( "java.net.URL" ) ) ) {
                        if ( parameterType == null || parameterType.equals( "java.lang.String" ) ) {
                            // add the reference
                            value = ( (ReferencedComplexInput) parameter ).getURL().toExternalForm();
                        } else if ( parameterType.equals( "java.io.File" ) ) {
                            value = new File( ( (ReferencedComplexInput) parameter ).getURL().toExternalForm() );
                        } else if ( parameterType.equals( "java.net.URL" ) ) {
                            value = ( (ReferencedComplexInput) parameter ).getURL();
                        }
                    } else {
                        ComplexInput litIn = (ComplexInput) parameter;
                        try {
                            InputStream is = litIn.getValueAsBinaryStream();
                            if ( parameterType == null || parameterType.equals( "java.lang.String" ) ) {
                                value = writeToFile( is ).toString();
                            } else if ( parameterType.equals( "java.io.File" ) ) {
                                value = writeToFile( is );
                            } else if ( parameterType.equals( "java.net.URL" ) ) {
                                value = writeToFile( is ).toURI().toURL();
                            } else if ( parameterType.equals( "java.io.InputStream" ) ) {
                                value = is;
                            } else if ( parameterType.equals( "java.awt.Image" ) ) {
                                value = ImageIO.read( is );
                            }
                        } catch ( IOException e ) {
                            throw new ProcessletException( "Could not process parameter '" + parameter.getIdentifier()
                                                           + "' as image parameter!" );
                        }
                    }
                    params.put( parameter.getIdentifier().getCode(), value );
                    processedIds.add( parameter.getIdentifier() );
                }
            }
        }
        // nothing to prepare here
        return new Pair<InputStream, Boolean>( jrxml, false );
    }

    private File writeToFile( InputStream is )
                            throws IOException {
        File f = File.createTempFile( "tmpImage", ".tmp" );
        OutputStream out = new FileOutputStream( f );
        byte buf[] = new byte[1024];
        int len;
        while ( ( len = is.read( buf ) ) > 0 )
            out.write( buf, 0, len );
        out.close();
        is.close();
        return f;
    }
}
