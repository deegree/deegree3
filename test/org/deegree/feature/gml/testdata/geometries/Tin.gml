<?xml version="1.0" encoding="UTF-8"?>
<Tin xmlns="http://www.opengis.net/gml" xmlns:gml="http://www.opengis.net/gml"
	srsName="EPSG:4326" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/geometryPrimitives.xsd">
	<!--
		It seems to be an error that GML 3.1.1 specifies both the "gml:trianglePatches" and the "gml:controlPoint" properties!? This is apparently
		redundant, and consequently (?) GML 3.2.1 only allows the controlPoint property.
	-->
	<trianglePatches>
			<Triangle> <exterior> <LinearRing> <posList>0 0 1 0 0 1 0 0</posList>
			</LinearRing> </exterior> </Triangle> <Triangle> <exterior>
			<LinearRing> <posList>0 0 1 0 0 1 0 0</posList> </LinearRing>
			</exterior> </Triangle> <Triangle> <exterior> <LinearRing> <posList>0
			0 1 0 0 1 0 0</posList> </LinearRing> </exterior> </Triangle>
	</trianglePatches>
	<stopLines>
		<LineStringSegment interpolation="linear">
			<posList>2 0 0 2 -2 0</posList>
		</LineStringSegment>
		<LineStringSegment interpolation="linear">
			<posList>2 0 0 2 -8 0</posList>
		</LineStringSegment>
	</stopLines>
	<stopLines>
		<LineStringSegment interpolation="linear">
			<posList>2 0 0 2 -6 0</posList>
		</LineStringSegment>
	</stopLines>
	<breakLines>
		<LineStringSegment interpolation="linear">
			<posList>2 0 0 2 -2 0</posList>
		</LineStringSegment>
	</breakLines>
	<maxLength uom="http://bogus...#metres">15.0</maxLength>
	<controlPoint>
		<posList>0.0 0.0 1.0 2.0 3.0 4.0</posList>
	</controlPoint>
</Tin>
