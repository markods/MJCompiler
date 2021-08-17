// ________________________________________________________________________________________________
// import section
package rs.ac.bg.etf.pp1;

import org.apache.log4j.*;
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
	protected static Logger logger = Logger.getLogger( Yylex.class );

    // create a symbol from the given symbol type
    private Symbol new_symbol( int type )
    {
        return new Symbol( type, yyline+1, yycolumn );
    }
	
    // create a symbol from the given symbol type and its value
    private Symbol new_symbol( int type, Object value )
    {
        return new Symbol( type, yyline+1, yycolumn, value );
    }

    // create a lexical error object
    private void report_error( String message )
    {
        Compiler.errorList().add( new CompilerError( yyline+1, yycolumn, message, CompilerError.CompilerErrorType.LEXICAL_ERROR ) );
        logger.error( Compiler.errorList().getLast() );
    }

    // create a lexical error object
    private void report_error()
    {
        String message = String.format( "Invalid token\n```%s```", yytext() );
        report_error( message );
    }
%}



// classes
Newline    = \r|\n|\r\n
NotNewline = [^\r\n]
Whitespace = [ \t\f]+

// different types of comments
// +   the line comment can be on the last line of the file, therefore not ending with a newline
Comment          = {LineComment} | {MultilineComment}
LineComment      = "//" {NotNewline}* {Newline}?
MultilineComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"

IntLiteral = 0 | [1-9][0-9]*
BoolLiteral = true | false
CharLiteral = "'"."'"

Identifier        = ([:jletter:]|_) ([:jletterdigit:]|_)*
InvalidIdentifier = [0-9]           ([:jletterdigit:]|_)*





// ________________________________________________________________________________________________
// regex section
%%

// send newlines, whitespaces and comments to the parser
// +   the parser will filter them out (used for better error reporting)
{Newline}    { return new_symbol( sym.ignore, yytext() ); }
{Whitespace} { return new_symbol( sym.ignore, yytext() ); }
{Comment}    { return new_symbol( sym.ignore, yytext() ); }



// keywords
"program"    { return new_symbol( sym.PROGRAM_K, yytext() ); }
"class"      { return new_symbol( sym.CLASS_K, yytext() ); }
"enum"       { report_error( "Keyword not implemented\n```enum```" ); return new_symbol( sym.error /*sym.ENUM_K*/, yytext() ); }
"extends"    { return new_symbol( sym.EXTENDS_K, yytext() ); }

"static"     { return new_symbol( sym.STATIC_K, yytext() ); }
"const"      { return new_symbol( sym.CONST_K, yytext() ); }
"void"       { return new_symbol( sym.VOID_K, yytext() ); }

"if"         { return new_symbol( sym.IF_K, yytext() ); }
"else"       { return new_symbol( sym.ELSE_K, yytext() ); }
"switch"     { return new_symbol( sym.SWITCH_K, yytext() ); }
"case"       { return new_symbol( sym.CASE_K, yytext() ); }
"default"    { report_error( "Keyword not implemented\n```default```" ); return new_symbol( sym.error /*sym.DEFAULT_K*/, yytext() ); }
"break"      { return new_symbol( sym.BREAK_K, yytext() ); }
"continue"   { return new_symbol( sym.CONTINUE_K, yytext() ); }
"return"     { return new_symbol( sym.RETURN_K, yytext() ); }

"do"         { return new_symbol( sym.DO_K, yytext() ); }
"while"      { return new_symbol( sym.WHILE_K, yytext() ); }

"new"        { return new_symbol( sym.NEW_K, yytext() ); }
"print"      { return new_symbol( sym.PRINT_K, yytext() ); }
"read"       { return new_symbol( sym.READ_K, yytext() ); }



// operators
"++"         { return new_symbol( sym.plusplus, yytext() ); }
"--"         { return new_symbol( sym.minusminus, yytext() ); }

"+"          { return new_symbol( sym.plus, yytext() ); }
"-"          { return new_symbol( sym.minus, yytext() ); }
"*"          { return new_symbol( sym.mul, yytext() ); }
"/"          { return new_symbol( sym.div, yytext() ); }
"%"          { return new_symbol( sym.perc, yytext() ); }

"=="         { return new_symbol( sym.eq, yytext() ); }
"!="         { return new_symbol( sym.ne, yytext() ); }
">"          { return new_symbol( sym.gt, yytext() ); }
">="         { return new_symbol( sym.ge, yytext() ); }
"<"          { return new_symbol( sym.lt, yytext() ); }
"<="         { return new_symbol( sym.le, yytext() ); }
"&&"         { return new_symbol( sym.and, yytext() ); }
"||"         { return new_symbol( sym.or, yytext() ); }

"="          { return new_symbol( sym.assign, yytext() ); }

";"          { return new_symbol( sym.semicol, yytext() ); }
","          { return new_symbol( sym.comma, yytext() ); }
"."          { return new_symbol( sym.dot, yytext() ); }
"{"          { return new_symbol( sym.lbrace, yytext() ); }
"}"          { return new_symbol( sym.rbrace, yytext() ); }
"("          { return new_symbol( sym.lparen, yytext() ); }
")"          { return new_symbol( sym.rparen, yytext() ); }
"["          { return new_symbol( sym.lbracket, yytext() ); }
"]"          { return new_symbol( sym.rbracket, yytext() ); }
// "?"       { return new_symbol( sym.qmark, yytext() ); }
":"          { return new_symbol( sym.colon, yytext() ); }
<<EOF>>      { return new_symbol( sym.EOF ); }



// constants
{IntLiteral}    { return new_symbol( sym.int_lit, Integer.parseInt( yytext() ) ); }
{BoolLiteral}   { return new_symbol( sym.bool_lit, Boolean.parseBoolean( yytext() ) ); }
{CharLiteral}   { return new_symbol( sym.char_lit, yytext().charAt( 1 ) ); }

// identifiers
{Identifier} 	      { return new_symbol( sym.ident, yytext() ); }
{InvalidIdentifier}   { report_error(); return new_symbol( sym.error, yytext() ); }

// error fallback (for unrecognized token)
[^]             { report_error(); return new_symbol( sym.error, yytext() ); }






