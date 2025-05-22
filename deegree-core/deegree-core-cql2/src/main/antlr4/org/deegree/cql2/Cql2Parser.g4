// Taken from OGC Sources https://github.com/opengeospatial/ogcapi-features/blob/master/cql2/standard/schema/cql2.bnf
// (specified in https://portal.ogc.org/files/96288) and adapted to use with ANTLR.
// Published under licence https://github.com/opengeospatial/ogcapi-features/blob/master/LICENSE:
// Permission is hereby granted by the Open Geospatial Consortium, ("Licensor"), free of charge and subject to the terms set forth below, to any person obtaining a copy of this Intellectual Property and any associated documentation, to deal in the Intellectual Property without restriction (except as set forth below), including without limitation the rights to implement, use, copy, modify, merge, publish, distribute, and/or sublicense copies of the Intellectual Property, and to permit persons to whom the Intellectual Property is furnished to do so, provided that all copyright notices on the intellectual property are retained intact and that each person to whom the Intellectual Property is furnished agrees to the terms of this Agreement.
// If you modify the Intellectual Property, all copies of the modified Intellectual Property must include, in addition to the above copyright notice, a notice that the Intellectual Property includes modifications that have not been approved or adopted by LICENSOR.
// THIS LICENSE IS A COPYRIGHT LICENSE ONLY, AND DOES NOT CONVEY ANY RIGHTS UNDER ANY PATENTS THAT MAY BE IN FORCE ANYWHERE IN THE WORLD.
// THE INTELLECTUAL PROPERTY IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE DO NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THE INTELLECTUAL PROPERTY WILL MEET YOUR REQUIREMENTS OR THAT THE OPERATION OF THE INTELLECTUAL PROPERTY WILL BE UNINTERRUPTED OR ERROR FREE. ANY USE OF THE INTELLECTUAL PROPERTY SHALL BE MADE ENTIRELY AT THE USER’S OWN RISK. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR ANY CONTRIBUTOR OF INTELLECTUAL PROPERTY RIGHTS TO THE INTELLECTUAL PROPERTY BE LIABLE FOR ANY CLAIM, OR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING FROM ANY ALLEGED INFRINGEMENT OR ANY LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR UNDER ANY OTHER LEGAL THEORY, ARISING OUT OF OR IN CONNECTION WITH THE IMPLEMENTATION, USE, COMMERCIALIZATION OR PERFORMANCE OF THIS INTELLECTUAL PROPERTY.
// This license is effective until terminated. You may terminate it at any time by destroying the Intellectual Property together with all copies in any form. The license will also terminate if you fail to comply with any term or condition of this Agreement. Except as provided in the following sentence, no such termination of this license shall require the termination of any third party end-user sublicense to the Intellectual Property which is in force as of the date of notice of such termination. In addition, should the Intellectual Property, or the operation of the Intellectual Property, infringe, or in LICENSOR’s sole opinion be likely to infringe, any patent, copyright, trademark or other right of a third party, you agree that LICENSOR, in its sole discretion, may terminate this license without any compensation or liability to you, your licensees or any other party. You agree upon termination of any kind to destroy or cause to be destroyed the Intellectual Property together with all copies in any form, whether held by you or by any third party.
// Except as contained in this notice, the name of LICENSOR or of any other holder of a copyright in all or part of the Intellectual Property shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Intellectual Property without prior written authorization of LICENSOR or such copyright holder. LICENSOR is and shall at all times be the sole entity that may authorize you or any third party to use certification marks, trademarks or other special designations to indicate compliance with any LICENSOR standards or specifications. This Agreement is governed by the laws of the Commonwealth of Massachusetts. The application to this Agreement of the United Nations Convention on Contracts for the International Sale of Goods is hereby expressly excluded. In the event any provision of this Agreement shall be deemed unenforceable, void or invalid, such provision shall be modified so as to make it valid and enforceable, and as so modified the entire Agreement shall remain in full force and effect. No decision, action or inaction by LICENSOR shall be construed to be a waiver of any rights or remedies available to it.
//
// MODULE:  cql2.bnf
// PURPOSE: A BNF grammar for the Common Query Language (CQL2).
// HISTORY:
// DATE         EMAIL                                  DESCRIPTION
// 13-SEP-2019  pvretano[at]cubewerx.com               Initial creation
// 28-OCT-2019  pvretano[at]cubewerx.com               Initial check-in into github
// 22-DEC-2020  pvretano[at]cubewerx.com               1.0.0-draft.1 (version for public review)
//              portele[at]interactive-instruments.de
// xx-MAY-2022  pvretano[at]cubewerx.com               1.0.0-rc.1 (release candidate)
//              portele[at]interactive-instruments.de

//=============================================================================//
// A CQL2 filter is a logically connected expression of one or more predicates.
// Predicates include scalar or comparison predicates, spatial predicates or
// temporal predicates.
//
// *** DISCLAIMER: ***
// Because there are many variations of EBNF, this standard has focused
// on verifying that this grammar validates using the following online
// validator:
//
// https://www.icosaedro.it/bnf_chk/bnf_chk-on-line.html
//
// If other tools are used (e.g. ANTLR), the grammar will likely need to be
// adapted to suite the target implementation context.
//=============================================================================//

parser grammar Cql2Parser;
options { tokenVocab=Cql2Lexer; }

booleanExpression : booleanTerm (OR booleanTerm)*;

booleanTerm : booleanFactor (AND booleanFactor)*;

booleanFactor : (NOT)? booleanPrimary;

booleanPrimary : predicate
               | function
               | BooleanLiteral
               | LEFTPAREN booleanExpression RIGHTPAREN;

predicate : comparisonPredicate
          | spatialPredicate
          | temporalPredicate
          | arrayPredicate;

//=============================================================================//
// A comparison predicate evaluates if two scalar expression statisfy the
// specified comparison operator.  The comparion operators includes an operator
// to evaluate pattern matching expressions (LIKE), a range evaluation operator
// and an operator to test if a scalar expression is NULL or not.
//=============================================================================//
comparisonPredicate : binaryComparisonPredicate
                    | isLikePredicate
                    | isBetweenPredicate
                    | isInListPredicate
                    | isNullPredicate;

// Binary comparison predicate
//
binaryComparisonPredicate : scalarExpression
                            ComparisonOperator
                            scalarExpression;

// LIKE predicate
//
isLikePredicate :  characterExpression (NOT)? LIKE patternExpression;

// BETWEEN predicate
//
isBetweenPredicate : numericExpression (NOT)? BETWEEN
                     numericExpression AND numericExpression;

// IN LIST predicate
//
isInListPredicate : scalarExpression (NOT)? IN LEFTPAREN inList RIGHTPAREN;

inList : scalarExpression (COMMA scalarExpression)?;

// IS NULL predicate
//
isNullPredicate : isNullOperand IS (NOT)? NULL;

isNullOperand : characterClause
              | NumericLiteral
              | temporalInstance
              | spatialInstance
              | arithmeticExpression
              | BooleanLiteral
              | propertyName
              | function;

scalarExpression : characterClause
                 | NumericLiteral
                 | instantInstance
                 | arithmeticExpression
                 | BooleanLiteral
                 | propertyName
                 | function;


patternExpression : CASEI LEFTPAREN patternExpression RIGHTPAREN
                  | ACCENTI LEFTPAREN patternExpression RIGHTPAREN
                  | characterLiteral;

numericExpression : arithmeticExpression
                  | NumericLiteral
                  | propertyName
                  | function;

//=============================================================================//
// Character expression
//=============================================================================//
characterExpression : characterClause
                    | propertyName
                    | function;

characterClause : characterLiteral
                | CASEI LEFTPAREN characterExpression RIGHTPAREN
                | ACCENTI LEFTPAREN characterExpression RIGHTPAREN;

//=============================================================================//
// Definition of CHARACTER literals
//=============================================================================//
characterLiteral : CharacterStringLiteral;

//=============================================================================//
// A spatial predicate evaluates if two spatial expressions satisfy the
// condition implied by a standardized spatial comparison function.  If the
// conditions of the spatial comparison function are met, the function returns
// a Boolean value of true.  Otherwise the function returns false.
//=============================================================================//
spatialPredicate :  SpatialFunction LEFTPAREN geomExpression COMMA geomExpression RIGHTPAREN;

// A geometric expression is a property name of a geometry-valued property,
// a geometric literal (expressed as WKT) or a function that returns a
// geometric value.
//
geomExpression : spatialInstance
               | propertyName
               | function;

//=============================================================================//
// A temporal predicate evaluates if two temporal expressions satisfy the
// condition implied by a standardized temporal comparison function.  If the
// conditions of the temporal comparison function are met, the function returns
// a Boolean value of true.  Otherwise the function returns false.
//=============================================================================//
temporalPredicate : TemporalFunction
                    LEFTPAREN temporalExpression COMMA temporalExpression RIGHTPAREN;

temporalExpression : temporalInstance
                   | propertyName
                   | function;

//=============================================================================//
// An array predicate evaluates if two array expressions satisfy the
// condition implied by a standardized array comparison function.  If the
// conditions of the array comparison function are met, the function returns
// a Boolean value of true.  Otherwise the function returns false.
//=============================================================================//
arrayPredicate : ArrayFunction
                 LEFTPAREN arrayExpression COMMA arrayExpression RIGHTPAREN;

arrayExpression : array
                | propertyName
                | function;

// An array is a parentheses-delimited, comma-separated list of array
// elements.
array : LEFTPAREN RIGHTPAREN
      | LEFTPAREN arrayElement (  COMMA arrayElement )* RIGHTPAREN;

// An array element is either a character literal, a numeric literal,
// a geometric literal, a temporal instance, a property name, a function,
// an arithmetic expression or an array.
arrayElement : characterClause
             | NumericLiteral
             | temporalInstance
             | spatialInstance
             | array
             | arithmeticExpression
             | BooleanLiteral
             | propertyName
             | function;

//=============================================================================//
// An arithmetic expression is an expression composed of an arithmetic
// operand (a property name, a number or a function that returns a number),
// an arithmetic operators (+,-,*,/,%,div,^) and another arithmetic operand.
//=============================================================================//

arithmeticExpression : arithmeticTerm ( arithmeticOperatorPlusMinus arithmeticTerm );

arithmeticOperatorPlusMinus : PLUS | MINUS;

arithmeticTerm : powerTerm (arithmeticOperatorMultDiv powerTerm);

arithmeticOperatorMultDiv : ASTERISK | SOLIDUS | PERCENT | DIV;

powerTerm : arithmeticFactor (CARET arithmeticFactor);

arithmeticFactor : LEFTPAREN arithmeticExpression RIGHTPAREN
                 | (MINUS) arithmeticOperand;

arithmeticOperand : NumericLiteral
                  | propertyName
                  | function;

//=============================================================================//
// Definition of a PROPERTYNAME
// Production copied from: https://www.w3.org/TR/REC-xml///sec-common-syn,
//                         'Names and Tokens'.
//=============================================================================//
propertyName : Identifier | DOUBLEQUOTE Identifier DOUBLEQUOTE;

//=============================================================================//
// Definition of a FUNCTION
//=============================================================================//
function : Identifier LEFTPAREN argumentList RIGHTPAREN;

argumentList : argument ( COMMA argument )*;

argument : characterClause
         | NumericLiteral
         | temporalInstance
         | spatialInstance
         | array
         | arithmeticExpression
         | BooleanLiteral
         | propertyName
         | function;

//=============================================================================//
// Definition of GEOMETRIC literals
//
// NOTE: This is basically BNF that define WKT encoding. It would be nice
//       to instead reference some normative BNF for WKT.
//=============================================================================//
spatialInstance : geometryLiteral
                | geometryCollectionTaggedText
                | bboxTaggedText;

geometryLiteral : pointTaggedText
                | linestringTaggedText
                | polygonTaggedText
                | multipointTaggedText
                | multilinestringTaggedText
                | multipolygonTaggedText;

pointTaggedText : POINT (Z)? pointText;

linestringTaggedText : LINESTRING (Z)? lineStringText;

polygonTaggedText : POLYGON (Z)? polygonText;

multipointTaggedText : MULTIPOINT (Z)? multiPointText;

multilinestringTaggedText : MULTILINESTRING (Z)? multiLineStringText;

multipolygonTaggedText : MULTIPOLYGON (Z)? multiPolygonText;

geometryCollectionTaggedText : GEOMETRYCOLLECTION (Z)? geometryCollectionText;

pointText : LEFTPAREN point RIGHTPAREN;

point : xCoord (Whitespace)* yCoord ((Whitespace)* zCoord)?;

xCoord : SignedNumericLiteral;

yCoord : SignedNumericLiteral;

zCoord : SignedNumericLiteral;

lineStringText : LEFTPAREN point COMMA point (COMMA point)? RIGHTPAREN;

linearRingText : LEFTPAREN point COMMA point COMMA point COMMA point (COMMA point )? RIGHTPAREN;

polygonText :  LEFTPAREN linearRingText (COMMA linearRingText)? RIGHTPAREN;

multiPointText : LEFTPAREN pointText (COMMA pointText)? RIGHTPAREN;

multiLineStringText : LEFTPAREN lineStringText (COMMA lineStringText)? RIGHTPAREN;

multiPolygonText : LEFTPAREN polygonText (COMMA polygonText)? RIGHTPAREN;

geometryCollectionText : LEFTPAREN geometryLiteral (COMMA geometryLiteral)* RIGHTPAREN;

bboxTaggedText : BBOX bboxText;

bboxText : LEFTPAREN westBoundLon COMMA southBoundLat COMMA (minElev COMMA)? eastBoundLon COMMA northBoundLat (COMMA maxElev)? RIGHTPAREN;

westBoundLon : SignedNumericLiteral;

eastBoundLon : SignedNumericLiteral;

northBoundLat : SignedNumericLiteral;

southBoundLat : SignedNumericLiteral;

minElev : SignedNumericLiteral;

maxElev : SignedNumericLiteral;

temporalInstance : instantInstance | intervalInstance;

instantInstance : dateInstant | timestampInstant;

dateInstant : DATE
              LEFTPAREN DateInstantString RIGHTPAREN;

timestampInstant : TIMESTAMP
                   LEFTPAREN TimestampInstantString RIGHTPAREN;

intervalInstance : INTERVAL LEFTPAREN instantParameter COMMA instantParameter RIGHTPAREN;

instantParameter : DateInstantString
                 | TimestampInstantString
//TODO: convert to antlr    | ''..''
                 | propertyName
                 | function;