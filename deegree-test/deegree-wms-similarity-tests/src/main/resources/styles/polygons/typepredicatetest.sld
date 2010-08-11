<?xml version="1.0"?>
<sld:StyledLayerDescriptor version="1.0.0" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">

  <sld:NamedLayer>
    <sld:Name>SGID500_DominantVegetation</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:Function name="IsSurface">
                <ogc:PropertyName>geometry</ogc:PropertyName>
              </ogc:Function>
              <ogc:Literal>true</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <sld:PolygonSymbolizer>
            <sld:Stroke>
              <sld:SvgParameter name="stroke">#119933</sld:SvgParameter>
              <sld:SvgParameter name="stroke-width">10</sld:SvgParameter>
            </sld:Stroke>
            <sld:PerpendicularOffset>5</sld:PerpendicularOffset>
          </sld:PolygonSymbolizer>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:SvgParameter name="stroke">#000000</sld:SvgParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>

  <sld:NamedLayer>
    <sld:Name>SGID500_Contours500Ft</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:Function name="IsCurve">
                <ogc:PropertyName>geometry</ogc:PropertyName>
              </ogc:Function>
              <ogc:Literal>true</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:SvgParameter name="stroke">#000000</sld:SvgParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>

  </sld:NamedLayer>

  <sld:NamedLayer>
    <sld:Name>SGID024_Springs</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:PointSymbolizer>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>cross</sld:WellKnownName>
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

  <sld:NamedLayer>
    <sld:Name>SGID500_Contours500Ft</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsEqualTo>
                <ogc:Function name="IsPoint">
                  <ogc:Function name="Centroid">
                    <ogc:PropertyName>geometry</ogc:PropertyName>
                  </ogc:Function>
                </ogc:Function>
                <ogc:Literal>true</ogc:Literal>
              </ogc:PropertyIsEqualTo>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>ELEV</ogc:PropertyName>
                <ogc:Literal>8000</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:And>
          </ogc:Filter>
          <sld:PointSymbolizer>
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
