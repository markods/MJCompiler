package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.CodeGen;
import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.Symbol;
import rs.ac.bg.etf.pp1.SymbolTable;
import rs.ac.bg.etf.pp1.SymbolType;
import rs.ac.bg.etf.pp1.TokenCode;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.props.*;
import rs.ac.bg.etf.pp1.props.JumpProp.JumpRecord;


public class CodeGenVisitor extends VisitorAdaptor
{
    private final Compiler.State state;
    private final Context context = new Context();
    
    private static class Context
    {
        public final StackProp<SyntaxNode> syntaxNodeStack = new StackProp<>();
    }
    private SymbolTable _symbolTable() { return state._symbolTable(); }
    private CodeGen _codeGen() { return state._codeGen(); }

    public CodeGenVisitor( Compiler.State state )
    {
        this.state = state;
    }

    // underscore all error tokens
    private void report_error( SyntaxNode node, String message )
    {
        state._errors().add( CompilerError.CODEGEN_ERROR, message, node, true, false );
    }
    // underscore all error tokens and throw an exception
    private void report_fatal( SyntaxNode node, String message )
    {
        state._errors().add( CompilerError.CODEGEN_ERROR, message, node, true, true );
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
        // if the symbol table has more or less scopes open than expected
        // +   global -> program
        if( _symbolTable()._localsLevel() != 0 )
        {
            report_error( curr.getProgramType(), String.format( "Unexpected symbol table scope level: %d (expected 0)", _symbolTable()._localsLevel() ) );
        }
        // if the syntax node stack is not empty
        if( context.syntaxNodeStack.size() != 0 )
        {
            report_error( curr.getProgramType(), "Syntax node stack not empty" );
        }
        // close the program's scope
        _symbolTable().closeScope();
    }

    ////// program my_program
    // ProgramType ::= (ProgramType_Plain) PROGRAM_K ident:ProgramName;
    @Override
    public void visit( ProgramType_Plain curr )
    {
        // open the program's scope and add the program's symbols to it
        _symbolTable().openScope();
        _symbolTable().addSymbols( curr.symbol._locals() );

        // initialize the predefined methods' addresses and add their code to the code segment
        {
            // reserve the null address with dummy code (so that methods don't start at null)
            _codeGen().loadConst( 0 );
            _codeGen().i_epop();

            ////// char chr( int i );
            _symbolTable().findSymbol( "chr" )._address( _codeGen()._pc32() );
            _codeGen().i_enter( 1, 1 );
            {
                // load the given int to the expression stack
                _codeGen().i_load_0();
                // calculate the int's remainder with 256
                _codeGen().loadConst( 256 );
                _codeGen().i_rem();
                // voila -- the resulting character is on the expression stack
            }
            _codeGen().i_exit();
            _codeGen().i_return();
            
            ////// int ord( char c );
            _symbolTable().findSymbol( "ord" )._address( _codeGen()._pc32() );
            _codeGen().i_enter( 1, 1 );
            {
                // load the zeroth method parameter to the expression stack
                _codeGen().i_load_0();
                // that's it, the character's index is on the expression stack
            }
            _codeGen().i_exit();
            _codeGen().i_return();

            ////// int len( anyType arr[] );
            _symbolTable().findSymbol( "len" )._address( _codeGen()._pc32() );
            _codeGen().i_enter( 1, 1 );
            {
                // load the array pointer to the expression stack
                _codeGen().i_load_0();
                // put the array length on the expression stack
                _codeGen().i_arraylength();
                // return
            }
            _codeGen().i_exit();
            _codeGen().i_return();
        }
    }

    ////// <epsilon>
    ////// constdl constdl vardl vardl classdl
    // GlobalDeclList ::= (GlobalDeclList_Tail ) GlobalDeclList GlobalDecl;
    // GlobalDeclList ::= (GlobalDeclList_Empty) ;

    ////// constdl
    ////// vardl
    ////// classdl
    ////// recorddl
    // GlobalDecl ::= (GlobalDecl_Const ) ConstDecl;
    // GlobalDecl ::= (GlobalDecl_Var   ) VarDecl;
    // GlobalDecl ::= (GlobalDecl_Class ) ClassDecl;
    // GlobalDecl ::= (GlobalDecl_Record) RecordDecl;



    ////// class A { }
    ////// class A { { method method method } }
    ////// class A extends B { vardl vardl vardl vardl }
    ////// class A extends B { vardl vardl vardl vardl { method method method } }
    // ClassDecl ::= (ClassDecl_Plain) ClassDeclType lbrace ClassDeclBody rbrace;
    @Override
    public void visit( ClassDecl_Plain curr )
    {
        // remove the class declaration type node from the syntax node stack
        context.syntaxNodeStack.remove();
        // close the class's scope
        _symbolTable().closeScope();
    }

    ////// class A
    ////// class A extends B
    // ClassDeclType ::= (ClassDeclType_Plain  ) CLASS_K ident:ClassName;
    @Override
    public void visit( ClassDeclType_Plain curr )
    {
        visit_ClassDeclType( curr );
    }
    // ClassDeclType ::= (ClassDeclType_Extends) CLASS_K ident:ClassName EXTENDS_K Type;
    @Override
    public void visit( ClassDeclType_Extends curr )
    {
        visit_ClassDeclType( curr );
    }
    // ClassDeclType ::= (ClassDeclType_Err    ) CLASS_K error {: parser.report_error( "Bad class declaration", null ); :};
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_ClassDeclType( ClassDeclType curr )
    {
        // add the class declaration type node to the syntax node stack
        context.syntaxNodeStack.add( curr );

        // the class symbol has already been added to the symbol table in the SemanticVisitor
     // _symbolTable().addSymbol( curr.symbol );
        SymbolType classType = curr.symbol._type();

        // open the class scope and add the class's members to the scope
        _symbolTable().openScope();
        _symbolTable().addSymbols( classType._members() );

        // add a dummy 'this' constant to the symbol table scope
        // +   set its value to be 0 (equal to the null constant)
        Symbol thisSymbol = Symbol.newConst( "this", classType, 0 );
        _symbolTable().addSymbol( thisSymbol );
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



    ////// record A { }
    ////// record A { vardl vardl vardl vardl }
    // RecordDecl ::= (RecordDecl_Plain) RecordDeclType lbrace RecordDeclBody rbrace;

    ////// record A
    // RecordDeclType ::= (RecordDeclType_Plain) RECORD_K ident:RecordName;
    // RecordDeclType ::= (RecordDeclType_Err  ) RECORD_K error {: parser.report_error( "Bad record declaration", null ); :};

    ////// <epsilon>
    ////// vardl vardl vardl vardl
    // RecordDeclBody ::= (RecordDeclBody_Vars) VarDeclList;



    ////// void foo() { }
    ////// void foo() { statement statement }
    ////// void foo() vardl vardl { }
    ////// void foo() vardl vardl { statement statement }
    //////      foo() vardl vardl { statement statement }   -- constructor
    ////// void foo( int a, char c, Node Array[] ) { }
    ////// void foo( int a, char c, Node Array[] ) { statement statement }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { statement statement }
    // MethodDecl ::= (MethodDecl_Plain) MethodDeclType FormParsScope FormPars rparen MethodDeclBody VarDeclList MethodDeclCode lbrace StatementList rbrace;    @Override
    public void visit( MethodDecl_Plain curr )
    {
        // remove the <method declaration code> node from the syntax node stack
        context.syntaxNodeStack.remove();
        // close the method's scope
        _symbolTable().closeScope();

        // get the method's symbol
        Symbol method = curr.getMethodDeclType().symbol;

        // if this method is the main method, or the method doesn't return anything
        if( method.isMain() || method._type().isVoidType() )
        {
            // add an exit instruction at the end of the method, followed by a return instruction
            // +    used to gracefully stop the program execution for main and functions that don't return anything
            _codeGen().i_exit(); _codeGen().i_return();
        }
        else
        {
            // add a trap instruction at the end of the method, to catch in runtime! any program paths that don't return a value in the method but should
            // +    weird, but according to the microjava specification
            _codeGen().i_trap( 1 );
        }
    }

    ////// void foo
    ////// A foo
    ////// foo   -- constructor
    // MethodDeclType ::= (MethodDeclType_Plain) ReturnType ident:MethodName;
    @Override
    public void visit( MethodDeclType_Plain curr )
    {
        visit_MethodDeclType( curr );
    }
    // MethodDeclType ::= (MethodDeclType_Empty)            ident:MethodName;
    @Override
    public void visit( MethodDeclType_Empty curr )
    {
        visit_MethodDeclType( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_MethodDeclType( MethodDeclType curr )
    {
        // get the method's symbol
        Symbol method = curr.symbol;

        // if this method is the main method
        if( method.isMain() )
        {
            // set the program entry point (before the actual main method)
            _codeGen()._entryAddr32Set();

            // initialize all class's virtual tables before the main method
            // +   the main method is located in the program scope (global -> program)
            // +   that's why we can filter these symbols for class definitions (classes are defined in the program scope)
            for( Symbol classDef : _symbolTable()._locals() )
            {
                // skip any non-class definitions
                if( !classDef.isType() || !classDef._type().isClass() ) continue;

                // initialize the class's virtual table in the static segment
                _codeGen().initVirtualTable( classDef );
            }
        }

        // open the function/method scope and add the function/method's members to the scope
        _symbolTable().openScope();
        _symbolTable().addSymbols( curr.symbol._params() );

        // set the method's address
        method._address( _codeGen()._pc32() );
    }

    ////// action symbol for opening a new scope
    // MethodDeclBody ::= (MethodDeclBody_Plain) ;

    ////// action symbol for the beginning of the method code
    // MethodDeclCode ::= (MethodDeclCode_Plain) ;
    @Override
    public void visit( MethodDeclCode_Plain curr )
    {
        // add the <method declaration code> node to the syntax node stack
        context.syntaxNodeStack.add( curr );

        // get the method's symbol
        Symbol method = ( ( MethodDecl_Plain )curr.getParent() ).getMethodDeclType().symbol;
        // initialize the method's stack frame
        int thisParamInc = ( method.isMethod() ) ? 1 : 0;
        _codeGen().i_enter( thisParamInc + method._paramCount(), thisParamInc + _symbolTable()._localsStackFrameSize() );
    }

    ////// <epsilon>
    ////// int ident, Node Array[], char c
    // FormPars ::= (FormPars_List ) FormParsList;
    // FormPars ::= (FormPars_Empty) ;

    ////// action symbol for opening a new scope
    // FormParsScope ::= (FormParsScope_Plain) lparen;

    ////// int a, char c, Node Array[]
    // FormParsList ::= (FormParsList_Init)                    FormParam;
    // FormParsList ::= (FormParsList_Tail) FormParsList comma FormParam;

    ////// int a, char c, Node Array[]
    // FormParam ::= (FormParam_Plain) FormParamType VarIdent;
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};

    ////// int
    ////// Node
    // FormParamType ::= (FormParamType_Plain) Type;

    ////// <epsilon>
    ////// vardl vardl vardl vardl
    // VarDeclList ::= (VarDeclList_VarDecl) VarDeclList VarDecl;
    // VarDeclList ::= (VarDeclList_Empty  ) ;



    ////// int a, b[], c;
    ////// A a1, a2;
    ////// static int a, b[], c;   // the static keyword is only allowed inside a class declaration!
    ////// static A a1, a2;
    // VarDecl ::= (VarDecl_Plain) VarDeclType VarIdentList semicol;

    ////// int
    ////// static A
    // VarDeclType ::= (VarDeclType_Plain )          Type;
    // VarDeclType ::= (VarDeclType_Static) STATIC_K Type;
    // VarDeclType ::= (VarDeclType_Err   ) error {: parser.report_error( "Bad class declaration", null ); :};

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
        visit_VarIdent( curr, curr.getVarName(), false );
    }
    // VarIdent ::= (VarIdent_Array) ident:VarName lbracket rbracket;
    @Override
    public void visit( VarIdent_Array curr )
    {
        visit_VarIdent( curr, curr.getVarName(), true );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_VarIdent( VarIdent curr, String varName, boolean isArray )
    {
        // if the current symbol is a local variable
        if( curr.symbol.isVar() && !curr.symbol.isGlobal() )
        {
            // add the symbol to the current symbol table scope
            _symbolTable().addSymbol( curr.symbol );
        }
    }
    // VarIdent ::= (VarIdent_Err  ) error {: parser.report_error( "Bad variable declaration", null ); :};



    ////// const int a = 5, b = 6, c = 11;
    // ConstDecl ::= (ConstDecl_Plain) ConstDeclType ConstInitList semicol;

    ////// const int
    // ConstDeclType ::= (ConstDeclType_Plain) CONST_K Type;
    // ConstDeclType ::= (ConstDeclType_Err  ) CONST_K error {: parser.report_error( "Bad constant type", null ); :};

    ////// a = 5, b = 6, c = 11
    // ConstInitList ::= (ConstInitList_Init)                     ConstInit;
    // ConstInitList ::= (ConstInitList_Tail) ConstInitList comma ConstInit;

    ////// a = 5
    // ConstInit ::= (ConstInit_Plain) ident:IdentName Assignop Literal;
    // ConstInit ::= (ConstInit_Err  ) error {: parser.report_error( "Bad initialization", null ); :};






    ////// <epsilon>
    ////// labstatement statement { statement statement } statement { }
    // StatementList ::= (StatementList_Tail ) StatementList Statement;
    // StatementList ::= (StatementList_Empty) ;

    ////// label_01:stmt
    ////// {}
    ////// { label1:statement label2:statement label3:statement }
    // Statement ::= (Statement_Plain)           Stmt;
    // Statement ::= (Statement_Label) StmtLabel Stmt;
    // Statement ::= (Statement_Scope) lbrace StatementList rbrace;
    // Statement ::= (Statement_Err  ) error {: parser.report_error( "Bad statement", null ); :};

    ////// action symbol for defining a label
    // StmtLabel ::= (StmtLabel_Plain) ident:Label;
    @Override
    public void visit( StmtLabel_Plain curr )
    {
        // find the surrounding method declaration
        MethodDeclCode_Plain methodDecl = ( MethodDeclCode_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodDeclCode_Plain )
        );

        // set the label's jump point's address
        // +   the method's labels have already been added in the semantic pass
        methodDecl.jumpprop.get( String.format( "@Label_%s", curr.getLabel() ) )._pointAddress( _codeGen()._pc32() );
    }

    ////// ident.ident[ expr ] = expr;
    ////// ident.ident[ expr ]( );
    ////// ident.ident[ expr ]( expr, expr, expr );
    ////// ident.ident[ expr ]++;
    ////// ident.ident[ expr ]--;
    //////
    ////// if( condition ) statement
    ////// if( condition ) statement else statement
    ////// do statement while( condition );
    ////// switch( expr ) { }
    ////// switch( expr ) { case 1: statement statement statement   case 2: statement statement }
    ////// break;
    ////// continue;
    ////// return;
    ////// return expr;
    ////// goto label_01;
    //////
    ////// read( ident.ident[ expr ] );
    ////// print( ident.ident[ expr ], 2 );
    //////
    ////// ;
    // Stmt ::= (Stmt_Designator ) DesignatorStmt semicol;
    // Stmt ::= (Stmt_If         ) IfScope lparen IfCondition rparen IfStmt;
    @Override
    public void visit( Stmt_If curr )
    {
        context.syntaxNodeStack.remove();
        IfScope scope = curr.getIfScope();

        // set the if statement's exit point here
        scope.jumpprop.get( "@End" )._pointAddress( _codeGen()._pc32() );
    }
    // Stmt ::= (Stmt_IfElse     ) IfScope lparen IfCondition rparen IfStmt ELSE_K ElseStmt;
    @Override
    public void visit( Stmt_IfElse curr )
    {
        context.syntaxNodeStack.remove();
        IfScope scope = curr.getIfScope();

        // set the if-else statement's exit point here
        scope.jumpprop.get( "@End" )._pointAddress( _codeGen()._pc32() );
    }
    // Stmt ::= (Stmt_DoWhile    ) DoWhileScope DoWhileStmt WHILE_K lparen DoWhileCondition rparen semicol;
    @Override
    public void visit( Stmt_DoWhile curr )
    {
        context.syntaxNodeStack.remove();
        DoWhileScope scope = curr.getDoWhileScope();

        // set the do-while statement's exit point here
        scope.jumpprop.get( "@FalseBranch" )._pointAddress( _codeGen()._pc32() );
        scope.jumpprop.get( "@End" )._pointAddress( _codeGen()._pc32() );
    }
    // Stmt ::= (Stmt_Switch     ) SWITCH_K lparen SwitchExpr rparen lbrace CaseList rbrace;
    @Override
    public void visit( Stmt_Switch curr )
    {
        context.syntaxNodeStack.remove();
        SwitchScope scope = curr.getSwitchScope();

        // set the switch statement's exit point here
        scope.jumpprop.get( "@End" )._pointAddress( _codeGen()._pc32() );
    }
    // Stmt ::= (Stmt_Break      ) BREAK_K       semicol;
    @Override
    public void visit( Stmt_Break curr )
    {
        // find the surrounding do-while or switch statement
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain
                   || elem instanceof SwitchScope_Plain )
        );

        JumpProp jumpprop = null;
        if     ( scope instanceof DoWhileScope_Plain ) jumpprop = ( ( DoWhileScope_Plain )scope ).jumpprop;
        else if( scope instanceof SwitchScope_Plain  ) jumpprop = ( ( SwitchScope_Plain  )scope ).jumpprop;
        
        // unconditionally jump to the end of the do-while or switch statement
        // +    get the jump-instruction's starting address
        int pointA = _codeGen().jump( CodeGen.NO_ADDRESS );

        // mark the jump instruction's offset to be fixed later
        jumpprop.get( "@End" )._addAddressToFix( pointA );
    }
    // Stmt ::= (Stmt_Continue   ) CONTINUE_K    semicol;
    @Override
    public void visit( Stmt_Continue curr )
    {
        // find the surrounding do-while statement
        DoWhileScope_Plain scope = ( DoWhileScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain )
        );

        // unconditionally jump to the beginning of the do-while statement's condition
        // +    get the jump-instruction's starting address
        int pointA = _codeGen().jump( CodeGen.NO_ADDRESS );

        // mark the jump instruction's offset to be fixed later
        scope.jumpprop.get( "@Cond" )._addAddressToFix( pointA );
    }
    // Stmt ::= (Stmt_Return     ) RETURN_K      semicol;
    @Override
    public void visit( Stmt_Return curr )
    {
        _codeGen().i_exit();
        _codeGen().i_return();
    }
    // Stmt ::= (Stmt_ReturnExpr ) RETURN_K Expr semicol;
    @Override
    public void visit( Stmt_ReturnExpr curr )
    {
        _codeGen().i_exit();
        _codeGen().i_return();
    }
    // Stmt ::= (Stmt_Goto       ) GOTO_K ident:Label semicol;
    @Override
    public void visit( Stmt_Goto curr )
    {
        // find the surrounding method declaration
        MethodDeclCode_Plain methodDecl = ( MethodDeclCode_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodDeclCode_Plain )
        );

        // unconditionally jump to the label
        // +    get the jump-instruction's starting address
        int pointA = _codeGen().jump( CodeGen.NO_ADDRESS );

        // initialize the jump instruction's address
        // +   the actual address will be fixed once the label's address is resolved
        methodDecl.jumpprop.get( String.format( "@Label_%s", curr.getLabel() ) )._addAddressToFix( pointA );
    }
    // Stmt ::= (Stmt_Read       ) READ_K lparen Designator rparen semicol;
    @Override
    public void visit( Stmt_Read curr )
    {
        // read the value from the standard input
        _codeGen().read( curr.getDesignator().symbol._type() );
        // store the read value in the symbol
        _codeGen().storeSymbolValue( curr.getDesignator().symbol );
    }
    // Stmt ::= (Stmt_Print      ) PRINT_K lparen Expr                        rparen semicol;
    @Override
    public void visit( Stmt_Print curr )
    {
        _codeGen().loadConst( 0 );
        _codeGen().print( curr.getExpr().symbol._type() );
    }
    // Stmt ::= (Stmt_PrintFormat) PRINT_K lparen Expr comma int_lit:MinWidth rparen semicol;
    @Override
    public void visit( Stmt_PrintFormat curr )
    {
        _codeGen().loadConst( curr.getMinWidth() );
        _codeGen().print( curr.getExpr().symbol._type() );
    }

    ////// action symbols for opening a new scope and the if-statement's jump instructions
    // IfScope ::= (IfScope_Plain) IF_K;
    @Override
    public void visit( IfScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );
    }
    // IfCondition ::= (IfCondition_Plain) Condition;
    @Override
    public void visit( IfCondition_Plain curr )
    {
        // find the surrounding if/if-else statement
        IfScope_Plain scope = ( IfScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof IfScope_Plain )
        );

        // set the if statement's starting address
        scope.jumpprop.get( "@TrueBranch" )._pointAddress( _codeGen()._pc32() );
    }
    // IfStmt ::= (IfStmt_Plain) Stmt;
    @Override
    public void visit( IfStmt_Plain curr )
    {
        // find the surrounding if/if-else statement
        IfScope_Plain scope = ( IfScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof IfScope_Plain )
        );

        // if there is a following 'else'
        if( curr.getParent() instanceof Stmt_IfElse )
        {
            // add an unconditional jump to the end of the if-else statement
            int pointA = _codeGen().jump( CodeGen.NO_ADDRESS );
            scope.jumpprop.get( "@End" )._addAddressToFix( pointA );
        }

        // set the else statement's starting address (equal to the end address if 'else' doesn't exist)
        // IMPORTANT: this code should be the last code in this visitor function
        scope.jumpprop.get( "@FalseBranch" )._pointAddress( _codeGen()._pc32() );
    }
    // ElseStmt ::= (ElseStmt_Plain) Stmt;

    ////// action symbols for opening a new scope and the do-while statement's jump instructions
    // DoWhileScope ::= (DoWhileScope_Plain) DO_K;
    @Override
    public void visit( DoWhileScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );

        // set the address of the first instruction in the do-while statement
        curr.jumpprop.get( "@TrueBranch" )._pointAddress( _codeGen()._pc32() );
    }
    // DoWhileStmt ::= (DoWhileStmt_Plain) Statement;
    @Override
    public void visit( DoWhileStmt_Plain curr )
    {
        // find the surrounding do-while statement
        DoWhileScope_Plain scope = ( DoWhileScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain )
        );

        // set the do-while-condition's entry point
        scope.jumpprop.get( "@Cond" )._pointAddress( _codeGen()._pc32() );
    }
    // DoWhileCondition ::= (DoWhileCondition_Plain) Condition;

    ////// action symbols for opening a new scope and the switch statement's jump instructions
    // SwitchScope ::= (SwitchScope_Plain) SWITCH_K;
    @Override
    public void visit( SwitchScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );
    }
    // SwitchExpr ::= (SwitchExpr_Plain) Expr;
    @Override
    public void visit( SwitchExpr_Plain curr )
    {
        // find the switch scope surrounding this symbol
        SwitchScope_Plain scope = ( SwitchScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof SwitchScope_Plain )
        );

        // for all the switch's cases in the order they appeared (default not yet supported)
        for( JumpRecord caseRecord : scope.jumpprop )
        {
            // get the <switch case>'s integer number
            int value_i32 = 0;
            try
            {
                String string_i32 = caseRecord._pointName()/*"@Case_%d"*/.substring( 6 );
                value_i32 = Integer.parseInt( string_i32 );
            }
            // if the jump record name isn't in the form "@Case_%d", skip that jump record
            catch( NumberFormatException | StringIndexOutOfBoundsException ex )
            {
                continue;
            }

            // duplicate the switch-expression's result, so that it is there for other conditional jumps as well
            // +    (the expression stack's top duplicate gets consumed whenever the conditional jump's condition is evaluated)
            _codeGen().i_dup();

            // add the case statement's value to the expression stack
            _codeGen().loadConst( value_i32 );

            // initialize the jump instruction's address
            // +    jump if the condition is not true
            int pointCaseX = _codeGen().jumpIf( TokenCode.eq, CodeGen.NO_ADDRESS );
            scope.jumpprop.get( caseRecord._pointName() )._addAddressToFix( pointCaseX );
        }

        // remove the last duplicate, since there are no more cases left (default not yet supported)
        _codeGen().i_epop();
        // jump unconditionally to the first instruction after the switch statement
        int pointSkipAllCases = _codeGen().jump( CodeGen.NO_ADDRESS );

        // add the switch's exit point
        scope.jumpprop.get( "@End" )._addAddressToFix( pointSkipAllCases );
    }

    ////// ident.ident[ expr ] = expr
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// ident.ident[ expr ]++
    ////// ident.ident[ expr ]--
    // DesignatorStmt ::= (DesignatorStmt_Assign    ) Designator Assignop Expr;
    @Override
    public void visit( DesignatorStmt_Assign curr )
    {
        visit_UpdateDesignatorValue( curr, curr.getDesignator().symbol );
    }
    // DesignatorStmt ::= (DesignatorStmt_Call      ) MethodCall ActParsScope ActPars rparen;
    @Override
    public void visit( DesignatorStmt_Call curr )
    {
        // get the <designator> from the <method call> syntax node
        MethodCall methodCall = curr.getMethodCall();
        Designator methodDesign = null;
        if( methodCall instanceof MethodCall_Plain ) methodDesign = ( ( MethodCall_Plain )methodCall ).getDesignator();
        else                                         report_fatal( curr, "Method call type not yet supported" );

        // call the function/method/static_method and discard its return value from the expression stack (if it exists)
        visit_MethodOrFunctionCall( curr, methodDesign, methodCall.symbol, false );
    }
    // DesignatorStmt ::= (DesignatorStmt_Plusplus  ) Designator plusplus;
    @Override
    public void visit( DesignatorStmt_Plusplus curr )
    {
        visit_UpdateDesignatorValue( curr, curr.getDesignator().symbol );
    }
    // DesignatorStmt ::= (DesignatorStmt_Minusminus) Designator minusminus;
    @Override
    public void visit( DesignatorStmt_Minusminus curr )
    {
        visit_UpdateDesignatorValue( curr, curr.getDesignator().symbol );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_UpdateDesignatorValue( DesignatorStmt curr, Symbol designator )
    {
        if( curr instanceof DesignatorStmt_Assign )
        {
            // store the value of the expression to the symbol
            // +   the symbol's address is present on the expression stack (if needed), followed by the expression value
            _codeGen().storeSymbolValue( designator );
            return;
        }

        // if the symbol needs designation by another symbol (whose value must be placed onto the expression stack in order for this symbol to be accessed)
        if( _codeGen().needsPrevDesignatorValue( designator ) )
        {
            // if the last designator segment is not an array element
            if( !designator.isArrayElem() )
            {
                // duplicate the designator address (so that we don't lose it)
                _codeGen().i_dup();
            }
            // if the last designator segment is an array element
            else
            {
                // duplicate the designator address and the array element index (so that we don't lose them)
                _codeGen().i_dup2();
            }
        }

        // load the designator's value to the expression stack
        // +    this uses up one of the duplicate designator's addresses
        // +    (if they are present on the expression stack, that means that they were needed to load and store the designator's value)
        _codeGen().loadSymbolValue( designator );

        // load the constant 1 to the expression stack and add/sub it from the designator
        _codeGen().loadConst( 1 );
        if     ( curr instanceof DesignatorStmt_Plusplus   ) _codeGen().i_add();
        else if( curr instanceof DesignatorStmt_Minusminus ) _codeGen().i_sub();

        // store the updated value back to the designator
        _codeGen().storeSymbolValue( designator );
    }

    ////// <epsilon>
    ////// statement statement statement statement
    // StmtList ::= (StmtList_Tail ) StmtList Stmt;
    // StmtList ::= (StmtList_Empty) ;

    ////// <epsilon>
    ////// case 1: statement statement statement   case 2: statement statement
    // CaseList ::= (CaseList_Tail ) CaseList Case;
    // CaseList ::= (CaseList_Empty) ;

    ////// case 1: statement statement statement
    ////// case 2: 
    ////// case 3: {}
    // Case ::= (Case_Plain) CaseScope StmtList;
    @Override
    public void visit( Case_Plain curr )
    {
        // add a random constant on the stack, which will be removed by the next case
        // +   this supports cases that fall through to the next case (there is a code path that doesn't hit a break/continue/return statement in the case)
        // +   the next case will remove this constant at the beginning (as if it were a switch-expression's value duplicate)
        _codeGen().loadConst( 0 );
    }

    ////// action symbols for opening a new scope and the case-statement's jump instructions
    // CaseScope ::= (CaseScope_Plain) CASE_K int_lit:CaseNum colon;
    @Override
    public void visit( CaseScope_Plain curr )
    {
        // find the switch scope surrounding this symbol
        SwitchScope_Plain scope = ( SwitchScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof SwitchScope_Plain )
        );

        // remove the last switch-expression's result (last duplicate)
        int pointCase = _codeGen().i_epop();

        // update the case's starting address
        scope.jumpprop.get( String.format( "@Case_%d", curr.getCaseNum() ) )._pointAddress( pointCase );
    }



    ////// <epsilon>
    ////// expr
    ////// expr, expr, expr
    // ActPars ::= (ActPars_Plain) ActParsList;
    // ActPars ::= (ActPars_Empty) ;

    ////// action symbol for opening a new scope
    // ActParsScope ::= (ActParsScope_Plain) lparen;


    ////// expr
    ////// expr, expr, expr
    // ActParsList ::= (ActParsList_Expr)                   ActParam;
    // ActParsList ::= (ActParsList_Tail) ActParsList comma ActParam;

    ////// expr
    // ActParam ::= (ActParam_Plain) Expr;
    @Override
    public void visit( ActParam_Plain curr )
    {
        // HACK: swap the expression's value and the class instance pointer on the expression stack
        // +    that way, the class instance pointer always ends up at the top of the stack (similar to how bubble sort works)
        _codeGen().i_dup_x1();   // ExprStack=[...,valA, valB -> ...,valB, valA, valB]
        _codeGen().i_epop();     // ExprStack=[..., val -> ...]
    }



    ////// bool   |   expr < expr   |   expr != expr
    ////// ( expr == expr )
    ////// ( expr >= expr || expr == expr && expr >= expr )   // 'and' has greater priority than 'or' implicitly
    //////       .A                .C        .H         .F             .H             .K   .K        .L        .P   // jumpIfNot(X) to .(&Y)
    ////// if(   M && N   ||   ((( A && B || C ))) && ( D && E || F || G && Q )   ||  H && I && J || K && R || L   )(&&)   O   else(||)   P;
    //////            .O                .D                   .O   .O        .O                  .O        .O        // jumpIf(X) to .(&Y)
    // Condition ::= (Condition_Single ) CondTerm;
    // Condition ::= (Condition_Multi  ) CondTermList;

    ////// ((( true )))                                       // the parentheses belong to the expression! (not to the condition)
    ////// bool && b > c
    ////// ((( ((bool)) && (( (b) > (c) )) )))
    // CondTermList ::= (CondTermList_Aor ) CondTerm     CondTermScope CondTerm;
    // CondTermList ::= (CondTermList_Tail) CondTermList CondTermScope CondTerm;
    
    ////// ((( cterm && cterm || cterm )))   |   expr   |   expr < expr   |   expr != expr
    // CondTerm ::= (CondTerm_Fact) CondFact;
    @Override
    public void visit( CondTerm_Fact curr )
    {
        visit_CondTerm( curr );
    }
    // CondTerm ::= (CondTerm_Nest) CondNest;
    @Override
    public void visit( CondTerm_Nest curr )
    {
        visit_CondTerm( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_CondTerm( CondTerm curr )
    {
        // Condition ::= (Condition_Single ) CondTerm;   // <---   #sentinel
        // Condition ::= (Condition_Multi  ) CondTermList;
        //
        // CondTermList ::= (CondTermList_Aor ) CondTerm     CondTermScope CondTerm;   // <---
        // CondTermList ::= (CondTermList_Tail) CondTermList CondTermScope CondTerm;   // <---
        //
        // CondTerm ::= (CondTerm_Fact) CondFact;   // *
        // CondTerm ::= (CondTerm_Nest) CondNest;   // *
        //
        // CondNest ::= (CondNest_Head) lparen CondTermList rparen;   // #sentinel
        // CondNest ::= (CondNest_Tail) lparen CondNest     rparen;
        //
        // CondFact ::= (CondFact_Expr ) Expr;
        // CondFact ::= (CondFact_Relop) Expr Relop Expr;
        //
        // CondTermScope ::= (CondTermScope_Plain) Aorop;

        // if the current term doesn't have an associated jump instruction, return
        if( !( curr instanceof CondTerm_Fact ) ) return;

        // get the comparison type used in the current term
        int currRelop = curr.jumpprop.get( "@Relop" )._pointAddress();   // HACK: not actually an address, but a token code constant

        // get the and/or operator that comes immediately after the current term (ignore parentheses)
        int nextAorop = TokenCode.invalid;
        SyntaxNode node = curr;
        {
            // if the current entry term in the list should be skipped
            boolean skipListEntry = false;

            while( true )
            {
                if( node instanceof CondTermList )
                {
                    if( !skipListEntry )
                    {
                        CondTermScope scope = null;
                        if     ( node instanceof CondTermList_Tail ) { scope = ( ( CondTermList_Tail )node ).getCondTermScope(); }
                        else if( node instanceof CondTermList_Aor  ) { scope = ( ( CondTermList_Aor  )node ).getCondTermScope(); }

                        nextAorop = ( ( CondTermScope_Plain )scope ).getAorop().symbol._value();
                        break;
                    }

                    skipListEntry = false;
                }
                else if( node instanceof CondTerm )
                {
                    if( node.getParent() instanceof CondTermList )
                    {
                        CondTermList list = ( CondTermList )node.getParent();
                        boolean beforeAorop = false;
                        if     ( list instanceof CondTermList_Tail ) { beforeAorop = node != ( ( CondTermList_Tail )list ).getCondTerm();  }
                        else if( list instanceof CondTermList_Aor  ) { beforeAorop = node != ( ( CondTermList_Aor  )list ).getCondTerm1(); }

                        if( !beforeAorop ) { skipListEntry = true; }
                    }
                }
                else if( node instanceof Condition )
                {
                    if     ( node.getParent() instanceof IfCondition      ) { nextAorop = TokenCode.and; }   // if( ... )(&&) ... else(||) ...
                    else if( node.getParent() instanceof DoWhileCondition ) { nextAorop = TokenCode.or;  }   // do(&&) ... while( ... );   (||)
                    else                                                    { report_fatal( curr, "<Condition parent type> not yet supported" ); }
                    
                    break;
                }

                node = node.getParent();
            }
        }

        //////       .A                .C        .H         .F             .H             .K   .K        .L        .P   // jumpIfNot(X) to .(&Y)
        ////// if(   M && N   ||   ((( A && B || C ))) && ( D && E || F || G && Q )   ||  H && I && J || K && R || L   )(&&)   O   else(||)   P;
        //////            .O                .D                   .O   .O        .O                  .O        .O        // jumpIf(X) to .(&Y)

        //////                                   .P          .P                 .P     .P     // jumpIfNot(X) to .(&Y)
        ////// do(&&)   O   while(   A || ( B || C && ( D || E && ( F || G ) || H ) && G )   );   (||)P
        //////                       .O     .O          .O          .O   .O                   // jumpIf(X) to .(&Y)

        //////             .D     .D                 .P   // jumpIfNot(X) to .(&Y)
        ////// if(   ( ( ( A ) && B ) && C || D ) || E   )(&&)   O   else(||)   P;
        //////                           .O   .O          // jumpIf(X) to .(&Y)

        //////                           .P   .P     .P   // jumpIfNot(X) to .(&Y)
        ////// if(   ( ( ( A ) || B ) || C && D ) && E   )(&&)   O   else(||)   P;
        //////             .E     .E                      // jumpIf(X) to .(&Y)   -- special case for 'or' (A and B jumpIf to E!, not D)

        // get the location to which the current term jumps to (if the current term's jump condition is satisfied)
        JumpProp destJumpprop = null;
        // invert the aorop -- this is the aorop that we are searching for next
        // +   we want to jump immediately after it
        // +   NOTE: this also works for the case when the next aorop is unknown (it should be 'and' by default)
        int wantedAorop = ( nextAorop != TokenCode.and ) ? TokenCode.and : TokenCode.or;
        {
            // if everything in the current! parentheses should be skipped
            boolean skipParens = false;
            // if the current entry term in the list should be skipped
            boolean skipListEntry = false;

            // do this until we find a term that fulfills the specified criteria, or we've run out of terms
            while( true )
            {
                // if we aren't skipping the current parentheses and there are still more terms after the current term
                if( node instanceof CondTermList )
                {
                    if( !skipParens && !skipListEntry )
                    {
                        // get stuff from the syntax node
                        int destAorop = TokenCode.invalid;

                        CondTerm term = null;
                        CondTermScope scope = null;
                        if     ( node instanceof CondTermList_Tail ) {   term = ( ( CondTermList_Tail )node ).getCondTerm();    scope = ( ( CondTermList_Tail )node ).getCondTermScope();   }
                        else if( node instanceof CondTermList_Aor  ) {   term = ( ( CondTermList_Aor  )node ).getCondTerm1();   scope = ( ( CondTermList_Aor  )node ).getCondTermScope();   }
            
                        // get the <possible destination term>'s aorop and jump map
                        destAorop = ( ( CondTermScope_Plain )scope ).getAorop().symbol._value();
                        destJumpprop = ( ( CondTerm )term ).jumpprop;

                        // if the first aor operator after the parentheses is not 'and' but we want 'and'
                        // +   NOTE: this is a special case for or; it defines that 'and' has greater precedence than 'or'
                        if( ( node instanceof CondTermList_Aor )/*first*/ && destAorop != TokenCode.and && wantedAorop == TokenCode.and )
                        {
                            // skip the current parentheses and continue the search
                            skipParens = true;
                        }
                        // otherwise, if the aorops match
                        else if( destAorop == wantedAorop )
                        {
                            // stop the search
                            break;
                        }
                    }

                    // there's now always an aor operator after the current term in these parentheses
                    skipListEntry = false;
                }
                // if we are skipping the current parentheses and we've just exited from (possibly multiply nested) parentheses
                // +   NOTE: only CondTerm_Nest! can be encountered (not CondTerm_Fact or anything similar)
                else if( node instanceof CondTerm )
                {
                    // set that we've just exited from (possibly multiply nested) parentheses
                    //    ((( ... ))) .HERE
                    skipParens = false;

                    if( node.getParent() instanceof CondTermList )
                    {
                        CondTermList list = ( CondTermList )node.getParent();
                        boolean beforeAorop = false;
                        if     ( list instanceof CondTermList_Tail ) { beforeAorop = node != ( ( CondTermList_Tail )list ).getCondTerm();  }
                        else if( list instanceof CondTermList_Aor  ) { beforeAorop = node != ( ( CondTermList_Aor  )list ).getCondTerm1(); }

                        if( !beforeAorop ) { skipListEntry = true; }
                    }
                }
                // if we've hit the end of the condition
                else if( node instanceof Condition )
                {
                    // find the surrounding if/if-else/do-while statement
                    SyntaxNode scope = context.syntaxNodeStack.find(
                        elem -> ( elem instanceof IfScope      )
                             || ( elem instanceof DoWhileScope )
                    );

                    // update the destination jump map
                    if     ( scope instanceof IfScope      ) { destJumpprop = ( ( IfScope      )scope ).jumpprop; }
                    else if( scope instanceof DoWhileScope ) { destJumpprop = ( ( DoWhileScope )scope ).jumpprop; }

                    // stop the search
                    break;
                }

                // continue traversing the condition to the right
                // +   this doesn't enter more-nested parents (only exits from them)
                node = node.getParent();
            }
        }

        // emit code for the jump instruction and update the destination term's jump record
        // +   the destination term will fix the offset for the current jump instruction (when the destination term's starting address becomes known)
        int pointA = CodeGen.NO_ADDRESS;
        if     ( nextAorop == TokenCode.and ) { pointA = _codeGen().jumpIfNot( currRelop, CodeGen.NO_ADDRESS ); destJumpprop.get( "@FalseBranch" )._addAddressToFix( pointA ); }
        else if( nextAorop == TokenCode.or  ) { pointA = _codeGen().jumpIf   ( currRelop, CodeGen.NO_ADDRESS ); destJumpprop.get( "@TrueBranch"  )._addAddressToFix( pointA ); }
        else                                  { report_fatal( curr, "Aorop must be either 'and' or 'or'" ); }
    }

    ////// ((( cterm && cterm || cterm )))
    // CondNest ::= (CondNest_Head) lparen CondTermList rparen;
    // CondNest ::= (CondNest_Tail) lparen CondNest     rparen;

    ////// expr   |   expr < expr   |   expr != expr
    // CondFact ::= (CondFact_Expr ) Expr;
    @Override
    public void visit( CondFact_Expr curr )
    {
        visit_CondFact( curr );
    }
    // CondFact ::= (CondFact_Relop) Expr Relop Expr;
    @Override
    public void visit( CondFact_Relop curr )
    {
        visit_CondFact( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_CondFact( CondFact curr )
    {
        // Condition ::= (Condition_Single ) CondTerm;
        // Condition ::= (Condition_Multi  ) CondTermList;
        //
        // CondTermList ::= (CondTermList_Aor ) CondTerm     CondTermScope CondTerm;
        // CondTermList ::= (CondTermList_Tail) CondTermList CondTermScope CondTerm;
        //
        // CondTerm ::= (CondTerm_Fact) CondFact;   // <---
        // CondTerm ::= (CondTerm_Nest) CondNest;
        //
        // CondNest ::= (CondNest_Head) lparen CondTermList rparen;
        // CondNest ::= (CondNest_Tail) lparen CondNest     rparen;
        //
        // CondFact ::= (CondFact_Expr ) Expr;              // *
        // CondFact ::= (CondFact_Relop) Expr Relop Expr;   // *
        //
        // CondTermScope ::= (CondTermScope_Plain) Aorop;

        // get the possible relational operator used
        // +   also emit a 'false' constant if a boolean expression is checked for truthness
        int relop = TokenCode.invalid;
        if     ( curr instanceof CondFact_Relop ) { relop = ( ( CondFact_Relop )curr ).getRelop().symbol._value(); }
        else if( curr instanceof CondFact_Expr  ) { relop = TokenCode.ne; _codeGen().loadConst( CodeGen.FALSE ); }
        else                                      { report_fatal( curr, "<Condition factor>'s type not yet supported" ); }

        // get the parent term
        CondTerm term = ( CondTerm )curr.getParent();

        // save the current node's possible relational operator to the parent term
        // +   the current node's starting address will be saved by the <term's scope> and <nested term's scope>
        //     (+   because the program counter here isn't actually the starting address for the term)
     // term.jumpprop.get( "@TrueBranch"  )._pointAddress( _codeGen()._pc32() );
     // term.jumpprop.get( "@FalseBranch" )._pointAddress( _codeGen()._pc32() );
        term.jumpprop.get( "@Relop" )._pointAddress( relop );

        // IMPORTANT: the actual jump instruction will be emitted in the parent term, not here!
    }

    ////// action symbols for finding out the next term's starting address
    // CondTermScope ::= (CondTermScope_Plain) Aorop;
    @Override
    public void visit( CondTermScope_Plain curr )
    {
        // get the term that immediately follows the current node in the term list
        CondTerm term = null;
        CondTermList parent = ( CondTermList )curr.getParent();
        if     ( parent instanceof CondTermList_Aor  ) { term = ( ( CondTermList_Aor  )parent ).getCondTerm1(); }
        else if( parent instanceof CondTermList_Tail ) { term = ( ( CondTermList_Tail )parent ).getCondTerm(); }
        else                                           { report_fatal( curr, "<Condition term list>'s type not yet supported" ); }

        // update the term's starting address
        term.jumpprop.get( "@TrueBranch"  )._pointAddress( _codeGen()._pc32() );
        term.jumpprop.get( "@FalseBranch" )._pointAddress( _codeGen()._pc32() );

        // NOTE: this won't cover the first term in any parentheses, but that isn't a problem because
        // +   either the term is the first in the entire condition, in which case nothing from inside the condition can jump to it
        // +   or the term is the first in some nested parentheses, in which case, the outermost parenteses will have a starting address if they are not the first in their parentheses' scope
        //               .HERE       .       .        .
        //     +    A || ((( (( B || C )) && D ))) && E   // dots represent terms that have a starting address
    }



    ////// +term - term + term + term
    // Expr ::= (Expr_Addition) Addition;
    // Expr ::= (Expr_Err     ) error {: parser.report_error( "Bad expression", null ); :};

    ////// term
    ////// +term
    ////// -term
    ////// term + term - term + term
    ////// -term + term - term + term
    ////// +term + term + term + term
    // Addition ::= (Addition_Term )                Term;
    // Addition ::= (Addition_STerm)          Addop Term;
    @Override
    public void visit( Addition_STerm curr )
    {
        if( curr.getAddop().symbol._value() == TokenCode.minus ) _codeGen().i_neg();
    }
    // Addition ::= (Addition_Tail ) Addition Addop Term;
    @Override
    public void visit( Addition_Tail curr )
    {
        switch( curr.getAddop().symbol._value() )
        {
            case TokenCode.plus:  _codeGen().i_add(); break;
            case TokenCode.minus: _codeGen().i_sub(); break;
            default: report_fatal( curr, "Unsupported addition operator code" );
        }
    }

    ////// factor
    ////// factor*factor*factor
    // Term ::= (Term_Factor)            Factor;
    // Term ::= (Term_Tail  ) Term Mulop Factor;
    @Override
    public void visit( Term_Tail curr )
    {
        switch( curr.getMulop().symbol._value() )
        {
            case TokenCode.mul:  _codeGen().i_mul(); break;
            case TokenCode.div:  _codeGen().i_div(); break;
            case TokenCode.perc: _codeGen().i_rem(); break;
            default: report_fatal( curr, "Unsupported multiplication operator code" );
        }
    }



    ////// ident.ident[ expr ]
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// 1202 | 'c' | true
    ////// new Object
    ////// new Array[ expr ]
    ////// ((( expr )))
    // Factor ::= (Factor_Designator ) Designator;
    @Override
    public void visit( Factor_Designator curr )
    {
        // load the designator's value on the expression stack (given the previous designator where the designator evalueation stopped)
        // +   IMPORTANT: this loads the null constant's value
        _codeGen().loadSymbolValue( curr.symbol );
    }
    // Factor ::= (Factor_MethodCall ) MethodCall ActParsScope ActPars rparen;
    @Override
    public void visit( Factor_MethodCall curr )
    {
        // get the <designator> from the <method call> syntax node
        MethodCall methodCall = curr.getMethodCall();
        Designator methodDesign = null;
        if( methodCall instanceof MethodCall_Plain ) methodDesign = ( ( MethodCall_Plain )methodCall ).getDesignator();
        else                                         report_fatal( curr, "Method call type not yet supported" );

        // call the function/method/static_method and keep its return value on the expression stack (if it exists)
        visit_MethodOrFunctionCall( curr, methodDesign, methodCall.symbol, true );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_MethodOrFunctionCall( SyntaxNode curr, Designator functionDesign, Symbol function, boolean saveReturnValueIfItExists )
    {
        // if the function is a placeholder function (with no code), don't call it
        // NOTE: this works for the dummy default constructor
        if( function.isPlaceholder() )
        {
            // check if the function returns anything and its return value is wanted
            if( saveReturnValueIfItExists && !function._type().isVoidType() )
            {
                report_fatal( curr, "Placeholder function/method cannot be called since its return type is not 'void' and its return value is requested" );
            }

            // don't call the placeholder function
            return;
        }

        // get if the method should be called virtually or not (through the virtual table)
        boolean isVirtualCall = false;
        // static method calls and function calls are definitely non-virtual
        // NOTE: constructor is a method, so don't swap the if and else-if conditions
        if( function.isStaticMethod() || function.isFunction() || function.isConstructor() )
        {
            // this works for 'super();' as well, since the <'super' symbol> was replaced with the <supertype constructors' symbol> that it aliases
        }
        // methods are always virtual except when accessed through the 'super' keyword
        // HACK: use do-while to allow break statements
        else if( function.isMethod() ) do
        {
            // if the last designator is implicitly called from 'this'
            if( functionDesign instanceof Designator_Ident )
            {
                isVirtualCall = true;
                break;
            }
            // if the last designator is a method call (could be called from 'this' or 'super')
            else if( functionDesign instanceof Designator_Member )
            {
                // if the method call is through the 'super' keyword then it is non-virtual
                Designator prev = ( ( Designator_Member )functionDesign ).getDesignator();
                if( prev instanceof Designator_Super )
                {
                    break;
                }
                
                // the method exists in the virtual table and is invoked virtually
                isVirtualCall = true;
            }
            // if the last designator is not a function
            else
            {
                report_fatal( curr, "Function designator type not yet supported" );
            }
        } while( false );
    
        
        // if the function should be called virtually
        if( isVirtualCall )
        {
            // push the virtual table pointer to the expression stack (#0 field in the class)
            _codeGen().i_getfield( 0 );
            // call the virtual method
            _codeGen().i_invokevirtual( function._name() );
        }
        // if the function should be called non-virtually
        else
        {
            // should currently never happen, but just in case
            if( !function.hasValidAddress() )
            {
                report_fatal( curr, "Illegal function call -- starting address must be positive and non-null" );
            }

            // remove the random constant (quasi class instance pointer) from the expression stack
            // NOTE: this works for the constructor, as it still leaves the 'this' formal parameter in the zeroth place
            _codeGen().i_epop();
            // call the function/static method starting at the given address
            // NOTE: this works for super() and super.foo(), as their addresses are set inside the superclass, which will have been visited before the subclass
            int pointA = _codeGen().i_call( CodeGen.NO_ADDRESS );
            _codeGen().fixJumpOffset( pointA, function._address() );
        }

        // if the return value should not be saved and the function/method returns something, remove the result from the expression stack
        if( !saveReturnValueIfItExists && !function._type().isVoidType() ) _codeGen().i_epop();
    }
    // Factor ::= (Factor_Literal    ) Literal;
    @Override
    public void visit( Factor_Literal curr )
    {
        // load the literal's value onto the expression stack
        _codeGen().loadConst( curr.symbol._value() );
    }
    // Factor ::= (Factor_NewVar     ) NEW_K Type;
    @Override
    public void visit( Factor_NewVar curr )
    {
        // get the current symbol's type
        SymbolType symbolType = curr.symbol._type();

        // allocate space on the heap for the class/record instance and add the starting address to the expression stack
        _codeGen().i_new( symbolType._fieldCount()*4/*B*/ );
        
        // if the type contains a virtual table pointer (class only)
        if( symbolType.isClass() )
        {
            // initialize the virtual table pointer, but leave the class instance's address on the expression stack
            _codeGen().i_dup();
            _codeGen().loadConst( curr.symbol._address() );
            _codeGen().i_putfield( 0 );

            // if the class contains a non-placeholder constructor, call it
            Symbol constructor = symbolType._members().findSymbol( "@Constructor" );
            if( constructor.isNoSym() || constructor.isPlaceholder() ) return;

            // copy the class instance's address on the expression stack
            // +   set the implicit 'this' as the zeroth constructor argument
            _codeGen().i_dup();
            // call the constructor starting at the given address
            int pointA = _codeGen().i_call( CodeGen.NO_ADDRESS );
            _codeGen().fixJumpOffset( pointA, constructor._address() );
        }
    }
    // Factor ::= (Factor_NewArray   ) NEW_K Type lbracket Expr rbracket;
    @Override
    public void visit( Factor_NewArray curr )
    {
        // allocate space on the heap for the array and add the array starting address to the expression stack
        _codeGen().i_newarray( curr.symbol._type().isChar() );
    }
    // Factor ::= (Factor_Expr       ) lparen Expr rparen;

    ////// ident.ident[ expr ]( expr, expr, expr )
    // MethodCall ::= (MethodCall_Plain) Designator;
    @Override
    public void visit( MethodCall_Plain curr )
    {
        // if the current symbol is a method
        if( curr.symbol.isMethod() )
        {
            // duplicate the method address on the expression stack (the first address will be used to get the virtual table pointer)
            _codeGen().i_dup();
        }
        // if the current symbol is a static method or function
        else if( curr.symbol.isStaticMethod() || curr.symbol.isFunction() )
        {
            // load a random constant on the expression stack (quasi class instance pointer)
            // so that both the method's and the function's activation parameters are handled in the same way
            _codeGen().loadConst( 0 );
        }
    }

    ////// ident
    ////// this.ident
    ////// super()
    ////// null
    ////// ident.ident
    ////// ident[ expr ]
    ////// ident.ident.ident[ expr ].ident
    ////// ident.ident.ident[ expr ].ident[ expr ]
    // Designator ::= (Designator_Ident  ) ident:Name;
    @Override
    public void visit( Designator_Ident curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_This   ) THIS_K;
    @Override
    public void visit( Designator_This curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_Super  ) SUPER_K;
    @Override
    public void visit( Designator_Super curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_Null   ) NULL_K;
    @Override
    public void visit( Designator_Null curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_Member ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Member curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_ArrElem) Designator lbracket Expr rbracket;
    @Override
    public void visit( Designator_ArrElem curr )
    {
        visit_Designator( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Designator( Designator curr )
    {
        // check if the designator is inside a class method (in the class's scope)
        boolean isInClassScope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ClassDeclType )
        ) != null;

        // if the current designator is a class member, but does not start with 'this' (this.field)
        // NOTE: this won't be done for 'this' and 'super', since they aren't Designator_Ident
        if( curr instanceof Designator_Ident && isInClassScope && curr.symbol.isClassMember() )
        {
            // load the instance pointer on the expression stack
            _codeGen().loadSymbolValue( _symbolTable().findSymbol( "this" ) );
        }

        // if the designator is not the last one in the sequence
        boolean hasNext = curr.getParent() instanceof Designator;
        if( hasNext )
        {
            // load its value on the expression stack
            _codeGen().loadSymbolValue( curr.symbol );
        }
        
        // if the designator is a call to the supertype's constructor ('super()')
        if( curr instanceof Designator_Super && !hasNext )
        {
            // load the instance pointer on the expression stack
            // NOTE: this loads the zeroth constructor's formal parameter, which wouln't be done otherwise
            _codeGen().loadSymbolValue( _symbolTable().findSymbol( "this" ) );
        }
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

    ////// &&  |  ||
    // Aorop ::= (Aorop_And) and:Aorop;
    // Aorop ::= (Aorop_Or ) or :Aorop;

    ////// ==  |  !=  |  >  |  >=  |  <  |  <=
    // Relop ::= (Relop_Eq) eq:Relop;
    // Relop ::= (Relop_Ne) ne:Relop;
    // Relop ::= (Relop_Gt) gt:Relop;
    // Relop ::= (Relop_Ge) ge:Relop;
    // Relop ::= (Relop_Lt) lt:Relop;
    // Relop ::= (Relop_Le) le:Relop;

    ////// +  |  -
    // Addop ::= (Addop_Plus ) plus :Addop;
    // Addop ::= (Addop_Minus) minus:Addop;

    ////// *  |  /  |  %
    // Mulop ::= (Mulop_Mul ) mul :Mulop;
    // Mulop ::= (Mulop_Div ) div :Mulop;
    // Mulop ::= (Mulop_Perc) perc:Mulop;

}
