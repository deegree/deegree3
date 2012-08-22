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
package org.deegree.owscommon;

import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;

/**
 * Represents a configuration for an OGC-Webservice according to the OWS Common Implementation
 * Specification 0.2, i.e. it consists of the following parts:
 * <ul>
 * <li>ServiceIdentification (corresponds to and expands the SV_ServiceIdentification class in ISO
 * 19119)
 * <li>ServiceProvider (corresponds to and expands the SV_ServiceProvider class in ISO 19119)
 * <li>OperationsMetadata (contains set of Operation elements that each corresponds to and expand
 * the SV_OperationsMetadata class in ISO 19119)
 * <li>Contents (whenever relevant, contains set of elements that each corresponds to the
 * MD_DataIdentification class in ISO 19119 and 19115)
 * </ul>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class OWSCommonCapabilities extends OGCCapabilities {

    private ServiceIdentification serviceIdentification;

    private ServiceProvider serviceProvider;

    private OperationsMetadata operationsMetadata;

    private Contents contents;

    /**
     * Constructor to be used from the implementing subclasses.
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     */
    protected OWSCommonCapabilities( String version, String updateSequence,
                                     ServiceIdentification serviceIdentification, ServiceProvider serviceProvider,
                                     OperationsMetadata operationsMetadata, Contents contents ) {
        super( version, updateSequence );
        this.serviceIdentification = serviceIdentification;
        this.serviceProvider = serviceProvider;
        this.operationsMetadata = operationsMetadata;
        this.contents = contents;
    }

    /**
     * @return Returns the contents.
     */
    public Contents getContents() {
        return contents;
    }

    /**
     * @param contents
     *            The contents to set.
     */
    public void setContents( Contents contents ) {
        this.contents = contents;
    }

    /**
     * @return Returns the operationsMetadata.
     */
    public OperationsMetadata getOperationsMetadata() {
        return operationsMetadata;
    }

    /**
     * @param operationsMetadata
     *            The operationsMetadata to set.
     */
    public void setOperationsMetadata( OperationsMetadata operationsMetadata ) {
        this.operationsMetadata = operationsMetadata;
    }

    /**
     * @return Returns the serviceIdentification.
     */
    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * @param serviceIdentification
     *            The serviceIdentification to set.
     */
    public void setServiceIdentification( ServiceIdentification serviceIdentification ) {
        this.serviceIdentification = serviceIdentification;
    }

    /**
     * @return Returns the serviceProvider.
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * @param serviceProvider
     *            The serviceProvider to set.
     */
    public void setServiceProvider( ServiceProvider serviceProvider ) {
        this.serviceProvider = serviceProvider;
    }

}
