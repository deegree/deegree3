// $HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/branches/2.2_testing/src/org/deegree/framework/concurrent/ExecutionFinishedEvent.java $
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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

/**
 * Event that is sent when an asynchronous task finished.
 * <p>
 * This can mean:
 * <ul>
 * <li>it finished successfully</li>
 * <li>it terminated abnormally (with an exception or error)</li>
 * <li>a time out occurred during the performing of the task (or it's thread has been
 * cancelled)</li>
 * </ul>
 * </p>
 * <p>
 * If the task did not finish successfully, the thrown exception / error is rethrown when
 * {@link #getResult()} is called.
 * </p>
 * 
 * @param <T> type of return value
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: apoth $
 *
 * @version $Revision: 9339 $, $Date: 2007-12-27 12:31:52 +0000 (Do, 27 Dez 2007) $
 */
public class ExecutionFinishedEvent<T> { 
    
    private Callable<T> task;

    private T result;

    private Throwable t;   
    
    /**
     * Constructs an <code>ExecutionFinishedEvent</code> for a task that finished
     * successfully.
     * 
     * @param task
     * @param result
     */
    ExecutionFinishedEvent( Callable<T> task, T result  ) {
        this.task = task;
        this.result = result;
    }

    /**
     * Constructs an <code>ExecutionFinishedEvent</code> for a task that terminated
     * abnormally.
     * 
     * @param t Throwable that the terminated task threw
     * @param task
     */
    ExecutionFinishedEvent( Throwable t, Callable<T> task ) {
        this.task = task;
        this.t = t;
    }    

    /**
     * Returns the corresponding task instance.
     * 
     * @return the corresponding task instance
     */
    public Callable<T> getTask() {
        return this.task;
    }

    /**
     * Returns the result value that the finished task returned.
     * <p>
     * If the task produced an exception or error, it is rethrown here. If the task has been
     * cancelled (usually this means that the time out occurred), a {@link CancellationException}
     * is thrown. 
     * 
     * @return the result value that the task returned
     * @throws CancellationException
     *            if task timed out / has been cancelled
     * @throws Throwable
     *            if task terminated with an exception or error
     */    
    public T getResult() throws CancellationException, Throwable {
        if (this.t != null) {
            throw t;
        }
        return this.result;
    }    
}
