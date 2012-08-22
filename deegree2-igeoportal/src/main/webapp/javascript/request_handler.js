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
/**
 * central submit method for performing ajax KVP requests. The passed handler is
 * a javascript method that receives one parameter: the result as string
 * 
 * @param url
 *            target URL of a request
 * @param handler
 *            function that will be invoked when response is received. The
 *            function must receive one parameter containig the result of a
 *            request as a string
 * @param requestHeader
 *            assoziative array containing header fields (as "field") and
 *            assigend values (as "value")
 * @param asynch
 *            if true a request will be performed asynchronoulsy (default if
 *            missing or asynch == null is true)
 */
function submitGetRequest(url, handler, requestHeader, asynch) {

	var req = null;
	if (asynch == null) {
		asynch = true;
	}

	if (window.XMLHttpRequest) {
		req = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		try {
			req = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				req = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				alert(e);
			}
		}
	}

	req.onreadystatechange = function() {
		if (req.readyState == 4) {
			if (req.status == 200) {
				if (asynch) {
					handler(req.responseText);
				}
			} else {
				loggingHandler("Error: returned status code " + req.status
						+ " " + req.statusText);
			}
		}
	}

	req.open("GET", url, asynch);
	// header just can be set after opening a request
	// avoid caching
	req.setRequestHeader("If-Modified-Since", new Date(0));
	if (requestHeader != null) {
		setRequestHeader(req, requestHeader);
	}
	req.send(null);
	if (!asynch) {
		handler(req.responseText);
		return req.responseText;
	}
}

/**
 * central submit method for performing ajax requests with post content. The
 * passed handler is a javascript method that receives one parameter: the result
 * as string
 * 
 * @param url
 *            target URL of a request
 * @param handler
 *            function that will be invoked when response is received. The
 *            function must receive one parameter containig the result of a
 *            request as a string
 * @param content
 *            content that will be written into the post body of a request. This
 *            can be either a postable string or DOM object data.
 * @param requestHeader
 *            assoziative array containing header fields (as "field") and
 *            assigned values (as "value")
 * @param asynch
 *            if true a request will be performed asynchronoulsy (default if
 *            missing or asynch == null is true)
 */
function submitPostRequest(url, handler, content, requestHeader, asynch) {

	var req = null;
	if (asynch == null) {
		asynch = true;
	}

	if (window.XMLHttpRequest) {
		req = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		try {
			req = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				req = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				alert(e);
			}
		}
	}

	req.onreadystatechange = function() {
		if (req.readyState == 4) {
			if (req.status == 200) {
				if (asynch) {
					handler(req.responseText);
				}
			} else {
				loggingHandler("Error: returned status code " + req.status
						+ " " + req.statusText);
			}
		}
	}

	req.open("POST", url, asynch);
	// header just can be set after opening a request
	// avoid caching
	req.setRequestHeader("If-Modified-Since", new Date(0));
	if (requestHeader != null) {
		setRequestHeader(req, requestHeader);
	}
	req.send(content);
	if (!asynch) {
		handler(req.responseText);
		return req.responseText;
	}
}

function setRequestHeader(xmlHttpRequest, requestHeader) {
	for ( var i = 0; i < requestHeader.length; i++) {
		xmlHttpRequest.setRequestHeader(requestHeader[i]["field"],
				requestHeader[i]["value"]);
	}
}

/**
 * ajax logging handler method: just alerts the result
 */
function loggingHandler(value) {
	alert(value);
}