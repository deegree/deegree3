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

import java.sql.Connection;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.iso.generating.generatingelements.GenerateOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class HierarchieLevelInspector implements RecordInspector {

    private static Logger LOG = LoggerFactory.getLogger( HierarchieLevelInspector.class );

    private final XMLAdapter a;

    public HierarchieLevelInspector() {
        this.a = new XMLAdapter();
    }

    @Override
    public OMElement inspect( OMElement record, Connection conn )
                            throws MetadataInspectorException {

        a.setRootElement( record );

        NamespaceBindings nsContext = a.getNamespaceContext( record );
        // NamespaceContext newNSC = generateNSC(nsContext);
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContext.addNamespace( "gmd", "http://www.isotc211.org/2005/gmd" );
        nsContext.addNamespace( "gco", "http://www.isotc211.org/2005/gco" );

        /**
         * if provided data is a dataset: type = dataset (default)
         * <p>
         * if provided data is a datasetCollection: type = series
         * <p>
         * if provided data is an application: type = application
         * <p>
         * if provided data is a service: type = service
         */
        String type = a.getNodeAsString( record, new XPath( "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
                                                            nsContext ), null );

        if ( type == null ) {

            OMElement hln = a.getElement( record, new XPath( "./gmd:hierarchyLevelName", nsContext ) );
            OMElement contact = record.getFirstChildWithName( new QName( "http://www.isotc211.org/2005/gmd", "contact" ) );
            if ( hln != null ) {
                hln.insertSiblingBefore( GenerateOMElement.newInstance().createHierarchieLevelElement() );
            } else {
                if ( contact != null ) {
                    contact.insertSiblingBefore( GenerateOMElement.newInstance().createHierarchieLevelElement() );
                } else {
                    String msg = Messages.getMessage( "ERROR_MANDATORY_ELEMENT_MISSING", "contact" );
                    LOG.debug( msg );
                    throw new MetadataInspectorException( msg );
                }
            }

        }

        return record;
    }

}
