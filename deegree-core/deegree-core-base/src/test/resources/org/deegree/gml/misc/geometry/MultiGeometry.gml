<?xml version="1.0" encoding="UTF-8"?>
<MultiGeometry xmlns="http://www.opengis.net/gml" xmlns:gml="http://www.opengis.net/gml" srsName="EPSG:4326" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd">
	<geometryMembers>
		<Point>
			<coord>
				<X>7.12</X>
				<Y>50.72</Y>
			</coord>
		</Point>
		<Point>
			<coordinates cs="," decimal="." ts=" ">7.12,50.72</coordinates>
		</Point>
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
		<MultiSurface>
			<surfaceMembers>
				<Surface>
					<patches>
						<PolygonPatch>
							<exterior>
								<LinearRing>
									<posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</posList>
								</LinearRing>
							</exterior>
							<interior>
								<LinearRing>
									<posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</posList>
								</LinearRing>
							</interior>
							<interior>
								<LinearRing>
									<posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</posList>
								</LinearRing>
							</interior>
						</PolygonPatch>
						<PolygonPatch>
							<exterior>
								<LinearRing>
									<posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</posList>
								</LinearRing>
							</exterior>
							<interior>
								<LinearRing>
									<posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</posList>
								</LinearRing>
							</interior>
							<interior>
								<LinearRing>
									<posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</posList>
								</LinearRing>
							</interior>
						</PolygonPatch>
					</patches>
				</Surface>
				<TriangulatedSurface>
					<trianglePatches>
						<Triangle>
							<exterior>
								<LinearRing>
									<posList>0 0 1 0 0 1 0 0</posList>
								</LinearRing>
							</exterior>
						</Triangle>
						<Triangle>
							<exterior>
								<LinearRing>
									<posList>0 0 1 0 0 1 0 0</posList>
								</LinearRing>
							</exterior>
						</Triangle>
						<Triangle>
							<exterior>
								<LinearRing>
									<posList>0 0 1 0 0 1 0 0</posList>
								</LinearRing>
							</exterior>
						</Triangle>
					</trianglePatches>
				</TriangulatedSurface>
			</surfaceMembers>
		</MultiSurface>
		<CompositeSolid>
			<solidMember>
				<Solid>
					<exterior>
						<CompositeSurface>
							<!-- Boden -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568790.511
												5662882.872 60.3842642785516 2568792.556 5662876.452
												60.3842642785516 2568789.569 5662875.556 60.3842642785516
												2568787.556 5662874.953 60.3842642785516 2568785.626
												5662881.244
												60.3842642785516 2568786.096 5662881.386 60.3842642785516
												2568790.511 5662882.872 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Dach -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568790.511
												5662882.872 64.28 2568786.096 5662881.386 64.28 2568785.626
												5662881.244 64.28 2568787.556 5662874.953 64.28 2568789.569
												5662875.556 64.28 2568792.556 5662876.452 64.28 2568790.511
												5662882.872 64.28</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Seite 1 -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568790.511
												5662882.872 60.3842642785516 2568790.511 5662882.872 64.28
												2568792.556 5662876.452 64.28 2568792.556 5662876.452
												60.3842642785516 2568790.511 5662882.872 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Seite 2 -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568792.556
												5662876.452 60.3842642785516 2568792.556 5662876.452 64.28
												2568789.569 5662875.556 64.28 2568789.569 5662875.556
												60.3842642785516 2568792.556 5662876.452 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Seite 3 -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568789.569
												5662875.556 60.3842642785516 2568789.569 5662875.556 64.28
												2568787.556 5662874.953 64.28 2568787.556 5662874.953
												60.3842642785516 2568789.569 5662875.556 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Seite 4 -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568787.556
												5662874.953 60.3842642785516 2568787.556 5662874.953 64.28
												2568785.626 5662881.244 64.28 2568785.626 5662881.244
												60.3842642785516 2568787.556 5662874.953 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Seite 5 -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568785.626
												5662881.244 60.3842642785516 2568785.626 5662881.244 64.28
												2568786.096 5662881.386 64.28 2568786.096 5662881.386
												60.3842642785516 2568785.626 5662881.244 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
							<!-- Seite 6 -->
							<surfaceMember>
								<Polygon>
									<exterior>
										<LinearRing>
											<posList srsDimension="3">2568786.096
												5662881.386 60.3842642785516 2568786.096 5662881.386 64.28
												2568790.511 5662882.872 64.28 2568790.511 5662882.872
												60.3842642785516 2568786.096 5662881.386 60.3842642785516</posList>
										</LinearRing>
									</exterior>
								</Polygon>
							</surfaceMember>
						</CompositeSurface>
					</exterior>
				</Solid>
			</solidMember>
		</CompositeSolid>
	</geometryMembers>
</MultiGeometry>
