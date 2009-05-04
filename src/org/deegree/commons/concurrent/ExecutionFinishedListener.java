// $HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/branches/2.2_testing/src/org/deegree/framework/concurrent/ExecutionFinishedListener.java $
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
 53177 Bonn
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
package org.deegree.commons.concurrent;

/**
 * Listener interface for sending a notification that the asynchronous execution of a
 * task has finished (successfully or abnormally).
 * 
 * @param <T> type of return value
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: apoth $
 *
 * @version $Revision: 9339 $, $Date: 2007-12-27 12:31:52 +0000 (Do, 27 Dez 2007) $
 */
public interface ExecutionFinishedListener<T> {

    /**
     * Called after an asynchronous task has finished. 
     * 
     * @param finishedEvent
     *            event representing the state of the finished task
     */
    void executionFinished( ExecutionFinishedEvent<T> finishedEvent );
}
