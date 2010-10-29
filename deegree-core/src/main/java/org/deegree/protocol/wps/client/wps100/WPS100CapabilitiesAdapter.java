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
package org.deegree.protocol.wps.client.wps100;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XMLNS;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.OWS110CapabilitiesAdapter;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.process.ProcessInfo;

/**
 * Provides access to the relevant information from a WPS 1.0.0 capabilities document.
 * 
 * @see WPSClient
 * @see ProcessInfo
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPS100CapabilitiesAdapter extends OWS110CapabilitiesAdapter {

    private final NamespaceContext nsContext = new NamespaceContext();

    /**
     * Creates a new {@link WPS100CapabilitiesAdapter} instance.
     */
    public WPS100CapabilitiesAdapter() {
        nsContext.addNamespace( "wps", WPS_100_NS );
        nsContext.addNamespace( "ows", OWS_11_NS );
        nsContext.addNamespace( "xlink", XLNNS );
    }

    /**
     * Returns information on all processes announced in the document.
     * 
     * @return information on all processes, may be empty, but never <code>null</code>
     */
    public List<ProcessInfo> getProcesses() {
        List<ProcessInfo> processes = new ArrayList<ProcessInfo>();
        XPath xpath = new XPath( "wps:ProcessOfferings/wps:Process", nsContext );
        List<OMElement> omProcesses = getElements( getRootElement(), xpath );
        for ( OMElement omProcess : omProcesses ) {
            String version = omProcess.getAttributeValue( new QName( WPS_100_NS, "processVersion" ) );
            CodeType id = parseId( omProcess );
            LanguageString processTitle = parseLanguageString( omProcess, "Title" );
            LanguageString processAbstract = parseLanguageString( omProcess, "Abstract" );
            processes.add( new ProcessInfo( id, processTitle, processAbstract, version ) );
        }
        return processes;
    }

    private LanguageString parseLanguageString( OMElement omElement, String name ) {
        OMElement omElem = omElement.getFirstChildWithName( new QName( OWS_11_NS, name ) );
        if ( omElem != null ) {
            String lang = omElem.getAttributeValue( new QName( XMLNS, "lang" ) );
            return new LanguageString( omElem.getText(), lang );
        }
        return null;
    }

    private CodeType parseId( OMElement omProcess ) {
        OMElement omId = omProcess.getFirstChildWithName( new QName( OWS_11_NS, "Identifier" ) );
        String codeSpace = omId.getAttributeValue( new QName( null, "codeSpace" ) );
        if ( codeSpace != null ) {
            return new CodeType( omId.getText(), codeSpace );
        }
        return new CodeType( omId.getText() );
    }
}
