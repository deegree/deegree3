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
package org.deegree.services.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.commons.tom.ows.Version;

/**
 * This class contains metadata information on the details of an implementation of a {@link AbstractOGCServiceController}.
 * <p>
 * Contained information:
 * <ul>
 * <li>OGC versions supported by the implementation</li>
 * <li>Names of requests that are supported by the implementation</li>
 * <li>Namespaces used by XML requests</li>
 * <li>Supported configuration file versions.</li>
 * </ul>
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 */
public abstract class ImplementationMetadata<T extends Enum<?>> {

    /**
     * The supportedVersions of this service implementation.
     */
    protected Version[] supportedVersions;

    /**
     * The supportedConfigVersions of this service implementation.
     */
    protected Version[] supportedConfigVersions;

    /**
     * The namespaces of this service.
     */
    protected String[] handledNamespaces;

    /**
     * should the cite test mode be enabled, meaning, that requests should be in the same case as they were defined in
     * the specification?
     */
    protected boolean citeTestMode = false;

    /**
     * An enum with all supported request names.
     */
    protected Class<T> handledRequests;

    private final Map<String, T> handledRequestsMap = new HashMap<String, T>();

    private final Set<String> handledNamespacesSet = new HashSet<String>();

    private final Set<Version> implementedVersionsSet = new HashSet<Version>();

    private final Set<Version> supportedConfigVersionsSet = new HashSet<Version>();

    private String[] retrieveRequestNames() {
        T[] enums = handledRequests.getEnumConstants();
        String[] result = new String[enums.length];
        int i = 0;
        for ( T e : enums ) {
            result[i++] = e.name();
            handledRequestsMap.put( e.name(), e );
        }
        return result;
    }

    /**
     * Returns the (local) names of the requests handled by the associated controller.
     * 
     * @return the (local) names of the handled requests
     */
    public Set<String> getHandledRequests() {
        if ( handledRequestsMap.isEmpty() && this.handledRequests != null ) {
            retrieveRequestNames();
        }
        return handledRequestsMap.keySet();
    }

    /**
     * Returns the namespaces of request elements handled by the associated controller.
     * 
     * @return the namespaces of the handled requests
     */
    public Set<String> getHandledNamespaces() {
        if ( handledNamespacesSet.size() == 0 && this.handledNamespaces != null ) {
            for ( String ns : this.handledNamespaces ) {
                this.handledNamespacesSet.add( ns );
            }
        }
        return handledNamespacesSet;
    }

    /**
     * Returns the OGC versions supported by the associated controller implementation.
     * 
     * @return the supported OGC versions
     */
    public Set<Version> getImplementedVersions() {
        if ( implementedVersionsSet.size() == 0 && this.supportedVersions != null ) {
            for ( Version version : this.supportedVersions ) {
                this.implementedVersionsSet.add( version );
            }
        }
        return implementedVersionsSet;
    }

    /**
     * Returns the configuration file versions supported by the associated controller implementation.
     * 
     * @return the supported configuration file versions
     */
    public Set<Version> getSupportedConfigVersions() {
        if ( supportedConfigVersionsSet.size() == 0 && this.supportedConfigVersions != null ) {
            for ( Version version : this.supportedConfigVersions ) {
                this.supportedConfigVersionsSet.add( version );
            }
        }
        return supportedConfigVersionsSet;
    }

    /**
     * find an enum type by a given name ignoring case, or if the citetest mode is enabled map perfectly.
     * 
     * @param requestName
     * @return the Enum type or <code>null</code> if the request was not found.
     */
    public T getRequestTypeByName( String requestName ) {
        T requestType = null;
        if ( requestName != null ) {
            if ( citeTestMode ) {
                requestType = handledRequestsMap.get( requestName );
            } else {
                for ( String req : getHandledRequests() ) {
                    if ( req.equalsIgnoreCase( requestName ) ) {
                        requestType = handledRequestsMap.get( req );
                        break;
                    }
                }
            }
        }
        return requestType;
    }
}
