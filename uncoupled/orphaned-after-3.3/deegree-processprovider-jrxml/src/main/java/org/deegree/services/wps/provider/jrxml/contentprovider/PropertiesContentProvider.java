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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition.DataType;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class PropertiesContentProvider extends AbstractJrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( PropertiesContentProvider.class );

    private static final String PROPERTYNAME = "LANGUAGE";

    private final org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle resourceBundle;

    public PropertiesContentProvider( Workspace workspace,
                                      org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle resourceBundle ) {
        super( workspace );
        this.resourceBundle = resourceBundle;

    }

    @Override
    public void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescriptions,
                                                 List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {

        if ( !containsLanguageInput( inputs ) ) {
            LiteralInputDefinition localeInput = getInputDefinition( parameterDescriptions );

            inputs.add( new JAXBElement<LiteralInputDefinition>( new QName( "ProcessInput" ),
                                                                 LiteralInputDefinition.class, localeInput ) );
        }
        for ( String key : parameters.keySet() ) {
            if ( key.startsWith( resourceBundle.getPrefix() ) ) {
                handledParameters.add( key );
            }
        }
    }

    private boolean containsLanguageInput( List<JAXBElement<? extends ProcessletInputDefinition>> inputs ) {
        for ( JAXBElement<? extends ProcessletInputDefinition> input : inputs ) {
            if ( PROPERTYNAME.equals( input.getValue().getIdentifier().getValue() ) ) {
                return true;
            }
        }
        return false;
    }

    LiteralInputDefinition getInputDefinition( Map<String, ParameterDescription> parameterDescriptions ) {
        LiteralInputDefinition localeInput = new LiteralInputDefinition();
        addInput( localeInput, parameterDescriptions, PROPERTYNAME, 1, 0 );
        localeInput.setDefaultValue( resourceBundle.getDefaultLocale() );
        org.deegree.process.jaxb.java.AllowedValues avs = new org.deegree.process.jaxb.java.AllowedValues();
        avs.getValueOrRange().addAll( resourceBundle.getSupportedLocale() );
        localeInput.setAllowedValues( avs );

        DataType dataType = new DataType();
        dataType.setValue( "string" );
        dataType.setReference( "http://www.w3.org/2001/XMLSchema.xsd#~string" );
        localeInput.setDataType( dataType );
        return localeInput;
    }

    @Override
    public Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml,
                                                                          Map<String, Object> params,
                                                                          ProcessletInputs in,
                                                                          List<CodeType> processedIds,
                                                                          Map<String, String> parameters )
                            throws ProcessletException {

        Locale l;
        ProcessletInput parameter = in.getParameter( PROPERTYNAME );
        if ( parameter != null ) {
            LOG.debug( "Found parameter '{}'", PROPERTYNAME );
            LiteralInput lit = (LiteralInput) parameter;
            l = new Locale( lit.getValue() );
            processedIds.add( parameter.getIdentifier() );
        } else {
            l = new Locale( resourceBundle.getDefaultLocale() );
        }
        ResourceBundle rb = null;
        try {
            rb = ResourceBundle.getBundle( resourceBundle.getName(), l );
        } catch ( Exception e ) {
            if ( workspace != null ) {
                LOG.debug( "Try to find resource bundle in deegrees module class loader" );
                ClassLoader moduleClassLoader = workspace.getModuleClassLoader();
                LOG.debug( "Found module class loader {}", moduleClassLoader );
                if ( moduleClassLoader != null )
                    rb = ResourceBundle.getBundle( resourceBundle.getName(), l, moduleClassLoader );
            }
        }
        if ( rb != null ) {
            LOG.debug( "Found resource bundle for name '{}' and language '{}' ", resourceBundle.getName(),
                       l.getLanguage() );
            Enumeration<String> keys = rb.getKeys();
            while ( keys.hasMoreElements() ) {
                String key = keys.nextElement();
                if ( key.startsWith( resourceBundle.getPrefix() ) ) {
                    params.put( key, rb.getString( key ) );
                }
            }
        } else {
            LOG.info( "Could not find resource bundle for name '{}'.", resourceBundle.getName() );
        }
        return new Pair<InputStream, Boolean>( jrxml, false );
    }
}
