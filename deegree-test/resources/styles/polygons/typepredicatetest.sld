<?xml version="1.0"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc">

  <NamedLayer>
    <Name>SGID500_DominantVegetation</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
  	  <ogc:Filter>
  	    <ogc:PropertyIsEqualTo>
  	      <ogc:Function name="IsSurface">
  		<ogc:PropertyName>geometry</ogc:PropertyName>
  	      </ogc:Function>
  	      <ogc:Literal>true</ogc:Literal>
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

  <NamedLayer>
    <Name>SGID500_Contours500Ft</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
	  <ogc:Filter>
	    <ogc:PropertyIsEqualTo>
	      <ogc:Function name="IsCurve">
		<ogc:PropertyName>geometry</ogc:PropertyName>
	      </ogc:Function>
	      <ogc:Literal>true</ogc:Literal>
	    </ogc:PropertyIsEqualTo>
	  </ogc:Filter>
	  <LineSymbolizer>
	    <Stroke>
	      <SvgParameter name="stroke">#000000</SvgParameter>
	    </Stroke>
	  </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>

  </NamedLayer>

  <NamedLayer>
    <Name>SGID024_Springs</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>cross</WellKnownName>
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

  <NamedLayer>
    <Name>SGID500_Contours500Ft</Name>

    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
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
	  <PointSymbolizer>
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
