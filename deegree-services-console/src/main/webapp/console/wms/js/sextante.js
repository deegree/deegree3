// WFS-Operations
var WFS_GET_CAPABILITIES = "?SERVICE=WFS&REQUEST=GetCapabilities";
var WFS_DESCRIBE_FEATURE_TYPE_WFS = "?SERVICE=WFS&REQUEST=DescribeFeatureType&VERSION=1.0.0&TYPENAME=";
var WFS_GET_FEATURE = "?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=";

// WFS URLs for init
var wfsURLs = new Array();


// WPS-Operations
var WPS_GET_CAPABILITIES = "?SERVICE=WPS&REQUEST=GetCapabilities";
var WPS_DESCRIBE_PROCESS = "?SERVICE=WPS&REQUEST=DescribeProcess&VERSION=1.0.0&IDENTIFIER=";
var WPS_EXECUTE = "?SERVICE=WPS&REQUEST=Execute&VERSION=1.0.0&IDENTIFIER=";

// WPS URLs for init
var wpsURLs = new Array();

// make map available for easy debugging
var map;

// proxy for external data sources
OpenLayers.ProxyHost = "cgi-bin/proxy.py?url="; // saved:
												// /usr/lib/cgi-bin/proxy.py


// Array of layer array (layer, attr1, attr2, ...)
var layers = new Array();


// HTML-Elemente
var selectProcesses;
var selectWPS;
var divInputValues;
var selectLayers = new Array();
var inputLiterals = new Array();


// increase reload attempts
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

// new OpenLayers.Geometry.Point(0, 52).transform(new
// OpenLayers.Projection("EPSG:900913"),new
// OpenLayers.Projection("EPSG:4326"))

function init() {

	// ------------------------------------------------------------------------------------------------------
	// map options
	var options = {
		projection : new OpenLayers.Projection("EPSG:4326")
		// projection : new OpenLayers.Projection("EPSG:900913"),
		// displayProjection : new OpenLayers.Projection("EPSG:4326"),
		// units : "m"
	// numZoomLevels : 18,
	// maxResolution : 156543.0339,
	// maxExtent : new OpenLayers.Bounds(-20037508, -20037508, 20037508,
	// 20037508.34)
	};

	// ------------------------------------------------------------------------------------------------------
	// create map
	map = new OpenLayers.Map('map', options);

	// ------------------------------------------------------------------------------------------------------
	// add wms as basic layer
	var wms = new OpenLayers.Layer.WMS("OpenLayers BASIC",
			"http://labs.metacarta.com/wms/vmap0", {
				layers : 'basic',
				crs : "EPSG:4326"
			});
	map.addLayer(wms);

	// ------------------------------------------------------------------------------------------------------
	// add wms as layer
	var wms2 = new OpenLayers.Layer.WMS("OpenLayers STATE LABEL",
			"http://labs.metacarta.com/wms/vmap0", {
				layers : 'statelabel',
				format : "image/png",
				transparent : "true",
				crs : "EPSG:4326"
			}, {
				isBaseLayer : false,
				visibility : false
			});
	map.addLayer(wms2);

	// ------------------------------------------------------------------------------------------------------
	// add a marker
	var vectorLayer = new OpenLayers.Layer.Vector("TEST Maker");
	var feature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point(-0.0014, -0.0024),
			{
				some : 'data'
			},
			{
				externalGraphic : 'http://funmap.co.uk/cloudmade-examples/markers/marker.png',
				graphicHeight : 37,
				graphicWidth : 24
			});
	vectorLayer.addFeatures(feature);
	map.addLayer(vectorLayer);

	// ------------------------------------------------------------------------------------------------------
	// add gml files as layer (only gml2 without schema)
	addGMLLayer("GML2 Polygon", "gml/GML2_FeatureCollection_Polygon.xml", "");
	addGMLLayer("GML2 Buffered Polygon",
			"gml/GML2_FeatureCollection_Polygon_Buffered.xml", "");

	// ------------------------------------------------------------------------------------------------------
	// add web feature service (only gml2)
	addWFSURL( "http://giv-wps.uni-muenster.de:8080/geoserver/wfs");
	addWFSURL(  "http://www.dge.upd.edu.ph/geoserver/wfs");
	
	addWFSLayer("WFS topp:states", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","topp:states", true);
	addWFSLayer("WFS ns1:tasmania_roads", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","ns1:tasmania_roads", true);

	
	//
	// ------------------------------------------------------------------------------------------------------
	// add WPS
	addWPSURL( "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services");
	addWPSURL( "http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService");
	
	// centroid algorithm
	var data = new WPSInputData();
	var wfsFeatureURL = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states";
	data.addVectorLayer("LAYER", wfsFeatureURL, WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
	data.addOutputVectorLayerFormat("RESULT", WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
	addWPSLayer("WPS centroids", "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services", "st_centroids", data);
	
	// transform algorithm
	var data2 = new WPSInputData();
	var wfsFeatureURL2 = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states";
	data2.addVectorLayer("LAYER", wfsFeatureURL2, WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
	data2.addLiteral("DISTANCEX", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "-20");
	data2.addLiteral("DISTANCEY", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "40");
	data2.addLiteral("ANGLE", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "90");
	data2.addLiteral("SCALEX", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "0.5");
	data2.addLiteral("SCALEY", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "0.5");
	data2.addLiteral("ANCHORX", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "-46");
	data2.addLiteral("ANCHORY", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "35");
	data2.addOutputVectorLayerFormat("RESULT", WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
	addWPSLayer("WPS transform", "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services", "st_transform", data2);
	
	// fixeddistancebuffer algorithm
	var data3 = new WPSInputData();
	var wfsFeatureURL3 = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=ns1:tasmania_roads";
	data3.addVectorLayer("LAYER", wfsFeatureURL3, WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
	data3.addLiteral("DISTANCE", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "0.05");
	data3.addLiteral("TYPES", WPSInputData.LITERAL_SELECTION_TYPE, "0");
	data3.addLiteral("RINGS", WPSInputData.LITERAL_SELECTION_TYPE, "0");
	data3.addLiteral("NOTROUNDED", WPSInputData.LITERAL_BOOLEAN_TYPE, "false");
	data3.addOutputVectorLayerFormat("RESULT", WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
	addWPSLayer("WPS fixeddistancebuffer", "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services", "st_fixeddistancebuffer", data3);

	
	test = determineWPSProcessDescription("http://flexigeoweb.lat-lon.de/deegree-wps-demo/services","st_transform");
	
	// ------------------------------------------------------------------------------------------------------
	// add controls
	map.addControl(new WPSSwitcher());
	map.addControl(new OpenLayers.Control.LayerSwitcher());
	map.addControl(new OpenLayers.Control.MousePosition());

	// ------------------------------------------------------------------------------------------------------
	// set center
	map.setCenter(new OpenLayers.LonLat(0, 0), 2);
}

function getStyleMap(){

	var color1="#FF8A14";
	
	var styleMap = new OpenLayers.StyleMap({ 
		strokeColor: color1, 
		fillColor : color1, 
		strokeWidth: 2, 
		pointRadius : 2,
		fillOpacity: 0.4
	});
		
	return styleMap;
}

/**
 * This Method adds a GML file to the map.
 * 
 * @param gmlName
 *            Layer name.
 * @param gmlURL
 *            URL of GML file.
 * @param attr
 *            Optional attribute like absolute URL of GML file.
 */
function addGMLLayer(gmlName, gmlURL, attr) {

	// check whether name available
	if (layers[gmlName] != null)
		gmlName += "_1"; // change name

	// create a GML layer
	var layer = new OpenLayers.Layer.GML(gmlName, gmlURL, { styleMap: getStyleMap() });

	// var layerProtocol = new OpenLayers.Protocol.HTTP( {
	// url : url,
	// format : new OpenLayers.Format.GML()
	// });
	//
	// var layer = new OpenLayers.Layer.Vector(name, {
	// strategies : [ new OpenLayers.Strategy.BBOX() ],
	// protocol : layerProtocol
	// });

	// notice layer
	var gmlArray = new Array();
	gmlArray[0] = layer;
	gmlArray[1] = attr;
	layers[gmlName] = gmlArray;

	// add layer to map
	map.addLayer(layer);
}

/**
 * This method adds a WFS layer to map.
 * 
 * @param wfsURL
 *            URL of WFS.
 * @param wfsName
 *            Name of WFS.
 * @param wfsFeatureTypeWithPrefix
 *            FeatureTypeName with prefix.
 * @param visible
 *            visible=true.
 */
function addWFSLayer(wfsName, wfsURL, wfsFeatureTypeWithPrefix, visible) {

	// check whether name available
	if (layers[wfsName] != null)
		wfsName += "_1"; // change name

	// determine namespace
	var ns = determineWFSFeatureTypeNamespace(wfsURL, wfsFeatureTypeWithPrefix);
	
	// check namespace
	if(ns == null)
		return;
	
	// check name
	var ftArray = wfsFeatureTypeWithPrefix.split(":");
	if(ftArray.length != 2){
		var name = new Array();
		name[0] = "";
		name[1] = wfsFeatureTypeWithPrefix;
		ftArray = name;
		// alert("FeatureType isn't in the correct format like 'prefix:name'.");
	}
	
	// create wfs layer
	var wfs = new OpenLayers.Layer.Vector(wfsName, {
		strategies : [ new OpenLayers.Strategy.BBOX() ],
		protocol : new OpenLayers.Protocol.WFS( {
			url : wfsURL,
			featureType : ftArray[1],
			featureNS : ns
		}), visibility : visible, styleMap: getStyleMap()
	});
	
	// add wfs layer to map
	var wfsArray = new Array();
	wfsArray[0] = wfs;
	wfsArray[1] = wfsURL + WFS_GET_FEATURE + wfsFeatureTypeWithPrefix;
	layers[wfsName] = wfsArray;
	map.addLayer(wfs);
}

/**
 * This method adds all layers of a WFS to the map.
 * 
 * @param wfsName
 *            Name of WFS.
 * @param wfsURL
 *            URL of WFS.
 */
function addWFSLayers(wfsName, wfsURL){
	
	// determine feature type names
	var types = determineWFSFeatureTypeNames(wfsURL);
	
	// add all layers
	for ( var i = 0; i < types.length; i++) {	
		addWFSLayer(wfsURL, wfsName +" " + types[i], types[i] , false);
	}
}

function addWFSURL(url){
	wfsURLs[url] = url;
}

function removeWFSURL(url){
	wfsURLs[url] = null;
}

/**
 * This method adds a layer by WPS.
 * 
 * @param wpsURL
 *            URL of WPS.
 * @param wpsProcess
 *            Identifier of process
 * @param data
 *            Process data, use class WPSInputData
 */
function addWPSLayer(wpsName, wpsURL, wpsProcess, data){
	var gmlName = wpsName;
	var attr = wpsURL + WPS_EXECUTE + wpsProcess + data.toString();
	var gmlURL = OpenLayers.ProxyHost + escape(attr);	
	addGMLLayer(gmlName, gmlURL, attr);
}

function addWPSURL(url){
	wpsURLs[url] = url;
}

function removeWPSURL(url){
	wpsURLs[url] = null;
}

/**
 * The method retuns an array of the FeatureTypeNames of the WFS.
 * 
 * @param wfsURL
 *            URL of WFS.
 * 
 * @return Array of FeatureTypeNames (prefix : name).
 */
function determineWFSFeatureTypeNames(wfsURL) {

	// url of xml file
	var url = OpenLayers.ProxyHost + wfsURL;
	url += escape(WFS_GET_CAPABILITIES);

	// parse xml file
	var request = getXMLHttpRequest('text/xml');
	request.open('GET', url, false);
	request.send();
	var xmlDoc = request.responseXML;
		
	// determine FeatureTypes
	var collectionOfFeatureTypes = xmlDoc.getElementsByTagName("FeatureType");
	var arrayOfFeatureTypes = new Array();
	if (collectionOfFeatureTypes.length > 0)
		for ( var int = 0; int < collectionOfFeatureTypes.length; int++) {
			arrayOfFeatureTypes[int] = collectionOfFeatureTypes[int].childNodes[0].firstChild.nodeValue;
		}
	else
		alert("Can't determine feature type names of the WFS.");

	// for debugging
	determineWFSFeatureTypeNames_XML = xmlDoc;
	determineWFSFeatureTypeNames_ARRAY = arrayOfFeatureTypes;
	
	return arrayOfFeatureTypes;
}


/**
 * This methode returns the namespace of a feature.
 * 
 * @param wfsURL
 *            URL of the WFS.
 * 
 * @param featureTypeName
 *            Name of a feature.
 * 
 * @return Namespace of feature.
 */
function determineWFSFeatureTypeNamespace(wfsURL, featureTypeName) {

	// url of xml file
	var url = OpenLayers.ProxyHost + wfsURL;
	url += escape(WFS_DESCRIBE_FEATURE_TYPE_WFS + featureTypeName);

	// parse xml file
	var request = getXMLHttpRequest('text/xml');
	request.open('GET', url, false);
	request.send();
	var xmlDoc = request.responseXML;

	// for debugging
	determineWFSFeatureTypeNS_XML = xmlDoc;
		
	// determine namespace and return
	var atts = xmlDoc.childNodes[0].attributes;
	for ( var int = 0; int < atts.length; int++) {
		if (atts[int].nodeName == "targetNamespace") {
			return atts[int].nodeValue;
		}
	}
	
	// namespace not found
	alert("Namspace of feature type '" + featureTypeName + "' wasn't found.");
	return null;
}


/**
 * This method returns all processes of a WPS.
 * 
 * @param wpsURL
 *            URL of WPS.
 * 
 * @return Array of Processes with names, titles and abstracts.
 */
function determineWPSProcesses(wpsURL) {

	// url of xml file
	var url = OpenLayers.ProxyHost + wpsURL;
	url += escape(WPS_GET_CAPABILITIES);

	// parse xml file
	var request = getXMLHttpRequest('text/xml');
	request.open('GET', url, false);
	request.send();
	var xmlDoc = request.responseXML;
	

	// determine Processes
	var collectionOfProcesses = xmlDoc.getElementsByTagName("wps:Process");
	var arrayOfProcesses = new Array();
	if (collectionOfProcesses.length > 0)
		for ( var int = 0; int < collectionOfProcesses.length; int++) {
			
			// process (identifier, title, abstract)
			var process = new Array();
			
			// determine process properties
			var processProperties = collectionOfProcesses[int].childNodes;

			for ( var i = 0; i < processProperties.length; i++) {
				
				var prob = processProperties[i];
				// alert(prob.localName + " " + prob.textContent);
				if(prob.localName == "Identifier")
					process[0] = prob.textContent;
				else if(prob.localName == "Title")
					process[1] = prob.textContent;
				else if(prob.localName == "Abstract")
					process[2] = prob.textContent;
			}
			
			// notice process
			arrayOfProcesses[int] = process;
		}
	else
		 alert("Can't determine Processes of the WPS.");
	
	return arrayOfProcesses;
}


/**
 * This method returns an array of all input and output parameter of the
 * process.
 * 
 * @param wpsURL
 *            URL of WPS.
 * @param identifier
 *            Identifier of process.
 * 
 * @return Array with all input and output parameters. [0]: input parameter, [1]
 *         output parameter with identifier, title, abstract and format.
 */
function determineWPSProcessDescription(wpsURL, identifier){
	
	// url of xml file
	var url = OpenLayers.ProxyHost + wpsURL;
	url += escape(WPS_DESCRIBE_PROCESS + identifier);

	// parse xml file
	var request = getXMLHttpRequest('text/xml');
	request.open('GET', url, false);
	request.send();
	var xmlDoc = request.responseXML;

	// determine input parameter
	var collectionOfInputs = xmlDoc.getElementsByTagName("Input");
	var arrayOfInputs = new Array();
	if (collectionOfInputs.length > 0)
		for ( var k = 0; k < collectionOfInputs.length; k++) {
			
			// input (identifier, title, abstract)
			var input = new Array();
			
			// determine input properties
			var inputProperties = collectionOfInputs[k].childNodes;
			var j = 0;
			for ( var i = 0; i < inputProperties.length; i++) {
				var prob = inputProperties[i];
				if(prob.localName != null){
					
					// input formats
					if(prob.tagName == "ComplexData"){

						// TODO support only this format
						input[3] = WPSInputData.VECTOR_LAYER_SCHEMA_GML2;

					}else{
						if(prob.tagName == "LiteralData"){
							input[3] = prob.textContent.replace(/^\s*|\s*$/g,''); // remove
																				// whitespace
						}else{
							// identifier, title, abstract
							input[j++] = prob.textContent;
						}
					}
				}
			}
			
		
			
			// notice process
			arrayOfInputs[k] = input;
		}
	else
		 alert("Can't determine input parameter of the WPS.");
	
	// determine output parameter
	var collectionOfOutputs = xmlDoc.getElementsByTagName("Output");
	var arrayOfOutputs = new Array();
	if (collectionOfOutputs.length > 0)
		for ( var k = 0; k < collectionOfOutputs.length; k++) {
			
			// output (identifier, title, abstract)
			var output = new Array();
			
			// determine output properties
			var outputProperties = collectionOfOutputs[k].childNodes;
			var j = 0;
			for ( var i = 0; i < outputProperties.length; i++) {
				var prob = outputProperties[i];
				if(prob.localName != null){
					
					// output formats
					if(prob.tagName == "ComplexOutput"){

						// TODO support only this format
						output[3] = WPSInputData.VECTOR_LAYER_SCHEMA_GML2;

					}else{
						// identifier, title, abstract
						output[j++] = prob.textContent;
					}
					
				}
			}
			
	
			
			// notice process
			arrayOfOutputs[k] = output;
		}
	else
		 alert("Can't determine output parameter of the WPS.");

	// create array with input and output parameters
	var inputOutput = new Array();
	inputOutput[0] = arrayOfInputs;
	inputOutput[1] = arrayOfOutputs;
	
	// only for debuging
	arraysOfinputOutput = inputOutput;
	
	return inputOutput;
}


/**
 * This method removes a GML file form the map.
 * 
 * @param layerName
 *            Layer name.
 */
function removeLayerByName(layerName) {

	// get layer by name
	var layer = layers[layerName];

	// check whether layer is available
	if (layer != null) {
		map.removeLayer(layer[0]); // remove layer from map
		layers[layerName] = null; // remove layer from array
	}
}


/**
 * This method creates the XMLHttpRequests for the use of Ajax.
 * 
 * @param mimeType
 *            Mime type like 'text/xml' oder 'text/plain'.
 * 
 * @return XMLHttpRequest.
 */
function getXMLHttpRequest(mimeType) {
	var request = null;
	if (window.ActiveXObject) {
		// IE:
		try {
			request = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				request = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
			}
		}
	} else {
		// other browsers:
		request = new XMLHttpRequest();
		if (request.overrideMimeType) {
			request.overrideMimeType(mimeType);
		}
	}

	return request;
}

function loadProcessList(){
	
 // remove options
	
	for ( var opt in selectProcesses.options) {
		selectProcesses.remove(opt.index);
	}

 // determine and load WPS processes
 var processes = determineWPSProcesses(selectWPS.value);
 for ( var i = 0; i < processes.length; i++) {
	var option = document.createElement("option");
 	option.text = processes[i][1];
 	option.value = processes[i][0];
 	// select.appendChild( option );
 	 	
 	try
 	  {
 		selectProcesses.add(option,null); // standards compliant
 	  }
 	catch(ex)
 	  {
 		selectProcesses.add(option); // IE only
 	  }
 }
}

// function loadLayerList(){
//	
// // remove options
// for ( var opt in selectLayers.options) {
// selectLayers.remove(opt.index);
// }
//
// // determine and load WPS processes
// for ( var i in layers) {
//		
//	
// var option = document.createElement("option");
// option.text = i;
// option.value = layers[i][1];
//	 	 	
// try
// {
// selectLayers.add(option,null); // standards compliant
// }
// catch(ex)
// {
// selectLayers.add(option); // IE only
// }
// }
// }


/**
 * 
 * @param identifier
 * @param title
 * @param abstr
 * @param type
 * @return
 */
function createLiteralHTMLElement(identifier, title, abstr, type){
	
	
	var p = document.createElement("p");
	p.innerHTML = title + " (" + type + "): ";
	var input = document.createElement("input");
	input.type = "text";
	input.disabled='enabled'; 
	input.id = identifier;
	input.className = type;
	inputLiterals[identifier] = input;
	p.appendChild(input);
	
	
	return p;
}

/**
 * 
 * @param identifier
 * @param title
 * @param abstr
 * @param schema
 * @return
 */
function createVectorLayerHTMLElement(identifier, title, abstr, schema){
	
	var p = document.createElement("p");
	p.innerHTML = title + ": ";
	var select = document.createElement("select");
	select.id = identifier;
	select.className = schema;
	selectLayers[identifier] = select;
	p.appendChild(select);
	
	// determine and load WPS processes
	for ( var i in layers) {
	
	var option = document.createElement("option");
 	option.text = i;
 	option.value = layers[i][1];
 	 	
 	try
 	  {
 		select.add(option,null); // standards compliant
 	  }
 	catch(ex)
 	  {
 		select.add(option); // IE only
 	  }
	}
		
	return p;
}


/**
 * This method load all input parameter as HTML form of the selected process.
 */
function loadProcessInputForms(){
	
	// determine process description
    var processDesc = determineWPSProcessDescription(selectWPS.value, selectProcesses.value);
    
    // remove old input forms
    divInputValues.innerHTML="";
    
    // add input forms
    for ( var i = 0; i < processDesc[0].length; i++) {
		var inputParam = processDesc[0][i];		
		if(inputParam[3] == WPSInputData.VECTOR_LAYER_SCHEMA_GML2)
			divInputValues.appendChild(createVectorLayerHTMLElement(inputParam[0],inputParam[1],inputParam[2],inputParam[3]));
		else
			divInputValues.appendChild(createLiteralHTMLElement(inputParam[0],inputParam[1],inputParam[2],inputParam[3]));
	}
    
}

function createWPSInputData(){
	var data = new WPSInputData();
	
	for ( var i = 0; i < selectLayers.length; i++) {
		data.addVectorLayer(selectLayers[i].id, wfsFeatures, selectLayers[i].className);
	}
	
	return data;
}

/**
 * This class manage all input parameter for execute a process of a WPS.
 */
function WPSInputData() {
	this.firstInputValue = true;
	this.firstOutputValue = true;
	this.inputData = "DataInputs=";
	this.outputData = "RawDataOutput=";
}

// literal types
WPSInputData.LITERAL_BOOLEAN_TYPE = "boolean";
WPSInputData.LITERAL_SELECTION_TYPE = "integer";
WPSInputData.LITERAL_STRING_TYPE = "string";
WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE = "double";

// vector layer schemas
WPSInputData.VECTOR_LAYER_SCHEMA_GML2 = "http://schemas.opengis.net/gml/2.1.2/feature.xsd";

/**
 * This method adds a vector layer (gml feature collection).
 * 
 * @param identifier
 *            Identifier of this input data.
 * @param wfsFeatures
 *            URL of features of a WFS.
 * @param schema
 *            Schema to identify input format like VECTOR_LAYER_SCHEMA_GML2.
 */
WPSInputData.prototype.addVectorLayer = function(identifier, wfsFeatures, schema){
	
	// create vector data for WPS EXECUTION HTTP GET REQUEST
	var layer = "";
	if(!this.firstInputValue)
		layer += "%3B"; // ;
		
	
	layer += identifier;
	layer += "%3D%40"; // =@
	layer += "xlink"; 
	layer += "%3A"; // :
	layer += "href"; 
	layer += "%3D"; // =
	layer += escape(wfsFeatures); 
	layer += "%40mimeType%3Dtext/xml"; // @mimeType=text/xml
	layer += "%40encoding%3Dutf-8"; // @encoding=utf-8
	layer += "%40schema%3D"; // @schema=
	layer += escape(schema); 
	
	// add to total WPS EXECUTION HTTP GET REQUEST
	this.inputData += layer;
	
	this.firstInputValue = false;
};

/**
 * This method adds a literal (numerical_value, boolean, selection or string);
 * 
 * @param identifier
 *            Identifier of literal.
 * @param type
 *            LITERAL_BOOLEAN_TYPE, LITERAL_BOOLEAN_TYPE, LITERAL_STRING_TYPE or
 *            LITERAL_NUMERICAL_VALUE_TYPE.
 * @param value
 *            Value of literal.
 * @return
 */
WPSInputData.prototype.addLiteral = function(identifier, type, value){
	
	// create literal data for WPS EXECUTION HTTP GET REQUEST
	var literal = "";
	if(!this.firstInputValue)
		literal += "%3B"; // ;
	literal += identifier; // literal identifier
	literal += "%3D"; // =
	literal += value; // literal value
	literal += "%40datatype%3D"; // @datatype=
	literal += type; // literal datatype
	
	// add to total WPS EXECUTION HTTP GET REQUEST
	this.inputData += literal;
	
	this.firstInputValue = false;
};

/**
 * This method adds a vector layer output format.
 * 
 * @param identifier
 *            Identifier of ouput format.
 * @param schema
 *            Schema of output format like VECTOR_LAYER_SCHEMA_GML2.
 */
WPSInputData.prototype.addOutputVectorLayerFormat = function(identifier, schema){
	
	// create vector data for WPS EXECUTION HTTP GET REQUEST
	var format = "";
	if(!this.firstOutputValue){
		format += "%3B"; // ;
		this.firstOutputValue = false;
	}
	format += identifier;
	format += "%40mimeType%3Dtext/xml"; // @mimeType=text/xml
	format += "%40encoding%3Dutf-8"; // @encoding=utf-8
	format += "%40schema%3D"; // @schema=
	format += escape(schema); 
	
	// add to total WPS EXECUTION HTTP GET REQUEST
	this.outputData += format;
};

/**
 * This method returns the managed input parameter as query string to execute a
 * WPS over HTTP GET.
 */
WPSInputData.prototype.toString = function(){
	var data =  "";
	
	if(this.outputData != "DataInputs=")
		data += "&" + this.inputData;
	
	if(this.outputData != "RawDataOutput=")
		data += "&" + this.outputData;
	
	return data;
};


var WPSSwitcher = 
	  OpenLayers.Class(OpenLayers.Control, {

	    /**
		 * APIProperty: roundedCorner {Boolean} If true the Rico library is used
		 * for rounding the corners of the layer switcher div, defaults to true.
		 */
	    roundedCorner: true,

	    /**
		 * APIProperty: roundedCornerColor {String} The color of the rounded
		 * corners, only applies if roundedCorner is true, defaults to
		 * "darkblue".
		 */
	    roundedCornerColor: "darkblue",
	    
	    /**
		 * Property: layerStates {Array(Object)} Basically a copy of the "state"
		 * of the map's layers the last time the control was drawn. We have this
		 * in order to avoid unnecessarily redrawing the control.
		 */
	    layerStates: null,
	    

	  // DOM Elements
	  
	    /**
		 * Property: layersDiv {DOMElement}
		 */
	    layersDiv: null,
	    
// /**
// * Property: baseLayersDiv {DOMElement}
// */
// baseLayersDiv: null,

	    /**
		 * Property: baseLayers {Array(<OpenLayers.Layer>)}
		 */
	    baseLayers: null,
	    
	    
// /**
// * Property: dataLbl {DOMElement}
// */
// dataLbl: null,
	    
// /**
// * Property: dataLayersDiv {DOMElement}
// */
// dataLayersDiv: null,

	    /**
		 * Property: dataLayers {Array(<OpenLayers.Layer>)}
		 */
	    dataLayers: null,


	    /**
		 * Property: minimizeDiv {DOMElement}
		 */
	    minimizeDiv: null,

	    /**
		 * Property: maximizeDiv {DOMElement}
		 */
	    maximizeDiv: null,
	    
	    /**
		 * APIProperty: ascending {Boolean}
		 */
	    ascending: true,
	 
	    /**
		 * Constructor: OpenLayers.Control.LayerSwitcher
		 * 
		 * Parameters: options - {Object}
		 */
	    initialize: function(options) {
	        OpenLayers.Control.prototype.initialize.apply(this, arguments);
	        this.layerStates = [];
	    },

	    /**
		 * APIMethod: destroy
		 */    
	    destroy: function() {
	        
	        OpenLayers.Event.stopObservingElement(this.div);

	        OpenLayers.Event.stopObservingElement(this.minimizeDiv);
	        OpenLayers.Event.stopObservingElement(this.maximizeDiv);

	        // clear out layers info and unregister their events
	        this.clearLayersArray("base");
	        this.clearLayersArray("data");
	        
	        this.map.events.un({
	            "addlayer": this.redraw,
	            "changelayer": this.redraw,
	            "removelayer": this.redraw,
	            "changebaselayer": this.redraw,
	            scope: this
	        });
	        
	        OpenLayers.Control.prototype.destroy.apply(this, arguments);
	    },

	    /**
		 * Method: setMap
		 * 
		 * Properties: map - {<OpenLayers.Map>}
		 */
	    setMap: function(map) {
	        OpenLayers.Control.prototype.setMap.apply(this, arguments);

	        this.map.events.on({
	            "addlayer": this.redraw,
	            "changelayer": this.redraw,
	            "removelayer": this.redraw,
	            "changebaselayer": this.redraw,
	            scope: this
	        });
	    },

	    /**
		 * Method: draw
		 * 
		 * Returns: {DOMElement} A reference to the DIV DOMElement containing
		 * the switcher tabs.
		 */  
	    draw: function() {
	        OpenLayers.Control.prototype.draw.apply(this);

	        // create layout divs
	        this.loadContents();

	        // set mode to minimize
	        if(!this.outsideViewport) {
	            this.minimizeControl();
	        }

	        // populate div with current info
	        this.redraw();    

	        return this.div;
	    },

	    /**
		 * Method: clearLayersArray User specifies either "base" or "data". we
		 * then clear all the corresponding listeners, the div, and reinitialize
		 * a new array.
		 * 
		 * Parameters: layersType - {String}
		 */
	    clearLayersArray: function(layersType) {
	        var layers = this[layersType + "Layers"];
	        if (layers) {
	            for(var i=0, len=layers.length; i<len ; i++) {
	                var layer = layers[i];
	                OpenLayers.Event.stopObservingElement(layer.inputElem);
	                OpenLayers.Event.stopObservingElement(layer.labelSpan);
	            }
	        }
	        this[layersType + "LayersDiv"].innerHTML = "";
	        this[layersType + "Layers"] = [];
	    },


	    /**
		 * Method: checkRedraw Checks if the layer state has changed since the
		 * last redraw() call.
		 * 
		 * Returns: {Boolean} The layer state changed since the last redraw()
		 * call.
		 */
	    checkRedraw: function() {
	        var redraw = false;
	        if ( !this.layerStates.length ||
	             (this.map.layers.length != this.layerStates.length) ) {
	            redraw = true;
	        } else {
	            for (var i=0, len=this.layerStates.length; i<len; i++) {
	                var layerState = this.layerStates[i];
	                var layer = this.map.layers[i];
	                if ( (layerState.name != layer.name) || 
	                     (layerState.inRange != layer.inRange) || 
	                     (layerState.id != layer.id) || 
	                     (layerState.visibility != layer.visibility) ) {
	                    redraw = true;
	                    break;
	                }    
	            }
	        }    
	        return redraw;
	    },
	    
	    /**
		 * Method: redraw Goes through and takes the current state of the Map
		 * and rebuilds the control to display that state. Groups base layers
		 * into a radio-button group and lists each data layer with a checkbox.
		 * 
		 * Returns: {DOMElement} A reference to the DIV DOMElement containing
		 * the control
		 */  
	    redraw: function() {
	        // if the state hasn't changed since last redraw, no need
	        // to do anything. Just return the existing div.
	        if (!this.checkRedraw()) { 
	            return this.div; 
	        } 

// // clear out previous layers
// this.clearLayersArray("base");
// this.clearLayersArray("data");
//	        
// var containsOverlays = false;
// var containsBaseLayers = false;
//	        
// // Save state -- for checking layer if the map state changed.
// // We save this before redrawing, because in the process of
// // redrawing
// // we will trigger more visibility changes, and we want to not
// // redraw
// // and enter an infinite loop.
// var len = this.map.layers.length;
// this.layerStates = new Array(len);
// for (var i=0; i <len; i++) {
// var layer = this.map.layers[i];
// this.layerStates[i] = {
// 'name': layer.name,
// 'visibility': layer.visibility,
// 'inRange': layer.inRange,
// 'id': layer.id
// };
// }
//
// var layers = this.map.layers.slice();
// if (!this.ascending) { layers.reverse(); }
// for(var i=0, len=layers.length; i<len; i++) {
// var layer = layers[i];
// var baseLayer = layer.isBaseLayer;
//
// if (layer.displayInLayerSwitcher) {
//
// if (baseLayer) {
// containsBaseLayers = true;
// } else {
// containsOverlays = true;
// }
//
// // only check a baselayer if it is *the* baselayer, check
// // data
// // layers if they are visible
// var checked = (baseLayer) ? (layer == this.map.baseLayer)
// : layer.getVisibility();
//	    
// // create input element
// var inputElem = document.createElement("input");
// inputElem.id = this.id + "_input_" + layer.name;
// inputElem.name = (baseLayer) ? this.id + "_baseLayers" : layer.name;
// inputElem.type = (baseLayer) ? "radio" : "checkbox";
// inputElem.value = layer.name;
// inputElem.checked = checked;
// inputElem.defaultChecked = checked;
//
// if (!baseLayer && !layer.inRange) {
// inputElem.disabled = true;
// }
// var context = {
// 'inputElem': inputElem,
// 'layer': layer,
// 'layerSwitcher': this
// };
// OpenLayers.Event.observe(inputElem, "mouseup",
// OpenLayers.Function.bindAsEventListener(this.onInputClick,
// context)
// );
//	                
// // create span
// var labelSpan = document.createElement("span");
// OpenLayers.Element.addClass(labelSpan, "labelSpan");
// if (!baseLayer && !layer.inRange) {
// labelSpan.style.color = "gray";
// }
// labelSpan.innerHTML = layer.name;
// labelSpan.style.verticalAlign = (baseLayer) ? "bottom"
// : "baseline";
// OpenLayers.Event.observe(labelSpan, "click",
// OpenLayers.Function.bindAsEventListener(this.onInputClick,
// context)
// );
// // create line break
// var br = document.createElement("br");
//	    
//	                
// var groupArray = (baseLayer) ? this.baseLayers
// : this.dataLayers;
// groupArray.push({
// 'layer': layer,
// 'inputElem': inputElem,
// 'labelSpan': labelSpan
// });
//	                                                     
//	    
// var groupDiv = this.baseLayersDiv
// //this.dataLayersDiv;
// groupDiv.appendChild(inputElem);
// groupDiv.appendChild(labelSpan);
// groupDiv.appendChild(br);
// }
// }

	        // if no overlays, dont display the overlay label
	        // this.dataLbl.style.display = (containsOverlays) ? "" : "none";
	        
	        // if no baselayers, dont display the baselayer label
	        // this.baseLbl.style.display = (containsBaseLayers) ? "" : "none";

	        return this.div;
	    },

	    /**
		 * Method: A label has been clicked, check or uncheck its corresponding
		 * input
		 * 
		 * Parameters: e - {Event}
		 * 
		 * Context: - {DOMElement} inputElem - {<OpenLayers.Control.LayerSwitcher>}
		 * layerSwitcher - {<OpenLayers.Layer>} layer
		 */

	    onInputClick: function(e) {

	        if (!this.inputElem.disabled) {
	            if (this.inputElem.type == "radio") {
	                this.inputElem.checked = true;
	                this.layer.map.setBaseLayer(this.layer);
	            } else {
	                this.inputElem.checked = !this.inputElem.checked;
	                this.layerSwitcher.updateMap();
	            }
	        }
	        OpenLayers.Event.stop(e);
	    },
	    
	    /**
		 * Method: onLayerClick Need to update the map accordingly whenever user
		 * clicks in either of the layers.
		 * 
		 * Parameters: e - {Event}
		 */
	    onLayerClick: function(e) {
	        this.updateMap();
	    },


	    /**
		 * Method: updateMap Cycles through the loaded data and base layer input
		 * arrays and makes the necessary calls to the Map object such that that
		 * the map's visual state corresponds to what the user has selected in
		 * the control.
		 */
	    updateMap: function() {

	        // set the newly selected base layer
	        for(var i=0, len=this.baseLayers.length; i<len; i++) {
	            var layerEntry = this.baseLayers[i];
	            if (layerEntry.inputElem.checked) {
	                this.map.setBaseLayer(layerEntry.layer, false);
	            }
	        }

	        // set the correct visibilities for the overlays
	        for(var i=0, len=this.dataLayers.length; i<len; i++) {
	            var layerEntry = this.dataLayers[i];   
	            layerEntry.layer.setVisibility(layerEntry.inputElem.checked);
	        }

	    },

	    /**
		 * Method: maximizeControl Set up the labels and divs for the control
		 * 
		 * Parameters: e - {Event}
		 */
	    maximizeControl: function(e) {

	        // set the div's width and height to empty values, so
	        // the div dimensions can be controlled by CSS
	        this.div.style.width = "";
	        this.div.style.height = "";

	        this.showControls(false);

	        if (e != null) {
	            OpenLayers.Event.stop(e);                                            
	        }
	    },
	    
	    /**
		 * Method: minimizeControl Hide all the contents of the control, shrink
		 * the size, add the maximize icon
		 * 
		 * Parameters: e - {Event}
		 */
	    minimizeControl: function(e) {

	        // to minimize the control we set its div's width
	        // and height to 0px, we cannot just set "display"
	        // to "none" because it would hide the maximize
	        // div
	        this.div.style.width = "0px";
	        this.div.style.height = "0px";

	        this.showControls(true);

	        if (e != null) {
	            OpenLayers.Event.stop(e);                                            
	        }
	    },

	    /**
		 * Method: showControls Hide/Show all LayerSwitcher controls depending
		 * on whether we are minimized or not
		 * 
		 * Parameters: minimize - {Boolean}
		 */
	    showControls: function(minimize) {

	        this.maximizeDiv.style.display = minimize ? "" : "none";
	        this.minimizeDiv.style.display = minimize ? "none" : "";

	        this.layersDiv.style.display = minimize ? "none" : "";
	    },
	    
	    /**
		 * Method: loadContents Set up the labels and divs for the control
		 */
	    loadContents: function() {

	        // configure main div
	        OpenLayers.Event.observe(this.div, "mouseup", 
	            OpenLayers.Function.bindAsEventListener(this.mouseUp, this));
	        OpenLayers.Event.observe(this.div, "click",
	                      this.ignoreEvent);
	        OpenLayers.Event.observe(this.div, "mousedown",
	            OpenLayers.Function.bindAsEventListener(this.mouseDown, this));
	        OpenLayers.Event.observe(this.div, "dblclick", this.ignoreEvent);
        
	        // style of main div
	        with(this.div.style) {
                position = "absolute";
                top = "0px";
                // right = "";
                left = "100px";
                fontFamily = "sans-serif";
                fontWeight = "bold";
                fontSize = "smaller";
                marginTop = "3px";
                marginLeft = "3px";
                color = "white";
                backgroundColor = "darkblue";
	        }

	        
	        
	        // layers list div
	        this.layersDiv = document.createElement("div");
	        this.layersDiv.id = this.id + "_layersDiv";
	        OpenLayers.Element.addClass(this.layersDiv, "layersDiv");
	        
	        var content = "Web Processing Service<br>";
	        content += "-----------------------------------------<br>";
        
	        
	        // form
	        var form = document.createElement("form");
	        
	        // create list of wps urls
	        var p1 = document.createElement("p");
	        p1.innerHTML = "WPS: ";
	        form.appendChild(p1);

	        var select1 = document.createElement("select");
	        select1.name = "wpslist";
	        select1.size = "1";
	        p1.appendChild(select1);
	        selectWPS = select1;
	        
	        select1.onchange = function(){
	        	loadProcessList();
	        	loadProcessInputForms();
	        };
	        
	        var selectedWPSURL = null;
	        for ( var url in wpsURLs) {
	        	if(url != null){
	        		var option = document.createElement("option");
	        		option.text  = url;
	        		option.value = url;
	        		select1.appendChild(option);
	        		if(selectedWPSURL == null)
	        			selectedWPSURL = url;
	        	}
			} 
	        
	        // create process list
	        var p2 = document.createElement("p");
	        p2.innerHTML = "Process: ";
	        form.appendChild(p2);

	        var select2 = document.createElement("select");
	        select2.name = "processlist";
	        select2.size = "1";
	        p2.appendChild(select2);
	        select2.onchange = function(){
	        	loadProcessInputForms();
	        };
	        selectProcesses = select2;
	        loadProcessList(selectedWPSURL);
	                
	        // create layer list
// var p3 = document.createElement("p");
// form.appendChild(p3);
//
// var select3 = document.createElement("select");
// select3.name = "layerlist";
// select3.size = "1";
// p3.appendChild(select3);
// selectLayers = select3;
// loadLayerList();
	        this.layersDiv.appendChild(form);
	        this.div.appendChild(this.layersDiv);
  
	        // create input forms
	        var p5 = document.createElement("p");
	        p5.innerHTML = "<hr>Input Parameter:";
	        form.appendChild(p5);
	        var inputDiv = document.createElement("div");
	        form.appendChild(inputDiv);
	        divInputValues = inputDiv;        
	        loadProcessInputForms();

       
	        // create execute button
	        var p4 = document.createElement("p");
	        p4.innerHTML = "<hr>";
	        form.appendChild(p4);
	       
	        var input = document.createElement("input");
	        input.type = "button";
	        input.value = "Execute";
	        input.onclick = function(){
	        	
	        	var data = createWPSInputData();
	        
	        	
// data.addVectorLayer("LAYER", selectLayers.value,
// WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
// data.addOutputVectorLayerFormat("RESULT",
// WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
//	        	
// addWPSLayer("WPS name", selectWPS.value, selectProcesses.value, data );
	        };
	        p4.appendChild(input);
	        
	        
	        
	        
	        	        
	
	        
	        

	        if(this.roundedCorner) {
	            OpenLayers.Rico.Corner.round(this.div, {
	                corners: "tl bl",
	                bgColor: "darkblue",
	                color: this.roundedCornerColor,
	                blend: false
	            });
	            OpenLayers.Rico.Corner.changeOpacity(this.layersDiv, 0.75);
	        }

	        var imgLocation = OpenLayers.Util.getImagesLocation();
	        var sz = new OpenLayers.Size(18,18);        

	        // maximize button div
	        var img = imgLocation + 'layer-switcher-maximize.png';
	        this.maximizeDiv = OpenLayers.Util.createAlphaImageDiv(
	                                    "OpenLayers_Control_MaximizeDiv", 
	                                    new OpenLayers.Pixel(180,0), 
	                                    sz, 
	                                    img, 
	                                    "absolute");
	        OpenLayers.Element.addClass(this.maximizeDiv, "maximizeDiv");
	        this.maximizeDiv.style.display = "none";
	        OpenLayers.Event.observe(this.maximizeDiv, "click", 
	            OpenLayers.Function.bindAsEventListener(this.maximizeControl, this)
	        );
	        
	        this.div.appendChild(this.maximizeDiv);

	        // minimize button div
	        var img = imgLocation + 'layer-switcher-minimize.png';
	        var sz = new OpenLayers.Size(18,18);        
	        this.minimizeDiv = OpenLayers.Util.createAlphaImageDiv(
	                                    "OpenLayers_Control_MinimizeDiv", 
	                                    new OpenLayers.Pixel(180,0), 
	                                    sz, 
	                                    img, 
	                                    "absolute");
	        OpenLayers.Element.addClass(this.minimizeDiv, "minimizeDiv");
	        this.minimizeDiv.style.display = "none";
	        OpenLayers.Event.observe(this.minimizeDiv, "click", 
	            OpenLayers.Function.bindAsEventListener(this.minimizeControl, this)
	        );

	        this.div.appendChild(this.minimizeDiv);
	    },
	    
	    /**
		 * Method: ignoreEvent
		 * 
		 * Parameters: evt - {Event}
		 */
	    ignoreEvent: function(evt) {
	        OpenLayers.Event.stop(evt);
	    },

	    /**
		 * Method: mouseDown Register a local 'mouseDown' flag so that we'll
		 * know whether or not to ignore a mouseUp event
		 * 
		 * Parameters: evt - {Event}
		 */
	    mouseDown: function(evt) {
	        this.isMouseDown = true;
	        this.ignoreEvent(evt);
	    },

	    /**
		 * Method: mouseUp If the 'isMouseDown' flag has been set, that means
		 * that the drag was started from within the LayerSwitcher control, and
		 * thus we can ignore the mouseup. Otherwise, let the Event continue.
		 * 
		 * Parameters: evt - {Event}
		 */
	    mouseUp: function(evt) {
	        if (this.isMouseDown) {
	            this.isMouseDown = false;
	            this.ignoreEvent(evt);
	        }
	    },

	    CLASS_NAME: "WPSSwitcher"
	});


