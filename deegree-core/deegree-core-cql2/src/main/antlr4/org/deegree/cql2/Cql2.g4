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

grammar Cql2;

booleanExpression : booleanTerm ('OR' booleanTerm)*;

booleanTerm : booleanFactor ('AND' booleanFactor)*;

booleanFactor : ('NOT')? booleanPrimary;

booleanPrimary : function
               | predicate
               | BooleanLiteral
               | '(' booleanExpression ')';

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

scalarExpression : characterClause
                 | NumericLiteral
                 | instantInstance
                 | arithmeticExpression
                 | BooleanLiteral
                 | propertyName
                 | function;

comparisonOperator : '='      // equal
                   | '<' '>'  // not equal
                   | '<'      // less than
                   | '>'      // greater than
                   | '<' '='  // less than or equal
                   | '>' '='; // greater than or equal

// LIKE predicate
//
isLikePredicate :  characterExpression ('NOT')? 'LIKE' patternExpression;

patternExpression : 'CASEI' '(' patternExpression ')'
                  | 'ACCENTI' '(' patternExpression ')'
                  | characterLiteral;

// BETWEEN predicate
//
isBetweenPredicate : numericExpression ('NOT')? 'BETWEEN'
                     numericExpression 'AND' numericExpression;

numericExpression : arithmeticExpression
                  | NumericLiteral
                  | propertyName
                  | function;

// IN LIST predicate
//
isInListPredicate : scalarExpression ('NOT')? IN '(' inList ')';

inList : scalarExpression (',' scalarExpression)?;

// IS NULL predicate
//
isNullPredicate : isNullOperand 'IS' ('NOT')? 'NULL';

isNullOperand : characterClause
              | NumericLiteral
              | temporalInstance
              | spatialInstance
              | arithmeticExpression
              | BooleanLiteral
              | propertyName
              | function;

//=============================================================================//
// A spatial predicate evaluates if two spatial expressions satisfy the
// condition implied by a standardized spatial comparison function.  If the
// conditions of the spatial comparison function are met, the function returns
// a Boolean value of true.  Otherwise the function returns false.
//=============================================================================//
spatialPredicate :  SpatialFunction '(' geomExpression ',' geomExpression ')';

// NOTE: The buffer functions (DWITHIN and BEYOND) are not included because
//       these are outside the scope of a 'simple' core for CQL2.  These
//       can be added as extensions.
//
SpatialFunction : 'S_INTERSECTS'
                | 'S_EQUALS'
                | 'S_DISJOINT'
                | 'S_TOUCHES'
                | 'S_WITHIN'
                | 'S_OVERLAPS'
                | 'S_CROSSES'
                | 'S_CONTAINS';

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
                    '(' temporalExpression ',' temporalExpression ')';

temporalExpression : temporalInstance
                   | propertyName
                   | function;

TemporalFunction : 'T_AFTER'
                 | 'T_BEFORE'
                 | 'T_CONTAINS'
                 | 'T_DISJOINT'
                 | 'T_DURING'
                 | 'T_EQUALS'
                 | 'T_FINISHEDBY'
                 | 'T_FINISHES'
                 | 'T_INTERSECTS'
                 | 'T_MEETS'
                 | 'T_METBY'
                 | 'T_OVERLAPPEDBY'
                 | 'T_OVERLAPS'
                 | 'T_STARTEDBY'
                 | 'T_STARTS';

//=============================================================================//
// An array predicate evaluates if two array expressions satisfy the
// condition implied by a standardized array comparison function.  If the
// conditions of the array comparison function are met, the function returns
// a Boolean value of true.  Otherwise the function returns false.
//=============================================================================//
arrayPredicate : ArrayFunction
                 '(' arrayExpression ',' arrayExpression ')';

arrayExpression : array
                | propertyName
                | function;

// An array is a parentheses-delimited, comma-separated list of array
// elements.
array : '(' ')'
      | '(' arrayElement (  ',' arrayElement )* ')';

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

arrayFunction : 'A_EQUALS'
              | 'A_CONTAINS'
              | 'A_CONTAINEDBY'
              | 'A_OVERLAPS';

//=============================================================================//
// An arithmetic expression is an expression composed of an arithmetic
// operand (a property name, a number or a function that returns a number),
// an arithmetic operators (+,-,*,/,%,div,^) and another arithmetic operand.
//=============================================================================//

arithmeticExpression : arithmeticTerm ( arithmeticOperatorPlusMinus arithmeticTerm );

arithmeticOperatorPlusMinus : '+' | '-';

arithmeticTerm : powerTerm (arithmeticOperatorMultDiv powerTerm);

arithmeticOperatorMultDiv : '*' | '/' | '%' | 'div';

powerTerm : arithmeticFactor ('^' arithmeticFactor);

arithmeticFactor : '(' arithmeticExpression ')'
                 | ('-') arithmeticOperand;

arithmeticOperand : NumericLiteral
                  | propertyName
                  | function;

//=============================================================================//
// Definition of a PROPERTYNAME
// Production copied from: https://www.w3.org/TR/REC-xml///sec-common-syn,
//                         'Names and Tokens'.
//=============================================================================//
propertyName : Identifier | '"' Identifier '"';

//=============================================================================//
// Definition of a FUNCTION
//=============================================================================//
function : Identifier '(' argumentList ')';

argumentList : argument ( ',' argument )*;

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
// Character expression
//=============================================================================//
characterExpression : characterClause
                    | propertyName
                    | function;

characterClause : 'CASEI' '(' characterExpression ')'
                | 'ACCENTI' '(' characterExpression ')'
                | characterLiteral;

//=============================================================================//
// Definition of CHARACTER literals
//=============================================================================//
characterLiteral : '\'' character '\'';

character : ALPHA | DIGIT | Whitespace | EscapeQuote;

EscapeQuote : '\'\'' | '\\\'';

// MOVED ALPHA, DIGIT, Whitespace

//=============================================================================//
// Definition of NUMERIC literals
//=============================================================================//

SignedNumericLiteral : (Sign)? ExactNumericLiteral | ApproximateNumericLiteral;

UnsignedNumericLiteral : ExactNumericLiteral | ApproximateNumericLiteral;

NumericLiteral : UnsignedNumericLiteral | SignedNumericLiteral;

ExactNumericLiteral : UnsignedInteger ('.' (UnsignedInteger)? )?
                      | '.' UnsignedInteger;

ApproximateNumericLiteral : Mantissa 'E' Exponent;

Mantissa : ExactNumericLiteral;

Exponent : SignedInteger;

SignedInteger : (Sign)? UnsignedInteger;

UnsignedInteger : (DIGIT)+;

Sign : '+' | '-';

//=============================================================================//
// Boolean literal
//=============================================================================//
//
BooleanLiteral : 'TRUE' | 'FALSE';

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

pointTaggedText : 'POINT' ('Z')? pointText;

linestringTaggedText : 'LINESTRING' ('Z')? lineStringText;

polygonTaggedText : 'POLYGON' ('Z')? polygonText;

multipointTaggedText : 'MULTIPOINT' ('Z')? multiPointText;

multilinestringTaggedText : 'MULTILINESTRING' ('Z')? multiLineStringText;

multipolygonTaggedText : 'MULTIPOLYGON' ('Z')? multiPolygonText;

geometryCollectionTaggedText : 'GEOMETRYCOLLECTION' ('Z')? geometryCollectionText;

pointText : '(' point ')';

point : xCoord (Whitespace)* yCoord ((Whitespace)* zCoord)?;

xCoord : SignedNumericLiteral;

yCoord : SignedNumericLiteral;

zCoord : SignedNumericLiteral;

lineStringText : '(' point ',' point (',' point)? ')';

linearRingText : '(' point ',' point ',' point ',' point (',' point )? ')';

polygonText :  '(' linearRingText (',' linearRingText)? ')';

multiPointText : '(' pointText (',' pointText)? ')';

multiLineStringText : '(' lineStringText (',' lineStringText)? ')';

multiPolygonText : '(' polygonText (',' polygonText)? ')';

geometryCollectionText : '(' geometryLiteral (',' geometryLiteral)* ')';

bboxTaggedText : 'BBOX' bboxText;

bboxText : '(' westBoundLon ',' southBoundLat ',' (minElev ',')? eastBoundLon ',' northBoundLat (',' maxElev)? ')';

westBoundLon : SignedNumericLiteral;

eastBoundLon : SignedNumericLiteral;

northBoundLat : SignedNumericLiteral;

southBoundLat : SignedNumericLiteral;

minElev : SignedNumericLiteral;

maxElev : SignedNumericLiteral;

temporalInstance : instantInstance | intervalInstance;

instantInstance : dateInstant | timestampInstant;

dateInstant : 'DATE'
              '(' dateInstantString ')';

dateInstantString : '\'' FullDate '\'';

timestampInstant : 'TIMESTAMP'
                   '(' TimestampInstantString ')';

TimestampInstantString : '\'' FullDate 'T' UtcTime '\'';

intervalInstance : 'INTERVAL' '(' instantParameter ',' instantParameter ')';

instantParameter : dateInstantString
                 | TimestampInstantString
//TODO: convert to antlr    | ''..''
                 | propertyName
                 | function;

FullDate   : DateYear '-' DateMonth '-' DateDay;

DateYear   : DIGIT DIGIT DIGIT DIGIT;

DateMonth  : DIGIT DIGIT;

DateDay    : DIGIT DIGIT;

UtcTime  : TimeHour ':' TimeMinute ':' TimeSecond 'Z';

TimeHour   : DIGIT DIGIT;

TimeMinute : DIGIT DIGIT;

TimeSecond : DIGIT DIGIT ('.' DIGIT (DIGIT)?)?;

fragment A : ('A'|'a');
fragment B : ('B'|'b');
fragment C : ('D'|'c');
fragment D : ('D'|'d');
fragment E : ('E'|'e');
fragment F : ('F'|'f');
fragment G : ('G'|'g');
fragment H : ('H'|'h');
fragment I : ('I'|'i');
fragment J : ('A'|'j');
fragment K : ('A'|'k');
fragment L : ('L'|'l');
fragment M : ('M'|'m');
fragment N : ('N'|'n');
fragment O : ('O'|'o');
fragment P : ('P'|'p');
fragment Q : ('Q'|'q');
fragment R : ('R'|'r');
fragment S : ('S'|'s');
fragment T : ('T'|'t');
fragment U : ('U'|'u');
fragment V : ('V'|'v');
fragment W : ('W'|'w');
fragment X : ('X'|'x');
fragment Y : ('Y'|'y');
fragment Z : ('Z'|'z');

// character & digit productions copied from:
// https://www.w3.org/TR/REC-xml///charsets
//
ALPHA : '\u0007'..'\u0008'    // bell, bs
      | '\u0021'..'\u0026'    // !, ', //, $, %, &
      | '\u0028'..'\u002F'     // (, ), *, +, comma, -, ., /
      | '\u003A'..'\u0084'     // --+
      | '\u0086'..'\u009F'     //   |
      | '\u00A1'..'\u167F'     //   |
      | '\u1681'..'\u1FFF'     //   |
      | '\u200B'..'\u2027'     //   +-> :,;,<,=,>,?,@,A-Z,[,\,],^,_,`,a-z,...
      | '\u202A'..'\u202E'     //   |
      | '\u2030'..'\u205E'     //   |
      | '\u2060'..'\u2FFF'     //   |
      | '\u3001'..'\uD7FF'     // --+
      | '\uE000'..'\uFFFD'     // See note 8.
      | '\u{10000}'..'\u{10FFFF}'; // See note 9.

// Note 8: Private Use, CJK Compatibility Ideographs, Alphabetic Presentation
//         Forms, Arabic Presentation Forms-A, Combining Half Marks, CJK
//         Compatibility Forms, Small Form Variants, Arabic Presentation Forms-B,
//         Specials, Halfwidth and Fullwidth Forms, Specials
// Note 9: Linear B Syllabary, Linear B Ideograms, Aegean Numbers, Ancient Greek
//         Numbers, Ancient Symbols, Phaistos Disc, Lycian, Carian, Coptic
//         Epact Numbers, Old Italic, Gothic, Old Permic, Ugaritic, Old Persian,
//         Deseret, Shavian, Osmanya, Osage, Elbasan, Caucasian Albanian,
//         Vithkuqi, Linear A, Latin Extended-F, Cypriot Syllabary, Imperial
//         Aramaic, Palmyrene, Nabataean, Hatran, Phoenician, Lydian, Meroitic
//         Hieroglyphs, Meroitic Cursive, Kharoshthi, Old South Arabian, Old
//         North Arabian, Manichaean, Avestan, Inscriptional Parthian,
//         Inscriptional Pahlavi, Psalter Pahlavi, Old Turkic, Old Hungarian,
//         Hanifi Rohingya, Rumi Numeral Symbols, Yezidi, Arabic Extended-C,
//         Old Sogdian, Sogdian, Old Uyghur, Chorasmian, Elymaic, Brahmi,
//         Kaithi, Sora Sompeng, Chakma, Mahajani, Sharada, Sinhala Archaic
//         Numbers, Khojki, Multani, Khudawadi, Grantha, Newa, Tirhuta, Siddham,
//         Modi, Mongolian Supplement, Takri, Ahom, Dogra, Warang Citi, Dives
//         Akuru, Nandinagari, Zanabazar Square, Soyombo, Unified Canadian
//         Aboriginal Syllabics Extended-A, Pau Cin Hau, Devanagari Extended-A,
//         Bhaiksuki, Marchen, Masaram Gondi, Gunjala Gondi, Makasar, Kawi,
//         Lisu Supplement, Tamil Supplement, Cuneiform, Cuneiform Numbers and
//         Punctuation, Early Dynastic Cuneiform, Cypro-Minoan, Egyptian
//         Hieroglyphs, Egyptian Hieroglyph Format Controls, Anatolian
//         Hieroglyphs, Bamum Supplement, Mro, Tangsa, Bassa Vah, Pahawh Hmong,
//         Medefaidrin, Miao, Ideographic Symbols and Punctuation, Tangut,
//         Tangut Components, Khitan Small Script, Tangut Supplement, Kana
//         Extended-B, Kana Supplement, Kana Extended-A, Small Kana Extension,
//         Nushu, Duployan, Shorthand Format Controls, Znamenny Musical Notation,
//         Byzantine Musical Symbols, Musical Symbols, Ancient Greek Musical
//         Notation, Kaktovik Numerals, Mayan Numerals, Tai Xuan Jing Symbols,
//         Counting Rod Numerals, Mathematical Alphanumeric Symbols, Sutton
//         SignWriting, Latin Extended-G, Glagolitic Supplement, Cyrillic
//         Extended-D, Nyiakeng Puachue Hmong, Toto, Wancho, Nag Mundari,
//         Ethiopic Extended-B, Mende Kikakui, Adlam, Indic Siyaq Numbers,
//         Ottoman Siyaq Numbers, Arabic Mathematical Alphabetic Symbols,
//         Mahjong Tiles, Domino Tiles, Playing Cards, Enclosed Alphanumeric
//         Supplement, Enclosed Ideographic Supplement, Miscellaneous Symbols
//         and Pictographs, Emoticons, Ornamental Dingbats, Transport and Map
//         Symbols, Alchemical Symbols, Geometric Shapes Extended, Supplemental
//         Arrows-C, Supplemental Symbols and Pictographs, Chess Symbols, Symbols
//         and Pictographs Extended-A, Symbols for Legacy Computing, CJK Unified
//         Ideographs Extension B, CJK Unified Ideographs Extension C, CJK
//         Unified Ideographs Extension D, CJK Unified Ideographs Extension E,
//         CJK Unified Ideographs Extension F, CJK Compatibility Ideographs
//         Supplement, CJK Unified Ideographs Extension G, CJK Unified
//         Ideographs Extension H, Tags, Variation Selectors Supplement,
//         Supplementary Private Use Area-A, Supplementary Private Use Area-B

DIGIT : '\u0030'..'\u0039';

Whitespace : '\u0009'  // Character tabulation
           | '\u000A'  // Line feed
           | '\u000B'  // Line tabulation
           | '\u000C'  // Form feed
           | '\u000D'  // Carriage return
           | '\u0020'  // Space
           | '\u0085'  // Next line
           | '\u00A0'  // No-break space
           | '\u1680'  // Ogham space mark
           | '\u2000'  // En quad
           | '\u2001'  // Em quad
           | '\u2002'  // En space
           | '\u2003'  // Em space
           | '\u2004'  // Three-per-em space
           | '\u2005'  // Four-per-em space
           | '\u2006'  // Six-per-em space
           | '\u2007'  // Figure space
           | '\u2008'  // Punctuation space
           | '\u2009'  // Thin space
           | '\u200A'  // Hair space
           | '\u2028'  // Line separator
           | '\u2029'  // Paragraph separator
           | '\u202F'  // Narrow no-break space
           | '\u205F'  // Medium mathematical space
           | '\u3000'; // Ideographic space


Identifier : IDENTIFIERSTART ( IDENTIFIERPART )*;

IDENTIFIERPART : IDENTIFIERSTART
               | '.'                    // '\u002E'
               | DIGIT                  // 0-9
               | '\u0300'..'\u036F'     // combining and diacritical marks
               | '\u203F'..'\u2040';    // ‿ and ⁀

IDENTIFIERSTART : '\u003A'              // colon
                | '\u005F'              // underscore
                | '\u0041'..'\u005A'    // A-Z
                | '\u0061'..'\u007A'    // a-z
                | '\u00C0'..'\u00D6'    // À-Ö Latin-1 Supplement Letters
                | '\u00D8'..'\u00F6'    // Ø-ö Latin-1 Supplement Letters
                | '\u00F8'..'\u02FF'    // ø-ÿ Latin-1 Supplement Letters
                | '\u0370'..'\u037D'    // Ͱ-ͽ Greek and Coptic (without ';')
                | '\u037F'..'\u1FFE'    // See note 1.
                | '\u200C'..'\u200D'    // zero width non-joiner and joiner
                | '\u2070'..'\u218F'    // See note 2.
                | '\u2C00'..'\u2FEF'    // See note 3.
                | '\u3001'..'\uD7FF'    // See note 4.
                | '\uF900'..'\uFDCF'    // See note 5.
                | '\uFDF0'..'\uFFFD'    // See note 6.
                | '\u{10000}'..'\u{EFFFF}'; // See note 7.

// See: https://unicode-table.com/en/blocks/

// Note 1: Greek, Coptic, Cyrillic, Cyrillic Supplement, Armenian, Hebrew,
//         Arabic, Syriac, Arabic Supplement, Thaana, NKo, Samaritan, Mandaic,
//         Syriac Supplement, Arabic Extended-A, Devanagari, Bengali, Gurmukhi,
//         Gujarati, Oriya, Tamil, Telugu, Kannada, Malayalam, Sinhala, Thai,
//         Lao, Tibetan, Myanmar, Georgian, Hangul Jamo, Ethiopic, Ethiopic
//         Supplement, Cherokee, Unified Canadian Aboriginal Syllabics, Ogham,
//         Runic, Tagalog, Hanunoo, Buhid, Tagbanwa, Khmer, Mongolian, Unified
//         Canadian Aboriginal Syllabics Extended, Limbu, Tai Le, New Tai Lue,
//         Khmer Symbols, Buginese, Tai Tham, Combining Diacritical Marks
//         Extended, Balinese, Sundanese, Batak, Lepcha, Ol Chiki, Cyrillic
//         Extended C, Georgian Extended, Sundanese Supplement, Vedic
//         Extensions, Phonetic Extensions, Phonetic Extensions Supplement,
//         Combining Diacritical Marks Supplement, Latin Extended Additional,
//         Greek Extended
//
// Note 2: Superscripts and Subscripts, Currency Symbols, Combining Diacritical
//         Marks for Symbols, Letterlike Symbols, Number Forms (e.g. Roman
//         numbers)
//
// Note 3: Glagolitic, Latin Extended-C, Coptic, Georgian Supplement, Tifinagh,
//         Ethiopic Extended, Cyrillic Extended-A, Supplemental Punctuation,
//         CJK Radicals Supplement, Kangxi Radicals
//
// Note 4: CJK Symbols and Punctuation Hiragana, Katakana, Bopomofo, Hangul
//         Compatibility Jamo, Kanbun, Bopomofo Extended, CJK Strokes, Katakana
//         Phonetic Extensions, Enclosed CJK Letters and Months, CJK
//         Compatibility, CJK Unified Ideographs Extension A, Yijing Hexagram
//         Symbols, CJK Unified Ideographs, Yi Syllables, Yi Radicals, Lisu,
//         Vai, Cyrillic Extended-B, Bamum, Modifier Tone Letters, Latin
//         Extended-D, Syloti Nagri, Common Indic Number Forms, Phags-pa,
//         Saurashtra, Devanagari Extended, Kayah Li, Rejang, Hangul Jamo
//         Extended-A, Javanese, Myanmar Extended-B, Cham, Myanmar Extended-A,
//         Tai Viet, Meetei Mayek Extensions, Ethiopic Extended-A, Latin
//         Extended-E, Cherokee Supplement, Meetei Mayek, Hangul Syllables,
//         Hangul Jamo Extended-B
//
// Note 5: CJK Compatibility Ideographs, Alphabetic Presentation Forms,
//         Arabic Presentation Forms-A
//
// Note 6: Arabic Presentation Forms-A, Variation Selectors, Vertical Forms,
//         Combining Half Marks, CJK Compatibility Forms, Small Form Variants,
//         Arabic Presentation Forms-B, Halfwidth and Fullwidth Forms, Specials
//
// Note 7: Linear B Syllabary, Linear B Ideograms, Aegean Numbers, Ancient
//         Greek Numbers, Ancient Symbols, Phaistos Disc, Lycian, Carian,
//         Coptic Epact Numbers, Old Italic, Gothic, Old Permic, Ugaritic, Old
//         Persian, Deseret, Shavian, Osmanya, Osage, Elbasan, Caucasian
//         Albanian, Linear A, Cypriot Syllabary, Imperial Aramaic, Palmyrene,
//         Nabataean, Hatran, Phoenician, Lydian, Meroitic Hieroglyphs,
//         Meroitic Cursive, Kharoshthi, Old South Arabian, Old North Arabian,
//         Manichaean, Avestan, Inscriptional Parthian, Inscriptional Pahlavi,
//         Psalter Pahlavi, Old Turkic, Old Hungarian, Hanifi Rohingya, Rumi
//         Numeral Symbols, Yezidi, Old Sogdian, Sogdian, Chorasmian, Elymaic,
//         Brahmi, Kaithi, Sora Sompeng, Chakma, Mahajani, Sharada, Sinhala
//         Archaic Numbers, Khojki, Multani, Khudawadi, Grantha, Newa, Tirhuta,
//         Siddham, Modi, Mongolian Supplement, Takri, Ahom, Dogra, Warang Citi,
//         Dives Akuru, Nandinagari, Zanabazar Square, Soyombo, Pau Cin Hau,
//         Bhaiksuki, Marchen, Masaram Gondi, Gunjala Gondi, Makasar, Lisu
//         Supplement, Tamil Supplement, Cuneiform, Cuneiform Numbers and
//         Punctuation, Early Dynastic Cuneiform, Egyptian Hieroglyphs,
//         Egyptian Hieroglyph Format Controls, Anatolian Hieroglyphs,Bamum
//         Supplement, Mro, Bassa Vah, Pahawh Hmong, Medefaidrin, Miao,
//         Ideographic Symbols and Punctuation, Tangut, Tangut Components,
//         Khitan Small Script, Tangut Supplement, Kana Supplement, Kana
//         Extended-A, Small Kana Extension, Nushu, Duployan, Shorthand Format
//         Controls, Byzantine Musical Symbols, Musical Symbols, Ancient Greek
//         Musical Notation, Mayan Numerals, Tai Xuan Jing Symbols, Counting
//         Rod Numerals, Mathematical Alphanumeric Symbols, Sutton SignWriting,
//         Glagolitic Supplement, Nyiakeng Puachue Hmong, Wancho, Mende Kikakui,
//         Adlam, Indic Siyaq Numbers, Ottoman Siyaq Numbers, Arabic
//         Mathematical Alphabetic Symbols, Mahjong Tiles, Domino Tiles,
//         Playing Cards, Enclosed Alphanumeric Supplement, Enclosed Ideographic
//         Supplement, Miscellaneous Symbols and Pictographs, Emoticons (Emoji),
//         Ornamental Dingbats, Transport and Map Symbols, Alchemical Symbols,
//         Geometric Shapes Extended, Supplemental Arrows-C, Supplemental
//         Symbols and Pictographs, Chess Symbols, Symbols and Pictographs
//         Extended-A, Symbols for Legacy Computing, CJK Unified Ideographs
//         Extension B, CJK Unified Ideographs Extension C, CJK Unified
//         Ideographs Extension D, CJK Unified Ideographs Extension E, CJK
//         Unified Ideographs Extension F, CJK Compatibility Ideographs
//         Supplement, CJK Unified Ideographs Extension G, Tags, Variation
//         Selectors Supplement