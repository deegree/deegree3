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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.jrxml.contentprovider.DataTableContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.ImageContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.JrxmlContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.OtherContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.PropertiesContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.SubreportContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic {@link WPSProcess} encapsulating an jrxml file. The input parameters are the parameter out of the jrxml
 * file, output parameter is the report.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class JrxmlWPSProcess extends AbstractJrxmlWPSProcess {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlWPSProcess.class );

    private Processlet processlet;

    private ProcessDefinition description;

    private final List<JrxmlContentProvider> contentProviders = new ArrayList<JrxmlContentProvider>();

    private final JrxmlProcessDescription processDescription;

    private final static List<String> globalParameters = new ArrayList<String>();

    static String JRXML_LOCATION_PARAM = "TEMPLATE_LOCATION";

    public JrxmlWPSProcess( JrxmlProcessDescription processDescription ) {
        this.processDescription = processDescription;
        globalParameters.add( JRXML_LOCATION_PARAM );
    }

    @Override
    public void init( Workspace workspace ) {
        contentProviders.add( new DataTableContentProvider( workspace ) );
        contentProviders.add( new MapContentProvider( workspace ) );
        if ( processDescription.getResourceBundle() != null ) {
            contentProviders.add( new PropertiesContentProvider( workspace, processDescription.getResourceBundle() ) );
        }
        for ( String parameterName : processDescription.getSubreports().keySet() ) {
            contentProviders.add( new SubreportContentProvider(
                                                                workspace,
                                                                parameterName,
                                                                processDescription.getSubreports().get( parameterName ),
                                                                processDescription.getResourceBundle() ) );
        }
        contentProviders.add( new ImageContentProvider( workspace ) );
        contentProviders.add( new OtherContentProvider( workspace ) );
        try {
            XMLAdapter a = new XMLAdapter( processDescription.getUrl().openStream() );
            String name = processDescription.getUrl().getFile();
            if ( name.contains( "." ) )
                name = name.substring( 0, name.lastIndexOf( '.' ) );
            if ( name.contains( "/" ) )
                name = name.substring( name.lastIndexOf( '/' ) + 1, name.length() );

            Pair<ProcessDefinition, Map<String, String>> parsed = new JrxmlParser( globalParameters ).parse( processDescription.getId(),
                                                                                                             name,
                                                                                                             processDescription.getDescription(),
                                                                                                             a,
                                                                                                             contentProviders,
                                                                                                             processDescription.getParameterDescriptions() );
            this.description = parsed.first;
            this.processlet = new JrxmlProcesslet( processDescription.getUrl(), contentProviders, parsed.second );
        } catch ( XMLProcessingException e ) {
            String msg = "could not parse jrxml file: " + e.getMessage();
            LOG.error( msg, e );
            throw new IllegalArgumentException( msg );
        } catch ( IOException e ) {
            String msg = "could not read jrxml file: " + e.getMessage();
            LOG.error( msg, e );
            throw new IllegalArgumentException( msg );
        }
        getProcesslet().init();
    }

    @Override
    public ProcessDefinition getDescription() {
        return description;
    }

    @Override
    public Processlet getProcesslet() {
        return processlet;
    }

    @Override
    public ExceptionCustomizer getExceptionCustomizer() {
        return null;
    }

}
