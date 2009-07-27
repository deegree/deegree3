load('common.js')

var r = top.rendering.r2d

var parser = r.se.parser.SE110SymbolizerAdapter()
parser.load(java.io.File("/tmp/test.xml").toURI().toURL())
var smb = parser.parsePointSymbolizer()


var da = top.commons.dataaccess
var fac = top.geometry.GeometryFactoryCreator.getInstance().getGeometryFactory()
var bbox = fac.createEnvelope(150890,400000,200000,500000,null)
var ds = da.shape.ShapeDatastore("/home/stranger/daten/simplepoints", null, null)
var bboxop = top.commons.filter.spatial.BBOX(bbox)
var filter = top.commons.filter.OperatorFilter(bboxop)

var fc = ds.query(filter)

var BI = java.awt.image.BufferedImage
var img = BI(1458, 926, BI.TYPE_INT_ARGB)
var graphics = img.createGraphics()
var renderer = r.Java2DRenderer(graphics, 1458, 926, fac.createEnvelope(154830.53674429376,443370.63256666646,154947.25529915286,443444.76245816133, null))

var iterator = fc.iterator()
while(iterator.hasNext()){
  var f = iterator.next()
  var p = smb.evaluate(f)
  renderer.render(p.first, p.second)
}

graphics.dispose()

javax.imageio.ImageIO.write(img, "png", java.io.File("/tmp/test.png"))
