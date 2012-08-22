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

package org.deegree.portal.owswatch;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.MailMessage;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLTools;
import org.deegree.portal.owswatch.configs.OwsWatchConfig;
import org.deegree.portal.owswatch.configs.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Mail class of this framework. Its called upon starting tomcat to start watching services and test them regularly
 * according to their test intervals and logs the result through its service invoker
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceWatcher extends Thread implements Serializable {

    private static final long serialVersionUID = -5247964268533099679L;

    private static final ILogger LOG = LoggerFactory.getLogger( ServiceWatcher.class );

    private volatile int minTestInterval = 0;

    private volatile boolean threadsuspended = true;

    private volatile int leastWaitTime = 0;

    // Used to save the time, when the leastWaitTime was taken.
    private volatile Calendar calLeastTime = null;

    /**
     * This hash table holds all ServiceMonitors in the form <serviceHashcode,ServiceMonitor>
     */
    static private TreeMap<Integer, ServiceConfiguration> services = new TreeMap<Integer, ServiceConfiguration>();

    /**
     * Integer Service id ServiceLog Logger class
     */
    private Map<ServiceConfiguration, ServiceLog> serviceLogs = new HashMap<ServiceConfiguration, ServiceLog>();

    /**
     * This hash table holds the time of the next run for the given Services in the form <ServiceHashcode,Time remaining
     * for the next run>
     */
    private Hashtable<Integer, Integer> activeServices = new Hashtable<Integer, Integer>( 30 );

    /**
     * This function will be called at the begining of the run() method
     */
    private void init() {

        Iterator<ServiceConfiguration> it = services.values().iterator();
        if ( it.hasNext() ) {
            threadsuspended = false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        init();
        // This will cause the function to start at hours:minutes:00 seconds
        // Where minutes, should be :10, :15, :30, :60
        try {
            Calendar cal = Calendar.getInstance();
            cal.add( Calendar.MINUTE, 1 );
            cal.set( Calendar.SECOND, 60 );
            sleep( ( Calendar.getInstance().getTimeInMillis() - calLeastTime.getTimeInMillis() ) );
        } catch ( InterruptedException e ) {
            // Nothing. Its possible to be interrupted
        }
        Calendar nextCall = Calendar.getInstance();
        while ( true ) {
            try {
                synchronized ( this ) {
                    while ( threadsuspended ) {
                        wait( 1000 );
                    }
                    // In case the wait is interrupted, we guarantee that the test will not be
                    // executed until its 00 seconds
                    if ( Calendar.getInstance().get( Calendar.SECOND ) != 0 ) {
                        int seconds = Calendar.getInstance().get( Calendar.SECOND );
                        sleep( ( 60 - seconds ) * 1000 );
                    }
                    refreshAllServices();
                    // if true then we have to shift the time system less than one minute forward
                    // to make the seconds 00 again
                    // minTestInterval = findMinTestInterval();
                    nextCall.add( Calendar.MINUTE, 1 );
                    long sleepTime = nextCall.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                    wait( sleepTime );
                }
            } catch ( InterruptedException e ) {
                // Nothing. Its possible to be interrupted
            }
        }
    }

    /**
     * adds a new Service to the ServiceList
     *
     * @param service
     * @param servicelog
     */
    public synchronized void addService( ServiceConfiguration service, ServiceLog servicelog ) {

        if ( !services.containsKey( Integer.valueOf( service.getServiceid() ) ) ) {
            serviceLogs.put( service, servicelog );
        } else {
            ServiceLog log = serviceLogs.remove( services.get( Integer.valueOf( service.getServiceid() ) ) );
            serviceLogs.put( service, log );
        }
        services.put( Integer.valueOf( service.getServiceid() ), service );

        if ( service.isActive() ) {
            addActiveService( service );
        }
    }

    /**
     * Adds an active service to the list of active services. It also sets when the first test should take place. This
     * is useful to start tests at :00 :15 :30 minutes etc..
     *
     * @param service
     */
    protected synchronized void addActiveService( ServiceConfiguration service ) {

        int serviceId = service.getServiceid();
        Calendar cal = Calendar.getInstance();
        // Use the minimum refreshRate as a refreshRate for the watcher thread
        if ( minTestInterval == 0 || service.getRefreshRate() < minTestInterval ) {
            minTestInterval = service.getRefreshRate();
        }
        int waitTime = 0;
        // The checks are made in descending order, so that the bigger number takes precedence
        if ( service.getRefreshRate() % 60 == 0 ) {
            // if the test takes place every hour or multiple of hour
            waitTime = 60 - cal.get( Calendar.MINUTE );
        } else if ( service.getRefreshRate() % 30 == 0 ) {
            // if the test takes place every half an hour
            waitTime = 30 - ( cal.get( Calendar.MINUTE ) % 30 );
        } else if ( service.getRefreshRate() % 15 == 0 ) {
            // if the test takes place every 15 minutes
            waitTime = 15 - ( cal.get( Calendar.MINUTE ) % 15 );
        } else if ( service.getRefreshRate() % 10 == 0 ) {
            // if the test takes place every 10 mins
            waitTime = 10 - ( cal.get( Calendar.MINUTE ) % 10 );
        } else {
            // This one is actually thought for tests every one minute,
            // but it will of course cover any other number
            // Although I strongly recommend to have the tests in or a multiple of the mentioned time above
            // waitTime = 0; I'll leave this block just for clarification
        }
        activeServices.put( Integer.valueOf( serviceId ), Integer.valueOf( waitTime ) );
        LOG.logDebug( "service: ", service.getServiceName(), " will start in: ",
                      activeServices.get( Integer.valueOf( serviceId ) ), " minutes" );

        if ( leastWaitTime == 0 || leastWaitTime > waitTime ) {
            leastWaitTime = waitTime;
            calLeastTime = Calendar.getInstance();
        }

    }

    /**
     * returns the service identified by its Id in the serviceList
     *
     * @param serviceId
     * @return ServiceConfiguration
     */
    public ServiceConfiguration getService( int serviceId ) {
        return services.get( Integer.valueOf( serviceId ) );
    }

    /**
     * removes the service identify by -id- from the ServiceList
     *
     * @param serviceId
     * @return new ServiceConfiguration
     *
     */
    public ServiceConfiguration removeService( int serviceId ) {
        if ( activeServices.containsKey( Integer.valueOf( serviceId ) ) ) {
            ServiceConfiguration service = services.get( Integer.valueOf( serviceId ) );
            activeServices.remove( Integer.valueOf( serviceId ) );
            serviceLogs.remove( service );
            if ( service.getRefreshRate() == minTestInterval ) {
                minTestInterval = findMinTestInterval();
            }
            if ( minTestInterval == 0 ) {
                threadsuspended = true;
            }
        }

        return services.remove( serviceId );
    }

    /**
     * Finds the minimum test interval from the active Services
     */
    private int findMinTestInterval() {
        int minInterval = 0;
        Iterator<Integer> it = activeServices.keySet().iterator();
        while ( it.hasNext() ) {
            int serviceRefreshRate = services.get( it.next() ).getRefreshRate();
            if ( minInterval == 0 || serviceRefreshRate < minInterval ) {
                minInterval = serviceRefreshRate;
            }
        }
        return minInterval;
    }

    /**
     * This method will set the threadSuspended vaiable to true in order for the id-th service to stop sending
     * GetCapabilities requests
     *
     * @param serviceId
     *
     */
    public void stopServiceConfiguration( int serviceId ) {
        activeServices.remove( serviceId );
    }

    /**
     * This method will set the threadSuspended vaiable to false in order for the id-th service to restart sending
     * requests
     *
     * @param serviceId
     *
     */
    public void startServiceConfiguration( int serviceId ) {
        activeServices.put( serviceId, Integer.valueOf( 0 ) );
    }

    /**
     * @param serviceId
     * @return true if the currrent Service id at all exists in watcher, false otherwise
     */
    public boolean containsService( int serviceId ) {
        return services.containsKey( serviceId );
    }

    /**
     * returns list of services being watched
     *
     * @return the serviceList
     */
    public List<ServiceConfiguration> getServiceList() {
        return new ArrayList<ServiceConfiguration>( services.values() );
    }

    /**
     * RefreshAllServices executes all the requests in the active monitors, and subtract the time from those who are
     * waiting in line and restarts the time for those who had a turn This is the default to be called during the
     * regular tests. If you want to execute tests in the main thread and without affecting the regular tests
     * executeTest(int) and executeall()
     */
    public void refreshAllServices() {

        Set<Integer> keys = activeServices.keySet();
        Iterator<Integer> it = keys.iterator();
        while ( it.hasNext() ) {
            Integer key = it.next();
            int value = activeServices.get( key );
            // Check will be repeated every minute
            value -= 1;
            ServiceConfiguration service = services.get( key );
            // if the waiting time for this service has elapsed, and now its time to execute a new request
            if ( value <= 0 ) {
                LOG.logDebug( "service: ", service.getServiceName(), " will be refreshed" );
                // resets the timing
                // -value adjusts the timing exactly to the subtracted sum. For example if a service refreshs
                // every 3 minutes and value is -1, that means this service has to wait 2 minutes this time not 3
                // activeServices.put( key, (Integer) service.getRefreshRate() - value );
                activeServices.put( key, (Integer) service.getRefreshRate() );
                ServiceLog serviceLog = serviceLogs.get( services.get( key ) );
                new ServiceInvoker( service, serviceLog ).executeTestThreaded();
            } else {
                // else subtract the refresh rate
                activeServices.put( key, (Integer) value );
            }
        }
    }

    /**
     * executes the given service one time in the main thread
     *
     * @param serviceId
     */
    public void execute( int serviceId ) {

        ServiceConfiguration service = services.get( serviceId );
        if ( service == null ) {
            return;
        }
        ServiceLog serviceLog = this.serviceLogs.get( service );
        new ServiceInvoker( service, serviceLog ).executeTest();
    }

    /**
     * This function is called on starting tomcat. It checks when was the last test performed before tomcat was
     * shutdown. and sends an email to the user, notifying him with the situation and that a number of tests have been
     * missed
     *
     * @param webinfPath
     *            WEB-INF folder path
     * @param conf
     *            owsWatch configuration instance
     */
    public void compileDownTimeReport( String webinfPath, OwsWatchConfig conf ) {

        LOG.logDebug( "compileDownTimeReport()" );
        String protFolderPath = StringTools.concat( 100, webinfPath, conf.getGeneral().getProtFolderPath() );
        File protFolder = new File( protFolderPath );
        if ( !protFolder.exists() || !protFolder.isDirectory() ) {
            LOG.logError( Messages.getMessage( "ERROR_PROT_DIR_NOT_FOLDER", protFolderPath ) );
            return;
        }
        Iterator<Integer> it = activeServices.keySet().iterator();
        Hashtable<Integer, String> missedTests = new Hashtable<Integer, String>();

        while ( it.hasNext() ) {
            Integer next = it.next();
            LOG.logDebug( "Active test: ", next.intValue() );
            File prot = getLatestProtocolPath( protFolderPath, next.intValue(), 2 );
            if ( prot == null ) {
                LOG.logDebug( "protocol is null" );
                continue;
            }
            String protLatest = getLatestTest( prot );
            if ( protLatest == null ) {
                LOG.logDebug( "protLastTest is null" );
                continue;
            }
            missedTests.put( next, protLatest );
        }
        LOG.logDebug( "number of missed tests: ", missedTests.keySet().size() );
        writeAndSendTimeDownEmail( missedTests, conf );

    }

    /**
     * Returns the latest written protocol file of a certain serviceconfiguration. The search starts with the actual
     * date and tries to find the protocl of that date if not found it subtracts a month and repeat the operation. This
     * procedure is repeated monthsDepth
     *
     * @param protFolderPath
     *            Path to the folder containing the protocol files
     * @param serviceID
     *            ID to the serviceConfiguration
     * @param monthsDepth
     *            determines how many times this operation should be repeated, one month earlier every time
     *
     * @return Path to the latest protocol file or null if non is found
     */
    private File getLatestProtocolPath( String protFolderPath, int serviceID, int monthsDepth ) {

        Calendar now = Calendar.getInstance();
        int month = now.get( Calendar.MONTH ) + 1;
        int year = now.get( Calendar.YEAR );

        while ( monthsDepth-- > 0 ) {

            String path = StringTools.concat( 200, protFolderPath, "/", "protocol_ID_", serviceID, "_", year, "_",
                                              month, ".xml" );
            LOG.logDebug( "path to protocol: ", path );
            File prot = new File( path );
            if ( prot.exists() ) {
                return prot;
            }
            if ( --month == 0 ) {
                month = 12;
                year--;
            }
        }
        return null;
    }

    /**
     * Parses the given xml file and finds the latest test executed on a ServiceConfiguration
     *
     * @param protFile
     *            protocol path of a given ServiceConfiguration
     * @return Date of the latest test executed. Note. Year and month
     */
    private String getLatestTest( File protFile ) {

        try {
            Document doc = XMLTools.parse( new FileReader( protFile ) );
            Element elem = doc.getDocumentElement();
            List<String> nodes = XMLTools.getNodesAsStringList( elem, "./Entry/TimeStamp",
                                                                CommonNamepspaces.getNameSpaceContext() );
            return nodes.get( nodes.size() - 1 );

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
        return null;
    }

    /**
     * Authors the down time report and sends it to the designated users
     *
     * @param missedTests
     *            a hastable containing the missed tests
     * @param conf
     *            owsWatchConfiguration
     */
    protected void writeAndSendTimeDownEmail( Hashtable<Integer, String> missedTests, OwsWatchConfig conf ) {

        String message = createDownTimeReportBody( missedTests );
        String subject = Messages.getString( "TimeDownReport.subject" );
        List<String> emails = getUserEmails( conf );
        String mailFrom = conf.getGeneral().getMailFrom();
        String mailServer = conf.getGeneral().getMailServer();
        Iterator<String> email = emails.iterator();
        while ( email.hasNext() ) {
            // send message to the user
            EMailMessage mm = new EMailMessage( mailFrom, email.next(), subject, message );
            try {
                mm.setMimeType( MailMessage.PLAIN_TEXT );
                MailHelper.createAndSendMail( mm, mailServer );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage() );
            }

        }

    }

    /**
     * Create the content of the down time time report
     *
     * @param missedTests
     * @return the down time report
     */
    protected String createDownTimeReportBody( Hashtable<Integer, String> missedTests ) {
        StringBuffer body = new StringBuffer( 500 );
        body.append( Messages.getString( "TimeDownReport.address" ) );
        body.append( Messages.getMessage( "TimeDownReport.messageBody", Calendar.getInstance().getTime().toString() ) );
        Iterator<Integer> it = missedTests.keySet().iterator();
        while ( it.hasNext() ) {
            Integer next = it.next();
            String name = services.get( next ).getServiceName();
            String lastTest = missedTests.get( next );
            body.append( Messages.getMessage( "TimeDownReport.missedTest", name, lastTest ) );
        }
        body.append( Messages.getString( "TimeDownReport.closingWords" ) );
        body.append( Messages.getString( "TimeDownReport.signer" ) );
        return body.toString();
    }

    private List<String> getUserEmails( OwsWatchConfig conf ) {
        List<String> emails = new ArrayList<String>();
        Map<String, User> users = conf.getGeneral().getUsers();
        Iterator<User> user = users.values().iterator();
        while ( user.hasNext() ) {
            emails.add( user.next().getEmail() );
        }
        return emails;
    }

    /**
     * @return A map of all services
     */
    public Map<Integer, ServiceConfiguration> getServices() {
        return services;
    }

    /**
     * @return Map<Integer, ServiceLog> key = ServiceConfiguration id
     */
    public Map<ServiceConfiguration, ServiceLog> getServiceLogs() {
        return serviceLogs;
    }

    /**
     * @return true if thread is suspended, false otherwise
     */
    public boolean isThreadsuspended() {
        return threadsuspended;
    }

    /**
     * @param threadsuspended
     */
    public void setThreadsuspended( boolean threadsuspended ) {
        this.threadsuspended = threadsuspended;
    }
}
