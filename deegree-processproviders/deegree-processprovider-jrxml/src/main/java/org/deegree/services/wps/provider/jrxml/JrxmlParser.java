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
package org.deegree.services.wps.provider.jrxml;

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.LiteralOutputDefinition;
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
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class JrxmlParser {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlParser.class );

    /**
     * @param processId
     *            the id of the process, never <code>null</code>
     * @param name
     *            the name of the process
     * @param jrxmlAdapter
     *            the adapter encapsulating the jrxml, never <code>null</code>
     * @param contentProviders
     *            a list of {@link JrxmlContentProvider}s, never <code>null</code>
     * @return
     */
    public ProcessDefinition parse( String processId, String name, XMLAdapter jrxmlAdapter,
                                    List<JrxmlContentProvider> contentProviders ) {

        OMElement root = jrxmlAdapter.getRootElement();

        // textFields
        String[] textFieldExprs = jrxmlAdapter.getNodesAsStrings( root,
                                                                  new XPath(
                                                                             ".//jasper:textField/jasper:textFieldExpression",
                                                                             nsContext ) );
        List<String> textParameters = new ArrayList<String>();
        for ( String textFieldExpr : textFieldExprs ) {
            if ( isParameter( textFieldExpr ) ) {
                String normalizedParameter = normalize( textFieldExpr );
                LOG.debug( "Found textFieldExpression parameter: " + normalizedParameter );
                textParameters.add( normalizedParameter );
            }
        }

        // images
        String[] imgExprs = jrxmlAdapter.getNodesAsStrings( root, new XPath( ".//jasper:image/jasper:imageExpression",
                                                                             nsContext ) );
        List<String> imgParameters = new ArrayList<String>();
        for ( String imgExpr : imgExprs ) {
            if ( isParameter( imgExpr ) ) {
                String normalizedParameter = normalize( imgExpr );
                LOG.debug( "Found imageExpression parameter: " + normalizedParameter );
                imgParameters.add( normalizedParameter );
            }
        }

        // tables
        String processName = jrxmlAdapter.getNodeAsString( root, new XPath( "/jasper:jasperReport/@name", nsContext ),
                                                           name );

        InputParameters inputParams = new InputParameters();
        List<JAXBElement<? extends ProcessletInputDefinition>> processInput = inputParams.getProcessInput();
        for ( JrxmlContentProvider contentProvider : contentProviders ) {
            contentProvider.inspectInputParametersFromJrxml( processInput, textParameters, imgParameters );
        }

        OutputParameters outputParams = new OutputParameters();
        LiteralOutputDefinition output = new LiteralOutputDefinition();
        output.setTitle( getAsLanguageStringType( "report" ) );
        output.setIdentifier( getAsCodeType( "report" ) );
        // TODO: check the output, is it a link or emebedded? the format is interesting also
        // ComplexFormatType format = new ComplexFormatType();
        // format.setMimeType( JrxmlConstants.OUTPUT_MIME_TYPES.PDF.getMimeType() );
        // output.setDefaultFormat( format );
        outputParams.getProcessOutput().add( new JAXBElement<LiteralOutputDefinition>( new QName( "ProcessOutput" ),
                                                                                       LiteralOutputDefinition.class,
                                                                                       output ) );

        ProcessDefinition processDefinition = new ProcessDefinition();

        processDefinition.setConfigVersion( "0.5.0" );
        processDefinition.setProcessVersion( "1.0.0" );
        processDefinition.setStatusSupported( false );
        processDefinition.setStoreSupported( true );

        processDefinition.setIdentifier( getAsCodeType( processId ) );
        processDefinition.setTitle( getAsLanguageStringType( processName ) );
        processDefinition.setInputParameters( inputParams );
        processDefinition.setOutputParameters( outputParams );
        return processDefinition;
    }

    private String normalize( String parameter ) {
        if ( isParameter( parameter ) ) {
            return parameter.substring( 3, parameter.length() - 1 );
        }
        return parameter;
    }

    private boolean isParameter( String text ) {
        return text.matches( "\\$P\\{(\\w*)\\}" );
    }

}
