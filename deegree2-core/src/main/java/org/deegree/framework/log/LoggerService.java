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
package org.deegree.framework.log;

// J2EE 1.3
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;

import org.deegree.framework.jndi.JndiUtils;
import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.MailMessage;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.version.Version;
import org.deegree.framework.xml.XMLFragment;

/**
 * The Logger is used to log messages to files. This service will use a logging service provided by the application
 * server or a 3rd party logging service such as Apache Log4J to enable asychronous call of the method log(). The log
 * server is configured by a set of Properties which are provided to the class init.
 * <p>
 * There are some global properties as well: <BR>
 * <UL>
 * <LI><B>log.class </B>: the logging class.
 * <LI><B>log.active </B>: to enable or disabel the logging service
 * <LI><B>log.mail.active </B>: to activate the email notification
 * <LI><B>log.mail.to </B>: the mail address
 * <LI><B>log.mail.session </B>: the mail session used to send mail
 * </UL>
 * <P>
 * Messages are logged using log(). If an error occurs during creation or logging, the message will be written to the
 * server BootLogger.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 *
 * @author last edited by: UID=$Author$
 *
 * @version $Revision$, $Date$
 *
 * @see LoggerFactory
 * @see org.deegree.framework.util.BootLogger
 */
abstract class LoggerService implements ILogger {

    /**
     * the log channel name
     */
    protected static String defaultChannelName;

    private static String hostAddress;

    private static String hostName;

    private static String mailNotificationAddress;

    private static String mailSessionName;

    private static Session mailSession;

    private static String mailHostName;

    private static boolean sendMailIsActive;

    private static DateFormat df;

    public void init( Properties props ) {

        df = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );

        try {

            // fetch all configuration parameters
            LoggerService.defaultChannelName = props.getProperty( "log.channel.name" );
            LoggerService.mailNotificationAddress = props.getProperty( "log.mail.to" );
            String mailerActive = props.getProperty( "log.mail.active" );
            LoggerService.mailSessionName = props.getProperty( "log.mail.session" );
            LoggerService.mailHostName = props.getProperty( "log.mail.smtphost" );

            // set defaults if not set
            if ( mailerActive == null || mailerActive.equalsIgnoreCase( "false" ) ) {
                LoggerService.sendMailIsActive = false;
            }
            if ( defaultChannelName == null ) {
                LoggerService.defaultChannelName = LoggerService.class.getName();
            }
            if ( mailNotificationAddress == null ) {
                LoggerService.mailNotificationAddress = "UNKNOWN@" + LoggerService.mailHostName;
            }

            hostAddress = InetAddress.getLocalHost().getHostAddress();
            hostName = InetAddress.getLocalHost().getHostName();

        } catch ( UnknownHostException ex ) {
            BootLogger.log( "Unable to determine host: " + ex.getMessage() );
        } catch ( Exception ex ) {
            ex.printStackTrace();
            BootLogger.log( "Error while initializing " + LoggerService.class.getName() + " : " + ex.getMessage() );
        } finally {
            BootLogger.logDebug( "Using: defaultChannelName=" + defaultChannelName + ", mailNotificationAddress="
                                 + mailNotificationAddress + ", mailSessionName=" + mailSessionName
                                 + ", log.mail.active=" + LoggerService.sendMailIsActive );
        }
    }

    /**
     * Create logger instance
     *
     */
    protected LoggerService() {
        // only callable for the package.
    }

    /**
     * Log error with exception
     *
     * @param message
     *            the log message
     * @param e
     *            the exception to be logged
     * @param properties
     *            a given Property file in which specific email notification possibilities are saved.
     */
    public final void logError( String message, Throwable e, Map<?, ?> properties ) {
        this.logError( message, e );
        if ( sendMailIsActive ) {
            this.sendMail( message, e, properties );
        }
    }

    /**
     * Log warning message
     *
     * @param message
     *            the log message
     */
    public abstract void logWarning( String message );

    /**
     * Log warning message with exception
     *
     * @param message
     *            the log message
     * @param e
     *            the exception to be logged
     */
    public abstract void logWarning( String message, Throwable e );

    /**
     * Log info message
     *
     * @param message
     *            the log message
     */
    public abstract void logInfo( String message );

    /**
     * Log info message
     *
     * @param message
     *            the log message
     * @param e
     *            the exception to be logged
     */
    public abstract void logInfo( String message, Throwable e );

    /**
     * Log debug message
     *
     * @param message
     *            the log message
     */
    public abstract void logDebug( String message );

    /**
     * Log debug message.
     *
     * @param message
     *            the log message
     * @param e
     *            the exception to be logged
     */
    public abstract void logDebug( String message, Throwable e );

    /**
     * Logs the given text to the specified file if log level is set to <code>LOG_DEBUG</code>.
     *
     * @param file
     *            file to log to
     * @param content
     *            text to be logged
     */
    public void logDebugFile( File file, String content ) {
        if ( getLevel() == LOG_DEBUG ) {
            logDebug( "Writing debug file '" + file.getAbsolutePath() + "'." );
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter( new FileWriter( file ) );
                writer.write( content );
            } catch ( IOException e ) {
                String msg = "Could not write to debug file '" + file.getAbsolutePath() + "'.";
                logError( msg, e );
            } finally {
                if ( writer != null ) {
                    try {
                        writer.close();
                    } catch ( IOException e ) {
                        String msg = "Error closing debug file '" + file.getAbsolutePath() + "'.";
                        logError( msg, e );
                    }
                }
            }
        }
    }

    /**
     * Logs the given text to a temporary file (created from specified prefix and suffix) if log level is set to
     * <code>LOG_DEBUG</code>.
     *
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param content
     *            text to be logged
     */
    public void logDebugFile( String filePrefix, String fileSuffix, String content ) {
        if ( getLevel() == LOG_DEBUG ) {
            try {
                File tmpFile = File.createTempFile( filePrefix, fileSuffix );
                logDebugFile( tmpFile, content );
            } catch ( IOException e ) {
                String msg = "Cannot create debug file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".";
                logError( msg, e );
            }
        }
    }

    /**
     * Logs the given {@link XMLFragment} to a temporary file (created from specified prefix and suffix ".xml") if log
     * level is set to <code>LOG_DEBUG</code>.
     *
     * @param filePrefix
     *            prefix for the temp file name
     * @param fragment
     *            XMLFragment to be logged (will be pretty-printed)
     */
    public void logDebugXMLFile( String filePrefix, XMLFragment fragment ) {
        if ( getLevel() == LOG_DEBUG ) {
            if ( fragment == null ) {
                logDebug( "The given xmlfragment is null." );
            } else {
                logDebugFile( filePrefix, ".xml", fragment.getAsPrettyString() );
            }

        }
    }

    /**
     * Logs the given binary data to the specified file if log level is set to <code>LOG_DEBUG</code>.
     *
     * @param file
     *            file to log to
     * @param data
     *            binary data to be logged
     */
    public void logDebugBinaryFile( File file, byte[] data ) {
        if ( getLevel() == LOG_DEBUG ) {
            logDebug( "Writing binary debug file '" + file.getAbsolutePath() + "'." );
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream( new FileOutputStream( file ) );
                out.write( data );
            } catch ( IOException e ) {
                String msg = "Could not write to debug file '" + file.getAbsolutePath() + "'.";
                logError( msg, e );
            } finally {
                if ( out != null ) {
                    try {
                        out.close();
                    } catch ( IOException e ) {
                        String msg = "Error closing debug file '" + file.getAbsolutePath() + "'.";
                        logError( msg, e );
                    }
                }
            }
        }
    }

    /**
     * Logs the given binary data to a temporary file (created from specified prefix and suffix) if log level is set to
     * <code>LOG_DEBUG</code>.
     *
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param data
     *            binary data to be logged
     */
    public void logDebugBinaryFile( String filePrefix, String fileSuffix, byte[] data ) {
        if ( getLevel() == LOG_DEBUG ) {
            try {
                File tmpFile = File.createTempFile( filePrefix, fileSuffix );
                logDebugBinaryFile( tmpFile, data );
            } catch ( IOException e ) {
                String msg = "Cannot create debug file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".";
                logError( msg, e );
            }
        }
    }

    /**
     * Sends email with exception string. If mail session or mail notification address is null no email is send.
     *
     * @param message
     *            message is in mail subject
     * @param ex
     *            full exception is displayed in body
     * @param properties
     *            list of properties listed in body
     */
    protected void sendMail( String message, Throwable ex, Map<?, ?> properties ) {
        if ( sendMailIsActive && mailSessionName != null && mailNotificationAddress != null ) {
            this.sendMailNotification( message, ex, properties );
        }
    }

    private void sendMailNotification( String message, Throwable ex, Map<?, ?> properties ) {
        MailMessage mail;
        StringBuffer emailBody = new StringBuffer();

        emailBody.append( "A critical error occured in " + Version.getVersion() );
        emailBody.append( " running on " + hostName + "/" + hostAddress );
        emailBody.append( " on " + df.format( new Date( System.currentTimeMillis() ) ) );
        emailBody.append( "\n\n" );
        if ( message != null ) {
            emailBody.append( "\n\nThe error message: " + message );
        }
        if ( properties != null ) {
            emailBody.append( "\n\nRequest data:\n--------------\n" + properties.toString() );
        }
        if ( ex != null ) {
            emailBody.append( "\n\nException:\n---------\n" + ex.getMessage() );
            emailBody.append( "\n\nStack Trace:\n------------\n\n" + ex.toString() );
        }
        emailBody.append( "\n\nSystem Info:\n--------------\n" + getSystemInfo() );

        String subject = "Critical Error:" + Version.getVersion() + " on " + hostName + "/" + hostAddress;

        mail = new EMailMessage( "DO_NOT_REPLY@" + hostName, mailNotificationAddress, subject, emailBody.toString() );
        try {
            if ( mailSession == null ) {
                mailSession = this.getMailSession();
            }
            new MailHelper().createAndSendMail( mail, mailSession );
        } catch ( Exception e ) {
            BootLogger.logError( "Can't send email notification: " + mail.getHeader(), e );
        }
    }

    /**
     * Return system information (memory and properties) as string.
     *
     * @return system information (memory and properties) as string.
     */
    private String getSystemInfo() {
        StringBuffer buf = new StringBuffer();
        buf.append( "Total Memory: " + Runtime.getRuntime().totalMemory() / 1024 + " Kilobyte\n" );
        buf.append( "Free Memory: " + Runtime.getRuntime().freeMemory() / 1024 + " Kilobyte\n" );
        java.util.Properties sysprops = System.getProperties();
        for ( Enumeration<Object> e = sysprops.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement().toString();
            String value = sysprops.getProperty( key );
            buf.append( key + " : " + value + "\n" );
        }
        return buf.toString();
    }

    /**
     *
     */
    private Session getMailSession() {
        Session session = null;
        try {
            session = (Session) JndiUtils.lookup( mailSessionName, Session.class );
        } catch ( Exception ex ) {
            BootLogger.logError( "Error while initializing " + LoggerService.class.getName() + " : " + ex.getMessage(),
                                 ex );
        } finally {
            if ( session == null ) {
                Properties p = System.getProperties();
                p.put( "mail.smtp.host", LoggerService.mailHostName );
                session = Session.getDefaultInstance( p, null );
            }
        }
        return session;
    }

}
