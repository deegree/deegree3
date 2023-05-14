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

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider;
import org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages subreports with XMLDatasources
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class SubreportContentProvider extends AbstractJrxmlContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SubreportContentProvider.class );

    private final String parameterPrefix;

    private static final String DIR_SUFFIX = "_dir";

    private static final String DS_SUFFIX = "_datasource";

    private final URL subreportJrxml;

    private List<JrxmlContentProvider> contentProviders;

    private final ResourceBundle resourceBundle;

    private Map<String, String> subreportParameters = new HashMap<String, String>();

    /**
     * @param parameterName
     * @param url
     * @param processDescription
     */
    public SubreportContentProvider( Workspace workspace, String parameterName, URL url, ResourceBundle resourceBundle ) {
        super( workspace );
        this.parameterPrefix = parameterName;
        this.subreportJrxml = url;
        this.resourceBundle = resourceBundle;
    }

    private List<JrxmlContentProvider> getContentProviders( String datasourceParameter ) {
        if ( contentProviders == null ) {
            contentProviders = new ArrayList<JrxmlContentProvider>();
            contentProviders.add( new DataTableContentProvider( workspace, getDatasourceParameter() ) );
            contentProviders.add( new MapContentProvider( workspace ) );
            contentProviders.add( new ImageContentProvider( workspace ) );
            if ( resourceBundle != null ) {
                contentProviders.add( new PropertiesContentProvider( workspace, resourceBundle ) );
            }
            contentProviders.add( new OtherContentProvider( workspace ) );
        }
        return contentProviders;
    }

    @Override
    public void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescription,
                                                 List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {
        if ( parameters.containsKey( getDirParameter() )
             && !handledParameters.contains( getDirParameter() )
             && jrxmlAdapter.getNode( jrxmlAdapter.getRootElement(),
                                      new XPath( ".//jasper:subreport/jasper:subreportExpression/text()='$P{"
                                                 + getDirParameter() + "}'", nsContext ) ) != null ) {
            LOG.debug( "Found subreport for '{}'", parameterPrefix );
            XMLAdapter subreportAdapter = new XMLAdapter( subreportJrxml );

            // Map<String, String> subreportParameters = new HashMap<String, String>();
            List<OMElement> paramElements = subreportAdapter.getElements( subreportAdapter.getRootElement(),
                                                                          new XPath(
                                                                                     "/jasper:jasperReport/jasper:parameter",
                                                                                     nsContext ) );
            for ( OMElement paramElement : paramElements ) {
                String paramName = paramElement.getAttributeValue( new QName( "name" ) );
                String paramType = paramElement.getAttributeValue( new QName( "class" ) );
                LOG.debug( "Found subreport parameter '{}', type {}", paramName, paramType );
                subreportParameters.put( paramName, paramType );
            }
            String datasourceParam = parameters.get( getDatasourceParameter() );
            if ( datasourceParam != null ) {
                handledParameters.add( getDatasourceParameter() );
                subreportParameters.put( getDatasourceParameter(), datasourceParam );
            }

            List<String> handledSubreportParameters = new ArrayList<String>();
            for ( JrxmlContentProvider contentProvider : getContentProviders( datasourceParam ) ) {
                contentProvider.inspectInputParametersFromJrxml( parameterDescription, inputs, subreportAdapter,
                                                                 subreportParameters, handledSubreportParameters );
            }
            handledParameters.add( getDirParameter() );
        }
    }

    private String getDirParameter() {
        return parameterPrefix + DIR_SUFFIX;
    }

    private String getDatasourceParameter() {
        return parameterPrefix + DS_SUFFIX;
    }

    @Override
    public Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml,
                                                                          Map<String, Object> params,
                                                                          ProcessletInputs in,
                                                                          List<CodeType> processedIds,
                                                                          Map<String, String> parameters )
                            throws ProcessletException {
        try {
            Pair<InputStream, Boolean> is = new Pair<InputStream, Boolean>( subreportJrxml.openStream(), false );
            if ( parameters.containsKey( getDirParameter() ) ) {
                for ( JrxmlContentProvider contentProvider : getContentProviders( getDatasourceParameter() ) ) {
                    LOG.debug( "ContentProvider in subreport: " + contentProvider.getClass().getName() );
                    is = contentProvider.prepareJrxmlAndReadInputParameters( is.first, params, in, processedIds,
                                                                             subreportParameters );
                }
                String subreport = File.createTempFile( "subreport", ".jasper" ).toString();
                JasperDesign jasperDesign = JRXmlLoader.load( is.first );
                JasperCompileManager.compileReportToFile( jasperDesign, subreport );
                params.put( getDirParameter(), subreport );
            }

            return new Pair<InputStream, Boolean>( jrxml, is.second );
        } catch ( JRException e ) {
            String msg = "Could not compile/create subreport with prefix " + parameterPrefix;
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        } catch ( IOException e ) {
            String msg = "Could not handle subreport with prefix " + parameterPrefix;
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        }

    }
}
