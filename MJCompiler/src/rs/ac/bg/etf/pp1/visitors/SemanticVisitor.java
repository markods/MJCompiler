package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.SymbolTable;
import rs.etf.pp1.symboltable.concepts.*;


public class SemanticVisitor extends VisitorAdaptor
{
    private boolean errorDetected = false;
    public int nVars;   // TODO
    
    public boolean hasErrors() { return errorDetected; }

    private void report_error( SyntaxNode node, String message )
    {
        errorDetected = true;
        SyntaxVisitor syntaxVisitor = new SyntaxVisitor();
        node.accept( syntaxVisitor );
        Compiler.errors.add( CompilerError.SEMANTIC_ERROR, message, syntaxVisitor.getSymbolFromIdx(), syntaxVisitor.getSymbolToIdx() );
    }

    
    
    // ________________________________________________________________________________________________
    // visitor methods


    //////// program ident { }
    //////// program ident { method method method }
    //////// program ident constdl constdl vardl vardl classdl { }
    //////// program ident constdl constdl vardl vardl classdl { method method method }
    // Program ::= (Program_Plain) PROGRAM_K ident GlobalDeclList MethodDeclScope;
    @Override
    public void visit( Program_Plain node )
    {
    }

    //////// <epsilon>
    //////// constdl constdl vardl vardl classdl
    // GlobalDeclList ::= (GlobalDeclList_Tail ) GlobalDecl GlobalDeclList;
    // GlobalDeclList ::= (GlobalDeclList_Empty) ;

    // GlobalDecl ::= (GlobalDecl_Const) ConstDecl;
    // GlobalDecl ::= (GlobalDecl_Var  ) VarDecl;
    // GlobalDecl ::= (GlobalDecl_Class) ClassDecl;



    // ____________________________________
    //////// class A { }
    //////// class A { { method method method } }
    //////// class A extends B { vardl vardl vardl vardl }
    //////// class A extends B { vardl vardl vardl vardl { method method method } }
    // ClassDecl ::= (ClassDecl_Plain) ClassIdentDecl ClassDeclScope;
    @Override
    public void visit( ClassDecl_Plain node )
    {
    }

    //////// class A
    //////// class A extends B
    // ClassIdentDecl ::= (ClassIdentDecl_Plain  ) CLASS_K ident;
    @Override
    public void visit( ClassIdentDecl_Plain node )
    {
    }
    // ClassIdentDecl ::= (ClassIdentDecl_Extends) CLASS_K ident EXTENDS_K Type;
    @Override
    public void visit( ClassIdentDecl_Extends node )
    {
    }
    // ClassIdentDecl ::= (ClassIdentDecl_Err    ) CLASS_K error:e {: parser.report_error( "Invalid class identifier declaration", null ); :};
    @Override
    public void visit( ClassIdentDecl_Err node )
    {
    }

    //////// { }
    //////// { { method method method } }
    //////// { vardl vardl vardl vardl }
    //////// { vardl vardl vardl vardl { method method method } }
    // ClassDeclScope ::= (ClassDeclScope_Vars       ) lbrace ClassVarDeclList                 rbrace;
    @Override
    public void visit( ClassDeclScope_Vars node )
    {
    }
    // ClassDeclScope ::= (ClassDeclScope_VarsMethods) lbrace ClassVarDeclList MethodDeclScope rbrace;
    @Override
    public void visit( ClassDeclScope_VarsMethods node )
    {
    }

    //////// <epsilon>
    //////// clsvardl clsvardl clsvardl clsvardl
    // ClassVarDeclList ::= (ClassVarDeclList_VarDecl) ClassVarDecl ClassVarDeclList;
    @Override
    public void visit( ClassVarDeclList_VarDecl node )
    {
    }
    // ClassVarDeclList ::= (ClassVarDeclList_Empty  ) ;
    @Override
    public void visit( ClassVarDeclList_Empty node )
    {
    }

    //////// int a, b[], c;
    //////// A a1, a2;
    //////// static int a, b[], c;
    //////// static A a1, a2;
    // ClassVarDecl ::= (ClassVarDecl_Plain )          Type VarIdentList semicol;
    @Override
    public void visit( ClassVarDecl_Plain node )
    {
    }
    // ClassVarDecl ::= (ClassVarDecl_Static) STATIC_K Type VarIdentList semicol;
    @Override
    public void visit( ClassVarDecl_Static node )
    {
    }

    //////// { }
    //////// { method method method }
    // MethodDeclScope ::= (MethodDeclScope_Plain) lbrace MethodDeclList rbrace;
    @Override
    public void visit( MethodDeclScope_Plain node )
    {
    }

    // MethodDeclList ::= (MethodDeclList_Tail ) MethodDecl MethodDeclList;
    @Override
    public void visit( MethodDeclList_Tail node )
    {
    }
    // MethodDeclList ::= (MethodDeclList_Empty) ;
    @Override
    public void visit( MethodDeclList_Empty node )
    {
    }



    // ____________________________________
    //////// void foo() { }
    //////// void foo() { statement statement }
    //////// void foo() vardl vardl { }
    //////// void foo() vardl vardl { statement statement }
    //////// void foo( int a, char c, Node Array[] ) { }
    //////// void foo( int a, char c, Node Array[] ) { statement statement }
    //////// void foo( int a, char c, Node Array[] ) vardl vardl { }
    //////// void foo( int a, char c, Node Array[] ) vardl vardl { statement statement }
    // MethodDecl ::= (MethodDecl_Plain) ReturnType ident lparen FormPars rparen VarDeclList lbrace StatementList rbrace;
    @Override
    public void visit( MethodDecl_Plain node )
    {
    }

    //////// <epsilon>
    //////// int ident, Node Array[], char c
    // FormPars ::= (FormPars_List ) FormParsList;
    @Override
    public void visit( FormPars_List node )
    {
    }
    // FormPars ::= (FormPars_Empty) ;
    @Override
    public void visit( FormPars_Empty node )
    {
    }

    // FormParsList ::= (FormParsList_Init) FormParam;
    @Override
    public void visit( FormParsList_Init node )
    {
    }
    // FormParsList ::= (FormParsList_Tail) FormParam comma FormParsList;
    @Override
    public void visit( FormParsList_Tail node )
    {
    }

    //////// <epsilon>
    //////// vardl vardl vardl vardl
    // VarDeclList ::= (VarDeclList_VarDecl) VarDecl VarDeclList;
    @Override
    public void visit( VarDeclList_VarDecl node )
    {
    }
    // VarDeclList ::= (VarDeclList_Empty  ) ;
    @Override
    public void visit( VarDeclList_Empty node )
    {
    }



    // ____________________________________
    //////// int a, b[], c;
    //////// A a1, a2;
    // VarDecl ::= (VarDecl_Plain) Type VarIdentList semicol;
    @Override
    public void visit( VarDecl_Plain node )
    {
    }

    // VarIdentList ::= (VarIdentList_VarIdent) VarIdent;
    @Override
    public void visit( VarIdentList_VarIdent node )
    {
    }
    // VarIdentList ::= (VarIdentList_Tail    ) VarIdent comma VarIdentList;
    @Override
    public void visit( VarIdentList_Tail node )
    {
    }



    // ____________________________________
    //////// const int a = 5, b = 6, c = 11;
    // ConstDecl ::= (ConstDecl_Plain) CONST_K Type IdentInitList semicol;
    @Override
    public void visit( ConstDecl_Plain node )
    {
    }

    // IdentInitList ::= (IdentInitList_Init) IdentInit;
    @Override
    public void visit( IdentInitList_Init node )
    {
    }
    // IdentInitList ::= (IdentInitList_Tail) IdentInit comma IdentInitList;
    @Override
    public void visit( IdentInitList_Tail node )
    {
    }



    // ____________________________________
    //////// ident.ident[ expr ] = expr;
    //////// ident.ident[ expr ]( );
    //////// ident.ident[ expr ]( expr, expr, expr );
    //////// ident.ident[ expr ]++;
    //////// ident.ident[ expr ]--;
    ////////
    //////// if( condition ) statement
    //////// if( condition ) statement else statement
    //////// do statement while( condition );
    //////// switch( expr ) { }
    //////// switch( expr ) { case 1: statement statement statement   case 2: statement statement }
    //////// break;
    //////// continue;
    //////// return;
    //////// return expr;
    ////////
    //////// read( ident.ident[ expr ] );
    //////// print( ident.ident[ expr ], 2 );
    ////////
    //////// {}
    //////// { statement statement statement }
    // Statement ::= (Statement_Designator ) DesignatorStatement semicol;
    @Override
    public void visit( Statement_Designator node )
    {
    }
    // Statement ::= (Statement_If         ) IF_K lparen Condition rparen Statement;
    @Override
    public void visit( Statement_If node )
    {
    }
    // Statement ::= (Statement_IfElse     ) IF_K lparen Condition rparen Statement ELSE_K Statement;
    @Override
    public void visit( Statement_IfElse node )
    {
    }
    // Statement ::= (Statement_DoWhile    ) DO_K Statement WHILE_K lparen Condition rparen semicol;
    @Override
    public void visit( Statement_DoWhile node )
    {
    }
    // Statement ::= (Statement_Switch     ) SWITCH_K lparen Expr rparen lbrace CaseList rbrace;
    @Override
    public void visit( Statement_Switch node )
    {
    }
    // Statement ::= (Statement_Break      ) BREAK_K semicol;
    @Override
    public void visit( Statement_Break node )
    {
    }
    // Statement ::= (Statement_Continue   ) CONTINUE_K semicol;
    @Override
    public void visit( Statement_Continue node )
    {
    }
    // Statement ::= (Statement_Return     ) RETURN_K semicol;
    @Override
    public void visit( Statement_Return node )
    {
    }
    // Statement ::= (Statement_ReturnExpr ) RETURN_K Expr semicol;
    @Override
    public void visit( Statement_ReturnExpr node )
    {
    }
    // Statement ::= (Statement_Read       ) READ_K lparen Designator rparen semicol;
    @Override
    public void visit( Statement_Read node )
    {
    }
    // Statement ::= (Statement_Print      ) PRINT_K lparen Expr rparen semicol;
    @Override
    public void visit( Statement_Print node )
    {
    }
    // Statement ::= (Statement_PrintFormat) PRINT_K lparen Expr comma int_lit rparen semicol;
    @Override
    public void visit( Statement_PrintFormat node )
    {
    }
    // Statement ::= (Statement_Scope      ) lbrace StatementList rbrace;
    @Override
    public void visit( Statement_Scope node )
    {
    }
    // Statement ::= (Statement_Semicolon  ) semicol;
    @Override
    public void visit( Statement_Semicolon node )
    {
    }
    // Statement ::= (Statement_Err        ) error:e semicol {: parser.report_error( "Invalid statement", null ); :};
    @Override
    public void visit( Statement_Err node )
    {
    }

    //////// ident.ident[ expr ] = expr
    //////// ident.ident[ expr ]( )
    //////// ident.ident[ expr ]( expr, expr, expr )
    //////// ident.ident[ expr ]++
    //////// ident.ident[ expr ]--
    // DesignatorStatement ::= (DesignatorStatement_Assign    ) Designator Assignop Expr;
    @Override
    public void visit( DesignatorStatement_Assign node )
    {
    }
    // DesignatorStatement ::= (DesignatorStatement_Call      ) Designator lparen ActPars rparen;
    @Override
    public void visit( DesignatorStatement_Call node )
    {
    }
    // DesignatorStatement ::= (DesignatorStatement_Plusplus  ) Designator plusplus;
    @Override
    public void visit( DesignatorStatement_Plusplus node )
    {
    }
    // DesignatorStatement ::= (DesignatorStatement_Minusminus) Designator minusminus;
    @Override
    public void visit( DesignatorStatement_Minusminus node )
    {
    }



    // ____________________________________
    //////// <epsilon>
    //////// statement statement statement statement
    // StatementList ::= (StatementList_Tail ) Statement StatementList;
    @Override
    public void visit( StatementList_Tail node )
    {
    }
    // StatementList ::= (StatementList_Empty) ;
    @Override
    public void visit( StatementList_Empty node )
    {
    }

    //////// <epsilon>
    //////// case 1: statement statement statement   case 2: statement statement
    // CaseList ::= (CaseList_Tail ) CASE_K int_lit colon StatementList CaseList;
    @Override
    public void visit( CaseList_Tail node )
    {
    }
    // CaseList ::= (CaseList_Empty) ;
    @Override
    public void visit( CaseList_Empty node )
    {
    }

    //////// <epsilon>
    //////// expr
    //////// expr, expr, expr
    // ActPars ::= (ActPars_Tail ) ActParsList;
    @Override
    public void visit( ActPars_Tail node )
    {
    }
    // ActPars ::= (ActPars_Empty) ;
    @Override
    public void visit( ActPars_Empty node )
    {
    }

    // ActParsList ::= (ActParsList_Expr) Expr;
    @Override
    public void visit( ActParsList_Expr node )
    {
    }
    // ActParsList ::= (ActParsList_Tail) Expr comma ActParsList;
    @Override
    public void visit( ActParsList_Tail node )
    {
    }

    //////// expr   or   expr < expr and expr >= expr  or  expr != expr   //////// 'and' has greater priority than 'or'!
    // Condition ::= (Condition_Term) CondTerm;
    @Override
    public void visit( Condition_Term node )
    {
    }
    // Condition ::= (Condition_Tail) CondTerm or Condition;
    @Override
    public void visit( Condition_Tail node )
    {
    }

    // CondTerm ::= (CondTerm_Fact) CondFact;
    @Override
    public void visit( CondTerm_Fact node )
    {
    }
    // CondTerm ::= (CondTerm_Tail) CondFact and CondTerm;
    @Override
    public void visit( CondTerm_Tail node )
    {
    }

    // CondFact ::= (CondFact_Expr) Expr;
    @Override
    public void visit( CondFact_Expr node )
    {
    }
    // CondFact ::= (CondFact_Tail) Expr Relop Expr;
    @Override
    public void visit( CondFact_Tail node )
    {
    }

    //////// term
    //////// +term
    //////// -term
    //////// term + term - term + term
    //////// -term + term - term + term
    //////// +term + term + term + term
    // Expr ::= (Expr_Addition) Addition;
    @Override
    public void visit( Expr_Addition node )
    {
    }
    // Expr ::= (Expr_Err     ) error:e {: parser.report_error( "Invalid expression", null ); :};
    @Override
    public void visit( Expr_Err node )
    {
    }

    // Addition ::= (Addition_Term    ) Term;
    @Override
    public void visit( Addition_Term node )
    {
    }
    // Addition ::= (Addition_Tail    )      SignedAddition;
    @Override
    public void visit( Addition_Tail node )
    {
    }
    // Addition ::= (Addition_TermTail) Term SignedAddition;
    @Override
    public void visit( Addition_TermTail node )
    {
    }

    // SignedAddition ::= (SignedAddition_Term) Addop Term;
    @Override
    public void visit( SignedAddition_Term node )
    {
    }
    // SignedAddition ::= (SignedAddition_Tail) Addop Term SignedAddition;
    @Override
    public void visit( SignedAddition_Tail node )
    {
    }



    // ____________________________________
    //////// factor
    //////// factor*factor*factor
    // Term ::= (Term_Factor) Factor;
    @Override
    public void visit( Term_Factor node )
    {
    }
    // Term ::= (Term_Tail  ) Factor Mulop Term;
    @Override
    public void visit( Term_Tail node )
    {
    }

    //////// ident.ident[ expr ]
    //////// ident.ident[ expr ]( )
    //////// ident.ident[ expr ]( expr, expr, expr )
    //////// 1202 | 'c' | true
    //////// new Object
    //////// new Array[ expr ]
    //////// ( expr )
    // Factor ::= (Factor_Designator    ) Designator;
    @Override
    public void visit( Factor_Designator node )
    {
    }
    // Factor ::= (Factor_DesignatorCall) Designator lparen ActPars rparen;
    @Override
    public void visit( Factor_DesignatorCall node )
    {
    }
    // Factor ::= (Factor_Literal       ) Literal;
    @Override
    public void visit( Factor_Literal node )
    {
    }
    // Factor ::= (Factor_NewVar        ) NEW_K Type;
    @Override
    public void visit( Factor_NewVar node )
    {
    }
    // Factor ::= (Factor_NewArray      ) NEW_K Type lbracket Expr rbracket;
    @Override
    public void visit( Factor_NewArray node )
    {
    }
    // Factor ::= (Factor_Expr          ) lparen Expr rparen;
    @Override
    public void visit( Factor_Expr node )
    {
    }

    //////// ident
    //////// ident.ident
    //////// ident[ expr ]
    //////// ident.ident.ident[ expr ].ident
    //////// ident.ident.ident[ expr ].ident[ expr ]
    // Designator ::= (Designator_Plain) ident DesignatorNext;
    @Override
    public void visit( Designator_Plain node )
    {
    }

    // DesignatorNext ::= (DesignatorNext_FieldTail) dot ident              DesignatorNext;
    @Override
    public void visit( DesignatorNext_FieldTail node )
    {
    }
    // DesignatorNext ::= (DesignatorNext_ElemTail ) lbracket Expr rbracket DesignatorNext;
    @Override
    public void visit( DesignatorNext_ElemTail node )
    {
    }
    // DesignatorNext ::= (DesignatorNext_Empty    ) ;
    @Override
    public void visit( DesignatorNext_Empty node )
    {
    }



    // ____________________________________
    //////// int ident
    //////// Node Array[]
    //////// char c
    // FormParam ::= Type VarIdent;
    // FormParam ::= error:e {: parser.report_error( "Invalid formal parameter", null ); :};
    //////// ident = 12430
    // IdentInit ::= (IdentInit_Plain) ident Assignop Literal;
    // IdentInit ::= (IdentInit_Err  ) error:e {: parser.report_error( "Invalid identifier initialization", null ); :};
    //////// ident | array[]
    // VarIdent ::= (VarIdent_Ident) ident;
    // VarIdent ::= (VarIdent_Array) ident lbracket rbracket;
    // VarIdent ::= (VarIdent_Err  ) error:e {: parser.report_error( "Invalid variable identifier", null ); :};

    //////// 1202 | 'c' | true
    // Literal ::= (Literal_Int ) int_lit;
    // Literal ::= (Literal_Char) char_lit;
    // Literal ::= (Literal_Bool) bool_lit;
    //////// void | type
    // ReturnType ::= (ReturnType_Void ) VOID_K;
    // ReturnType ::= (ReturnType_Ident) ident;
    //////// int | bool | char | ident
    // Type ::= (Type_Ident) ident;
    // Type ::= (Type_Err  ) error:e {: parser.report_error( "Invalid type", null ); :};

    //////// =
    // Assignop ::= (Assignop_Assign) assign;
    //////// ==  |  !=  |  >  |  >=  |  <  |  <=
    // Relop ::= (Relop_Eq ) eq;
    // Relop ::= (Relop_Neq) ne;
    // Relop ::= (Relop_Gt ) gt;
    // Relop ::= (Relop_Geq) ge;
    // Relop ::= (Relop_Lt ) lt;
    // Relop ::= (Relop_Leq) le;
    //////// +  |  -
    // Addop ::= (Addop_Plus ) plus;
    // Addop ::= (Addop_Minus) minus;
    //////// *  |  /  |  %
    // Mulop ::= (Mulop_Mul ) mul;
    // Mulop ::= (Mulop_Div ) div;
    // Mulop ::= (Mulop_Perc) perc;























    // public void visit( Program program )
    // {
    //     nVars = Tab.currentScope.getnVars();
    //     Tab.chainLocalSymbols( program.getProgName().obj );
    //     Tab.closeScope();
    // }

    // public void visit( ProgName progName )
    // {
    //     progName.obj = Tab.insert( Obj.Prog, progName.getPName(), Tab.noType );
    //     Tab.openScope();
    // }

    // public void visit( VarDecl varDecl )
    // {
    //     report_info( "Deklarisana promenljiva " + varDecl.getVarName(), varDecl );
    //     Obj varNode = Tab.insert( Obj.Var, varDecl.getVarName(), varDecl.getType().struct );
    // }

    // public void visit( Type type )
    // {
    //     Obj typeNode = Tab.find( type.getTypeName() );
    //     if( typeNode == Tab.noObj )
    //     {
    //         report_error( "Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola", null );
    //         type.struct = Tab.noType;
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
    //             type.struct = Tab.noType;
    //         }
    //     }
    // }

    // public void visit( MethodDecl methodDecl )
    // {
    //     if( !returnFound && currentMethod.getType() != Tab.noType )
    //     {
    //         report_error( "Semanticka greska na liniji " + methodDecl.getLine() + ": funcija " + currentMethod.getName() + " nema return iskaz!", null );
    //     }

    //     Tab.chainLocalSymbols( currentMethod );
    //     Tab.closeScope();

    //     returnFound = false;
    //     currentMethod = null;
    // }

    // public void visit( MethodTypeName methodTypeName )
    // {
    //     currentMethod = Tab.insert( Obj.Meth, methodTypeName.getMethName(), methodTypeName.getType().struct );
    //     methodTypeName.obj = currentMethod;
    //     Tab.openScope();
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
    //         ////////RESULT = Tab.noType;
    //     }
    // }

    // public void visit( AddExpr addExpr )
    // {
    //     Struct te = addExpr.getExpr().struct;
    //     Struct t = addExpr.getTerm().struct;
    //     if( te.equals( t ) && te == Tab.intType )
    //         addExpr.struct = te;
    //     else
    //     {
    //         report_error( "Greska na liniji " + addExpr.getLine() + " : nekompatibilni tipovi u izrazu za sabiranje.", null );
    //         addExpr.struct = Tab.noType;
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
    //     cnst.struct = Tab.intType;
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
    //         funcCall.struct = Tab.noType;
    //     }

    // }

    // public void visit( Designator designator )
    // {
    //     Obj obj = Tab.find( designator.getName() );
    //     if( obj == Tab.noObj )
    //     {
    //         report_error( "Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano! ", null );
    //     }
    //     designator.obj = obj;
    // }

}
