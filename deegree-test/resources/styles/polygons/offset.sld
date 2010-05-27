<?xml version="1.0"?>
<sld:StyledLayerDescriptor version="1.0.0" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
  <sld:NamedLayer>
    <sld:Name>SGID500_DominantVegetation</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>DOM</ogc:PropertyName>
              <ogc:Literal>B1</ogc:Literal>
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

</sld:StyledLayerDescriptor>
