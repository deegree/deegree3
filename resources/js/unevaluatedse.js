load('common.js')

var r = top.rendering.r2d

var parser = r.se.parser.SE110SymbolizerAdapter()
parser.load(java.io.File("/tmp/test.xml").toURI().toURL())
var smb = parser.parsePointSymbolizer()
