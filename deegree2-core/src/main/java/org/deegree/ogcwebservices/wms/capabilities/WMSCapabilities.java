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
package org.deegree.ogcwebservices.wms.capabilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.owscommon_new.ServiceIdentification;
import org.deegree.owscommon_new.ServiceProvider;

/**
 * <code>WMSCapabilities</code> is the data class for the WMS version of capabilities. Since WMS is not yet using the
 * OWS commons implementation, it is more or less just a copy of the old version.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$
 */
public class WMSCapabilities extends OGCCapabilities {

    private static final long serialVersionUID = -6040994669604563061L;

    private static final ILogger LOG = LoggerFactory.getLogger( WMSCapabilities.class );

    private ServiceIdentification serviceIdentification = null;

    private ServiceProvider serviceProvider = null;

    private UserDefinedSymbolization userDefinedSymbolization = null;

    private OperationsMetadata operationMetadata = null;

    private Layer layer = null;

    protected List<String> exceptions;

    /**
     * constructor initializing the class with the <code>WMSCapabilities</code>
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param userDefinedSymbolization
     * @param metadata
     * @param layer
     */
    protected WMSCapabilities( String version, String updateSequence, ServiceIdentification serviceIdentification,
                               ServiceProvider serviceProvider, UserDefinedSymbolization userDefinedSymbolization,
                               OperationsMetadata metadata, Layer layer ) {
        super( version, updateSequence );

        setServiceProvider( serviceProvider );
        setServiceIdentification( serviceIdentification );
        setUserDefinedSymbolization( userDefinedSymbolization );
        setOperationMetadata( metadata );
        setLayer( layer );
        exceptions = new LinkedList<String>();
        if ( version.equals( "1.3.0" ) ) {
            exceptions.add( "XML" );
        } else {
            exceptions.add( "application/vnd.ogc.se_xml" );
        }
    }

    /**
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param userDefinedSymbolization
     * @param metadata
     * @param layer
     * @param exceptions
     */
    protected WMSCapabilities( String version, String updateSequence, ServiceIdentification serviceIdentification,
                               ServiceProvider serviceProvider, UserDefinedSymbolization userDefinedSymbolization,
                               OperationsMetadata metadata, Layer layer, List<String> exceptions ) {
        this( version, updateSequence, serviceIdentification, serviceProvider, userDefinedSymbolization, metadata,
              layer );
        this.exceptions = exceptions;
    }

    /**
     *
     * @return the service description section
     */
    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * sets the service description section
     *
     * @param serviceIdentification
     */
    public void setServiceIdentification( ServiceIdentification serviceIdentification ) {
        this.serviceIdentification = serviceIdentification;
    }

    /**
     *
     * @return the root layer provided by a WMS
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     *
     * @param name
     *            the layer name
     * @return the layer provided by a WMS, or null, if there is no such layer
     */
    public Layer getLayer( String name ) {
        Layer lay = null;
        if ( layer.getName() != null && name.equals( layer.getName() ) ) {
            lay = layer;
        } else {
            lay = getLayer( name, layer.getLayer() );
        }
        return lay;
    }

    /**
     * recursion over all layers to find the layer that matches the submitted name. If no layer can be found that
     * fullfills the condition <tt>null</tt> will be returned.
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
                if ( lay != null )
                    break;
            }
        }

        return lay;
    }

    /**
     * returns the
     *
     * @see Layer identified by its title. If no layer matching the passed title can be found <code>null</code> will
     *      be returned.
     *
     * @param title
     * @return the layer
     */
    public Layer getLayerByTitle( String title ) {
        Layer lay = null;
        if ( title.equals( layer.getTitle() ) ) {
            lay = layer;
        } else {
            lay = getLayerByTitle( title, layer.getLayer() );
        }
        return lay;
    }

    /**
     * recursion over all layers to find the layer that matches the passed title. If no layer can be found that
     * fullfills the condition <tt>null</tt> will be returned.
     *
     * @param title
     *            of the layer to be found
     * @param layers
     *            list of searchable layers
     *
     * @return a layer object or <tt>null</tt>
     */
    private Layer getLayerByTitle( String title, Layer[] layers ) {
        Layer lay = null;

        if ( layers != null ) {
            for ( int i = 0; i < layers.length; i++ ) {
                if ( title.equals( layers[i].getTitle() ) ) {
                    lay = layers[i];
                    break;
                }
                lay = getLayer( title, layers[i].getLayer() );
                if ( lay != null )
                    break;
            }
        }

        return lay;
    }

    /**
     * @param layer
     * @param layers
     * @return a layer name, if a layer has been defined two times with the same name, null otherwise
     */
    public static String hasDoubleLayers( Layer layer, Set<String> layers ) {

        if ( layers.contains( layer.getName() ) ) {
            return layer.getName();
        }

        layers.add( layer.getName() );

        for ( Layer lay : layer.getLayer() ) {
            String dl = hasDoubleLayers( lay, layers );
            if ( dl != null ) {
                return dl;
            }
        }
        return null;

    }

    /**
     * sets the root layer provided by a WMS
     *
     * @param layer
     */
    public void setLayer( Layer layer ) {
        String dl = hasDoubleLayers( layer, new HashSet<String>() );
        if ( dl != null ) {
            LOG.logWarning( Messages.getMessage( "WMS_REDEFINED_LAYER", dl ) );
            // throw new IllegalArgumentException( Messages.getMessage( "WMS_REDEFINED_LAYER", dl )
            // );
        }
        this.layer = layer;
    }

    /**
     *
     * @return metadata about the offered access methods like GetMap or GetCapabilities
     */
    public OperationsMetadata getOperationMetadata() {
        return operationMetadata;
    }

    /**
     * sets metadata for the offered access methods like GetMap or GetCapabiliites
     *
     * @param operationMetadata
     */
    public void setOperationMetadata( OperationsMetadata operationMetadata ) {
        this.operationMetadata = operationMetadata;
    }

    /**
     * @return the list of exceptions
     */
    public List<String> getExceptions() {
        return Collections.unmodifiableList( exceptions );
    }

    /**
     *
     * @return informations about the provider of a WMS
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * sets informations about the provider of a WMS
     *
     * @param serviceProvider
     */
    public void setServiceProvider( ServiceProvider serviceProvider ) {
        this.serviceProvider = serviceProvider;
    }

    /**
     * @return the user defined symbolization
     */
    public UserDefinedSymbolization getUserDefinedSymbolization() {
        return userDefinedSymbolization;
    }

    /**
     * @param userDefinedSymbolization
     */
    public void setUserDefinedSymbolization( UserDefinedSymbolization userDefinedSymbolization ) {
        this.userDefinedSymbolization = userDefinedSymbolization;
    }
}
