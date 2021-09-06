package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.Symbol;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.SymbolTable;
import rs.ac.bg.etf.pp1.SymbolType;
import rs.ac.bg.etf.pp1.TokenCode;


public class SemanticVisitor extends VisitorAdaptor
{
    private boolean errorDetected = false;
    private int varCount = 0;
    
    public boolean hasErrors() { return errorDetected; }
    public int getVarCount() { return varCount; }


    private void report_error( SyntaxNode node, String message )
    {
        report_error( node, message, true );
    }

    private void report_error( SyntaxNode node, String message, boolean entireScope )
    {
        errorDetected = true;

        ScopeVisitor scopeVisitor = new ScopeVisitor();
        node.accept( scopeVisitor );

        int tokenFromIdx = scopeVisitor.getTokenFromIdx();
        int tokenToIdx = ( entireScope ) ? scopeVisitor.getTokenToIdx() : tokenFromIdx + 1;

        Compiler.errors.add( CompilerError.SEMANTIC_ERROR, message, tokenFromIdx, tokenToIdx );
    }

    
    
    // ________________________________________________________________________________________________
    // visitor methods

    ////// program ident { }
    ////// program ident { method method method }
    ////// program ident constdl constdl vardl vardl classdl { }
    ////// program ident constdl constdl vardl vardl classdl { method method method }
    // Program ::= (Program_Plain) ProgramType GlobalDeclList lbrace MethodDeclList rbrace;
    @Override
    public void visit( Program_Plain curr )
    {
    }

    ////// program my_program
    // ProgramType ::= (ProgramType_Plain) PROGRAM_K ident:ProgramName;
    @Override
    public void visit( ProgramType_Plain curr )
    {
    }

    ////// <epsilon>
    ////// constdl constdl vardl vardl classdl
    // GlobalDeclList ::= (GlobalDeclList_Tail ) GlobalDeclList GlobalDecl;
    // GlobalDeclList ::= (GlobalDeclList_Empty) ;

    ////// constdl
    ////// vardl
    ////// classdl
    // GlobalDecl ::= (GlobalDecl_Const) ConstDecl;
    // GlobalDecl ::= (GlobalDecl_Var  ) VarDecl;
    // GlobalDecl ::= (GlobalDecl_Class) ClassDecl;



    ////// class A { }
    ////// class A { { method method method } }
    ////// class A extends B { vardl vardl vardl vardl }
    ////// class A extends B { vardl vardl vardl vardl { method method method } }
    // ClassDecl ::= (ClassDecl_Plain) ClassDeclType lbrace OpenScope ClassDeclBody rbrace;
    @Override
    public void visit( ClassDecl_Plain curr )
    {
    }

    ////// class A
    ////// class A extends B
    // ClassDeclType ::= (ClassDeclType_Plain  ) CLASS_K ident:ClassName;
    @Override
    public void visit( ClassDeclType_Plain curr )
    {
    }
    // ClassDeclType ::= (ClassDeclType_Extends) CLASS_K ident:ClassName EXTENDS_K Type;
    @Override
    public void visit( ClassDeclType_Extends curr )
    {
    }
    // ClassDeclType ::= (ClassDeclType_Err    ) CLASS_K error {: parser.report_error( "Bad class declaration", null ); :};
    @Override
    public void visit( ClassDeclType_Err curr )
    {
    }

    ////// <epsilon>
    ////// { method method method }
    ////// vardl vardl vardl vardl
    ////// vardl vardl vardl vardl { method method method }
    // ClassDeclBody ::= (ClassDeclBody_Vars       ) VarDeclList;
    @Override
    public void visit( ClassDeclBody_Vars curr )
    {
    }
    // ClassDeclBody ::= (ClassDeclBody_VarsMethods) VarDeclList lbrace OpenScope MethodDeclList rbrace;
    @Override
    public void visit( ClassDeclBody_VarsMethods curr )
    {
    }

    ////// <epsilon>
    ////// method method method
    // MethodDeclList ::= (MethodDeclList_Tail ) MethodDeclList MethodDecl;
    @Override
    public void visit( MethodDeclList_Tail curr )
    {
    }
    // MethodDeclList ::= (MethodDeclList_Empty) ;
    @Override
    public void visit( MethodDeclList_Empty curr )
    {
    }



    ////// void foo() { }
    ////// void foo() { statement statement }
    ////// void foo() vardl vardl { }
    ////// void foo() vardl vardl { statement statement }
    ////// void foo( int a, char c, Node Array[] ) { }
    ////// void foo( int a, char c, Node Array[] ) { statement statement }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { statement statement }
    // MethodDecl ::= (MethodDecl_Plain) MethodDeclType lparen OpenScope FormPars rparen OpenScope VarDeclList lbrace StatementList rbrace;
    @Override
    public void visit( MethodDecl_Plain curr )
    {
    }

    ////// void foo
    ////// A foo
    // MethodDeclType ::= (MethodDeclType_Plain) ReturnType ident:MethodName;
    @Override
    public void visit( MethodDeclType_Plain curr )
    {
    }

    ////// <epsilon>
    ////// int ident, Node Array[], char c
    // FormPars ::= (FormPars_List ) FormParsList;
    @Override
    public void visit( FormPars_List curr )
    {
    }
    // FormPars ::= (FormPars_Empty) ;
    @Override
    public void visit( FormPars_Empty curr )
    {
    }

    ////// int a, char c, Node Array[]
    // FormParsList ::= (FormParsList_Init)                    FormParam;
    @Override
    public void visit( FormParsList_Init curr )
    {
    }
    // FormParsList ::= (FormParsList_Tail) FormParsList comma FormParam;
    @Override
    public void visit( FormParsList_Tail curr )
    {
    }

    ////// int a, char c, Node Array[]
    // FormParam ::= (FormParam_Plain) Type VarIdent;
    @Override
    public void visit( FormParam_Plain curr )
    {
    }
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};
    @Override
    public void visit( FormParam_Err curr )
    {
    }

    ////// <epsilon>
    ////// vardl vardl vardl vardl
    // VarDeclList ::= (VarDeclList_VarDecl) VarDeclList VarDecl;
    @Override
    public void visit( VarDeclList_VarDecl curr )
    {
    }
    // VarDeclList ::= (VarDeclList_Empty  ) ;
    @Override
    public void visit( VarDeclList_Empty curr )
    {
    }



    ////// int a, b[], c;
    ////// A a1, a2;
    ////// static int a, b[], c;   // the static keyword is only allowed inside a class declaration!
    ////// static A a1, a2;
    // VarDecl ::= (VarDecl_Plain) VarDeclType VarIdentList semicol;
    @Override
    public void visit( VarDecl_Plain curr )
    {
    }

    ////// int
    ////// static A
    // VarDeclType ::= (VarDeclType_Plain )          Type;
    @Override
    public void visit( VarDeclType_Plain curr )
    {
    }
    // VarDeclType ::= (VarDeclType_Static) STATIC_K Type;
    @Override
    public void visit( VarDeclType_Static curr )
    {
    }
    // VarDeclType ::= (VarDeclType_Err   ) error {: parser.report_error( "Bad class declaration", null ); :};
    @Override
    public void visit( VarDeclType_Err curr )
    {
    }

    ////// a
    ////// a, b[], c
    // VarIdentList ::= (VarIdentList_VarIdent)                    VarIdent;
    @Override
    public void visit( VarIdentList_VarIdent curr )
    {
    }
    // VarIdentList ::= (VarIdentList_Tail    ) VarIdentList comma VarIdent;
    @Override
    public void visit( VarIdentList_Tail curr )
    {
    }

    ////// a
    ////// b[]
    // VarIdent ::= (VarIdent_Ident) ident:VarName;
    @Override
    public void visit( VarIdent_Ident curr )
    {
    }
    // VarIdent ::= (VarIdent_Array) ident:VarName lbracket rbracket;
    @Override
    public void visit( VarIdent_Array curr )
    {
    }
    // VarIdent ::= (VarIdent_Err  ) error {: parser.report_error( "Bad variable declaration", null ); :};
    @Override
    public void visit( VarIdent_Err curr )
    {
    }



    ////// const int a = 5, b = 6, c = 11;
    // ConstDecl ::= (ConstDecl_Plain) ConstDeclType ConstInitList semicol;
    @Override
    public void visit( ConstDecl_Plain curr )
    {
    }

    ////// const int
    // ConstDeclType ::= (ConstDeclType_Plain) CONST_K Type;
    @Override
    public void visit( ConstDeclType_Plain curr )
    {
    }
    // ConstDeclType ::= (ConstDeclType_Err  ) CONST_K error {: parser.report_error( "Bad constant type", null ); :};
    @Override
    public void visit( ConstDeclType_Err curr )
    {
    }

    ////// a = 5, b = 6, c = 11
    // ConstInitList ::= (ConstInitList_Init)                     ConstInit;
    @Override
    public void visit( ConstInitList_Init curr )
    {
    }
    // ConstInitList ::= (ConstInitList_Tail) ConstInitList comma ConstInit;
    @Override
    public void visit( ConstInitList_Tail curr )
    {
    }

    ////// a = 5
    // ConstInit ::= (ConstInit_Plain) ident:IdentName Assignop Literal;
    @Override
    public void visit( ConstInit_Plain curr )
    {
    }
    // ConstInit ::= (ConstInit_Err  ) error {: parser.report_error( "Bad initialization", null ); :};
    @Override
    public void visit( ConstInit_Err curr )
    {
    }






    ////// ident.ident[ expr ] = expr;
    ////// ident.ident[ expr ]( );
    ////// ident.ident[ expr ]( expr, expr, expr );
    ////// ident.ident[ expr ]++;
    ////// ident.ident[ expr ]--;
    //
    ////// if( condition ) statement
    ////// if( condition ) statement else statement
    ////// do statement while( condition );
    ////// switch( expr ) { }
    ////// switch( expr ) { case 1: statement statement statement   case 2: statement statement }
    ////// break;
    ////// continue;
    ////// return;
    ////// return expr;
    //
    ////// read( ident.ident[ expr ] );
    ////// print( ident.ident[ expr ], 2 );
    //
    ////// {}
    ////// { statement statement statement }
    // Statement ::= (Statement_Designator ) DesignatorStatement semicol;
    @Override
    public void visit( Statement_Designator curr )
    {
    }
    // Statement ::= (Statement_If         ) IF_K lparen Condition rparen Statement;
    @Override
    public void visit( Statement_If curr )
    {
    }
    // Statement ::= (Statement_IfElse     ) IF_K lparen Condition rparen Statement ELSE_K Statement;
    @Override
    public void visit( Statement_IfElse curr )
    {
    }
    // Statement ::= (Statement_DoWhile    ) DO_K Statement WHILE_K lparen Condition rparen semicol;
    @Override
    public void visit( Statement_DoWhile curr )
    {
    }
    // Statement ::= (Statement_Switch     ) SWITCH_K lparen Expr rparen lbrace CaseList rbrace;
    @Override
    public void visit( Statement_Switch curr )
    {
    }
    // Statement ::= (Statement_Break      ) BREAK_K       semicol;
    @Override
    public void visit( Statement_Break curr )
    {
    }
    // Statement ::= (Statement_Continue   ) CONTINUE_K    semicol;
    @Override
    public void visit( Statement_Continue curr )
    {
    }
    // Statement ::= (Statement_Return     ) RETURN_K      semicol;
    @Override
    public void visit( Statement_Return curr )
    {
    }
    // Statement ::= (Statement_ReturnExpr ) RETURN_K Expr semicol;
    @Override
    public void visit( Statement_ReturnExpr curr )
    {
    }
    // Statement ::= (Statement_Read       ) READ_K lparen Designator rparen semicol;
    @Override
    public void visit( Statement_Read curr )
    {
    }
    // Statement ::= (Statement_Print      ) PRINT_K lparen Expr                        rparen semicol;
    @Override
    public void visit( Statement_Print curr )
    {
    }
    // Statement ::= (Statement_PrintFormat) PRINT_K lparen Expr comma int_lit:MinWidth rparen semicol;
    @Override
    public void visit( Statement_PrintFormat curr )
    {
    }
    // Statement ::= (Statement_Scope      ) lbrace OpenScope StatementList rbrace;
    @Override
    public void visit( Statement_Scope curr )
    {
    }
    // Statement ::= (Statement_Semicolon  ) semicol;
    @Override
    public void visit( Statement_Semicolon curr )
    {
    }
    // Statement ::= (Statement_Err        ) error {: parser.report_error( "Bad statement", null ); :};
    @Override
    public void visit( Statement_Err curr )
    {
    }

    ////// ident.ident[ expr ] = expr
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// ident.ident[ expr ]++
    ////// ident.ident[ expr ]--
    // DesignatorStatement ::= (DesignatorStatement_Assign    ) Designator Assignop Expr;
    @Override
    public void visit( DesignatorStatement_Assign curr )
    {
    }
    // DesignatorStatement ::= (DesignatorStatement_Call      ) Designator lparen ActPars rparen;
    @Override
    public void visit( DesignatorStatement_Call curr )
    {
    }
    // DesignatorStatement ::= (DesignatorStatement_Plusplus  ) Designator plusplus;
    @Override
    public void visit( DesignatorStatement_Plusplus curr )
    {
    }
    // DesignatorStatement ::= (DesignatorStatement_Minusminus) Designator minusminus;
    @Override
    public void visit( DesignatorStatement_Minusminus curr )
    {
    }

    ////// <epsilon>
    ////// statement statement statement statement
    // StatementList ::= (StatementList_Tail ) StatementList Statement;
    @Override
    public void visit( StatementList_Tail curr )
    {
    }
    // StatementList ::= (StatementList_Empty) ;
    @Override
    public void visit( StatementList_Empty curr )
    {
    }

    ////// <epsilon>
    ////// case 1: statement statement statement   case 2: statement statement
    // CaseList ::= (CaseList_Tail ) CaseList Case;
    @Override
    public void visit( CaseList_Tail curr )
    {
    }
    // CaseList ::= (CaseList_Empty) ;
    @Override
    public void visit( CaseList_Empty curr )
    {
    }

    ////// case 1: statement statement statement
    ////// case 2: 
    ////// case 3: {}
    // Case ::= (Case_Plain) CASE_K int_lit:CaseNum colon StatementList;
    @Override
    public void visit( Case_Plain curr )
    {
    }



    ////// <epsilon>
    ////// expr
    ////// expr, expr, expr
    // ActPars ::= (ActPars_Plain) ActParsList;
    @Override
    public void visit( ActPars_Plain curr )
    {
    }
    // ActPars ::= (ActPars_Empty) ;
    @Override
    public void visit( ActPars_Empty curr )
    {
    }

    ////// expr
    ////// expr, expr, expr
    // ActParsList ::= (ActParsList_Expr)                   Expr;
    @Override
    public void visit( ActParsList_Expr curr )
    {
    }
    // ActParsList ::= (ActParsList_Tail) ActParsList comma Expr;
    @Override
    public void visit( ActParsList_Tail curr )
    {
    }

    ////// expr   or   expr < expr and expr >= expr  or  expr != expr   // 'and' has greater priority than 'or'!
    // Condition ::= (Condition_Term)              CondTerm;
    @Override
    public void visit( Condition_Term curr )
    {
    }
    // Condition ::= (Condition_Tail) Condition or CondTerm;
    @Override
    public void visit( Condition_Tail curr )
    {
    }

    ////// expr < expr and expr >= expr
    // CondTerm ::= (CondTerm_Fact)              CondFact;
    @Override
    public void visit( CondTerm_Fact curr )
    {
    }
    // CondTerm ::= (CondTerm_Tail) CondTerm and CondFact;
    @Override
    public void visit( CondTerm_Tail curr )
    {
    }

    ////// expr < expr and expr >= expr
    // CondFact ::= (CondFact_Expr) Expr;
    @Override
    public void visit( CondFact_Expr curr )
    {
    }
    // CondFact ::= (CondFact_Tail) Expr Relop Expr;
    @Override
    public void visit( CondFact_Tail curr )
    {
    }

    ////// +term - term + term + term
    // Expr ::= (Expr_Addition) Addition;
    @Override
    public void visit( Expr_Addition curr )
    {
        curr.symbol = curr.getAddition().symbol;
    }
    // Expr ::= (Expr_Err     ) error {: parser.report_error( "Bad expression", null ); :};
    @Override
    public void visit( Expr_Err curr )
    {
        curr.symbol = SymbolTable.noSym;
    }

    ////// term
    ////// +term
    ////// -term
    ////// term + term - term + term
    ////// -term + term - term + term
    ////// +term + term + term + term
    // Addition ::= (Addition_Term )                Term;
    @Override
    public void visit( Addition_Term curr )
    {
        curr.symbol = curr.getTerm().symbol;
    }
    // Addition ::= (Addition_STerm)          Addop Term;
    @Override
    public void visit( Addition_STerm curr )
    {
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getTerm().symbol;

        if( left.isNoSym() ) return;

        // if the term is not an int
        if( !left._type().isInt() )
        {
            report_error( curr.getTerm(), "This signed expression must result in an int" );
            return;
        }

        curr.symbol = left;
    }
    // Addition ::= (Addition_Tail ) Addition Addop Term;
    @Override
    public void visit( Addition_Tail curr )
    {
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getAddition().symbol;
        Symbol right = curr.getTerm().symbol;

        // if the left symbol is valid
        if( !left.isNoSym() )
        {
            // if the left symbol is not an int
            if( !left._type().isInt() )
            {
                report_error( curr.getAddition(), "The left side of the addition is not an int" );
            }
            // otherwise, save it
            else
            {
                curr.symbol = left;
            }
        }
        
        // if the right symbol is valid
        if( !right.isNoSym() )
        {
            // if the right symbol is not an int
            if( !right._type().isInt() )
            {
                report_error( curr.getTerm(), "The right side of the addition is not an int" );
            }
            // otherwise, save it
            else
            {
                curr.symbol = right;
            }
        }
    }

    ////// factor
    ////// factor*factor*factor
    // Term ::= (Term_Factor)            Factor;
    @Override
    public void visit( Term_Factor curr )
    {
        curr.symbol = curr.getFactor().symbol;
    }
    // Term ::= (Term_Tail  ) Term Mulop Factor;
    @Override
    public void visit( Term_Tail curr )
    {
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getTerm().symbol;
        Symbol right = curr.getFactor().symbol;

        // if the left symbol is valid
        if( !left.isNoSym() )
        {
            // if the left symbol is not an int
            if( !left._type().isInt() )
            {
                report_error( curr.getTerm(), "The left side of the multiplication is not an int" );
            }
            // otherwise, save it
            else
            {
                curr.symbol = left;
            }
        }
        
        // if the right symbol is valid
        if( !right.isNoSym() )
        {
            // if the right symbol is not an int
            if( !right._type().isInt() )
            {
                report_error( curr.getFactor(), "The right side of the multiplication is not an int" );
            }
            // otherwise, save it
            else
            {
                curr.symbol = right;
            }
        }
    }



    ////// ident.ident[ expr ]
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// 1202 | 'c' | true
    ////// new Object
    ////// new Array[ expr ]
    ////// ( expr )
    // Factor ::= (Factor_Designator    ) Designator;
    @Override
    public void visit( Factor_Designator curr )
    {
        curr.symbol = SymbolTable.noSym;
        
        Symbol left = curr.getDesignator().symbol;

        if( left.isNoSym() ) return;
        
        // if the designator is not a rvalue
        if( !left.isRvalue() )
        {
            report_error( curr, "This expression doesn't have a value" );
            return;
        }

        curr.symbol = left;
    }
    // Factor ::= (Factor_DesignatorCall) Designator lparen ActPars rparen;
    @Override
    public void visit( Factor_DesignatorCall curr )
    {
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;
     // Symbol right = curr.getActPars().symbol;

        if( left.isNoSym() ) return;

        // if the designator is not a function or class member method
        if( !left.isFunction() && !left.isMethod() )
        {
            report_error( curr.getDesignator(), "Expected function or class member method" );
            return;
        }

        curr.symbol = left;
    }
    // Factor ::= (Factor_Literal       ) Literal;
    @Override
    public void visit( Factor_Literal curr )
    {
        curr.symbol = curr.getLiteral().symbol;
    }
    // Factor ::= (Factor_NewVar        ) NEW_K Type;
    @Override
    public void visit( Factor_NewVar curr )
    {
        curr.symbol = SymbolTable.noSym;
        
        Symbol left = curr.getType().symbol;
        
        if( left.isNoSym() ) return;
        
        // if the <type token> is not a <class type>
        if( !left.isType() || !left._type().isClass() )
        {
            report_error( curr.getType(), "Cannot instantiate a non-class type", false );
            return;
        }

        curr.symbol = left;
    }
    // Factor ::= (Factor_NewArray      ) NEW_K Type lbracket Expr rbracket;
    @Override
    public void visit( Factor_NewArray curr )
    {
        curr.symbol = SymbolTable.noSym;
        
        Symbol left = curr.getType().symbol;
        Symbol right = curr.getExpr().symbol;
        
        if( left.isNoSym() ) return;
        
        // if the <type token> is not a <type>
        if( !left.isType() )
        {
            report_error( curr.getType(), "Cannot instantiate a non-type", false );
            return;
        }

        curr.symbol = Symbol.newVar(
            "@Factor_NewArray",
            SymbolType.newArray( "@Factor_NewArray", left._type() ),
            Symbol.NO_VALUE,
            SymbolTable._scopeLevel()
        );


        if( right.isNoSym() ) return;

        // if the expression does not result in an int
        if( !right._type().isInt() )
        {
            report_error( curr.getExpr(), "This expression must result in an int" );
            return;
        }
    }
    // Factor ::= (Factor_Expr          ) lparen Expr rparen;
    @Override
    public void visit( Factor_Expr curr )
    {
        curr.symbol = curr.getExpr().symbol;
    }

    ////// ident
    ////// ident.ident
    ////// ident[ expr ]
    ////// ident.ident.ident[ expr ].ident
    ////// ident.ident.ident[ expr ].ident[ expr ]
    private Designator lastDesignator = null;
    
    // Designator ::= (Designator_Ident  ) ident:Name;
    @Override
    public void visit( Designator_Ident curr )
    {
        // save the last encountered designator
        lastDesignator = curr;
        // try to find the symbol in the symbol table
        curr.symbol = SymbolTable.findSymbol( curr.getName() );

        // if the symbol does not exist in the symbol table
        if( curr.symbol.isNoSym() )
        {
            report_error( curr, "This symbol has not been declared", false );
            return;
        }
    }
    // Designator ::= (Designator_Field  ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Field curr )
    {
        // save the last encountered designator
        lastDesignator = curr;
        // set the current designator to the default value
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;

        // if the previous designator segment does not exist, an error must have been reported somewhere in the previous segments, return
        if( left.isNoSym() ) return;

        // if the previous symbol is not a class (doesn't have inner methods)
        if( !left._type().isClass() )
        {
            report_error( curr, "Expected class member, but the left designator is not a class", false );
            return;
        }

        // find the symbol with the given name in the previous designator's type's members
        Symbol member = left._type()._members().findSymbol( curr.getName() );

        // if the previous symbol doesn't contain the current field/member
        if( member.isNoSym() )
        {
            report_error( curr, "The specified class does not contain this member", false );
            return;
        }
        // if the previous designator is a type (static access) and its non-static member is accessed
        if( left.isType() && !member.isStaticField() )
        {
            report_error( curr, "This non-static class member cannot be accessed in a static way", false );
            return;
        }

        // save the class's member -- don't modify it in the future, since it is a part of the class's definition
        curr.symbol = member;
    }
    // Designator ::= (Designator_ArrElem) Designator lbracket Expr rbracket;
    @Override
    public void visit( Designator_ArrElem curr )
    {
        // save the last encountered designator
        lastDesignator = curr;
        // set the current designator to the default value
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;
        Symbol right = curr.getExpr().symbol;

        // if the previous designator segment does not exist, an error must have been reported somewhere in the previous segments, return
        if( left.isNoSym() ) return;

        // if the previous symbol is not an array
        if( !left._type().isArray() )
        {
            report_error( curr, "The left side of the brackets is not an array", false );
            return;
        }

        // find the symbol with the given name in the previous designator's type's members
        Symbol member = left._type()._members().findSymbol( "@elem" );

        // if the expression inside the angle brackets has an error, an error must have already been reported for the expression, return
        if( right.isNoSym() ) return;

        // if the expression inside the angle brackets does not result in an int
        if( !right._type().isInt() )
        {
            report_error( curr, "This expression must result in an int", false );
            return;
        }

        // save the array's member -- don't modify it in the future, since it is a part of the array's type definition
        curr.symbol = member;
    }






    ////// action rule for opening a new scope
    // OpenScope ::= (OpenScope_Plain) ;
    @Override
    public void visit( OpenScope_Plain curr )
    {
        // open a scope (some other top-level rule will close it)
        SymbolTable.openScope();
    }

    ////// void | type
    // ReturnType ::= (ReturnType_Void ) VOID_K:ReturnType;
    @Override
    public void visit( ReturnType_Void curr )
    {
        curr.symbol = SymbolTable.voidSym;
    }
    // ReturnType ::= (ReturnType_Ident) ident :ReturnType;
    @Override
    public void visit( ReturnType_Ident curr )
    {
        curr.symbol = SymbolTable.findSymbol( curr.getTypeName() );

        // if the symbol is missing from the symbol table
        if( curr.symbol.isNoSym() )
        {
            report_error( curr, "Expected type here, but this symbol has not been declared", false );
            return;
        }
        // if the symbol is not a type
        if( !curr.symbol.isType() )
        {
            report_error( curr, "Expected type here, but this isn't a type", false );
            return;
        }
    }

    ////// int | bool | char | ident
    // Type ::= (Type_Ident) ident:Type;
    @Override
    public void visit( Type_Ident curr )
    {
        curr.symbol = SymbolTable.findSymbol( curr.getTypeName() );

        // if the symbol is missing from the symbol table
        if( curr.symbol.isNoSym() )
        {
            report_error( curr, "Expected type here, but this symbol has not been declared", false );
            return;
        }
        // if the symbol is not a type
        if( !curr.symbol.isType() )
        {
            report_error( curr, "Expected type here, but this isn't a type", false );
            return;
        }
    }

    ////// 1202 | 'c' | true
    // Literal ::= (Literal_Int ) int_lit :Literal;
    @Override
    public void visit( Literal_Int curr ) { curr.symbol = Symbol.newConst( "@Literal_Int", SymbolTable.intType, curr.getLiteral() ); }
    // Literal ::= (Literal_Char) char_lit:Literal;
    @Override
    public void visit( Literal_Char curr )  { curr.symbol = Symbol.newConst( "@Literal_Char", SymbolTable.charType, curr.getLiteral() ); }
    // Literal ::= (Literal_Bool) bool_lit:Literal;
    @Override
    public void visit( Literal_Bool curr ) { curr.symbol = Symbol.newConst( "@Literal_Bool", SymbolTable.boolType, ( curr.getLiteral() ) ? 1 : 0 ); }

    ////// =
    // Assignop ::= (Assignop_Assign) assign:Assignop;
    @Override
    public void visit( Assignop_Assign curr ) { curr.symbol = Symbol.newConst( "@Assignop_Assign", SymbolTable.intType, TokenCode.assign ); }

    ////// ==  |  !=  |  >  |  >=  |  <  |  <=
    // Relop ::= (Relop_Eq) eq:Relop;
    @Override
    public void visit( Relop_Eq curr ) { curr.symbol = Symbol.newConst( "@Relop_Eq", SymbolTable.intType, TokenCode.eq ); }
    // Relop ::= (Relop_Ne) ne:Relop;
    @Override
    public void visit( Relop_Ne curr ) { curr.symbol = Symbol.newConst( "@Relop_Ne", SymbolTable.intType, TokenCode.ne ); }
    // Relop ::= (Relop_Gt) gt:Relop;
    @Override
    public void visit( Relop_Gt curr ) { curr.symbol = Symbol.newConst( "@Relop_Gt", SymbolTable.intType, TokenCode.gt ); }
    // Relop ::= (Relop_Ge) ge:Relop;
    @Override
    public void visit( Relop_Ge curr ) { curr.symbol = Symbol.newConst( "@Relop_Ge", SymbolTable.intType, TokenCode.ge ); }
    // Relop ::= (Relop_Lt) lt:Relop;
    @Override
    public void visit( Relop_Lt curr ) { curr.symbol = Symbol.newConst( "@Relop_Lt", SymbolTable.intType, TokenCode.lt ); }
    // Relop ::= (Relop_Le) le:Relop;
    @Override
    public void visit( Relop_Le curr ) { curr.symbol = Symbol.newConst( "@Relop_Le", SymbolTable.intType, TokenCode.le ); }

    ////// +  |  -
    // Addop ::= (Addop_Plus ) plus :Addop;
    @Override
    public void visit( Addop_Plus curr ) { curr.symbol = Symbol.newConst( "@Addop_Plus", SymbolTable.intType, TokenCode.plus ); }
    // Addop ::= (Addop_Minus) minus:Addop;
    @Override
    public void visit( Addop_Minus curr ) { curr.symbol = Symbol.newConst( "@Addop_Minus", SymbolTable.intType, TokenCode.minus ); }

    ////// *  |  /  |  %
    // Mulop ::= (Mulop_Mul ) mul :Mulop;
    @Override
    public void visit( Mulop_Mul curr ) { curr.symbol = Symbol.newConst( "@Mulop_Mul", SymbolTable.intType, TokenCode.mul ); }
    // Mulop ::= (Mulop_Div ) div :Mulop;
    @Override
    public void visit( Mulop_Div curr ) { curr.symbol = Symbol.newConst( "@Mulop_Div", SymbolTable.intType, TokenCode.div ); }
    // Mulop ::= (Mulop_Perc) perc:Mulop;
    @Override
    public void visit( Mulop_Perc curr ) { curr.symbol = Symbol.newConst( "@Mulop_Perc", SymbolTable.intType, TokenCode.perc ); }
















    // public void visit( Program program )
    // {
    //     nVars = SymbolTable.currentScope.getnVars();
    //     SymbolTable.chainLocalSymbols( program.getProgName().obj );
    //     SymbolTable.closeScope();
    // }

    // public void visit( ProgName progName )
    // {
    //     progName.obj = SymbolTable.insert( Obj.Prog, progName.getPName(), SymbolTable.noType );
    //     SymbolTable.openScope();
    // }

    // public void visit( VarDecl varDecl )
    // {
    //     report_info( "Deklarisana promenljiva " + varDecl.getVarName(), varDecl );
    //     Obj varNode = SymbolTable.insert( Obj.Var, varDecl.getVarName(), varDecl.getType().struct );
    // }

    // public void visit( Type type )
    // {
    //     Obj typeNode = SymbolTable.find( type.getTypeName() );
    //     if( typeNode == SymbolTable.noObj )
    //     {
    //         report_error( "Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola", null );
    //         type.struct = SymbolTable.noType;
    //     }
    //     else
    //     {
    //         if( Obj.Type == typeNode.getKind() )
    //         {
    //             type.struct = typeNode.getType();
    //         }
    //         else
    //         {
    //             report_error( "Greska: Ime " + type.getTypeName() + " ne predstavlja tip ", type );
    //             type.struct = SymbolTable.noType;
    //         }
    //     }
    // }

    // public void visit( MethodDecl methodDecl )
    // {
    //     if( !returnFound && currentMethod.getType() != SymbolTable.noType )
    //     {
    //         report_error( "Semanticka greska na liniji " + methodDecl.getLine() + ": funcija " + currentMethod.getName() + " nema return iskaz!", null );
    //     }

    //     SymbolTable.chainLocalSymbols( currentMethod );
    //     SymbolTable.closeScope();

    //     returnFound = false;
    //     currentMethod = null;
    // }

    // public void visit( MethodTypeName methodTypeName )
    // {
    //     currentMethod = SymbolTable.insert( Obj.Meth, methodTypeName.getMethName(), methodTypeName.getType().struct );
    //     methodTypeName.obj = currentMethod;
    //     SymbolTable.openScope();
    //     report_info( "Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName );
    // }

    // public void visit( Assignment assignment )
    // {
    //     if( !assignment.getExpr().struct.assignableTo( assignment.getDesignator().obj.getType() ) )
    //         report_error( "Greska na liniji " + assignment.getLine() + " : " + " nekompatibilni tipovi u dodeli vrednosti ", null );
    // }

    // public void visit( PrintStmt printStmt )
    // {
    //     printCallCount++;
    // }

    // public void visit( ReturnExpr returnExpr )
    // {
    //     returnFound = true;
    //     Struct currMethType = currentMethod.getType();
    //     if( !currMethType.compatibleWith( returnExpr.getExpr().struct ) )
    //     {
    //         report_error( "Greska na liniji " + returnExpr.getLine() + " : " + "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije " + currentMethod.getName(), null );
    //     }
    // }

    // public void visit( ProcCall procCall )
    // {
    //     Obj func = procCall.getDesignator().obj;
    //     if( Obj.Meth == func.getKind() )
    //     {
    //         report_info( "Pronadjen poziv funkcije " + func.getName() + " na liniji " + procCall.getLine(), null );
    //         ////////RESULT = func.getType();
    //     }
    //     else
    //     {
    //         report_error( "Greska na liniji " + procCall.getLine() + " : ime " + func.getName() + " nije funkcija!", null );
    //         ////////RESULT = SymbolTable.noType;
    //     }
    // }

    // public void visit( AddExpr addExpr )
    // {
    //     Struct te = addExpr.getExpr().struct;
    //     Struct t = addExpr.getTerm().struct;
    //     if( te.equals( t ) && te == SymbolTable.intType )
    //         addExpr.struct = te;
    //     else
    //     {
    //         report_error( "Greska na liniji " + addExpr.getLine() + " : nekompatibilni tipovi u izrazu za sabiranje.", null );
    //         addExpr.struct = SymbolTable.noType;
    //     }
    // }

    // public void visit( TermExpr termExpr )
    // {
    //     termExpr.struct = termExpr.getTerm().struct;
    // }

    // public void visit( Term term )
    // {
    //     term.struct = term.getFactor().struct;
    // }

    // public void visit( Const cnst )
    // {
    //     cnst.struct = SymbolTable.intType;
    // }

    // public void visit( Var var )
    // {
    //     var.struct = var.getDesignator().obj.getType();
    // }

    // public void visit( FuncCall funcCall )
    // {
    //     Obj func = funcCall.getDesignator().obj;
    //     if( Obj.Meth == func.getKind() )
    //     {
    //         report_info( "Pronadjen poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null );
    //         funcCall.struct = func.getType();
    //     }
    //     else
    //     {
    //         report_error( "Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!", null );
    //         funcCall.struct = SymbolTable.noType;
    //     }

    // }

    // public void visit( Designator designator )
    // {
    //     Obj obj = SymbolTable.find( designator.getName() );
    //     if( obj == SymbolTable.noObj )
    //     {
    //         report_error( "Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano! ", null );
    //     }
    //     designator.obj = obj;
    // }

}
