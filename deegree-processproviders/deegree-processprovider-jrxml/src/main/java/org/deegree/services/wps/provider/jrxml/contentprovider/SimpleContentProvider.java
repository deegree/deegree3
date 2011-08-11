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
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;

/**
 * A {@link JrxmlContentProvider} for simple text and image parameters
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SimpleContentProvider implements JrxmlContentProvider {

    @Override
    public void inspectInputParametersFromJrxml( List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 List<String> textParameters, List<String> imgParameters ) {
        for ( String textFieldExpr : textParameters ) {
            LiteralInputDefinition lit = new LiteralInputDefinition();
            lit.setIdentifier( getAsCodeType( textFieldExpr ) );
            lit.setTitle( getAsLanguageStringType( textFieldExpr ) );
            lit.setDefaultValue( textFieldExpr );
            lit.setMaxOccurs( BigInteger.valueOf( 1 ) );
            lit.setMinOccurs( BigInteger.valueOf( 0 ) );
            // TODO: type
            inputs.add( new JAXBElement<LiteralInputDefinition>( new QName( "ProcessInput" ),
                                                                 LiteralInputDefinition.class, lit ) );
        }
        for ( String imgExpr : imgParameters ) {
            ComplexInputDefinition comp = new ComplexInputDefinition();
            comp.setTitle( getAsLanguageStringType( imgExpr ) );
            comp.setIdentifier( getAsCodeType( imgExpr ) );
            ComplexFormatType format = new ComplexFormatType();
            // TODO
            format.setEncoding( "UTF-8" );
            format.setMimeType( "text/xml" );
            format.setSchema( "" );
            comp.setDefaultFormat( format );
            comp.setMaxOccurs( BigInteger.valueOf( 1 ) );
            comp.setMinOccurs( BigInteger.valueOf( 0 ) );
            inputs.add( new JAXBElement<ComplexInputDefinition>( new QName( "ProcessInput" ),
                                                                 ComplexInputDefinition.class, comp ) );
        }
    }

    @Override
    public InputStream prepareJrxmlAndReadInputParameters( InputStream jrxml, Map<String, Object> params,
                                                           ProcessletInputs in, List<CodeType> processedIds ) {
        for ( ProcessletInput parameter : in.getParameters() ) {
            if ( !processedIds.contains( parameter.getIdentifier() ) ) {
                if ( parameter instanceof LiteralInput ) {
                    LiteralInput litIn = (LiteralInput) parameter;
                    // TODO: type
                    params.put( parameter.getIdentifier().getCode(), litIn.getValue() );
                    processedIds.add( litIn.getIdentifier() );
                }
                // TODO: complexinput
            }
        }
        // nothing to prepare here
        return jrxml;
    }

}
