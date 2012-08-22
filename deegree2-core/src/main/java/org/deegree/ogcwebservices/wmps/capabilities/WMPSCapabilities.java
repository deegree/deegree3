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
package org.deegree.ogcwebservices.wmps.capabilities;

import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.UserDefinedSymbolization;

/**
 * The purpose of the GetCapabilities operation is described in the Basic Capabilities Service
 * Elements section, above. In the particular case of a Web Map Print Service Capabilities Service,
 * the response of a GetCapabilities request is general information about the service itself and
 * specific information about the available maps.
 *
 * The available output formats and the online resource are listed for each operation offered by the
 * server,

 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @version 2.0
 */
public class WMPSCapabilities extends OGCCapabilities {

    private static final long serialVersionUID = -5173204173366244735L;

    private ServiceIdentification serviceIdentification;

    private ServiceProvider serviceProvider;

    private OperationsMetadata operationMetadata;

    private UserDefinedSymbolization userDefinedSymbolization;

    private Layer layer;

    /**
     * Constructor initializing the class with the <code>WMPSCapabilities</code>
     *
     * @param version
     * @param serviceIdentification
     * @param serviceProvider
     * @param userDefinedSymbolization
     * @param metadata
     * @param layer
     */
    protected WMPSCapabilities( String version, ServiceIdentification serviceIdentification,
                               ServiceProvider serviceProvider,
                               UserDefinedSymbolization userDefinedSymbolization,
                               OperationsMetadata metadata, Layer layer ) {
        super( version, null );
        setServiceProvider( serviceProvider );
        setServiceIdentification( serviceIdentification );
        setUserDefinedSymbolization( userDefinedSymbolization );
        setOperationMetadata( metadata );
        setLayer( layer );
    }

    /**
     * returns the service description section
     *
     * @return ServiceIdentification
     */
    public ServiceIdentification getServiceIdentification() {
        return this.serviceIdentification;
    }

    /**
     * the service description section
     *
     * @param serviceIdentification
     */
    public void setServiceIdentification( ServiceIdentification serviceIdentification ) {
        this.serviceIdentification = serviceIdentification;
    }

    /**
     * returns the root layer provided by a WMPS
     *
     * @return Layer
     */
    public Layer getLayer() {
        return this.layer;
    }

    /**
     * recursion over all layers to find the layer that matches the submitted name. If no layer can
     * be found that fullfills the condition <tt>null</tt> will be returned.
     *
     * @param name
     * @return Layer
     */
    public Layer getLayer( String name ) {
        Layer lay = null;

        if ( name.equals( this.layer.getName() ) ) {
            lay = this.layer;
        } else {
            lay = getLayer( name, this.layer.getLayer() );
        }

        return lay;
    }

    /**
     * returns the layer provided by a WMPS for the submitted name. If not found null will be
     * returned.
     *
     * @param name
     *            name of the layer to be found
     * @param layers
     *            list of searchable layers
     *
     * @return a layer object or <tt>null</tt>
     */
    private Layer getLayer( String name, Layer[] layers ) {
        Layer lay = null;

        if ( layers != null ) {
            for ( int i = 0; i < layers.length; i++ ) {
                if ( name.equals( layers[i].getName() ) ) {
                    lay = layers[i];
                    break;
                }
                lay = getLayer( name, layers[i].getLayer() );
                if ( lay != null ) break;
            }
        }

        return lay;
    }

    /**
     * sets the root layer provided by a WMPS
     *
     * @param layer
     */
    public void setLayer( Layer layer ) {
        this.layer = layer;
    }

    /**
     * returns metadata about the offered access methods like PrintMap or GetCapabiliites
     *
     * @return OperationsMetadata
     */
    public OperationsMetadata getOperationMetadata() {
        return this.operationMetadata;
    }

    /**
     * sets metadata for the offered access methods like PrintMap or GetCapabiliites
     *
     * @param operationMetadata
     */
    public void setOperationMetadata( OperationsMetadata operationMetadata ) {
        this.operationMetadata = operationMetadata;
    }

    /**
     * returns informations about the provider of a WMPS
     *
     * @return ServiceProvider
     */
    public ServiceProvider getServiceProvider() {
        return this.serviceProvider;
    }

    /**
     * sets informations about the provider of a WMPS
     *
     * @param serviceProvider
     */
    public void setServiceProvider( ServiceProvider serviceProvider ) {
        this.serviceProvider = serviceProvider;
    }

    public UserDefinedSymbolization getUserDefinedSymbolization() {
        return this.userDefinedSymbolization;
    }

    public void setUserDefinedSymbolization( UserDefinedSymbolization userDefinedSymbolization ) {
        this.userDefinedSymbolization = userDefinedSymbolization;
    }
}
