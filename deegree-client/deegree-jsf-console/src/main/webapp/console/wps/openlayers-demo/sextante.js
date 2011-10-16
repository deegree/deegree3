// WFS-Operations
var WFS_GET_CAPABILITIES = "?SERVICE=WFS&REQUEST=GetCapabilities&VERSION=1.0.0";
var WFS_DESCRIBE_FEATURE_TYPE_WFS = "?SERVICE=WFS&REQUEST=DescribeFeatureType&VERSION=1.0.0&TYPENAME=";
var WFS_GET_FEATURE = "?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&OUTPUTFORMAT=GML2&SRSNAME=EPSG:4326&TYPENAME=";

// WPS-Operations
var WPS_GET_CAPABILITIES = "?SERVICE=WPS&REQUEST=GetCapabilities";
var WPS_DESCRIBE_PROCESS = "?SERVICE=WPS&REQUEST=DescribeProcess&VERSION=1.0.0&IDENTIFIER=";
var WPS_EXECUTE = "?SERVICE=WPS&REQUEST=Execute&VERSION=1.0.0&IDENTIFIER=";

// WFS URLs for init
var wfsURLs = new Array();

// WPS URLs for init
var wpsURLs = new Array();

// OpenLayers map element
var map;

// proxy for external data sources
OpenLayers.ProxyHost = "proxy.jsp?url=";

// Array of layers {layer, sourceURL}
var layers = new Array();

// HTML elements
var selectWPSProcesses; // select element with WPS processes
var selectWPS; // select element with WPS URLs
var selectWFS; // select element with WFS URLs
var selectWFSLayers; // select element with WFS layers
var divInputValues; // div element with input parameter of a process
var selectLayers = new Array(); // select elements of input layers
var inputLiterals = new Array(); // input elements with input literals
var inputOutputLayers = new Array(); // hidden input elements with output layer formats
var resultDisplayWPS = document.createTextNode(""); // textnode for infos and errors of WPS
var resultDisplayWFS = document.createTextNode(""); // textnode for infos and errors of WFS
var aProcessDesc = document.createElement("a");
aProcessDesc.innerHTML = "(i)";
aProcessDesc.style.color = "white";
aProcessDesc.href = "#";
var wfsClipByView = true;

// increase reload attempts
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

// ------------------------------------------------------------------------------------------------------
// INFO: Transform features
// var pTrans = new OpenLayers.Geometry.Point(0, 52).transform(new OpenLayers.Projection("EPSG:900913"),new OpenLayers.Projection("EPSG:4326"));

/**
 * This method initialize the map-div with OpenLayers.
 */
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
  //
  // var wms2 = new OpenLayers.Layer.WMS("OpenLayers STATE LABEL",
  // "http://labs.metacarta.com/wms/vmap0", {
  // layers : 'statelabel',
  // format : "image/png",
  // transparent : "true",
  // crs : "EPSG:4326"
  // }, {
  // isBaseLayer : false,
  // visibility : false
  // });
  // map.addLayer(wms2);

  // ------------------------------------------------------------------------------------------------------
  // add a marker
  //
  // var vectorLayer = new OpenLayers.Layer.Vector("TEST Maker");
  // var feature = new OpenLayers.Feature.Vector(
  // new OpenLayers.Geometry.Point(-0.0014, -0.0024),
  // {
  // some : 'data'
  // },
  // {
  // externalGraphic :
  // 'http://funmap.co.uk/cloudmade-examples/markers/marker.png',
  // graphicHeight : 37,
  // graphicWidth : 24
  // });
  // vectorLayer.addFeatures(feature);
  // map.addLayer(vectorLayer);

  // ------------------------------------------------------------------------------------------------------
  // add gml files as layer (only gml2 without schema)
  //
  // addGMLLayer("GML2 Polygon", "gml/GML2_FeatureCollection_Polygon.xml", "");
  // addGMLLayer("GML2 Buffered Polygon", "http://127.0.0.1:8080/deegree-utah-demo-3.0-SNAPSHOT/services?service=WFS&version=1.0.0&request=GetFeature&typeName=app:SGID024_StateBoundary", "http://127.0.0.1:8080/deegree-utah-demo-3.0-SNAPSHOT/services?service=WFS&version=1.0.0&request=GetFeature&typeName=app:SGID024_StateBoundary");

  // http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states

  // addGMLLayer("GML2 Buffered Polygon", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states", "");

  // ------------------------------------------------------------------------------------------------------
  // add web feature service (only gml2)
  addWFSURL("http://deegree3-testing.deegree.org/deegree-utah-demo/services");
  addWFSURL( "http://giv-wps.uni-muenster.de:8080/geoserver/wfs");
  addWFSURL(  "http://www.dge.upd.edu.ph/geoserver/wfs");


  // http://127.0.0.1:8080/deegree-utah-demo-3.0-SNAPSHOT/services?service=WFS&version=1.0.0&request=GetFeature&typeName=app:SGID024_StateBoundary
  // http://127.0.0.1:8080/deegree-utah-demo-3.0-SNAPSHOT/services?service=WFS&version=1.0.0&request=GetCapabilities

  addWFSLayer('WFS topp:states', "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","topp:states");
  // addWFSLayer("WFS ns1:tasmania_roads", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","ns1:tasmania_roads", false);
  // addWFSLayer("WFS tiger:poi", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","tiger:poi", false);
  // addWFSLayer("WFS tiger:tiger_roads", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","tiger:tiger_roads", false);
  // addWFSLayer("WFS tiger:poly_landmarks", "http://giv-wps.uni-muenster.de:8080/geoserver/wfs","tiger:poly_landmarks", false);
  //
// // create wfs layer
// var wfs = new OpenLayers.Layer.Vector("WFS name", {
// strategies : [ new OpenLayers.Strategy.BBOX() ],
// protocol : new OpenLayers.Protocol.WFS( {
// url : "http://127.0.0.1:8080/deegree-utah-demo-3.0-SNAPSHOT/services",
// featureType : "SGID024_StateBoundary",
// featureNS : "http://www.deegree.org/app"
// })
// });
//
// // add wfs layer to map
// map.addLayer(wfs);



  //
  // ------------------------------------------------------------------------------------------------------
  // add WPS URL
  addWPSURL( "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services");
  addWPSURL( "http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService");

  //
  // ------------------------------------------------------------------------------------------------------
  // add WPS Layer
  //
  // // centroid algorithm
  // var data = new WPSInputData();
  // var wfsFeatureURL = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states";
  // data.addVectorLayer("LAYER", wfsFeatureURL,WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
  // data.addOutputVectorLayerFormat("RESULT",
  // WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
  // addWPSLayer("WPS centroids", "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services", "st_centroids", data);
  //
  // // transform algorithm
  // var data2 = new WPSInputData();
  // var wfsFeatureURL2 =
  // "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=topp:states";
  // data2.addVectorLayer("LAYER", wfsFeatureURL2, WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
  // data2.addLiteral("DISTANCEX", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "-20");
  // data2.addLiteral("DISTANCEY", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "40");
  // data2.addLiteral("ANGLE", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "90");
  // data2.addLiteral("SCALEX", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "0.5");
  // data2.addLiteral("SCALEY", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "0.5");
  // data2.addLiteral("ANCHORX", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "-46");
  // data2.addLiteral("ANCHORY", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "35");
  // data2.addOutputVectorLayerFormat("RESULT",
  // WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
  // addWPSLayer("WPS transform", "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services", "st_transform", data2);
  //
  // // fixeddistancebuffer algorithm
  // var data3 = new WPSInputData();
  // var wfsFeatureURL3 = "http://giv-wps.uni-muenster.de:8080/geoserver/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.0.0&OUTPUTFORMAT=GML2&TYPENAME=ns1:tasmania_roads";
  // data3.addVectorLayer("LAYER", wfsFeatureURL3, WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
  // data3.addLiteral("DISTANCE", WPSInputData.LITERAL_NUMERICAL_VALUE_TYPE, "0.05");
  // data3.addLiteral("TYPES", WPSInputData.LITERAL_SELECTION_TYPE, "0");
  // data3.addLiteral("RINGS", WPSInputData.LITERAL_SELECTION_TYPE, "0");
  // data3.addLiteral("NOTROUNDED", WPSInputData.LITERAL_BOOLEAN_TYPE, "false");
  // data3.addOutputVectorLayerFormat("RESULT", WPSInputData.VECTOR_LAYER_SCHEMA_GML2);
  // addWPSLayer("WPS fixeddistancebuffer", "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services", "st_fixeddistancebuffer", data3);

  // ------------------------------------------------------------------------------------------------------
  // add controls
  map.addControl(new WFSSwitcher());
  map.addControl(new WPSSwitcher());
  map.addControl(new OpenLayers.Control.LayerSwitcher());
  map.addControl(new OpenLayers.Control.MousePosition());

  // ------------------------------------------------------------------------------------------------------
  // set center
  map.setCenter(new OpenLayers.LonLat(-30, 25), 3);
}



/**
 * This Method adds a GML file to the map.
 *
 * @param gmlName
 *            Layer name.
 * @param gmlURL
 *            URL of GML file (local or external).
 * @param attr
 *            External URL of GML file.
 *
 * @return OpenLayers.Vector
 */
function addGMLLayer(gmlName, gmlURL, attr) {

  // check file size error
  var request = getXMLHttpRequest('text/xml');
  request.open('GET', gmlURL, false);
  request.send();
  var xmlDoc = request.responseXML;
  var msg = xmlDoc.documentElement.localName;
  //alert(msg);
  var errorMsg = "Parse error / File size of input data is to large? (max. 2 MB)";
  if(msg == "parsererror"){
    resultDisplayWPS.data = errorMsg;
    resultDisplayWFS.data = errorMsg;
    return null;
  }


  // Layer name
  var gmlNameArray = determineIndividualLayerName(gmlName);

  // create a GML layer
  var layer = new OpenLayers.Layer.GML(gmlNameArray[1], gmlURL, { styleMap: getStyleMap() });

//   var layerProtocol = new OpenLayers.Protocol.HTTP( {
//     url : gmlURL,
//    format : new OpenLayers.Format.GML()
//   });
//
//   var layer = new OpenLayers.Layer.Vector(name, {
//     strategies : [ new OpenLayers.Strategy.BBOX() ],
//     protocol : layerProtocol,
//     styleMap: getStyleMap()
//   });

  // notice layer
  var gmlArray = new Array();
  gmlArray[0] = layer;
  gmlArray[1] = attr;
  layers[gmlNameArray[0]] = gmlArray;


//  var name = "states.1";
//  for (name in layer.features) {
//      alert(name + ": " + layer.features[name]);
//  }
//  alert(layer.getFeatureByFid("states.1"));


  // add layer to map
  map.addLayer(layer);



  return layer;
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
 * @return OpenLayers.Layer
 */
function addWFSLayer(wfsName, wfsURL, wfsFeatureTypeWithPrefix) {

  // INFO message
  resultDisplayWFS.data = "Loading...";

// // Layer name
// var wfsNameArray = determineIndividualLayerName(wfsName);
//
// // determine namespace
// var ns = determineWFSFeatureTypeNamespace(wfsURL, wfsFeatureTypeWithPrefix);
//
// // check namespace
// if(ns == null)
// return;
//
// // check name
// var ftArray = wfsFeatureTypeWithPrefix.split(":");
// if(ftArray.length != 2){
// var name = new Array();
// name[0] = "";
// name[1] = wfsFeatureTypeWithPrefix;
// ftArray = name;
// }
//
// // create wfs layer
// var wfs = new OpenLayers.Layer.Vector(wfsNameArray[1], {
// strategies : [ new OpenLayers.Strategy.BBOX() ],
// protocol : new OpenLayers.Protocol.WFS( {
// url : wfsURL,
// featureType : ftArray[1],
// featureNS : ns
// }), visibility : visible, styleMap: getStyleMap()
// });


  //determine view box
  var bbox = "";
  if(wfsClipByView){
    var viewPort = map.getExtent();
    if(viewPort != null){
      bbox += "BBOX=" + viewPort.left + "," + viewPort.bottom + ","+ viewPort.right + ","+ viewPort.top + ",EPSG:4326";
      //BBOX=<minX>, <minY>,<maxX>, <maxY>
    }
  }


  var sourceURL = wfsURL + WFS_GET_FEATURE + wfsFeatureTypeWithPrefix  + "&" + bbox;
  var name = "WFS " + wfsFeatureTypeWithPrefix;

  var layer = addGMLLayer(name, OpenLayers.ProxyHost + escape(sourceURL), sourceURL);

  if(layer != null)
    resultDisplayWFS.data = "Added layer '" + name + "'";

  return layer;

// // add wfs layer to map
// var wfsArray = new Array();
// wfsArray[0] = wfs;
// wfsArray[1] = wfsURL + WFS_GET_FEATURE + wfsFeatureTypeWithPrefix;
// layers[wfsNameArray[0]] = wfsArray;
// map.addLayer(wfs);
//
// resultDisplayWFS.data = "Added layer '" + wfsNameArray[0] + "'";
//
// return wfs;
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
    addWFSLayer(wfsURL, wfsName +" " + types[i], types[i]);
  }
}


/**
 * This method adds a WFS URL.
 *
 * @param url
 *            WFS URL.
 */
function addWFSURL(url){
  wfsURLs[url] = url;
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

  // INFO message
  resultDisplayWPS.data = "Processing...";


  var attr = wpsURL + WPS_EXECUTE + wpsProcess + data.toString(); // external URL of executed GML file
  var gmlURL = OpenLayers.ProxyHost + escape(attr); // external URL of executed GML file with proxy


  // check gml file for errors
  var request = getXMLHttpRequest('text/xml');
  request.open('GET', gmlURL, false);
  request.send();
  var xmlDoc = request.responseXML;
  checkParseError(xmlDoc);

  if(xmlDoc.childNodes[0].localName == "ExceptionReport"){
    var errorMsg = xmlDoc.childNodes[0].textContent.replace(/^\s*|\s*$/g,'');
    if(errorMsg == "")
      resultDisplayWPS.data = "Execution failed!";
    else
      resultDisplayWPS.data = errorMsg;
    return null;
  }else{
    var layer = addGMLLayer(wpsName, gmlURL, attr);
    if(layer != null)
      resultDisplayWPS.data = "Added layer '" + wpsName + "'";
    return layer;
  }

}

/**
 * This method adds a new WPS URL.
 *
 * @param url
 *            WPS URL.
 */
function addWPSURL(url){
  wpsURLs[url] = url;
}

/**
 * This method determines an individual name of layer and adds a link to remove this layer.
 *
 * @param name
 *            Raw layer name.
 *
 * @return Modified layer name [0] = name, [1] = name with link to remove.
 */
function determineIndividualLayerName(name){
  var newName = name;

  // check whether name available
  if (layers[name] != null)
    newName = determineIndividualLayerName(name + "_1")[0];

  var display = newName + ' <a href="#" onclick="removeLayerByName(' + "'" + newName + "'" + ');" style="color:white;" title="Remove">(x)</a>';

  var names = new Array();
  names[0] = newName;
  names[1] = display;

  return names;
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
  var url = OpenLayers.ProxyHost;
  // url + = wfsURL;
  url += escape(wfsURL + WFS_GET_CAPABILITIES);


  // alert(url);

  // parse xml file
  var request = getXMLHttpRequest('text/xml');
  request.open('GET', url, false);
  request.send();
  var xmlDoc = request.responseXML;
  checkParseError(xmlDoc);

  // determine FeatureTypes
  var collectionOfFeatureTypes = xmlDoc.getElementsByTagName("FeatureType");
  if (collectionOfFeatureTypes.length == 0)
    collectionOfFeatureTypes = xmlDoc.getElementsByTagName("wfs:FeatureType");

  // search names
  var arrayOfFeatureTypes = new Array();
  if (collectionOfFeatureTypes.length > 0)
    for ( var i = 0; i < collectionOfFeatureTypes.length; i++) {

      var childs = collectionOfFeatureTypes[i].childNodes;
      for ( var j = 0; j < childs.length; j++) {
        if(childs[j].localName == "Name" || childs[j].localName == "name" ){
          arrayOfFeatureTypes[i] = childs[j].firstChild.nodeValue;
          break;
        }
      }

    }
  else
    alert("Can't determine feature type names of the WFS.");

  // for debugging
  // determineWFSFeatureTypeNames_XML = xmlDoc;
  // determineWFSFeatureTypeNames_ARRAY = arrayOfFeatureTypes;

  return arrayOfFeatureTypes;
}

function checkParseError(xmlDoc){
  var msg = xmlDoc.documentElement.localName;
  if(msg == "parsererror")
    alert("JavaScript can not parse this file.");
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
  checkParseError(xmlDoc);
  // for debugging
  // determineWFSFeatureTypeNS_XML = xmlDoc;

  // determine namespace and return
  var atts = xmlDoc.childNodes[0].attributes;
  for ( var i = 0; i < atts.length; i++) {
    if (atts[i].nodeName == "targetNamespace") {
      return atts[i].nodeValue;
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
  checkParseError(xmlDoc);

  // determine Processes
  var collectionOfProcesses = xmlDoc.getElementsByTagName("wps:Process");
  var arrayOfProcesses = new Array();
  if (collectionOfProcesses.length > 0)
    for ( var k = 0; k < collectionOfProcesses.length; k++) {

      // process (identifier, title, abstract)
      var process = new Array();

      // determine process properties
      var processProperties = collectionOfProcesses[k].childNodes;
      for ( var i = 0; i < processProperties.length; i++) {
        var prob = processProperties[i];
        if(prob.localName == "Identifier")
          process[0] = prob.textContent;
        else if(prob.localName == "Title")
          process[1] = prob.textContent;
        else if(prob.localName == "Abstract")
          process[2] = prob.textContent;
      }

      // notice process
      arrayOfProcesses[k] = process;
    }
  else
     alert("Can't determine Processes of the WPS.");


  // sort processes
  arrayOfProcesses.sort(function sortByName(a, b) {
        var x = a[1].toLowerCase();
        var y = b[1].toLowerCase();
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    }
  );

  return arrayOfProcesses;
}


/**
 * This method returns an array of all input and output parameter of the process.
 *
 * @param wpsURL
 *            URL of WPS.
 * @param identifier
 *            Identifier of process.
 *
 * @return Array with all input and output parameters. [0]: input parameter, [1] output parameter with identifier, title, abstract and format.
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
  checkParseError(xmlDoc);

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
              input[3] = prob.textContent.replace(/^\s*|\s*$/g,''); // remove whitespace
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
  // arraysOfinputOutput = inputOutput;

  return inputOutput;
}


/**
 * This method creates a styleMap for vector data of map.
 *
 * @return OpenLayers.StyleMap.
 */
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
 * This method removes a WFS URL.
 *
 * @param url
 *            WFS URL.
 */
function removeWFSURL(url){
  wfsURLs[url] = null;
}

/**
 * This method removes a WPS URL:
 *
 * @param url
 *            WPS URL.
 */
function removeWPSURL(url){
  wpsURLs[url] = null;
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
  loadProcessInputForms();
}

/**
 * This method loads the list of processes of the selected WPS to the select element "selectWPSProcesses".
 */
function loadWPSProcessList(){

  // remove options
  for ( var opt in selectWPSProcesses.options) {
    selectWPSProcesses.remove(opt.index);
  }

  // determine and load WPS processes
  var processes = determineWPSProcesses(selectWPS.value);

  // add processes
  for ( var i = 0; i < processes.length; i++) {
    var option = document.createElement("option");

    // modify SEXTANTE process names
    var text = processes[i][1];
    var value = processes[i][0];
    if(value.startsWith("st_"))
      text += " (SEXTANTE)";
    else{
      if(selectWPS.value == "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services"){
        continue;
      }
    }

    // add process
    option.text = text;
    option.value = value;
    option.title =  processes[i][2];

    try
    {
      selectWPSProcesses.add(option,null); // standards compliant
    }
    catch(ex)
    {
      selectWPSProcesses.add(option); // IE only
    }
  }
}

/**
 * This method loads the list of processes of the selected WPS to the select element "selectWPSProcesses".
 */
function loadWFSLayerList(){

  // remove options
  for ( var opt in selectWFSLayers.options) {
    selectWFSLayers.remove(opt.index);
  }

  // determine and load WFS layers
  var layers = determineWFSFeatureTypeNames(selectWFS.value);

  // add layers
  for ( var i = 0; i < layers.length; i++) {
    var option = document.createElement("option");

    // add layer
    option.text = layers[i];
    option.value = layers[i];

    try
    {
      selectWFSLayers.add(option,null); // standards compliant
    }
    catch(ex)
    {
      selectWFSLayers.add(option); // IE only
    }
  }
}


function loadWFSURLs(){

  // remove options
  for ( var opt in selectWFS.options) {
    selectWFS.remove(opt.index);
  }

    var selectedWFSURL = null;
    for ( var url in wfsURLs) {
      if(wfsURLs[url] != null){
        var option = document.createElement("option");
        option.text  = wfsURLs[url];
        option.value = wfsURLs[url];
        selectWFS.appendChild(option);
        if(selectedWFSURL == null)
          selectedWFSURL = wfsURLs[url];
      }
  }

    return selectedWFSURL;
}

function loadWPSURLs(){

  // remove options
  for ( var opt in selectWPS.options) {
    selectWPS.remove(opt.index);
  }

    var selectedWPSURL = null;
    for ( var url in wpsURLs) {
      if(wpsURLs[url] != null){
        var option = document.createElement("option");
        option.text  = wpsURLs[url];
        option.value = wpsURLs[url];
        selectWPS.appendChild(option);
        if(selectedWPSURL == null)
          selectedWPSURL = wpsURLs[url];
      }
  }

    return selectedWPSURL;
}

/**
 * This method load all input parameter as HTML form of the selected process.
 */
function loadProcessInputForms(){

  // determine process description
    var processDesc = determineWPSProcessDescription(selectWPS.value, selectWPSProcesses.value);

    // remove old input forms
    divInputValues.innerHTML="";
    inputLiterals = new Array();
    selectLayers = new Array();
    inputOutputLayers = new Array();

    // add input forms
    for ( var i = 0; i < processDesc[0].length; i++) {
    var inputParam = processDesc[0][i];
    if(inputParam[3] == WPSInputData.VECTOR_LAYER_SCHEMA_GML2)
      divInputValues.appendChild(createVectorLayerHTMLElement(inputParam[0],inputParam[1],inputParam[2],inputParam[3]));
    else
      divInputValues.appendChild(createLiteralHTMLElement(inputParam[0],inputParam[1],inputParam[2],inputParam[3]));
  }

    // add hidden output formats
    for ( var i = 0; i < processDesc[1].length; i++) {
    var outputParam = processDesc[1][i];
    if(outputParam[3] == WPSInputData.VECTOR_LAYER_SCHEMA_GML2)
      divInputValues.appendChild(createOutputVectorLayerHTMLElement(outputParam[0],outputParam[1],outputParam[2],outputParam[3]));
   }

}

/**
 * This method creates a HTML input element for a literal input parameter.
 *
 * @param identifier
 *            Parameter identifier
 * @param title
 *            Parameter title
 * @param abstr
 *            Parameter abstract
 * @param type
 *            Parameter type like "double", "string", "boolean" or "integer"
 *
 * @return HTML input element for a literal input parameter.
 */
function createLiteralHTMLElement(identifier, title, abstr, type){

  var p = document.createElement("p");
  p.innerHTML = title;

  if(type != null && type != "")
    p.innerHTML += " (" + type + "): ";
  else
    p.innerHTML += ": ";

  var input = null;
  if(type != "boolean"){// others
    input = document.createElement("input");
    input.type = "text";
    input.id = identifier;
    input.className = type;
    input.onclick = function(){
      input.focus();
      }
  }else{// boolean
    input = document.createElement("select");
    input.id = identifier;
    input.className = type;

    var yes = document.createElement("option");
    yes.text = "yes";
    yes.value = "true";

    var no = document.createElement("option");
    no.text = "no";
    no.value = "false";

    try
      {
      input.add(yes,null); // standards compliant
      input.add(no,null); // standards compliant
      }
    catch(ex)
      {
      input.add(yes); // IE only
      input.add(no); // IE only
      }
  }

  inputLiterals[identifier] = input;
  p.appendChild(input);


  // popup with abstract
  p.appendChild(document.createTextNode(" "));
  p.appendChild(getHyperlinkForPopup(title, abstr, type));

  return p;
}



/**
 * This method creates a HTML input element for a vector layer input parameter.
 *
 * @param identifier
 *            Parameter identifier
 * @param title
 *            Parameter title
 * @param abstr
 *            Parameter abstract
 * @param schema
 *            Schema of GML vector layer
 *
 * @return HTML input element for a vector layer input parameter
 */
function createVectorLayerHTMLElement(identifier, title, abstr, schema){

  var p = document.createElement("p");
  p.innerHTML = title + ": ";
  var select = document.createElement("select");
  select.id = identifier;
  select.className = schema;
  select.style.width = "400px";
  select.onchange = function(){resultDisplayWPS.data = ""};
  selectLayers[identifier] = select;
  p.appendChild(select);


  // determine and load WPS processes
  for ( var i in layers) {

    if(layers[i] != null){
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
  }

  // popup with abstract
  p.appendChild(document.createTextNode(" "));
  p.appendChild(getHyperlinkForPopup(title, abstr, schema));

  return p;
}

/**
 * This method creates a hidden HTML input element for a vector layer output parameter.
 *
 * @param identifier
 *            Parameter identifier
 * @param title
 *            Parameter title
 * @param abstr
 *            Parameter abstract
 * @param schema
 *            Schema of GML vector layer
 *
 * @return Hidden HTML input element for a vector layer output parameter
 */
function createOutputVectorLayerHTMLElement(identifier, title, abstr, schema){
  var input = document.createElement("input");
  input.type = "hidden";
  input.id = identifier;
  input.value = schema;
  inputOutputLayers[identifier] = input;
  return input;
}

/**
 * This method creates a WPSInputData object with all input data from GUI.
 *
 * @return WPSInputData object with all input data from GUI.
 */
function createWPSInputData(){
  var data = new WPSInputData();

  // add input vector layers to data object
  for ( var i in selectLayers) {
    data.addVectorLayer(selectLayers[i].id, selectLayers[i].value, selectLayers[i].className);
  }

  // add input literals to data object
  for ( var i in inputLiterals) {
    data.addLiteral(inputLiterals[i].id, inputLiterals[i].className, inputLiterals[i].value);
  }

  // add output vector layer schemas to data object
  for ( var i in inputOutputLayers) {
    outputLayer = inputOutputLayers[i];
    data.addOutputVectorLayerFormat(inputOutputLayers[i].id, inputOutputLayers[i].defaultValue);
  }

  return data;
}

function getHyperlinkForPopup(title, abstr, type){

  // popup with abstract
  var info = document.createElement("a");
  info.innerHTML = "(i)";
  info.title = abstr;
  info.style.color = "white";
  info.href = "#";
  info.onclick = function(){
    showInputDataDescription(title, abstr, type);
  };

  return info;
}

/**
 * This method shows a popup with a input parameter descripton.
 *
 * @param title
 *            Parameter title
 * @param abstr
 *            Parameter abstract
 * @param type
 *            Parameter type
 */
function showInputDataDescription(title, abstr, type) {

  if(abstr == null || abstr == "" || abstr == "undefined")
    abstr = "No description available!";

  var htmlMsg = "<h3>" + title + "</h3><hr />";
  htmlMsg += "<h4>" + abstr + "</h4>";

   var win = window.open("", "win", "width=450,height=270,scrollbars=yes");
   win.document.open("text/html", "replace");
   win.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"><html xmlns="http://www.w3.org/1999/xhtml"><head><title></title></head><body>' + htmlMsg + '</body></html>');
   win.focus();
   win.document.close();
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
 *            LITERAL_BOOLEAN_TYPE, LITERAL_BOOLEAN_TYPE, LITERAL_STRING_TYPE or LITERAL_NUMERICAL_VALUE_TYPE.
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
 * This method returns the managed input parameter as query string to execute a WPS over HTTP GET.
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
     * APIProperty: roundedCorner {Boolean} If true the Rico library is used for rounding the corners of the layer switcher div, defaults to true.
     */
      roundedCorner: true,

      /**
     * APIProperty: roundedCornerColor {String} The color of the rounded corners, only applies if roundedCorner is true, defaults to "darkblue".
     */
      roundedCornerColor: "darkblue",

      /**
     * Property: layerStates {Array(Object)} Basically a copy of the "state" of the map's layers the last time the control was drawn. We have this in order to avoid unnecessarily redrawing the control.
     */
      layerStates: null,


      // DOM Elements

      /**
     * Property: layersDiv {DOMElement}
     */
      layersDiv: null,

      /**
     * Property: baseLayers {Array(<OpenLayers.Layer>)}
     */
      baseLayers: null,


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
     * Returns: {DOMElement} A reference to the DIV DOMElement containing the switcher tabs.
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
     * Method: clearLayersArray User specifies either "base" or "data". we then clear all the corresponding listeners, the div, and reinitialize a new array.
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
     * Method: checkRedraw Checks if the layer state has changed since the last redraw() call.
     *
     * Returns: {Boolean} The layer state changed since the last redraw() call.
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
     * Method: redraw Goes through and takes the current state of the Map and rebuilds the control to display that state. Groups base layers into a radio-button group and lists each data layer with a checkbox.
     *
     * Returns: {DOMElement} A reference to the DIV DOMElement containing the control
     */
      redraw: function() {
          // if the state hasn't changed since last redraw, no need
          // to do anything. Just return the existing div.
          if (!this.checkRedraw()) {
              return this.div;
          }

          return this.div;
      },

      /**
     * Method: A label has been clicked, check or uncheck its corresponding input
     *
     * Parameters: e - {Event}
     *
     * Context: - {DOMElement} inputElem - {<OpenLayers.Control.LayerSwitcher>} layerSwitcher - {<OpenLayers.Layer>} layer
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
     * Method: onLayerClick Need to update the map accordingly whenever user clicks in either of the layers.
     *
     * Parameters: e - {Event}
     */
      onLayerClick: function(e) {
          this.updateMap();
      },


      /**
     * Method: updateMap Cycles through the loaded data and base layer input arrays and makes the necessary calls to the Map object such that that the map's visual state corresponds to what the user has selected in the control.
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

          resultDisplayWPS.data = "";
          resultDisplayWFS.data = "";


          this.showControls(false);

          if (e != null) {
              OpenLayers.Event.stop(e);
          }
      },

      /**
     * Method: minimizeControl Hide all the contents of the control, shrink the size, add the maximize icon
     *
     * Parameters: e - {Event}
     */
      minimizeControl: function(e) {

          // to minimize the control we set its div's width
          // and height to 0px, we cannot just set "display"
          // to "none" because it would hide the maximize
          // div

          resultDisplayWPS.data = "";
          resultDisplayWFS.data = "";

          this.div.style.width = "0px";
          this.div.style.height = "0px";

          this.showControls(true);

          if (e != null) {
              OpenLayers.Event.stop(e);
          }
      },

      /**
     * Method: showControls Hide/Show all LayerSwitcher controls depending on whether we are minimized or not
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
                top = "10px";
                // right = "";
                left = "70px";
                fontFamily = "sans-serif";
                fontWeight = "bold";
                fontSize = "smaller";
                // marginTop = "3px";
                // marginLeft = "3px";
                padding = "10px";
                color = "white";
                backgroundColor = "darkblue";
          }



          // layers list div
          this.layersDiv = document.createElement("div");
          this.layersDiv.id = this.id + "_layersDiv";
          OpenLayers.Element.addClass(this.layersDiv, "layersDiv");


          with(this.layersDiv.style) {
              // backgroundColor = "red";
               width = "600px";
          }


          // form
          var form = document.createElement("form");
          this.layersDiv.appendChild(form);
          this.div.appendChild(this.layersDiv);


          // add wps textfield
          var p0 = document.createElement("p");
          p0.innerHTML = "WPS: ";
          form.appendChild(p0);
          var wpsInputText = document.createElement("input");
          wpsInputText.type = "text";
          wpsInputText.style.width = "400px";
          wpsInputText.onclick = function(){
            wpsInputText.focus();
          }

          p0.appendChild(wpsInputText);
          p0.appendChild(document.createTextNode(" "));

          // add wps button
          var addButton = document.createElement("input");
          addButton.type = "button";
          addButton.value = "Add URL";
          addButton.onclick = function(){
            addWPSURL(wpsInputText.value);
            loadWPSURLs();
            wpsInputText.value = "";
            resultDisplayWPS.data = "Added new WPS URL.";
          };
          p0.appendChild(addButton);

          // wps remove button
          var removeButton = document.createElement("input");
          removeButton.type = "button";
          removeButton.value = "Remove URL";
          removeButton.onclick = function(){
            removeWPSURL(selectWPS.value);
            loadWPSURLs();
            loadWPSProcessList();
            loadProcessInputForms();
            resultDisplayWPS.data = "Removed WPS URL.";
          };



          // create list of wps urls
          var p1 = document.createElement("p");
          p1.innerHTML = "<hr />WPS: ";
          form.appendChild(p1);

          var select1 = document.createElement("select");
          selectWPS = select1;
          select1.name = "wpslist";
          select1.size = "1";
          select1.style.width = "400px";
          p1.appendChild(select1);
          p1.appendChild(document.createTextNode(" "));
          p1.appendChild(removeButton);


          select1.onchange = function(){
            resultDisplayWPS.data = "";
            loadWPSProcessList();
            loadProcessInputForms();
          };

          // load wps urls
          var selectedWPSURL = loadWPSURLs();

          // create process list
          var p2 = document.createElement("p");
          p2.innerHTML = "Process: ";
          form.appendChild(p2);

          var select2 = document.createElement("select");
          select2.name = "processlist";
          select2.size = "1";
          select2.style.width = "400px";
          p2.appendChild(select2);
          aProcessDesc.onclick = function(){
            var opt = select2.options[select2.selectedIndex];
            showInputDataDescription(opt.text, opt.title, opt.value);
          };
          select2.onchange = function(){
            aProcessDesc.onclick = function(){
              var opt = select2.options[select2.selectedIndex];
              showInputDataDescription(opt.text, opt.title, opt.value);
            };
            resultDisplayWPS.data = "";
            loadProcessInputForms();
          };
          selectWPSProcesses = select2;
          loadWPSProcessList();


          // create input forms
          var p5 = document.createElement("p");
          p5.innerHTML = "<hr>Input Parameter:";
          form.appendChild(p5);
          var inputDiv = document.createElement("div");
          form.appendChild(inputDiv);
          divInputValues = inputDiv;
          loadProcessInputForms();


          // create execute button
          var input = document.createElement("input");
          input.type = "button";
          input.value = "Execute";
          input.onclick = function(){

            // create layer name
            var layerName = "WPS ";
            layerName += selectWPSProcesses.value;
            var name = null;
            for ( var i in selectLayers) {
              if(name == null)
                name = selectLayers[i].options[selectLayers[i].selectedIndex].text;
              else
                name += "_" + selectLayers[i].options[selectLayers[i].selectedIndex].text;
            }
            if(name != null){
              layerName += " (";
              layerName += name;
              layerName +=  ")";
            }

            // add WPS layer
            addWPSLayer(layerName, selectWPS.value, selectWPSProcesses.value, createWPSInputData() );
          };
          p2.appendChild(document.createTextNode(" "));
          p2.appendChild(input); // execute button
          p2.appendChild(document.createTextNode(" "));
          p2.appendChild(aProcessDesc); // process description

          var p4 = document.createElement("p");
          p4.innerHTML = "<hr>";
          form.appendChild(p4);
          p4.appendChild(resultDisplayWPS);
          resultDisplayWPS.data = "";


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
          var sz = new OpenLayers.Size(44,22);

          // maximize button div
          var img = imgLocation + 'layer-switcher-maximize.png';
          var wpsIcon = 'images/wps-icon.png';
          this.maximizeDiv = OpenLayers.Util.createAlphaImageDiv(
                                      "OpenLayers_Control_MaximizeDiv",
                                      new OpenLayers.Pixel(0,0),
                                      sz,
                                      wpsIcon,
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
                                      new OpenLayers.Pixel(0,0),
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
     * Method: mouseDown Register a local 'mouseDown' flag so that we'll know whether or not to ignore a mouseUp event
     *
     * Parameters: evt - {Event}
     */
      mouseDown: function(evt) {
          this.isMouseDown = true;
          this.ignoreEvent(evt);
      },

      /**
     * Method: mouseUp If the 'isMouseDown' flag has been set, that means that the drag was started from within the LayerSwitcher control, and thus we can ignore the mouseup. Otherwise, let the Event continue.
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

var WFSSwitcher =
    OpenLayers.Class(OpenLayers.Control, {

      /**
     * APIProperty: roundedCorner {Boolean} If true the Rico library is used for rounding the corners of the layer switcher div, defaults to true.
     */
      roundedCorner: true,

      /**
     * APIProperty: roundedCornerColor {String} The color of the rounded corners, only applies if roundedCorner is true, defaults to "darkblue".
     */
      roundedCornerColor: "darkblue",

      /**
     * Property: layerStates {Array(Object)} Basically a copy of the "state" of the map's layers the last time the control was drawn. We have this in order to avoid unnecessarily redrawing the control.
     */
      layerStates: null,


      // DOM Elements

      /**
     * Property: layersDiv {DOMElement}
     */
      layersDiv: null,

      /**
     * Property: baseLayers {Array(<OpenLayers.Layer>)}
     */
      baseLayers: null,


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
     * Returns: {DOMElement} A reference to the DIV DOMElement containing the switcher tabs.
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
     * Method: clearLayersArray User specifies either "base" or "data". we then clear all the corresponding listeners, the div, and reinitialize a new array.
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
     * Method: checkRedraw Checks if the layer state has changed since the last redraw() call.
     *
     * Returns: {Boolean} The layer state changed since the last redraw() call.
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
     * Method: redraw Goes through and takes the current state of the Map and rebuilds the control to display that state. Groups base layers into a radio-button group and lists each data layer with a checkbox.
     *
     * Returns: {DOMElement} A reference to the DIV DOMElement containing the control
     */
      redraw: function() {
          // if the state hasn't changed since last redraw, no need
          // to do anything. Just return the existing div.
          if (!this.checkRedraw()) {
              return this.div;
          }

          return this.div;
      },

      /**
     * Method: A label has been clicked, check or uncheck its corresponding input
     *
     * Parameters: e - {Event}
     *
     * Context: - {DOMElement} inputElem - {<OpenLayers.Control.LayerSwitcher>} layerSwitcher - {<OpenLayers.Layer>} layer
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
     * Method: onLayerClick Need to update the map accordingly whenever user clicks in either of the layers.
     *
     * Parameters: e - {Event}
     */
      onLayerClick: function(e) {
          this.updateMap();
      },


      /**
     * Method: updateMap Cycles through the loaded data and base layer input arrays and makes the necessary calls to the Map object such that that the map's visual state corresponds to what the user has selected in the control.
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

          resultDisplayWPS.data = "";
          resultDisplayWFS.data = "";

          this.showControls(false);

          if (e != null) {
              OpenLayers.Event.stop(e);
          }
      },

      /**
     * Method: minimizeControl Hide all the contents of the control, shrink the size, add the maximize icon
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

          resultDisplayWPS.data = "";
          resultDisplayWFS.data = "";

          this.showControls(true);

          if (e != null) {
              OpenLayers.Event.stop(e);
          }
      },

      /**
     * Method: showControls Hide/Show all LayerSwitcher controls depending on whether we are minimized or not
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
              top = "42px";
              // right = "";
              left = "70px";
              fontFamily = "sans-serif";
              fontWeight = "bold";
              fontSize = "smaller";
              // marginTop = "3px";
              // marginLeft = "3px";
              padding = "10px";
              color = "white";
              backgroundColor = "darkblue";
          }



          // layers list div
          this.layersDiv = document.createElement("div");
          this.layersDiv.id = this.id + "_layersDiv";
          OpenLayers.Element.addClass(this.layersDiv, "layersDiv");


          with(this.layersDiv.style) {
            // backgroundColor = "red";
             width = "600px";
          }



          // form
          var form = document.createElement("form");
          this.layersDiv.appendChild(form);
          this.div.appendChild(this.layersDiv);



          // add wps textfield
          var p0 = document.createElement("p");
          p0.innerHTML = "WFS: ";
          form.appendChild(p0);
          var wfsInputText = document.createElement("input");
          wfsInputText.type = "text";
          wfsInputText.style.width = "400px";
          wfsInputText.onclick = function(){
            wfsInputText.focus();
          }
          p0.appendChild(wfsInputText);
          p0.appendChild(document.createTextNode(" "));

          // add wfs add button
          var addButton = document.createElement("input");
          addButton.type = "button";
          addButton.value = "Add URL";
          addButton.onclick = function(){
            addWFSURL(wfsInputText.value);
            loadWFSURLs();
            wfsInputText.value = "";
            resultDisplayWFS.data = "Added new WFS URL.";
          };
          p0.appendChild(addButton);

          // add wfs remove sbutton
          var removeButton = document.createElement("input");
          removeButton.type = "button";
          removeButton.value = "Remove URL";
          removeButton.onclick = function(){
            removeWFSURL(selectWFS.value);
            loadWFSURLs();
            loadWFSLayerList();
            resultDisplayWFS.data = "Removed WFS URL.";
          };


          // create list of wfs urls
          var p1 = document.createElement("p");
          p1.innerHTML = "<hr />WFS: ";
          form.appendChild(p1);

          var select1 = document.createElement("select");
          selectWFS = select1;
          select1.name = "wpslist";
          select1.size = "1";
          select1.style.width = "400px";
          p1.appendChild(select1);
          p1.appendChild(document.createTextNode(" "));
          p1.appendChild(removeButton);
          selectWFS = select1;

          select1.onchange = function(){
            resultDisplayWPS.data = "";
            loadWFSLayerList();
          };


          // load wfs urls
          var selectedWFSURL = loadWFSURLs();

          // create layer list
          var p2 = document.createElement("p");
          p2.innerHTML = "Layer: ";
          form.appendChild(p2);

          var select2 = document.createElement("select");
          select2.name = "layerlist";
          select2.size = "1";
          select2.style.width = "300px";
          p2.appendChild(select2);
          select2.onchange = function(){
            resultDisplayWFS.data = "";
          };

          selectWFSLayers = select2;
          loadWFSLayerList();


          //clip by view
          var input2 = document.createElement("select");
      var yes = document.createElement("option");
      yes.text = "Clip by View";
      yes.value = true;
      var no = document.createElement("option");
      no.text = "All Data";
      no.value = false;
      try
        {
        input2.add(yes,null); // standards compliant
        input2.add(no,null); // standards compliant
        }
      catch(ex)
        {
        input2.add(yes); // IE only
        input2.add(no); // IE only
        }
      input2.onchange = function(){
        if(input2.value == "true")
          wfsClipByView = true;
        else
          wfsClipByView = false;
          };
          p2.appendChild(document.createTextNode(" "));
          p2.appendChild(input2);

          var input = document.createElement("input");
          input.type = "button";
          input.value = "Add Layer";
          input.onclick = function(){

            // create layer name
            var layerName = "WFS ";
            layerName += selectWFSLayers.value;

            // add WFS layer
            var wfsLayer = addWFSLayer(layerName, selectWFS.value,selectWFSLayers.value);

            // reload input layers
            loadProcessInputForms();

            // bounds = wfsLayer;
            // alert(bounds);
            // map.setCenter(new OpenLayers.LonLat(0, 0), 3);
          };
          p2.appendChild(document.createTextNode(" "));
          p2.appendChild(input);




          // create info panel
          var p4 = document.createElement("p");
          p4.innerHTML = "<hr>";
          form.appendChild(p4);
          resultDisplayWFS.data = "";
          p4.appendChild(resultDisplayWFS);

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
          var sz = new OpenLayers.Size(44,22);

          // maximize button div
          var img = imgLocation + 'layer-switcher-maximize.png';
          var wfsIcon = 'images/wfs-icon.png';
          this.maximizeDiv = OpenLayers.Util.createAlphaImageDiv(
                                      "OpenLayers_Control_MaximizeDiv",
                                      new OpenLayers.Pixel(0,0),
                                      sz,
                                      wfsIcon,
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
                                      new OpenLayers.Pixel(0,0),
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
     * Method: mouseDown Register a local 'mouseDown' flag so that we'll know whether or not to ignore a mouseUp event
     *
     * Parameters: evt - {Event}
     */
      mouseDown: function(evt) {
          this.isMouseDown = true;
          this.ignoreEvent(evt);
      },

      /**
     * Method: mouseUp If the 'isMouseDown' flag has been set, that means that the drag was started from within the LayerSwitcher control, and thus we can ignore the mouseup. Otherwise, let the Event continue.
     *
     * Parameters: evt - {Event}
     */
      mouseUp: function(evt) {
          if (this.isMouseDown) {
              this.isMouseDown = false;
              this.ignoreEvent(evt);
          }
      },

      CLASS_NAME: "WFSSwitcher"
});
