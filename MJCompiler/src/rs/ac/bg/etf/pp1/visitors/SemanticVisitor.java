package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.SymbolTable;
import rs.etf.pp1.symboltable.concepts.*;


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
    public void visit( Program_Plain node )
    {
    }

    ////// program my_program
    // ProgramType ::= (ProgramType_Plain) PROGRAM_K ident:ProgramName;
    @Override
    public void visit( ProgramType_Plain node )
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
    public void visit( ClassDecl_Plain node )
    {
    }

    ////// class A
    ////// class A extends B
    // ClassDeclType ::= (ClassDeclType_Plain  ) CLASS_K ident:ClassName;
    @Override
    public void visit( ClassDeclType_Plain node )
    {
    }
    // ClassDeclType ::= (ClassDeclType_Extends) CLASS_K ident:ClassName EXTENDS_K Type;
    @Override
    public void visit( ClassDeclType_Extends node )
    {
    }
    // ClassDeclType ::= (ClassDeclType_Err    ) CLASS_K error {: parser.report_error( "Bad class declaration", null ); :};
    @Override
    public void visit( ClassDeclType_Err node )
    {
    }

    ////// <epsilon>
    ////// { method method method }
    ////// vardl vardl vardl vardl
    ////// vardl vardl vardl vardl { method method method }
    // ClassDeclBody ::= (ClassDeclBody_Vars       ) VarDeclList;
    @Override
    public void visit( ClassDeclBody_Vars node )
    {
    }
    // ClassDeclBody ::= (ClassDeclBody_VarsMethods) VarDeclList lbrace OpenScope MethodDeclList rbrace;
    @Override
    public void visit( ClassDeclBody_VarsMethods node )
    {
    }

    ////// <epsilon>
    ////// method method method
    // MethodDeclList ::= (MethodDeclList_Tail ) MethodDeclList MethodDecl;
    @Override
    public void visit( MethodDeclList_Tail node )
    {
    }
    // MethodDeclList ::= (MethodDeclList_Empty) ;
    @Override
    public void visit( MethodDeclList_Empty node )
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
    public void visit( MethodDecl_Plain node )
    {
    }

    ////// void foo
    ////// A foo
    // MethodDeclType ::= (MethodDeclType_Plain) ReturnType ident:MethodName;
    @Override
    public void visit( MethodDeclType_Plain node )
    {
    }

    ////// <epsilon>
    ////// int ident, Node Array[], char c
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

    ////// int a, char c, Node Array[]
    // FormParsList ::= (FormParsList_Init)                    FormParam;
    @Override
    public void visit( FormParsList_Init node )
    {
    }
    // FormParsList ::= (FormParsList_Tail) FormParsList comma FormParam;
    @Override
    public void visit( FormParsList_Tail node )
    {
    }

    ////// int a, char c, Node Array[]
    // FormParam ::= (FormParam_Plain) Type VarIdent;
    @Override
    public void visit( FormParam_Plain node )
    {
    }
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};
    @Override
    public void visit( FormParam_Err node )
    {
    }

    ////// <epsilon>
    ////// vardl vardl vardl vardl
    // VarDeclList ::= (VarDeclList_VarDecl) VarDeclList VarDecl;
    @Override
    public void visit( VarDeclList_VarDecl node )
    {
    }
    // VarDeclList ::= (VarDeclList_Empty  ) ;
    @Override
    public void visit( VarDeclList_Empty node )
    {
    }



    ////// int a, b[], c;
    ////// A a1, a2;
    ////// static int a, b[], c;   // the static keyword is only allowed inside a class declaration!
    ////// static A a1, a2;
    // VarDecl ::= (VarDecl_Plain) VarDeclType VarIdentList semicol;
    @Override
    public void visit( VarDecl_Plain node )
    {
    }

    ////// int
    ////// static A
    // VarDeclType ::= (VarDeclType_Plain )          Type;
    @Override
    public void visit( VarDeclType_Plain node )
    {
    }
    // VarDeclType ::= (VarDeclType_Static) STATIC_K Type;
    @Override
    public void visit( VarDeclType_Static node )
    {
    }
    // VarDeclType ::= (VarDeclType_Err   ) error {: parser.report_error( "Bad class declaration", null ); :};
    @Override
    public void visit( VarDeclType_Err node )
    {
    }

    ////// a
    ////// a, b[], c
    // VarIdentList ::= (VarIdentList_VarIdent)                    VarIdent;
    @Override
    public void visit( VarIdentList_VarIdent node )
    {
    }
    // VarIdentList ::= (VarIdentList_Tail    ) VarIdentList comma VarIdent;
    @Override
    public void visit( VarIdentList_Tail node )
    {
    }

    ////// a
    ////// b[]
    // VarIdent ::= (VarIdent_Ident) ident:VarName;
    @Override
    public void visit( VarIdent_Ident node )
    {
    }
    // VarIdent ::= (VarIdent_Array) ident:VarName lbracket rbracket;
    @Override
    public void visit( VarIdent_Array node )
    {
    }
    // VarIdent ::= (VarIdent_Err  ) error {: parser.report_error( "Bad variable declaration", null ); :};
    @Override
    public void visit( VarIdent_Err node )
    {
    }



    ////// const int a = 5, b = 6, c = 11;
    // ConstDecl ::= (ConstDecl_Plain) ConstDeclType ConstInitList semicol;
    @Override
    public void visit( ConstDecl_Plain node )
    {
    }

    ////// const int
    // ConstDeclType ::= (ConstDeclType_Plain) CONST_K Type;
    @Override
    public void visit( ConstDeclType_Plain node )
    {
    }
    // ConstDeclType ::= (ConstDeclType_Err  ) CONST_K error {: parser.report_error( "Bad constant type", null ); :};
    @Override
    public void visit( ConstDeclType_Err node )
    {
    }

    ////// a = 5, b = 6, c = 11
    // ConstInitList ::= (ConstInitList_Init)                     ConstInit;
    @Override
    public void visit( ConstInitList_Init node )
    {
    }
    // ConstInitList ::= (ConstInitList_Tail) ConstInitList comma ConstInit;
    @Override
    public void visit( ConstInitList_Tail node )
    {
    }

    ////// a = 5
    // ConstInit ::= (ConstInit_Plain) ident:IdentName Assignop Literal;
    @Override
    public void visit( ConstInit_Plain node )
    {
    }
    // ConstInit ::= (ConstInit_Err  ) error {: parser.report_error( "Bad initialization", null ); :};
    @Override
    public void visit( ConstInit_Err node )
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
    // Statement ::= (Statement_Break      ) BREAK_K       semicol;
    @Override
    public void visit( Statement_Break node )
    {
    }
    // Statement ::= (Statement_Continue   ) CONTINUE_K    semicol;
    @Override
    public void visit( Statement_Continue node )
    {
    }
    // Statement ::= (Statement_Return     ) RETURN_K      semicol;
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
    // Statement ::= (Statement_Print      ) PRINT_K lparen Expr                        rparen semicol;
    @Override
    public void visit( Statement_Print node )
    {
    }
    // Statement ::= (Statement_PrintFormat) PRINT_K lparen Expr comma int_lit:MinWidth rparen semicol;
    @Override
    public void visit( Statement_PrintFormat node )
    {
    }
    // Statement ::= (Statement_Scope      ) lbrace OpenScope StatementList rbrace;
    @Override
    public void visit( Statement_Scope node )
    {
    }
    // Statement ::= (Statement_Semicolon  ) semicol;
    @Override
    public void visit( Statement_Semicolon node )
    {
    }
    // Statement ::= (Statement_Err        ) error {: parser.report_error( "Bad statement", null ); :};
    @Override
    public void visit( Statement_Err node )
    {
    }

    ////// ident.ident[ expr ] = expr
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// ident.ident[ expr ]++
    ////// ident.ident[ expr ]--
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

    ////// <epsilon>
    ////// statement statement statement statement
    // StatementList ::= (StatementList_Tail ) StatementList Statement;
    @Override
    public void visit( StatementList_Tail node )
    {
    }
    // StatementList ::= (StatementList_Empty) ;
    @Override
    public void visit( StatementList_Empty node )
    {
    }

    ////// <epsilon>
    ////// case 1: statement statement statement   case 2: statement statement
    // CaseList ::= (CaseList_Tail ) CaseList Case;
    @Override
    public void visit( CaseList_Tail node )
    {
    }
    // CaseList ::= (CaseList_Empty) ;
    @Override
    public void visit( CaseList_Empty node )
    {
    }

    ////// case 1: statement statement statement
    ////// case 2: 
    ////// case 3: {}
    // Case ::= (Case_Plain) CASE_K int_lit:CaseNum colon StatementList;
    @Override
    public void visit( Case_Plain node )
    {
    }



    ////// <epsilon>
    ////// expr
    ////// expr, expr, expr
    // ActPars ::= (ActPars_Plain) ActParsList;
    @Override
    public void visit( ActPars_Plain node )
    {
    }
    // ActPars ::= (ActPars_Empty) ;
    @Override
    public void visit( ActPars_Empty node )
    {
    }

    ////// expr
    ////// expr, expr, expr
    // ActParsList ::= (ActParsList_Expr)                   Expr;
    @Override
    public void visit( ActParsList_Expr node )
    {
    }
    // ActParsList ::= (ActParsList_Tail) ActParsList comma Expr;
    @Override
    public void visit( ActParsList_Tail node )
    {
    }

    ////// expr   or   expr < expr and expr >= expr  or  expr != expr   // 'and' has greater priority than 'or'!
    // Condition ::= (Condition_Term)              CondTerm;
    @Override
    public void visit( Condition_Term node )
    {
    }
    // Condition ::= (Condition_Tail) Condition or CondTerm;
    @Override
    public void visit( Condition_Tail node )
    {
    }

    ////// expr < expr and expr >= expr
    // CondTerm ::= (CondTerm_Fact)              CondFact;
    @Override
    public void visit( CondTerm_Fact node )
    {
    }
    // CondTerm ::= (CondTerm_Tail) CondTerm and CondFact;
    @Override
    public void visit( CondTerm_Tail node )
    {
    }

    ////// expr < expr and expr >= expr
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

    ////// +term - term + term + term
    // Expr ::= (Expr_Addition) Addition;
    @Override
    public void visit( Expr_Addition node )
    {
    }
    // Expr ::= (Expr_Err     ) error {: parser.report_error( "Bad expression", null ); :};
    @Override
    public void visit( Expr_Err node )
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
    public void visit( Addition_Term node )
    {
    }
    // Addition ::= (Addition_STerm)          Addop Term;
    @Override
    public void visit( Addition_STerm node )
    {
    }
    // Addition ::= (Addition_Tail ) Addition Addop Term;
    @Override
    public void visit( Addition_Tail node )
    {
    }

    ////// factor
    ////// factor*factor*factor
    // Term ::= (Term_Factor)            Factor;
    @Override
    public void visit( Term_Factor node )
    {
    }
    // Term ::= (Term_Tail  ) Term Mulop Factor;
    @Override
    public void visit( Term_Tail node )
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

    ////// ident
    ////// ident.ident
    ////// ident[ expr ]
    ////// ident.ident.ident[ expr ].ident
    ////// ident.ident.ident[ expr ].ident[ expr ]
    // Designator ::= (Designator_Ident  ) ident:Name;
    @Override
    public void visit( Designator_Ident curr )
    {
        // try to find the variable in the symbol table
        curr.symbol = SymbolTable.findSymbol( curr.getName() );

        // if the variable does not exist in the current scopes
        if( curr.symbol == SymbolTable.noSym )
        {
            // then it has not been declared, return
            report_error( curr, "This variable has not been declared", false );
            return;
        }
    }
    // Designator ::= (Designator_Field  ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Field curr )
    {
        // set the current designator to the default value
        curr.symbol = SymbolTable.noSym;

        // if the previous designator segment does not exist
        Designator prev = curr.getDesignator();
        if( prev.symbol == SymbolTable.noSym )
        {
            // an error must have been reported somewhere in the previous segments, return
            return;
        }

        // if the previous variable is not a class (doesn't have inner methods)
        Struct prevType = prev.symbol.getType();
        if( prevType.getKind() != Struct.Class )
        {
            // report an error and return
            report_error( curr, "Cannot access inner members since the variable is not a class", false );
            return;
        }

        // go through the fields and members of the previous designator
        for( Obj member : prevType.getMembers() )
        {
            // if the previous variable (designator segment) contains the current field/member
            if( curr.getName().equals( member.getName() ) )
            {
                // if the member is not a variable or method
                int memberKind = member.getKind();
                if( memberKind != Obj.Var && memberKind != Obj.Meth )
                {
                    // report an error and return
                    report_error( curr, "Cannot access the requested member since it is not a variable or method", false );
                    return;
                }
                
                // // clone the current object and save it as the 
                // curr.obj = new Object(  );
                // // TODO: clone the object

                // stop the search
                break;
            }
        }

        // if the previous variable doesn't contain the current field/member
        if( curr.symbol == SymbolTable.noSym )
        {
            // report an error and return
            report_error( curr, "The requested member does not exist inside the variable", false );
            return;
        }
    }
    // Designator ::= (Designator_ArrElem) Designator lbracket Expr rbracket;
    @Override
    public void visit( Designator_ArrElem curr )
    {
        // set the current designator to the default value
        curr.symbol = SymbolTable.noSym;

        // if the previous designator segment does not exist
        Designator prev = curr.getDesignator();
        if( prev.symbol == SymbolTable.noSym )
        {
            // an error must have been reported somewhere in the previous segments, return
            return;
        }

        Struct prevType = prev.symbol.getType();
        // if the previous variable is not an array
        if( prevType.getKind() != Struct.Array )
        {
            // report an error and return
            report_error( curr, "Cannot access the requested array element, since the variable is not an array", false );
            return;
        }
        // // if the expression inside the angle brackets does not result in an int
        // if( curr.getExpr().obj )
        // {
        //     // report an error and return
        //     report_error( curr, "Cannot access the requested array element, since the variable is not an array", false );
        //     return;
        // }

        // // get the type of array element from the array type
        // Struct currType = prevType.getElemType();

        // // create a new object
        // curr.obj = new Obj( kind, name, type );
        // prevType.getElemType().get;
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
    // ReturnType ::= (ReturnType_Ident) ident :ReturnType;
    ////// int | bool | char | ident
    // Type ::= (Type_Ident) ident:Type;
    ////// 1202 | 'c' | true
    // Literal ::= (Literal_Int ) int_lit :Literal;
    // Literal ::= (Literal_Char) char_lit:Literal;
    // Literal ::= (Literal_Bool) bool_lit:Literal;

    ////// =
    // Assignop ::= (Assignop_Assign) assign:Assignop;
    ////// ==  |  !=  |  >  |  >=  |  <  |  <=
    // Relop ::= (Relop_Eq ) eq:Relop;
    // Relop ::= (Relop_Neq) ne:Relop;
    // Relop ::= (Relop_Gt ) gt:Relop;
    // Relop ::= (Relop_Geq) ge:Relop;
    // Relop ::= (Relop_Lt ) lt:Relop;
    // Relop ::= (Relop_Leq) le:Relop;
    ////// +  |  -
    // Addop ::= (Addop_Plus ) plus :Addop;
    // Addop ::= (Addop_Minus) minus:Addop;
    ////// *  |  /  |  %
    // Mulop ::= (Mulop_Mul ) mul :Mulop;
    // Mulop ::= (Mulop_Div ) div :Mulop;
    // Mulop ::= (Mulop_Perc) perc:Mulop;
















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
