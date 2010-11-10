//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.persistence.iso.parsing.inspectation;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMAttributeImpl;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso.generating.generatingelements.GenerateOMElement;
import org.deegree.metadata.persistence.iso.parsing.IdUtils;
import org.deegree.metadata.persistence.iso19115.jaxb.AbstractInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.InspireInspector;
import org.slf4j.Logger;

/**
 * ResourceIdentifier is a subInspector of the InspireInspector. If there is no INSPIRE needed, this inspector is
 * unnecessary.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ResourceIdentifier implements InspireCompliance {

    private static final Logger LOG = getLogger( ResourceIdentifier.class );

    private static ResourceIdentifier instance;

    private final InspireInspector ric;

    private Connection conn;

    private static final String NAME = InspireInspector.class.getSimpleName();

    private final XMLAdapter a;

    private IdUtils util;

    public ResourceIdentifier( InspireInspector ric ) {
        this.ric = ric;
        this.a = new XMLAdapter();
        instance = this;
    }

    public boolean checkAvailability( AbstractInspector inspector ) {
        InspireInspector ric = (InspireInspector) inspector;
        if ( ric == null ) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the required constraint of the equality of the attribute
     * 
     * <Code>gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier</Code>
     * and <Code>gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/@id</Code> is given.
     * 
     * @param rsList
     *            the list of RS_Identifier, not <Code>null</Code>.
     * @param id
     *            the id attribute if exists, can be <Code>null</Code>.
     * @return a list of RS_Identifier, not <Code>null</Code> but empty, at least.
     * @throws MetadataStoreException
     */
    private List<String> determineResourceIdentifier( List<String> rsList, String id )
                            throws MetadataInspectorException {

        if ( checkAvailability( ric ) ) {
            boolean generateAutomatic = ric.isGenerateMissingResourceIdentifier();
            if ( generateAutomatic == false ) {
                if ( id != null ) {
                    if ( util.checkUUIDCompliance( id ) ) {
                        if ( checkRSListAgainstID( rsList, id ) ) {
                            LOG.info( Messages.getMessage( "INFO_RI_ACCEPTED" ) );
                            return rsList;
                        }
                    }
                }
                String msg = Messages.getMessage( "ERROR_RI_ID" );
                LOG.debug( msg );
                JDBCUtils.close( conn );
                throw new MetadataInspectorException( msg );
            }
            if ( checkRSListAgainstID( rsList, id ) ) {
                LOG.info( Messages.getMessage( "INFO_RI_ACCEPTED" ) );
                return rsList;
            }
            /**
             * if both, id and resourceIdentifier exists but different: update id with resourceIdentifier
             * <p>
             * if id exists: update resourceIdentifier with id
             * <p>
             * if resourceIdentifier exists: update id with resourceIdentifier
             * <p>
             * if nothing exists: generate it for id and resourceIdentifier
             */
            if ( rsList.size() == 0 && id == null ) {
                LOG.info( Messages.getMessage( "INFO_RI_NEW" ) );
                rsList.add( util.generateUUID() );
                return rsList;
            } else if ( rsList.size() == 0 && id != null ) {
                LOG.info( Messages.getMessage( "INFO_RI_ADAPT_FROM_ID" ) );
                LOG.debug( Messages.getMessage( "INFO_UUID_COMPLIANCE", id ) );
                if ( util.checkUUIDCompliance( id ) ) {
                    LOG.debug( "Take the id" );

                    rsList.add( id );
                } else {
                    LOG.debug( Messages.getMessage( "INFO_RI_NEW" ) );
                    rsList.add( util.generateUUID() );
                }
                return rsList;
            }
        }
        LOG.info( Messages.getMessage( "INFO_RI_NO_MODIFICATION" ) );
        // rsList.clear();
        return rsList;
    }

    private boolean checkRSListAgainstID( List<String> rsList, String id ) {

        if ( rsList.size() == 0 ) {
            return false;

        } else {

            if ( util.checkUUIDCompliance( rsList.get( 0 ) ) ) {

                if ( id != null && util.checkUUIDCompliance( id ) ) {
                    return rsList.get( 0 ).equals( id );
                }

            }

        }
        return false;
    }

    @Override
    public OMElement inspect( OMElement record, Connection conn )
                            throws MetadataInspectorException {
        this.conn = conn;
        a.setRootElement( record );
        this.util = IdUtils.newInstance( conn );

        NamespaceContext nsContext = a.getNamespaceContext( record );
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContext.addNamespace( "gmd", "http://www.isotc211.org/2005/gmd" );
        nsContext.addNamespace( "gco", "http://www.isotc211.org/2005/gco" );

        OMElement sv_service_OR_md_dataIdentification = a.getElement(
                                                                      record,
                                                                      new XPath(
                                                                                 "./gmd:identificationInfo/srv:SV_ServiceIdentification | ./gmd:identificationInfo/gmd:MD_DataIdentification",
                                                                                 nsContext ) );
        String dataIdentificationId = sv_service_OR_md_dataIdentification.getAttributeValue( new QName( "id" ) );
        String dataIdentificationUuId = sv_service_OR_md_dataIdentification.getAttributeValue( new QName( "uuid" ) );
        List<OMElement> identifier = a.getElements( sv_service_OR_md_dataIdentification,
                                                    new XPath( "./gmd:citation/gmd:CI_Citation/gmd:identifier",
                                                               nsContext ) );

        List<OMElement> CI_Citation_date = a.getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:citation/gmd:CI_Citation/gmd:date",
                                                                     nsContext ) );
        OMElement edition = a.getElement( sv_service_OR_md_dataIdentification,
                                          new XPath( "./gmd:citation/gmd:CI_Citation/edition", nsContext ) );
        OMElement editionDate = a.getElement( sv_service_OR_md_dataIdentification,
                                              new XPath( "./gmd:citation/gmd:CI_Citation/gmd:editionDate", nsContext ) );

        List<String> resourceIdentifierList = new ArrayList<String>();
        for ( OMElement resourceElement : identifier ) {
            String resourceIdentifier = a.getNodeAsString(
                                                           resourceElement,
                                                           new XPath(
                                                                      "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                      nsContext ), null );
            LOG.debug( "resourceIdentifier: '" + resourceIdentifier + "' " );
            resourceIdentifierList.add( resourceIdentifier );

        }
        List<String> rsList = determineResourceIdentifier( resourceIdentifierList, dataIdentificationId );

        LOG.debug( Messages.getMessage( "INFO_RI_FINISHED", rsList ) );
        if ( rsList.isEmpty() ) {
            LOG.debug( Messages.getMessage( "INFO_RI_SKIP" ) );
        }
        if ( !rsList.isEmpty() ) {
            if ( dataIdentificationUuId == null ) {
                LOG.debug( Messages.getMessage( "INFO_RI_NO_UUID" ) );

            } else {
                LOG.debug( Messages.getMessage( "INFO_RI_SET_IT", "uuid attribute" ) );
                sv_service_OR_md_dataIdentification.getAttribute( new QName( "uuid" ) );
            }
            sv_service_OR_md_dataIdentification.addAttribute( new OMAttributeImpl( "uuid", null, rsList.get( 0 ),
                                                                                   OMAbstractFactory.getOMFactory() ) );

            LOG.debug( Messages.getMessage( "INFO_RI_SET_IT", "id attribute" ) );
            OMAttribute attribute_id = sv_service_OR_md_dataIdentification.getAttribute( new QName( "id" ) );

            if ( attribute_id != null ) {

                sv_service_OR_md_dataIdentification.removeAttribute( attribute_id );
            }

            // sv_service_OR_md_dataIdentification.addAttribute( "id", "", null );
            sv_service_OR_md_dataIdentification.addAttribute( new OMAttributeImpl( "id", null, rsList.get( 0 ),
                                                                                   OMAbstractFactory.getOMFactory() ) );

            LOG.info( "id attribute is now: '"
                      + sv_service_OR_md_dataIdentification.getAttributeValue( new QName( "id" ) ) + "' " );
            // LOG.debug( "id attribute is now: '"
            // + sv_service_OR_md_dataIdentification.getAttributeValue( new QName( nsContext.getURI( "gmd" ),
            // "id", "gmd" ) ) + "' " );
        }

        // check where to set the resourceIdentifier element
        if ( identifier.isEmpty() && !rsList.isEmpty() ) {
            OMElement resourceID = GenerateOMElement.newInstance().createMD_ResourceIdentifier( rsList.get( 0 ) );
            if ( editionDate != null ) {
                LOG.debug( Messages.getMessage( "INFO_RI_INSERT_NEW", rsList.get( 0 ), "editionDate" ) );
                editionDate.insertSiblingAfter( resourceID );
            } else if ( edition != null ) {
                LOG.debug( Messages.getMessage( "INFO_RI_INSERT_NEW", rsList.get( 0 ), "edition" ) );
                edition.insertSiblingAfter( resourceID );
            } else {
                LOG.debug( Messages.getMessage( "INFO_RI_INSERT_NEW", rsList.get( 0 ), "date" ) );
                CI_Citation_date.get( CI_Citation_date.size() - 1 ).insertSiblingAfter( resourceID );
            }
        }

        return record;
    }

    public static ResourceIdentifier getInstance() {
        return instance;
    }

}
