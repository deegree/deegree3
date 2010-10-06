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
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso.generating.generatingelements.GenerateOMElement;
import org.deegree.metadata.persistence.iso.parsing.IdUtils;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.RequireInspireCompliance;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ResourceIdentifier implements RecordInspector {

    private static final Logger LOG = getLogger( ResourceIdentifier.class );

    private final RequireInspireCompliance ric;

    private final Connection conn;

    private final XMLAdapter a;

    private boolean isRic;

    private final IdUtils util;

    private ResourceIdentifier( RequireInspireCompliance ric, Connection conn ) {
        this.ric = ric;
        this.conn = conn;
        this.util = IdUtils.newInstance( conn );
        this.a = new XMLAdapter();
    }

    public static ResourceIdentifier newInstance( RequireInspireCompliance ric, Connection conn ) {
        return new ResourceIdentifier( ric, conn );
    }

    public boolean checkInspireCompliance() {
        if ( ric == null ) {
            return isRic = false;
        } else {
            return isRic = true;
        }
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
    public List<String> determineResourceIdentifier( List<String> rsList, String id )
                            throws MetadataStoreException {

        if ( checkInspireCompliance() ) {
            boolean generateAutomatic = ric.isGenerateAutomatic();
            if ( generateAutomatic == false ) {
                if ( id != null ) {
                    if ( checkRSListAgainstID( rsList, id ) ) {
                        LOG.info( "The resourceIdentifier has been accepted." );
                        return rsList;
                    }
                }
                LOG.debug( "There was no match between resourceIdentifier and the id-attribute! Without any automatic guarantee this metadata has to be rejected! " );
                JDBCUtils.close( conn );
                throw new MetadataStoreException( "There was no match between resourceIdentifier and the id-attribute!" );
            }
            if ( checkRSListAgainstID( rsList, id ) ) {
                LOG.info( "The resourceIdentifier has been accepted without any automatic creation. " );
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
                LOG.info( "Neither an id nor a resourceIdentifier exists...so this creates a new one. " );
                rsList.add( util.generateUUID() );
                return rsList;
            } else if ( rsList.size() == 0 && id != null ) {
                LOG.info( "An id exists but not a resourceIdentifier...so adapting resourceIdentifier with id. " );
                LOG.debug( "check uuid compliance of id. " );
                if ( util.checkUUIDCompliance( id ) ) {
                    LOG.debug( "true...so take it" );

                    rsList.add( id );
                } else {
                    LOG.debug( "false...so generate a new one" );
                    rsList.add( util.generateUUID() );
                }
                return rsList;
            }
        }
        LOG.info( "No modification happened, so the resourceIdentifierList will be passed through. " );
        rsList.clear();
        return rsList;
    }

    private boolean checkRSListAgainstID( List<String> rsList, String id ) {

        if ( rsList.size() == 0 ) {
            return false;

        } else {

            if ( util.checkUUIDCompliance( rsList.get( 0 ) ) ) {

                if ( util.checkUUIDCompliance( id ) ) {
                    return rsList.get( 0 ).equals( id );
                }

            }

        }
        return false;
    }

    @Override
    public OMElement inspect( OMElement record )
                            throws MetadataStoreException {
        a.setRootElement( record );

        NamespaceContext nsContext = a.getNamespaceContext( record );
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );

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

        LOG.debug( "Creating of resourceIdentifierList finished: " + rsList );
        if ( rsList.isEmpty() ) {
            LOG.debug( "ResourceIdentifier compliance test is not activated so skip it." );
        }
        if ( !rsList.isEmpty() ) {
            if ( dataIdentificationUuId == null ) {
                LOG.debug( "No uuid attribute found, set it from the resourceIdentifier..." );

            } else {
                LOG.debug( "uuid attribute found, but anyway, set it from the resourceIdentifier..." );
                sv_service_OR_md_dataIdentification.getAttribute( new QName( "uuid" ) );
            }
            sv_service_OR_md_dataIdentification.addAttribute( new OMAttributeImpl( "uuid", null, rsList.get( 0 ),
                                                                                   OMAbstractFactory.getOMFactory() ) );

            LOG.debug( "Setting id attribute from the resourceIdentifier..." );
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
                LOG.debug( "Set resourceIdentifier '" + rsList.get( 0 ) + "' after the 'editionDate-element'." );
                editionDate.insertSiblingAfter( resourceID );
            } else if ( edition != null ) {
                LOG.debug( "Set resourceIdentifier '" + rsList.get( 0 ) + "' after the 'edition-element'." );
                edition.insertSiblingAfter( resourceID );
            } else {
                LOG.debug( "Set resourceIdentifier '" + rsList.get( 0 ) + "' after the last 'date-element'." );
                CI_Citation_date.get( CI_Citation_date.size() - 1 ).insertSiblingAfter( resourceID );
            }
        }

        return record;
    }

}
