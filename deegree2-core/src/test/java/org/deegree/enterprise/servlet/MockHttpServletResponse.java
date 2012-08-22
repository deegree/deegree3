//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/enterprise/servlet/MockHttpServletResponse.java $
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
package org.deegree.enterprise.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class MockHttpServletResponse implements HttpServletResponse {
    private Writer myWriter;

    public MockHttpServletResponse(Writer writer) {
        myWriter = writer;
    }

    public void addCookie(Cookie arg0) {
        // TODO Auto-generated method stub

    }

    public boolean containsHeader(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public String encodeURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeRedirectURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeRedirectUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void sendError(int arg0, String arg1) throws IOException {
        // TODO Auto-generated method stub

    }

    public void sendError(int arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    public void sendRedirect(String arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    public void setDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    public void addDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    public void setHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void addHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void setIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public void addIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public void setStatus(int arg0) {
        // TODO Auto-generated method stub

    }

    public void setStatus(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public String getCharacterEncoding() {
        return "utf-8";
    }

    public ServletOutputStream getOutputStream() throws IOException {
        ServletOutputStream out = new ServletOutputStream() {

            public void write(int b) throws IOException {
                myWriter.write(b);

            }
        };
        return out;
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(myWriter);
    }

    public void setContentLength(int arg0) {
        // TODO Auto-generated method stub

    }

    public void setContentType(String arg0) {
        // TODO Auto-generated method stub

    }

    public void setBufferSize(int arg0) {
        // TODO Auto-generated method stub

    }

    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub

    }

    public void resetBuffer() {
        // TODO Auto-generated method stub

    }

    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void setLocale(Locale arg0) {
        // TODO Auto-generated method stub

    }

    public Locale getLocale() {
        // TODO Auto-generated method stub
        return Locale.getDefault();
    }


    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }
    public void setCharacterEncoding( String arg0 ) {
        // TODO Auto-generated method stub

    }
};
