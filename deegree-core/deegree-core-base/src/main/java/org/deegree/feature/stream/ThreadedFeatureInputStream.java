//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.feature.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureInputStream} that uses a separate thread to keep an internal queue of features filled.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ThreadedFeatureInputStream implements FeatureInputStream {

    private static Logger LOG = LoggerFactory.getLogger( ThreadedFeatureInputStream.class );

    // TODO where to manage this?
    private static ExecutorService service = Executors.newFixedThreadPool( 10 );

    private final QueueFiller producer;

    /**
     * Creates a new {@link ThreadedFeatureInputStream} based on the given {@link FeatureInputStream} that uses the
     * given thread to keep the internal queue of results filled.
     * 
     * @param rs
     * @param maxFill
     * @param minFill
     */
    public ThreadedFeatureInputStream( FeatureInputStream rs, int maxFill, int minFill ) {
        producer = new QueueFiller( rs, maxFill, minFill );
        service.execute( producer );
    }

    @Override
    public void close() {
        producer.exit();
    }

    @Override
    public FeatureCollection toCollection() {
        return Features.toCollection( this );
    }

    @Override
    public Iterator<Feature> iterator() {
        return new Iterator<Feature>() {

            @Override
            public boolean hasNext() {
                return producer.hasNext();
            }

            @Override
            public Feature next() {
                return producer.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int count() {
        int i = 0;
        for ( @SuppressWarnings("unused")
        Feature f : this ) {
            i++;
        }
        close();
        return i;
    }

    private class QueueFiller implements Runnable {

        private final FeatureInputStream rs;

        private final Queue<Feature> featureQueue;

        private int minFill;

        private boolean exitRequested;

        private boolean sleeping;

        private boolean finished;

        private QueueFiller( FeatureInputStream rs, int maxFill, int minFill ) {
            this.rs = rs;
            this.featureQueue = new ArrayBlockingQueue<Feature>( maxFill, true );
            this.minFill = minFill;
        }

        @Override
        public void run() {
            LOG.debug( "Producer thread starting" );
            try {
                Iterator<Feature> iter = rs.iterator();
                try {
                    Feature f = null;
                    while ( iter.hasNext() && !exitRequested ) {
                        if ( f == null ) {
                            f = iter.next();
                        }
                        synchronized ( this ) {
                        if ( !featureQueue.offer( f ) ) {
                            // wait until we get notified that queue needs to be filled up again

                            // LOG.debug( "Producer thread going to sleep: fill=" + featureQueue.size() );
                            sleeping = true;
                            wait();
                            sleeping = false;
                            // LOG.debug( "Producer thread waking up: fill=" + featureQueue.size() );
                        } else {
                            f = null;
                            // Wake reading thread
                            notify();
                        }
                    }
                    }
                } catch ( InterruptedException e ) {
                    LOG.debug( "Got interrupted." );
                }
            } finally {
                finished = true;
                rs.close();
                LOG.debug( "Producer thread exiting" );
            }
        }

        private boolean hasNext() {
            int fill = featureQueue.size();
            if ( sleeping && fill < minFill ) {
                // LOG.debug( "Queue below min fill. Waking producer thread." );
                synchronized ( this ) {
                    notify();
                }
            }
            if ( fill > 0 ) {
                return true;
            }
            synchronized (this) {
                while ( true ) {
                    // LOG.debug( "Queue empty. Checking if more features are coming from producer." );
                    if ( finished && featureQueue.isEmpty() ) {
                        return false;
                    }
                    if ( !featureQueue.isEmpty() ) {
                        return true;
                    }
                    try {
                        wait(1000);
                    } catch(InterruptedException ex) {
                        // Ignore
                    }
                }
            }
        }

        private Feature next() {
            if ( !hasNext() ) {
                throw new NoSuchElementException();
            }
            return featureQueue.poll();
        }

        private void exit() {
            exitRequested = true;
            if ( sleeping ) {
                synchronized ( this ) {
                    notify();
                }
            }
        }
    }

    /**
     * 
     */
    public static void shutdown() {
        service.shutdown();
    }

}
