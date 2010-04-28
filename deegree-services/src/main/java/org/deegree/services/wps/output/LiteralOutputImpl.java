//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.output;

import java.util.LinkedList;
import java.util.List;

import org.deegree.services.jaxb.wps.LiteralOutputDefinition;
import org.deegree.services.jaxb.wps.LiteralOutputDefinition.OtherUOM;

/**
 * Implementation of {@link LiteralOutput}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class LiteralOutputImpl extends ProcessletOutputImpl implements LiteralOutput {

    private String value;

    private final String dataType;

    private final String requestedUOM;

    public LiteralOutputImpl( LiteralOutputDefinition outputType, String requestedUOM, boolean isRequested ) {
        super( outputType, isRequested );
        this.requestedUOM = requestedUOM;
        this.dataType = outputType.getDataType() != null ? outputType.getDataType().getValue() : null;
    }

    @Override
    public void setValue( String value ) {
        this.value = value;
    }

    @Override
    public String getRequestedUOM() {
        return requestedUOM;
    }

    /**
     * Returns the announced literal data type from the process definition (e.g. integer, real, etc) as an URI, such as
     * <code>http://www.w3.org/TR/xmlschema-2/#integer</code>.
     * 
     * @return the data type, or null if not specified in the process definition
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * Returns the supported UOMs (unit-of-measures) for the literal output parameter as defined in the process
     * definition (e.g. 'meters','feet', etc).
     * 
     * @return the supported UOMs (array is never null, but may be empty)
     */
    public String[] getSupportedUOMs() {
        List<String> supportedUOMs = new LinkedList<String>();
        LiteralOutputDefinition definition = (LiteralOutputDefinition) this.definition;
        if ( definition.getDefaultUOM() != null ) {
            supportedUOMs.add( definition.getDefaultUOM().getValue() );
        }
        for ( OtherUOM otherUom : definition.getOtherUOM() ) {
            supportedUOMs.add( otherUom.getValue() );
        }

        return supportedUOMs.toArray( new String[supportedUOMs.size()] );
    }

    public String getValue() {
        return this.value;
    }
}
