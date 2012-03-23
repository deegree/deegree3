var WMSUtils = {

  nodesAsStrings: function(xml, xpath){
    if(window.ActiveXObject){
      // IE
      xml.setProperty('SelectionLanguage','XPath')
      var res = xml.documentElement.selectNodes(xpath)
      var list = []
      for(var i = 0; i < res.length; ++i) list[i] = res[i].firstChild.nodeValue
      return list
    }

    // the rest (at least firefox and chrome)
    var res = xml.evaluate(xpath, xml.documentElement, null, 0, null)
    var next = res.iterateNext()
    var list = []
    while(next){
      list[list.length] = next.textContent
      next = res.iterateNext()
    }
    return list
  },

  getNodes: function(xml, xpath){
    if(window.ActiveXObject){
      // IE
      xml.setProperty('SelectionLanguage','XPath')
      var res = xml.documentElement.selectNodes(xpath)
      var list = []
      for(var i = 0; i < res.length; ++i) list[i] = res[i]
      return list
    }

    // the rest (at least firefox and chrome)
    var res = xml.evaluate(xpath, xml.documentElement, null, 0, null)
    var next = res.iterateNext()
    var list = []
    while(next){
      list[list.length] = next
      next = res.iterateNext()
    }
    return list
  },

  getCapabilities: function(url){
    if((!document.compatMode) && (!document.documentMode)){
      return OpenLayers.Request.GET({url: url, async: false, params: {request: 'GetCapabilities', service: 'WMTS', version: '1.0.0'}}).responseXML
    }

    if(window.ActiveXObject){
      // IE
      var doc = new ActiveXObject("Microsoft.XMLDOM")
      doc.async = false

      var xml = OpenLayers.Request.GET({url: url, async: false, params: {request: 'GetCapabilities', service: 'WMTS', version: '1.0.0'}}).responseText
      doc.loadXML(xml)
      return doc
    }

    // for some reason chrome 7 does not have responseXML as well
    var xml = OpenLayers.Request.GET({url: url, async: false, params: {request: 'GetCapabilities', service: 'WMTS', version: '1.0.0'}}).responseText
    return new DOMParser().parseFromString( xml, "text/xml" )
  },

  layerNames : function (capabilities, leafNodesOnly){
    var xpath = leafNodesOnly ? '//Layer[not(Layer)]/Name' : '//Layer/Name'
    return WMSUtils.nodesAsStrings(capabilities, xpath)
  },

  getBoundingBox: function(capabilities){
    var xpath = "//Capability/Layer/BoundingBox[@SRS='EPSG:900913']"
    var box = new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34)
    var selected = WMSUtils.getNodes(capabilities, xpath)
    if(selected.length > 0){
      box = new OpenLayers.Bounds(selected[0].getAttribute('minx'),
                                  selected[0].getAttribute('miny'),
                                  selected[0].getAttribute('maxx'),
                                  selected[0].getAttribute('maxy'))
    }

    xpath = "//Capability/Layer/LatLonBoundingBox"
    selected = WMSUtils.getNodes(capabilities, xpath)
    if(selected.length > 0){
      var src = new OpenLayers.Projection('EPSG:4326')
      var dest = new OpenLayers.Projection('EPSG:900913')
      box = new OpenLayers.Bounds(selected[0].getAttribute('minx'),
                                  selected[0].getAttribute('miny'),
                                  selected[0].getAttribute('maxx'),
                                  selected[0].getAttribute('maxy'))
      box.transform(src, dest)
    }

    return box
  },

  createOpenlayersMap: function(capabilities, layers, loc){
//    var initialBox = WMSUtils.getBoundingBox(capabilities)
OpenLayers.DOTS_PER_INCH = 90.72;
    var map = new OpenLayers.Map( 'map', {
                                    projection: new OpenLayers.Projection("EPSG:26912"),
                                    units: "m",
                                    maxExtent: new OpenLayers.Bounds(431174.000, 4348455.000,  457810.000, 4556039.000 ),
    scales: [
    114285.71428571429 ,
    57142.857142857145 ,
    28571.428571428572 ,
    14285.714285714286 ,
    7142.857142857143  
    ],

                                    allOverlays: true
                                  } )
console.log(map)
var caps = new OpenLayers.Format.WMTSCapabilities().read(capabilities)

//    var osmLayer = new OpenLayers.Layer.OSM("OpenStreetMap")
//    map.addLayer(osmLayer)
    
//    var matrixIds = [
//    '7142.857142857143',
//    '14285.714285714286',
//    '28571.428571428572',
//    '57142.857142857145',
//    '114285.71428571429'
//    ]
//    var matrixIds = [
//    '114285.71428571429',
//    '57142.857142857145',
//    '28571.428571428572',
//    '14285.714285714286',
//    '7142.857142857143'
//    ]

//    var l = new OpenLayers.Layer.WMTS({
//      name: 'test',
//      url: 'http://localhost:8080/deegree-webservices/services',
//      layer: 'pyramid',
//      matrixSet: 'pyramid',
//      format: 'image/png',
//      style: 'default',
//      opacity: 1,
//      isBaseLayer: false
//    });
map.addLayer(new OpenLayers.Format.WMTSCapabilities().createLayer(caps, {layer: 'pyramid', matrixSet: 'pyramid', format: 'image/png'}));

      var layer = new OpenLayers.Layer.WMS( 'pyramid', loc, {layers: 'pyramid', transparent: true}, {singleTile: true})
      layer.setName('pyramid')
      layer.setVisibility(false)
      map.addLayer(layer)
    for(var i in layers){
      var layer = new OpenLayers.Layer.WMS( layers[i], loc, {layers: layers[i], transparent: true}, {singleTile: true})
      layer.setName(layers[i])
      layer.setVisibility(false)
      map.addLayer(layer)
    }



    map.setCenter(new OpenLayers.LonLat(0, 0), 0)
    map.addControl(new OpenLayers.Control.LayerSwitcher())
//    map.zoomToExtent(initialBox, true)
    return map
  }

}
