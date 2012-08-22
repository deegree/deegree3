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

package org.deegree.ogcwebservices.csw.iso_profile.ebrim;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.manager.Insert;
import org.deegree.ogcwebservices.csw.manager.Manager;
import org.deegree.ogcwebservices.csw.manager.Operation;
import org.deegree.ogcwebservices.csw.manager.Transaction;
import org.deegree.ogcwebservices.csw.manager.TransactionResult;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.ogcwebservices.wfs.operation.LockFeature.ALL_SOME_TYPE;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionDocument;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert.ID_GEN;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The <code>InsertTransactionHandler</code> class will cut an csw/wrs ebrim insert transaction into four differend
 * transactions, some of which are handled as wfs transactions. For each record in an Insert Transaction the basic
 * workflow is following:
 * <ol>
 * <li>find out if the to id of the to inserted record is allready in the wfs database</li>
 * <li>if so, set it's app:status value to "invalid"</li>
 * <li>insert / update the records</li>
 * <li>create an audittrail, that is an app:AuditableEvent of the insertion</li>
 * </ol>
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class InsertTransactionHandler {

    private static ILogger LOG = LoggerFactory.getLogger( InsertTransactionHandler.class );

    private Transaction originalTransaction;

    private Insert insert;

    private URI appURI;

    private String userName;

    /**
     * Creates an TransactionHandler which will be able to handle csw/ebrim inserts as defined in the wrs spec.
     * 
     * @param originalTransaction
     *            parsed from the incoming HttpServletRequest.
     * @param insert
     *            InsertOperation to be handled (as part of the original Transaction) may not be null;
     * @param appURI
     *            defining a namespace in which the wfs RegistryObjects Recide.
     * @param userName
     *            of the users which wants to insert registryObjects, if not set it will be set to anonymous.
     */
    public InsertTransactionHandler( Transaction originalTransaction, Insert insert, URI appURI, String userName ) {
        if ( originalTransaction == null ) {
            throw new InvalidParameterException( "The transaction parameter may not be null" );
        }
        if ( insert == null ) {
            throw new InvalidParameterException( "The insert parameter may not be null" );
        }
        this.originalTransaction = originalTransaction;
        this.insert = insert;
        if ( appURI == null ) {
            try {
                appURI = new URI( "http://www.deegree.org/app" );
            } catch ( URISyntaxException e ) {
                // nothing to do here.
            }
        } else {
            this.appURI = appURI;
        }
        if ( userName == null || "".equals( userName ) ) {
            userName = "anonymous";
        }
        this.userName = userName;

    }

    /**
     * This method will handle the insert (given from
     * 
     * @param transactionManager
     *            which can handle the csw transactions and allows the access to a localwfs, if null an
     *            InvalidParameterException will be thrown.
     * @param resultValues
     *            an array[3] in which the number of insertions (resultValues[0]) and/or updates (resultValues[2]) will
     *            be saved. If resultValues.length != 3 an InvalidParameterException will be thrown.
     * @return the brief representation of the inserted (not updated) elements.
     * @throws OGCWebServiceException
     */
    public List<Element> handleInsertTransaction( Manager transactionManager, int[] resultValues )
                            throws OGCWebServiceException {
        if ( transactionManager == null ) {
            throw new InvalidParameterException( "The transactionManager may not be null" );
        }
        if ( resultValues.length != 3 ) {
            throw new InvalidParameterException( "The length of the resultValues array must be 3" );
        }

        List<Element> records = insert.getRecords();

        // Some properterypaths which are used for the creation of a complex filter.
        QualifiedName registryObject = new QualifiedName( "app", "RegistryObject", appURI );
        Expression iduriExpr = new PropertyName( new QualifiedName( "app", "liduri", appURI ) );

        Expression statusExpr = new PropertyName( new QualifiedName( "app", "status", appURI ) );
        PropertyIsCOMPOperation validOperator = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                                             statusExpr, new Literal( "valid" ) );

        PropertyIsCOMPOperation emptyOperator = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                                             statusExpr, new Literal( "" ) );
        ComplexFilter vFilter = new ComplexFilter( validOperator );
        ComplexFilter eFilter = new ComplexFilter( emptyOperator );
        ComplexFilter statusFilter = new ComplexFilter( vFilter, eFilter, OperationDefines.OR );

        FeatureCollection featureCollectionOnId = null;
        WFService localWFS = transactionManager.getWFService();

        List<Element> briefRecords = new ArrayList<Element>( records.size() );

        /**
         * Iterate over all records and for each record do the following, <code>
         * 1) find out if the to id of the to inserted record is allready in the wfs database
         * 2) if so, set it's app:status value to "invalid"
         * 3) insert / update the records
         * 4) create an audittrail, that is an app:AuditableEvent of the insertion
         * </code>
         */

        for ( int recordCount = 0; recordCount < records.size(); ++recordCount ) {
            Element record = records.get( recordCount );
            String auditableEventType = "Created";
            String oldID = record.getAttribute( "id" );
            if ( oldID == null || "".equals( oldID ) ) {
                throw new OGCWebServiceException( "You are trying to insert a(n) " + record.getNodeName()
                                                  + " which has no 'id' attribute set, this is a required attribute." );
            }
            String predecessorID = oldID;
            String logicalID = record.getAttribute( "lid" );
            if ( logicalID == null || "".equals( logicalID ) ) {
                // throw new OGCWebServiceException( "You are trying to insert a(n) " + record.getNodeName()
                // + " which has no 'lid' attribute set, for this registry, this is a required attribute." );
                LOG.logDebug( " no lid given, setting attribute to value of id" );
                logicalID = oldID;
                record.setAttribute( "lid", oldID );
            }

            String home = record.getAttribute( "home" );
            if ( home == null ) {
                home = "";
            }

            // Expression idLiteral = new Literal( oldID );
            Expression idLiteral = new Literal( logicalID );
            PropertyIsCOMPOperation idOperator = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                                              iduriExpr, idLiteral );
            ComplexFilter idFilter = new ComplexFilter( idOperator );
            ComplexFilter idAndStatusFilter = new ComplexFilter( idFilter, statusFilter, OperationDefines.AND );
            try {
                // FeatureResult fr = sendWFSGetFeature( localWFS, registryObject, idFilter );
                FeatureResult fr = sendWFSGetFeature( localWFS, registryObject, idAndStatusFilter );
                if ( fr != null ) {
                    featureCollectionOnId = (FeatureCollection) fr.getResponse();
                }
            } catch ( OGCWebServiceException e ) {
                throw new OGCWebServiceException( "The insertion of " + record.getNodeName() + " failed because: "
                                                  + e.getMessage() );
            }
            if ( featureCollectionOnId == null || "".equals( featureCollectionOnId.getId() ) ) {
                throw new OGCWebServiceException( "The insertion of " + record.getNodeName() + " failed." );
            }

            String lockId = featureCollectionOnId.getAttribute( "lockId" );
            LOG.logDebug( " InsertHandler, the GetFeature lock is: " + lockId );
            if ( lockId == null || "".equals( lockId ) ) {
                throw new OGCWebServiceException( "Couldn't get a lock for " + record.getNodeName()
                                                  + ". This object can therefore not be inserted." );
            }
            String numbOfFeatures = featureCollectionOnId.getAttribute( "numberOfFeatures" );
            int featureCount = 0;
            try {
                featureCount = Integer.parseInt( numbOfFeatures );
                LOG.logDebug( " InsertHandler: the number of features in the GetFeatureWithLock was: " + featureCount );
            } catch ( NumberFormatException nfe ) {
                // nottin
            }
            // Check the number of hits we've found, if the id allready exists it means we want to set the status of the
            // object to invalid.
            // String newID = id;
            if ( featureCount > 1 ) {
                throw new OGCWebServiceException( "The lid of this element: " + record.getNodeName()
                                                  + " is not unique. This object can therefore not be inserted." );
            } else if ( featureCount == 1 ) {
                int totalUpdated = changeStatusOfObject( lockId, registryObject, idAndStatusFilter,
                                                         record.getNodeName(), localWFS );

                Feature f = featureCollectionOnId.getFeature( 0 );
                if ( f == null ) {
                    LOG.logError( "No feature found!!!!!" );
                } else {
                    FeatureProperty iduriProperty = f.getDefaultProperty( new QualifiedName( "app", "iduri", appURI ) );
                    if ( iduriProperty == null ) {
                        LOG.logError( "The id of this element: "
                                      + record.getNodeName()
                                      + " is not found in the registry. No association of type 'predecessor' will be inserted!." );
                    } else {
                        predecessorID = (String) iduriProperty.getValue();
                        if ( predecessorID == null || "".equals( predecessorID.trim() ) ) {
                            LOG.logError( "The registry helds an id of this element: "
                                          + record.getNodeName()
                                          + " but it is empty. An association of type 'predecessor' will be inserted with to the oldID!." );
                            predecessorID = oldID;
                        } else {
                            LOG.logDebug( " setting predecessorID to id of the registry (" + predecessorID + ")." );
                        }
                        LOG.logDebug( " wcsFilter: total updated wfs:records (should be >= 1) = " + totalUpdated );
                        if ( totalUpdated == 1 ) {
                            auditableEventType = "Versioned";
                        }
                    }
                }
            }

            // send the insertion to wcs and insert the auditable event

            String newID = UUID.randomUUID().toString();

            if ( "Versioned".equals( auditableEventType ) ) {
                record.setAttribute( "id", newID );
            }

            List<Element> tmpRecords = new ArrayList<Element>( 1 );
            tmpRecords.add( record );

            Insert ins = new Insert( insert.getHandle(), tmpRecords );
            List<Operation> tmpOp = new ArrayList<Operation>( 1 );
            tmpOp.add( ins );
            Transaction transaction = new Transaction( originalTransaction.getVersion(), originalTransaction.getId(),
                                                       originalTransaction.getVendorSpecificParameters(), tmpOp, false );
            TransactionResult tmpInsertResult = null;
            try {
                tmpInsertResult = transactionManager.transaction( transaction );
            } catch ( OGCWebServiceException ogws ) {
                throw new OGCWebServiceException( "CSW Insert Transaction: Error while inserting '"
                                                  + record.getNodeName() + "' with id='" + oldID + "' because: "
                                                  + ogws.getMessage() );
            }

            if ( tmpInsertResult == null || tmpInsertResult.getTotalInserted() != 1 ) {
                throw new OGCWebServiceException(
                                                  "The insertion of the element: "
                                                                          + record.getNodeName()
                                                                          + " failed, because the transactionresult is null or the number of inserted objects wasn't 1." );
            }

            if ( featureCount == 1 ) {
                // update
                resultValues[2]++;
            } else {
                // insert
                resultValues[0]++;
            }
            // First create the necessary Features
            List<Feature> newObjectsInDB = new ArrayList<Feature>();
            newObjectsInDB.add( createAuditableEvent( localWFS, oldID, home, auditableEventType, userName ) );
            if ( "Versioned".equals( auditableEventType ) ) {
                newObjectsInDB.add( createAssociation( localWFS, newID, predecessorID ) );

                // Now update all following associations which referenced the oldID.
                for ( int i = ( recordCount + 1 ); i < records.size(); ++i ) {
                    Element tmpRec = records.get( i );
                    if ( CommonNamespaces.OASIS_EBRIMNS.toASCIIString().equals( tmpRec.getNamespaceURI() )
                         && "Association".equals( tmpRec.getLocalName() ) ) {
                        String sourceObject = tmpRec.getAttribute( "sourceObject" );
                        String targetObject = tmpRec.getAttribute( "targetObject" );
                        if ( oldID.equals( sourceObject ) ) {
                            LOG.logDebug( " Updating 'rim:Association/@sourceObject' Attribute to new id: " + newID
                                          + " after an update of registryObject: " + record.getLocalName() );
                            tmpRec.setAttribute( "sourceObject", newID );
                        }
                        if ( oldID.equals( targetObject ) ) {
                            LOG.logDebug( " Updating 'rim:Association/@targetObject' Attribute to new id: " + newID
                                          + " after an update of registryObject: " + record.getLocalName() );
                            tmpRec.setAttribute( "targetObject", newID );
                        }
                    }
                }
            }
            insertFeatures( localWFS, newObjectsInDB, record.getNodeName() );
            // sendAuditableEvent( transactionManager.getWfsService(), record.getNodeName(), auditableEventType,
            // username,
            // id, home );
            // create a brief record description of the inserted record.
            briefRecords.add( generateBriefRecord( record ) );
        }
        return briefRecords;
    }

    /**
     * 
     * @param localWFS
     * @param registryObject
     * @param filter
     * @return the FeatureResult of the given filter or <code>null</code> if something went wrong.
     * @throws OGCWebServiceException
     */
    private FeatureResult sendWFSGetFeature( WFService localWFS, QualifiedName registryObject, ComplexFilter filter )
                            throws OGCWebServiceException {
        Query q = Query.create( registryObject, filter );
        GetFeatureWithLock gfwl = GetFeatureWithLock.create( "1.1.0", "0", "no_handle", RESULT_TYPE.RESULTS,
                                                             "text/xml; subtype=gml/3.1.1", -1, 0, -1, -1,
                                                             new Query[] { q }, null, 300000l, ALL_SOME_TYPE.ALL );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                GetFeatureDocument gd = XMLFactory.export( gfwl );
                LOG.logDebug( "The getFeature with lock request: " + gd.getAsPrettyString() );
            } catch ( IOException e ) {
                LOG.logError( "InsertTransactionHandler: An error occurred while trying to get a debugging output for the generated GetFeatureDocument: "
                              + e.getMessage() );
            } catch ( XMLParsingException e ) {
                LOG.logError( "InsertTransactionHandler: An error occurred while trying to get a debugging output for the generated GetFeatureDocument: "
                              + e.getMessage() );
            }
        }

        Object response = localWFS.doService( gfwl );
        if ( response instanceof FeatureResult ) {
            LOG.logDebug( "InsertHandler tried to get A feature with Lock, with a valid response from the localwfs" );
            return (FeatureResult) response;
        }
        return null;
    }

    /**
     * This method will create a WFSTransaction containing one update operation, which will set the app:status of the
     * app:RegistryObject found using the complexFilter to superseded.
     * 
     * @param newId
     *            of the registryObject
     * @param lockId
     *            which was set while querying the app:RegistryObject for it's app:status
     * @return the number of updated records, this value should only be 1 or 0.
     * @throws OGCWebServiceException
     *             if something went wrong.
     */
    private int changeStatusOfObject( String lockId, QualifiedName registryObject, ComplexFilter filter,
                                      String originalRecordNodeName, WFService localWFS )
                            throws OGCWebServiceException {
        List<TransactionOperation> operations = new ArrayList<TransactionOperation>();
        Map<PropertyPath, FeatureProperty> properties = new HashMap<PropertyPath, FeatureProperty>();

        // the new status value, e.g. app:RegistryObject/app:status=invalid
        QualifiedName status = new QualifiedName( "app", "status", appURI );
        PropertyPath statusPP = PropertyPathFactory.createPropertyPath( registryObject );
        statusPP.append( PropertyPathFactory.createPropertyPathStep( status ) );

        // // the new id value e.g app:RegistryObject/app:iduri=newId
        // QualifiedName iduri = new QualifiedName( "app", "iduri", appURI );
        // PropertyPath iduriPP = PropertyPathFactory.createPropertyPath( registryObject );
        // iduriPP.append( PropertyPathFactory.createAttributePropertyPathStep( iduri ) );

        // Adding the properties (e.g. the status=ivalid and the iduri=newId) to the wfs:UpdateOperation.
        properties.put( statusPP, FeatureFactory.createFeatureProperty( status, "superseded" ) );
        // properties.put( iduriPP, FeatureFactory.createFeatureProperty( iduri, newId ) );

        operations.add( new org.deegree.ogcwebservices.wfs.operation.transaction.Update( "no_handle", registryObject,
                                                                                         properties, filter ) );
        org.deegree.ogcwebservices.wfs.operation.transaction.Transaction wfsTransaction = new org.deegree.ogcwebservices.wfs.operation.transaction.Transaction(
                                                                                                                                                                "0",
                                                                                                                                                                "1.1.0",
                                                                                                                                                                null,
                                                                                                                                                                lockId,
                                                                                                                                                                operations,
                                                                                                                                                                true,
                                                                                                                                                                null );
        int totalUpdated = 0;
        try {
            Object response = localWFS.doService( wfsTransaction );
            if ( response instanceof TransactionResponse ) {
                totalUpdated = ( (TransactionResponse) response ).getTotalUpdated();
            }
        } catch ( OGCWebServiceException e ) {
            throw new OGCWebServiceException( "The insertion of " + originalRecordNodeName + " failed: "
                                              + e.getMessage() );
        }
        return totalUpdated;

    }

    /**
     * creates a brief representation of the given RegistryObject element, with following values (wrs spec):
     * <ul>
     * <li>rim:RegistryObject/@id</li>
     * <li>rim:RegistryObject/@lid</li>
     * <li>rim:RegistryObject/@objectType</li>
     * <li>rim:RegistryObject/@status</li>
     * <li>rim:RegistryObject/rim:VersionInfo</li>
     * </ul>
     * 
     * @return a brief record description of the given ebrim:RegistryObject
     */
    private Element generateBriefRecord( Element record ) {
        Document doc = XMLTools.create();
        Element resultElement = doc.createElement( "csw:result" );
        Element a = (Element) doc.importNode( record, false );
        resultElement.appendChild( a );
        List<Node> attribs = null;
        try {
            attribs = XMLTools.getNodes( a, "./@*", CommonNamespaces.getNamespaceContext() );
        } catch ( XMLParsingException e1 ) {
            LOG.logError(
                          "InsertTransactionHandler: an error occurred while creating a briefrecord for registryObject: "
                                                  + record.getNodeName(), e1 );
        }
        // NamedNodeMap attribs = a.getAttributes();
        if ( attribs != null ) {
            for ( Node attribute : attribs ) {
                // Attr attribute = (Attr) attribs.item( i );
                String localName = attribute.getLocalName();

                LOG.logDebug( "From: " + a.getNodeName() + " found attribute (localname): " + localName );
                if ( !( "id".equals( localName ) || "lid".equals( localName ) || "objectType".equals( localName ) || "status".equals( localName ) ) ) {
                    // resultElement.setAttributeNode( (Attr)attribs.item(i) );
                    LOG.logDebug( " From: " + a.getNodeName() + " removing attribute (localname): " + localName );
                    String namespace = attribute.getBaseURI();
                    // a.removeChild( attribs.item(i) );
                    a.removeAttributeNS( namespace, localName );
                }
            }
        }
        Element versionInfo = null;
        try {
            versionInfo = XMLTools.getElement( record, "rim:VersionInfo", CommonNamespaces.getNamespaceContext() );
            if ( versionInfo != null ) {
                Node vi = doc.importNode( versionInfo, true );
                a.appendChild( vi );
            }
        } catch ( XMLParsingException e ) {
            LOG.logError(
                          "InsertTransactionHandler: an error occurred while creating a briefrecord for registryObject: "
                                                  + record.getNodeName(), e );
        }
        return a;
    }

    /**
     * Creates an association of type "urn:adv:registry:AssociationType:predecessor" which associates an old
     * (updated/superseded) registry object with a new registry object.
     * 
     * @param localWFS
     *            which will be talked to directly (superseding the csw).
     * @param newRegisterID
     *            the id of the new object inserted in the db
     * @param oldRegisterID
     *            the id of the old updated object, superseded in the db
     */
    private Feature createAssociation( WFService localWFS, String newRegisterID, String oldRegisterID ) {

        QualifiedName registryObject = new QualifiedName( "app", "RegistryObject", appURI );
        QualifiedName associationType = new QualifiedName( "app", "Association", appURI );

        MappedFeatureType rootFT = localWFS.getMappedFeatureType( registryObject );
        MappedFeatureType associationFT = localWFS.getMappedFeatureType( associationType );

        List<FeatureProperty> featureProperties = new ArrayList<FeatureProperty>();

        // Generate the Auditable Event complex subfeature

        QualifiedName associationTypeProp = new QualifiedName( "app", "associationType", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( associationTypeProp,
                                                                     "urn:adv:registry:AssociationType:predecessor" ) );

        QualifiedName sourceObject = new QualifiedName( "app", "sourceObject", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( sourceObject, newRegisterID ) );

        QualifiedName targetObject = new QualifiedName( "app", "targetObject", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( targetObject, oldRegisterID ) );

        Feature associationFeature = FeatureFactory.createFeature( null, associationFT, featureProperties );

        // Creation of the RegistryObject
        featureProperties.clear();

        // type
        QualifiedName type = new QualifiedName( "app", "type", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( type, "Association" ) );

        QualifiedName iduri = new QualifiedName( "app", "iduri", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( iduri, UUID.randomUUID().toString() ) );

        // objecttype
        QualifiedName objectType = new QualifiedName( "app", "objectType", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( objectType,
                                                                     "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association" ) );

        // status
        QualifiedName status = new QualifiedName( "app", "status", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( status, "valid" ) );

        // create the auditable Event property with the feature
        QualifiedName association = new QualifiedName( "app", "association", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( association, associationFeature ) );

        Feature rootFeature = FeatureFactory.createFeature( null, rootFT, featureProperties );
        return rootFeature;
    }

    /**
     * Creates an auditable event for the given objectid
     * 
     * TODO shouldn't the slots of the original inserted Object not be handled?
     * 
     * @param localWFS
     *            which will be talked to directly (superseding the csw).
     * @param affectedObjectId
     *            of the object which has been inserted or updated
     * @param affectedHome
     *            of the object which has been inserted or updated
     * @param auditEventType
     *            should be one of 'Created' or 'Updated' (see the ebrim spec)
     * @param username
     *            of the person doing the insertion
     */
    private Feature createAuditableEvent( WFService localWFS, String affectedObjectId, String affectedHome,
                                          String auditEventType, String username ) {
        String requestId = originalTransaction.getId();

        QualifiedName registryObject = new QualifiedName( "app", "RegistryObject", appURI );
        QualifiedName auditableEventType = new QualifiedName( "app", "AuditableEvent", appURI );
        QualifiedName objectRefType = new QualifiedName( "app", "ObjectRef", appURI );

        MappedFeatureType rootFT = localWFS.getMappedFeatureType( registryObject );
        MappedFeatureType auditableEventFT = localWFS.getMappedFeatureType( auditableEventType );
        MappedFeatureType objectRefFT = localWFS.getMappedFeatureType( objectRefType );

        List<FeatureProperty> featureProperties = new ArrayList<FeatureProperty>();

        // Creating the Objectref
        QualifiedName replacedURI = new QualifiedName( "app", "iduri", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( replacedURI, affectedObjectId ) );

        QualifiedName replacedHome = new QualifiedName( "app", "home", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( replacedHome, affectedHome ) );

        QualifiedName createReplica = new QualifiedName( "app", "createReplica", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( createReplica, "false" ) );

        Feature objectRefFeature = FeatureFactory.createFeature( null, objectRefFT, featureProperties );

        // Generate the Auditable Event complex subfeature
        featureProperties.clear();

        QualifiedName eventType = new QualifiedName( "app", "eventType", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( eventType, auditEventType ) );

        QualifiedName timestamp = new QualifiedName( "app", "timestamp", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( timestamp, TimeTools.getISOFormattedTime() ) );

        QualifiedName usernameQName = new QualifiedName( "app", "username", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( usernameQName, username ) );

        QualifiedName requestIdQName = new QualifiedName( "app", "requestId", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( requestIdQName, requestId ) );

        // add the affected ObjectsFeatureType to the affectedObjects property
        QualifiedName affectedObjects = new QualifiedName( "app", "affectedObjects", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( affectedObjects, objectRefFeature ) );

        Feature auditEventFeature = FeatureFactory.createFeature( null, auditableEventFT, featureProperties );

        // Creation of the RegistryObject
        featureProperties.clear();

        // type
        QualifiedName type = new QualifiedName( "app", "type", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( type, "AuditableEvent" ) );

        QualifiedName iduri = new QualifiedName( "app", "iduri", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( iduri, UUID.randomUUID().toString() ) );

        // objecttype
        QualifiedName objectType = new QualifiedName( "app", "objectType", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( objectType,
                                                                     "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent" ) );

        // status
        QualifiedName status = new QualifiedName( "app", "status", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( status, "valid" ) );

        // create the auditable Event property with the feature
        QualifiedName auditableEvent = new QualifiedName( "app", "auditableEvent", appURI );
        featureProperties.add( FeatureFactory.createFeatureProperty( auditableEvent, auditEventFeature ) );

        Feature rootFeature = FeatureFactory.createFeature( null, rootFT, featureProperties );

        return rootFeature;
    }

    /**
     * Puts an auditable event for the given objectid into the database, thus resulting in an AuditTrail for the
     * inserted/updated Object.
     * 
     * 
     * @param localWFS
     *            which will be talked to directly (superseding the csw).
     * @param featuresToInsert
     *            an array of features (either an auditableEvent or an auditableEvent and an Association (if an update
     *            occurred) ).
     * @param originalInsertObjectName
     *            the name of the object to be inserted (used for debug messages)
     * @throws OGCWebServiceException
     */
    private void insertFeatures( WFService localWFS, List<Feature> featuresToInsert, String originalInsertObjectName )
                            throws OGCWebServiceException {
        String requestId = originalTransaction.getId();
        if ( featuresToInsert.size() == 0 ) {
            LOG.logError( "CSW (Ebrim) InsertTransactionHandler: there were no features to insert, this may not be (at least an auditableEvent feature should be inserted)!" );
            return;
        }
        Feature[] fA = new Feature[featuresToInsert.size()];
        for ( int i = 0; i < fA.length; ++i ) {
            fA[i] = featuresToInsert.get( i );
        }
        FeatureCollection fc = FeatureFactory.createFeatureCollection( requestId, fA );

        org.deegree.ogcwebservices.wfs.operation.transaction.Insert wfsInsert = new org.deegree.ogcwebservices.wfs.operation.transaction.Insert(
                                                                                                                                                 "no_handle",
                                                                                                                                                 ID_GEN.GENERATE_NEW,
                                                                                                                                                 null,
                                                                                                                                                 fc );
        List<TransactionOperation> ops = new ArrayList<TransactionOperation>( 1 );
        ops.add( wfsInsert );
        org.deegree.ogcwebservices.wfs.operation.transaction.Transaction transaction = new org.deegree.ogcwebservices.wfs.operation.transaction.Transaction(
                                                                                                                                                             originalTransaction.getId(),
                                                                                                                                                             "1.1.0",
                                                                                                                                                             null,
                                                                                                                                                             null,
                                                                                                                                                             ops,
                                                                                                                                                             true,
                                                                                                                                                             null );

        try {
            localWFS.doService( transaction );
        } catch ( OGCWebServiceException e ) {
            String features = "AuditableEvent ";
            if ( fA.length > 1 )
                features += "and an Association ";
            throw new OGCWebServiceException( "Could not insert an " + features
                                              + "for the insertion/update of the RegistryObject: "
                                              + originalInsertObjectName + " because: " + e.getMessage() );
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                TransactionDocument doc = XMLFactory.export( transaction );
                LOG.logDebug( " The auditable event created for the insertion of '" + originalInsertObjectName
                              + "' is:\n" + doc.getAsPrettyString() );
            } catch ( IOException e ) {
                LOG.logError( "InsertTransactionHandler: An error occurred while trying to create an auditable Event for insertion of the '"
                              + originalInsertObjectName + "'. Errormessage: " + e.getMessage() );
            } catch ( XMLParsingException e ) {
                LOG.logError( "InsertTransactionHandler: An error occurred while trying to create an auditable Event for insertion of the '"
                              + originalInsertObjectName + "'. Errormessage: " + e.getMessage() );
            }
        }

    }

}
