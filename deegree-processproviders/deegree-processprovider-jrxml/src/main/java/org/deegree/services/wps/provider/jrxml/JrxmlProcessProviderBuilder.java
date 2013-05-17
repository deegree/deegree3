/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.wps.provider.jrxml;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.services.wps.provider.jrxml.jaxb.process.JrxmlProcess;
import org.deegree.services.wps.provider.jrxml.jaxb.process.JrxmlProcess.Subreport;
import org.deegree.services.wps.provider.jrxml.jaxb.process.JrxmlProcesses;
import org.deegree.services.wps.provider.jrxml.jaxb.process.Metadata;
import org.deegree.services.wps.provider.jrxml.jaxb.process.Metadata.Parameter;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This class is responsible for building jrxml process providers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class JrxmlProcessProviderBuilder implements ResourceBuilder<ProcessProvider> {

    private ResourceMetadata<ProcessProvider> metadata;

    private JrxmlProcesses config;

    private Workspace workspace;

    public JrxmlProcessProviderBuilder( ResourceMetadata<ProcessProvider> metadata, JrxmlProcesses config,
                                        Workspace workspace ) {
        this.metadata = metadata;
        this.config = config;
        this.workspace = workspace;
    }

    @Override
    public ProcessProvider build() {
        ResourceLocation<ProcessProvider> loc = metadata.getLocation();

        List<JrxmlProcessDescription> processes = new ArrayList<JrxmlProcessDescription>();
        String jrxml = null;
        try {
            List<JrxmlProcess> processList = config.getJrxmlProcess();
            for ( JrxmlProcess jrxmlProcess : processList ) {
                jrxml = jrxmlProcess.getJrxml();
                org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle resourceBundle = jrxmlProcess.getResourceBundle();
                URL template = null;
                String description = null;
                Map<String, ParameterDescription> paramDescription = new HashMap<String, ParameterDescription>();
                if ( jrxmlProcess.getMetadata() != null ) {
                    Metadata metadata = jrxmlProcess.getMetadata();
                    if ( metadata.getTemplate() != null ) {
                        template = loc.resolveToUrl( metadata.getTemplate() );
                    }
                    description = metadata.getDescription();
                    for ( Parameter p : metadata.getParameter() ) {
                        paramDescription.put( p.getId(),
                                              new ParameterDescription( p.getId(), p.getTitle(), p.getDescription() ) );
                    }
                }
                Map<String, URL> subreports = new HashMap<String, URL>();
                for ( Subreport subreport : jrxmlProcess.getSubreport() ) {
                    subreports.put( subreport.getId(), loc.resolveToUrl( subreport.getValue() ) );
                }
                processes.add( new JrxmlProcessDescription( jrxmlProcess.getId(), loc.resolveToUrl( jrxml ),
                                                            description, paramDescription, template, subreports,
                                                            resourceBundle ) );
            }

        } catch ( Exception e ) {
            String msg = "Could not parse configuration " + metadata.getIdentifier() + ": " + e.getMessage();
            throw new ResourceInitException( msg, e );
        }
        return new JrxmlProcessProvider( processes, metadata, workspace );
    }

}
