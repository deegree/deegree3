<?xml version="1.0"?>
<sld:StyledLayerDescriptor version="1.0.0" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
  <sld:NamedLayer>
    <sld:Name>SGID500_Contours500Ft</sld:Name>

    <sld:UserStyle>
      <sld:FeatureTypeStyle>
        <sld:Rule>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:SvgParameter name="stroke">#11aa22</sld:SvgParameter>
              <sld:SvgParameter name="stroke-width">
                <ogc:Function name="idiv">
                  <ogc:PropertyName>SHAPE_LEN</ogc:PropertyName>
                  <ogc:Literal>5</ogc:Literal>
                </ogc:Function>
              </sld:SvgParameter>
              <sld:SvgParameter name="stroke-dasharray">20 10 50 20</sld:SvgParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>

  </sld:NamedLayer>

</sld:StyledLayerDescriptor>
