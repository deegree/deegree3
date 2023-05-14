/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.wps.provider.sextante;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.services.wps.provider.ProcessProviderProvider;
import org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProcessProviderProvider} for the {@link SextanteProcessProvider}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class SextanteProcessProviderProvider implements ProcessProviderProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SextanteProcessProviderProvider.class );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/processes/sextante";

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public ProcessProvider create( URL configURL ) {

        LOG.info( "Configuring Sextante process provider using file '" + configURL + "'." );

        SextanteProcesses config = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.wps.provider.sextante.jaxb",
                                                      workspace.getModuleClassLoader() );
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            config = (SextanteProcesses) unmarshaller.unmarshal( configURL );
        } catch ( JAXBException e ) {
            throw new IllegalArgumentException( e.getMessage(), e );
        }

        // return a SEXTANTE process provider instance with the extracted configuration
        return new SextanteProcessProvider( config );
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class };
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public URL getConfigSchema() {
        return SextanteProcessProviderProvider.class.getResource( "/META-INF/schemas/processes/sextante/0.1.0/sextante.xsd" );
    }
}