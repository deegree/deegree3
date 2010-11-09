//$HeadURL$
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
package org.deegree.services.wps.provider;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProcessProviderProvider} for the {@link JavaProcessProvider}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JavaProcessProviderProvider implements ProcessProviderProvider {

    private static final Logger LOG = LoggerFactory.getLogger( JavaProcessProviderProvider.class );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/services/wps";

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public ProcessProvider createProvider( URL configURL ) {

        ProcessProvider manager = null;

        LOG.info( "Loading process definition from file '" + configURL + "'." );
        //
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.jaxb.wps" );
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            ProcessDefinition processDef = (ProcessDefinition) unmarshaller.unmarshal( configURL );
//            checkConfigVersion( definitionFile, processDef.getConfigVersion() );

            // processDefinitions.add( processDef );
            //
            // String wsdlFile = definitionFile.substring( 0, definitionFile.lastIndexOf( ".xml" ) ) + ".wsdl";
            // LOG.debug( "Checking for process WSDL file: '" + wsdlFile + "'" );
            // File f = new File( processesDir, wsdlFile );
            // if ( f.exists() ) {
            // CodeType processId = new CodeType( processDef.getIdentifier().getValue(),
            // processDef.getIdentifier().getCodeSpace() );
            // LOG.info( "Found process WSDL file." );
            // processIdToWSDL.put( processId, f );
            // }
            manager = new JavaProcessProvider( processDef );
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
        return manager;
    }
}
