//$HeadURL: 
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
package org.deegree.portal.cataloguemanager.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.cataloguemanager.model.AName;
import org.deegree.portal.cataloguemanager.model.Address;
import org.deegree.portal.cataloguemanager.model.AttributeType;
import org.deegree.portal.cataloguemanager.model.CICitation;
import org.deegree.portal.cataloguemanager.model.CIDate;
import org.deegree.portal.cataloguemanager.model.CIDateTypeCode;
import org.deegree.portal.cataloguemanager.model.CIOnlineResource;
import org.deegree.portal.cataloguemanager.model.CIResponsibleParty;
import org.deegree.portal.cataloguemanager.model.ConnectPoint;
import org.deegree.portal.cataloguemanager.model.Contact;
import org.deegree.portal.cataloguemanager.model.ContactInfo;
import org.deegree.portal.cataloguemanager.model.ContainsOperations;
import org.deegree.portal.cataloguemanager.model.CoupledResource;
import org.deegree.portal.cataloguemanager.model.DCP;
import org.deegree.portal.cataloguemanager.model.DCPList;
import org.deegree.portal.cataloguemanager.model.Date;
import org.deegree.portal.cataloguemanager.model.DateType;
import org.deegree.portal.cataloguemanager.model.DescriptiveKeywords;
import org.deegree.portal.cataloguemanager.model.Direction;
import org.deegree.portal.cataloguemanager.model.DistributionFormat;
import org.deegree.portal.cataloguemanager.model.DistributionInfo;
import org.deegree.portal.cataloguemanager.model.EXExtent;
import org.deegree.portal.cataloguemanager.model.EXGeographicBoundingBox;
import org.deegree.portal.cataloguemanager.model.EXTemporalExtent;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.cataloguemanager.model.Keyword;
import org.deegree.portal.cataloguemanager.model.Linkage;
import org.deegree.portal.cataloguemanager.model.MDDataIdentification;
import org.deegree.portal.cataloguemanager.model.MDDigitalTransferOptions;
import org.deegree.portal.cataloguemanager.model.MDDistribution;
import org.deegree.portal.cataloguemanager.model.MDFormat;
import org.deegree.portal.cataloguemanager.model.MDLegalConstraints;
import org.deegree.portal.cataloguemanager.model.MDMetadata;
import org.deegree.portal.cataloguemanager.model.MDReferenceSystem;
import org.deegree.portal.cataloguemanager.model.MDRepresentativeFraction;
import org.deegree.portal.cataloguemanager.model.MetadataBean;
import org.deegree.portal.cataloguemanager.model.Name;
import org.deegree.portal.cataloguemanager.model.OnLine;
import org.deegree.portal.cataloguemanager.model.OperationName;
import org.deegree.portal.cataloguemanager.model.Optionality;
import org.deegree.portal.cataloguemanager.model.Parameters;
import org.deegree.portal.cataloguemanager.model.ParentIdentifier;
import org.deegree.portal.cataloguemanager.model.Phone;
import org.deegree.portal.cataloguemanager.model.PointOfContact;
import org.deegree.portal.cataloguemanager.model.Repeatability;
import org.deegree.portal.cataloguemanager.model.ResourceConstraints;
import org.deegree.portal.cataloguemanager.model.SVCoupledResource;
import org.deegree.portal.cataloguemanager.model.SVOperationMetadata;
import org.deegree.portal.cataloguemanager.model.SVParameter;
import org.deegree.portal.cataloguemanager.model.SVServiceIdentification;
import org.deegree.portal.cataloguemanager.model.SpatialResolution;
import org.deegree.portal.cataloguemanager.model.TimePeriod;
import org.deegree.portal.cataloguemanager.model.TransferOptions;
import org.deegree.portal.cataloguemanager.model.TypeName;
import org.deegree.portal.cataloguemanager.model.ValueType;
import org.deegree.portal.cataloguemanager.model.Version;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InsertMetadataListener extends AbstractMetadataListener {

    protected static CoordinateSystem crs;

    static {
        if ( crs == null ) {
            try {
                crs = CRSFactory.create( "EPSG:4326" );
            } catch ( Exception e ) {
                BootLogger.logError( e.getMessage(), e );
            }
        }
    }

    private static ILogger LOG = LoggerFactory.getLogger( InsertMetadataListener.class );

    private static Properties operationParameters;

    static {
        if ( operationParameters == null ) {
            try {
                InputStream is = InsertMetadataListener.class.getResourceAsStream( "/org/deegree/portal/cataloguemanager/control/resources/operations.properties" );
                operationParameters = new Properties();
                operationParameters.load( is );
                is.close();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    protected CatalogueManagerConfiguration conf;

    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        try {
            conf = getCatalogueManagerConfiguration( event );
            MetadataBean bean = (MetadataBean) event.getAsBean();

            if ( "service".equals( bean.getHlevel() ) ) {
                handleServiceMetadata( responseHandler, bean );
            } else {
                handleDataMetadata( responseHandler, bean );
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }
    }

    /**
     * @param responseHandler
     * @param bean
     */
    @SuppressWarnings("unchecked")
    private void handleServiceMetadata( ResponseHandler responseHandler, MetadataBean bean )
                            throws Exception {
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.portal.cataloguemanager.model" );
        Unmarshaller u = jc.createUnmarshaller();

        String tmp = conf.getTemplateURL( bean.getHlevel() );
        URL url = conf.resolve( tmp );
        MDMetadata mdMetadata = (MDMetadata) u.unmarshal( url );

        Map<String, ?> serviceMD = bean.getServiceMetadataBean();

        // metadata settings
        mdMetadata.getFileIdentifier().setCharacterString( bean.getIdentifier() );

        mdMetadata.getHierarchyLevel().getMDScopeCode().setCodeListValue( bean.getHlevel() );
        mdMetadata.getHierarchyLevelName().setCharacterString( bean.getHlevel() );
        setContact( mdMetadata, bean );
        mdMetadata.getDateStamp().setDateTime( TimeTools.getISOFormattedTime() );
        if ( bean.getCrs() != null ) {
            MDReferenceSystem mdRef = mdMetadata.getReferenceSystemInfo().getMDReferenceSystem();
            mdRef.getReferenceSystemIdentifier().getRSIdentifier().getCode().setCharacterString( bean.getCrs() );
        }

        // data identification settings
        SVServiceIdentification svDataId = mdMetadata.getIdentificationInfo().getSVServiceIdentification();
        if ( "WMS".equalsIgnoreCase( (String) serviceMD.get( "type" ) ) ) {
            tmp = "view";
        } else if ( "WCS".equalsIgnoreCase( (String) serviceMD.get( "type" ) ) ) {
            tmp = "download";
        } else if ( "WFS".equalsIgnoreCase( (String) serviceMD.get( "type" ) ) ) {
            tmp = "download";
        } else if ( "CSW".equalsIgnoreCase( (String) serviceMD.get( "type" ) ) ) {
            tmp = "discovery";
        } else {
            tmp = (String) serviceMD.get( "type" );
        }
        svDataId.getServiceType().setLocalName( tmp );
        svDataId.getServiceTypeVersion().setCharacterString( (String) serviceMD.get( "version" ) );
        svDataId.getAbstract().setCharacterString( bean.getAbstract_() );
        // service metadata will have two sets of keywords; one (first) for keywords
        // from gemet thesaurs and one (second) for inspire service description keyword
        setKeywords( svDataId.getDescriptiveKeywords().get( 0 ), bean );
        setInspireKeyword( svDataId.getDescriptiveKeywords().get( 1 ), bean );
        setPointOfContact( svDataId.getPointOfContact(), bean );
        setExtent( svDataId.getExtent().getEXExtent(), bean );

        CICitation citation = svDataId.getCitation().getCICitation();
        citation.getTitle().setCharacterString( bean.getDatasetTitle() );
        citation.getIdentifier().getMDIdentifier().getCode().setCharacterString( bean.getIdentifier() );

        // citation dates
        List<Date> dates = citation.getDate();
        dates.clear();
        if ( bean.getCreation() != null ) {
            addDate( dates, "creation", bean.getCreation() );
        }
        if ( bean.getPublication() != null ) {
            addDate( dates, "publication", bean.getPublication() );
        }
        if ( bean.getRevision() != null ) {
            addDate( dates, "revision", bean.getRevision() );
        }

        // set operations metadata
        setOperationMetadata( serviceMD, svDataId );

        // set coupling type depending on service type
        if ( "CSW".equalsIgnoreCase( (String) serviceMD.get( "type" ) ) ) {
            svDataId.getCouplingType().getSVCouplingType().setCodeListValue( "loose" );
        } else if ( "WMS".equalsIgnoreCase( (String) serviceMD.get( "type" ) ) ) {
            svDataId.getCouplingType().getSVCouplingType().setCodeListValue( "mixed" );
        } else {
            svDataId.getCouplingType().getSVCouplingType().setCodeListValue( "tight" );
        }

        // set coupled resources; layer/featuretypes/coverages that are not coupled to
        // a metadata set will be ignored
        setCoupledResources( serviceMD, svDataId );

        // lineage
        if ( bean.getLineage() != null && bean.getLineage().trim().length() > 0 ) {
            mdMetadata.getDataQualityInfo().getDQDataQuality().getLineage().getLILineage().getStatement().setCharacterString(
                                                                                                                              bean.getLineage() );
        } else {
            mdMetadata.setDataQualityInfo( null );
        }

        // resource constraints
        List<ResourceConstraints> list = svDataId.getResourceConstraints();
        if ( bean.getAccessConstraints() == null ) {
            list.clear();
        } else {
            MDLegalConstraints legal = null;
            for ( ResourceConstraints resourceConstraints : list ) {
                if ( resourceConstraints.getMDLegalConstraints() != null ) {
                    legal = resourceConstraints.getMDLegalConstraints();
                    legal.getAccessConstraints().getMDRestrictionCode().setCodeListValue( bean.getAccessConstraints() );
                    legal.getAccessConstraints().getMDRestrictionCode().setValue( bean.getAccessConstraints() );
                }
            }
        }

        insertIntoCSW( responseHandler, mdMetadata );
    }

    @SuppressWarnings("unchecked")
    private void setCoupledResources( Map<String, ?> serviceMD, SVServiceIdentification svDataId ) {
        List<CoupledResource> coupledResources = svDataId.getCoupledResource();
        coupledResources.clear();

        List<?> list = (List<?>) serviceMD.get( "resources" );
        for ( Object object : list ) {
            Map<String, ?> resource = (Map<String, ?>) object;
            if ( resource.get( "resourceIdentifier" ) != null ) {
                List<String> operations = (List<String>) resource.get( "operations" );
                for ( String operation : operations ) {
                    CoupledResource cr = new CoupledResource();
                    SVCoupledResource svcr = new SVCoupledResource();
                    SVCoupledResource.Identifier identifier = new SVCoupledResource.Identifier();
                    identifier.setCharacterString( (String) resource.get( "resourceIdentifier" ) );
                    svcr.setIdentifier( identifier );
                    OperationName opName = new OperationName();
                    opName.setCharacterString( operation );
                    svcr.setOperationName( opName );
                    svcr.setScopedName( (String) resource.get( "name" ) );
                    cr.setSVCoupledResource( svcr );
                    coupledResources.add( cr );
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setOperationMetadata( Map<String, ?> serviceMD, SVServiceIdentification svDataId ) {
        List<ContainsOperations> containsOperations = svDataId.getContainsOperations();
        containsOperations.clear();

        List<?> list = (List<?>) serviceMD.get( "operations" );
        for ( Object object : list ) {
            Map<String, ?> operation = (Map<String, ?>) object;
            ContainsOperations co = new ContainsOperations();
            SVOperationMetadata om = new SVOperationMetadata();

            // operation name
            OperationName on = new OperationName();
            on.setCharacterString( (String) operation.get( "name" ) );
            om.setOperationName( on );

            // operation DCPs and connect points
            handleConnectPoints( operation, om );

            // add operation parameters
            handleParameters( serviceMD, operation, om );

            co.setSVOperationMetadata( om );
            containsOperations.add( co );
        }

    }

    private void handleParameters( Map<String, ?> serviceMD, Map<String, ?> operation, SVOperationMetadata om ) {
        List<Parameters> parametersList = om.getParameters();
        parametersList.clear();

        String type = (String) serviceMD.get( "type" );
        String version = (String) serviceMD.get( "version" );
        // comma seperated list of all parameters of an opertaion
        String operationParameter = operationParameters.getProperty( type + '.' + version + '.'
                                                                     + operation.get( "name" ) );
        if ( operationParameter != null ) {
            String[] tmp1 = operationParameter.split( "," );
            for ( String parameterName : tmp1 ) {
                // values for fields 'type' and 'optionality' (comma seperated)
                String tmp2 = operationParameters.getProperty( type + '.' + version + '.' + operation.get( "name" )
                                                               + '.' + parameterName );
                if ( tmp2 != null ) {
                    String[] tmp3 = tmp2.split( "," );
                    Parameters param = new Parameters();
                    SVParameter svParam = new SVParameter();
                    SVParameter.Name name = new SVParameter.Name();
                    AName aname = new AName();
                    aname.setCharacterString( parameterName );
                    name.setAName( aname );
                    AttributeType attributeType = new AttributeType();
                    TypeName typeName = new TypeName();
                    aname = new AName();
                    aname.setCharacterString( tmp3[0] );
                    typeName.setAName( aname );
                    attributeType.setTypeName( typeName );
                    name.setAttributeType( attributeType );
                    svParam.setName( name );
                    Direction direction = new Direction();
                    direction.setSVParameterDirection( "in" );
                    svParam.setDirection( direction );
                    ValueType valueType = new ValueType();
                    typeName = new TypeName();
                    aname = new AName();
                    aname.setCharacterString( tmp3[0] );
                    typeName.setAName( aname );
                    valueType.setTypeName( typeName );
                    svParam.setValueType( valueType );
                    Optionality optionality = new Optionality();
                    optionality.setCharacterString( tmp3[1] );
                    svParam.setOptionality( optionality );
                    Repeatability repeatability = new Repeatability();
                    repeatability.setBoolean( "false" );
                    svParam.setRepeatability( repeatability );
                    param.setSVParameter( svParam );
                    parametersList.add( param );
                }
            }
        }
    }

    private void handleConnectPoints( Map<String, ?> operation, SVOperationMetadata om ) {
        List<ConnectPoint> cps = om.getConnectPoint();

        List<DCP> dcps = om.getDCP();
        dcps.clear();
        String[] dcpType = new String[] { "dcp_get", "dcp_post", "dcp_soap" };
        String[] dcpValue = new String[] { "HTTPGet", "HTTPPosz", "SOAP" };
        for ( int i = 0; i < 3; i++ ) {
            if ( operation.get( dcpType[i] ) != null ) {
                DCP dcp = new DCP();
                DCPList dcpList = new DCPList();
                dcpList.setCodeList( "SV_DCPTypeCode" );
                dcpList.setCodeListValue( dcpValue[i] );
                dcp.setDCPList( dcpList );
                dcps.add( dcp );

                ConnectPoint connectPoint = new ConnectPoint();
                CIOnlineResource or = new CIOnlineResource();
                Linkage linkage = new Linkage();
                linkage.setURL( (String) operation.get( dcpType[i] ) );
                or.setLinkage( linkage );
                connectPoint.setCIOnlineResource( or );
                cps.add( connectPoint );
            }
        }
    }

    private void handleDataMetadata( ResponseHandler responseHandler, MetadataBean bean )
                            throws Exception {
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.portal.cataloguemanager.model" );
        Unmarshaller u = jc.createUnmarshaller();

        String tmp = conf.getTemplateURL( bean.getHlevel() );
        URL url = conf.resolve( tmp );
        MDMetadata mdMetadata = (MDMetadata) u.unmarshal( url );

        // metadata settings
        mdMetadata.getFileIdentifier().setCharacterString( bean.getIdentifier() );
        mdMetadata.getHierarchyLevel().getMDScopeCode().setCodeListValue( bean.getHlevel() );
        tmp = bean.getHlevel();
        if ( "series".equalsIgnoreCase( tmp ) ) {
            tmp = "datasetcollection";
        }
        if ( "dataset".equalsIgnoreCase( tmp ) && bean.getParentId() != null ) {
            ParentIdentifier pi = new ParentIdentifier();
            pi.setCharacterString( bean.getParentId() );
            mdMetadata.setParentIdentifier( pi );
        }
        mdMetadata.getHierarchyLevelName().setCharacterString( tmp );
        setContact( mdMetadata, bean );
        mdMetadata.getDateStamp().setDateTime( TimeTools.getISOFormattedTime() );
        if ( bean.getCrs() != null ) {
            MDReferenceSystem mdRef = mdMetadata.getReferenceSystemInfo().getMDReferenceSystem();
            mdRef.getReferenceSystemIdentifier().getRSIdentifier().getCode().setCharacterString( bean.getCrs() );
        }

        // data identification settings
        MDDataIdentification mdDataId = mdMetadata.getIdentificationInfo().getMDDataIdentification();
        mdDataId.getAbstract().setCharacterString( bean.getAbstract_() );
        mdDataId.setUuid( "_" + bean.getIdentifier() );
        mdDataId.setId( bean.getIdentifier() );
        // data metadata just will be include one set of keywords
        setKeywords( mdDataId.getDescriptiveKeywords().get( 0 ), bean );
        setInspireKeyword( mdDataId.getDescriptiveKeywords().get( 0 ), bean );
        setPointOfContact( mdDataId.getPointOfContact(), bean );
        mdDataId.getTopicCategory().setMDTopicCategoryCode( bean.getTopCat() );
        setExtent( mdDataId.getExtent().getEXExtent(), bean );

        SpatialResolution spr = mdDataId.getSpatialResolution();
        if ( bean.getScale() == null || bean.getScale().trim().length() == 0 ) {
            mdDataId.setSpatialResolution( null );
        } else {
            MDRepresentativeFraction frac = spr.getMDResolution().getEquivalentScale().getMDRepresentativeFraction();
            frac.getDenominator().setInteger( Integer.parseInt( bean.getScale().trim() ) );
        }

        CICitation citation = mdDataId.getCitation().getCICitation();
        citation.getTitle().setCharacterString( bean.getDatasetTitle() );
        citation.getIdentifier().getMDIdentifier().getCode().setCharacterString( bean.getIdentifier() );

        // citation dates
        List<Date> dates = citation.getDate();
        dates.clear();
        if ( bean.getCreation() != null ) {
            addDate( dates, "creation", bean.getCreation() );
        }
        if ( bean.getPublication() != null ) {
            addDate( dates, "publication", bean.getPublication() );
        }
        if ( bean.getRevision() != null ) {
            addDate( dates, "revision", bean.getRevision() );
        }

        if ( bean.getTransferOnline() != null && bean.getTransferOnline().trim().length() > 0 ) {
            setDistributionInfo( mdMetadata, bean );
        } else {
            mdMetadata.setDistributionInfo( null );
        }

        // lineage
        if ( bean.getLineage() != null && bean.getLineage().trim().length() > 0 ) {
            mdMetadata.getDataQualityInfo().getDQDataQuality().getLineage().getLILineage().getStatement().setCharacterString(
                                                                                                                              bean.getLineage() );
        } else {
            mdMetadata.setDataQualityInfo( null );
        }

        // resource constraints
        List<ResourceConstraints> list = mdMetadata.getIdentificationInfo().getMDDataIdentification().getResourceConstraints();
        if ( bean.getAccessConstraints() == null ) {
            list.clear();
        } else {
            MDLegalConstraints legal = null;
            for ( ResourceConstraints resourceConstraints : list ) {
                if ( resourceConstraints.getMDLegalConstraints() != null ) {
                    legal = resourceConstraints.getMDLegalConstraints();
                    legal.getAccessConstraints().getMDRestrictionCode().setCodeListValue( bean.getAccessConstraints() );
                    legal.getAccessConstraints().getMDRestrictionCode().setValue( bean.getAccessConstraints() );
                }
            }
        }

        insertIntoCSW( responseHandler, mdMetadata );
    }

    private void setDistributionInfo( MDMetadata mdMetadata, MetadataBean bean ) {
        if ( mdMetadata.getDistributionInfo() == null ) {
            DistributionInfo distInfo = new DistributionInfo();
            mdMetadata.setDistributionInfo( distInfo );
        }
        MDDistribution mddist = mdMetadata.getDistributionInfo().getMDDistribution();
        if ( mddist == null ) {
            mddist = new MDDistribution();
            TransferOptions to = new TransferOptions();
            MDDigitalTransferOptions dto = new MDDigitalTransferOptions();
            OnLine ol = new OnLine();
            CIOnlineResource ciolr = new CIOnlineResource();
            ciolr.setLinkage( new Linkage() );
            ol.setCIOnlineResource( ciolr );
            dto.setOnLine( ol );
            to.setMDDigitalTransferOptions( dto );
            mddist.setTransferOptions( to );
            mdMetadata.getDistributionInfo().setMDDistribution( mddist );
        }
        MDDigitalTransferOptions to = mddist.getTransferOptions().getMDDigitalTransferOptions();
        to.getOnLine().getCIOnlineResource().getLinkage().setURL( bean.getTransferOnline() );
        if ( bean.getTransferFormatName() != null && bean.getTransferFormatName().length() > 0 ) {
            DistributionFormat format = mdMetadata.getDistributionInfo().getMDDistribution().getDistributionFormat();
            if ( format == null ) {
                format = new DistributionFormat();
                MDFormat mdf = new MDFormat();
                mdf.setName( new Name() );
                mdf.setVersion( new Version() );
                format.setMDFormat( mdf );
                mdMetadata.getDistributionInfo().getMDDistribution().setDistributionFormat( format );
            }
            format.getMDFormat().getName().setCharacterString( bean.getTransferFormatName() );
            if ( bean.getTransferFormatVersion() != null && bean.getTransferFormatVersion().length() > 0 ) {
                format.getMDFormat().getVersion().setCharacterString( bean.getTransferFormatVersion() );
            } else {
                format.getMDFormat().getVersion().setCharacterString( "unknown" );
            }
        } else {
            mdMetadata.getDistributionInfo().getMDDistribution().setDistributionFormat( null );
        }
    }

    @SuppressWarnings("unchecked")
    private String insertIntoCSW( ResponseHandler responseHandler, MDMetadata mdMetadata )
                            throws JAXBException, IOException, HttpException, SAXException, XMLParsingException {

        JAXBContext jc = JAXBContext.newInstance( "org.deegree.portal.cataloguemanager.model" );
        Marshaller m = jc.createMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
        m.marshal( mdMetadata, bos );
        XMLFragment md = new XMLFragment();
        md.load( new ByteArrayInputStream( bos.toByteArray() ), XMLFragment.DEFAULT_URL );
        // FileUtils.writeToFile( "e:/temp/servicemetadata.xml", md.getAsPrettyString() );
        URL url = getClass().getResource( "/org/deegree/portal/cataloguemanager/control/resources/insert.xml" );
        XMLFragment insert = new XMLFragment( url );
        Element elem = XMLTools.getElement( insert.getRootElement(), "csw202:Insert",
                                            CommonNamespaces.getNamespaceContext() );
        XMLTools.copyNode( md.getRootElement().getOwnerDocument(), elem );

        Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        while ( en.hasMoreElements() ) {
            String name = (String) en.nextElement();
            if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                 && !name.equalsIgnoreCase( "user-agent" ) ) {
                map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
            }
        }
        HttpMethod post = HttpUtils.performHttpPost( conf.getCatalogueURL(), insert, 60000, null, null, map );
        String s = post.getResponseBodyAsString();
        if ( s.toLowerCase().indexOf( "exception" ) > -1 ) {
            ExceptionBean eb = new ExceptionBean( getClass().getName(), "insert failed" );
            responseHandler.writeAndClose( true, eb );
        } else {
            responseHandler.writeAndClose( "insert performed" );
        }

        return s;
    }

    protected void setContact( MDMetadata mdMetadata, MetadataBean bean ) {
        Contact contact = mdMetadata.getContact();
        CIResponsibleParty respParty = contact.getCIResponsibleParty();
        respParty.getIndividualName().setCharacterString( bean.getContactIndividualName() );
        respParty.getOrganisationName().setCharacterString( bean.getContactOrganisationName() );
        respParty.getRole().getCIRoleCode().setCodeListValue( bean.getContactRole() );
        ContactInfo ci = respParty.getContactInfo();
        Phone phone = ci.getCIContact().getPhone();
        phone.getCITelephone().getFacsimile().setCharacterString( bean.getContactFacsimile() );
        phone.getCITelephone().getVoice().setCharacterString( bean.getContactVoice() );
        Address address = ci.getCIContact().getAddress();
        address.getCIAddress().getCity().setCharacterString( bean.getContactCity() );
        address.getCIAddress().getCountry().setCharacterString( bean.getContactCountry() );
        address.getCIAddress().getDeliveryPoint().setCharacterString( bean.getContactDeliveryPoint() );
        address.getCIAddress().getElectronicMailAddress().setCharacterString( bean.getContactEmailAddress() );
        address.getCIAddress().getPostalCode().setCharacterString( bean.getContactPostalCode() );
    }

    protected void setExtent( EXExtent extent, MetadataBean bean )
                            throws CRSTransformationException {
        extent.getDescription().setCharacterString( getGeogrDesc( bean.getGeogrDescription() ) );
        EXTemporalExtent te = extent.getTemporalElement().getEXTemporalExtent();
        TimePeriod tp = te.getExtent().getTimePeriod();

        tp.setBeginPosition( bean.getBegin() );
        tp.setEndPosition( bean.getEnd() );
        EXGeographicBoundingBox bbox = extent.getGeographicElement().getEXGeographicBoundingBox();
        Envelope env = getEnvelope( bean.getGeogrDescription() );
        bbox.getWestBoundLongitude().setDecimal( (float) env.getMin().getX() );
        bbox.getSouthBoundLatitude().setDecimal( (float) env.getMin().getY() );
        bbox.getEastBoundLongitude().setDecimal( (float) env.getMax().getX() );
        bbox.getNorthBoundLatitude().setDecimal( (float) env.getMax().getY() );
    }

    /**
     * @param geographicIdentifier
     * @return
     */
    private String getGeogrDesc( String geographicIdentifier ) {
        List<SpatialExtent> extents = conf.getSpatialExtents();
        for ( SpatialExtent spatialExtent : extents ) {
            if ( spatialExtent.getId().equalsIgnoreCase( geographicIdentifier ) ) {
                return spatialExtent.getName();
            }
        }
        return "-";
    }

    protected Envelope getEnvelope( String geogrDescription )
                            throws CRSTransformationException {
        List<SpatialExtent> spatialExtents = conf.getSpatialExtents();
        String bbox = "-180,-90,180,90";
        for ( SpatialExtent spatialExtent : spatialExtents ) {
            if ( spatialExtent.getId().equals( geogrDescription ) ) {
                bbox = spatialExtent.getBbox();
                break;
            }
        }
        return GeometryFactory.createEnvelope( bbox, crs );
    }

    protected void setPointOfContact( PointOfContact pointOfContact, MetadataBean bean ) {
        PointOfContact poc = pointOfContact;
        CIResponsibleParty respParty = poc.getCIResponsibleParty();
        respParty.getIndividualName().setCharacterString( bean.getPocIndividualName() );
        respParty.getOrganisationName().setCharacterString( bean.getPocOrganisationName() );
        respParty.getRole().getCIRoleCode().setCodeListValue( bean.getPocRole() );
        ContactInfo ci = respParty.getContactInfo();
        Phone phone = ci.getCIContact().getPhone();
        phone.getCITelephone().getFacsimile().setCharacterString( bean.getPocFacsimile() );
        phone.getCITelephone().getVoice().setCharacterString( bean.getPocVoice() );
        Address address = ci.getCIContact().getAddress();
        address.getCIAddress().getCity().setCharacterString( bean.getPocCity() );
        address.getCIAddress().getCountry().setCharacterString( bean.getPocCountry() );
        address.getCIAddress().getDeliveryPoint().setCharacterString( bean.getPocDeliveryPoint() );
        address.getCIAddress().getElectronicMailAddress().setCharacterString( bean.getPocEmailAddress() );
        address.getCIAddress().getPostalCode().setCharacterString( bean.getPocPostalCode() );
    }

    @SuppressWarnings("unchecked")
    protected void setKeywords( DescriptiveKeywords descriptiveKeywords, MetadataBean bean ) {
        List<Keyword> list = descriptiveKeywords.getMDKeywords().getKeyword();
        list.clear();
        List<String> kw = bean.getKeywords();
        for ( String keyword : kw ) {
            Keyword key = new Keyword();
            key.setCharacterString( keyword );
            list.add( key );
        }
    }

    protected void setInspireKeyword( DescriptiveKeywords descriptiveKeywords, MetadataBean bean ) {
        List<Keyword> list = descriptiveKeywords.getMDKeywords().getKeyword();
        Keyword key = new Keyword();
        key.setCharacterString( bean.getInspireDataTheme() );
        list.add( key );
    }

    protected void addDate( List<Date> dates, String type, String value ) {
        Date date = new Date();
        CIDate cidate = new CIDate();
        cidate.setDate( date );
        DateType dt = new DateType();
        CIDateTypeCode dtc = new CIDateTypeCode();
        dtc.setCodeList( "http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode" );
        dtc.setCodeListValue( type );
        dt.setCIDateTypeCode( dtc );
        cidate.setDateType( dt );
        Date dateValue = new Date();
        dateValue.setDateTime( value + "T00:00:00Z" );
        cidate.setDate( dateValue );
        date.setCIDate( cidate );
        dates.add( date );
    }

}
