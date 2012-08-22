//$$HeadURL$$
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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.WFServiceFactory;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;
import org.deegree.owscommon.OWSDomainType;
import org.xml.sax.SAXException;

/**
 * This class represents a local WFS dataSource object.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 */
public class LocalWFSDataSource extends AbstractDataSource {

    private static final ILogger LOG = LoggerFactory.getLogger( LocalWFSDataSource.class );

    private final PropertyPath geometryProperty;

    private int maxFeatures;

    private static Map<URL, WFSConfiguration> cache = new ConcurrentHashMap<URL, WFSConfiguration>();

    /**
     * Creates a new <code>LocalWFSDataSource</code> object from the given parameters.
     *
     * @param name
     * @param owsCapabilities
     * @param validArea
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param geomProperty
     * @param filterCondition
     *            a wfs query //TODO give an example //*
     * @param maxFeatures to query this datasource for.
     */
    public LocalWFSDataSource( QualifiedName name, OWSCapabilities owsCapabilities,
                               Surface validArea, double minScaleDenominator,
                               double maxScaleDenominator, PropertyPath geomProperty,
                               Filter filterCondition/* , FeatureCollectionAdapter adapter */, int maxFeatures) {

        super( LOCAL_WFS, name, owsCapabilities, validArea, minScaleDenominator,
               maxScaleDenominator, filterCondition );
        this.geometryProperty = geomProperty;
        this.maxFeatures = maxFeatures;
        // this.fcAdapter = adapter;
    }

    @Override
    public String toString() {
        return super.toString() +"\n\t" + geometryProperty.getAsString();
    }

    /**
     * @return the Filter of the filterCondition.
     */
    public Filter getFilter() {
        return (Filter) getFilterCondition();
    }

    /**
     * @return the geometryProperty.
     */
    public PropertyPath getGeometryProperty() {
        return geometryProperty;
    }

    @Override
    public OGCWebService getOGCWebService()
                            throws OGCWebServiceException {
        WFSConfiguration wfsConfig = null;
        synchronized ( this ) {

            URL url = getOwsCapabilities().getOnlineResource();
            wfsConfig = cache.get( url );
            if ( !cache.containsKey( url ) || wfsConfig == null ) {
                WFSConfigurationDocument wfsDoc = new WFSConfigurationDocument();
                try {
                    wfsDoc.load( getOwsCapabilities().getOnlineResource() );
                    wfsConfig = wfsDoc.getConfiguration();
                    cache.put( url, wfsConfig );
                } catch ( IOException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                } catch ( SAXException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                } catch ( InvalidConfigurationException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                }

            }
        }
        return WFServiceFactory.createInstance( wfsConfig );
    }

    // /**
    // * @return the configured FeatureCollectionAdapter.
    // */
    // public FeatureCollectionAdapter getFeatureCollectionAdapter() {
    // return fcAdapter;
    // }

    /**
     * ---DO NOT REMOVE --- NOT FUNCTIONAL YET, BUT might be if the WFS uses the new OWSCommon
     * Package.
     *
     * Retrieves (if it exists) the first value of the requestedParameterName of the Operation
     * defined by it's name. For example one wants to get GetFeature#outputFormat
     *
     * @param operationName
     *            the name of the configured Operation
     * @param requestedParameterName
     *            the name of the Parameter.
     * @return <code>null</code> - in the future: the Value of the (first) parameter if it exists else
     *         <code>null</code>.
     */
    @SuppressWarnings("unused")
    public String retrieveConfiguredValueForOperationOfNewOWSCommon( String operationName,
                                                                     String requestedParameterName ) {
        String result = null;
        /*
         * if( operationName == null || requestedParameterName == null )return null; OGCCapabilities
         * ogcCap = getOGCWebService().getCapabilities(); List<Operation> operations =
         * ((org.deegree.owscommon_new.OWSCommonCapabilities)ogcCap).getOperationsMetadata().getOperations();
         *
         * for( Operation operation : operations ){ if( operationName.equalsIgnoreCase(
         * operation.getName().getLocalName() ) ){ QualifiedName outputFormatName = new
         * QualifiedName( operation.getName().getPrefix(), requestedParameterName,
         * operation.getName().getNamespace() ); Parameter para = operation.getParameter(
         * outputFormatName ); if( para != null ){ if( para instanceof DomainType ){ List<TypedLiteral>
         * values = ((DomainType)para).getValues(); if( values.size() > 0 ){ outputFormat =
         * values.get(0).getValue(); } else { outputFormat =
         * ((DomainType)para).getDefaultValue().getValue(); } } } } }
         */
        return result;

    }

    /**
     * Retrieves (if it exists) the first value of the requestedParameterName of the Operation
     * defined by it's name. For example one wants to get GetFeature#outputFormat
     *
     * @param operationName
     *            the name of the configured Operation
     * @param requestedParameterName
     *            the name of the Parameter.
     * @return the Value of the (first) parameter if it exists else <code>null</code>.
     */
    public String retrieveConfiguredValueForOperation( String operationName,
                                                       String requestedParameterName ) {
        if ( operationName == null || requestedParameterName == null )
            return null;
        WFSConfiguration wfsConfig = null;
        synchronized ( this ) {
            URL url = getOwsCapabilities().getOnlineResource();
            wfsConfig = cache.get( url );
            if ( !cache.containsKey( url ) || wfsConfig == null ) {
                WFSConfigurationDocument wfsDoc = new WFSConfigurationDocument();
                try {
                    wfsDoc.load( getOwsCapabilities().getOnlineResource() );
                    wfsConfig = wfsDoc.getConfiguration();
                    cache.put( url, wfsConfig );
                } catch ( IOException e ) {
                    LOG.logError(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                    return null;
                } catch ( SAXException e ) {
                    LOG.logError(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                    return null;
                } catch ( InvalidConfigurationException e ) {
                    LOG.logError(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                    return null;
                }

            }
        }

        OWSDomainType[] operations = ( (org.deegree.owscommon.OWSCommonCapabilities) wfsConfig ).getOperationsMetadata().getParameter();
        for ( OWSDomainType operation : operations ) {
            if ( operationName.equalsIgnoreCase( operation.getName() ) ) {
                String[] values = operation.getValues();
                if ( values != null && values.length > 0 ) {
                    return values[0];
                }
            }
        }
        return null;
    }

    /**
     * returns the (first) value of the configured constraint (given by it's name) for this
     * WFSDataSource.
     *
     * @param constraintName
     *            the name of the constraint.
     * @return the value of the Constraint or <code>null</code> if no such constraint could be found.
     */
    public String retrieveConfiguredConstraintValue( String constraintName ) {
        if ( constraintName == null )
            return null;
        WFSConfiguration wfsConfig = null;
        synchronized ( this ) {
            URL url = getOwsCapabilities().getOnlineResource();
            wfsConfig = cache.get( url );
            if ( !cache.containsKey( url ) || wfsConfig == null ) {
                WFSConfigurationDocument wfsDoc = new WFSConfigurationDocument();
                try {
                    wfsDoc.load( getOwsCapabilities().getOnlineResource() );
                    wfsConfig = wfsDoc.getConfiguration();
                    cache.put( url, wfsConfig );
                } catch ( IOException e ) {
                    LOG.logError(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                    return null;
                } catch ( SAXException e ) {
                    LOG.logError(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                    return null;
                } catch ( InvalidConfigurationException e ) {
                    LOG.logError(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                    return null;
                }

            }
        }

        OWSDomainType[] constraints = ( (org.deegree.owscommon.OWSCommonCapabilities) wfsConfig ).getOperationsMetadata().getConstraints();
        for ( OWSDomainType constraint : constraints ) {
            if ( constraintName.equalsIgnoreCase( constraint.getName() ) ) {
                String[] values = constraint.getValues();
                if ( values != null && values.length > 0 ) {
                    return values[0];
                }
            }
        }
        return null;
    }

    /**
     * @return the configured MaxFeatures.
     */
    public final int getMaxFeatures() {
        return maxFeatures;
    }
}
