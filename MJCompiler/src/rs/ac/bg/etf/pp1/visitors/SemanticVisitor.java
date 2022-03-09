package rs.ac.bg.etf.pp1.visitors;

import java.util.List;

import rs.ac.bg.etf.pp1.CodeGen;
import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.Symbol;
import rs.ac.bg.etf.pp1.SymbolMap;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.props.*;
import rs.ac.bg.etf.pp1.SymbolTable;
import rs.ac.bg.etf.pp1.SymbolType;
import rs.ac.bg.etf.pp1.TokenCode;


public class SemanticVisitor extends VisitorAdaptor
{
    private final Compiler.State state;
    private final Context context;
    
    private static class Context
    {
        public final StackProp<SyntaxNode> syntaxNodeStack = new StackProp<>();
        // FIX: this should also be on the syntax node stack (allows for nested classes)
        public final BoolProp isInForwardDeclMode = new BoolProp();
    }
    private SymbolTable _symbolTable() { return state._symbolTable(); }
    private CodeGen _codeGen() { return state._codeGen(); }

    public SemanticVisitor( Compiler.State state )
    {
        this.state = state;
        this.context = new Context();
    }

    // underscore all error tokens
    private void report_error( SyntaxNode node, String message )
    {
        state._errors().add( CompilerError.SEMANTIC_ERROR, message, node, true, false );
    }
    // underscore all error tokens and throw an exception
    private void report_fatal( SyntaxNode node, String message )
    {
        state._errors().add( CompilerError.SEMANTIC_ERROR, message, node, true, true );
    }

    
    


    // ________________________________________________________________________________________________
    // forward class declaration visitor

    private class ForwardClassVisitor extends VisitorAdaptor
    {
        ////// void foo() { }
        ////// void foo() { statement statement }
        ////// void foo() vardl vardl { }
        ////// void foo() vardl vardl { statement statement }
        //////      foo() vardl vardl { statement statement }   -- constructor
        ////// void foo( int a, char c, Node Array[] ) { }
        ////// void foo( int a, char c, Node Array[] ) { statement statement }
        ////// void foo( int a, char c, Node Array[] ) vardl vardl { }
        ////// void foo( int a, char c, Node Array[] ) vardl vardl { statement statement }
        // MethodDecl ::= (MethodDecl_Plain) MethodDeclType lparen FormPars rparen MethodDeclBody VarDeclList MethodDeclCode lbrace StatementList rbrace;
        @Override
        public void visit( MethodDecl_Plain curr )
        {
            visit_MethodDecl( curr );
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

        ////// action symbol for opening a new scope
        // MethodDeclBody ::= (MethodDeclBody_Plain) ;
        @Override
        public void visit( MethodDeclBody_Plain curr )
        {
            visit_MethodDeclBody( curr );
        }
        
        ////// action symbol for the beginning of the method code
        // MethodDeclCode ::= (MethodDeclCode_Plain) ;
        
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
        @Override
        public void visit( FormParam_Plain curr )
        {
            visit_FormParam( curr );
        }
        // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};
        @Override
        public void visit( FormParam_Err curr )
        {
            visit_FormParam( curr );
        }

        ////// int
        ////// Node
        // FormParamType ::= (FormParamType_Plain) Type;
        @Override
        public void visit( FormParamType_Plain curr )
        {
            visit_FormParamType( curr );
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
            visit_VarDecl( curr );
        }

        ////// int
        ////// static A
        // VarDeclType ::= (VarDeclType_Plain )          Type;
        @Override
        public void visit( VarDeclType_Plain curr )
        {
            visit_VarDeclType( curr, curr.getType(), false );
        }
        // VarDeclType ::= (VarDeclType_Static) STATIC_K Type;
        @Override
        public void visit( VarDeclType_Static curr )
        {
            visit_VarDeclType( curr, curr.getType(), true );
        }
        // VarDeclType ::= (VarDeclType_Err   ) error {: parser.report_error( "Bad class declaration", null ); :};
        @Override
        public void visit( VarDeclType_Err curr )
        {
            visit_VarDeclType( curr, null, false );
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
            visit_VarIdent( curr, curr.getVarName(), false );
        }
        // VarIdent ::= (VarIdent_Array) ident:VarName lbracket rbracket;
        @Override
        public void visit( VarIdent_Array curr )
        {
            visit_VarIdent( curr, curr.getVarName(), true );
        }
        // VarIdent ::= (VarIdent_Err  ) error {: parser.report_error( "Bad variable declaration", null ); :};



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
            visit_Type( curr, curr.getTypeName() );
        }

        ////// int | bool | char | ident
        // Type ::= (Type_Ident) ident:Type;
        @Override
        public void visit( Type_Ident curr )
        {
            visit_Type( curr, curr.getTypeName() );
        }
    }





    // ________________________________________________________________________________________________
    // forward label visitor

    private class ForwardLabelVisitor extends VisitorAdaptor
    {
        ////// action symbol for defining a label
        // StmtLabel ::= (StmtLabel_Plain) ident:Label colon;
        @Override
        public void visit( StmtLabel_Plain curr )
        {
            // create a symbol for the label
            String labelName = String.format( "@Label_%s", curr.getLabel() );
            Symbol labelSymbol = Symbol.newConst( labelName, SymbolTable.intType, Symbol.NO_VALUE );
            // if the label's symbol could not be added to the symbol table
            if( !_symbolTable().addSymbol( labelSymbol ) )
            {
                report_error( curr, "Label already exists in the function scope" );
                return;
            }
        }
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
        // remove the program node from the syntax node stack
        context.syntaxNodeStack.remove();

        Symbol left = curr.getProgramType().symbol;
        if( left.isNoSym() ) return;

        // update the program's local symbols
        left._locals( _symbolTable()._locals() );

        // if the <void main()> function doesn't exist in the program
        Symbol main = left._locals().findSymbol( "main" );
        if( main.isNoSym() )
        {
            report_error( curr.getProgramType(), "Main function missing from program" );
        }
        // if the main function's return type is not void
        else if( !main._type().isVoidType() )
        {
            report_error( curr.getProgramType(), "Main function's return type is not void" );
        }
        // if the main function has parameters
        else if( main._paramCount() != 0 )
        {
            report_error( curr.getProgramType(), "Main function can't have parameters" );
        }

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
        // if the semantic visitor is in forward declaration mode (impossible here)
        if( context.isInForwardDeclMode.true_() )
        {
            report_error( curr.getProgramType(), "Semantic visitor somehow still in forward declaration mode" );
        }

        // close the program's scope
        _symbolTable().closeScope();
    }

    ////// program my_program
    // ProgramType ::= (ProgramType_Plain) PROGRAM_K ident:ProgramName;
    @Override
    public void visit( ProgramType_Plain curr )
    {
        // add the program node to the syntax node stack
        context.syntaxNodeStack.add( curr );
        SymbolType programType = SymbolType.newProgram( "@Program", null );
        curr.symbol = Symbol.newProgram( curr.getProgramName(), programType, null );

        // if the program cannot be added to the symbol table
        if( !_symbolTable().addSymbol( curr.symbol ) )
        {
            // NOTE: currently this should never happen since there is only one program
            report_error( curr, "Program cannot be added since a symbol with the same name already exists" );
            
            // just a precaution
            curr.symbol = curr.symbol.clone( String.format( "@Program_%d", _symbolTable()._globalsProgramCount() ) );
            _symbolTable().addSymbol( curr.symbol );
        }

        // open the program's scope
        _symbolTable().openScope();
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
        visit_ClassDeclType( curr, curr.getClassName(), null );
    }
    // ClassDeclType ::= (ClassDeclType_Extends) CLASS_K ident:ClassName EXTENDS_K Type;
    @Override
    public void visit( ClassDeclType_Extends curr )
    {
        visit_ClassDeclType( curr, curr.getClassName(), curr.getType().symbol );
    }
    // ClassDeclType ::= (ClassDeclType_Err    ) CLASS_K error {: parser.report_error( "Bad class declaration", null ); :};
    @Override
    public void visit( ClassDeclType_Err curr )
    {
        visit_ClassDeclType( curr, null, null );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_ClassDeclType( ClassDeclType curr, String className, Symbol base )
    {
        // add the class declaration type node to the syntax node stack
        context.syntaxNodeStack.add( curr );
        String dummyName = String.format( "@Class_%d", _symbolTable()._localsClassCount() );

        // if the class name or the extends type is missing, use default values
        if( className == null ) className = dummyName;
        if( base == null ) base = SymbolTable.noSym;
        
        // if the type that is extended is not a class
        SymbolType baseType = base._type();
        if( !baseType.isAnyType() && !baseType.isClass() )
        {
            report_error( curr, "Extended type must be a class" );
            
            // set the extended type to be the <any type>
            base = SymbolTable.anySym;
            baseType = base._type();
        }

        // create the <class type> and <class type symbol>
        SymbolType classType = SymbolType.newClass( className, baseType, null );
        curr.symbol = Symbol.newType( className, classType, Symbol.NO_VALUE );

        // if the class cannot be added to the symbol table
        if( !_symbolTable().addSymbol( curr.symbol ) )
        {
            report_error( curr, "Class cannot be added since a symbol with the same name already exists" );
            
            // add an artificial class used for type checking later on
            classType = SymbolType.newClass( dummyName, baseType, null );
            curr.symbol = Symbol.newType( dummyName, classType, Symbol.NO_VALUE );
            
            _symbolTable().addSymbol( curr.symbol );
        }

        // open the class's scope
        _symbolTable().openScope();
        // get the class's inherited (non-static) members, and remove the virtual table pointer (so that it isn't shared between the base class and the subclass)
        // +    save the inherited members in a new symbol map, because we don't want to modify the members list in the base class
        SymbolMap baseMembers = new SymbolMap( classType._base()._nonStaticMembers() );
        baseMembers.removeSymbol( "@pVirtualTable" );
        // add a 'virtual table pointer' field to the inherited members
        // +   set its index to be 0, so that it is the first class field
        Symbol pVirtualTable = Symbol.newField( "@pVirtualTable", SymbolTable.intType, Symbol.NO_VALUE, 0 );
        baseMembers.addSymbol( pVirtualTable );
        // add the class's inherited members (this includes the constructor)
        _symbolTable().addSymbols( baseMembers );



        // forward visit only the class members' declarations
        ForwardClassVisitor visitor = new ForwardClassVisitor();
        ClassDeclBody classDeclBody = ( ( ClassDecl_Plain )curr.getParent() ).getClassDeclBody();
        context.isInForwardDeclMode.set();
        classDeclBody.traverseBottomUp( visitor );
        context.isInForwardDeclMode.reset();

        // get the class members from the symbol table
        SymbolMap classMembers = _symbolTable()._locals();

        // update the class members' indexes by category
        SymbolMap methods = new SymbolMap();
        SymbolMap fields = new SymbolMap();
        SymbolMap staticMethods = new SymbolMap();
        SymbolMap staticFields = new SymbolMap();
        for( Symbol member : classMembers )
        {
            if     ( member.isMethod()       ) methods      .addSymbol( member );
            else if( member.isField()        ) fields       .addSymbol( member );
            else if( member.isStaticMethod() ) staticMethods.addSymbol( member );
            else if( member.isStaticField()  ) staticFields .addSymbol( member );
            else                               report_fatal( curr, String.format( "Class member not yet supported: %s", member._name() ) );
        }

        // sort the methods and fields based on their member indexes
        methods._symbols( methods._sorted() );
        fields._symbols( fields._sorted() );
        staticMethods._symbols( staticMethods._sorted() );
        staticFields._symbols( staticFields._sorted() );
        
        int idx = 0;
        // compact the <methods>', <fields>' and <static fields>' indexes
        // HACK: the constructor's index is -1, as it isn't present in the virtual table
        idx = -1;  for( Symbol member : methods       ) { member._memberIdx( idx++ ); }
        idx = 0;   for( Symbol member : fields        ) { member._memberIdx( idx++ ); }
        idx = 0;   for( Symbol member : staticMethods ) { member._memberIdx( idx++ ); }
        idx = 0;   for( Symbol member : staticFields  ) { member._memberIdx( idx++ ); }
        idx = 0;

        // join all the now sorted members by category
        SymbolMap members = new SymbolMap();
        members.addSymbols( staticFields  );
        members.addSymbols( staticMethods );
        members.addSymbols( fields );
        members.addSymbols( methods );

        // set the class's members in the correct order
        classType._members( members );

        // if the class has more fields than the microjava virtual machine supports
        if( fields.size() > state._maxFieldsInClass() )
        {
            report_error( curr, String.format( "Class has more fields than the virtual machine supports: %d (max %d)", fields.size(), state._maxFieldsInClass() ) );
        }

        // save the starting address for the class's virtual table and reserve the space needed for the symbol table in the static segment
        // +    also update the class's virtual table pointer to point to the new location
        curr.symbol._address( _codeGen()._staticSize32() );
        pVirtualTable._address( curr.symbol._address() );
        _codeGen()._staticSize32PostInc( curr.symbol._virtualTableSize() );

        // add a dummy 'this' constant to the symbol table scope
        // +   set its value to be zero -- very important, since it will be the 0th method call's formal parameter
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
    @Override
    public void visit( RecordDecl_Plain curr )
    {
        // remove the record declaration type node from the syntax node stack
        context.syntaxNodeStack.remove();
        // close the record's scope
        _symbolTable().closeScope();
    }

    ////// record A
    // RecordDeclType ::= (RecordDeclType_Plain) RECORD_K ident:RecordName;
    @Override
    public void visit( RecordDeclType_Plain curr )
    {
        visit_RecordDeclType( curr, curr.getRecordName() );
    }
    // RecordDeclType ::= (RecordDeclType_Err  ) RECORD_K error {: parser.report_error( "Bad record declaration", null ); :};
    @Override
    public void visit( RecordDeclType_Err curr )
    {
        visit_RecordDeclType( curr, null );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_RecordDeclType( RecordDeclType curr, String recordName )
    {
        // add the record declaration type node to the syntax node stack
        context.syntaxNodeStack.add( curr );
        String dummyName = String.format( "@Record_%d", _symbolTable()._localsRecordCount() );

        // if the record name is missing, use default values
        if( recordName == null ) recordName = dummyName;
        
        // create the <record type> and <record type symbol>
        SymbolType recordType = SymbolType.newRecord( recordName, null );
        curr.symbol = Symbol.newType( recordName, recordType, Symbol.NO_VALUE );

        // if the record cannot be added to the symbol table
        if( !_symbolTable().addSymbol( curr.symbol ) )
        {
            report_error( curr, "Record cannot be added since a symbol with the same name already exists" );
            
            // add an artificial class used for type checking later on
            recordType = SymbolType.newRecord( dummyName, null );
            curr.symbol = Symbol.newType( dummyName, recordType, Symbol.NO_VALUE );
            
            _symbolTable().addSymbol( curr.symbol );
        }

        // open the record's scope
        _symbolTable().openScope();
    }

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
    // MethodDecl ::= (MethodDecl_Plain) MethodDeclType FormParsScope FormPars rparen MethodDeclBody VarDeclList MethodDeclCode lbrace StatementList rbrace;
    @Override
    public void visit( MethodDecl_Plain curr )
    {
        visit_MethodDecl( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_MethodDecl( MethodDecl_Plain curr )
    {
        // close the function's scope
        context.syntaxNodeStack.remove();
        _symbolTable().closeScope();
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
        context.syntaxNodeStack.add( curr );

        // if the method's symbol has already been initialized (in the forward declaration pass)
        if( curr.symbol != null )
        {
            // get the formal parameters
            SymbolMap formalParams = curr.symbol._params();
            
            // add the formal parameters to the method scope
            _symbolTable().openScope();
            _symbolTable().addSymbols( formalParams );

            return;
        }

        // get the method index in its scope
        int methodIdx = _symbolTable()._localsMethodCount();
        // open a scope for the function's formal parameters
        _symbolTable().openScope();
        
        // get the function/method name and its return type
        // +    update the locals later on
        String functionName = null;
        SymbolType returnType = null;

        if( curr instanceof MethodDeclType_Plain )
        {
            functionName = ( ( MethodDeclType_Plain )curr ).getMethodName();
            returnType   = ( ( MethodDeclType_Plain )curr ).getReturnType().symbol._type();
        }
        else if( curr instanceof MethodDeclType_Empty )
        {
            functionName = ( ( MethodDeclType_Empty )curr ).getMethodName();
            returnType   = null;
        }
        else
        {
            report_fatal( curr, "Function/method declaration not yet supported" );
        }

        // get the scope in which the function's been declared
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ProgramType   )
                 || ( elem instanceof ClassDeclType )
        );
        if( scope == null )
        {
            report_fatal( curr, "Function/method declaration not yet supported" );
        }

        // get the scope's symbol
        SymbolType scopeType = null;
        if     ( scope instanceof ProgramType   ) scopeType = ( ( ProgramType   ) scope ).symbol._type();
        else if( scope instanceof ClassDeclType ) scopeType = ( ( ClassDeclType ) scope ).symbol._type();

        // if the function/method doesn't have a return type but isn't a constructor (isn't in class scope or doesn't have the same name as the class)
        if( returnType == null   &&   !( scopeType.isClass() && functionName.equals( scopeType._name() ) )
        )
        {
            report_error( curr, "Function/method must have a return type" );
            // set its return type to be void
            returnType = SymbolTable.voidType;
        }

        
        // if the function is in the program scope
        if( scope instanceof ProgramType )
        {
            // create the function's symbol
            curr.symbol = Symbol.newFunction( functionName, returnType, Symbol.NO_VALUE, null );
        }
        // if the function is in the class scope (a method)
        else if( scope instanceof ClassDeclType )
        {
            // if the method is a constructor
            if( returnType == null )
            {
                // set the constructor's return type to be void
                returnType = SymbolTable.voidType;
                // set the constructor's name to be a reserved keyword
                // +   that way the constructor can't be explicitly called (like in C++)
                functionName = "@Constructor";
            }

            // create the method's symbol
            curr.symbol = Symbol.newMethod( functionName, returnType, Symbol.NO_VALUE, methodIdx, null );
            // add a dummy 'this' formal parameter that will be removed later (just to reserve the 0th formal parameter)
            _symbolTable().addSymbol( Symbol.newFormalParam( "this", ( ( ClassDeclType )scope ).symbol._type(), 0 ) );
        }
    }

    ////// action symbol for opening a new scope
    // MethodDeclBody ::= (MethodDeclBody_Plain) ;
    @Override
    public void visit( MethodDeclBody_Plain curr )
    {
        visit_MethodDeclBody( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_MethodDeclBody( MethodDeclBody_Plain curr )
    {
        // get the function declaration type
        MethodDeclType methodDeclType = ( MethodDeclType )context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodDeclType )
        );
        // if the function's type declaration is missing from the syntax node stack
        if( methodDeclType == null )
        {
            report_fatal( curr, "Function/method type declaration not yet supported" );
        }

        // get the function symbol from the function/method type declaration
        Symbol function = methodDeclType.symbol;
        // used if the function is a method (inside a class)
        SymbolType classType = null;

        // if the method's formal parameters have already been set by the forward declaration pass
        if( function.isMethod() && context.isInForwardDeclMode.false_() )
        {
            // do nothing, as the method scope was already set up by <MethodDeclType>
            // +   the scope currently contains the method's formal parameters, but it will contain its locals in the future
            return;
        }

        // get the function's formal parameters from the symbol table and also close the formal parameter scope
        SymbolMap formalParams = _symbolTable()._locals();
        _symbolTable().closeScope();
        // remove the dummy 'this' symbol from the method's formal parameters
        formalParams.removeSymbol( "this" );
        
        // update the function's formal parameters
        function._params( formalParams );
        // prepare the function's dummy name
        // +   IMPORTANT: get the function's index here, in its containing scope, not after the function's scope is reopened
        String dummyName = null;
        if     ( function.isMethod()       ) { dummyName = String.format( "@Method_%d",       _symbolTable()._localsMethodCount()       ); }
        else if( function.isStaticMethod() ) { dummyName = String.format( "@StaticMethod_%d", _symbolTable()._localsStaticMethodCount() ); }
        else if( function.isFunction()     ) { dummyName = String.format( "@Function_%d",     _symbolTable()._localsFunctionCount()     ); }
        else                                 { report_fatal( curr, "Function/method type declaration not yet supported" ); }



        // if the function is a method
        if( function.isMethod() )
        {
            // get the function symbol from the method type declaration
            Symbol method = function;

            // get the method's surrounding class
            ClassDeclType classDeclType = ( ClassDeclType )context.syntaxNodeStack.find(
                elem -> ( elem instanceof ClassDeclType )
            );
            // if the method is not declared in a class scope
            if( classDeclType == null ) report_fatal( curr, "Method declaration not yet supported" );

            // get the class type from the class declaration
            classType = classDeclType.symbol._type();

            
            // find the inherited method from the class's superclass, if such a method exists
            Symbol baseMethod = classType._base()._members().findSymbol( method._name() );

            // if the inherited method exists
            if( !baseMethod.isNoSym() )
            {
                // if the current method can override the class's inherited method (because their declarations are 'compatible')
                if( Symbol.canOverride( method, baseMethod ) )
                {
                    // get the class's members (the current symbol table scope), and close the symbol table scope
                    SymbolMap classMembers = _symbolTable()._locals();
                    _symbolTable().closeScope();

                    // save the inherited method's index in the class declaration (useful for keeping the method's index in the virtual table)
                    method._memberIdx( baseMethod._memberIdx() );
                    // remove the original method from the class's locals
                    classMembers.removeSymbol( baseMethod._name() );

                    // HACK: restore the class's members (in the symbol table scope)
                    _symbolTable().openScope();
                    _symbolTable().addSymbols( classMembers );
                }
                else
                {
                    report_error( methodDeclType, "Cannot override, method's signature different from inherited method's signature" );
                    // give the method a new dummy name, guaranteed not to cause collisions in the symbol table
                    function = method = method.clone( dummyName );
                    // update the <method declaration>'s symbol with the dummy function
                    methodDeclType.symbol = method;
                }
            }
        }

        // try to add the function/method to the symbol table
        // +   if the function/method cannot be added to the symbol table
        if( !_symbolTable().addSymbol( function ) )
        {
            report_error( methodDeclType, "Function/method cannot be added since a symbol with the same name already exists" );
            // give the function a new dummy name, guaranteed not to cause collisions in the symbol table
            function = function.clone( dummyName );
            // update the <method declaration>'s symbol with the dummy function
            methodDeclType.symbol = function;
            // add an artificial function used for type checking later on
            _symbolTable().addSymbol( function );
        }


        
        // reopen the method's scope
        // +   the function's locals will be declared in it in the future
        _symbolTable().openScope();

        // if the function is a method
        if( function.isMethod() )
        {
            // add a dummy 'this' constant to the symbol table scope
            // +   set its value to be zero -- very important, since it will be the 0th method call's formal parameter
            Symbol thisSymbol = Symbol.newConst( "this", classType, 0 );
            _symbolTable().addSymbol( thisSymbol );
        }
        // restore the formal parameters
        _symbolTable().addSymbols( formalParams );
    }

    ////// action symbol for the beginning of the method code
    // MethodDeclCode ::= (MethodDeclCode_Plain) ;
    @Override
    public void visit( MethodDeclCode_Plain curr )
    {
        // initialize the jump map
        curr.jumpprop = new JumpProp( state._codeGen() );

        // get the function/method's type
        MethodDeclType methodDeclType = ( MethodDeclType )context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodDeclType )
        );

        // if the function/method has more local variables (including parameters) in the stack frame than the microjava virtual machine supports
        int stackFrameSize = _symbolTable()._localsStackFrameSize();
        if( stackFrameSize > state._maxStackFrameSize() )
        {
            report_error( methodDeclType, String.format( "Function/method has more parameters + local variables than the virtual machine supports: %d (max %d)", stackFrameSize, state._maxStackFrameSize() ) );
        }

        // if the constructor has formal parameters
        Symbol method = methodDeclType.symbol;
        if( method.isConstructor() && _symbolTable()._localsFormalParamCount() > 0 )
        {
            report_error( methodDeclType, String.format( "The constructor doesn't currently support having parameters" ) );
        }

        // forward visit only the function/method's labels' declarations
        // +   that way, the label's symbols will be added to the symbol table before their first use
        MethodDecl methodDecl = ( MethodDecl )curr.getParent();
        ForwardLabelVisitor visitor = new ForwardLabelVisitor();
        context.isInForwardDeclMode.set();
        methodDecl.traverseBottomUp( visitor );
        context.isInForwardDeclMode.reset();

        // add the labels to the jump map
        for( Symbol labelSymbol : _symbolTable()._locals() )
        {
            // add the label as a jump point to the jump map
            curr.jumpprop.add( labelSymbol._name() );
        }
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
    @Override
    public void visit( FormParam_Plain curr )
    {
        visit_FormParam( curr );
    }
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};
    @Override
    public void visit( FormParam_Err curr )
    {
        visit_FormParam( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_FormParam( FormParam curr )
    {
        // find the (possible) class declaration surrounding the symbol
        boolean isInClassScope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ClassDeclType )
        ) != null;
        // if the class method's formal parameters have already been declared by the forward declaration visitor, return
        // +   if we are in a class and the forward declaration mode is finished
        if( isInClassScope && context.isInForwardDeclMode.false_() ) return;



        // if the formal parameter is plain
        if( curr instanceof FormParam_Plain )
        {
            // remove the FormParamType from the syntax node stack
            context.syntaxNodeStack.remove();
        }
        // if the formal parameter is an error
        else if( curr instanceof FormParam_Err )
        {
            // don't do this next line since the FormParam_Err syntax node wasn't added to the syntax node stack
         // context.syntaxNodeStack.remove();

            // get the formal parameter index in the formal parameter list
            int paramIdx = _symbolTable()._localsFormalParamCount();

            // add a dummy symbol as the formal parameter
            Symbol paramSymbol = Symbol.newFormalParam( String.format( "@Param_%d", paramIdx ), SymbolTable.anyType, paramIdx );
            _symbolTable().addSymbol( paramSymbol );
        }
        // ...
        else
        {
            // if the formal parameter is of unknown kind
            report_fatal( curr, "Formal parameter kind not supported yet" );
        }
    }

    ////// int
    ////// Node
    // FormParamType ::= (FormParamType_Plain) Type;
    @Override
    public void visit( FormParamType_Plain curr )
    {
        visit_FormParamType( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_FormParamType( FormParamType_Plain curr )
    {
        // if the symbol has already been initialized by the forward class declaration visitor, return
        if( curr.symbol != null ) return;
        
        context.syntaxNodeStack.add( curr );
        SymbolType paramType = curr.getType().symbol._type();
        curr.symbol = Symbol.newConst( "@FormParamType_Plain", paramType, Symbol.FORMAL_PARAM );
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
        visit_VarDecl( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_VarDecl( VarDecl_Plain curr )
    {
        // find the (possible) class declaration surrounding the symbol
        boolean isInClassScope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ClassDeclType )
        ) != null;
        // if the class's fields/<static fields> have already been declared by the forward declaration visitor, return
        // +   if we are in a class and the forward declaration mode is finished
        if( isInClassScope && context.isInForwardDeclMode.false_() ) return;

        // remove the VarDeclType from the syntax node stack
        // +   works for classes and records
        context.syntaxNodeStack.remove();
    }

    ////// int
    ////// static A
    // VarDeclType ::= (VarDeclType_Plain )          Type;
    @Override
    public void visit( VarDeclType_Plain curr )
    {
        visit_VarDeclType( curr, curr.getType(), false );
    }
    // VarDeclType ::= (VarDeclType_Static) STATIC_K Type;
    @Override
    public void visit( VarDeclType_Static curr )
    {
        visit_VarDeclType( curr, curr.getType(), true );
    }
    // VarDeclType ::= (VarDeclType_Err   ) error {: parser.report_error( "Bad class declaration", null ); :};
    @Override
    public void visit( VarDeclType_Err curr )
    {
        visit_VarDeclType( curr, null, false );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_VarDeclType( VarDeclType curr, Type varDeclType, boolean isStatic )
    {
        // if the symbol has already been initialized by the forward class declaration visitor, return
        if( curr.symbol != null ) return;

        context.syntaxNodeStack.add( curr );
        Symbol right = ( varDeclType != null ) ? varDeclType.symbol : SymbolTable.noSym;
        
        // get the variable declaration's scope
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ProgramType )
                 || ( elem instanceof ClassDeclType )
                 || ( elem instanceof RecordDeclType )
                 || ( elem instanceof MethodDeclType )
        );
        // if the variable/field declaration is located outside a program/class/function/method
        if( scope == null )
        {
            report_fatal( curr, "Variable/field/<static field> declaration not yet supported" );
        }
        
        int varKind = Symbol.VAR;
        if     ( scope instanceof ProgramType    ) { varKind = Symbol.VAR;   }
        else if( scope instanceof ClassDeclType  ) { varKind = Symbol.FIELD; }
        else if( scope instanceof RecordDeclType ) { varKind = Symbol.FIELD; }
        else if( scope instanceof MethodDeclType ) { varKind = Symbol.VAR;   }

        // if the static modifier is used
        if( isStatic == true )
        {
            // on a class field, that's supported
            if( scope instanceof ClassDeclType && varKind == Symbol.FIELD )
            {
                varKind = Symbol.STATIC_FIELD;
            }
            // otherwise, the modifier cannot be applied
            else
            {
                report_error( curr, "Illegal 'static' modifier use -- only class fields can be static" );
            }
        }

        curr.symbol = Symbol.newConst( "@VarDeclType", right._type(), varKind );
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
        // if the symbol has already been initialized by the forward class declaration visitor
        if( curr.symbol != null )
        {
            // if the symbol is in a class/method declaration, add the symbol to the current symbol table scope (method scope)
            // +   the forward declaration visitor initialized this symbol, but now in the second pass it is missing from the symbol table, so re-add it
            _symbolTable().addSymbol( curr.symbol );
            return;
        }
        // initialize the current symbol
        curr.symbol = SymbolTable.noSym;

        // if the variable/field is declared outside a variable declaration or a formal parameter declaration
        // +   if the current declaration type is not yet supported
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof VarDeclType_Plain   )
                 || ( elem instanceof VarDeclType_Static  )
                 || ( elem instanceof VarDeclType_Err     )
                 || ( elem instanceof FormParamType_Plain )
        );
        if( scope == null )
        {
            report_fatal( curr, "Variable/field/<static field>/<formal parameter> declaration not yet supported" );
        }

        Symbol left = SymbolTable.noSym;
        if     ( scope instanceof VarDeclType_Plain   ) { left = ( ( VarDeclType_Plain   )scope ).symbol; }
        else if( scope instanceof VarDeclType_Static  ) { left = ( ( VarDeclType_Static  )scope ).symbol; }
        else if( scope instanceof VarDeclType_Err     ) { /* if error do nothing */;                      }
        else if( scope instanceof FormParamType_Plain ) { left = ( ( FormParamType_Plain )scope ).symbol; }

        SymbolType leftType = left._type();
        SymbolType varType = ( !isArray ) ? leftType : SymbolType.newArray( String.format( "@Array<%s>", leftType._name() ), leftType );

        // update the current symbol
        // HACK: don't worry about the class member's local index here, it will be fixed in <ClassDeclType ::= ...>
        int symLevel = _symbolTable()._localsLevel();
        int localIdx = _symbolTable()._localsSize();
        int varIdx   = _symbolTable()._localsStackFrameSize();
        
        switch( left._value() )
        {
            case Symbol.VAR:          { curr.symbol = Symbol.newVar        ( varName, varType, Symbol.NO_VALUE, symLevel, varIdx );   if( curr.symbol.isGlobal() ) { curr.symbol._address( _codeGen()._staticSize32PostInc( 1 ) ); } break; }
            case Symbol.FIELD:        { curr.symbol = Symbol.newField      ( varName, varType, Symbol.NO_VALUE, localIdx         );   break; }
            case Symbol.STATIC_FIELD: { curr.symbol = Symbol.newStaticField( varName, varType, Symbol.NO_VALUE, localIdx         );   { curr.symbol._address( _codeGen()._staticSize32PostInc( 1 ) ); } break; }
            case Symbol.FORMAL_PARAM: { curr.symbol = Symbol.newFormalParam( varName, varType, localIdx                          );   break; }
            
            default: report_fatal( curr, "Variable/field/<static field>/<formal parameter> declaration not yet supported" );
        }
        
        // if the variable/field cannot be added to the symbol table
        if( !_symbolTable().addSymbol( curr.symbol ) )
        {
            report_error( curr, "Variable/field cannot be added since a symbol with the same name already exists" );
            
            // add a dummy symbol to the symbol table, used for semantic checking later on
            curr.symbol = curr.symbol.clone( String.format( "@Var_%d", _symbolTable()._localsVarCount() ) );
            _symbolTable().addSymbol( curr.symbol );
        }

        // if the symbol is declared inside a class/record, add it to the class/record type
        if( curr.symbol.isField() || curr.symbol.isStaticField() )
        {
            // find the class/record declaration surrounding the symbol
            SyntaxNode declType = context.syntaxNodeStack.find(
                elem -> ( elem instanceof ClassDeclType  )
                     || ( elem instanceof RecordDeclType )
            );

            // if the symbol is a field/static field but not in a class/record scope
            if( declType == null ) report_fatal( curr, "Field/<static field> declaration not yet supported" );

            // HACK: set the top symbol table scope as the class's members (since the field/static field declaration must be in the class's scope)
            // +   used only for semantic checking while inside the class, the members will be sorted based on their category later on
            // +   this updates the members of the class, since there is currently no exposed method for adding new members to the list
            if( declType instanceof ClassDeclType )
            {
                ClassDeclType classDeclType = ( ClassDeclType )declType;
                classDeclType.symbol._type()._members( _symbolTable()._locals() );
                //                                    ~~~~~~~~~~~~~~~~~~~~~~~~   <--- HACK
            }
            else if( declType instanceof RecordDeclType )
            {
                RecordDeclType recordDeclType = ( RecordDeclType )declType;
                recordDeclType.symbol._type()._members( _symbolTable()._locals() );
                //                                     ~~~~~~~~~~~~~~~~~~~~~~~~   <--- HACK
            }
        }
    }
    // VarIdent ::= (VarIdent_Err  ) error {: parser.report_error( "Bad variable declaration", null ); :};



    ////// const int a = 5, b = 6, c = 11;
    // ConstDecl ::= (ConstDecl_Plain) ConstDeclType ConstInitList semicol;
    @Override
    public void visit( ConstDecl_Plain curr )
    {
        context.syntaxNodeStack.remove();
    }

    ////// const int
    // ConstDeclType ::= (ConstDeclType_Plain) CONST_K Type;
    @Override
    public void visit( ConstDeclType_Plain curr )
    {
        context.syntaxNodeStack.add( curr );
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getType().symbol;
        if( left.isNoSym() ) return;

        curr.symbol = Symbol.newConst( "@ConstDeclType_Plain", left._type(), Symbol.CONST );
    }
    // ConstDeclType ::= (ConstDeclType_Err  ) CONST_K error {: parser.report_error( "Bad constant type", null ); :};
    @Override
    public void visit( ConstDeclType_Err curr )
    {
        context.syntaxNodeStack.add( curr );
        curr.symbol = SymbolTable.noSym;
    }

    ////// a = 5, b = 6, c = 11
    // ConstInitList ::= (ConstInitList_Init)                     ConstInit;
    // ConstInitList ::= (ConstInitList_Tail) ConstInitList comma ConstInit;

    ////// a = 5
    // ConstInit ::= (ConstInit_Plain) ident:IdentName Assignop Literal;
    @Override
    public void visit( ConstInit_Plain curr )
    {
        curr.symbol = SymbolTable.noSym;

        // if the current constant declaration is not yet supported
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ConstDeclType_Plain )
                 || ( elem instanceof ConstDeclType_Err   )
        );
        if( scope == null )
        {
            report_fatal( curr, "Constant declaration not yet supported" );
        }

        Symbol left = SymbolTable.noSym;
        if     ( scope instanceof ConstDeclType_Plain ) { left = ( ( ConstDeclType_Plain )scope ).symbol; }
        else if( scope instanceof ConstDeclType_Err   ) { /* if error do nothing */;                      }

        Symbol right = curr.getLiteral().symbol;
        if( right.isNoSym() ) return;
        
        // if the literal type is incompatible with the constant declaration type
        if( !SymbolType.isAssignableFrom( left._type(), right._type() ) )
        {
            report_error( curr.getLiteral(), "Literal type is incompatible with the declared constant type" );
            return;
        }

        // if the constant cannot be added to the symbol table
        curr.symbol = Symbol.newConst( curr.getIdentName(), left._type(), right._value() );
        if( !_symbolTable().addSymbol( curr.symbol ) )
        {
            report_error( curr, "Constant cannot be added since a symbol with the same name already exists" );
            return;
        }
    }
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
    }
    // Stmt ::= (Stmt_IfElse     ) IfScope lparen IfCondition rparen IfStmt ELSE_K ElseStmt;
    @Override
    public void visit( Stmt_IfElse curr )
    {
        context.syntaxNodeStack.remove();
    }
    // Stmt ::= (Stmt_DoWhile    ) DoWhileScope DoWhileStmt WHILE_K lparen DoWhileCondition rparen semicol;
    @Override
    public void visit( Stmt_DoWhile curr )
    {
        context.syntaxNodeStack.remove();
    }
    // Stmt ::= (Stmt_Switch     ) SwitchScope lparen SwitchExpr rparen lbrace CaseList rbrace;
    @Override
    public void visit( Stmt_Switch curr )
    {
        context.syntaxNodeStack.remove();

        Expr exprNode = ( ( SwitchExpr_Plain )curr.getSwitchExpr() ).getExpr();
        Symbol left = exprNode.symbol;
        if( left.isNoSym() ) return;

        // if the switch expresssion does not result in an int
        if( !left._type().isInt() )
        {
            report_error( exprNode, "Switch expression must result in an int" );
            return;
        }
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

        // if the break is not in a do-while or switch statement
        if( scope == null )
        {
            report_error( curr, "Break has no effect here (it must be inside a do-while or switch statement)" );
            return;
        }
    }
    // Stmt ::= (Stmt_Continue   ) CONTINUE_K    semicol;
    @Override
    public void visit( Stmt_Continue curr )
    {
        // find the surrounding do-while statement
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain )
        );

        // if the continue is not in a do-while statement
        if( scope == null )
        {
            report_error( curr, "Continue has no effect here (it must be inside a do-while statement)" );
            return;
        }
    }
    // Stmt ::= (Stmt_Return     ) RETURN_K      semicol;
    @Override
    public void visit( Stmt_Return curr )
    {
        visit_Stmt_Return( curr, null );
    }
    // Stmt ::= (Stmt_ReturnExpr ) RETURN_K Expr semicol;
    @Override
    public void visit( Stmt_ReturnExpr curr )
    {
        visit_Stmt_Return( curr, curr.getExpr() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Stmt_Return( Stmt curr, Expr expr )
    {
        // find the surrounding method declaration
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodDeclType )
        );

        // if the return is not in a method declaration
        if( scope == null )
        {
            report_error( curr, "Return has no effect here (it must be inside a method declaration)" );
            return;
        }

        Symbol left = SymbolTable.noSym;
        if     ( scope instanceof MethodDeclType_Plain ) left = ( ( MethodDeclType_Plain )scope ).symbol;
        else if( scope instanceof MethodDeclType_Empty ) left = ( ( MethodDeclType_Empty )scope ).symbol;
        // if the method type is not yet supported
        else
        {
            report_fatal( curr, "Method type not yet supported" );
        }

        SymbolType leftType = left._type();
        SymbolType rightType = ( expr != null ) ? expr.symbol._type() : SymbolTable.voidType;

        // if the return expression is not equivalent or a subtype of the method return type 
        if( !SymbolType.canOverride( leftType, rightType ) )
        {
            report_error( curr, "Return expression is not equivalent to or a subtype of the method return type" );
            return;
        }
    }
    // Stmt ::= (Stmt_Goto       ) GOTO_K ident:Label semicol;
    @Override
    public void visit( Stmt_Goto curr )
    {
        // try to find the label for the current function scope in the symbol table
        String labelName = String.format( "@Label_%s", curr.getLabel() );
        Symbol labelSymbol = _symbolTable().findSymbol( labelName );
        // if the label doesn't exist in the function scope
        if( labelSymbol.isNoSym() )
        {
            report_error( curr, "Label does not exist in the function scope" );
            return;
        }
    }
    // Stmt ::= (Stmt_Read       ) READ_K lparen Designator rparen semicol;
    @Override
    public void visit( Stmt_Read curr )
    {
        Symbol left = curr.getDesignator().symbol;

        if( left.isNoSym() ) return;

        // if the designator is not an lvalue
        if( !left.isLvalue() )
        {
            report_error( curr.getDesignator(), "This designator is not an lvalue and cannot be read into" );
            return;
        }

        // if the designator is not a primitive type
        if( !left._type().isPrimitiveType() )
        {
            report_error( curr.getDesignator(), "This designator must be a primitive type in order to be read into" );
            return;
        }
    }
    // Stmt ::= (Stmt_Print      ) PRINT_K lparen Expr                        rparen semicol;
    @Override
    public void visit( Stmt_Print curr )
    {
        visit_Stmt_Print( curr.getExpr() );
    }
    // Stmt ::= (Stmt_PrintFormat) PRINT_K lparen Expr comma int_lit:MinWidth rparen semicol;
    @Override
    public void visit( Stmt_PrintFormat curr )
    {
        visit_Stmt_Print( curr.getExpr() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Stmt_Print( Expr curr )
    {
        Symbol left = curr.symbol;

        if( left.isNoSym() ) return;

        // if the expression is not a primitive type
        if( !left._type().isPrimitiveType() )
        {
            report_error( curr, "This expression must be a primitive type" );
            return;
        }
    }
    // Stmt ::= (Stmt_Semicolon  ) semicol;

    ////// action symbols for opening a new scope and the if-statement's jump instructions
    // IfScope ::= (IfScope_Plain) IF_K;
    @Override
    public void visit( IfScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );

        // initialize the jump map
        curr.jumpprop = new JumpProp( state._codeGen() );
        curr.jumpprop.add( "@TrueBranch" );
        curr.jumpprop.add( "@FalseBranch" );
        curr.jumpprop.add( "@End" );
    }
    // IfCondition ::= (IfCondition_Plain) Condition;
    // IfStmt ::= (IfStmt_Plain) Stmt;
    // ElseStmt ::= (ElseStmt_Plain) Stmt;

    ////// action symbols for opening a new scope and the do-while-statement's jump instructions
    // DoWhileScope ::= (DoWhileScope_Plain) DO_K;
    @Override
    public void visit( DoWhileScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );

        // initialize the jump map
        curr.jumpprop = new JumpProp( state._codeGen() );
        curr.jumpprop.add( "@TrueBranch" );
        curr.jumpprop.add( "@Cond" );
        curr.jumpprop.add( "@FalseBranch" );
        curr.jumpprop.add( "@End" );
    }
    // DoWhileStmt ::= (DoWhileStmt_Plain) Statement;
    // DoWhileCondition ::= (DoWhileCondition_Plain) Condition;
    
    ////// action symbols for opening a new scope and the switch-statement's jump instructions
    // SwitchScope ::= (SwitchScope_Plain) SWITCH_K;
    @Override
    public void visit( SwitchScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );

        // initialize the jump map
        curr.jumpprop = new JumpProp( state._codeGen() );
        curr.jumpprop.add( "@End" );
    }
    // SwitchExpr ::= (SwitchExpr_Plain) Expr;

    ////// ident.ident[ expr ] = expr
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// ident.ident[ expr ]++
    ////// ident.ident[ expr ]--
    // DesignatorStmt ::= (DesignatorStmt_Assign    ) Designator Assignop Expr;
    @Override
    public void visit( DesignatorStmt_Assign curr )
    {
        Symbol left = curr.getDesignator().symbol;
        Symbol center = curr.getAssignop().symbol;
        Symbol right = curr.getExpr().symbol;

        if( left.isNoSym() ) return;

        // if the designator is not assignable
        if( !left.isLvalue() )
        {
            report_error( curr.getDesignator(), "This designator is not assignable to" );
            return;
        }

        if( right.isNoSym() ) return;

        // if the designator is not compatible with the expression
        if( !SymbolType.isAssignableFrom( left._type(), right._type() ) )
        {
            report_error( curr.getExpr(), "This expression is not assignable to" );
            return;
        }

        // if the assignment operator is not =
        if( center._value() != TokenCode.assign )
        {
            report_fatal( curr.getExpr(), "Assignment operator not yet supported" );
        }
    }
    // DesignatorStmt ::= (DesignatorStmt_Call      ) MethodCall ActParsScope ActPars rparen;
    @Override
    public void visit( DesignatorStmt_Call curr )
    {
        // remove the function call's designator from the syntax node stack
        context.syntaxNodeStack.remove();
    }
    // DesignatorStmt ::= (DesignatorStmt_Plusplus  ) Designator plusplus;
    @Override
    public void visit( DesignatorStmt_Plusplus curr )
    {
        visit_DesignatorStmt_IncrementOrDecrement( curr.getDesignator() );
    }
    // DesignatorStmt ::= (DesignatorStmt_Minusminus) Designator minusminus;
    @Override
    public void visit( DesignatorStmt_Minusminus curr )
    {
        visit_DesignatorStmt_IncrementOrDecrement( curr.getDesignator() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_DesignatorStmt_IncrementOrDecrement( Designator curr )
    {
        Symbol left = curr.symbol;

        if( left.isNoSym() ) return;

        // if the designator is not an lvalue
        if( !left.isLvalue() )
        {
            report_error( curr, "This designator is not an lvalue and cannot be incremented/decremented" );
            return;
        }

        // if the designator is not an int
        if( !left._type().isInt() )
        {
            report_error( curr, "This designator must be of type int to allow being incremented/decremented" );
            return;
        }
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

    ////// action symbols for opening a new scope and the case-statement's jump instructions
    // CaseScope ::= (CaseScope_Plain) CASE_K int_lit:CaseNum colon;
    @Override
    public void visit( CaseScope_Plain curr )
    {
        int caseNum = Integer.parseInt( curr.getCaseNum() );

        // find the switch scope surrounding this symbol
        SwitchScope_Plain scope = ( SwitchScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof SwitchScope_Plain )
        );

        // if the switch scope doesn't exist
        if( scope == null )
        {
            report_fatal( curr, "Switch statement not yet supported" );
        }
        
        // if the case number already exists
        if( !scope.jumpprop.add( String.format( "@Case_%d", caseNum ) ) )
        {
            report_error( curr, "Case with the same number already exists" );
            return;
        }
    }



    ////// <epsilon>
    ////// expr
    ////// expr, expr, expr
    // ActPars ::= (ActPars_Plain) ActParsList;
    @Override
    public void visit( ActPars_Plain curr )
    {
        _symbolTable().closeScope();
    }
    // ActPars ::= (ActPars_Empty) ;
    @Override
    public void visit( ActPars_Empty curr )
    {
        visit_ActParam( curr, null );
        _symbolTable().closeScope();
    }

    ////// action symbol for opening a new scope
    // ActParsScope ::= (ActParsScope_Plain) lparen;
    @Override
    public void visit( ActParsScope_Plain curr )
    {
        // open a temporary scope for the activation parameters
        _symbolTable().openScope();

        // find the method call scope surrounding this symbol
        MethodCall_Plain MethodCall = ( MethodCall_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodCall_Plain )
        );
        // if the method call scope doesn't exist
        if( MethodCall == null )
        {
            report_fatal( curr, "Method call not yet supported" );
        }

        // if an error has been reported somewhere in the designator
        Symbol design = MethodCall.symbol;
        if( design.isNoSym() )
        {
            return;
        }
        // if the designator is not a function/method
        if( !design.isFunction() && !design.isMethod() )
        {
            report_error( MethodCall, "Expected function or method" );
            MethodCall.symbol = SymbolTable.noSym;
            return;
        }
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
        visit_ActParam( curr, curr.getExpr() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_ActParam( SyntaxNode curr, Expr currExpression )
    {
        // the current activation parameter's index in the function/method declaration
        int actParamIdx = _symbolTable()._localsActivParamCount();
        ActParam actParamNode = null;

        // if this node has an associated expression (is an ActParam and not an ActPars_Empty), reset its symbol
        if( currExpression != null )
        {
            actParamNode = ( ActParam_Plain )curr;
            actParamNode.symbol = null;
        }

        // find the method call scope surrounding this symbol
        // +   the check if this is a function/method is performed in the ActParsScope
        MethodCall_Plain MethodCall = ( MethodCall_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodCall_Plain )
        );
        // if the method call scope doesn't exist
        if( MethodCall == null )
        {
            report_fatal( curr, "Function/method call not yet supported" );
        }
        // if there was an error in the method call scope (either designator error, or one of the formal parameters reported an error that should stop the next formal parameters' checking)
        if( MethodCall.symbol.isNoSym())
        {
            return;
        }

        // if the actual parameter list is empty (currExpression is null in ActPars_Empty)
        List<Symbol> formParams = MethodCall.symbol._params()._sorted();
        if( currExpression == null )
        {
            // but the function has formal parameters
            if( !formParams.isEmpty() )
            {
                report_error( MethodCall, "Less parameters given than expected in function/method call" );
                MethodCall.symbol = SymbolTable.noSym;
            }
            
            // IMPORTANT: the <empty activation parameter list> checking always ends here
            return;
        }
        // if more activation parameters are given than expected in the function declaration
        if( actParamIdx >= formParams.size() )
        {
            report_error( currExpression, "More parameters given than expected in function/method call" );
            MethodCall.symbol = SymbolTable.noSym;
            return;
        }
        // if less activation parameters are given than expected in the function declaration
        // +   if there are no more activation parameters after the current
        boolean hasNext = ( /*Expr*/curr.getParent() ).getParent() instanceof ActParsList;
        if( !hasNext && actParamIdx != formParams.size()-1 )
        {
            report_error( MethodCall, "Less parameters given than expected in function/method call" );
            MethodCall.symbol = SymbolTable.noSym;
            // IMPORTANT: don't return here, because the current parameter hasn't yet been checked
         // return;
        }

        // if the activation parameter can't be assigned to the formal parameter (but both are valid)
        Symbol formParam = formParams.get( actParamIdx );
        Symbol actParam = currExpression.symbol;
        if( !formParam.isNoSym() && !actParam.isNoSym() && !SymbolType.isAssignableFrom( formParam._type(), actParam._type() ) )
        {
            report_error( currExpression, "This expression is incompatible with the function/method's formal parameter" );
            // IMPORTANT: don't return here, because the next parameter's index hasn't yet been set (by adding the activation parameter to the symbol table)
         // return;
        }

        // add an activation parameter to the symbol table
        // +   only used for setting the next activation parameter's index
        actParamNode.symbol = Symbol.newActivParam( String.format( "@Param_%d", actParamIdx ), actParam._type(), actParamIdx );
        _symbolTable().addSymbol( actParamNode.symbol );
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
        // initialize the jump map
        curr.jumpprop = new JumpProp( state._codeGen() );
        curr.jumpprop.add( "@TrueBranch" );
        curr.jumpprop.add( "@FalseBranch" );
        curr.jumpprop.add( "@Relop" );
    }
    // CondTerm ::= (CondTerm_Nest) CondNest;
    @Override
    public void visit( CondTerm_Nest curr )
    {
        // initialize the jump map
        curr.jumpprop = new JumpProp( state._codeGen() );
        curr.jumpprop.add( "@TrueBranch" );
        curr.jumpprop.add( "@FalseBranch" );
    }

    ////// ((( cterm && cterm || cterm )))
    // CondNest ::= (CondNest_Head) lparen CondTermList rparen;
    // CondNest ::= (CondNest_Tail) lparen CondNest     rparen;
    
    ////// expr   |   expr < expr   |   expr != expr
    // CondFact ::= (CondFact_Expr ) Expr;
    @Override
    public void visit( CondFact_Expr curr )
    {
        Symbol left = curr.getExpr().symbol;

        // if the left symbol is not defined
        if( left.isNoSym() ) return;

        // if the symbol's type is not a bool
        if( !left._type().isBool() )
        {
            report_error( curr, "This expression must result in a bool" );
            return;
        }
    }
    // CondFact ::= (CondFact_Relop) Expr Relop Expr;
    @Override
    public void visit( CondFact_Relop curr )
    {
        Symbol left = curr.getExpr().symbol;
        Symbol right = curr.getExpr1().symbol;
        int relop = curr.getRelop().symbol._value();

        // if any of the symbols is not defined
        if( left.isNoSym() || right.isNoSym() ) return;

        // if the symbols are not compatible
        if( !SymbolType.isCompatibleWith( left._type(), right._type() ) )
        {
            report_error( curr.getExpr1(), "The left and right side of the comparison are not compatible types" );
            return;
        }

        // if the symbols are references and the relational operator is not an (in)equality comparison ( == or != )
        if( left._type().isReferenceType() && !TokenCode.isEqualityComparison( relop ) )
        {
            report_error( curr.getExpr1(), "Cannot compare references" );
            return;
        }
    }

    ////// action symbols for finding out the next term's starting address
    // CondTermScope ::= (CondTermScope_Plain) Aorop;



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
    ////// ((( expr )))
    // Factor ::= (Factor_Designator ) Designator;
    @Override
    public void visit( Factor_Designator curr )
    {
        curr.symbol = SymbolTable.noSym;
        
        Symbol left = curr.getDesignator().symbol;

        if( left.isNoSym() ) return;
        
        // if the designator is not a rvalue
        if( !left.isRvalue() )
        {
            report_error( curr, "This expression doesn't result in a value" );
            return;
        }

        curr.symbol = left;
    }
    // Factor ::= (Factor_MethodCall ) MethodCall ActParsScope ActPars rparen;
    @Override
    public void visit( Factor_MethodCall curr )
    {
        curr.symbol = curr.getMethodCall().symbol;
        // remove the function call's designator from the syntax node stack
        context.syntaxNodeStack.remove();
    }
    // Factor ::= (Factor_Literal    ) Literal;
    @Override
    public void visit( Factor_Literal curr )
    {
        curr.symbol = curr.getLiteral().symbol;
    }
    // Factor ::= (Factor_NewVar     ) NEW_K Type;
    @Override
    public void visit( Factor_NewVar curr )
    {
        curr.symbol = SymbolTable.noSym;
        
        Symbol left = curr.getType().symbol;
        
        if( left.isNoSym() ) return;
        
        // if the <type token> is not a <class type> or <record type>
        if( !left.isType() || !left._type().hasMembers() )
        {
            report_error( curr.getType(), "Cannot instantiate a non-class type" );
            return;
        }

        curr.symbol = left;
    }
    // Factor ::= (Factor_NewArray   ) NEW_K Type lbracket Expr rbracket;
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
            report_error( curr.getType(), "Cannot instantiate a non-type" );
            return;
        }

        curr.symbol = Symbol.newVar(
            "@Factor_NewArray",
            SymbolType.newArray( "@Factor_NewArray", left._type() ),
            Symbol.NO_VALUE,
            _symbolTable()._localsLevel(),
            _symbolTable()._localsVarCount()
        );


        if( right.isNoSym() ) return;

        // if the expression does not result in an int
        if( !right._type().isInt() )
        {
            report_error( curr.getExpr(), "This expression must result in an int" );
            return;
        }
    }
    // Factor ::= (Factor_Expr       ) lparen Expr rparen;
    @Override
    public void visit( Factor_Expr curr )
    {
        curr.symbol = curr.getExpr().symbol;
    }

    ////// ident.ident[ expr ]( expr, expr, expr )
    // MethodCall ::= (MethodCall_Plain) Designator;
    @Override
    public void visit( MethodCall_Plain curr )
    {
        // reset the function call's symbol
        curr.symbol = SymbolTable.noSym;
        // add the function call's designator to the syntax node stack
        context.syntaxNodeStack.add( curr );

        // get the designator's symbol
        Symbol left = curr.getDesignator().symbol;

        if( left.isNoSym() ) return;

        // if the function call is a call to the supertype's constructor (super())
        if( left.isSuper() )
        {
            // replace the designator's symbol with the supertype's constructor
            // NOTE: use _type() instead of _base(), since the <'super' symbol>'s type is the <'this' symbol>'s supertype
            left = left._type()._members().findSymbol( "@Constructor" );
            curr.getDesignator().symbol = left;

            // get the surrounding function's declaration
            SyntaxNode methodDeclType = context.syntaxNodeStack.find(
                elem -> ( elem instanceof MethodDeclType )
            );

            // get the surrounding function's symbol
            Symbol methodSymbol = null;
            if( methodDeclType instanceof MethodDeclType_Empty ) methodSymbol = ( ( MethodDeclType_Empty )methodDeclType ).symbol;

            // if 'super()' is used outside the constructor
            if( methodSymbol == null || !methodSymbol.isConstructor() )
            {
                report_error( curr, "'super()' can only be used inside the constructor" );
                return;
            }

            // check if the 'super();' statement is the first statement in the constructor
            // +   the 'super();' statement must not be nested: <void @constructor() { {{{ super(); }}} }> is not allowed
            // +   NOTE: the statement is going to be in a <StatementList_Tail ::= ...>, since the <MethodDecl ::= ...> node contains a StatementList!
            SyntaxNode node = curr;
            int nestLevel = 0;
            boolean isInStatementList = false;
            boolean isFirstStmt = true;
            while( true )
            {
                ////// MethodDecl ::= (MethodDecl_Plain) MethodDeclType lparen FormPars rparen MethodDeclBody VarDeclList MethodDeclCode lbrace (((StatementList))) rbrace;
                if( node instanceof MethodDecl )
                {
                    isInStatementList = false;
                    break;
                }
                ////// StatementList ::= (StatementList_Tail ) StatementList Statement;
                ////// StatementList ::= (StatementList_Empty) ;
                if( node instanceof StatementList )
                {
                    // if the <statement list> node has just been entered
                    if( !isInStatementList )
                    {
                        // increment the current nest level
                        isInStatementList = true;
                        nestLevel++;

                        // check if there is a statement before 'super();'
                        SyntaxNode prevNode = ( ( StatementList_Tail )node ).getStatementList();
                        if( !( prevNode instanceof StatementList_Empty ) ) isFirstStmt = false;

                        // if the 'super();' statement isn't the first! statement in the constructor's! scope
                        if( nestLevel > 1 || !isFirstStmt )
                        {
                            report_error( curr, "'super()' must be the first statement in the constructor's scope" );
                            return;
                        }
                    }
                }
                ////// Statement ::= (Statement_Plain)           Stmt;
                ////// Statement ::= (Statement_Label) StmtLabel Stmt;
                ////// Statement ::= (Statement_Scope) lbrace StatementList rbrace;
                ////// ...
                else
                {
                    isInStatementList = false;
                }

                // go up the syntax tree
                node = node.getParent();
            }
        }

        // set the function call's symbol to the designator's symbol
        curr.symbol = left;
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
        // try to find the symbol in the symbol table
        curr.symbol = _symbolTable().findSymbol( curr.getName() );

        // if the symbol does not exist in the symbol table
        if( curr.symbol.isNoSym() )
        {
            report_error( curr, "This symbol has not been declared" );
            return;
        }
    }
    // Designator ::= (Designator_This   ) THIS_K;
    @Override
    public void visit( Designator_This curr )
    {
        // reset the current designator's symbol
        curr.symbol = SymbolTable.noSym;

        // find the class! declaration surrounding 'this' (not method declaration)
        // +   this allows for using 'this' in the class's field initializers
        boolean isInClassScope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ClassDeclType )
        ) != null;
        // if the 'this' designator is not in a class declaration
        if( !isInClassScope )
        {
            report_error( curr, "'this' has no effect here (it must be inside a class declaration)" );
            return;
        }

        // try to find the 'this' symbol in the symbol table
        curr.symbol = _symbolTable().findSymbol( "this" );
        // if the 'this' symbol does not exist in the symbol table
        if( curr.symbol.isNoSym() )
        {
            report_fatal( curr, "Cannot find 'this' symbol in class scope" );
        }
    }
    // Designator ::= (Designator_Super  ) SUPER_K;
    @Override
    public void visit( Designator_Super curr )
    {
        // reset the current designator's symbol
        curr.symbol = SymbolTable.noSym;

        // find the class! declaration surrounding 'super' (not method declaration)
        // +   this allows for using 'super' in the class's field initializers
        boolean isInClassScope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof ClassDeclType )
        ) != null;
        // if the 'super' designator is not in a class declaration
        if( !isInClassScope )
        {
            report_error( curr, "'super' has no effect here (it must be inside a class declaration)" );
            return;
        }

        // try to find the 'this' symbol in the symbol table
        Symbol thisSymbol = _symbolTable().findSymbol( "this" );
        // if the 'this' symbol does not exist in the symbol table
        if( thisSymbol.isNoSym() )
        {
            report_fatal( curr, "Cannot find 'this' symbol in class scope" );
        }

        // get the next syntax node after 'super'
        SyntaxNode next = /*Designator_Super*/curr.getParent();
        // check if the 'super' keyword refers to the supertype's constructor or a supertype's method
        if( !( next instanceof Designator
            || next instanceof MethodCall )
        )
        {
            report_error( next, "'super' can only be used to call the supertype's virtual methods or the constructor" );
            return;
        }

        // get the 'super' symbol's type, which is the supertype of the 'this' symbol
        SymbolType superType = thisSymbol._type()._base();
        // set the current designator's symbol
        // +   important: the 'super' symbol is the same as the 'this' symbol, except that its type is the supertype of the 'this' symbol
        // +   this means that the zeroth method parameter (which is 'this') can be used as 'super' as well
        //     +   but it can only be used to call the supertype's methods non-virtually (directly instead through the virtual table)
        curr.symbol = Symbol.newConst( "super", superType, 0 );
    }
    // Designator ::= (Designator_Null   ) NULL_K;
    @Override
    public void visit( Designator_Null curr )
    {
        // set the current designator's symbol to be the 'null' symbol
        curr.symbol = SymbolTable.nullSym;
    }
    // Designator ::= (Designator_Member ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Member curr )
    {
        // reset the current designator's symbol
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;

        // if the previous designator segment does not exist, an error must have been reported somewhere in the previous segments, return
        if( left.isNoSym() ) return;

        // if the previous symbol is not a class/record (doesn't have members)
        if( !left._type().hasMembers() )
        {
            report_error( curr, "Expected class/record member, but the left designator is not a class/record" );
            return;
        }

        // find the symbol with the given name in the previous designator's type's members
        Symbol member = left._type()._members().findSymbol( curr.getName() );

        // if 'super' is used to access the inherited supertype's fields
        if( left.isSuper() && !member.isMethod() )
        {
            report_error( curr, "'super' cannot be used to access the inherited supertype's fields (use 'this' instead)" );
            return;
        }
        // if the previous symbol doesn't contain the current field/member
        if( member.isNoSym() )
        {
            report_error( curr, "The specified class/record does not contain this member" );
            return;
        }
        // if the previous designator is a type (static access) and its non-static member is accessed
        if( left.isType() && !member.isStaticField() )
        {
            report_error( curr, "This non-static class/record member cannot be accessed in a static way" );
            return;
        }
        // if the previous designator is not a type (non-static access) and its type's static member is accessed
        if( !left.isType() && member.isStaticField() )
        {
            report_error( curr, "This static class/record member cannot be accessed in a non-static way" );
            return;
        }

        // save the class/record's member -- don't modify it in the future, since it is a part of the class/record's definition
        curr.symbol = member;
    }
    // Designator ::= (Designator_ArrElem) Designator lbracket Expr rbracket;
    @Override
    public void visit( Designator_ArrElem curr )
    {
        // set the current designator to the default value
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;
        Symbol right = curr.getExpr().symbol;

        // if the previous designator segment does not exist, an error must have been reported somewhere in the previous segments, return
        if( left.isNoSym() ) return;

        // if the previous symbol is not an array
        if( !left._type().isArray() )
        {
            report_error( curr, "The left side of the brackets is not an array" );
            return;
        }

        // find the symbol with the given name in the previous designator's type's members
        Symbol member = left._type()._members().findSymbol( "@Element" );

        // if the expression inside the angle brackets has an error, an error must have already been reported for the expression, return
        if( right.isNoSym() ) return;

        // if the expression inside the angle brackets does not result in an int
        if( !right._type().isInt() )
        {
            report_error( curr, "This expression must result in an int" );
            return;
        }

        // save the array's member -- don't modify it in the future, since it is a part of the array's type definition
        curr.symbol = member;
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
        visit_Type( curr, curr.getTypeName() );
    }

    ////// int | bool | char | ident
    // Type ::= (Type_Ident) ident:Type;
    @Override
    public void visit( Type_Ident curr )
    {
        visit_Type( curr, curr.getTypeName() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Type( SyntaxNode curr, String typeName )
    {
        Symbol typeSymbol = SymbolTable.noSym;
        if( curr instanceof Type_Ident )
        {
            // if the symbol has already been initialized by the forward class declaration visitor, return
            Type_Ident typeNode = ( Type_Ident )curr;
            if( typeNode.symbol != null ) return;

            // initialize the type's symbol
            typeSymbol = typeNode.symbol = _symbolTable().findSymbol( typeName );
        }
        else if( curr instanceof ReturnType_Ident )
        {
            // if the symbol has already been initialized by the forward class declaration visitor, return
            ReturnType_Ident typeNode = ( ReturnType_Ident )curr;
            if( typeNode.symbol != null ) return;

            // initialize the type's symbol
            typeSymbol = typeNode.symbol = _symbolTable().findSymbol( typeName );
        }
        else
        {
            report_fatal( curr, "Type's context not yet supported" );
        }

        // if the symbol is missing from the symbol table
        if( typeSymbol.isNoSym() )
        {
            report_error( curr, "Expected type here, but this symbol has not been declared" );
            return;
        }
        // if the symbol is not a type
        if( !typeSymbol.isType() )
        {
            report_error( curr, "Expected type here, but this isn't a type" );
            return;
        }
    }

    ////// 1202 | 'c' | true
    // Literal ::= (Literal_Int ) int_lit :Literal;
    @Override
    public void visit( Literal_Int curr ) { curr.symbol = Symbol.newConst( "@Literal_Int", SymbolTable.intType, Integer.parseInt( curr.getLiteral() ) ); }
    // Literal ::= (Literal_Char) char_lit:Literal;
    @Override
    public void visit( Literal_Char curr )  { curr.symbol = Symbol.newConst( "@Literal_Char", SymbolTable.charType, curr.getLiteral().charAt( 1 ) ); }
    // Literal ::= (Literal_Bool) bool_lit:Literal;
    @Override
    public void visit( Literal_Bool curr ) { curr.symbol = Symbol.newConst( "@Literal_Bool", SymbolTable.boolType, Boolean.parseBoolean( curr.getLiteral() ) ? 1 : 0 ); }

    ////// =
    // Assignop ::= (Assignop_Assign) assign:Assignop;
    @Override
    public void visit( Assignop_Assign curr ) { curr.symbol = Symbol.newConst( "@Assignop_Assign", SymbolTable.intType, TokenCode.assign ); }

    ////// &&  |  ||
    // Aorop ::= (Aorop_And) and:Aorop;
    @Override
    public void visit( Aorop_And curr ) { curr.symbol = Symbol.newConst( "@Aorop_And", SymbolTable.intType, TokenCode.and ); }
    // Aorop ::= (Aorop_Or ) or :Aorop;
    @Override
    public void visit( Aorop_Or curr ) { curr.symbol = Symbol.newConst( "@Aorop_Or", SymbolTable.intType, TokenCode.or ); }

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

}
