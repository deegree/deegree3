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

package org.deegree.portal.portlet.jsp.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.deegree.i18n.Messages;

/**
 * Localisation workaround classes for JSP not hidden behind WEB-INF
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class IGeoPortalL10nTag extends TagSupport {

    /**
     *
     */
    private static final long serialVersionUID = 5544143865289937439L;
    private String key;

    @Override
    public int doStartTag()
                            throws JspException {

        JspWriter jw = pageContext.getOut();

        String mesg = Messages.getMessage( key );

        try {
            jw.write( mesg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key to set.
     */
    public void setKey( String key ) {
        this.key = key;
    }

    /**
     *
     * @param key
     * @return the message for <code>key</code>
     */
    public static final String getMessage( String key ) {
        String mesg = key;

        try {
            mesg = Messages.getMessage( key );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return mesg;
    }

}
