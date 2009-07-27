load('common.js')

var Client = top.protocol.wms.client.WMSClient111
var xml = top.commons.xml.XMLAdapter
var capas = xml(java.net.URL("http://demo.deegree.org/deegree-wms/services?request=capabilities&service=WMS"))
// var capaslocal = xml(java.io.File("/tmp/test.xml"))
var client = Client(capas)
var r = Client.Requests
