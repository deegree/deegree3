load('common.js')

var r = top.rendering.r2d
var ps = r.styling.PointStyling()
var symbolizer = r.se.unevaluated.Symbolizer(ps)
var evald1 = symbolizer.evaluate(null)
var cont = r.se.unevaluated.Continuation(null)

function test(){
  if(ps != evald1) print("Already evaluated PointStyling not equal to the evaluated one!")
}

test()
