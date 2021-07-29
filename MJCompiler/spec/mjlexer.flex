// ________________________________________________________________________________________________
// import section
package rs.ac.bg.etf.pp1;
import java_cup.runtime.Symbol;


// ________________________________________________________________________________________________
// directive section
%%

%class Yylex
// %unicode
%cup
%line
%column



// methods
%{

    // ukljucivanje informacije o poziciji tokena
    private Symbol new_symbol( int type )
    {
        return new Symbol( type, yyline+1, yycolumn );
    }
	
    // ukljucivanje informacije o poziciji tokena
    private Symbol new_symbol( int type, Object value )
    {
        return new Symbol( type, yyline+1, yycolumn, value );
    }

%}

%eofval{
    return new_symbol( sym.EOF );
%eofval}



// classes
LineTerminator   = \r|\n|\r\n
CommentCharacter = [^\r\n]
Whitespace       = {LineTerminator} | [ \t\f]

// comment can be the last line of the file, without line terminator
Comment          = {LineComment} | {MultilineComment}
LineComment      = "//" {CommentCharacter}* {LineTerminator}?
MultilineComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"

IntLiteral = 0 | [1-9][0-9]*
BoolLiteral = true | false
CharLiteral = "'"."'"

Identifier = [:jletter:] ([:jletterdigit:]|_)*





// ________________________________________________________________________________________________
// regex section
%%

// ignore whitespaces and comments
{Whitespace} { }
{Comment} { }



// keywords
"program"    { return new_symbol( sym.K_PROGRAM, yytext() ); }
"class"      { return new_symbol( sym.K_CLASS, yytext() ); }
"enum"       { return new_symbol( sym.K_ENUM, yytext() ); }
"extends"    { return new_symbol( sym.K_EXTENDS, yytext() ); }

"const"      { return new_symbol( sym.K_CONST, yytext() ); }
"void"       { return new_symbol( sym.K_VOID, yytext() ); }
"int"        { return new_symbol( sym.K_INT, yytext() ); }
"bool"       { return new_symbol( sym.K_BOOL, yytext() ); }
"char"       { return new_symbol( sym.K_CHAR, yytext() ); }

"if"         { return new_symbol( sym.K_IF, yytext() ); }
"else"       { return new_symbol( sym.K_ELSE, yytext() ); }
"switch"     { return new_symbol( sym.K_SWITCH, yytext() ); }
"case"       { return new_symbol( sym.K_CASE, yytext() ); }
"default"    { return new_symbol( sym.K_DEFAULT, yytext() ); }
"break"      { return new_symbol( sym.K_BREAK, yytext() ); }
"continue"   { return new_symbol( sym.K_CONTINUE, yytext() ); }
"return"     { return new_symbol( sym.K_RETURN, yytext() ); }

"do"         { return new_symbol( sym.K_DO, yytext() ); }
"while"      { return new_symbol( sym.K_WHILE, yytext() ); }

"new"        { return new_symbol( sym.K_NEW, yytext() ); }
"print"      { return new_symbol( sym.K_PRINT, yytext() ); }
"read"       { return new_symbol( sym.K_READ, yytext() ); }



// operators
"+"          { return new_symbol( sym.O_PLUS, yytext() ); }
"-"          { return new_symbol( sym.O_MINUS, yytext() ); }
"*"          { return new_symbol( sym.O_MUL, yytext() ); }
"/"          { return new_symbol( sym.O_DIV, yytext() ); }
"%"          { return new_symbol( sym.O_PERC, yytext() ); }

"=="         { return new_symbol( sym.O_EQUAL, yytext() ); }
"!="         { return new_symbol( sym.O_NOT_EQUAL, yytext() ); }
">"          { return new_symbol( sym.O_GREAT, yytext() ); }
">="         { return new_symbol( sym.O_GREAT_EQUALS, yytext() ); }
"<"          { return new_symbol( sym.O_LESS, yytext() ); }
"<="         { return new_symbol( sym.O_LESS_EQUALS, yytext() ); }
"&&"         { return new_symbol( sym.O_AND, yytext() ); }
"||"         { return new_symbol( sym.O_OR, yytext() ); }

"="          { return new_symbol( sym.O_ASSIGN, yytext() ); }
"++"         { return new_symbol( sym.O_PLUS_PLUS, yytext() ); }
"‐‐"         { return new_symbol( sym.O_MINUS_MINUS, yytext() ); }

";"          { return new_symbol( sym.O_SEMICOLON, yytext() ); }
","          { return new_symbol( sym.O_COMMA, yytext() ); }
"."          { return new_symbol( sym.O_DOT, yytext() ); }
"("          { return new_symbol( sym.O_PAREN_OPEN, yytext() ); }
")"          { return new_symbol( sym.O_PAREN_CLOSE, yytext() ); }
"["          { return new_symbol( sym.O_BRACKET_OPEN, yytext() ); }
"]"          { return new_symbol( sym.O_BRACKET_CLOSE, yytext() ); }
"{"          { return new_symbol( sym.O_BRACE_OPEN, yytext() ); }
"}"          { return new_symbol( sym.O_BRACE_CLOSE, yytext() ); }
"?"          { return new_symbol( sym.O_QUESTION_MARK, yytext() ); }
":"          { return new_symbol( sym.O_COLON, yytext() ); }



// constants
{IntLiteral}    { return new_symbol( sym.C_INT, Integer.parseInt( yytext() ) ); }
{BoolLiteral}   { return new_symbol( sym.C_BOOL, Boolean.parseBoolean( yytext() ) ); }
{CharLiteral}   { return new_symbol( sym.C_CHAR, yytext().charAt( 1 ) ); }

// identifiers
{Identifier} 	{ return new_symbol( sym.IDENTIFIER, yytext() ); }



// default action (for unrecognized token)
. { System.err.println( String.format( "Leksicka greska na liniji %d kolona %d:\n\t`%s`", yyline+1, yycolumn, yytext() ) ); }






