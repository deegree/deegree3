//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/model/filterencoding/FilterEvaluationException.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 
 ---------------------------------------------------------------------------*/
package org.deegree.model.filter;

/**
 * This exception is thrown when the evaluation of a {@link Filter} expression (or subexpression) fails.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision: 9343 $ $Date: 2007-12-27 13:30:32 +0000 (Do, 27 Dez 2007) $
 */
public class FilterEvaluationException extends Exception {

    private static final long serialVersionUID = 2009587694159886757L;

    /**
     * Constructs a new <code>FilterEvaluationException</code> with the given detail message.
     * 
     * @param msg
     *            the detail message
     */
    public FilterEvaluationException( String msg ) {
        super( msg );
    }
}
