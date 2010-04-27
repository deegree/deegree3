<?xml version="1.0"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
  <NamedLayer>
    <Name>SGID500_Contours500Ft</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
	    <Geometry>
	      <ogc:Function name="Centroid">
		<ogc:PropertyName>geometry</ogc:PropertyName>
	      </ogc:Function>
	    </Geometry>
            <Graphic>
              <Mark>
                <WellKnownName>star</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#449933</CssParameter>
                </Fill>
                <Stroke>
                  <CssParameter name="stroke">#003300</CssParameter>
                </Stroke>
              </Mark>
              <Size>32</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>

  </NamedLayer>

</StyledLayerDescriptor>
