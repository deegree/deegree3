function pretty(o,doc){
  var funs = []
  var objs = []
  var oths = []
  var unks = []
  for(var x in o)
    try{
      if(typeof o[x] == 'function') funs.push(x)
      else if(typeof o[x] == 'object') objs.push(x)
      else oths.push(x)
    }catch(e){
      unks.push(x)
    }

  if(funs.length > 0)
    print("\nFunctions:")
  for(x in funs){
    if(doc)
      print(o[funs[x]])
    else
      print(funs[x] + " (" + (typeof o[funs[x]]) + ")")
  }
  if(objs.length > 0)
    print("\nObjects:")
  for(x in objs)
    print(objs[x] + " (" + (typeof o[objs[x]]) + ")")
  if(oths.length > 0)
    print("\nOther properties:")
  for(x in oths)
    print(oths[x] + " (" + (typeof o[oths[x]]) + ")")
  if(unks.length > 0)
    print("\nUnknown properties:")
  for(x in oths)
    print(oths[x] + " (unknown)")
}

var top = org.deegree
