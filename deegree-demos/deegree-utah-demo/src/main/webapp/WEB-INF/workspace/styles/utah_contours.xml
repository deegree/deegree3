<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:java="java" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xslutil="de.latlon.deejump.plugin.style.XSLUtility" xmlns:deegreewfs="http://www.deegree.org/wfs" xmlns:app="http://www.deegree.org/app" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns="http://www.opengis.net/sld" version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
    <sld:NamedLayer>
    <!--  This styling file shows the use of SLD styling -->
        <sld:Name>ElevationContours</sld:Name>
        <sld:UserStyle>
            <sld:Name>default:ElevationContours</sld:Name>
            <sld:Title>default:ElevationContours</sld:Title>
            <sld:FeatureTypeStyle>
                <sld:Title>default:ElevationContours</sld:Title>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>all below 5200 ft</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:PropertyIsLessThan>
                            <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                            <ogc:Literal>5200.0</ogc:Literal>
                        </ogc:PropertyIsLessThan>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator>
                    <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#666600</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>5200 - 5900</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>5200.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>5900.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator>
                    <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#887d00</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>5900 - 6600</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>5900.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>6600.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator>
                    <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#aa9300</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>6600 - 7300</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>6600.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>7300.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#ccaa00</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>7300 - 8000</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>7300.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>8000.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#eec100</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>8000 - 8700</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>8000.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>8700.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#eeb500</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>8700 - 9400</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>8700.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>9400.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#cc8800</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>9400 - 10100</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>9400.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>10100.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#aa5b00</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>10100 - 10800</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsGreaterThanOrEqualTo>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>10100.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                                <ogc:Literal>10800.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
                        </ogc:And>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#882d00</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ElevationContours</sld:Name>
                    <sld:Title>all above 10800 ft</sld:Title>
                    <sld:Abstract>default</sld:Abstract>
                    <ogc:Filter>
                        <ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                            <ogc:Literal>10800.0</ogc:Literal>
                        </ogc:PropertyIsGreaterThanOrEqualTo>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0.0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>1.0E9</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name='stroke'>#660000</sld:CssParameter>
                            <sld:CssParameter name='stroke-linecap'>butt</sld:CssParameter>
                            <sld:CssParameter name='stroke-dasharray'>1.0,0.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-linejoin'>round</sld:CssParameter>
                            <sld:CssParameter name='stroke-opacity'>1.0</sld:CssParameter>
                            <sld:CssParameter name='stroke-width'>1.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
            </sld:FeatureTypeStyle>
        </sld:UserStyle>
        <sld:UserStyle>
            <sld:Name>ElevationContoursSimple</sld:Name>
            <sld:Title>ElevationContoursSimple</sld:Title>
            <sld:IsDefault>1</sld:IsDefault>
            <sld:FeatureTypeStyle>
                <sld:Name>ElevationContoursStyle</sld:Name>
                <sld:Rule>
                    <sld:Name>ELEV_4999</sld:Name>
                    <ogc:Filter>
                        <ogc:PropertyIsLessThanOrEqualTo>
                            <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                            <ogc:Literal>4999</ogc:Literal>
                        </ogc:PropertyIsLessThanOrEqualTo>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>3.779527559017323E14</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name="stroke">#588c58</sld:CssParameter>
                            <sld:CssParameter name="stroke-opacity">1.0</sld:CssParameter>
                            <sld:CssParameter name="stroke-width">1</sld:CssParameter>
                            <sld:CssParameter name="stroke-dasharray">1</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ELEV_5000</sld:Name>
                    <ogc:Filter>
                        <ogc:PropertyIsBetween>
                            <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                            <ogc:LowerBoundary>
                                <ogc:Literal>5000</ogc:Literal>
                            </ogc:LowerBoundary>
                            <ogc:UpperBoundary>
                                <ogc:Literal>7499</ogc:Literal>
                            </ogc:UpperBoundary>
                        </ogc:PropertyIsBetween>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>3.779527559017323E14</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name="stroke">#b26b00</sld:CssParameter>
                            <sld:CssParameter name="stroke-opacity">1.0</sld:CssParameter>
                            <sld:CssParameter name="stroke-width">1</sld:CssParameter>
                            <sld:CssParameter name="stroke-dasharray">1</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ELEV_7500</sld:Name>
                    <ogc:Filter>
                        <ogc:PropertyIsBetween>
                            <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                            <ogc:LowerBoundary>
                                <ogc:Literal>7500</ogc:Literal>
                            </ogc:LowerBoundary>
                            <ogc:UpperBoundary>
                                <ogc:Literal>9999</ogc:Literal>
                            </ogc:UpperBoundary>
                        </ogc:PropertyIsBetween>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>3.779527559017323E14</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name="stroke">#8e8e00</sld:CssParameter>
                            <sld:CssParameter name="stroke-opacity">1.0</sld:CssParameter>
                            <sld:CssParameter name="stroke-width">1</sld:CssParameter>
                            <sld:CssParameter name="stroke-dasharray">1</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
                <sld:Rule>
                    <sld:Name>ELEV_10000</sld:Name>
                    <ogc:Filter>
                        <ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyName>app:ELEV</ogc:PropertyName>
                            <ogc:Literal>10000</ogc:Literal>
                        </ogc:PropertyIsGreaterThanOrEqualTo>
                    </ogc:Filter>
                    <!-- <sld:MinScaleDenominator>0</sld:MinScaleDenominator> -->
                    <!-- <sld:MaxScaleDenominator>3.779527559017323E14</sld:MaxScaleDenominator> -->
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name="stroke">#853f10</sld:CssParameter>
                            <sld:CssParameter name="stroke-opacity">1.0</sld:CssParameter>
                            <sld:CssParameter name="stroke-width">1</sld:CssParameter>
                            <sld:CssParameter name="stroke-dasharray">1</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
            </sld:FeatureTypeStyle>
        </sld:UserStyle>
    </sld:NamedLayer>
</sld:StyledLayerDescriptor>
