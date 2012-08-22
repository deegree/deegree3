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
package org.deegree.security.owsproxy;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 *
 * @since 1.1
 *
 */
public class Condition {

    private Map<String, OperationParameter> opMap = null;

    private boolean any = false;

    /**
     *
     * @param operationParameters
     */
    public Condition( OperationParameter[] operationParameters ) {
        opMap = new HashMap<String, OperationParameter>();
        setOperationParameters( operationParameters );
    }

    /**
     *
     * @param any
     */
    public Condition( boolean any ) {
        opMap = new HashMap<String, OperationParameter>();
        this.any = any;
    }

    /**
     *
     * @return all operation parameters
     */
    public OperationParameter[] getOperationParameters() {
        OperationParameter[] op = new OperationParameter[opMap.size()];
        return opMap.values().toArray( op );
    }

    /**
     * @param name
     * @return named operation parameter
     */
    public OperationParameter getOperationParameter( String name ) {
        return opMap.get( name );
    }

    /**
     * @param param
     *
     */
    public void setOperationParameters( OperationParameter[] param ) {
        opMap.clear();
        for ( int i = 0; i < param.length; i++ ) {
            opMap.put( param[i].getName(), param[i] );
        }
    }

    /**
     * @param param
     *
     */
    public void addOperationParameter( OperationParameter param ) {
        opMap.put( param.getName(), param );
    }

    /**
     * @param param
     *
     */
    public void removeOperationParameter( OperationParameter param ) {
        removeOperationParameter( param.getName() );
    }

    /**
     * @param name
     *
     */
    public void removeOperationParameter( String name ) {
        opMap.remove( name );
    }

    /**
     *
     * @return true if no validation for specific parameters shall be performed
     */
    public boolean isAny() {
        return any;
    }

}
