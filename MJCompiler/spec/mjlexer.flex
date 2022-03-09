// ________________________________________________________________________________________________
// import section
package rs.ac.bg.etf.pp1;


// ________________________________________________________________________________________________
// directive section
%%

%class Lexer
%apiprivate
%unicode
%cup
// %unicode
%line
%column



// methods
%{
    // the current token's index
    private int tokenIdx = -1;

    // create a token from the given token type and its value
    private Token new_token( int tokenCode )
    {
        return new Token( tokenCode, ++tokenIdx, 1+yyline, yycolumn, yytext() );
    }
%}

// macros (user classes)
Anything   = [^]*
Newline    = \r\n|[\r\n\u2028\u2029\u000B\u000C\u0085]
NotNewline = [^\r\n\u2028\u2029\u000B\u000C\u0085]
Whitespace = [ \t\f]+





// ________________________________________________________________________________________________
// regex section
%%

// send newlines, whitespaces and comments to the parser
// +   the parser will filter them out (used for better error reporting)
{Newline}    { return new_token( TokenCode.newline ); }
{Whitespace} { return new_token( TokenCode.whitespace ); }

// different types of comments
// +   the line comment can be on the last line of the file, therefore not ending with a newline
// +   NOTE: the line comment doesn't end with a newline! (the newline is a part of the look-ahead expression -- after '/')
"/*" !( {Anything} ( {Newline}|"*/" ) {Anything} ) "*/" { return new_token( TokenCode.inline_comment ); }
"//" {NotNewline}* / {Newline}?                         { return new_token( TokenCode.line_comment ); }

// end of file
<<EOF>>      { return new_token( TokenCode.EOF ); }



// keywords
"program"    { return new_token( TokenCode.PROGRAM_K ); }
"class"      { return new_token( TokenCode.CLASS_K ); }
"struct"     { return new_token( TokenCode.invalid /*TokenCode.STRUCT_K*/ ); }
"record"     { return new_token( TokenCode.RECORD_K ); }
"enum"       { return new_token( TokenCode.invalid /*TokenCode.ENUM_K*/ ); }

"static"     { return new_token( TokenCode.STATIC_K ); }
"const"      { return new_token( TokenCode.CONST_K ); }
"void"       { return new_token( TokenCode.VOID_K ); }
"null"       { return new_token( TokenCode.NULL_K ); }

"abstract"   { return new_token( TokenCode.invalid /*TokenCode.ABSTRACT_K*/ ); }
"extends"    { return new_token( TokenCode.EXTENDS_K ); }
"implements" { return new_token( TokenCode.invalid /*TokenCode.IMPLEMENTS_K*/ ); }
"this"       { return new_token( TokenCode.THIS_K ); }
"super"      { return new_token( TokenCode.SUPER_K ); }

"if"         { return new_token( TokenCode.IF_K ); }
"else"       { return new_token( TokenCode.ELSE_K ); }
"switch"     { return new_token( TokenCode.SWITCH_K ); }
"case"       { return new_token( TokenCode.CASE_K ); }
"do"         { return new_token( TokenCode.DO_K ); }
"while"      { return new_token( TokenCode.WHILE_K ); }
"for"        { return new_token( TokenCode.invalid /*TokenCode.FOR_K*/ ); }

"default"    { return new_token( TokenCode.invalid /*TokenCode.DEFAULT_K*/ ); }
"break"      { return new_token( TokenCode.BREAK_K ); }
"continue"   { return new_token( TokenCode.CONTINUE_K ); }
"return"     { return new_token( TokenCode.RETURN_K ); }
"goto"       { return new_token( TokenCode.GOTO_K ); }

"new"        { return new_token( TokenCode.NEW_K ); }
"delete"     { return new_token( TokenCode.invalid /*TokenCode.DELETE_K*/ ); }
"print"      { return new_token( TokenCode.PRINT_K ); }
"read"       { return new_token( TokenCode.READ_K ); }



// operators
"++"         { return new_token( TokenCode.plusplus ); }
"--"         { return new_token( TokenCode.minusminus ); }

"+"          { return new_token( TokenCode.plus ); }
"-"          { return new_token( TokenCode.minus ); }
"*"          { return new_token( TokenCode.mul ); }
"/"          { return new_token( TokenCode.div ); }
"%"          { return new_token( TokenCode.perc ); }

"=="         { return new_token( TokenCode.eq ); }
"!="         { return new_token( TokenCode.ne ); }
">"          { return new_token( TokenCode.gt ); }
">="         { return new_token( TokenCode.ge ); }
"<"          { return new_token( TokenCode.lt ); }
"<="         { return new_token( TokenCode.le ); }
"&&"         { return new_token( TokenCode.and ); }
"||"         { return new_token( TokenCode.or ); }
"!"          { return new_token( TokenCode.invalid /*TokenCode.emark*/ ); }

"="          { return new_token( TokenCode.assign ); }

";"          { return new_token( TokenCode.semicol ); }
","          { return new_token( TokenCode.comma ); }
"."          { return new_token( TokenCode.dot ); }
"{"          { return new_token( TokenCode.lbrace ); }
"}"          { return new_token( TokenCode.rbrace ); }
"("          { return new_token( TokenCode.lparen ); }
")"          { return new_token( TokenCode.rparen ); }
"["          { return new_token( TokenCode.lbracket ); }
"]"          { return new_token( TokenCode.rbracket ); }
"?"          { return new_token( TokenCode.invalid /*TokenCode.qmark*/ ); }
":"          { return new_token( TokenCode.colon ); }



// constants
[:digit:]+     { return new_token( TokenCode.int_lit ); }
true | false   { return new_token( TokenCode.bool_lit ); }
"'"."'"        { return new_token( TokenCode.char_lit ); }

// FIX: support unicode identifiers (for some reason this doesn't work as expected in jflex)
// identifiers
([:letter:]|_) ([:letter:]|[:digit:]|_)*   { return new_token( TokenCode.ident ); }
[:digit:]      ([:letter:]|[:digit:]|_)*   { return new_token( TokenCode.invalid ); }

// error fallback (for unrecognized token)
[^]             { return new_token( TokenCode.invalid ); }






