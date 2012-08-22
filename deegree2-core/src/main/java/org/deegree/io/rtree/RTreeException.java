//$HeadURL$
//----------------------------------------
//RTree implementation.
//Copyright (C) 2002-2004 Wolfgang Baer - WBaer@gmx.de
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//----------------------------------------

package org.deegree.io.rtree;

/**
 * <p>
 * The common exception thrown by problems during the rtree methods. Thats the exception a user of
 * the package gets if errors occur *
 * </p>
 *
 * @author Wolfgang Baer - WBaer@gmx.de
 */
public class RTreeException extends Exception {

    /**
     * Constructor for RTreeException.
     */
    public RTreeException() {
        super();
    }

    /**
     * Constructor for RTreeException.
     *
     * @param message
     */
    public RTreeException( String message ) {
        super( message );
    }
}