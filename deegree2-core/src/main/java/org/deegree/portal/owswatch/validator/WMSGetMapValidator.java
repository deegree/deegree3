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

package org.deegree.portal.owswatch.validator;

import java.io.InputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpMethodBase;
import org.deegree.framework.util.StringTools;

import org.deegree.portal.owswatch.Status;
import org.deegree.portal.owswatch.ValidatorResponse;

/**
 * A specific implementation of AbstractValidator
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WMSGetMapValidator extends AbstractValidator implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5983538240882784183L;

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.portal.owswatch.validator.AbstractValidator#validateAnswer(org.apache.commons.httpclient.HttpMethodBase,
     *      int)
     */
    @Override
    public ValidatorResponse validateAnswer( HttpMethodBase method, int statusCode ) {

        String contentType = method.getResponseHeader( "Content-Type" ).getValue();
        String lastMessage = null;
        Status status = null;

        if ( !contentType.contains( "image" ) ) {
            if ( !contentType.contains( "xml" ) ) {
                status = Status.RESULT_STATE_UNEXPECTED_CONTENT;
                lastMessage = StringTools.concat( 100, "Error: Response Content is ", contentType, " not image" );
                return new ValidatorResponse( lastMessage, status );
            } else {
                return validateXmlServiceException( method );
            }
        }

        try {
            InputStream stream = copyStream( method.getResponseBodyAsStream() );
            stream.reset();
            ImageIO.read( stream );
            status = Status.RESULT_STATE_AVAILABLE;
            lastMessage = status.getStatusMessage();
            return new ValidatorResponse( lastMessage, status );
        } catch ( Exception e ) {
            status = Status.RESULT_STATE_SERVICE_UNAVAILABLE;
            lastMessage = e.getLocalizedMessage();
            return new ValidatorResponse( lastMessage, status );
        }
    }
}
