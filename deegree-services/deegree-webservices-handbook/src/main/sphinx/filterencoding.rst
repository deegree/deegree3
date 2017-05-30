.. _anchor-configuration-filter:

===============
Filter Encoding
===============

deegree makes extensive use of the OGC Filter Encoding standard. Within deegree there are implementations
of the versions  1.1.0 and 2.0.0 of this standards and several extensions and additional functions.
This chapter is meant to explain the filter capabilities of deegree which can 
be used within :ref:`anchor-configuration-renderstyles` and :ref:`anchor-configuration-wfs´ requests.

^^^^^^^^^^^^^^^^
Filter Operators
^^^^^^^^^^^^^^^^

The purpose of FE is to have a standardized way for defining selection criteria on data.
This requires the definition of operators for the creation of filter expressions. Within this section,
the supported operators are explained. Additionally there is information about deegree specific behaviour.
Depending on the version of FE, syntax may differ. In the following, FE 1.1.0 syntax is used.

____________________
Arithmetic operators
____________________

FE enables the use of the following arithmetic operators:

 * Add: used for addition
 * Sub: used for substraction
 * Mul: used for multiplication
 * Div: used for division

**Example:**

.. code-block:: xml

    <Add>
      <PropertyName>app:ID</PropertyName>
      <Literal>15</Literal>
    </Add>

____________________
Logical operators
____________________

FE enables the use of the following logical operators:

 * And: Links two conditions with AND
 * Or: links two conditions with OR
 * Not: negates a condition

**Example:**

.. code-block:: xml

    <Not>
      <PropertyName>app:ID</PropertyName>
      <Literal>15</Literal>
    </Not>

____________________
Comparison operators
____________________

deegree has implementations for the following list of comparison operators:

 * PropertyIsEqualTo: Evaluates if a property value equals to another value.
 * PropertyIsNotEqualTo: Evaluates if a property value differs from another value.
 * PropertyIsLessThan: Evaluates if a property value is smaller than another value.
 * PropertyIsGreaterThan: Evaluates if a property value is greater than another value.
 * PropertyIsLessThanOrEqualTo: Evaluates if a property value is smaller than or equal to another value.
 * PropertyIsGreaterThanOrEqualTo: Evaluates if a property value is greater than or euqal to another value.
 * PropertyIsLike: Evaluates if a property value is like another value. It compares string values which each other.
 * PropertyIsNull: Evaluates if a property value is NULL.
 * PropertyIsBetween: Evaluates if a property value is between 2 defined values.

**Example:**

.. code-block:: xml

    <PropertyIsEqualTo>
      <PropertyName>SomeProperty</PropertyName>
      <Literal>100</Literal>
    </PropertyIsEqualTo>

_________________
Spatial operators
_________________

With deegree you can make use of the following spatial operators:

 * Equals: Evaluates if geometries are identical
 * Disjoin: Evaluates if geometries are spatially disjoined
 * Touches: Evaluates if geometries are spatially touching
 * Within: Evaluates if a geometry is spatially within another
 * Overlaps: Evaluates if geometries are spatially overlapping
 * Crosses: Evaluates if geometries are spatially crossing
 * Intersects: Evaluates if geometries are spatially intersecting. This is meant as the opposite of disjoin.
 * Contains: Evaluates if a geometry spatially contains another.
 * DWithin: Evaluates if a geometry is within a specific distance to another.
 * Beyond: Evaluates if a geometry is beyond a specific distance to another.
 * BBOX: Evaluates if a geometry spatially intersects with a given bounding box.

**Example:**

.. code-block:: xml

    <Overlaps>
      <PropertyName>Geometry</PropertyName>
      <gml:Polygon srsName="EPSG:4258">
        <gml:outerBoundaryIs>
          <gml:LinearRing>
            <gml:posList> ... </gml:posList>
          </gml:LinearRing>
        </gml:outerBoundaryIs>
      </gml:Polygon>
    </Overlaps>

.. tip:: For further reading on spatial operators, please refer to the OGC Simple Features Specification For SQL.

^^^^^^^^^^^^^^^^^^
Filter expressions
^^^^^^^^^^^^^^^^^^

For the use within map styles or WFS requests, filter expressions can be constructed from the above operators to select specific data.
This section gives some examples for the use of such filter expressions. 

_________________________
Simple filter expressions
_________________________

-----------------------------
Comparative filter expression
-----------------------------

.. code-block:: xml

    <Filter>
      <PropertyIsEqualTo>
        <PropertyName>SomeProperty</PropertyName>
        <Literal>100</Literal>
      </PropertyIsEqualTo>
    </Filter>

This filter expressions shows, how filter expressions with a comparative filter are constructed
In the concrete example, the property SomeProperty is evaluated, if it equals to the value of "100".

-------------------------
Spatial filter expression
-------------------------

.. code-block:: xml

    <Filter>
      <Overlaps>
        <PropertyName>Geometry</PropertyName>
        <gml:Polygon srsName="EPSG:4258">
          <gml:outerBoundaryIs>
            <gml:LinearRing>
              <gml:posList> ... </gml:posList>
            </gml:LinearRing>
          </gml:outerBoundaryIs>
        </gml:Polygon>
      </Overlaps>
    </Filter>

This filter expressions shows, how filter expressions with a spatial filter are constructed. In the concrete case, the defined filter looks up,
if the property geometry overlaps with the define polygon of ...

___________________________
Advanced filter expressions
___________________________

-------------------------
Multiple filter operators
-------------------------

.. code-block:: xml

    <Filter>
      <And>
        <PropertyIsLessThan>
          <PropertyName>DEPTH</PropertyName>
          <Literal>30</Literal>
        </PropertyIsLessThan>
        <Not>
          <Disjoint>
            <PropertyName>Geometry</PropertyName>
            <gml:Envelope srsName="EPSG:4258">
              <gml:lowerCorner>13.0983 31.5899</gml:lowerCorner>
              <gml:upperCorner>35.5472 42.8143</gml:upperCorner>
            </gml:Envelope>
          </Disjoint>
        </Not>
      </And>
    </Filter>

This more complex filter expressions shows, how to make use of combinations of filter operators.
THe given filter expression evaluates if the value of the property DEPTH is smaller than "30" AND if the
geometry property named Geometry is spatially disjoint with the given envelope.


------------------------------
PropertyIsLike with a function
------------------------------

.. code-block:: xml

    <fes:Filter xmlns:fes="http://www.opengis.net/fes/2.0">
      <fes:PropertyIsLike wildCard="*" singleChar="#" escapeChar="!">
        <fes:ValueReference>name</fes:ValueReference>
        <fes:Function name="normalize">
          <fes:Literal>FALkenstrasse</fes:Literal>
        </fes:Function>
      </fes:PropertyIsLike>
    </fes:Filter>

This example shows, how functions can be used within filter expressions. Within the given example, the "name" property is evaluated, if it is like
the Literal FAlkenstrasse. Using a function for the evaluation of the Literal means, that the value is processed with the function before the
filter operator handles it. In the concrete case this means a normalization of the value (Which is not usable by default with deegree).

.. tip:: Please note, the use of functions within PropertyIsLike filter operators is only possible with FE 2.0. This is the reason for the FE 2.0 notation.

___________________________________________
Filter expressions on xlink:href attributes
___________________________________________

Filter expressions which filter on the static value of a xlink:href attribute itself just work if the feature store is configured a certain way. For example, this can be useful if a user wants to filter on INSPIRE codelists.

Chapter :ref:`anchor-mapping-strategies-href-attributes` describes how the configuration of the feature store is done and provides further details regarding usage.

^^^^^^^^^^^^^^^^^^^
Custom FE functions
^^^^^^^^^^^^^^^^^^^
Besides the filter capabilities described above, FE defines Functions to be used within filter expressions.
deegree offers the capability to use a nice set of custom FE functions for different purposes.
These are explained within the following chapter.

____
Area
____

The area function is the first in a row of custom geometry functions which can be used within deegree. With the area function it is possible to get the area of a geometry property. If multiple geometry nodes are selected, multiple area values are calculated.

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="Area">
      <PropertyName>app:geometry</PropertyName>
    </Function>

______
Length
______

This function calculates the length of a linestring/perimeter of a polygon. If multiple geometry nodes are selected, multiple length values are calculated.

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="Length">
      <PropertyName>app:geometry</PropertyName>
    </Function>

________
Centroid
________

This function calculates the centroid of a polygon. If multiple geometry nodes are selected, multiple centroids are calculated.

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="Centroid">
      <PropertyName>app:geometry</PropertyName>
    </Function>

_____________
InteriorPoint
_____________

This function calculates an interior point within a polygon. If multiple geometry nodes are selected, multiple centroids are calculated. Useful to place text on a point within a polygon (centroids may not actually be a point on the polygon).

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="InteriorPoint">
      <PropertyName>app:geometry</PropertyName>
    </Function>

___________________________
IsPoint, IsCurve, IsSurface
___________________________

Takes one parameter, which must evaluate to exactly one geometry node.

This function returns true, if the geometry is a point/multipoint, curve/multicurve or surface/multisurface, respectively.

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="IsCurve">
      <PropertyName>app:geometry</PropertyName>
    </Function>

_______________
GeometryFromWKT
_______________

Useful to create a constant geometry valued expression.

.. code-block:: xml

    <Function xmlns="http://www.opengis.net/ogc" name="GeometryFromWKT">
      <Literal>EPSG:4326</Literal>
      <Literal>POINT(0.6 0.7)</Literal>
    </Function>

____________
MoveGeometry
____________

Useful to displace geometries by a certain value in x and/or y direction.

To shift 20 geometry units in y direction:

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="MoveGeometry">
      <PropertyName>app:geometry</PropertyName>
      <Literal>0</Literal>
      <Literal>20</Literal>
    </Function>

____
iDiv
____

Integer division discarding the remainder.

.. code-block:: xml

    <Function xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/ogc" name="idiv">
      <PropertyName>app:count</PropertyName>
      <Literal>20</Literal>
    </Function>

____
iMod
____

Integer division resulting in the remainder only.

.. code-block:: xml

    <Function xmlns="http://www.opengis.net/ogc" name="ExtraProp">
      <Literal>planArt</Literal>
    </Function>

_________
ExtraProp
_________

Access extra (hidden) properties attached to feature objects. The availability of such properties depends on the loading/storage mechanism used.

.. code-block:: xml

    <Function xmlns="http://www.opengis.net/ogc" name="ExtraProp">
      <Literal>planArt</Literal>
    </Function>

_______________
GetCurrentScale
_______________

The GetCurrentScale function takes no arguments, and dynamically provides you with the value of the current map scale denominator (only to be used in GetMap requests!). The scale denominator will be adapted to any custom pixel size you may be using in your request, and is the same scale denominator the WMS uses internally for filtering out layers/style rules.

Let's have a look at an example:

.. code-block:: xml

  ...
  <sld:SvgParameter name="stroke-width">
    <ogc:Function name="idiv">
      <ogc:Literal>500000</ogc:Literal>
      <ogc:Function name="GetCurrentScale" />
    </ogc:Function>
  </sld:SvgParameter>
  ...

In this case, the stroke width will be one pixel for scales around 500000, and will get bigger as you zoom in (and the scale denominator gets smaller). Scale denominators above 500000 will yield invisible strokes with a width of zero.
