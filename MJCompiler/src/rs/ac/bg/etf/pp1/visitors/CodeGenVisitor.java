package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.ast.*;


public class CodeGenVisitor extends VisitorAdaptor
{
    private int varCount;
    private int paramCnt;
    private int mainPc;

    public int getMainPc() { return mainPc; }



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
    // ClassDecl ::= (ClassDecl_Plain) ClassDeclType lbrace ClassDeclBody rbrace;
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
    // ClassDeclBody ::= (ClassDeclBody_VarsMethods) VarDeclList lbrace MethodDeclList rbrace;

    ////// <epsilon>
    ////// method method method
    // MethodDeclList ::= (MethodDeclList_Tail ) MethodDeclList MethodDecl;
    // MethodDeclList ::= (MethodDeclList_Empty) ;



    ////// void foo() { }
    ////// void foo() { statement statement }
    ////// void foo() vardl vardl { }
    ////// void foo() vardl vardl { statement statement }
    ////// void foo( int a, char c, Node Array[] ) { }
    ////// void foo( int a, char c, Node Array[] ) { statement statement }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { statement statement }
    // MethodDecl ::= (MethodDecl_Plain) MethodDeclType lparen FormPars rparen MethodDeclBody VarDeclList lbrace StatementList rbrace;
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

    ////// action symbol for opening a new scope
    // MethodDeclBody ::= (MethodDeclBody_Plain) ;
    @Override
    public void visit( MethodDeclBody_Plain curr )
    {
    }
    
    ////// <epsilon>
    ////// int ident, Node Array[], char c
    // FormPars ::= (FormPars_List ) FormParsScope FormParsList;
    // FormPars ::= (FormPars_Empty) FormParsScope ;

    ////// action symbol for opening a new scope
    // FormParsScope ::= (FormParsScope_Plain) ;

    ////// int a, char c, Node Array[]
    // FormParsList ::= (FormParsList_Init)                    FormParam;
    // FormParsList ::= (FormParsList_Tail) FormParsList comma FormParam;

    ////// int a, char c, Node Array[]
    // FormParam ::= (FormParam_Plain) FormParamType VarIdent;
    @Override
    public void visit( FormParam_Plain curr )
    {
    }
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};
    @Override
    public void visit( FormParam_Err curr )
    {
    }

    ////// int
    ////// Node
    // FormParamType ::= (FormParamType_Plain) Type;
    @Override
    public void visit( FormParamType_Plain curr )
    {
    }


    ////// <epsilon>
    ////// vardl vardl vardl vardl
    // VarDeclList ::= (VarDeclList_VarDecl) VarDeclList VarDecl;
    // VarDeclList ::= (VarDeclList_Empty  ) ;



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
    // VarIdentList ::= (VarIdentList_Tail    ) VarIdentList comma VarIdent;

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
    // ConstInitList ::= (ConstInitList_Tail) ConstInitList comma ConstInit;

    ////// a = 5
    // ConstInit ::= (ConstInit_Plain) ident:IdentName Assignop Literal;
    @Override
    public void visit( ConstInit_Plain curr )
    {
    }
    // ConstInit ::= (ConstInit_Err  ) error {: parser.report_error( "Bad initialization", null ); :};






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
    // Statement ::= (Statement_If         ) IF_K lparen Condition rparen Statement;
    // Statement ::= (Statement_IfElse     ) IF_K lparen Condition rparen Statement ELSE_K Statement;
    // Statement ::= (Statement_DoWhile    ) DoWhileScope Statement WHILE_K lparen Condition rparen semicol;
    @Override
    public void visit( Statement_DoWhile curr )
    {
    }
    // Statement ::= (Statement_Switch     ) SwitchScope lparen Expr rparen lbrace CaseList rbrace;
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
    // Statement ::= (Statement_Scope      ) lbrace StatementList rbrace;
    // Statement ::= (Statement_Semicolon  ) semicol;
    // Statement ::= (Statement_Err        ) error {: parser.report_error( "Bad statement", null ); :};

    ////// action symbols for opening a new scope
    // DoWhileScope ::= (DoWhileScope_Plain) DO_K;
    @Override
    public void visit( DoWhileScope_Plain curr )
    {
    }
    // SwitchScope ::= (SwitchScope_Plain) SWITCH_K;
    @Override
    public void visit( SwitchScope_Plain curr )
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
    // DesignatorStatement ::= (DesignatorStatement_Call      ) MethodCall lparen ActPars rparen;
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
    // StatementList ::= (StatementList_Empty) ;

    ////// <epsilon>
    ////// case 1: statement statement statement   case 2: statement statement
    // CaseList ::= (CaseList_Tail ) CaseList Case;
    // CaseList ::= (CaseList_Empty) ;

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
    // ActPars ::= (ActPars_Plain) ActParsScope ActParsList;
    @Override
    public void visit( ActPars_Plain curr )
    {
    }
    // ActPars ::= (ActPars_Empty) ActParsScope;
    @Override
    public void visit( ActPars_Empty curr )
    {
    }

    ////// action symbol for opening a new scope
    // ActParsScope ::= (ActParsScope_Plain) ;
    @Override
    public void visit( ActParsScope_Plain curr )
    {
    }


    ////// expr
    ////// expr, expr, expr
    // ActParsList ::= (ActParsList_Expr)                   ActParam;
    // ActParsList ::= (ActParsList_Tail) ActParsList comma ActParam;

    ////// expr
    // ActParam ::= (ActParam_Plain) Expr;
    @Override
    public void visit( ActParam_Plain curr )
    {
    }



    ////// expr   or   expr < expr and expr >= expr  or  expr != expr   // 'and' has greater priority than 'or'!
    // Condition ::= (Condition_Term)              CondTerm;
    @Override
    public void visit( Condition_Term curr )
    {
    }
    // Condition ::= (Condition_Or) Condition or CondTerm;
    @Override
    public void visit( Condition_Or curr )
    {
    }

    ////// expr < expr and expr >= expr
    // CondTerm ::= (CondTerm_Fact)              CondFact;
    @Override
    public void visit( CondTerm_Fact curr )
    {
    }
    // CondTerm ::= (CondTerm_And) CondTerm and CondFact;
    @Override
    public void visit( CondTerm_And curr )
    {
    }

    ////// expr < expr and expr >= expr
    // CondFact ::= (CondFact_Expr) Expr;
    @Override
    public void visit( CondFact_Expr curr )
    {
    }
    // CondFact ::= (CondFact_Relop) Expr Relop Expr;
    @Override
    public void visit( CondFact_Relop curr )
    {
    }



    ////// +term - term + term + term
    // Expr ::= (Expr_Addition) Addition;
    @Override
    public void visit( Expr_Addition curr )
    {
    }
    // Expr ::= (Expr_Err     ) error {: parser.report_error( "Bad expression", null ); :};
    @Override
    public void visit( Expr_Err curr )
    {
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
    }
    // Addition ::= (Addition_STerm)          Addop Term;
    @Override
    public void visit( Addition_STerm curr )
    {
    }
    // Addition ::= (Addition_Tail ) Addition Addop Term;
    @Override
    public void visit( Addition_Tail curr )
    {
    }

    ////// factor
    ////// factor*factor*factor
    // Term ::= (Term_Factor)            Factor;
    @Override
    public void visit( Term_Factor curr )
    {
    }
    // Term ::= (Term_Tail  ) Term Mulop Factor;
    @Override
    public void visit( Term_Tail curr )
    {
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
    }
    // Factor ::= (Factor_MethodCall ) MethodCall lparen ActPars rparen;
    @Override
    public void visit( Factor_MethodCall curr )
    {
    }
    // Factor ::= (Factor_Literal       ) Literal;
    @Override
    public void visit( Factor_Literal curr )
    {
    }
    // Factor ::= (Factor_NewVar        ) NEW_K Type;
    @Override
    public void visit( Factor_NewVar curr )
    {
    }
    // Factor ::= (Factor_NewArray      ) NEW_K Type lbracket Expr rbracket;
    @Override
    public void visit( Factor_NewArray curr )
    {
    }
    // Factor ::= (Factor_Expr          ) lparen Expr rparen;
    @Override
    public void visit( Factor_Expr curr )
    {
    }

    ////// ident.ident[ expr ]( expr, expr, expr )
    // MethodCall ::= (MethodCallScope_Plain) Designator;
    @Override
    public void visit( MethodCallScope_Plain curr )
    {
    }

    ////// null
    ////// ident
    ////// ident.ident
    ////// ident[ expr ]
    ////// ident.ident.ident[ expr ].ident
    ////// ident.ident.ident[ expr ].ident[ expr ]
    // Designator ::= (Designator_Ident  ) ident:Name;
    @Override
    public void visit( Designator_Ident curr )
    {
    }
    // Designator ::= (Designator_Null   ) NULL_K;
    @Override
    public void visit( Designator_Null curr )
    {
    }
    // Designator ::= (Designator_Field  ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Field curr )
    {
    }
    // Designator ::= (Designator_ArrElem) Designator lbracket Expr rbracket;
    @Override
    public void visit( Designator_ArrElem curr )
    {
    }






    ////// void | type
    // ReturnType ::= (ReturnType_Void ) VOID_K:ReturnType;
    @Override
    public void visit( ReturnType_Void curr )
    {
    }
    // ReturnType ::= (ReturnType_Ident) ident :ReturnType;
    @Override
    public void visit( ReturnType_Ident curr )
    {
    }

    ////// int | bool | char | ident
    // Type ::= (Type_Ident) ident:Type;
    @Override
    public void visit( Type_Ident curr )
    {
    }

    ////// 1202 | 'c' | true
    // Literal ::= (Literal_Int ) int_lit :Literal;
    @Override
    public void visit( Literal_Int curr ) {  }
    // Literal ::= (Literal_Char) char_lit:Literal;
    @Override
    public void visit( Literal_Char curr ) {  }
    // Literal ::= (Literal_Bool) bool_lit:Literal;
    @Override
    public void visit( Literal_Bool curr ) {  }

    ////// =
    // Assignop ::= (Assignop_Assign) assign:Assignop;
    @Override
    public void visit( Assignop_Assign curr ) {  }

    ////// ==  |  !=  |  >  |  >=  |  <  |  <=
    // Relop ::= (Relop_Eq) eq:Relop;
    @Override
    public void visit( Relop_Eq curr ) {  }
    // Relop ::= (Relop_Ne) ne:Relop;
    @Override
    public void visit( Relop_Ne curr ) {  }
    // Relop ::= (Relop_Gt) gt:Relop;
    @Override
    public void visit( Relop_Gt curr ) {  }
    // Relop ::= (Relop_Ge) ge:Relop;
    @Override
    public void visit( Relop_Ge curr ) {  }
    // Relop ::= (Relop_Lt) lt:Relop;
    @Override
    public void visit( Relop_Lt curr ) {  }
    // Relop ::= (Relop_Le) le:Relop;
    @Override
    public void visit( Relop_Le curr ) {  }

    ////// +  |  -
    // Addop ::= (Addop_Plus ) plus :Addop;
    @Override
    public void visit( Addop_Plus curr ) {  }
    // Addop ::= (Addop_Minus) minus:Addop;
    @Override
    public void visit( Addop_Minus curr ) {  }

    ////// *  |  /  |  %
    // Mulop ::= (Mulop_Mul ) mul :Mulop;
    @Override
    public void visit( Mulop_Mul curr ) {  }
    // Mulop ::= (Mulop_Div ) div :Mulop;
    @Override
    public void visit( Mulop_Div curr ) {  }
    // Mulop ::= (Mulop_Perc) perc:Mulop;
    @Override
    public void visit( Mulop_Perc curr ) {  }

}
