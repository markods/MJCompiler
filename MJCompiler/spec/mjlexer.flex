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
    private CompilerError new_error( String message )
    {
        return new CompilerError( yyline+1, message, CompilerError.CompilerErrorType.LEXICAL_ERROR );
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
"program"    { return new_symbol( sym.PROGRAM_K, yytext() ); }
"class"      { return new_symbol( sym.CLASS_K, yytext() ); }
"enum"       { return new_symbol( sym.error /*sym.ENUM_K*/, new_error( "Enum not implemented" ) ); }
"extends"    { return new_symbol( sym.EXTENDS_K, yytext() ); }

"const"      { return new_symbol( sym.CONST_K, yytext() ); }
"void"       { return new_symbol( sym.VOID_K, yytext() ); }

"if"         { return new_symbol( sym.IF_K, yytext() ); }
"else"       { return new_symbol( sym.ELSE_K, yytext() ); }
"switch"     { return new_symbol( sym.SWITCH_K, yytext() ); }
"case"       { return new_symbol( sym.CASE_K, yytext() ); }
"break"      { return new_symbol( sym.BREAK_K, yytext() ); }
"continue"   { return new_symbol( sym.CONTINUE_K, yytext() ); }
"return"     { return new_symbol( sym.RETURN_K, yytext() ); }

"do"         { return new_symbol( sym.DO_K, yytext() ); }
"while"      { return new_symbol( sym.WHILE_K, yytext() ); }

"new"        { return new_symbol( sym.NEW_K, yytext() ); }
"print"      { return new_symbol( sym.PRINT_K, yytext() ); }
"read"       { return new_symbol( sym.READ_K, yytext() ); }



// operators
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
"++"         { return new_symbol( sym.plusplus, yytext() ); }
"‐‐"         { return new_symbol( sym.minusminus, yytext() ); }

";"          { return new_symbol( sym.semicol, yytext() ); }
","          { return new_symbol( sym.comma, yytext() ); }
"."          { return new_symbol( sym.dot, yytext() ); }
"{"          { return new_symbol( sym.lbrace, yytext() ); }
"}"          { return new_symbol( sym.rbrace, yytext() ); }
"("          { return new_symbol( sym.lparen, yytext() ); }
")"          { return new_symbol( sym.rparen, yytext() ); }
"["          { return new_symbol( sym.lbracket, yytext() ); }
"]"          { return new_symbol( sym.rbracket, yytext() ); }
"?"          { return new_symbol( sym.qmark, yytext() ); }
":"          { return new_symbol( sym.colon, yytext() ); }



// constants
{IntLiteral}    { return new_symbol( sym.int_lit, Integer.parseInt( yytext() ) ); }
{BoolLiteral}   { return new_symbol( sym.bool_lit, Boolean.parseBoolean( yytext() ) ); }
{CharLiteral}   { return new_symbol( sym.char_lit, yytext().charAt( 1 ) ); }

// identifiers
{Identifier} 	{ return new_symbol( sym.ident, yytext() ); }

// error fallback (for unrecognized token)
[^]             { return new_symbol( sym.error, new_error( "Syntax error" ) ); }






