//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

function MenuBarTop(id) {

	this.id = id;
	this.paint = paint;
	this.repaint = repaint;	
	this.openHelp = openHelp;
	this.gotoHome = gotoHome;
	this.openSaveContext = openSaveContext;
	this.openLoadContext = openLoadContext;
	this.openLogin = openLogin;
	this.openLegal = openLegal;
	this.performLogout = performLogout;
	
	function paint(targetDocument, parentNode) {	
	}
	function repaint() {
	}
	
	function openHelp() {            
        var fiw = window.open( "./help/en/help.html", "Help",
                               "width=925,height=775,left=100,top=100,resizable=no,scrollbars=yes");
        fiw.focus();
    }
	
	function gotoHome() {
        var fiw = window.open( "http://www.deegree.org", "Home",
                               "width=600,height=400,left=100,top=100,scrollbars=yes");
        fiw.focus();
        //parent.controller.replace("http://www.deegree.org");
    }
	
	function openSaveContext() {
        var fiw = null;
        if ( controller.vSessionKeeper != null && parent.controller.vSessionKeeper.id == null ) {
            /* the user has to login before saving a new context to the user's folder */
            fiw = window.open( "./modules/authentication/missinglogin.jsp" ,"LoginInfo",
                               "width=500,height=200,left=100,top=100,scrollbars=yes" );
        } else {
            fiw = window.open( "./modules/wmc/savecontext.jsp", "Save",
                               "width=500,height=200,left=100,top=100,scrollbars=yes" );
        }
        fiw.focus();
    }
	
	function openLoadContext() {
        if ( controller.vSessionKeeper == null ) {
            var s = "control?rpc=<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
                    "<methodName>mapClient:listContexts</methodName><params><param><value><struct>"+
                    "<member><name>sessionID</name><value><string>ID1</string></value></member>" +
                    "</struct></value></param></params></methodCall>";
        } else {
            /* load context from the user's folder */
            if ( controller.vSessionKeeper.id == null ) {
                fiw = window.open( "./modules/authentication/missinglogin.jsp", "LoginInfo",
                                   "width=500,height=300,left=100,top=100,scrollbars=yes" );
            } else {
                var s = "control?rpc=<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
                        "<methodName>mapClient:listContexts</methodName><params><param><value><struct>"+
                        "<member><name>sessionID</name><value><string>" + controller.vSessionKeeper.id + "</string></value></member>" +
                        "</struct></value></param></params></methodCall>";
            }
        }
        fiw = window.open( s, "Load_and_manage_context", "width=550,height=300,left=100,top=100,scrollbars=yes");
        fiw.focus();
    }
	
	/* needed for wmc_testSecurity.xml */
    function openLogin() {
        if ( controller.vSessionKeeper != null ) {
            var fiw = parent.open( "./modules/authentication/login.jsp" ,"Login",
                                   "width=500,height=300,left=100,top=100,scrollbars=yes" );
            fiw.focus();
        } else {
            var msg = "<h3>This feature is not available</h3><p>The security feature is not enabled for this map context.</p>";
            fiw = window.open( "message.jsp?msg=" + encodeURIComponent( msg ), "Information", "width=400,height=250,left=100,top=100,scrollbars=yes");
        }
    }
    
    /* needed for wmc_testSecurity.xml */
    function performLogout() {
        if ( controller.vSessionKeeper != null ) {
            if ( controller.vSessionKeeper.id != null ) {
                var fiw = window.open( "./modules/authentication/logout.jsp", "Logout",
                                       "width=500,height=300,left=100,top=100,scrollbars=yes" );
                fiw.focus();
            } else {
                alert( "Logout not possible: there is no user logged in." );
            }
        } else {
            var msg = "<h3>This feature is not available</h3><p>The security feature is not enabled for this map context.</p>";
            fiw = window.open( "message.jsp?msg=" + encodeURIComponent( msg ), "Information", "width=400,height=250,left=100,top=100,scrollbars=yes");
        }
    }
    
    function openLegal() {
        var fiw = window.open( "./modules/welcome/legal.html", "CopyrightLicense", 
        		               "width=830, height=600, left=100, top=100, resizable=no, scrollbars=yes");
        fiw.focus();
    }
}
