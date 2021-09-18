package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.ast.*;


public class ScopeVisitor implements Visitor
{
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;

    public int getTokenFromIdx() { return ( min != Integer.MIN_VALUE ) ? min     : CompilerError.NO_INDEX; }
    public int getTokenToIdx()   { return ( max != Integer.MIN_VALUE ) ? max + 1 : CompilerError.NO_INDEX; }

    private void updateScope( SyntaxNode node )
    {
        int line = node.getLine();   // tokenFromIdx for current token
        if( min > line ) min = line;
        if( max < line ) max = line;
    }


    public void visit( ReturnType                     node ) { updateScope( node ); }
    public void visit( Mulop                          node ) { updateScope( node ); }
    public void visit( FormParsScope                  node ) { updateScope( node ); }
    public void visit( MethodDecl                     node ) { updateScope( node ); }
    public void visit( ConstDeclType                  node ) { updateScope( node ); }
    public void visit( VarIdent                       node ) { updateScope( node ); }
    public void visit( Literal                        node ) { updateScope( node ); }
    public void visit( Relop                          node ) { updateScope( node ); }
    public void visit( ActParsScope                   node ) { updateScope( node ); }
    public void visit( Assignop                       node ) { updateScope( node ); }
    public void visit( ActParam                       node ) { updateScope( node ); }
    public void visit( SwitchScope                    node ) { updateScope( node ); }
    public void visit( MethodCall                     node ) { updateScope( node ); }
    public void visit( VarDeclType                    node ) { updateScope( node ); }
    public void visit( StatementList                  node ) { updateScope( node ); }
    public void visit( Addop                          node ) { updateScope( node ); }
    public void visit( Addition                       node ) { updateScope( node ); }
    public void visit( FormParamType                  node ) { updateScope( node ); }
    public void visit( Factor                         node ) { updateScope( node ); }
    public void visit( ProgramType                    node ) { updateScope( node ); }
    public void visit( CondTerm                       node ) { updateScope( node ); }
    public void visit( ClassDeclType                  node ) { updateScope( node ); }
    public void visit( Designator                     node ) { updateScope( node ); }
    public void visit( Term                           node ) { updateScope( node ); }
    public void visit( FormParsList                   node ) { updateScope( node ); }
    public void visit( Condition                      node ) { updateScope( node ); }
    public void visit( MethodDeclType                 node ) { updateScope( node ); }
    public void visit( CaseList                       node ) { updateScope( node ); }
    public void visit( ActParsList                    node ) { updateScope( node ); }
    public void visit( DoWhileScope                   node ) { updateScope( node ); }
    public void visit( MethodDeclCode                 node ) { updateScope( node ); }
    public void visit( GlobalDeclList                 node ) { updateScope( node ); }
    public void visit( VarDeclList                    node ) { updateScope( node ); }
    public void visit( Expr                           node ) { updateScope( node ); }
    public void visit( Case                           node ) { updateScope( node ); }
    public void visit( ActPars                        node ) { updateScope( node ); }
    public void visit( DesignatorStatement            node ) { updateScope( node ); }
    public void visit( Statement                      node ) { updateScope( node ); }
    public void visit( ConstInitList                  node ) { updateScope( node ); }
    public void visit( ClassDeclBody                  node ) { updateScope( node ); }
    public void visit( VarDecl                        node ) { updateScope( node ); }
    public void visit( ConstInit                      node ) { updateScope( node ); }
    public void visit( Type                           node ) { updateScope( node ); }
    public void visit( VarIdentList                   node ) { updateScope( node ); }
    public void visit( ClassDecl                      node ) { updateScope( node ); }
    public void visit( ConstDecl                      node ) { updateScope( node ); }
    public void visit( CondFact                       node ) { updateScope( node ); }
    public void visit( MethodDeclList                 node ) { updateScope( node ); }
    public void visit( Program                        node ) { updateScope( node ); }
    public void visit( MethodDeclBody                 node ) { updateScope( node ); }
    public void visit( GlobalDecl                     node ) { updateScope( node ); }
    public void visit( FormPars                       node ) { updateScope( node ); }
    public void visit( FormParam                      node ) { updateScope( node ); }
    public void visit( Mulop_Perc                     node ) { updateScope( node ); }
    public void visit( Mulop_Div                      node ) { updateScope( node ); }
    public void visit( Mulop_Mul                      node ) { updateScope( node ); }
    public void visit( Addop_Minus                    node ) { updateScope( node ); }
    public void visit( Addop_Plus                     node ) { updateScope( node ); }
    public void visit( Relop_Le                       node ) { updateScope( node ); }
    public void visit( Relop_Lt                       node ) { updateScope( node ); }
    public void visit( Relop_Ge                       node ) { updateScope( node ); }
    public void visit( Relop_Gt                       node ) { updateScope( node ); }
    public void visit( Relop_Ne                       node ) { updateScope( node ); }
    public void visit( Relop_Eq                       node ) { updateScope( node ); }
    public void visit( Assignop_Assign                node ) { updateScope( node ); }
    public void visit( Literal_Bool                   node ) { updateScope( node ); }
    public void visit( Literal_Char                   node ) { updateScope( node ); }
    public void visit( Literal_Int                    node ) { updateScope( node ); }
    public void visit( Type_Ident                     node ) { updateScope( node ); }
    public void visit( ReturnType_Ident               node ) { updateScope( node ); }
    public void visit( ReturnType_Void                node ) { updateScope( node ); }
    public void visit( Designator_ArrElem             node ) { updateScope( node ); }
    public void visit( Designator_Field               node ) { updateScope( node ); }
    public void visit( Designator_Null                node ) { updateScope( node ); }
    public void visit( Designator_Ident               node ) { updateScope( node ); }
    public void visit( MethodCallScope_Plain          node ) { updateScope( node ); }
    public void visit( Factor_Expr                    node ) { updateScope( node ); }
    public void visit( Factor_NewArray                node ) { updateScope( node ); }
    public void visit( Factor_NewVar                  node ) { updateScope( node ); }
    public void visit( Factor_Literal                 node ) { updateScope( node ); }
    public void visit( Factor_MethodCall              node ) { updateScope( node ); }
    public void visit( Factor_Designator              node ) { updateScope( node ); }
    public void visit( Term_Tail                      node ) { updateScope( node ); }
    public void visit( Term_Factor                    node ) { updateScope( node ); }
    public void visit( Addition_Tail                  node ) { updateScope( node ); }
    public void visit( Addition_STerm                 node ) { updateScope( node ); }
    public void visit( Addition_Term                  node ) { updateScope( node ); }
    public void visit( Expr_Err                       node ) { updateScope( node ); }
    public void visit( Expr_Addition                  node ) { updateScope( node ); }
    public void visit( CondFact_Relop                 node ) { updateScope( node ); }
    public void visit( CondFact_Expr                  node ) { updateScope( node ); }
    public void visit( CondTerm_And                   node ) { updateScope( node ); }
    public void visit( CondTerm_Fact                  node ) { updateScope( node ); }
    public void visit( Condition_Or                   node ) { updateScope( node ); }
    public void visit( Condition_Term                 node ) { updateScope( node ); }
    public void visit( ActParam_Plain                 node ) { updateScope( node ); }
    public void visit( ActParsList_Tail               node ) { updateScope( node ); }
    public void visit( ActParsList_Expr               node ) { updateScope( node ); }
    public void visit( ActParsScope_Plain             node ) { updateScope( node ); }
    public void visit( ActPars_Empty                  node ) { updateScope( node ); }
    public void visit( ActPars_Plain                  node ) { updateScope( node ); }
    public void visit( Case_Plain                     node ) { updateScope( node ); }
    public void visit( CaseList_Empty                 node ) { updateScope( node ); }
    public void visit( CaseList_Tail                  node ) { updateScope( node ); }
    public void visit( StatementList_Empty            node ) { updateScope( node ); }
    public void visit( StatementList_Tail             node ) { updateScope( node ); }
    public void visit( DesignatorStatement_Minusminus node ) { updateScope( node ); }
    public void visit( DesignatorStatement_Plusplus   node ) { updateScope( node ); }
    public void visit( DesignatorStatement_Call       node ) { updateScope( node ); }
    public void visit( DesignatorStatement_Assign     node ) { updateScope( node ); }
    public void visit( SwitchScope_Plain              node ) { updateScope( node ); }
    public void visit( DoWhileScope_Plain             node ) { updateScope( node ); }
    public void visit( Statement_Err                  node ) { updateScope( node ); }
    public void visit( Statement_Semicolon            node ) { updateScope( node ); }
    public void visit( Statement_Scope                node ) { updateScope( node ); }
    public void visit( Statement_PrintFormat          node ) { updateScope( node ); }
    public void visit( Statement_Print                node ) { updateScope( node ); }
    public void visit( Statement_Read                 node ) { updateScope( node ); }
    public void visit( Statement_ReturnExpr           node ) { updateScope( node ); }
    public void visit( Statement_Return               node ) { updateScope( node ); }
    public void visit( Statement_Continue             node ) { updateScope( node ); }
    public void visit( Statement_Break                node ) { updateScope( node ); }
    public void visit( Statement_Switch               node ) { updateScope( node ); }
    public void visit( Statement_DoWhile              node ) { updateScope( node ); }
    public void visit( Statement_IfElse               node ) { updateScope( node ); }
    public void visit( Statement_If                   node ) { updateScope( node ); }
    public void visit( Statement_Designator           node ) { updateScope( node ); }
    public void visit( ConstInit_Err                  node ) { updateScope( node ); }
    public void visit( ConstInit_Plain                node ) { updateScope( node ); }
    public void visit( ConstInitList_Tail             node ) { updateScope( node ); }
    public void visit( ConstInitList_Init             node ) { updateScope( node ); }
    public void visit( ConstDeclType_Err              node ) { updateScope( node ); }
    public void visit( ConstDeclType_Plain            node ) { updateScope( node ); }
    public void visit( ConstDecl_Plain                node ) { updateScope( node ); }
    public void visit( VarIdent_Err                   node ) { updateScope( node ); }
    public void visit( VarIdent_Array                 node ) { updateScope( node ); }
    public void visit( VarIdent_Ident                 node ) { updateScope( node ); }
    public void visit( VarIdentList_Tail              node ) { updateScope( node ); }
    public void visit( VarIdentList_VarIdent          node ) { updateScope( node ); }
    public void visit( VarDeclType_Err                node ) { updateScope( node ); }
    public void visit( VarDeclType_Static             node ) { updateScope( node ); }
    public void visit( VarDeclType_Plain              node ) { updateScope( node ); }
    public void visit( VarDecl_Plain                  node ) { updateScope( node ); }
    public void visit( VarDeclList_Empty              node ) { updateScope( node ); }
    public void visit( VarDeclList_VarDecl            node ) { updateScope( node ); }
    public void visit( FormParamType_Plain            node ) { updateScope( node ); }
    public void visit( FormParam_Err                  node ) { updateScope( node ); }
    public void visit( FormParam_Plain                node ) { updateScope( node ); }
    public void visit( FormParsList_Tail              node ) { updateScope( node ); }
    public void visit( FormParsList_Init              node ) { updateScope( node ); }
    public void visit( FormParsScope_Plain            node ) { updateScope( node ); }
    public void visit( FormPars_Empty                 node ) { updateScope( node ); }
    public void visit( FormPars_List                  node ) { updateScope( node ); }
    public void visit( MethodDeclCode_Plain           node ) { updateScope( node ); }
    public void visit( MethodDeclBody_Plain           node ) { updateScope( node ); }
    public void visit( MethodDeclType_Plain           node ) { updateScope( node ); }
    public void visit( MethodDecl_Plain               node ) { updateScope( node ); }
    public void visit( MethodDeclList_Empty           node ) { updateScope( node ); }
    public void visit( MethodDeclList_Tail            node ) { updateScope( node ); }
    public void visit( ClassDeclBody_VarsMethods      node ) { updateScope( node ); }
    public void visit( ClassDeclBody_Vars             node ) { updateScope( node ); }
    public void visit( ClassDeclType_Err              node ) { updateScope( node ); }
    public void visit( ClassDeclType_Extends          node ) { updateScope( node ); }
    public void visit( ClassDeclType_Plain            node ) { updateScope( node ); }
    public void visit( ClassDecl_Plain                node ) { updateScope( node ); }
    public void visit( GlobalDecl_Class               node ) { updateScope( node ); }
    public void visit( GlobalDecl_Var                 node ) { updateScope( node ); }
    public void visit( GlobalDecl_Const               node ) { updateScope( node ); }
    public void visit( GlobalDeclList_Empty           node ) { updateScope( node ); }
    public void visit( GlobalDeclList_Tail            node ) { updateScope( node ); }
    public void visit( ProgramType_Plain              node ) { updateScope( node ); }
    public void visit( Program_Plain                  node ) { updateScope( node ); }
}
