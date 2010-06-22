<?xml version="1.0" encoding="UTF-8"?>
<MultiCurve xmlns="http://www.opengis.net/gml" xmlns:gml="http://www.opengis.net/gml"
	srsName="EPSG:4326" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd">
	<curveMembers>
		<Curve>
			<segments>
				<Arc interpolation="circularArc3Points">
					<posList srsName="EPSG:4326">2 0 0 2 -2 0</posList>
				</Arc>
				<LineStringSegment interpolation="linear">
					<posList srsName="EPSG:4326">-2 0 0 -2 2 0</posList>
				</LineStringSegment>
			</segments>
		</Curve>
		<CompositeCurve>
			<curveMember>
				<Curve>
					<segments>
						<Arc interpolation="circularArc3Points">
							<posList srsName="EPSG:4326">2 0 0 2 -2 0</posList>
						</Arc>
						<LineStringSegment interpolation="linear">
							<posList srsName="EPSG:4326">-2 0 0 -2</posList>
						</LineStringSegment>
					</segments>
				</Curve>
			</curveMember>
			<curveMember>
				<LineString>
					<posList srsName="EPSG:4326">0 -2 -10 -10</posList>
				</LineString>
			</curveMember>
		</CompositeCurve>
	</curveMembers>
</MultiCurve>
