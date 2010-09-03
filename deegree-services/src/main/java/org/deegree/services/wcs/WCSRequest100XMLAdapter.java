//$HeadURL$
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
package org.deegree.services.wcs;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_NS;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wcs.WCSConstants;
import org.deegree.services.controller.ows.OWSException;

/**
 * Base class for all WCS 1.0.0 XMLAdapter. Defines the WCS XML namespace.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WCSRequest100XMLAdapter extends XMLAdapter {

    /**
     * Prefix which is bound to the wcs_100 namespace
     */
    protected static final String WCS_PREFIX = "wcs";

    /**
     * namespace context with wcs ns
     */
    protected final static NamespaceContext wcsNSContext;

    static {
        wcsNSContext = new NamespaceContext( XMLAdapter.nsContext );
        wcsNSContext.addNamespace( WCS_PREFIX, WCS_100_NS );
        wcsNSContext.addNamespace( GML_PREFIX, GMLNS );
    }

    /**
     * Check that the value is not <code>null</code>, throws OWSException otherwise.
     * 
     * @param nodeName
     *            the name of the required node, used in the exception
     * @param requiredValue
     *            the value to check
     * @throws OWSException
     *             if requiredValue is <code>null</code>
     */
    protected void checkRequiredString( String nodeName, String requiredValue )
                            throws OWSException {
        if ( requiredValue == null ) {
            throw new OWSException( "Missing " + nodeName + " in request", OWSException.MISSING_PARAMETER_VALUE,
                                    nodeName );
        }
    }

    /**
     * Check that the requiredElem is not <code>null</code>, throws OWSException otherwise.
     * 
     * @param nodeName
     *            the name of the required node, used in the exception
     * @param requiredElem
     *            the element to check
     * @throws OWSException
     *             if requiredElement is <code>null</code>
     */
    protected void checkRequiredElement( String nodeName, OMElement requiredElem )
                            throws OWSException {
        if ( requiredElem == null ) {
            throw new OWSException( "Missing " + nodeName + " in request", OWSException.MISSING_PARAMETER_VALUE,
                                    nodeName );
        }
    }

    /**
     * Checks that the given element contains a version attribute.
     * 
     * @param root
     * @return the {@link WCSConstants#VERSION_100} if it was valid.
     * @throws OWSException
     */
    protected Version checkVersion( OMElement root )
                            throws OWSException {
        String version = getNodeAsString( root, new XPath( "@version", wcsNSContext ), null );
        if ( version == null ) {
            throw new OWSException( "Missing version attribute", OWSException.MISSING_PARAMETER_VALUE, "version" );
        }
        if ( !"1.0.0".equals( version ) ) {
            throw new OWSException( "Invalid version attribute", OWSException.INVALID_PARAMETER_VALUE, "version" );
        }
        return WCSConstants.VERSION_100;
    }
}
