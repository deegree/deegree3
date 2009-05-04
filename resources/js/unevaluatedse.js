load('common.js')

var r = top.rendering.r2d
var ps = r.styling.PointStyling()
var symbolizer = r.se.unevaluated.Symbolizer(ps)
var evald1 = symbolizer.evaluate(null)

function myContFun(obj, feat){
  print ("hallo" + feat)
  return
}

var cont = r.se.unevaluated.Continuation({updateStep: myContFun})
var symbolizer2 = r.se.unevaluated.Symbolizer(ps, cont)

function test(){
  if(ps != evald1) print("Already evaluated PointStyling not equal to the evaluated one!")
}

test()

symbolizer2.evaluate(null)
