load('common.js')

var r = top.rendering.r2d

var parser = r.se.parser.SE110SymbolizerAdapter()
parser.load(java.io.File("../../../d3_services/resources/example/conf/wms/styles/example-pointsymbolizer1.xml").toURI().toURL())
parser.parsePointSymbolizer()

parser.load(java.io.File("../../../d3_services/resources/example/conf/wms/styles/example-pointsymbolizer2.xml").toURI().toURL())
parser.parsePointSymbolizer()

parser.load(java.io.File("../../../d3_services/resources/example/conf/wms/styles/example-linesymbolizer.xml").toURI().toURL())
parser.parseLineSymbolizer().evaluate(null)

parser.load(java.io.File("../../../d3_services/resources/example/conf/wms/styles/example-polygonsymbolizer.xml").toURI().toURL())
parser.parsePolygonSymbolizer().evaluate(null)

