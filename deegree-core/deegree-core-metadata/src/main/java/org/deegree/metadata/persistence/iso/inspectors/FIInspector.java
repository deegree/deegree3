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
package org.deegree.metadata.persistence.iso.inspectors;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.metadata.persistence.iso.generating.generatingelements.GenerateOMElement;
import org.deegree.metadata.persistence.iso.parsing.IdUtils;
import org.deegree.metadata.persistence.iso19115.jaxb.FileIdentifierInspector;
import org.slf4j.Logger;

/**
 * Inspects whether the fileIdentifier should be set when inserting a metadata or not and what consequences should
 * occur.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FIInspector implements RecordInspector<ISORecord> {

    private static final Logger LOG = getLogger( FIInspector.class );

    private final FileIdentifierInspector config;

    private final NamespaceBindings nsContext = new NamespaceBindings();

    public FIInspector( FileIdentifierInspector inspector ) {
        this.config = inspector;
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContext.addNamespace( "gmd", "http://www.isotc211.org/2005/gmd" );
        nsContext.addNamespace( "gco", "http://www.isotc211.org/2005/gco" );
    }

    /**
     * 
     * @param fi
     *            the fileIdentifier that should be determined for one metadata, can be <Code>null</Code>.
     * @param rsList
     *            the list of resourceIdentifier, not <Code>null</Code>.
     * @param id
     *            the id-attribute, can be <Code>null<Code>.
     * @param uuid
     *            the uuid-attribure, can be <Code>null</Code>.
     * @return the new fileIdentifier.
     */
    private List<String> determineFileIdentifier( Connection conn, String[] fi, List<String> rsList, String id,
                                                  String uuid, Type connectionType )
                            throws MetadataInspectorException {
        List<String> idList = new ArrayList<String>();
        if ( fi.length != 0 ) {
            for ( String f : fi ) {
                LOG.info( Messages.getMessage( "INFO_FI_AVAILABLE", f.trim() ) );
                idList.add( f.trim() );
            }
            return idList;
        }
        if ( config != null && !config.isRejectEmpty() ) {
            if ( rsList.size() == 0 && id == null && uuid == null ) {

                LOG.debug( Messages.getMessage( "INFO_FI_GENERATE_NEW" ) );
                idList.add( IdUtils.newInstance( conn, connectionType ).generateUUID() );
                LOG.debug( Messages.getMessage( "INFO_FI_NEW", idList ) );
            } else {
                if ( rsList.size() == 0 && id != null ) {
                    LOG.debug( Messages.getMessage( "INFO_FI_DEFAULT_ID", id ) );
                    idList.add( id );
                } else if ( rsList.size() == 0 && uuid != null ) {
                    LOG.debug( Messages.getMessage( "INFO_FI_DEFAULT_UUID", uuid ) );
                    idList.add( uuid );
                } else {
                    LOG.debug( Messages.getMessage( "INFO_FI_DEFAULT_RSID", rsList.get( 0 ) ) );
                    idList.add( rsList.get( 0 ) );
                }
            }
            return idList;
        }
        if ( rsList.size() == 0 ) {
            String msg = Messages.getMessage( "ERROR_REJECT_FI" );
            LOG.debug( msg );
            throw new MetadataInspectorException( msg );
        }
        LOG.debug( Messages.getMessage( "INFO_FI_DEFAULT_RSID", rsList.get( 0 ) ) );
        idList.add( rsList.get( 0 ) );
        return idList;

    }

    @Override
    public ISORecord inspect( ISORecord record, Connection conn, Type connectionType )
                            throws MetadataInspectorException {

        XMLAdapter a = new XMLAdapter( record.getAsOMElement() );
        OMElement rootEl = record.getAsOMElement();

        String[] fileIdentifierString = a.getNodesAsStrings( rootEl,
                                                             new XPath( "./gmd:fileIdentifier/gco:CharacterString",
                                                                        nsContext ) );

        OMElement sv_service_OR_md_dataIdentification = a.getElement( rootEl,
                                                                      new XPath(
                                                                                 "./gmd:identificationInfo/srv:SV_ServiceIdentification | ./gmd:identificationInfo/gmd:MD_DataIdentification",
                                                                                 nsContext ) );
        String dataIdentificationId = sv_service_OR_md_dataIdentification.getAttributeValue( new QName( "id" ) );
        String dataIdentificationUuId = sv_service_OR_md_dataIdentification.getAttributeValue( new QName( "uuid" ) );
        List<OMElement> identifier = a.getElements( sv_service_OR_md_dataIdentification,
                                                    new XPath( "./gmd:citation/gmd:CI_Citation/gmd:identifier",
                                                               nsContext ) );
        List<String> resourceIdentifierList = new ArrayList<String>();
        for ( OMElement resourceElement : identifier ) {
            String resourceIdentifier = a.getNodeAsString( resourceElement,
                                                           new XPath(
                                                                      "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                      nsContext ), null );
            LOG.debug( "resourceIdentifier: '" + resourceIdentifier + "' " );
            resourceIdentifierList.add( resourceIdentifier );

        }

        List<String> idList = determineFileIdentifier( conn, fileIdentifierString, resourceIdentifierList,
                                                       dataIdentificationId, dataIdentificationUuId, connectionType );
        if ( !idList.isEmpty() && fileIdentifierString.length == 0 ) {
            for ( String id : idList ) {
                OMElement firstElement = rootEl.getFirstElement();
                firstElement.insertSiblingBefore( new GenerateOMElement().createFileIdentifierElement( id ) );
            }
        }
        return record;
    }
}