//$HeadURL$
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
package org.deegree.protocol.ows.metadata;

import org.deegree.commons.tom.ows.Version;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ServiceMetadata {

    private ServiceIdentification serviceIdentificaiton;

    private ServiceProvider serviceProvider;

    private OperationsMetadata operationsMetadata;

    private Version version;

    private String updateSequence;

    public void setServiceIdentification( ServiceIdentification serviceIdentificaiton ) {
        this.serviceIdentificaiton = serviceIdentificaiton;
    }

    public ServiceIdentification getServiceIdentification() {
        return serviceIdentificaiton;
    }

    public void setServiceProvider( ServiceProvider serviceProvider ) {
        this.serviceProvider = serviceProvider;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setOperationsMetadata( OperationsMetadata operationsMetadata ) {
        this.operationsMetadata = operationsMetadata;
    }

    public OperationsMetadata getOperationsMetadata() {
        return operationsMetadata;
    }

    public void setVersion( Version version ) {
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    public void setUpdateSequence( String updateSequence ) {
        this.updateSequence = updateSequence;
    }

    public String getUpdateSequence() {
        return updateSequence;
    }

}
