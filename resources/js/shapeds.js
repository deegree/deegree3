load('common.js')

var da = top.commons.dataaccess
var fac = top.geometry.GeometryFactoryCreator.getInstance().getGeometryFactory()
var bbox = fac.createEnvelope(150890,400000,200000,500000,null)
var ds = da.shape.ShapeDatastore("/home/stranger/daten/simplepoints", null, null)
var bboxop = top.commons.filter.spatial.BBOX(bbox)
var filter = top.commons.filter.OperatorFilter(bboxop)

var fc = ds.query(filter)
// var feature = fc.iterator().next()
var iterator = fc.iterator()
while(iterator.hasNext()){
  var f = iterator.next()
  print(f.getPropertyValue(javax.xml.namespace.QName("sometext")))
  print(f.getPropertyValue(javax.xml.namespace.QName("someint")))
  print(f.getPropertyValue(javax.xml.namespace.QName("somedouble")))
}
