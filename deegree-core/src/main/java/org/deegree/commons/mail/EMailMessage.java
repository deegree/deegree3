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
package org.deegree.commons.mail;

/**
 * This class encapsulates all the info need to send an email message. This object is passed to the MailerEJB
 * sendMail(...) method.
 *
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 * @author last edited by: $Author$
 *
 * @version $Revision$,$Date$
 */
public class EMailMessage implements java.io.Serializable, MailMessage {

    /**
     *
     */
    private static final long serialVersionUID = 7569652229263596515L;

    private String sender;

    private String subject;

    private String htmlContents;

    private String emailReceiver;

    private String mimeType;

    /**
     * Creates an empty email message with the default MIME type plain text.
     */
    private EMailMessage() {
        try {
            this.setMimeType( MailMessage.PLAIN_TEXT );
        } catch ( Exception ex ) {
            // nothing to do
        }
    }

    /**
     * Creates a new mail message with MIME type text/plain.
     *
     * @param from
     *            the sender
     * @param to
     *            the receiver list
     * @param subject
     *            the subject
     * @param messageBody
     *            the content of the message
     */
    public EMailMessage( String from, String to, String subject, String messageBody ) {
        this();

        this.setSender( from );
        this.setReceiver( to );
        this.setSubject( subject );
        this.setMessageBody( messageBody );
    }

    /**
     * Creates a new mail message with the given MIME type.
     *
     * @param from
     *            the sender
     * @param to
     *            the receiver list
     * @param subject
     *            the subject
     * @param messageBody
     *            the content of the message
     * @param mimeType
     *            the MIME type of the message body
     * @throws UnknownMimeTypeException
     *             if the given mimeType is not supported
     */
    public EMailMessage( String from, String to, String subject, String messageBody, String mimeType )
                            throws UnknownMimeTypeException {
        this( from, to, subject, messageBody );
        this.setMimeType( mimeType );
    }

    /**
     * Returns the state of this message. If sender and receiver are unequal null then this message is valid otherwise
     * invalid.
     *
     * @return validation state, <code>true</code> if sender and receiver are not <code>null</code>, otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        if ( this.getSender() != null && this.getReceiver() != null ) {
            return true;
        }
        return false;
    }

    /**
     * Return mail header including sender, receiver and subject.
     *
     * @return string with sender, receiver and subject
     */
    public String getHeader() {
        return ( "From:" + this.getSender() + ", To:" + this.getReceiver() + ", Subject:" + this.getSubject() );
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    @Override
    public String toString() {
        return ( "From:" + this.getSender() + ", To:" + this.getReceiver() + ", Subject:" + this.getSubject()
                 + ",Body: " + this.getMessageBody() );
    }

    /**
     * Method declaration
     *
     * @return sender
     */
    public String getSender() {
        return this.sender;
    }

    /**
     * Method declaration
     *
     * @return The messageBody value
     */
    public String getMessageBody() {
        return this.htmlContents;
    }

    /**
     * Method declaration
     *
     * @return emailReceiver
     */
    public String getReceiver() {
        return this.emailReceiver;
    }

    /**
     * Method declaration
     *
     * @param to
     */
    public void setReceiver( String to ) {
        this.emailReceiver = to;
    }

    /**
     * Method declaration
     *
     * @param message
     */
    public void setMessageBody( String message ) {
        this.htmlContents = message;
    }

    /**
     * Method declaration
     *
     * @param from
     */
    public void setSender( String from ) {
        this.sender = from;
    }

    /**
     * Method declaration
     *
     * @param title
     */
    public void setSubject( String title ) {
        this.subject = title;
    }

    /**
     * Gets the subject attribute of the EMailMessage object
     *
     * @return The subject value
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the mimeType attribute of the EMailMessage object
     *
     * @param mimeType
     *            The new mimeType value
     * @throws UnknownMimeTypeException
     *             if the given MIME type is not supported
     */
    public void setMimeType( String mimeType )
                            throws UnknownMimeTypeException {
        if ( mimeType.equalsIgnoreCase( MailMessage.PLAIN_TEXT ) ) {
            this.mimeType = mimeType;
        } else if ( mimeType.equalsIgnoreCase( MailMessage.TEXT_HTML ) ) {
            this.mimeType = mimeType;
        } else {
            throw new UnknownMimeTypeException( getClass().getName(), mimeType );
        }
    }

    /**
     * Gets the mimeType attribute of the EMailMessage object
     *
     * @return The mimeType value
     */
    public String getMimeType() {
        return this.mimeType;
    }

}
