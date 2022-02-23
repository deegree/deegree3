//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/wps/WPSController.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.services.wfs;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.lock.LockHasExpiredException;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.matcher.BaseUrlReferencePatternMatcher;
import org.deegree.gml.reference.matcher.MultipleReferencePatternMatcher;
import org.deegree.gml.reference.matcher.ReferencePatternMatcher;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.protocol.wfs.capabilities.GetCapabilitiesXMLAdapter;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.describefeaturetype.kvp.DescribeFeatureTypeKVPAdapter;
import org.deegree.protocol.wfs.describefeaturetype.xml.DescribeFeatureTypeXMLAdapter;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.kvp.GetFeatureKVPAdapter;
import org.deegree.protocol.wfs.getfeature.xml.GetFeatureXMLAdapter;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.getfeaturewithlock.kvp.GetFeatureWithLockKVPAdapter;
import org.deegree.protocol.wfs.getfeaturewithlock.xml.GetFeatureWithLockXMLAdapter;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.getgmlobject.kvp.GetGmlObjectKVPAdapter;
import org.deegree.protocol.wfs.getgmlobject.xml.GetGmlObjectXMLAdapter;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.protocol.wfs.getpropertyvalue.kvp.GetPropertyValueKVPAdapter;
import org.deegree.protocol.wfs.getpropertyvalue.xml.GetPropertyValueXMLAdapter;
import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.deegree.protocol.wfs.lockfeature.kvp.LockFeatureKVPAdapter;
import org.deegree.protocol.wfs.lockfeature.xml.LockFeatureXMLAdapter;
import org.deegree.protocol.wfs.storedquery.CreateStoredQuery;
import org.deegree.protocol.wfs.storedquery.DescribeStoredQueries;
import org.deegree.protocol.wfs.storedquery.DropStoredQuery;
import org.deegree.protocol.wfs.storedquery.ListStoredQueries;
import org.deegree.protocol.wfs.storedquery.kvp.DescribeStoredQueriesKVPAdapter;
import org.deegree.protocol.wfs.storedquery.kvp.DropStoredQueryKVPAdapter;
import org.deegree.protocol.wfs.storedquery.kvp.ListStoredQueriesKVPAdapter;
import org.deegree.protocol.wfs.storedquery.xml.CreateStoredQueryXMLAdapter;
import org.deegree.protocol.wfs.storedquery.xml.DescribeStoredQueriesXMLAdapter;
import org.deegree.protocol.wfs.storedquery.xml.DropStoredQueryXMLAdapter;
import org.deegree.protocol.wfs.storedquery.xml.ListStoredQueriesXMLAdapter;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.kvp.TransactionKVPAdapter;
import org.deegree.protocol.wfs.transaction.xml.TransactionXmlReader;
import org.deegree.protocol.wfs.transaction.xml.TransactionXmlReaderFactory;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.encoding.LimitedSupportedEncodings;
import org.deegree.services.encoding.SupportedEncodings;
import org.deegree.services.encoding.UnlimitedSupportedEncodings;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.wfs.AbstractFormatType;
import org.deegree.services.jaxb.wfs.CustomFormat;
import org.deegree.services.jaxb.wfs.DeegreeWFS;
import org.deegree.services.jaxb.wfs.DeegreeWFS.EnableTransactions;
import org.deegree.services.jaxb.wfs.DeegreeWFS.ExtendedCapabilities;
import org.deegree.services.jaxb.wfs.DeegreeWFS.SupportedRequests;
import org.deegree.services.jaxb.wfs.DeegreeWFS.SupportedVersions;
import org.deegree.services.jaxb.wfs.DisabledResources;
import org.deegree.services.jaxb.wfs.FeatureTypeMetadata;
import org.deegree.services.jaxb.wfs.GMLFormat;
import org.deegree.services.jaxb.wfs.IdentifierGenerationOptionType;
import org.deegree.services.jaxb.wfs.RequestType;
import org.deegree.services.metadata.MetadataUtils;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.DefaultOWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.services.ows.OWS100ExceptionReportSerializer;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.deegree.services.ows.PreOWSExceptionReportSerializer;
import org.deegree.services.wfs.format.Format;
import org.deegree.services.wfs.query.StoredQueryHandler;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.lang.StringUtils.trim;
import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.LOCK_HAS_EXPIRED;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.commons.utils.StringUtils.REMOVE_DOUBLE_FIELDS;
import static org.deegree.commons.utils.StringUtils.REMOVE_EMPTY_FIELDS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_30;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSRequestType.CreateStoredQuery;
import static org.deegree.protocol.wfs.WFSRequestType.DescribeFeatureType;
import static org.deegree.protocol.wfs.WFSRequestType.DescribeStoredQueries;
import static org.deegree.protocol.wfs.WFSRequestType.DropStoredQuery;
import static org.deegree.protocol.wfs.WFSRequestType.GetCapabilities;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeature;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeatureWithLock;
import static org.deegree.protocol.wfs.WFSRequestType.GetGmlObject;
import static org.deegree.protocol.wfs.WFSRequestType.GetPropertyValue;
import static org.deegree.protocol.wfs.WFSRequestType.ListStoredQueries;
import static org.deegree.protocol.wfs.WFSRequestType.LockFeature;
import static org.deegree.protocol.wfs.WFSRequestType.Transaction;
import static org.deegree.protocol.wfs.getfeature.ResultType.HITS;
import static org.deegree.services.jaxb.wfs.IdentifierGenerationOptionType.USE_EXISTING_RESOLVING_REFERENCES_INTERNALLY;
import static org.deegree.services.jaxb.wfs.IdentifierGenerationOptionType.USE_EXISTING_SKIP_RESOLVING_REFERENCES;
import static org.deegree.services.wfs.ReferenceResolvingMode.CHECK_ALL;
import static org.deegree.services.wfs.ReferenceResolvingMode.CHECK_INTERNALLY;
import static org.deegree.services.wfs.ReferenceResolvingMode.SKIP_ALL;

/**
 * Implementation of the <a href="http://www.opengeospatial.org/standards/wfs">OpenGIS Web Feature Service</a> server
 * protocol.
 * <p>
 * Supported WFS protocol versions:
 * <ul>
 * <li>1.0.0</li>
 * <li>1.1.0</li>
 * <li>2.0.0</li>
 * </ul>
 * </p>
 * 
 * @see AbstractOWS
 * @see OGCFrontController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 15339 $, $Date: 2008-12-11 18:40:09 +0100 (Do, 11 Dez 2008) $
 */
public class WebFeatureService extends AbstractOWS {

    private static final Logger LOG = LoggerFactory.getLogger( WebFeatureService.class );

    private static final int DEFAULT_MAX_FEATURES = 15000;
    
    private WfsFeatureStoreManager service;

    private LockFeatureHandler lockFeatureHandler;

    private StoredQueryHandler storedQueryHandler;

    private boolean enableTransactions;

    private IDGenMode idGenMode;

    private boolean disableBuffering = true;

    private ICRS defaultQueryCRS = CRSUtils.EPSG_4326;

    private List<ICRS> queryCRS = new ArrayList<ICRS>();

    private final Map<String, Format> mimeTypeToFormat = new LinkedHashMap<String, Format>();

    private final Map<GMLVersion, Format> gmlVersionToFormat = new HashMap<GMLVersion, Format>();

    private SupportedEncodings supportedEncodings;

    private int queryMaxFeatures;

    private BigInteger resolveTimeOutInSeconds;

    private boolean checkAreaOfUse;

    private boolean enableResponsePaging;

    private ReferenceResolvingMode referenceResolvingMode = CHECK_ALL;

    private OWSMetadataProvider mdProvider;

    private ReferencePatternMatcher referencePatternMatcher;

    private boolean isStrict;

    public WebFeatureService( ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig ) {
        super( metadata, workspace, jaxbConfig );
    }

    @Override
    public void init( DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConf,
                      Object controllerConf ) {

        LOG.info( "Initializing WFS." );

        DeegreeWFS jaxbConfig = (DeegreeWFS) controllerConf;
        initOfferedVersions( jaxbConfig.getSupportedVersions() );

        isStrict = jaxbConfig.isStrict() != null ? jaxbConfig.isStrict() : false;
        EnableTransactions enableTransactions = jaxbConfig.getEnableTransactions();
        if ( enableTransactions != null ) {
            this.enableTransactions = enableTransactions.isValue();
            IdentifierGenerationOptionType configuredIdGenMode = enableTransactions.getIdGen();
            this.idGenMode = parseIdGenMode( configuredIdGenMode );
            if ( USE_EXISTING_RESOLVING_REFERENCES_INTERNALLY.equals( configuredIdGenMode ) )
                this.referenceResolvingMode = CHECK_INTERNALLY;
            if ( USE_EXISTING_SKIP_RESOLVING_REFERENCES.equals( configuredIdGenMode ) )
                this.referenceResolvingMode = SKIP_ALL;
        }
        if ( jaxbConfig.isEnableResponseBuffering() != null ) {
            disableBuffering = !jaxbConfig.isEnableResponseBuffering();
        } else if ( jaxbConfig.isDisableResponseBuffering() != null ) {
            disableBuffering = jaxbConfig.isDisableResponseBuffering();
        }

        queryMaxFeatures = jaxbConfig.getQueryMaxFeatures() == null ? DEFAULT_MAX_FEATURES
                                                                   : jaxbConfig.getQueryMaxFeatures().intValue();
        resolveTimeOutInSeconds = jaxbConfig.getResolveTimeOutInSeconds();
        checkAreaOfUse = jaxbConfig.isQueryCheckAreaOfUse() == null ? false : jaxbConfig.isQueryCheckAreaOfUse();
        enableResponsePaging = jaxbConfig.isEnableResponsePaging() == null ? false
                                                                          : jaxbConfig.isEnableResponsePaging();

        service = new WfsFeatureStoreManager();
        try {
            service.init( jaxbConfig, workspace );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Error initializing WFS/FeatureStores: " + e.getMessage(), e );
        }

        lockFeatureHandler = new LockFeatureHandler( this );
        List<URL> list = new ArrayList<URL>();
        for ( String file : jaxbConfig.getStoredQuery() ) {
            URL url = metadata.getLocation().resolveToUrl( file );
            if ( url == null ) {
                LOG.warn( "Could not resolve {}.", file );
            } else {
                list.add( url );
            }
        }
        File managedStoredQueryDirectory = metadata.getLocation().resolveToFile( "../storedqueries/managed" );
        storedQueryHandler = new StoredQueryHandler( this, list, managedStoredQueryDirectory );

        initQueryCRS( jaxbConfig.getQueryCRS() );
        initFormats( jaxbConfig.getAbstractFormat() );
        mdProvider = initMetadataProvider( serviceMetadata, jaxbConfig );

        supportedEncodings = parseEncodings( jaxbConfig );

        referencePatternMatcher = parseDisabledResources( jaxbConfig );
    }

    SupportedEncodings parseEncodings( DeegreeWFS jaxbConfig ) {
        SupportedRequests supportedRequests = jaxbConfig.getSupportedRequests();
        if ( supportedRequests != null ) {
            if ( isAtLeastOneRequestTypeConfigured( supportedRequests )
                 || isGlobalSupportedEncodingsConfigured( supportedRequests ) )
                return parseEncodings( supportedRequests );
        }
        return new UnlimitedSupportedEncodings();
    }

    private ReferencePatternMatcher parseDisabledResources( DeegreeWFS jaxbConfig ) {
        DisabledResources disabledResources = jaxbConfig.getDisabledResources();
        if ( disabledResources != null && !disabledResources.getPattern().isEmpty() ) {
            MultipleReferencePatternMatcher matcher = new MultipleReferencePatternMatcher();
            List<String> patterns = disabledResources.getPattern();
            for ( String pattern : patterns ) {
                BaseUrlReferencePatternMatcher baseUrlMatcher = new BaseUrlReferencePatternMatcher( pattern );
                matcher.addMatcherToApply( baseUrlMatcher );
            }
            return matcher;
        }
        return null;
    }

    private LimitedSupportedEncodings parseEncodings( SupportedRequests supportedRequests ) {
        List<String> supportedEncodingsForAllRequestTypes = supportedRequests.getSupportedEncodings();
        if ( isAtLeastOneRequestTypeConfigured( supportedRequests ) )
            return parseEncodingsWithSpecifiedRequestTypes( supportedRequests, supportedEncodingsForAllRequestTypes );
        else
            return parseEncodingWithSupportedEncodings( supportedEncodingsForAllRequestTypes );
    }

    private LimitedSupportedEncodings parseEncodingWithSupportedEncodings( List<String> supportedEncodingsForAllRequestTypes ) {
        LimitedSupportedEncodings<WFSRequestType> limitedSupportedEncodings = new LimitedSupportedEncodings();
        limitedSupportedEncodings.addEnabledEncodings( CreateStoredQuery,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( DescribeFeatureType,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( DescribeStoredQueries,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( DropStoredQuery,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetCapabilities,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetFeature,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetFeatureWithLock,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetGmlObject,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetPropertyValue,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( ListStoredQueries,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( LockFeature,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( Transaction,
                                                       collectEnabledEncodings( supportedEncodingsForAllRequestTypes ) );
        return limitedSupportedEncodings;
    }

    private LimitedSupportedEncodings parseEncodingsWithSpecifiedRequestTypes( SupportedRequests supportedRequests,
                                                                               List<String> supportedEncodingsForAllRequestTypes ) {
        LimitedSupportedEncodings<WFSRequestType> limitedSupportedEncodings = new LimitedSupportedEncodings();
        limitedSupportedEncodings.addEnabledEncodings( CreateStoredQuery,
                                                       collectEnabledEncodings( supportedRequests.getCreateStoredQuery(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( DescribeFeatureType,
                                                       collectEnabledEncodings( supportedRequests.getDescribeFeatureType(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( DescribeStoredQueries,
                                                       collectEnabledEncodings( supportedRequests.getDescribeStoredQueries(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( DropStoredQuery,
                                                       collectEnabledEncodings( supportedRequests.getDropStoredQuery(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetCapabilities,
                                                       collectEnabledEncodings( supportedRequests.getGetCapabilities(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetFeature,
                                                       collectEnabledEncodings( supportedRequests.getGetFeature(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetFeatureWithLock,
                                                       collectEnabledEncodings( supportedRequests.getGetFeatureWithLock(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetGmlObject,
                                                       collectEnabledEncodings( supportedRequests.getGetGmlObject(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( GetPropertyValue,
                                                       collectEnabledEncodings( supportedRequests.getGetPropertyValue(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( ListStoredQueries,
                                                       collectEnabledEncodings( supportedRequests.getListStoredQueries(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( LockFeature,
                                                       collectEnabledEncodings( supportedRequests.getLockFeature(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        limitedSupportedEncodings.addEnabledEncodings( Transaction,
                                                       collectEnabledEncodings( supportedRequests.getTransaction(),
                                                                                supportedEncodingsForAllRequestTypes ) );
        return limitedSupportedEncodings;
    }

    private boolean isGlobalSupportedEncodingsConfigured( SupportedRequests supportedRequests ) {
        List<String> supportedEncodingsForAllRequestTypes = supportedRequests.getSupportedEncodings();
        return supportedEncodingsForAllRequestTypes != null && !supportedEncodingsForAllRequestTypes.isEmpty();
    }

    private boolean isAtLeastOneRequestTypeConfigured( SupportedRequests supportedRequests ) {
        return supportedRequests.getCreateStoredQuery() != null || supportedRequests.getDescribeFeatureType() != null
               || supportedRequests.getDescribeStoredQueries() != null
               || supportedRequests.getDropStoredQuery() != null || supportedRequests.getGetCapabilities() != null
               || supportedRequests.getGetFeature() != null || supportedRequests.getGetFeatureWithLock() != null
               || supportedRequests.getGetGmlObject() != null || supportedRequests.getGetPropertyValue() != null
               || supportedRequests.getListStoredQueries() != null || supportedRequests.getLockFeature() != null
               || supportedRequests.getTransaction() != null;
    }

    private Set<String> collectEnabledEncodings( RequestType supportedEncodingsForThisType,
                                                 List<String> supportedEncodingsForAllTypes ) {
        Set<String> allEnabledEncodingForThisType = new HashSet<String>();
        if ( supportedEncodingsForThisType != null ) {
            allEnabledEncodingForThisType.addAll( supportedEncodingsForAllTypes );
            List<String> encodingsForThisType = supportedEncodingsForThisType.getSupportedEncodings();
            if ( encodingsForThisType != null && encodingsForThisType.size() > 0 ) {
                allEnabledEncodingForThisType.addAll( encodingsForThisType );
            } else if ( supportedEncodingsForAllTypes == null || supportedEncodingsForAllTypes.isEmpty() ) {
                allEnabledEncodingForThisType.add( "kvp" );
                allEnabledEncodingForThisType.add( "xml" );
                allEnabledEncodingForThisType.add( "soap" );
            }
        }
        return allEnabledEncodingForThisType;
    }

    private Set<String> collectEnabledEncodings( List<String> supportedEncodingsForAllTypes ) {
        Set<String> allEnabledEncodingForThisType = new HashSet<String>();
        allEnabledEncodingForThisType.addAll( supportedEncodingsForAllTypes );
        return allEnabledEncodingForThisType;
    }

    private IDGenMode parseIdGenMode( IdentifierGenerationOptionType idGen ) {
        if ( idGen == null ) {
            return IDGenMode.GENERATE_NEW;
        }
        switch ( idGen ) {
        case GENERATE_NEW: {
            return IDGenMode.GENERATE_NEW;
        }
        case USE_EXISTING_SKIP_RESOLVING_REFERENCES: {
            return IDGenMode.USE_EXISTING.withSkipResolveReferences( true );
        }
        case USE_EXISTING_RESOLVING_REFERENCES_INTERNALLY:
        case USE_EXISTING: {
            return IDGenMode.USE_EXISTING;
        }
        case REPLACE_DUPLICATE: {
            return IDGenMode.REPLACE_DUPLICATE;
        }
        }
        return null;
    }

    private String getMetadataURL( String metadataUrlTemplate, FeatureTypeMetadata ftMd ) {
        if ( metadataUrlTemplate == null || ftMd == null || ftMd.getMetadataSetId() == null ) {
            return null;
        }
        return StringUtils.replaceAll( metadataUrlTemplate, "${metadataSetId}", ftMd.getMetadataSetId() );
    }

    private void initOfferedVersions( SupportedVersions supportedVersions ) {

        List<String> versions = null;
        if ( supportedVersions != null ) {
            versions = supportedVersions.getVersion();
        }
        if ( versions == null || versions.isEmpty() ) {
            LOG.info( "No protocol versions specified. Activating all implemented versions." );
            ImplementationMetadata<?> md = ( (OWSProvider) getMetadata().getProvider() ).getImplementationMetadata();
            versions = new ArrayList<String>( md.getImplementedVersions().size() );
            for ( Version version : md.getImplementedVersions() ) {
                versions.add( version.toString() );
            }
        }
        validateAndSetOfferedVersions( versions );
    }

    private void initQueryCRS( List<String> queryCRSLists ) {
        // try {
        for ( String queryCRS : queryCRSLists ) {
            String[] querySrs = StringUtils.split( queryCRS, " ", REMOVE_EMPTY_FIELDS | REMOVE_DOUBLE_FIELDS );
            for ( String srs : querySrs ) {
                LOG.debug( "Query CRS: " + srs );
                ICRS crs = CRSManager.getCRSRef( srs );
                this.queryCRS.add( crs );
            }
        }
        // } catch ( UnknownCRSException e ) {
        // String msg = "Invalid QuerySRS parameter: " + e.getMessage();
        // throw new ControllerInitException( msg );
        // }
        if ( queryCRS.isEmpty() ) {
            LOG.info( "No query CRS defined, defaulting to EPSG:4326." );
            queryCRS.add( CRSUtils.EPSG_4326 );
        }
        defaultQueryCRS = this.queryCRS.get( 0 );
    }

    private void initFormats( List<JAXBElement<? extends AbstractFormatType>> formatList ) {

        if ( formatList == null || formatList.isEmpty() ) {
            LOG.debug( "Using default format configuration." );
            org.deegree.services.wfs.format.gml.GmlFormat gml21 = new org.deegree.services.wfs.format.gml.GmlFormat(
                                                                                                                     this,
                                                                                                                     GML_2 );
            org.deegree.services.wfs.format.gml.GmlFormat gml30 = new org.deegree.services.wfs.format.gml.GmlFormat(
                                                                                                                     this,
                                                                                                                     GML_30 );
            org.deegree.services.wfs.format.gml.GmlFormat gml31 = new org.deegree.services.wfs.format.gml.GmlFormat(
                                                                                                                     this,
                                                                                                                     GML_31 );
            org.deegree.services.wfs.format.gml.GmlFormat gml32 = new org.deegree.services.wfs.format.gml.GmlFormat(
                                                                                                                     this,

                                                                                                                     GML_32 );
            mimeTypeToFormat.put( "application/gml+xml; version=2.1", gml21 );
            mimeTypeToFormat.put( "application/gml+xml; version=3.0", gml30 );
            mimeTypeToFormat.put( "application/gml+xml; version=3.1", gml31 );
            mimeTypeToFormat.put( "application/gml+xml; version=3.2", gml32 );
            mimeTypeToFormat.put( "text/xml; subtype=gml/2.1.2", gml21 );
            mimeTypeToFormat.put( "text/xml; subtype=gml/3.0.1", gml30 );
            mimeTypeToFormat.put( "text/xml; subtype=gml/3.1.1", gml31 );
            mimeTypeToFormat.put( "text/xml; subtype=gml/3.2.1", gml32 );
            mimeTypeToFormat.put( "text/xml; subtype=gml/3.2.2", gml32 );
            mimeTypeToFormat.put( "text/xml; subtype=\"gml/2.1.2\"", gml21 );
            mimeTypeToFormat.put( "text/xml; subtype=\"gml/3.0.1\"", gml30 );
            mimeTypeToFormat.put( "text/xml; subtype=\"gml/3.1.1\"", gml31 );
            mimeTypeToFormat.put( "text/xml; subtype=\"gml/3.2.1\"", gml32 );
            mimeTypeToFormat.put( "text/xml; subtype=\"gml/3.2.2\"", gml32 );
        } else {
            LOG.debug( "Using customized format configuration." );
            for ( JAXBElement<? extends AbstractFormatType> formatEl : formatList ) {
                AbstractFormatType formatDef = formatEl.getValue();
                List<String> mimeTypes = formatDef.getMimeType();
                Format format = null;
                if ( formatDef instanceof GMLFormat ) {
                    format = new org.deegree.services.wfs.format.gml.GmlFormat( this, (GMLFormat) formatDef );
                } else if ( formatDef instanceof CustomFormat ) {
                    CustomFormat cf = (CustomFormat) formatDef;
                    String className = cf.getJavaClass();
                    LOG.info( "Using custom format class '" + className + "'." );
                    try {
                        format = (org.deegree.services.wfs.format.CustomFormat) Class.forName( className ).newInstance();
                        ( (org.deegree.services.wfs.format.CustomFormat) format ).init( this, cf.getConfig() );
                    } catch ( Exception e ) {
                        throw new ResourceInitException( "Error initializing WFS format: " + e.getMessage(), e );
                    }
                } else {
                    throw new ResourceInitException( "Internal error. Unhandled AbstractFormatType '"
                                                     + formatDef.getClass() + "'." );
                }
                for ( String mimeType : mimeTypes ) {
                    mimeTypeToFormat.put( trim( mimeType ), format );
                }
            }
        }

        for ( Format f : mimeTypeToFormat.values() ) {
            if ( f instanceof org.deegree.services.wfs.format.gml.GmlFormat ) {
                gmlVersionToFormat.put( ( (org.deegree.services.wfs.format.gml.GmlFormat) f ).getGmlFormatOptions().getGmlVersion(),
                                        f );
            }
        }
    }

    private OWSMetadataProvider initMetadataProvider( DeegreeServicesMetadataType serviceMetadata, DeegreeWFS jaxbConfig )
                            throws ResourceInitException {
        OWSMetadataProvider provider = null;
        provider = workspace.getResource( OWSMetadataProviderProvider.class, getMetadata().getIdentifier().getId()
                                                                             + "_metadata" );

        if ( provider == null ) {
            ServiceIdentification serviceId = MetadataUtils.convertFromJAXB( serviceMetadata.getServiceIdentification() );
            if ( serviceId.getTitles().isEmpty() ) {
                serviceId.setTitles( Collections.singletonList( new LanguageString( "deegree 3 WFS", null ) ) );
            }
            if ( serviceId.getAbstracts().isEmpty() ) {
                serviceId.setAbstracts( Collections.singletonList( new LanguageString( "deegree 3 WFS", null ) ) );
            }
            ServiceProvider serviceProvider = MetadataUtils.convertFromJAXB( serviceMetadata.getServiceProvider() );

            if ( serviceProvider.getProviderName() == null ) {
                serviceProvider.setProviderName( "deegree organization" );
            }
            if ( serviceProvider.getProviderSite() == null ) {
                serviceProvider.setProviderSite( "http://www.deegree.org" );
            }

            List<DatasetMetadata> ftMetadata = new ArrayList<DatasetMetadata>();
            String metadataUrlTemplate = jaxbConfig.getMetadataURLTemplate();
            if ( metadataUrlTemplate == null ) {
                // use local CSW (if running)
                List<ResourceIdentifier<OWS>> owss = workspace.getResourcesOfType( OWSProvider.class );
                for ( ResourceIdentifier<OWS> id : owss ) {
                    OWS o = workspace.getResource( OWSProvider.class, id.getId() );
                    if ( o != null ) {
                        ImplementationMetadata<?> md = ( (OWSProvider) o.getMetadata().getProvider() ).getImplementationMetadata();
                        for ( String s : md.getImplementedServiceName() ) {
                            if ( s.equalsIgnoreCase( "csw" ) ) {
                                metadataUrlTemplate = OGCFrontController.getHttpGetURL();
                                if ( !metadataUrlTemplate.endsWith( "?" ) ) {
                                    metadataUrlTemplate = metadataUrlTemplate + "?";
                                }
                                metadataUrlTemplate += "service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=${metadataSetId}";
                            }
                        }
                    }
                }
            }

            for ( FeatureTypeMetadata ftMd : jaxbConfig.getFeatureTypeMetadata() ) {
                // TODO
                List<LanguageString> titles = null;
                // TODO
                List<LanguageString> abstracts = null;
                // TODO
                List<Pair<List<LanguageString>, CodeType>> keywords = null;
                final List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
                final String url = getMetadataURL( metadataUrlTemplate, ftMd );
                if ( url != null ) {
                    metadataUrls.add( new MetadataUrl( url, null, null ) );
                }
                try {
                    DatasetMetadata dsMd = new DatasetMetadata( ftMd.getName(), titles, abstracts, keywords,
                                                                metadataUrls, null, null, null, null );
                    ftMetadata.add( dsMd );
                } catch ( Throwable t ) {
                    t.printStackTrace();
                }

            }

            Map<String, List<OMElement>> wfsVersionToExtendedCaps = new HashMap<String, List<OMElement>>();
            List<ExtendedCapabilities> extendedCapConfigs = jaxbConfig.getExtendedCapabilities();
            if ( extendedCapConfigs != null ) {
                for ( ExtendedCapabilities extendedCapConfig : extendedCapConfigs ) {
                    Element extendedCaps = extendedCapConfig.getAny();
                    DOMSource domSource = new DOMSource( extendedCaps );
                    XMLStreamReader xmlStream;
                    try {
                        xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( domSource );
                    } catch ( Exception t ) {
                        throw new ResourceInitException( "Error extracting extended capabilities: " + t.getMessage(), t );
                    }
                    OMElement omEl = new XMLAdapter( xmlStream ).getRootElement();
                    for ( String wfsVersion : extendedCapConfig.getWfsVersions() ) {
                        if ( wfsVersionToExtendedCaps.containsKey( wfsVersion ) ) {
                            String msg = "Multiple ExtendedCapabilities sections for WFS version: " + wfsVersion + ".";
                            throw new ResourceInitException( msg );
                        }
                        wfsVersionToExtendedCaps.put( wfsVersion, Collections.singletonList( omEl ) );
                    }
                }
            }
            provider = new DefaultOWSMetadataProvider( serviceId, serviceProvider, wfsVersionToExtendedCaps,
                                                       ftMetadata, Collections.<String, String> emptyMap(), null );
        }
        return provider;
    }

    @Override
    public void destroy() {
        LOG.debug( "destroy" );
    }

    /**
     * Returns the underlying {@link WfsFeatureStoreManager} instance.
     * 
     * @return the underlying {@link WfsFeatureStoreManager}
     */
    public WfsFeatureStoreManager getStoreManager() {
        return service;
    }

    /**
     * @return the stored query handler for this service, never <code>null</code>.
     */
    public StoredQueryHandler getStoredQueryHandler() {
        return storedQueryHandler;
    }

    @Override
    public void doKVP( Map<String, String> kvpParamsUC, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        LOG.debug( "doKVP" );
        Version requestVersion = null;
        try {
            requestVersion = getVersion( kvpParamsUC.get( "VERSION" ) );
            String requestName = KVPUtils.getRequired( kvpParamsUC, "REQUEST" );
            WFSRequestType requestType = getRequestTypeByName( requestName );

            if ( !supportedEncodings.isEncodingSupported( requestType, "KVP" ) ) {
                throw new OWSException( "GET/KVP is not supported for " + requestName + " requests.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }

            // check if requested version is supported and offered (except for GetCapabilities)
            if ( requestType != WFSRequestType.GetCapabilities ) {
                if ( requestVersion == null ) {
                    throw new OWSException( "Missing version parameter.", OWSException.MISSING_PARAMETER_VALUE,
                                            "version" );
                }

                checkVersion( requestVersion );
            }

            // needed for CITE 1.1.0 compliance
            if ( requestVersion != null && requestVersion.equals( VERSION_110 ) ) {
                String serviceAttr = KVPUtils.getRequired( kvpParamsUC, "SERVICE" );
                if ( !"WFS".equals( serviceAttr ) ) {
                    throw new OWSException( "Wrong service attribute: '" + serviceAttr + "' -- must be 'WFS'.",
                                            OWSException.INVALID_PARAMETER_VALUE, "service" );
                }
            }

            // build namespaces from NamespaceHints given in the configuration
            Map<String, String> nsMap = service.getPrefixToNs();

            if ( disableBuffering ) {
                response.disableBuffering();
            }

            switch ( requestType ) {
            case CreateStoredQuery:
                throw new OWSException( Messages.get( "WFS_NO_KVP_BINDING", requestName, requestVersion ),
                                        OPERATION_NOT_SUPPORTED );
            case DescribeFeatureType:
                DescribeFeatureType describeFt = DescribeFeatureTypeKVPAdapter.parse( kvpParamsUC );
                Format format = determineFormat( requestVersion, describeFt.getOutputFormat(), "outputFormat" );
                format.doDescribeFeatureType( describeFt, response, false );
                break;
            case DescribeStoredQueries:
                DescribeStoredQueries describeStoredQueries = DescribeStoredQueriesKVPAdapter.parse( kvpParamsUC );
                storedQueryHandler.doDescribeStoredQueries( describeStoredQueries, response );
                break;
            case DropStoredQuery:
                DropStoredQuery dropStoredQuery = DropStoredQueryKVPAdapter.parse( kvpParamsUC );
                storedQueryHandler.doDropStoredQuery( dropStoredQuery, response );
                break;
            case GetCapabilities:
                GetCapabilities getCapabilities = GetCapabilitiesKVPParser.parse( kvpParamsUC );
                doGetCapabilities( getCapabilities, response );
                break;
            case GetFeature:
                GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpParamsUC, nsMap );
                updateResolveTimeOut( getFeature.getResolveParams() );
                format = determineFormat( requestVersion, getFeature.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetFeature( getFeature, response );
                break;
            case GetFeatureWithLock:
                checkTransactionsEnabled( requestName );
                GetFeatureWithLock getFeatureWithLock = GetFeatureWithLockKVPAdapter.parse( kvpParamsUC );
                updateResolveTimeOut( getFeatureWithLock.getResolveParams() );
                format = determineFormat( requestVersion, getFeatureWithLock.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetFeature( getFeatureWithLock, response );
                break;
            case GetGmlObject:
                GetGmlObject getGmlObject = GetGmlObjectKVPAdapter.parse( kvpParamsUC );
                format = determineFormat( requestVersion, getGmlObject.getOutputFormat(), "outputFormat" );
                format.doGetGmlObject( getGmlObject, response );
                break;
            case GetPropertyValue:
                GetPropertyValue getPropertyValue = GetPropertyValueKVPAdapter.parse( kvpParamsUC );
                updateResolveTimeOut( getPropertyValue.getResolveParams() );
                format = determineFormat( requestVersion, getPropertyValue.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetPropertyValue( getPropertyValue, response );
                break;
            case ListStoredQueries:
                ListStoredQueries listStoredQueries = ListStoredQueriesKVPAdapter.parse( kvpParamsUC );
                storedQueryHandler.doListStoredQueries( listStoredQueries, response );
                break;
            case LockFeature:
                checkTransactionsEnabled( requestName );
                LockFeature lockFeature = LockFeatureKVPAdapter.parse( kvpParamsUC );
                lockFeatureHandler.doLockFeature( lockFeature, response );
                break;
            case Transaction:
                if ( requestVersion.equals( VERSION_200 ) ) {
                    throw new OWSException( Messages.get( "WFS_NO_KVP_BINDING", requestName, requestVersion ),
                                            OPERATION_NOT_SUPPORTED );
                }
                checkTransactionsEnabled( requestName );
                Transaction transaction = TransactionKVPAdapter.parse( kvpParamsUC );
                new TransactionHandler( this, service, transaction, idGenMode, referenceResolvingMode ).doTransaction( response );
                break;
            default:
                throw new RuntimeException( "Internal error: Unhandled request '" + requestName + "'." );
            }
        } catch ( OWSException e ) {
            LOG.debug( "OWS-Exception: {}", e.getMessage() );
            LOG.trace( e.getMessage(), e );
            sendServiceException( requestVersion, e, response );
        } catch ( XMLParsingException e ) {
            LOG.trace( "Stack trace:", e );
            String exceptionCode = INVALID_PARAMETER_VALUE;
            if ( VERSION_200.equals( requestVersion ) )
                exceptionCode = OWSException.OPERATION_PROCESSING_FAILED;
            sendServiceException( requestVersion, new OWSException( e.getMessage(), exceptionCode ), response );
        } catch ( MissingParameterException e ) {
            LOG.debug( "OWS-Exception: {}", e.getMessage() );
            LOG.trace( e.getMessage(), e );
            sendServiceException( requestVersion, new OWSException( e ), response );
        } catch ( LockHasExpiredException e ) {
            LOG.debug( "OWS-Exception: {}", e.getMessage() );
            LOG.trace( e.getMessage(), e );
            if ( VERSION_200.equals( requestVersion ) )
                sendServiceException( requestVersion, new OWSException( e.getMessage(), LOCK_HAS_EXPIRED ), response );
            else
                sendServiceException( requestVersion, new OWSException( e ), response );
        } catch ( InvalidParameterValueException e ) {
            LOG.debug( "OWS-Exception: {}", e.getMessage() );
            LOG.trace( e.getMessage(), e );
            sendServiceException( requestVersion, new OWSException( e ), response );
        } catch ( Exception e ) {
            LOG.debug( "OWS-Exception: {}", e.getMessage() );
            LOG.trace( e.getMessage(), e );
            sendServiceException( requestVersion, new OWSException( e.getMessage(), NO_APPLICABLE_CODE ), response );
        }
    }

    private void checkTransactionsEnabled( String requestName )
                            throws OWSException {
        if ( !enableTransactions ) {
            throw new OWSException( Messages.get( "WFS_TRANSACTIONS_DISABLED", requestName ),
                                    OWSException.OPERATION_NOT_SUPPORTED );
        }
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        LOG.debug( "doXML" );
        Version requestVersion = null;
        try {
            String requestName = xmlStream.getLocalName();
            WFSRequestType requestType = getRequestTypeByName( requestName );

            if ( !supportedEncodings.isEncodingSupported( requestType, "XML" ) ) {
                throw new OWSException( "POST/XML is not supported for " + requestName + " requests.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }

            // check if requested version is supported and offered (except for GetCapabilities)
            requestVersion = getVersion( XMLStreamUtils.getAttributeValue( xmlStream, "version" ) );
            if ( requestType != WFSRequestType.GetCapabilities ) {
                requestVersion = checkVersion( requestVersion );

                // needed for CITE 1.1.0 compliance
                String serviceAttr = XMLStreamUtils.getAttributeValue( xmlStream, "service" );
                if ( serviceAttr != null && !( "WFS".equals( serviceAttr ) || "".equals( serviceAttr ) ) ) {
                    throw new OWSException( "Wrong service attribute: '" + serviceAttr + "' -- must be 'WFS'.",
                                            INVALID_PARAMETER_VALUE, "service" );
                }
            }

            if ( disableBuffering ) {
                response.disableBuffering();
            }

            switch ( requestType ) {
            case CreateStoredQuery:
                CreateStoredQueryXMLAdapter createStoredQueryAdapter = new CreateStoredQueryXMLAdapter();
                createStoredQueryAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                CreateStoredQuery createStoredQuery = createStoredQueryAdapter.parse();
                storedQueryHandler.doCreateStoredQuery( createStoredQuery, response );
                break;
            case DescribeFeatureType:
                DescribeFeatureTypeXMLAdapter describeFtAdapter = new DescribeFeatureTypeXMLAdapter();
                describeFtAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                DescribeFeatureType describeFt = describeFtAdapter.parse();
                Format format = determineFormat( requestVersion, describeFt.getOutputFormat(), "outputFormat" );
                format.doDescribeFeatureType( describeFt, response, false );
                break;
            case DropStoredQuery:
                DropStoredQueryXMLAdapter dropStoredQueryAdapter = new DropStoredQueryXMLAdapter();
                dropStoredQueryAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                DropStoredQuery dropStoredQuery = dropStoredQueryAdapter.parse();
                storedQueryHandler.doDropStoredQuery( dropStoredQuery, response );
                break;
            case DescribeStoredQueries:
                DescribeStoredQueriesXMLAdapter describeStoredQueriesAdapter = new DescribeStoredQueriesXMLAdapter();
                describeStoredQueriesAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                DescribeStoredQueries describeStoredQueries = describeStoredQueriesAdapter.parse();
                storedQueryHandler.doDescribeStoredQueries( describeStoredQueries, response );
                break;
            case GetCapabilities:
                GetCapabilitiesXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesXMLAdapter();
                getCapabilitiesAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                GetCapabilities wfsRequest = getCapabilitiesAdapter.parse( requestVersion );
                doGetCapabilities( wfsRequest, response );
                break;
            case GetFeature:
                GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
                getFeatureAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                GetFeature getFeature = getFeatureAdapter.parse();
                updateResolveTimeOut( getFeature.getResolveParams() );
                format = determineFormat( requestVersion, getFeature.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetFeature( getFeature, response );
                break;
            case GetFeatureWithLock:
                checkTransactionsEnabled( requestName );
                GetFeatureWithLockXMLAdapter getFeatureWithLockAdapter = new GetFeatureWithLockXMLAdapter();
                getFeatureWithLockAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                GetFeatureWithLock getFeatureWithLock = getFeatureWithLockAdapter.parse();
                checkGetFeatureWithLockRequest( requestVersion, getFeatureWithLock );
                updateResolveTimeOut( getFeatureWithLock.getResolveParams() );
                format = determineFormat( requestVersion, getFeatureWithLock.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetFeature( getFeatureWithLock, response );
                break;
            case GetGmlObject:
                GetGmlObjectXMLAdapter getGmlObjectAdapter = new GetGmlObjectXMLAdapter();
                getGmlObjectAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                GetGmlObject getGmlObject = getGmlObjectAdapter.parse();
                format = determineFormat( requestVersion, getGmlObject.getOutputFormat(), "outputFormat" );
                format.doGetGmlObject( getGmlObject, response );
                break;
            case GetPropertyValue:
                GetPropertyValueXMLAdapter getPropertyValueAdapter = new GetPropertyValueXMLAdapter();
                getPropertyValueAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                GetPropertyValue getPropertyValue = getPropertyValueAdapter.parse();
                updateResolveTimeOut( getPropertyValue.getResolveParams() );
                format = determineFormat( requestVersion, getPropertyValue.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetPropertyValue( getPropertyValue, response );
                break;
            case ListStoredQueries:
                ListStoredQueriesXMLAdapter listStoredQueriesAdapter = new ListStoredQueriesXMLAdapter();
                listStoredQueriesAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                ListStoredQueries listStoredQueries = listStoredQueriesAdapter.parse();
                storedQueryHandler.doListStoredQueries( listStoredQueries, response );
                break;
            case LockFeature:
                checkTransactionsEnabled( requestName );
                LockFeatureXMLAdapter lockFeatureAdapter = new LockFeatureXMLAdapter();
                lockFeatureAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                LockFeature lockFeature = lockFeatureAdapter.parse();
                lockFeatureHandler.doLockFeature( lockFeature, response );
                break;
            case Transaction:
                checkTransactionsEnabled( requestName );
                TransactionXmlReader transactionReader = new TransactionXmlReaderFactory().createReader( xmlStream );
                Transaction transaction = transactionReader.read( xmlStream );
                new TransactionHandler( this, service, transaction, idGenMode, referenceResolvingMode ).doTransaction( response );
                break;
            default:
                throw new RuntimeException( "Internal error: Unhandled request '" + requestName + "'." );
            }
        } catch ( OWSException e ) {
            LOG.debug( e.getMessage(), e );
            sendServiceException( requestVersion, e, response );
        } catch ( XMLParsingException e ) {
            LOG.trace( "Stack trace:", e );
            String exceptionCode = INVALID_PARAMETER_VALUE;
            if ( VERSION_200.equals( requestVersion ) )
                exceptionCode = OWSException.OPERATION_PROCESSING_FAILED;
            sendServiceException( requestVersion, new OWSException( e.getMessage(), exceptionCode ), response );
        } catch ( MissingParameterException e ) {
            LOG.trace( "Stack trace:", e );
            sendServiceException( requestVersion, new OWSException( e ), response );
        } catch ( LockHasExpiredException e ) {
            LOG.trace( "Stack trace:", e );
            if ( VERSION_200.equals( requestVersion ) )
                sendServiceException( requestVersion, new OWSException( e.getMessage(), LOCK_HAS_EXPIRED ), response );
            else
                sendServiceException( requestVersion, new OWSException( e ), response );
        } catch ( InvalidParameterValueException e ) {
            LOG.trace( "Stack trace:", e );
            sendServiceException( requestVersion, new OWSException( e ), response );
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            sendServiceException( requestVersion, new OWSException( e.getMessage(), NO_APPLICABLE_CODE ), response );
        }
    }

    @Override
    public void doSOAP( SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
                        List<FileItem> multiParts, SOAPFactory factory )
                            throws ServletException, IOException, org.deegree.services.authentication.SecurityException {
        LOG.debug( "doSOAP" );

        if ( !isSoapSupported() ) {
            super.doSOAP( soapDoc, request, response, multiParts, factory );
            return;
        }

        Version requestVersion = null;
        try {
            if ( soapDoc.getVersion() instanceof SOAP11Version ) {
                XMLStreamWriter xmlWriter = response.getXMLWriter();
                String soapEnvNS = "http://schemas.xmlsoap.org/soap/envelope/";
                String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";
                xmlWriter.writeStartElement( "soap", "Envelope", soapEnvNS );
                xmlWriter.writeNamespace( "soap", soapEnvNS );
                xmlWriter.writeNamespace( "xsi", xsiNS );
                xmlWriter.writeAttribute( xsiNS, "schemaLocation",
                                          "http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/" );
                xmlWriter.writeStartElement( soapEnvNS, "Body" );
            } else {
                beginSOAPResponse( response );
            }

            OMElement body = soapDoc.getBody().getFirstElement().cloneOMElement();
            XMLStreamReader bodyXmlStream = XMLStreamUtils.getAsXmlStrem( body );

            String requestName = body.getLocalName();
            WFSRequestType requestType = getRequestTypeByName( requestName );

            if ( !supportedEncodings.isEncodingSupported( requestType, "SOAP" ) ) {
                throw new OWSException( "POST/SOAP is not supported for " + requestName + " requests.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }

            // check if requested version is supported and offered (except for GetCapabilities)
            requestVersion = getVersion( body.getAttributeValue( new QName( "version" ) ) );
            if ( requestType != WFSRequestType.GetCapabilities ) {
                requestVersion = checkVersion( requestVersion );

                // needed for CITE 1.1.0 compliance
                String serviceAttr = body.getAttributeValue( new QName( "service" ) );
                if ( serviceAttr != null && !( "WFS".equals( serviceAttr ) || "".equals( serviceAttr ) ) ) {
                    throw new OWSException( "Wrong service attribute: '" + serviceAttr + "' -- must be 'WFS'.",
                                            INVALID_PARAMETER_VALUE, "service" );
                }
            }

            switch ( requestType ) {
            case CreateStoredQuery:
                CreateStoredQueryXMLAdapter createStoredQueryAdapter = new CreateStoredQueryXMLAdapter();
                createStoredQueryAdapter.setRootElement( body );
                CreateStoredQuery createStoredQuery = createStoredQueryAdapter.parse();
                storedQueryHandler.doCreateStoredQuery( createStoredQuery, response );
                break;
            case DescribeFeatureType:
                DescribeFeatureTypeXMLAdapter describeFtAdapter = new DescribeFeatureTypeXMLAdapter();
                describeFtAdapter.setRootElement( body );
                DescribeFeatureType describeFt = describeFtAdapter.parse();
                Format format = determineFormat( requestVersion, describeFt.getOutputFormat(), "outputFormat" );
                format.doDescribeFeatureType( describeFt, response, true );
                break;
            case DropStoredQuery:
                DropStoredQueryXMLAdapter dropStoredQueryAdapter = new DropStoredQueryXMLAdapter();
                dropStoredQueryAdapter.setRootElement( body );
                DropStoredQuery dropStoredQuery = dropStoredQueryAdapter.parse();
                storedQueryHandler.doDropStoredQuery( dropStoredQuery, response );
                break;
            case DescribeStoredQueries:
                DescribeStoredQueriesXMLAdapter describeStoredQueriesAdapter = new DescribeStoredQueriesXMLAdapter();
                describeStoredQueriesAdapter.setRootElement( body );
                DescribeStoredQueries describeStoredQueries = describeStoredQueriesAdapter.parse();
                storedQueryHandler.doDescribeStoredQueries( describeStoredQueries, response );
                break;
            case GetCapabilities:
                GetCapabilitiesXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesXMLAdapter();
                getCapabilitiesAdapter.setRootElement( body );
                GetCapabilities wfsRequest = getCapabilitiesAdapter.parse( requestVersion );
                doGetCapabilities( wfsRequest, response );
                break;
            case GetFeature:
                GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
                getFeatureAdapter.setRootElement( body );
                GetFeature getFeature = getFeatureAdapter.parse();
                updateResolveTimeOut( getFeature.getResolveParams() );
                format = determineFormat( requestVersion, getFeature.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetFeature( getFeature, response );
                break;
            case GetFeatureWithLock:
                checkTransactionsEnabled( requestName );
                GetFeatureWithLockXMLAdapter getFeatureWithLockAdapter = new GetFeatureWithLockXMLAdapter();
                getFeatureWithLockAdapter.setRootElement( body );
                GetFeatureWithLock getFeatureWithLock = getFeatureWithLockAdapter.parse();
                updateResolveTimeOut( getFeatureWithLock.getResolveParams() );
                format = determineFormat( requestVersion, getFeatureWithLock.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetFeature( getFeatureWithLock, response );
                break;
            case GetGmlObject:
                GetGmlObjectXMLAdapter getGmlObjectAdapter = new GetGmlObjectXMLAdapter();
                getGmlObjectAdapter.setRootElement( body );
                GetGmlObject getGmlObject = getGmlObjectAdapter.parse();
                format = determineFormat( requestVersion, getGmlObject.getOutputFormat(), "outputFormat" );
                format.doGetGmlObject( getGmlObject, response );
                break;
            case GetPropertyValue:
                GetPropertyValueXMLAdapter getPropertyValueAdapter = new GetPropertyValueXMLAdapter();
                getPropertyValueAdapter.setRootElement( body );
                GetPropertyValue getPropertyValue = getPropertyValueAdapter.parse();
                updateResolveTimeOut( getPropertyValue.getResolveParams() );
                format = determineFormat( requestVersion, getPropertyValue.getPresentationParams().getOutputFormat(),
                                          "outputFormat" );
                format.doGetPropertyValue( getPropertyValue, response );
                break;
            case ListStoredQueries:
                ListStoredQueriesXMLAdapter listStoredQueriesAdapter = new ListStoredQueriesXMLAdapter();
                listStoredQueriesAdapter.setRootElement( body );
                ListStoredQueries listStoredQueries = listStoredQueriesAdapter.parse();
                storedQueryHandler.doListStoredQueries( listStoredQueries, response );
                break;
            case LockFeature:
                checkTransactionsEnabled( requestName );
                LockFeatureXMLAdapter lockFeatureAdapter = new LockFeatureXMLAdapter();
                lockFeatureAdapter.setRootElement( body );
                LockFeature lockFeature = lockFeatureAdapter.parse();
                lockFeatureHandler.doLockFeature( lockFeature, response );
                break;
            case Transaction:
                checkTransactionsEnabled( requestName );
                TransactionXmlReader transactionReader = new TransactionXmlReaderFactory().createReader( requestVersion );
                Transaction transaction = transactionReader.read( bodyXmlStream );
                new TransactionHandler( this, service, transaction, idGenMode, referenceResolvingMode ).doTransaction( response );
                break;
            default:
                throw new RuntimeException( "Internal error: Unhandled request '" + requestName + "'." );
            }

            endSOAPResponse( response );
            response.setContentType( "application/soap+xml" );

        } catch ( OWSException e ) {
            LOG.debug( e.getMessage(), e );
            sendSoapException( soapDoc, factory, response, e, request, requestVersion );
        } catch ( XMLParsingException e ) {
            LOG.trace( "Stack trace:", e );
            String exceptionCode = INVALID_PARAMETER_VALUE;
            if ( VERSION_200.equals( requestVersion ) )
                exceptionCode = OWSException.OPERATION_PROCESSING_FAILED;
            sendSoapException( soapDoc, factory, response, new OWSException( e.getMessage(), exceptionCode ), request,
                               requestVersion );
        } catch ( MissingParameterException e ) {
            LOG.trace( "Stack trace:", e );
            sendSoapException( soapDoc, factory, response, new OWSException( e ), request, requestVersion );

        } catch ( LockHasExpiredException e ) {
            LOG.trace( "Stack trace:", e );
            if ( VERSION_200.equals( requestVersion ) )
                sendSoapException( soapDoc, factory, response, new OWSException( e.getMessage(), LOCK_HAS_EXPIRED ),
                                   request, requestVersion );
            else
                sendSoapException( soapDoc, factory, response, new OWSException( e ), request, requestVersion );
        } catch ( InvalidParameterValueException e ) {
            LOG.trace( "Stack trace:", e );
            sendSoapException( soapDoc, factory, response, new OWSException( e ), request, requestVersion );
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            sendSoapException( soapDoc, factory, response, new OWSException( e.getMessage(), NO_APPLICABLE_CODE ),
                               request, requestVersion );
        }

    }

    private Version getVersion( String versionString )
                            throws OWSException {
        Version version = null;
        if ( versionString != null && !"".equals( versionString ) ) {
            try {
                version = Version.parseVersion( versionString );
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE, "version" );
            }
        }
        return version;
    }

    private WFSRequestType getRequestTypeByName( String requestName )
                            throws OWSException {
        ImplementationMetadata<?> md = ( (OWSProvider) getMetadata().getProvider() ).getImplementationMetadata();
        WFSRequestType requestType = (WFSRequestType) md.getRequestTypeByName( requestName );
        if ( requestType == null ) {
            String msg = "Request type '" + requestName + "' is not supported.";
            throw new OWSException( msg, OWSException.OPERATION_NOT_SUPPORTED, "request" );
        }
        return requestType;
    }

    private void doGetCapabilities( GetCapabilities request, HttpResponseBuffer response )
                            throws XMLStreamException, IOException, OWSException {

        LOG.debug( "doGetCapabilities: " + request );
        Version negotiatedVersion = negotiateVersion( request );

        // cope with the 'All' section specifier
        Set<String> sections = request.getSections();
        Set<String> sectionsUC = new HashSet<String>();
        for ( String section : sections ) {
            if ( section.equalsIgnoreCase( "ALL" ) ) {
                sectionsUC = null;
                break;
            }
            sectionsUC.add( section.toUpperCase() );
        }
        // never empty (only null)
        if ( sectionsUC != null && sectionsUC.size() == 0 ) {
            sectionsUC = null;
        }
        final Collection<FeatureType> sortedFts = getFeatureTypesToExport();

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, "text/xml", null );
        GetCapabilitiesHandler adapter = new GetCapabilitiesHandler( this, service, negotiatedVersion, xmlWriter,
                                                                     sortedFts, sectionsUC, enableTransactions,
                                                                     queryCRS, supportedEncodings, mdProvider );
        adapter.export();
        xmlWriter.flush();
    }

    private Collection<FeatureType> getFeatureTypesToExport() {
        if ( mdProvider.getDatasetMetadata() != null && !mdProvider.getDatasetMetadata().isEmpty() ) {
            LOG.debug( "Dataset metadata available. Only announcing feature types with metadata." );
            return getFeatureTypesWithMetadata();
        }
        LOG.debug( "No dataset metadata available. Announcing feature types from all feature stores." );
        return getAllFeatureTypes();
    }

    private Collection<FeatureType> getFeatureTypesWithMetadata() {
        final Collection<FeatureType> sortedFts = new LinkedHashSet<FeatureType>();
        for ( final DatasetMetadata datasetMetadata : mdProvider.getDatasetMetadata() ) {
            final QName ftName = datasetMetadata.getQName();
            final FeatureStore fs = service.getStore( ftName );
            if ( fs != null ) {
                if ( fs.isMapped( ftName ) ) {
                    sortedFts.add( service.lookupFeatureType( ftName ) );
                }
            } else {
                LOG.warn( "Found metadata for feature type '" + ftName
                          + "', but feature type is not available from any store." );
            }
        }
        return sortedFts;
    }

    private Collection<FeatureType> getAllFeatureTypes() {
        Comparator<FeatureType> comp = new Comparator<FeatureType>() {
            @Override
            public int compare( FeatureType ftMd1, FeatureType ftMd2 ) {
                QName a = ftMd1.getName();
                QName b = ftMd2.getName();
                int order = a.getNamespaceURI().compareTo( b.getNamespaceURI() );
                if ( order == 0 ) {
                    order = a.getLocalPart().compareTo( b.getLocalPart() );
                }
                return order;
            }
        };
        Collection<FeatureType> sortedFts = new TreeSet<FeatureType>( comp );
        for ( FeatureType ft : service.getFeatureTypes() ) {
            FeatureStore fs = service.getStore( ft.getName() );
            if ( fs.isMapped( ft.getName() ) ) {
                sortedFts.add( ft );
            }
        }
        return sortedFts;
    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be <code>null</code>
     * @param mimeType
     *            mime type, must not be <code>null</code>
     * @param schemaLocation
     *            value for the 'xsi:schemaLocation' attribute in the root element, can be <code>null</code>
     * @return XML stream writer object that takes care of putting the schemaLocation in the root element
     * @throws XMLStreamException
     * @throws IOException
     */
    public static XMLStreamWriter getXMLResponseWriter( HttpResponseBuffer writer, String mimeType,
                                                        String schemaLocation )
                            throws XMLStreamException, IOException {

        boolean needsEncoding = mimeType.startsWith( "text" );
        XMLStreamWriter xmlWriter = writer.getXMLWriter( needsEncoding );
        // call setContentType(...) after setCharacterEncoding(...) to avoid problems on certain web containers
        // see http://tracker.deegree.org/deegree-services/ticket/323
        writer.setContentType( mimeType );
        if ( schemaLocation == null ) {
            return xmlWriter;
        }
        return new SchemaLocationXMLStreamWriter( xmlWriter, schemaLocation );
    }

    private void sendServiceException( Version requestVersion, OWSException e, HttpResponseBuffer response )
                            throws ServletException {
        XMLExceptionSerializer serializer = getExceptionSerializer( requestVersion );
        sendException( null, serializer, e, response );
    }

    private void sendSoapException( SOAPEnvelope soapDoc, SOAPFactory factory, HttpResponseBuffer response,
                                    OWSException e, ServletRequest request, Version requestVersion )
                            throws OMException, ServletException {
        XMLExceptionSerializer serializer = getExceptionSerializer( requestVersion );
        sendSOAPException( soapDoc.getHeader(), factory, response, e, serializer, null, null, request.getServerName(),
                           request.getCharacterEncoding() );
    }

    @Override
    public XMLExceptionSerializer getExceptionSerializer( Version requestVersion ) {
        XMLExceptionSerializer serializer = getDefaultExceptionSerializer();
        if ( VERSION_100.equals( requestVersion ) ) {
            serializer = new PreOWSExceptionReportSerializer( "application/vnd.ogc.se_xml" );
        } else if ( VERSION_110.equals( requestVersion ) ) {
            serializer = new OWS100ExceptionReportSerializer();
        } else if ( VERSION_200.equals( requestVersion ) ) {
            serializer = new OWS110ExceptionReportSerializer( VERSION_200 );
        }
        return serializer;
    }

    private XMLExceptionSerializer getDefaultExceptionSerializer() {
        List<String> offeredVersions = getOfferedVersions();
        if ( offeredVersions.contains( VERSION_200.toString() ) ) {
            return new OWS110ExceptionReportSerializer( VERSION_200 );
        } else if ( offeredVersions.contains( VERSION_110.toString() ) ) {
            return new OWS100ExceptionReportSerializer();
        }
        return new PreOWSExceptionReportSerializer( "application/vnd.ogc.se_xml" );
    }

    /**
     * Determines the requested output/input format.
     * 
     * @param requestVersion
     *            version of the WFS request, must not be <code>null</code>
     * @param format
     *            mimeType or identifier for the format, can be <code>null</code>
     * @param locator
     * @return format handler to use, never <code>null</code>
     * @throws OWSException
     */
    private Format determineFormat( Version requestVersion, String format, String locator )
                            throws OWSException {

        Format outputFormat = null;

        if ( format == null ) {
            // default values for the different WFS version
            if ( VERSION_100.equals( requestVersion ) ) {
                outputFormat = gmlVersionToFormat.get( GMLVersion.GML_2 );
            } else if ( VERSION_110.equals( requestVersion ) ) {
                outputFormat = gmlVersionToFormat.get( GMLVersion.GML_31 );
            } else if ( VERSION_200.equals( requestVersion ) ) {
                outputFormat = gmlVersionToFormat.get( GMLVersion.GML_32 );
            }
        } else {
            if ( "GML2".equals( format ) || "XMLSCHEMA".equals( format ) ) {
                outputFormat = gmlVersionToFormat.get( GMLVersion.GML_2 );
            } else if ( "GML3".equals( format ) ) {
                outputFormat = gmlVersionToFormat.get( GMLVersion.GML_31 );
            } else {
                outputFormat = mimeTypeToFormat.get( format );
            }
        }
        if ( outputFormat == null ) {
            String msg = "This WFS is not configured to handle the output/input format '" + format + "'";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, locator );
        }
        return outputFormat;
    }

    private void updateResolveTimeOut( ResolveParams resolveParams ) {
        if ( resolveParams.getTimeout() == null && resolveTimeOutInSeconds != null )
            resolveParams.setTimeout( resolveTimeOutInSeconds );
    }

    Collection<String> getOutputFormats() {
        return mimeTypeToFormat.keySet();
    }

    public int getQueryMaxFeatures() {
        return queryMaxFeatures;
    }

    /**
     * @return the configured value for ResolveTimeOut in seconds, <code>null</code> if not configured
     */
    public BigInteger getResolveTimeOutInSeconds() {
        return resolveTimeOutInSeconds;
    }

    public boolean getCheckAreaOfUse() {
        return checkAreaOfUse;
    }

    public ICRS getDefaultQueryCrs() {
        return defaultQueryCRS;
    }

    /**
     * @return <code>true</code> if response paging is enabled by configuration, <code>false</code> otherwise
     */
    public boolean isEnableResponsePaging() {
        return enableResponsePaging;
    }

    /**
     * @return <code>true</code> if soap is supported, <code>false</code> otherwise
     */
    public boolean isSoapSupported() {
        return !disableBuffering;
    }

    /**
     * @return the matcher to match disabled urls, may be <code>null</code>
     */
    public ReferencePatternMatcher getReferencePatternMatcher() {
        return referencePatternMatcher;
    }

    /**
     * @return <code>true</code> if the service should behave strict, <code>false</code> otherwise
     */
    public boolean isStrict() {
        return isStrict;
    }

    /**
     * Checks if a request version can be handled by this controller (i.e. if is supported by the implementation *and*
     * offered by the current configuration).
     * <p>
     * NOTE: This method does use exception code {@link OWSException#INVALID_PARAMETER_VALUE}, not
     * {@link OWSException#VERSION_NEGOTIATION_FAILED} -- the latter should only be used for failed GetCapabilities
     * requests.
     * </p>
     * 
     * @param requestedVersion
     *            version to be checked, may be null (causes exception)
     * @return <code>requestedVersion</code> (if it is not null), or highest version supported
     * @throws OWSException
     *             if the requested version is not available
     */
    @Override
    protected Version checkVersion( Version requestedVersion )
                            throws OWSException {

        Version version = requestedVersion;
        if ( requestedVersion == null ) {
            LOG.debug( "Assuming version 1.1.0 (the only one that has an optional version attribute)." );
            version = VERSION_110;
        }
        if ( !offeredVersions.contains( version ) ) {
            throw new OWSException(
                                    Messages.get( "CONTROLLER_UNSUPPORTED_VERSION", version, getOfferedVersionsString() ),
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        return version;
    }

    private void checkGetFeatureWithLockRequest( Version requestVersion, GetFeatureWithLock getFeatureWithLock ) {
        if ( VERSION_200.equals( requestVersion )
             && HITS.equals( getFeatureWithLock.getPresentationParams().getResultType() ) )
            throw new InvalidParameterValueException(
                                                      "ResultType 'hits' is not allowed in GetFeatureWithLock requests!" );
    }
}
