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

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition.DataType;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class PropertiesContentProvider implements JrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( PropertiesContentProvider.class );

    private static final String PROPERTYNAME = "LANGUAGE";

    private final org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle resourceBundle;

    public PropertiesContentProvider( org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle resourceBundle ) {
        this.resourceBundle = resourceBundle;

    }

    @Override
    public void inspectInputParametersFromJrxml( List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {
        LiteralInputDefinition localeInput = getInputDefinition();

        inputs.add( new JAXBElement<LiteralInputDefinition>( new QName( "ProcessInput" ), LiteralInputDefinition.class,
                                                             localeInput ) );
        for ( String key : parameters.keySet() ) {
            if ( key.startsWith( resourceBundle.getPrefix() ) ) {
                handledParameters.add( key );
            }
        }
    }

    LiteralInputDefinition getInputDefinition() {
        LiteralInputDefinition localeInput = new LiteralInputDefinition();
        localeInput.setIdentifier( getAsCodeType( PROPERTYNAME ) );
        localeInput.setTitle( getAsLanguageStringType( PROPERTYNAME ) );
        localeInput.setDefaultValue( resourceBundle.getDefaultLocale() );
        localeInput.setMaxOccurs( BigInteger.valueOf( 1 ) );
        localeInput.setMinOccurs( BigInteger.valueOf( 0 ) );
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
    public InputStream prepareJrxmlAndReadInputParameters( InputStream jrxml, Map<String, Object> params,
                                                           ProcessletInputs in, List<CodeType> processedIds,
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
            ClassLoader moduleClassLoader = DeegreeWorkspace.getInstance().getModuleClassLoader();
            if ( moduleClassLoader != null )
                rb = ResourceBundle.getBundle( resourceBundle.getName(), l, moduleClassLoader );
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
        return jrxml;
    }
}
