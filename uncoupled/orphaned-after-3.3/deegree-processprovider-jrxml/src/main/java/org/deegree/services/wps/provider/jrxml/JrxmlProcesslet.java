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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.ProcessletOutput;
import org.deegree.services.wps.provider.jrxml.JrxmlUtils.OUTPUT_MIME_TYPES;
import org.deegree.services.wps.provider.jrxml.contentprovider.JrxmlContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * processes {@link JrxmlWPSProcess} requests
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class JrxmlProcesslet implements Processlet {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlProcesslet.class );

    private final URL jrxml;

    private final List<JrxmlContentProvider> contentProviders;

    private final Map<String, String> parameters;

    /**
     * @param jrxml
     *            the jrxml file, never <code>null</code>
     * @param contentProviders
     *            a list of {@link JrxmlContentProvider}, never <code>null</code>
     * @param second
     */
    public JrxmlProcesslet( URL jrxml, List<JrxmlContentProvider> contentProviders, Map<String, String> second ) {
        this.jrxml = jrxml;
        this.contentProviders = contentProviders;
        this.parameters = second;
    }

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
        try {
            Pair<InputStream, Boolean> is = new Pair<InputStream, Boolean>( jrxml.openStream(), false );
            Map<String, Object> params = new HashMap<String, Object>();
            List<CodeType> processedIds = new ArrayList<CodeType>();
            boolean isDatasourceInserted = false;
            for ( JrxmlContentProvider contentProvider : contentProviders ) {
                LOG.debug( "ContentProvider: " + contentProvider.getClass().getName() );
                is = contentProvider.prepareJrxmlAndReadInputParameters( is.first, params, in, processedIds, parameters );
                isDatasourceInserted = isDatasourceInserted || is.second;
            }
            String tempLoc = jrxml.getPath().substring( 0, jrxml.getPath().lastIndexOf( "/" ) );
            params.put( JrxmlWPSProcess.JRXML_LOCATION_PARAM, tempLoc );
            
            for(String p : params.keySet())
                System.out.println(p + " + " + params.get(p));

            JasperPrint fillReport;
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "a datasource was" + ( isDatasourceInserted ? " " : " not " ) + "inserted" );
            }
            if ( isDatasourceInserted ) {
                fillReport = JasperFillManager.fillReport( JasperCompileManager.compileReport( is.first ), params );
            } else {
                fillReport = JasperFillManager.fillReport( JasperCompileManager.compileReport( is.first ), params,
                                                           new JREmptyDataSource() );
            }
            ProcessletOutput output = out.getParameter( "report" );
            processOutput( output, fillReport );

        } catch ( JRException e ) {
            String msg = "could not create pdf file: " + e.getMessage();
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        } catch ( IOException e ) {
            String msg = "could not create file: " + e.getMessage();
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        }
    }

    private void processOutput( ProcessletOutput output, JasperPrint fillReport )
                            throws IOException, JRException {
        ComplexOutput co = (ComplexOutput) output;
        if ( JrxmlUtils.OUTPUT_MIME_TYPES.valueOfMimeType( co.getRequestedMimeType() ) == OUTPUT_MIME_TYPES.PDF ) {
            OutputStream outputStream = co.getBinaryOutputStream();
            JasperExportManager.exportReportToPdfStream( fillReport, outputStream );
            // } else if ( JrxmlUtils.OUTPUT_MIME_TYPES.valueOfMimeType(co.getRequestedMimeType() ) ==
            // OUTPUT_MIME_TYPES.HTML ) {
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

}
