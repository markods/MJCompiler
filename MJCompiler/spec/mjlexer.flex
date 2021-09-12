// ________________________________________________________________________________________________
// import section
package rs.ac.bg.etf.pp1;


// ________________________________________________________________________________________________
// directive section
%%

%class Lexer
%apiprivate
%cup
// %unicode
%line
%column



// methods
%{
    private int tokenIdx = 0;

    // create a token from the given token type
    private Token new_token( int tokenCode )
    {
        return new_token( tokenCode, null );
    }
    // create a token from the given token type and its value
    private Token new_token( int tokenCode, Object value )
    {
        return new Token( tokenCode, tokenIdx++, yyline+1, yycolumn, value );
    }
%}



// classes
Newline    = \r|\n|\r\n
NotNewline = [^\r\n]
Whitespace = [ \t\f]+

// different types of comments
// +   the line comment can be on the last line of the file, therefore not ending with a newline
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
{Newline}           { return new_token( TokenCode.newline, yytext() ); }
{Whitespace}        { return new_token( TokenCode.whitespace, yytext() ); }
{LineComment}       { return new_token( TokenCode.line_comment, yytext() ); }
{MultilineComment}  { return new_token( TokenCode.multi_comment, yytext() ); }
<<EOF>>             { return new_token( TokenCode.EOF ); }



// keywords
"program"    { return new_token( TokenCode.PROGRAM_K, yytext() ); }
"class"      { return new_token( TokenCode.CLASS_K, yytext() ); }
"enum"       { return new_token( TokenCode.invalid /*TokenCode.ENUM_K*/, yytext() ); }
"extends"    { return new_token( TokenCode.EXTENDS_K, yytext() ); }

"static"     { return new_token( TokenCode.STATIC_K, yytext() ); }
"const"      { return new_token( TokenCode.CONST_K, yytext() ); }
"void"       { return new_token( TokenCode.VOID_K, yytext() ); }
"null"       { return new_token( TokenCode.NULL_K, yytext() ); }

"if"         { return new_token( TokenCode.IF_K, yytext() ); }
"else"       { return new_token( TokenCode.ELSE_K, yytext() ); }
"switch"     { return new_token( TokenCode.SWITCH_K, yytext() ); }
"case"       { return new_token( TokenCode.CASE_K, yytext() ); }
"default"    { return new_token( TokenCode.invalid /*TokenCode.DEFAULT_K*/, yytext() ); }
"break"      { return new_token( TokenCode.BREAK_K, yytext() ); }
"continue"   { return new_token( TokenCode.CONTINUE_K, yytext() ); }
"return"     { return new_token( TokenCode.RETURN_K, yytext() ); }

"do"         { return new_token( TokenCode.DO_K, yytext() ); }
"while"      { return new_token( TokenCode.WHILE_K, yytext() ); }

"new"        { return new_token( TokenCode.NEW_K, yytext() ); }
"print"      { return new_token( TokenCode.PRINT_K, yytext() ); }
"read"       { return new_token( TokenCode.READ_K, yytext() ); }



// operators
"++"         { return new_token( TokenCode.plusplus, yytext() ); }
"--"         { return new_token( TokenCode.minusminus, yytext() ); }

"+"          { return new_token( TokenCode.plus, yytext() ); }
"-"          { return new_token( TokenCode.minus, yytext() ); }
"*"          { return new_token( TokenCode.mul, yytext() ); }
"/"          { return new_token( TokenCode.div, yytext() ); }
"%"          { return new_token( TokenCode.perc, yytext() ); }

"=="         { return new_token( TokenCode.eq, yytext() ); }
"!="         { return new_token( TokenCode.ne, yytext() ); }
">"          { return new_token( TokenCode.gt, yytext() ); }
">="         { return new_token( TokenCode.ge, yytext() ); }
"<"          { return new_token( TokenCode.lt, yytext() ); }
"<="         { return new_token( TokenCode.le, yytext() ); }
"&&"         { return new_token( TokenCode.and, yytext() ); }
"||"         { return new_token( TokenCode.or, yytext() ); }

"="          { return new_token( TokenCode.assign, yytext() ); }

";"          { return new_token( TokenCode.semicol, yytext() ); }
","          { return new_token( TokenCode.comma, yytext() ); }
"."          { return new_token( TokenCode.dot, yytext() ); }
"{"          { return new_token( TokenCode.lbrace, yytext() ); }
"}"          { return new_token( TokenCode.rbrace, yytext() ); }
"("          { return new_token( TokenCode.lparen, yytext() ); }
")"          { return new_token( TokenCode.rparen, yytext() ); }
"["          { return new_token( TokenCode.lbracket, yytext() ); }
"]"          { return new_token( TokenCode.rbracket, yytext() ); }
// "?"       { return new_token( TokenCode.qmark, yytext() ); }
":"          { return new_token( TokenCode.colon, yytext() ); }



// constants
{IntLiteral}    { return new_token( TokenCode.int_lit, Integer.parseInt( yytext() ) ); }
{BoolLiteral}   { return new_token( TokenCode.bool_lit, Boolean.parseBoolean( yytext() ) ); }
{CharLiteral}   { return new_token( TokenCode.char_lit, yytext().charAt( 1 ) ); }

// identifiers
{Identifier} 	      { return new_token( TokenCode.ident, yytext() ); }
{InvalidIdentifier}   { return new_token( TokenCode.invalid, yytext() ); }

// error fallback (for unrecognized token)
[^]             { return new_token( TokenCode.invalid, yytext() ); }






