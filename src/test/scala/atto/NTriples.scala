package atto

import java.net.URI
import scalaz._
import Scalaz._
import spire.math.UByte

// http://www.w3.org/TR/rdf-testcases/#ntriples
trait NTriples {
  import Atto._

  type Comment = String
  type Line = Comment \/ Triple

  case class Triple(subj: Subject, pred: Predicate, obj: Obj)

  sealed trait Subject
  sealed trait Predicate
  sealed trait Obj

  case class UriRef(uri: URI) extends Subject with Predicate with Obj
  case class NodeID(id: String) extends Subject with Obj

  sealed trait Literal extends Obj
  case class LangString(string: String, lang: Option[String]) extends Literal
  case class DatatypeString(string: String, ref: UriRef) extends Literal

  // triples filters out the comments
  lazy val triples: Parser[List[Triple]] =
    ntripleDoc.map(_.map(_.toOption).flatten)

  // ntripleDoc ::= line* 
  lazy val ntripleDoc: Parser[List[Line]] =
    many(line).map(_.flatten)

  // line ::= ws* ( comment | triple )? eoln  
  lazy val line: Parser[Option[Line]] =
    many(ws) ~> opt(comment || triple) <~ eoln

  // comment ::= '#' ( character - ( cr | lf ) )*  
  lazy val comment: Parser[Comment] =
    char('#') ~> stringOf(elem(c => c != 0x0D && c != 0x0A))

  // triple ::= subject ws+ predicate ws+ object ws* '.' ws*  
  lazy val triple: Parser[Triple] =
    for { // N.B. could use applicative here but monad syntax is more clear i think
      s <- subject
      p <- many1(ws) ~> predicate
      o <- many1(ws) ~> obj <~ many(ws) <~ char('.') <~ many(ws)
    } yield Triple(s, p, o) 

  // subject ::= uriref | nodeID 
  lazy val subject: Parser[Subject] =
    uriref | nodeID

  // predicate ::= uriref  
  lazy val predicate: Parser[Predicate] =
    uriref

  // object ::= uriref | nodeID | literal 
  lazy val obj: Parser[Obj] =
    uriref | nodeID | literal

  // uriref ::= '<' absoluteURI '>' 
  lazy val uriref: Parser[UriRef] =
    (char('<') ~> absoluteURI <~ char('>')).map(UriRef.apply)

  // nodeID ::= '_:' name 
  lazy val nodeID: Parser[NodeID] =
    (string("_:") ~> name).map(NodeID.apply)

  // literal ::= langString | datatypeString 
  lazy val literal: Parser[Literal] =
    datatypeString | langString // N.B. had to flip these

  // langString ::= '"' string '"' ( '@' language )?  
  lazy val langString: Parser[LangString] =
    ((char('"') ~> str <~ char('"')) |@| opt(char('@') ~> language))(LangString.apply)

  // datatypeString ::= '"' string '"' '^^' uriref  
  lazy val datatypeString: Parser[DatatypeString] =
    ((char('"') ~> str <~ char('"')) |@| string("^^") ~> uriref)(DatatypeString.apply)

  // language ::= [a-z]+ ('-' [a-z0-9]+ )* encoding a language tag.  
  lazy val language: Parser[String] =
    stringOf(letterOrDigit | char('-')) 

  // ws ::= space | tab 
  lazy val ws: Parser[Char] =
    space | tab

  // eoln ::= cr | lf | cr lf 
  lazy val eoln: Parser[Unit] =
    (cr | lf | cr ~ lf) map (_ => ())

  // space ::= #x20 /* US-ASCII space - decimal 32 */  
  lazy val space: Parser[Char] =
    char(0x20)

  // cr ::= #xD /* US-ASCII carriage return - decimal 13 */ 
  lazy val cr: Parser[Char] =
    char(0x0D)

  // lf ::= #xA /* US-ASCII line feed - decimal 10 */ 
  lazy val lf: Parser[Char] =
    char(0x0A)

  // tab ::= #x9 /* US-ASCII horizontal tab - decimal 9 */ 
  lazy val tab: Parser[Char] =
    char(0x09)

  // character* with escapes as defined in section Strings
  lazy val str: Parser[String] =
    stringOf(character)

  // name ::= [A-Za-z][A-Za-z0-9]*  
  lazy val name: Parser[String] =
    (letter |@| stringOf(letterOrDigit))(_ + _)

  // absoluteURI ::= character+ with escapes as defined in section URI References  
  lazy val absoluteURI: Parser[URI] =
    stringOf1(elem(c => c != '>')).flatMap { s =>
      try { ok(new URI(s)) } catch { case e: Exception => err(e.toString) }
    }

  // character ::= [#x20-#x7E] /* US-ASCII space to decimal 126, with escapes */ 
  lazy val character: Parser[Char] =
    esc('t', '\t')        |
    esc('n', '\n')        |
    esc('r', '\r')        |
    charRange(' ' to '!') |
    esc('"', '"')         |
    charRange('#' to '[') |
    esc('\\', '\\')       |
    charRange(']' to '~')
  
  // combinator for backslash- escaped chars
  def esc(lit: Char, value: Char): Parser[Char] =
    char('\\') ~> char(lit).map(_ => value)

}


object NTriples extends NTriples {

  val test = """#
# Copyright World Wide Web Consortium, (Massachusetts Institute of
# Technology, Institut National de Recherche en Informatique et en
# Automatique, Keio University).
#
# All Rights Reserved.
#
# Please see the full Copyright clause at
# <http://www.w3.org/Consortium/Legal/copyright-software.html>
#
# Test file with a variety of legal N-Triples
#
# Dave Beckett - http://purl.org/net/dajobe/
# 
# $Id: test.nt,v 1.7 2003/10/06 15:52:19 dbeckett2 Exp $
# 
#####################################################################

# comment lines
           # comment line after whitespace
# empty blank line, then one with spaces and tabs

          
<http://example.org/resource1> <http://example.org/property> <http://example.org/resource2> .
_:anon <http://example.org/property> <http://example.org/resource2> .
<http://example.org/resource2> <http://example.org/property> _:anon .
# spaces and tabs throughout:
   <http://example.org/resource3>    <http://example.org/property>   <http://example.org/resource2>   .  

# line ending with CR NL (ASCII 13, ASCII 10)
<http://example.org/resource4> <http://example.org/property> <http://example.org/resource2> .

# 2 statement lines separated by single CR (ASCII 10)
<http://example.org/resource5> <http://example.org/property> <http://example.org/resource2> .
<http://example.org/resource6> <http://example.org/property> <http://example.org/resource2> .


# All literal escapes
<http://example.org/resource7> <http://example.org/property> "simple literal" .
<http://example.org/resource8> <http://example.org/property> "backslash:\\" .
<http://example.org/resource9> <http://example.org/property> "dquote:\"" .
<http://example.org/resource10> <http://example.org/property> "newline:\n" .
<http://example.org/resource11> <http://example.org/property> "return\r" .
<http://example.org/resource12> <http://example.org/property> "tab:\t" .

# Space is optional before final .
<http://example.org/resource13> <http://example.org/property> <http://example.org/resource2>.
<http://example.org/resource14> <http://example.org/property> "x".
<http://example.org/resource15> <http://example.org/property> _:anon.

# \\u and \\U escapes
# latin small letter e with acute symbol \u00E9 - 3 UTF-8 bytes #xC3 #A9
#<http://example.org/resource16> <http://example.org/property> "\u00E9" .
# Euro symbol \u20ac  - 3 UTF-8 bytes #xE2 #x82 #xAC
#<http://example.org/resource17> <http://example.org/property> "\u20AC" .
# resource18 test removed
# resource19 test removed
# resource20 test removed

# XML Literals as Datatyped Literals
<http://example.org/resource21> <http://example.org/property> ""^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource22> <http://example.org/property> " "^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource23> <http://example.org/property> "x"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource23> <http://example.org/property> "\""^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource24> <http://example.org/property> "<a></a>"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource25> <http://example.org/property> "a <b></b>"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource26> <http://example.org/property> "a <b></b> c"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource26> <http://example.org/property> "a\n<b></b>\nc"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
<http://example.org/resource27> <http://example.org/property> "chat"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
# resource28 test removed 2003-08-03
# resource29 test removed 2003-08-03

# Plain literals with languages
<http://example.org/resource30> <http://example.org/property> "chat"@fr .
<http://example.org/resource31> <http://example.org/property> "chat"@en .

# Typed Literals
<http://example.org/resource32> <http://example.org/property> "abc"^^<http://example.org/datatype1> .
# resource33 test removed 2003-08-03
"""

}

