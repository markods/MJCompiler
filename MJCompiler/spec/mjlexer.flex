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
"program"    { return new_symbol( sym.PROGRAM_K ); }
"class"      { return new_symbol( sym.CLASS_K ); }
"enum"       { return new_symbol( sym.error /*sym.ENUM_K*/, new_error( "Enum not implemented" ) ); }
"extends"    { return new_symbol( sym.EXTENDS_K ); }

"const"      { return new_symbol( sym.CONST_K ); }
"void"       { return new_symbol( sym.VOID_K ); }

"if"         { return new_symbol( sym.IF_K ); }
"else"       { return new_symbol( sym.ELSE_K ); }
"switch"     { return new_symbol( sym.SWITCH_K ); }
"case"       { return new_symbol( sym.CASE_K ); }
"break"      { return new_symbol( sym.BREAK_K ); }
"continue"   { return new_symbol( sym.CONTINUE_K ); }
"return"     { return new_symbol( sym.RETURN_K ); }

"do"         { return new_symbol( sym.DO_K ); }
"while"      { return new_symbol( sym.WHILE_K ); }

"new"        { return new_symbol( sym.NEW_K ); }
"print"      { return new_symbol( sym.PRINT_K ); }
"read"       { return new_symbol( sym.READ_K ); }



// operators
"+"          { return new_symbol( sym.plus ); }
"-"          { return new_symbol( sym.minus ); }
"*"          { return new_symbol( sym.mul ); }
"/"          { return new_symbol( sym.div ); }
"%"          { return new_symbol( sym.perc ); }

"=="         { return new_symbol( sym.eq ); }
"!="         { return new_symbol( sym.ne ); }
">"          { return new_symbol( sym.gt ); }
">="         { return new_symbol( sym.ge ); }
"<"          { return new_symbol( sym.lt ); }
"<="         { return new_symbol( sym.le ); }
"&&"         { return new_symbol( sym.and ); }
"||"         { return new_symbol( sym.or ); }

"="          { return new_symbol( sym.assign ); }
"++"         { return new_symbol( sym.plusplus ); }
"‐‐"         { return new_symbol( sym.minusminus ); }

";"          { return new_symbol( sym.semicol ); }
","          { return new_symbol( sym.comma ); }
"."          { return new_symbol( sym.dot ); }
"{"          { return new_symbol( sym.lbrace ); }
"}"          { return new_symbol( sym.rbrace ); }
"("          { return new_symbol( sym.lparen ); }
")"          { return new_symbol( sym.rparen ); }
"["          { return new_symbol( sym.lbracket ); }
"]"          { return new_symbol( sym.rbracket ); }
"?"          { return new_symbol( sym.qmark ); }
":"          { return new_symbol( sym.colon ); }



// constants
{IntLiteral}    { return new_symbol( sym.int_lit, Integer.parseInt( yytext() ) ); }
{BoolLiteral}   { return new_symbol( sym.bool_lit, Boolean.parseBoolean( yytext() ) ); }
{CharLiteral}   { return new_symbol( sym.char_lit, yytext().charAt( 1 ) ); }

// identifiers
{Identifier} 	{ return new_symbol( sym.ident, yytext() ); }

// error fallback (for unrecognized token)
[^]             { return new_symbol( sym.error, new_error( "Syntax error" ) ); }






