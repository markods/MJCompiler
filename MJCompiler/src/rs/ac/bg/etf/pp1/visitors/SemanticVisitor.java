package rs.ac.bg.etf.pp1.visitors;

import java.util.List;

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
    private Context context = new Context();
    
    private static class Context
    {
        public final BoolProp errorDetected = new BoolProp();
        public final CountProp programVarCnt = new CountProp();

        public final StackProp<SyntaxNode> syntaxNodeStack = new StackProp<>();

        public final RefProp<Designator> lastDesignator = new RefProp<>();
        public final SetProp<Integer> lastSwitchNumbers = new SetProp<>();
    }


    public boolean hasErrors() { return context.errorDetected.get(); }
    public int getVarCount() { return context.programVarCnt.get(); }

    // only have a single underscore point at the start of the error tokens
    private void report_basic( SyntaxNode node, String message )
    {
        // FIXME: restore the original behaviour when the error reporting prints lines and underlines their errors (instead of just printing errors)
     // report_error( node, message, false );
        report_error( node, message, true );
        throw new Error( message );
    }

    // underscore all error tokens
    private void report_verbose( SyntaxNode node, String message )
    {
        report_error( node, message, true );
        throw new Error( message );
    }

    // report verbose and throw an exception
    private void report_fatal( SyntaxNode node, String message )
    {
        report_error( node, message, true );
        throw new Error( message );
    }

    private void report_error( SyntaxNode node, String message, boolean entireScope )
    {
        context.errorDetected.set();

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
        context.syntaxNodeStack.remove();

        Symbol left = curr.getProgramType().symbol;
        if( left.isNoSym() ) return;

        // update the program's local symbols
        left._locals( SymbolTable._locals() );

        // if the <void main()> function doesn't exist in the program
        Symbol main = left._locals().findSymbol( "main" );
        if( main.isNoSym() )
        {
            report_verbose( curr.getProgramType(), "Main function missing from program" );
            return;
        }
        // if the main function's return type is not void
        if( !main._type().isVoidType() )
        {
            report_verbose( curr.getProgramType(), "Main function's return type is not void" );
            return;
        }
        // if the main function has parameters
        if( main._paramCount() != 0 )
        {
            report_verbose( curr.getProgramType(), "Main function can't have parameters" );
            return;
        }

        // update the number of variables in the program
        // IMPORTANT: Scope's nvars field doesn't correctly count the number of variables, don't use it!
        context.programVarCnt.reset();
        for( Symbol symbol : left._locals() )
        {
            if( symbol.isVar() ) context.programVarCnt.get_inc();
        }
    }

    ////// program my_program
    // ProgramType ::= (ProgramType_Plain) PROGRAM_K ident:ProgramName;
    @Override
    public void visit( ProgramType_Plain curr )
    {
        context.syntaxNodeStack.add( curr );
        SymbolType programType = SymbolType.newClass( "@prog", SymbolTable.anyType, null );
        curr.symbol = Symbol.newProgram( curr.getProgramName(), programType, null );

        // if the program cannot be added to the symbol table
        if( !SymbolTable.addSymbol( curr.symbol ) )
        {
            // NOTE: currently this should never happen since there is only one program
            report_verbose( curr, "A symbol with the same name already exists" );
            
            // just a precaution
            curr.symbol = curr.symbol.clone( String.format( "@Program[%d]", SymbolTable._globalsSizeNoPredef() ) );
            SymbolTable.addSymbol( curr.symbol );
        }

        // open the program's scope
        // +   this scope won't be closed, so that the symbol table can return the global symbols when needed
        SymbolTable.openScope();
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
        context.syntaxNodeStack.remove();
        // get the class members from the symbol table
        SymbolMap classMembers = SymbolTable._locals();
        SymbolTable.closeScope();

        // get the class's type
        Symbol classType = curr.getClassDeclType().symbol;
        if( classType.isNoSym() ) return;

        // update the class members' indexes by category
        SymbolMap methods = new SymbolMap();
        SymbolMap fields = new SymbolMap();
        SymbolMap staticFields = new SymbolMap();
        for( Symbol member : classMembers )
        {
            if     ( member.isMethod()      ) methods.addSymbol( member );
            else if( member.isField()       ) fields.addSymbol( member );
            else if( member.isStaticField() ) staticFields.addSymbol( member );
            else                              report_fatal( curr.getClassDeclType(), String.format( "Class member not yet supported: %s", member._name() ) );
        }

        // remove the 'this' field from the class type
        fields.removeSymbol( "this" );

        int idx = 0;
        // compact the <methods>', <fields>' and <static fields>' indexes
        idx = 0;   for( Symbol member : methods     ._sorted() ) { member._memberIdx( idx++ ); }
        idx = 0;   for( Symbol member : fields      ._sorted() ) { member._memberIdx( idx++ ); }
        idx = 0;   for( Symbol member : staticFields._sorted() ) { member._memberIdx( idx++ ); }
        idx = 0;

        // join all the now sorted members by category
        SymbolMap members = staticFields;
        members.addSymbols( fields );
        members.addSymbols( methods );

        // set the class's members in the correct order
        classType._members( members );
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
    private void visit_ClassDeclType( ClassDeclType curr, String className, Symbol extendsType )
    {
        context.syntaxNodeStack.add( curr );
        String dummyName = String.format( "@Class[%d]", SymbolTable._localsSize() );

        // if the class name or the extends type is missing, use default values
        if( className == null ) className = dummyName;
        if( extendsType == null || extendsType.isNoSym() ) extendsType = SymbolTable.anySym;

        // if the type that is extended is not a class
        if( !extendsType.isAnySym() && !extendsType._type().isClass() )
        {
            report_verbose( curr, "Extended type must be a class" );
            
            // set the extended type to be the base type for everything
            extendsType = SymbolTable.anySym;
        }

        // add the extended class's members to this class
        SymbolType classType = SymbolType.newClass( className, extendsType._type(), extendsType._locals() );
        curr.symbol = Symbol.newType( className, classType );

        // if the class cannot be added to the symbol table
        if( !SymbolTable.addSymbol( curr.symbol ) )
        {
            report_verbose( curr, "A symbol with the same name already exists" );
            
            // add an artificial class used for type checking later on
            classType = SymbolType.newClass( dummyName, extendsType._type(), extendsType._locals() );
            curr.symbol = Symbol.newType( dummyName, classType );
            
            SymbolTable.addSymbol( curr.symbol );
        }

        // open the class's scope
        SymbolTable.openScope();

        // add a dummy 'this' field to the class scope with the index -1 (remove it later)
        Symbol thisSymbol = Symbol.newField( "this", classType, Symbol.NO_VALUE, -1 );
        SymbolTable.addSymbol( thisSymbol );
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
        context.syntaxNodeStack.remove();
        SymbolTable.closeScope();
    }

    ////// void foo
    ////// A foo
    // MethodDeclType ::= (MethodDeclType_Plain) ReturnType ident:MethodName;
    @Override
    public void visit( MethodDeclType_Plain curr )
    {
        SyntaxNode scope = context.syntaxNodeStack.add( curr );
        int methodIdx = SymbolTable._localsSize();
        
        // create a function symbol
        // +    update the locals later on
        Symbol left = curr.getReturnType().symbol;

        if( scope instanceof ProgramType )
        {
            curr.symbol = Symbol.newFunction( curr.getMethodName(), left._type(), Symbol.NO_VALUE, null );
        }
        else if( scope instanceof ClassDeclType )
        {
            // add the this parameter as the -1'st method parameter
            Symbol classType = ( ( ClassDeclType )scope ).symbol;
            Symbol thisParam = Symbol.newFormalParam( "this", classType._type(), -1, SymbolTable._localsLevel()+1 );
            SymbolMap formalParams = new SymbolMap();
            formalParams.addSymbol( thisParam );
            curr.symbol = Symbol.newMethod( curr.getMethodName(), left._type(), Symbol.NO_VALUE, methodIdx, formalParams );
        }
        // ...
        else
        {
            report_fatal( curr, "Function/method declaration type not yet supported" );
        }

        // open a scope for the function's formal parameters
        SymbolTable.openScope();
    }

    ////// action symbol for opening a new scope
    // MethodDeclBody ::= (MethodDeclBody_Plain) ;
    @Override
    public void visit( MethodDeclBody_Plain curr )
    {
        // get the function's formal parameters and also close the formal parameter scope
        SymbolMap formalParams = SymbolTable._locals();
        SymbolTable.closeScope();
        // get the function declaration type
        MethodDeclType methodDeclType = ( MethodDeclType )( context.syntaxNodeStack.top() );
        Symbol function = methodDeclType.symbol;
        
        // update the function's formal parameters
        function._params( formalParams );
        // prepare the function index and its dummy name
        // +   IMPORTANT: get the function's index here, in its containing scope, not after the function's scope is reopened
        int functionIdx = SymbolTable._localsSize();
        String dummyName = String.format( "@Method[%d]", functionIdx );


        // if the function is a method
        if( function.isMethod() )
        {
            Symbol method = function;
            // get the method's surrounding class
            ClassDeclType classDeclType = ( ClassDeclType )context.syntaxNodeStack.find(
                elem -> ( elem instanceof ClassDeclType )
            );
            
            // find the inherited method from the class's superclass, if such a method exists
            Symbol inheritedMethod = classDeclType.symbol._members().findSymbol( method._name() );

            // if the inherited method exists
            if( !inheritedMethod.isNoSym() )
            {
                // save the inherited method's index in the class declaration (useful for keeping the method's index in the virtual table)
                method._memberIdx( inheritedMethod._memberIdx() );

                // if the current method can override the class's inherited method (because their declarations are 'compatible')
                if( Symbol.canOverride( method, inheritedMethod ) )
                {
                    // get the class's members (the current symbol table scope), and close the symbol table scope
                    SymbolMap classMembers = SymbolTable._locals();
                    SymbolTable.closeScope();
                    
                    // remove the original method from the class's locals, and add the new method
                    classMembers.removeSymbol( inheritedMethod._name() );
                    classMembers.addSymbol( method );

                    // restore the class's members (in the symbol table scope)
                    SymbolTable.openScope();
                    SymbolTable.addSymbols( classMembers );
                }
                else
                {
                    report_verbose( methodDeclType, "Cannot override, method's signature different from inherited method's signature" );
                    // give the method a new dummy name, guaranteed not to cause collisions in the symbol table
                    function = method = method.clone( dummyName );
                    // update the <method declaration>'s symbol with the dummy function
                    methodDeclType.symbol = method;
                }
            }
        }

        // try to add the function to the symbol table
        // +   if the function/method cannot be added to the symbol table
        if( !SymbolTable.addSymbol( function ) )
        {
            report_verbose( methodDeclType, "A symbol with the same name already exists" );
            // give the method a new dummy name, guaranteed not to cause collisions in the symbol table
            function = function.clone( dummyName );
            // update the <method declaration>'s symbol with the dummy function
            methodDeclType.symbol = function;
            // add an artificial method used for type checking later on
            SymbolTable.addSymbol( function );
        }




        // restore the formal parameter scope, in which the function's locals will be declared in the future
        SymbolTable.openScope();
        SymbolTable.addSymbols( formalParams );
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
        context.syntaxNodeStack.remove();
    }
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};
    @Override
    public void visit( FormParam_Err curr )
    {
        // don't do this next line since the syntax node stack wasn't added to in the <error formal parameter>
     // context.syntaxNodeStack.remove();

        int paramIdx = SymbolTable._localsSize();

        // add a dummy symbol as the formal parameter
        SymbolTable.addSymbol(
            Symbol.newFormalParam(
                String.format( "@Param[%d]", paramIdx ),
                SymbolTable.anyType,
                paramIdx,
                SymbolTable._localsLevel()
            )
        );
    }

    ////// int
    ////// Node
    // FormParamType ::= (FormParamType_Plain) Type;
    @Override
    public void visit( FormParamType_Plain curr )
    {
        context.syntaxNodeStack.add( curr );
        curr.symbol = SymbolTable.noSym;
        
        Symbol left = curr.getType().symbol;
        curr.symbol = Symbol.newConst( "@FormParamType_Plain", left._type(), Symbol.FORMAL_PARAM );
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
        SyntaxNode node = context.syntaxNodeStack.add( curr );
        curr.symbol = SymbolTable.noSym;
        if( varDeclType == null ) return;
        
        // if the variable/field declaration is located outside a program/class/function/method
        int varKind = Symbol.VAR;
        if     ( node instanceof ProgramType    ) { varKind = Symbol.VAR;   }
        else if( node instanceof ClassDeclType  ) { varKind = Symbol.FIELD; }
        else if( node instanceof MethodDeclType ) { varKind = Symbol.VAR;   }
        else
        {
            report_fatal( curr, "Variable/field declaration is located outside a program/class/function/method" );
        }

        // if the static modifier is used
        if( isStatic == true )
        {
            // on a class field, that's supported
            if( varKind == Symbol.FIELD )
            {
                varKind = Symbol.STATIC_FIELD;
            }
            // otherwise, the modifier cannot be applied
            else
            {
                report_verbose( curr, "Variables cannot be static (currently only fields can be static)" );
                return;
            }
        }

        Symbol right = varDeclType.symbol;
        if( right.isNoSym() ) return;

        curr.symbol = Symbol.newConst( "@VarDeclType_Plain", right._type(), varKind );
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
        curr.symbol = SymbolTable.noSym;

        // if the variable/field is declared outside a variable declaration or a formal parameter declaration
        SyntaxNode node = context.syntaxNodeStack.top();
        if( !( node instanceof VarDeclType )
         && !( node instanceof FormParamType ) )
        {
            report_fatal( curr, "Variable/field/<formal parameter> cannot be declared outside of a variable/field/<formal parameter> declaration" );
        }

        // if the current declaration type is not yet supported
        Symbol left = SymbolTable.noSym;
        if     ( node instanceof VarDeclType_Plain   ) left = ( ( VarDeclType_Plain   )node ).symbol;
        else if( node instanceof VarDeclType_Static  ) left = ( ( VarDeclType_Static  )node ).symbol;
        else if( node instanceof FormParamType_Plain ) left = ( ( FormParamType_Plain )node ).symbol;
        // ...
        else
        {
            report_fatal( curr, "Variable/field/<formal parameter> declaration type not yet supported" );
        }

        SymbolType leftType = left._type();
        SymbolType varType = ( !isArray ) ? leftType : SymbolType.newArray( String.format( "@Array<%s>", leftType._name() ), leftType );

        // update the current symbol
        // +   if the symbol is a formal parameter, use the symbol count stack to find its index
        int varIdx = SymbolTable._localsSize();
        int varLevel = SymbolTable._localsLevel();
        
        switch( left._value() )
        {
            case Symbol.VAR:          { curr.symbol = Symbol.newVar        ( varName, varType, Symbol.NO_VALUE, varLevel ); break; }
            case Symbol.FIELD:        { curr.symbol = Symbol.newField      ( varName, varType, Symbol.NO_VALUE, varIdx   ); break; }
            case Symbol.STATIC_FIELD: { curr.symbol = Symbol.newStaticField( varName, varType, Symbol.NO_VALUE, varIdx   ); break; }
            case Symbol.FORMAL_PARAM: { curr.symbol = Symbol.newFormalParam( varName, varType, varIdx,          varLevel ); break; }
        }
        
        // if the variable/field cannot be added to the symbol table
        if( !SymbolTable.addSymbol( curr.symbol ) )
        {
            report_verbose( curr, "A symbol with the same name already exists" );
            
            // add a dummy symbol to the symbol table, used for semantic checking later on
            curr.symbol = curr.symbol.clone( String.format( "@Var[%d]", varIdx ) );
            SymbolTable.addSymbol( curr.symbol );
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

        // if the constant is declared outside a constant declaration scope
        SyntaxNode node = context.syntaxNodeStack.top();
        if( !( node instanceof ConstDeclType ) )
        {
            report_fatal( curr, "Constant cannot be declared outside of a constant declaration" );
        }

        // if the current constant declaration is not yet supported
        Symbol left = SymbolTable.noSym;
        if( node instanceof ConstDeclType_Plain ) left = ( ( ConstDeclType_Plain )node ).symbol;
        // ...
        else
        {
            report_fatal( curr, "Constant declaration type not yet supported" );
        }

        Symbol right = curr.getLiteral().symbol;
        if( right.isNoSym() ) return;
        
        // if the literal type is incompatible with the constant declaration type
        if( !SymbolType.isAssignableFrom( left._type(), right._type() ) )
        {
            report_verbose( curr.getLiteral(), "Literal type is incompatible with the declared constant type" );
            return;
        }

        // if the constant cannot be added to the symbol table
        curr.symbol = Symbol.newConst( curr.getIdentName(), left._type(), right._value() );
        if( !SymbolTable.addSymbol( curr.symbol ) )
        {
            report_verbose( curr, "A symbol with the same name already exists" );
            return;
        }
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
        context.syntaxNodeStack.remove();
    }
    // Statement ::= (Statement_Switch     ) SwitchScope lparen Expr rparen lbrace CaseList rbrace;
    @Override
    public void visit( Statement_Switch curr )
    {
        context.syntaxNodeStack.remove();

        Symbol left = curr.getExpr().symbol;
        if( left.isNoSym() ) return;

        // if the switch expresssion does not result in an int
        if( !left._type().isInt() )
        {
            report_verbose( curr.getExpr(), "Switch expression must result in an int" );
            return;
        }
    }
    // Statement ::= (Statement_Break      ) BREAK_K       semicol;
    @Override
    public void visit( Statement_Break curr )
    {
        // find the surrounding do-while or switch statement
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof Statement_DoWhile
                   || elem instanceof Statement_Switch )
        );

        // if the break is not in a do-while or switch statement
        if( scope == null )
        {
            report_verbose( curr, "Break has no effect here (it must be inside a do-while or switch statement)" );
            return;
        }
    }
    // Statement ::= (Statement_Continue   ) CONTINUE_K    semicol;
    @Override
    public void visit( Statement_Continue curr )
    {
        // find the surrounding do-while statement
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof Statement_DoWhile )
        );

        // if the continue is not in a do-while statement
        if( scope == null )
        {
            report_verbose( curr, "Continue has no effect here (it must be inside a do-while statement)" );
            return;
        }
    }
    // Statement ::= (Statement_Return     ) RETURN_K      semicol;
    @Override
    public void visit( Statement_Return curr )
    {
        visit_Statement_Return( curr, null );
    }
    // Statement ::= (Statement_ReturnExpr ) RETURN_K Expr semicol;
    @Override
    public void visit( Statement_ReturnExpr curr )
    {
        visit_Statement_Return( curr, curr.getExpr() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Statement_Return( Statement curr, Expr expr )
    {
        // find the surrounding method declaration
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof MethodDeclType )
        );

        // if the return is not in a method declaration
        if( scope == null )
        {
            report_verbose( curr, "Return has no effect here (it must be inside a method declaration)" );
            return;
        }

        Symbol left = SymbolTable.voidSym;
        if( scope instanceof MethodDeclType_Plain ) left = ( ( MethodDeclType_Plain )scope ).symbol;
        // if the method type is not yet supported
        else
        {
            report_fatal( curr, "Method type not yet supported" );
        }

        SymbolType leftType = left._type();
        SymbolType rightType = ( expr != null ) ? expr.symbol._type() : SymbolTable.voidType;

        // if the return expression is not equivalent to the method return type 
        if( !SymbolType.isEquivalent( leftType, rightType ) )
        {
            report_verbose( curr, "Return expression is not equivalent to the method return type" );
            return;
        }
    }
    // Statement ::= (Statement_Read       ) READ_K lparen Designator rparen semicol;
    @Override
    public void visit( Statement_Read curr )
    {
        Symbol left = curr.getDesignator().symbol;

        if( left.isNoSym() ) return;

        // if the designator is not an lvalue
        if( !left.isLvalue() )
        {
            report_verbose( curr.getDesignator(), "This designator is not an lvalue and cannot be incremented/decremented" );
            return;
        }

        // if the designator is not a primitive type
        if( !left._type().isPrimitiveType() )
        {
            report_verbose( curr.getDesignator(), "This designator must be a primitive type in order to be read" );
            return;
        }
    }
    // Statement ::= (Statement_Print      ) PRINT_K lparen Expr                        rparen semicol;
    @Override
    public void visit( Statement_Print curr )
    {
        visit_Statement_Print( curr.getExpr() );
    }
    // Statement ::= (Statement_PrintFormat) PRINT_K lparen Expr comma int_lit:MinWidth rparen semicol;
    @Override
    public void visit( Statement_PrintFormat curr )
    {
        visit_Statement_Print( curr.getExpr() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Statement_Print( Expr curr )
    {
        Symbol left = curr.symbol;

        if( left.isNoSym() ) return;

        // if the expression is not a primitive type
        if( !left._type().isPrimitiveType() )
        {
            report_verbose( curr, "This expression must be a primitive type" );
            return;
        }
    }
    // Statement ::= (Statement_Scope      ) lbrace StatementList rbrace;
    // Statement ::= (Statement_Semicolon  ) semicol;
    // Statement ::= (Statement_Err        ) error {: parser.report_error( "Bad statement", null ); :};

    ////// action symbols for opening a new scope
    // DoWhileScope ::= (DoWhileScope_Plain) DO_K;
    @Override
    public void visit( DoWhileScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr.getParent() );
    }
    // SwitchScope ::= (SwitchScope_Plain) SWITCH_K;
    @Override
    public void visit( SwitchScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr.getParent() );
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
        Symbol left = curr.getDesignator().symbol;
        Symbol center = curr.getAssignop().symbol;
        Symbol right = curr.getExpr().symbol;

        if( left.isNoSym() ) return;

        // if the designator is not assignable
        if( !left.isLvalue() )
        {
            report_verbose( curr.getDesignator(), "This designator is not assignable to" );
            return;
        }

        if( right.isNoSym() ) return;

        // if the designator is not compatible with the expression
        if( !SymbolType.isAssignableFrom( left._type(), right._type() ) )
        {
            // FIXME: uncomment this line once the reporting bug has been fixed
         // report_verbose( curr.getAssignop(), "This expression is not assignable to" );
            report_verbose( curr, "This expression is not assignable to" );
            return;
        }

        // if the assignment operator is not =
        if( center._value() != TokenCode.assign )
        {
            // FIXME: uncomment this line once the reporting bug has been fixed
         // report_fatal( curr.getAssignop(), "Assignment operator not yet supported" );
            report_fatal( curr, "Assignment operator not yet supported" );
        }
    }
    // DesignatorStatement ::= (DesignatorStatement_Call      ) Designator lparen ActPars rparen;
    // DesignatorStatement ::= (DesignatorStatement_Plusplus  ) Designator plusplus;
    @Override
    public void visit( DesignatorStatement_Plusplus curr )
    {
        visit_DesignatorStatement_IncrementOrDecrement( curr.getDesignator() );
    }
    // DesignatorStatement ::= (DesignatorStatement_Minusminus) Designator minusminus;
    @Override
    public void visit( DesignatorStatement_Minusminus curr )
    {
        visit_DesignatorStatement_IncrementOrDecrement( curr.getDesignator() );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_DesignatorStatement_IncrementOrDecrement( Designator curr )
    {
        Symbol left = curr.symbol;

        if( left.isNoSym() ) return;

        // if the designator is not an lvalue
        if( !left.isLvalue() )
        {
            report_verbose( curr, "This designator is not an lvalue and cannot be incremented/decremented" );
            return;
        }

        // if the designator is not an int
        if( !left._type().isInt() )
        {
            report_verbose( curr, "This designator must be of type int to be incremented/decremented" );
            return;
        }
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
        // if the case number already exists
        if( !context.lastSwitchNumbers.add( curr.getCaseNum() ) )
        {
            report_verbose( curr, "Case with the same number already exists" );
            return;
        }
    }



    ////// <epsilon>
    ////// expr
    ////// expr, expr, expr
    // ActPars ::= (ActPars_Plain) ActParsScope ActParsList;
    @Override
    public void visit( ActPars_Plain curr )
    {
        SymbolTable.closeScope();
    }
    // ActPars ::= (ActPars_Empty) ActParsScope;
    @Override
    public void visit( ActPars_Empty curr )
    {
        visit_ActParam( curr, null );
        SymbolTable.closeScope();
    }

    ////// action symbol for opening a new scope
    // ActParsScope ::= (ActParsScope_Plain) ;
    @Override
    public void visit( ActParsScope_Plain curr )
    {
        // open a temporary scope for the activation parameters
        SymbolTable.openScope();

        // get the last designator
        Designator lastDesignator = context.lastDesignator.get();
        if( lastDesignator == null ) return;

        // if an error has been reported somewhere in the designator
        Symbol design = lastDesignator.symbol;
        if( design.isNoSym() )
        {
            lastDesignator = null;
            return;
        }
        // if the designator is not a function or class member method
        if( !design.isFunction() && !design.isMethod() )
        {
            report_verbose( lastDesignator, "Expected function or class member method" );
            lastDesignator = null;
            return;
        }

        // if the designator is a method, add the <this> activation parameter as its -1'st parameter
        if( design.isMethod() )
        {
            // add the <this> as the method call's -1'st activation parameter
            // +   the previous designator's type must have been <this>'s class type
            SymbolType classType = ( ( Designator )lastDesignator.getParent() ).symbol._type();
            Symbol thisParam = Symbol.newActivParam( "this", classType, -1 );
            SymbolTable.addSymbol( thisParam );
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
        // the current activation parameter's index
        int actParamIdx = SymbolTable._localsSize();
        ActParam actParamNode = null;

        // if this node is an <activation parameter>, reset its symbol
        if( curr instanceof ActParam )
        {
            actParamNode = ( ActParam_Plain )curr;
            actParamNode.symbol = null;
        }

        // get the last designator
        Designator lastDesignator = context.lastDesignator.get();
        if( lastDesignator == null ) return;

        // if the designator is a method (not a function), increase the activation parameter index (this parameter starts at the index -1)
        Symbol design = lastDesignator.symbol;
        if( design.isMethod() ) actParamIdx++;

        // if the actual parameter list is empty (currExpression is null)
        List<Symbol> formParams = design._params()._sorted();
        if( currExpression == null )
        {
            // but the function has formal parameters
            if( !formParams.isEmpty() ) report_basic( lastDesignator, "Less parameters given than expected in function call" );
            
            lastDesignator = null;
            return;
        }
        // if more activation parameters are given than expected in the function declaration
        if( actParamIdx >= formParams.size() )
        {
            report_basic( currExpression, "More parameters given than expected in function call" );
            lastDesignator = null;
            return;
        }
        // if less activation parameters are given than expected in the function declaration
        SyntaxNode next = ( curr instanceof ActParsList ) ? curr.getParent() : null;
        if( next == null && actParamIdx != formParams.size()-1 )
        {
            report_basic( lastDesignator.getParent(), "Less parameters given than expected in function call" );
            lastDesignator = null;
            // IMPORTANT: don't return here, because the current parameter hasn't yet been checked
         // return;
        }

        // if the activation parameter is valid but incompatible with the formal parameter in the function call
        Symbol actParam = currExpression.symbol;
        Symbol formParam = formParams.get( actParamIdx );
        if( !actParam.isNoSym() && !SymbolType.isAssignableFrom( formParam._type(), actParam._type() ) )
        {
            report_verbose( currExpression, "This expression is incompatible with the function's formal parameter" );
            // IMPORTANT: don't return here, because the next parameter's index hasn't yet been set (by adding the activation parameter to the symbol table)
         // return;
        }

        // add an activation parameter to the symbol table
        // +   only used for setting the next activation parameter's index
        actParamNode.symbol = Symbol.newActivParam( String.format( "@Param[%d]", actParamIdx ), actParam._type(), actParamIdx );
        SymbolTable.addSymbol( actParamNode.symbol );
    }



    ////// expr   or   expr < expr and expr >= expr  or  expr != expr   // 'and' has greater priority than 'or'!
    // Condition ::= (Condition_Term)              CondTerm;
    @Override
    public void visit( Condition_Term curr )
    {
        curr.symbol = curr.getCondTerm().symbol;
    }
    // Condition ::= (Condition_Or) Condition or CondTerm;
    @Override
    public void visit( Condition_Or curr )
    {
        curr.symbol = curr.getCondTerm().symbol;
    }

    ////// expr < expr and expr >= expr
    // CondTerm ::= (CondTerm_Fact)              CondFact;
    @Override
    public void visit( CondTerm_Fact curr )
    {
        curr.symbol = curr.getCondFact().symbol;
    }
    // CondTerm ::= (CondTerm_And) CondTerm and CondFact;
    @Override
    public void visit( CondTerm_And curr )
    {
        curr.symbol = curr.getCondFact().symbol;
    }

    ////// expr < expr and expr >= expr
    // CondFact ::= (CondFact_Expr) Expr;
    @Override
    public void visit( CondFact_Expr curr )
    {
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getExpr().symbol;

        // if the left symbol is defined
        if( !left.isNoSym() )
        {
            curr.symbol = Symbol.newVar(
                "@CondFact_Tail",
                SymbolTable.boolType,
                Symbol.NO_VALUE,
                SymbolTable._localsLevel()
            );
        }

        // if the symbol's type is not a bool
        if( !left._type().isBool() )
        {
            report_verbose( curr, "This expression must result in a bool" );
            return;
        }
    }
    // CondFact ::= (CondFact_Relop) Expr Relop Expr;
    @Override
    public void visit( CondFact_Relop curr )
    {
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getExpr().symbol;
        Symbol right = curr.getExpr1().symbol;
        int relop = curr.getRelop().symbol._value();

        // if any of the symbols is defined
        if( !left.isNoSym() || !right.isNoSym() )
        {
            curr.symbol = Symbol.newVar(
                "@CondFact_Tail",
                SymbolTable.boolType,
                Symbol.NO_VALUE,
                SymbolTable._localsLevel()
            );
        }

        // if the symbols are not compatible
        if( !SymbolType.isCompatibleWith( left._type(), right._type() ) )
        {
            // FIXME: uncomment this line once the reporting bug has been fixed
         // report_verbose( curr.getRelop(), "The left and right side of the condition do not result in compatible types" );
            report_verbose( curr, "The left and right side of the condition do not result in compatible types" );
            return;
        }

        // if the symbols are references and the relational operator is not an (in)equality comparison ( == or != )
        if( left._type().isReferenceType() && !TokenCode.isEqualityComparison( relop ) )
        {
            // FIXME: uncomment this line once the reporting bug has been fixed
         // report_verbose( curr.getRelop(), "Cannot compare references" );
            report_verbose( curr, "Cannot compare references" );
            return;
        }
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
            report_verbose( curr.getTerm(), "This signed expression must result in an int" );
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
                report_verbose( curr.getAddition(), "The left side of the addition is not an int" );
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
                report_verbose( curr.getTerm(), "The right side of the addition is not an int" );
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
                report_verbose( curr.getTerm(), "The left side of the multiplication is not an int" );
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
                report_verbose( curr.getFactor(), "The right side of the multiplication is not an int" );
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
            report_verbose( curr, "This expression doesn't result in a value" );
            return;
        }

        curr.symbol = left;
    }
    // Factor ::= (Factor_DesignatorCall) Designator lparen ActPars rparen;
    @Override
    public void visit( Factor_DesignatorCall curr )
    {
        curr.symbol = curr.getDesignator().symbol;
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
            report_basic( curr.getType(), "Cannot instantiate a non-class type" );
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
            report_basic( curr.getType(), "Cannot instantiate a non-type" );
            return;
        }

        curr.symbol = Symbol.newVar(
            "@Factor_NewArray",
            SymbolType.newArray( "@Factor_NewArray", left._type() ),
            Symbol.NO_VALUE,
            SymbolTable._localsLevel()
        );


        if( right.isNoSym() ) return;

        // if the expression does not result in an int
        if( !right._type().isInt() )
        {
            report_verbose( curr.getExpr(), "This expression must result in an int" );
            return;
        }
    }
    // Factor ::= (Factor_Expr          ) lparen Expr rparen;
    @Override
    public void visit( Factor_Expr curr )
    {
        curr.symbol = curr.getExpr().symbol;
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
        // save the last encountered designator
        context.lastDesignator.set( curr );
        // reset the current designator's symbol
        curr.symbol = SymbolTable.noSym;

        // if the current designator is not <this>
        if( !"this".equals( curr.getName() ) )
        {
            // try to find the symbol in the symbol table
            curr.symbol = SymbolTable.findSymbol( curr.getName() );
    
            // if the symbol does not exist in the symbol table
            if( curr.symbol.isNoSym() )
            {
                report_basic( curr, "This symbol has not been declared" );
                return;
            }
        }
        else
        {
            // find the class declaration surrounding <this>
            SyntaxNode scope = context.syntaxNodeStack.find(
                elem -> ( elem instanceof ClassDeclType )
            );

            // if the <this> designator is not in a class declaration
            if( scope == null )
            {
                report_verbose( curr, "'this' has no effect here (it must be inside a class declaration)" );
                return;
            }
    
            // try to find the symbol in the symbol table
            curr.symbol = SymbolTable.findSymbol( curr.getName() );

            // if the <this> designator is not in a class declaration
            if( curr.symbol.isNoSym() )
            {
                report_fatal( curr, "Cannot find 'this' symbol in class scope" );
                return;
            }
        }
    }
    // Designator ::= (Designator_Null   ) NULL_K;
    @Override
    public void visit( Designator_Null curr )
    {
        // save the last encountered designator
        context.lastDesignator.set( curr );
        // reset the current designator's symbol
        curr.symbol = SymbolTable.nullSym;
    }
    // Designator ::= (Designator_Field  ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Field curr )
    {
        // save the last encountered designator
        context.lastDesignator.set( curr );
        // reset the current designator's symbol
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;

        // if the previous designator segment does not exist, an error must have been reported somewhere in the previous segments, return
        if( left.isNoSym() ) return;

        // if the previous symbol is not a class (doesn't have inner methods)
        if( !left._type().isClass() )
        {
            report_basic( curr, "Expected class member, but the left designator is not a class" );
            return;
        }

        // find the symbol with the given name in the previous designator's type's members
        Symbol member = left._type()._members().findSymbol( curr.getName() );

        // if the previous symbol doesn't contain the current field/member
        if( member.isNoSym() )
        {
            report_basic( curr, "The specified class does not contain this member" );
            return;
        }
        // if the previous designator is a type (static access) and its non-static member is accessed
        if( left.isType() && !member.isStaticField() )
        {
            report_basic( curr, "This non-static class member cannot be accessed in a static way" );
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
        context.lastDesignator.set( curr );
        // set the current designator to the default value
        curr.symbol = SymbolTable.noSym;

        Symbol left = curr.getDesignator().symbol;
        Symbol right = curr.getExpr().symbol;

        // if the previous designator segment does not exist, an error must have been reported somewhere in the previous segments, return
        if( left.isNoSym() ) return;

        // if the previous symbol is not an array
        if( !left._type().isArray() )
        {
            report_basic( curr, "The left side of the brackets is not an array" );
            return;
        }

        // find the symbol with the given name in the previous designator's type's members
        Symbol member = left._type()._members().findSymbol( "@elem" );

        // if the expression inside the angle brackets has an error, an error must have already been reported for the expression, return
        if( right.isNoSym() ) return;

        // if the expression inside the angle brackets does not result in an int
        if( !right._type().isInt() )
        {
            report_basic( curr, "This expression must result in an int" );
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
        Symbol symbol = SymbolTable.findSymbol( typeName );
        if     ( curr instanceof Type_Ident       ) ( ( Type_Ident       )curr ).symbol = symbol;
        else if( curr instanceof ReturnType_Ident ) ( ( ReturnType_Ident )curr ).symbol = symbol;
        else                                        report_fatal( curr, "Type's context not yet supported" );

        // if the symbol is missing from the symbol table
        if( symbol.isNoSym() )
        {
            report_basic( curr, "Expected type here, but this symbol has not been declared" );
            return;
        }
        // if the symbol is not a type
        if( !symbol.isType() )
        {
            report_basic( curr, "Expected type here, but this isn't a type" );
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

}
