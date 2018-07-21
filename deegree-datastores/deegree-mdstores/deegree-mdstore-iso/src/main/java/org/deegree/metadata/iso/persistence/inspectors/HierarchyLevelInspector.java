//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/inspectors/HierarchyLevelInspector.java $
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
package org.deegree.metadata.iso.persistence.inspectors;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.logging.log4j.Logger;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.sqldialect.SQLDialect;

import javax.xml.namespace.QName;
import java.sql.Connection;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 30651 $, $Date: 2011-05-05 11:40:31 +0200 (Do, 05. Mai 2011) $
 */
public class HierarchyLevelInspector implements RecordInspector<ISORecord> {

    private static Logger LOG = getLogger( HierarchyLevelInspector.class );

    private final NamespaceBindings nsContext = new NamespaceBindings();

    public HierarchyLevelInspector() {
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContext.addNamespace( "gmd", "http://www.isotc211.org/2005/gmd" );
        nsContext.addNamespace( "gco", "http://www.isotc211.org/2005/gco" );
    }

    @Override
    public ISORecord inspect( ISORecord record, Connection conn, SQLDialect dialect )
                            throws MetadataInspectorException {

        XMLAdapter a = new XMLAdapter( record.getAsOMElement() );

        /**
         * if provided data is a dataset: type = dataset (default)
         * <p>
         * if provided data is a datasetCollection: type = series
         * <p>
         * if provided data is an application: type = application
         * <p>
         * if provided data is a service: type = service
         */
        OMElement rootEl = record.getAsOMElement();
        String type = a.getNodeAsString( rootEl, new XPath( "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
                                                            nsContext ), null );

        if ( type == null ) {
            OMElement hln = a.getElement( rootEl, new XPath( "./gmd:hierarchyLevelName", nsContext ) );
            OMElement contact = rootEl.getFirstChildWithName( new QName( "http://www.isotc211.org/2005/gmd", "contact" ) );
            if ( hln != null ) {
                hln.insertSiblingBefore( createHierarchyLevelElement() );
            } else {
                if ( contact != null ) {
                    contact.insertSiblingBefore( createHierarchyLevelElement() );
                } else {
                    String msg = Messages.getMessage( "ERROR_MANDATORY_ELEMENT_MISSING", "contact" );
                    LOG.debug( msg );
                    throw new MetadataInspectorException( msg );
                }
            }
        }
        return record;
    }

    private OMElement createHierarchyLevelElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "gmd" );
        OMElement omHierarchieLevel = factory.createOMElement( "hierarchyLevel", namespaceGMD );
        OMElement omScopeCode = factory.createOMElement( "MD_ScopeCode", namespaceGMD );
        omHierarchieLevel.addChild( omScopeCode );
        omScopeCode.addAttribute( "codeList",
                                  "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode",
                                  null );
        omScopeCode.addAttribute( "codeListValue", "dataset", null );
        omScopeCode.setText( "dataset" );
        return omHierarchieLevel;
    }
}