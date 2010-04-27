<?xml version="1.0"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">
  <NamedLayer>
    <Name>SGID500_Contours500Ft</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
	  <LineSymbolizer>
	    <Stroke>
	      <SvgParameter name="stroke">#11aa22</SvgParameter>
	      <SvgParameter name="stroke-width">
		<ogc:Function name="idiv">
		  <ogc:PropertyName>SHAPE_LEN</ogc:PropertyName>
		  <ogc:Literal>5</ogc:Literal>
		</ogc:Function>
	      </SvgParameter>
	      <SvgParameter name="stroke-dasharray">20 10 50 20</SvgParameter>
	    </Stroke>
	  </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>

  </NamedLayer>

</StyledLayerDescriptor>
