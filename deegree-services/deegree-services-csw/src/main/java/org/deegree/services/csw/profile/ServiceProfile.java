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
package org.deegree.services.csw.profile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.csw.exporthandling.CapabilitiesHandler;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ServiceProfile {

    ImplementationMetadata<CSWRequestType> getImplementationMetadata();

    List<String> getSupportedVersions();

    String[] getSupportedServiceNames();

    String getAcceptFormat( GetCapabilities getCapabilitiesRequest )
                            throws OWSException;

    /**
     * @param writer
     * @param mainControllerConf
     * @param mainConf
     * @param sections
     * @param identification
     * @param version
     * @param isTransactionEnabled
     * @param isEnabledInspireExtension
     * @return
     */
    CapabilitiesHandler getCapabilitiesHandler( XMLStreamWriter writer, DeegreeServicesMetadataType mainControllerConf,
                                                DeegreeServiceControllerType mainConf, Set<Sections> sections,
                                                ServiceIdentificationType identification, Version version,
                                                boolean isTransactionEnabled, boolean isEnabledInspireExtension,
                                                ServiceProviderType provider );

    QName[] getDefaultTypeNames();

    URL getSchema( QName typeName )
                            throws MalformedURLException;

    List<URL> getSchemaReferences( QName typeName );

    Version checkVersion( Version requestVersion );

}
