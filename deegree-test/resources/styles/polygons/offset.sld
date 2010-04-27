<?xml version="1.0"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
  <NamedLayer>
    <Name>SGID500_DominantVegetation</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
	  <ogc:Filter>
	    <ogc:PropertyIsEqualTo>
	      <ogc:PropertyName>DOM</ogc:PropertyName>
	      <ogc:Literal>B1</ogc:Literal>
	    </ogc:PropertyIsEqualTo>
	  </ogc:Filter>
	  <PolygonSymbolizer>
	    <Stroke>
	      <SvgParameter name="stroke">#119933</SvgParameter>
	      <SvgParameter name="stroke-width">10</SvgParameter>
	    </Stroke>
	    <PerpendicularOffset>5</PerpendicularOffset>
	  </PolygonSymbolizer>
	  <LineSymbolizer>
	    <Stroke>
	      <SvgParameter name="stroke">#000000</SvgParameter>
	    </Stroke>
	  </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>

  </NamedLayer>

</StyledLayerDescriptor>
