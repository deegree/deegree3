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
 * Interface of a email message.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public interface MailMessage {

    /**
     * MIME type <code>text/plain</code>
     */
    String PLAIN_TEXT = "text/plain";

    /**
     * MIME type <code>text/html</code>
     */
    String TEXT_HTML = "text/html";

    /**
     * MIME type <code>text/xml</code>
     */
    String TEXT_XML = "text/xml";

    /**
     * HTML mulitpart message with inline elements
     *
     * @see javax.mail.Part
     */
    short PART_INLINE = 0;

    /**
     * Mulitpart message with references
     *
     * @see javax.mail.Part
     */
    short PART_REF = 1;

    /**
     * Returns the subject.
     *
     *
     * @return the subjec string
     */
    String getSubject();

    /**
     * Returns the sender
     *
     *
     * @return the sender mail address
     */
    String getSender();

    /**
     * Returns the message body
     *
     * @return string representation of the message body
     */
    String getMessageBody();

    /**
     * Returns the receiver mail address
     *
     *
     * @return the mail address of the receiver
     */
    String getReceiver();

    /**
     * Sets the receiver mail address
     *
     *
     * @param to
     *            the receiver mail address
     */
    void setReceiver( String to );

    /**
     * Sets the message body
     *
     * @param message
     *            the string representation of the message body
     */
    void setMessageBody( String message );

    /**
     * Sets the sender mail address.
     *
     *
     * @param from
     *            the sender mail address
     */
    void setSender( String from );

    /**
     * Sets the subject.
     *
     * @param title
     *            the message subject
     */
    void setSubject( String title );

    /**
     * Return mail header including sender, receiver and subject.
     *
     * @return string with sender, receiver and subject
     */
    String getHeader();

    /**
     * Sets the MIME type of the message
     *
     * @param mimeType
     *            the MIME type as string
     * @throws UnknownMimeTypeException
     *
     * @see #PLAIN_TEXT
     * @see #TEXT_HTML
     * @see <a href="http://www.ietf.org/rfc/rfc1341.txt">IETF MIME RFC</a>
     * @see javax.mail.Part#setContent(java.lang.Object, java.lang.String)
     * @see javax.mail.Part#setText
     */
    void setMimeType( String mimeType )
                            throws UnknownMimeTypeException;

    /**
     * Returns the MIME type of the message body.
     *
     * @return the MIME type as a string
     *
     * @see javax.mail.Part#getContentType
     */
    String getMimeType();

    /**
     * Validates the message.
     *
     * @return <code>true</code> - if the message is complete, otherwise false.
     */
    boolean isValid();

}
