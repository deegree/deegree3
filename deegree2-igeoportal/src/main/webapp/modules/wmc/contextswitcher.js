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

function ContextSwitcher(label, listOfContexts, listSize, keepBBOX, bgcolor) {
	
    // variable declaration
    this.label = label;
    this.listOfContexts = listOfContexts;
    this.listSize = listSize;
    this.bgcolor = bgcolor;
    this.keepBBOX = 'false';
    
    // Both parameters "keepBBOX" and "bgcolor" are optional. 
    // keepBBOX is eihter 'true' or 'false'. If keepBBOX is ommitted, but bgcolor is set in the WMC, 
    // then the bgcolor will be passed in the keepBBOX parameter. 
    // Therefore, if keepBBOX is neither 'true' nor 'false', then it contains the bgcolor.
    if ( keepBBOX && keepBBOX != 'true' && keepBBOX != 'false' ) {
    	this.bgcolor = keepBBOX;    	
    } else {
    	this.keepBBOX = keepBBOX;
    }
    
    // method declaration
    this.paint = paint;
    this.repaint = repaint;
    this.createList = createList;
    this.getLabel = getLabel;
    this.switchContext = switchContext;
    this.setURL = setURL;

    // implementation
    
    function setURL ( url ) {
    	this.url = url;
    }
    
    function getLabel() {
        return this.label;
    }

    function paint(targetDocument, parentElement) {
        var span = targetDocument.createElement( "span" );
        span.setAttribute( "className", "listfont" );
        //var b = targetDocument.createElement( "b" );
        var font = targetDocument.createElement( "font" );
        
        //If condition added to enable loading images
        if ( label.length > 0 ) {
            /* this is the standard procedure */
            var ltxt = targetDocument.createTextNode( label );
            font.appendChild( ltxt );
        } else {
            /* Enables loading image instead of text */
            var img = targetDocument.createElement( "img" );
            img.setAttribute( "src", "./images/label_theme_select.gif" );
            font.appendChild( img );
        }
        //b.appendChild( font );
        //span.appendChild( b );
        span.appendChild( font );
        parentElement.appendChild( span );
        //var br = targetDocument.createElement( "BR" );
        //parentElement.appendChild( br );

        var sel = this.createList(targetDocument, parentElement);
        parentElement.appendChild( sel );
        
        //JM: refactored (css)
        //if param bgcolor is set in WMC, this overrides settings in deegree.css
        if ( this.bgcolor != null && this.bgcolor != "undefined" ) {
            parentElement.style.backgroundColor = this.bgcolor;
        }
    }

    function repaint() { }

    function createList(targetDocument) {
        var sel = targetDocument.createElement("select");
        sel.setAttribute("style", "width:95%;");
        sel.setAttribute("name", "contexts" );

        if( listSize< 0 ){
            listSize = 1;
        }

        sel.setAttribute("size", listSize);
        sel['onchange'] = new Function ( "controller.vContextSwitcher.switchContext(this);" );

        var list = listOfContexts.split(";");
        for (var i = 0; i < list.length; i++ ){
            var opt = targetDocument.createElement("option");
            var mc = list[i].split("|");
            if( mc[1] != "" && mc[1] != null && mc[0] != "" && mc[0] != null ){
                opt.setAttribute("value", mc[1] );
                if ( mc[2] != "" && mc[2] != null && mc[2] == "group" ) {
					opt.style.color = "#3333FF"; // change text color
					/* not in IE, only in Firefox */
					//opt.style.fontWeight = "bold";
					//opt.style.fontStyle = "italic";
					//opt.style.textDecoration="underline";
					//opt.style.textIndent="10%";
				}
                var txt = targetDocument.createTextNode( mc[0] );
                opt.appendChild( txt );
                sel.appendChild( opt );
            }
        }
        return sel;
    }

    function switchContext( sel ) {
        if( sel.selectedIndex != 0 ){
            var env = controller.mapModel.getBoundingBox();
            var req = this.url + "?rpc=<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
         		"<methodName>mapClient:contextSwitch</methodName><params><param><value><struct>"+
                "<member><name>mapContext</name><value><string>" + sel.value + "</string></value></member>";
                if ( this.keepBBOX == 'true' ) {
                	req += "<member><name>boundingBox</name><value><struct>"+
	                "<member><name>minx</name><value><double>" + env.minx + "</double></value></member>" + 
	                "<member><name>miny</name><value><double>" + env.miny + "</double></value></member>" +
	                "<member><name>maxx</name><value><double>" + env.maxx + "</double></value></member>" + 
	                "<member><name>maxy</name><value><double>" + env.maxy + "</double></value></member>" +
	                "</struct></value></member>";
                }
            req += "</struct></value></param></params></methodCall>";
            parent.window.location.replace(req);
        }
    }
}
