<?xml version="1.0"?>
<sld:StyledLayerDescriptor version="1.0.0" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
  <sld:NamedLayer>
    <sld:Name>SGID500_Contours500Ft</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:PointSymbolizer>
            <sld:Geometry>
              <ogc:Function name="Centroid">
                <ogc:PropertyName>geometry</ogc:PropertyName>
              </ogc:Function>
            </sld:Geometry>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>star</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">#449933</sld:CssParameter>
                </sld:Fill>
                <sld:Stroke>
                  <sld:CssParameter name="stroke">#003300</sld:CssParameter>
                </sld:Stroke>
              </sld:Mark>
              <sld:Size>32</sld:Size>
            </sld:Graphic>
          </sld:PointSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>

  </sld:NamedLayer>

</sld:StyledLayerDescriptor>
