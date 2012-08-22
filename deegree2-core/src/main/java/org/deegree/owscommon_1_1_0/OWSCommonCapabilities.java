//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.owscommon_1_1_0;

import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;

/**
 * <code>OWSCommonCapabilities</code> encapsulates the serviceIdentification, serviceProvider and the
 * operationsMetadata representations of the ows common version 1.1.0.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class OWSCommonCapabilities extends OGCCapabilities {
    /**
     * for inter operability
     */
    private static final long serialVersionUID = 799308405149136156L;

    private final ServiceIdentification serviceIdentification;

    private final ServiceProvider serviceProvider;

    private final OperationsMetadata operationsMetadata;

    /**
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     */
    public OWSCommonCapabilities( String version, String updateSequence, ServiceIdentification serviceIdentification,
                                  ServiceProvider serviceProvider, OperationsMetadata operationsMetadata ) {
        super( version, updateSequence );
        this.serviceIdentification = serviceIdentification;
        this.serviceProvider = serviceProvider;
        this.operationsMetadata = operationsMetadata;

    }

    /**
     * @return the serviceIdentification.
     */
    public final ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * @return the serviceProvider.
     */
    public final ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * @return the operationsMetadata.
     */
    public final OperationsMetadata getOperationsMetadata() {
        return operationsMetadata;
    }

}
