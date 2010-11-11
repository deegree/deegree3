var WMSUtils = {

  layerNames : function (url, leafNodesOnly){
    var xpath = leafNodesOnly ? '//Layer[not(Layer)]/Name' : '//Layer/Name'

    if((!document.compatMode) && (!document.documentMode)){
      var xml = OpenLayers.Request.GET({url: url, async: false, params: {request: 'GetCapabilities', service: 'WMS', version: '1.1.1'}}).responseXML
      var res = xml.evaluate(xpath, xml.documentElement, null, 0, null)
      var next = res.iterateNext()
      var lays = []
      while(next){
        lays[lays.length] = next.textContent
        next = res.iterateNext()
      }
      return lays
    }

    if(window.ActiveXObject){
      // IE
      var doc = new ActiveXObject("Microsoft.XMLDOM")
      doc.async = false

      var xml = OpenLayers.Request.GET({url: url, async: false, params: {request: 'GetCapabilities', service: 'WMS', version: '1.1.1'}}).responseText
      doc.loadXML(xml)
      xml = doc
      xml.setProperty('SelectionLanguage','XPath')
      var res = xml.documentElement.selectNodes(xpath)
      var lays = []
      for(var i = 0; i < res.length; ++i) lays[i] = res[i].firstChild.nodeValue
      return lays
    }

    // for some reason chrome 7 does not have responseXML as well
    var xml = OpenLayers.Request.GET({url: url, async: false, params: {request: 'GetCapabilities', service: 'WMS', version: '1.1.1'}}).responseText
    xml = new DOMParser().parseFromString( xml, "text/xml" )
    var res = xml.evaluate(xpath, xml.documentElement, null, 0, null)
    var next = res.iterateNext()
    var lays = []
    while(next){
      lays[lays.length] = next.textContent
      next = res.iterateNext()
    }
    return lays
  },

  createOpenlayersMap: function(layers, loc){
    var map = new OpenLayers.Map( 'map', {
                                    projection: new OpenLayers.Projection("EPSG:900913"),
                                    units: "m",
                                    maxResolution: 156543.0339,
                                    maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),
                                    allOverlays: true
                                  } )

    var osmLayer = new OpenLayers.Layer.OSM("OpenStreetMap")
    map.addLayer(osmLayer)

    for(var i in layers){
      var layer = new OpenLayers.Layer.WMS( layers[i], loc, {layers: layers[i], transparent: true}, {singleTile: true})
      layer.setName(layers[i])
      layer.setVisibility(false)
      map.addLayer(layer)
    }

    map.setCenter(new OpenLayers.LonLat(0, 0), 0)
    map.addControl(new OpenLayers.Control.LayerSwitcher())
    return map
  }

}