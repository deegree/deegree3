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

import java.io.IOException;
import java.net.URL;
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
import org.deegree.services.wps.provider.jrxml.contentprovider.MapContentProvider;
import org.deegree.services.wps.provider.jrxml.contentprovider.OtherContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic {@link WPSProcess} encapsulating an jrxml file. The input parameters are the parameter out of the jrxml
 * file, output parameter is the report.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class JrxmlWPSProcess implements WPSProcess {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlWPSProcess.class );

    private final Processlet processlet;

    private final ProcessDefinition description;

    private final List<JrxmlContentProvider> contentProviders = new ArrayList<JrxmlContentProvider>();

    /**
     * @param processId
     * @param file
     */
    public JrxmlWPSProcess( String processId, URL jrxml ) {
        contentProviders.add( new DataTableContentProvider() );
        contentProviders.add( new MapContentProvider() );
        contentProviders.add( new ImageContentProvider() );
        contentProviders.add( new OtherContentProvider() );
        try {
            XMLAdapter a = new XMLAdapter( jrxml.openStream() );
            String name = jrxml.getFile();
            if ( name.contains( "." ) )
                name = name.substring( 0, name.lastIndexOf( '.' ) );
            if ( name.contains( "/" ) )
                name = name.substring( name.lastIndexOf( '/' ) + 1, name.length() );

            Pair<ProcessDefinition, Map<String, String>> parsed = new JrxmlParser().parse( processId, name, a,
                                                                                           contentProviders );
            this.description = parsed.first;
            this.processlet = new JrxmlProcesslet( jrxml, contentProviders, parsed.second );
        } catch ( XMLProcessingException e ) {
            String msg = "could not parse jrxml file: " + e.getMessage();
            LOG.error( msg, e );
            throw new IllegalArgumentException( msg );
        } catch ( IOException e ) {
            String msg = "could not read jrxml file: " + e.getMessage();
            LOG.error( msg, e );
            throw new IllegalArgumentException( msg );
        }
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
