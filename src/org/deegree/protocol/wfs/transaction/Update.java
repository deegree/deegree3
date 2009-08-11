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

package org.deegree.protocol.wfs.transaction;

import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.PropertyName;

/**
 * Represents a WFS <code>Update</code> operation (part of a {@link Transaction} request).
 * 
 * @see Transaction
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Update extends TransactionOperation {

    private String inputFormat;

    private String srsName;
    
    private QName typeName;

    private Feature replacementFeature;

    private Map<PropertyName, Object> replacementProps;

    private Filter filter;    

    public Update( String handle ) {
        super( handle );
    }

    /**
     * Always returns {@link TransactionOperation.Type#UPDATE}.
     * 
     * @return {@link TransactionOperation.Type#UPDATE}
     */
    @Override
    public Type getType() {
        return Type.UPDATE;
    }     
    
    /**
     * Returns the format of encoded property values.
     * 
     * @return the format of encoded property values, may be null (unspecified)
     */
    public String getInputFormat() {
        return inputFormat;
    }

    /**
     * Returns the specified coordinate system for the geometries to be inserted.
     * 
     * @return the specified coordinate system, can be null (unspecified)
     */
    public String getSRSName() {
        return srsName;
    }    
}
