//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/framework/concurrent/ExecutorTest.java $
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
package org.deegree.framework.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version August 2nd 2006
 */
public class ExecutorTest extends TestCase implements ExecutionFinishedListener<Object>, Callable<Object> {

    private static ILogger LOG = LoggerFactory.getLogger( ExecutorTest.class );

    /**
     * @throws InterruptedException
     */
    public void testSynchronousCallsWithTimeout()
                            throws InterruptedException {
        int numberOfThreads = 50;
        long threadTimeout = 1000;
        LOG.logInfo( "Testing synchrous call of " + numberOfThreads + " threads with a timeout of " + threadTimeout
                     + " miliseconds" );

        ArrayList<Callable<Object>> mylist = new ArrayList<Callable<Object>>();
        for ( int i = 0; i < numberOfThreads; ++i ) {
            mylist.add( this );
        }

        List<ExecutionFinishedEvent<Object>> results = Executor.getInstance().performSynchronously( mylist,
                                                                                                    threadTimeout );

        for ( ExecutionFinishedEvent<Object> obj : results ) {
            try {
                LOG.logInfo( obj.getResult().toString() );
            } catch ( CancellationException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
            } catch ( Throwable e ) {
                LOG.logError( e.getLocalizedMessage(), e );
            }
        }
    }

    /**
     * @throws InterruptedException
     */
    public void testSynchronousCallsWithoutTimeout()
                            throws InterruptedException {
        int numberOfThreads = 100;
        LOG.logInfo( "Testing synchrous call of " + numberOfThreads + " threads." );

        ArrayList<Callable<Object>> mylist = new ArrayList<Callable<Object>>();
        for ( int i = 0; i < numberOfThreads; ++i ) {
            mylist.add( this );
        }

        List<ExecutionFinishedEvent<Object>> results = Executor.getInstance().performSynchronously( mylist );

        for ( ExecutionFinishedEvent<Object> obj : results ) {
            try {
                LOG.logInfo( obj.getResult().toString() );
            } catch ( CancellationException e ) {
                LOG.logError( "This is not an error, only a timeout: " + e.getLocalizedMessage(), e );
            } catch ( Throwable e ) {
                LOG.logError( e.getLocalizedMessage(), e );
            }
        }

    }

    /**
     * @throws Throwable
     */
    public void testSynchronousCallWithTimeout()
                            throws Throwable {
        long threadTimeout = 1000;
        LOG.logInfo( "Testing synchrous call with one Thread and a timeout of " + threadTimeout + " milliseconds." );

        Executor.getInstance().performSynchronously( this, threadTimeout );
    }

    /**
     * @throws Exception
     */
    public void testAsynchronousWithListeners()
                            throws Exception {
        LOG.logInfo( "Testing asynchrous call with one Thread and failed and finished listener." );
        // Callable, ExecutionFinishedListener, ExecutionFailedListener
        Executor.getInstance().performAsynchronously( this, this );
        // The Junit Testsuit does a System.exit() therefor we must wait for the
        // Tasks to be executes.
        Thread.sleep( 2000 );
    }

    /**
     * Is called if the execution of the tasks (in form of a Callable Object) finished.
     *
     * @param finishedEvent
     */
    public void executionFinished( ExecutionFinishedEvent<Object> finishedEvent ) {
        Object resultValue;
        try {
            resultValue = finishedEvent.getResult();
            assertNotNull( resultValue );
        } catch ( Throwable t ) {
            LOG.logError( "in Listener executionFailed: " + t, t );
        }
    }

    /**
     * Test "Task" to be called
     *
     * @return a String
     */
    public Object call()
                            throws Exception {
        LOG.logDebug( "Callthread->" + Thread.currentThread() );
        Thread.sleep( 950 );

        return "The answer to a call" + Thread.currentThread();
    }
}
