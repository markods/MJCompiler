package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4J;
import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.ac.bg.etf.pp1.visitors.CodeGenVisitor;
import rs.ac.bg.etf.pp1.visitors.SemanticVisitor;

public class Compiler
{
    // compiler state
    private State state = new State();

    public class State
    {
        private Log4J logger = Log4J.getLogger( Compiler.class );
        private CompilerErrorList errors = new CompilerErrorList( this );

        private boolean verbose = false;
        // --------------------------------------------
        private File fInput = null;
        private File fLex = null;
        private File fParse = null;
        private File fOutput = null;

        private final int maxFieldsInClass = 65536;
        private final int maxStackFrameSize = 256;

        private BufferedLexer lexer = null;
        // --------------------------------------------
        private Parser parser = null;
        private SymbolTable symbolTable = null;
        private SyntaxNode syntaxRoot = null;
        // --------------------------------------------
        private SemanticVisitor semanticVisitor = null;
        private CodeGen codeGen = null;
        // --------------------------------------------
        private CodeGenVisitor codeGenVisitor = null;


        // reset the compiler parameters
        public void resetParams()
        {
            verbose = false;
            fInput = null;
            fLex = null;
            fParse = null;
            fOutput = null;
        }

        // get the input file name, if it exists
        public String getInputFileName()
        {
            return ( fInput != null ) ? fInput.getName() : "";
        }

        // getters and setters
        public Log4J _logger() { return logger; }
        public CompilerErrorList _errors() { return errors; }

        public boolean _verbose() { return verbose; }

        public int _maxFieldsInClass() { return maxFieldsInClass; }
        public int _maxStackFrameSize() { return maxStackFrameSize; }

        public BufferedLexer _lexer() { return lexer; }
        // --------------------------------------------
        public Parser _parser() { return parser; }
        public SymbolTable _symbolTable() { return symbolTable; }
        public SyntaxNode _syntaxRoot() { return syntaxRoot; }
        // --------------------------------------------
        public SemanticVisitor _semanticVisitor() { return semanticVisitor; }
        public CodeGen _codeGen() { return codeGen; }
        // --------------------------------------------
        public CodeGenVisitor _codeGenVisitor() { return codeGenVisitor; }
    }

    // private constructor
    private Compiler() {}





    // compile [-lex file] [-par file] [-o file] file
    public static void main( String[] args )
    {
        Compiler compiler = new Compiler();

        if( !compiler.compile( args ) )
        {
            System.err.println( compiler.state.errors.toString() );
            System.exit( -1 );
        }
    }

    // compile the given source program
    // +   produce the lexer and parser intermediary files if requested
    public boolean compile( String[] args )
    {
        // if there are argument errors, skip compilation
        if( !setParams( args ) ) return false;
        
        // if lexer output file is specified
        if( state.fLex != null || state.verbose )
        {
            // lex the input file
            // +   if the lexer could not lex the input file, return
            if( !lex() ) return false;
        }

        // if parse output file or the object file is specified
        if( state.fParse != null || state.fOutput != null )
        {
            // parse the input file and create the syntax tree
            // +   if the syntax tree could not be created, return
            if( !parse() ) return false;

            // if compiler output is requested, write the parse results to the parse output file
            if( state.fOutput != null )
            {
                // do the semantic pass
                // +   if there is a semantic problem in the syntax tree, return
                if( !semanticAnalyse() ) return false;

                // if the code generation failed, return
                if( !generateCode() ) return false;
            }
        }
        
        // return if there were no compilation errors
        return state.errors.noErrors();
    }


    
    // lex the input file
    // +   write the results to the given lex file
    // +   only used to show the lexer results, the parser already does lexing on its own using the same lexer class
    private boolean lex()
    {
        // if the input file is missing, return
        if( state.fInput == null ) return false;

        state.logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ LEXER" );
        state.logger.info( "Lexing input file:" );

        StringBuilder output = new StringBuilder( "=========================LEXER OUTPUT==========================\n" );


        // read file and lex it
        // +   buffered reader constructor can throw an exception and not close the file reader!
        try( FileReader frInput = new FileReader( state.fInput );
             BufferedReader brInput = new BufferedReader( frInput );
        )
        {
            // if the lexer hasn't already been created, create it
            if( state.lexer == null ) { state.lexer = new BufferedLexer( brInput ); }
            Token token = null;

            // lex the input .mj file
            try
            {
                while( true )
                {
                    token = ( Token )( state.lexer.next_token() );

                    output.append( token.toString() ).append( "\n" );
                    state.logger.log( Log4J.INFO, token.toString(), true );

                    if( token.isEOF() ) break;
                    
                    // // if the token is invalid
                    // if( token.isInvalid() )
                    // {
                    //     // append an error to the output file
                    //     CompilerError error = new CompilerError( CompilerError.LEXICAL_ERROR, "Invalid token", token.getIdx(), token.getIdx()+1 );
                    //     output.append( error.toString() ).append( "\n" );
                    // }
                }
            }
            catch( IOException ex )
            {
                state.errors.add( CompilerError.LEXICAL_ERROR, "Error lexing input file", ex );
                return false;
            }
        }
        catch( IOException ex )
        {
            state.errors.add( CompilerError.LEXICAL_ERROR, "Cannot open input file", ex );
            return false;
        }


        // if the lexer output is requested, write the lexer results to the lexer output file
        if( state.fLex != null )
        {
            // write lex results to output lex file
            // +   buffered writer constructor can throw an exception and not close the file writer!
            try( FileWriter fwLex = new FileWriter( state.fLex );
                 BufferedWriter bwLex = new BufferedWriter( fwLex );
            )
            {
                bwLex.append( output );
            }
            catch( IOException ex )
            {
                state.errors.add( CompilerError.LEXICAL_ERROR, "Cannot open/write to output lex file", ex );
                return false;
            }
        }

        // return if the lexer finished successfully
        return state.errors.noErrorsSinceLastCheck();
    }

    // parse the input file
    // +   set the root 
    private boolean parse()
    {
        // if the input file is missing, return
        if( state.fInput == null ) return false;

        state.logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ PARSER" );
        state.logger.info( "Parsing input file:" );
        state.logger.info( "=========================PARSER OUTPUT==========================" );

        
        // read file and parse it
        // +   buffered reader constructor can throw an exception and not close the file reader!
        try( FileReader frInput = new FileReader( state.fInput );
             BufferedReader brInput = new BufferedReader( frInput );
        )
        {
            // if the symbol table hasn't already been created, create it
            if( state.symbolTable == null ) { state.symbolTable = new SymbolTable(); }
            // if the lexer hasn't already been created, create it
            if( state.lexer == null ) { state.lexer = new BufferedLexer( brInput ); }
            // if the parser hasn't already been created, create it
            if( state.parser == null ) { state.parser = new Parser( state.lexer ); state.parser.finishConstruction( state ); }

            try
            {
                // parse the input file
                java_cup.runtime.Symbol rootSymbol = null;
                
                if( !state.verbose )
                {
                    rootSymbol = state.parser.parse();
                }
                else
                {
                    try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                         SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDERR, buffer );
                    )
                    {
                        // workaround since debug parse method only outputs to System.out
                        rootSymbol = state.parser.debug_parse();
                        state.logger.info( "" );
                        state.logger.info( "=========================PARSER STATES==========================" );
                        state.logger.log( Log4J.INFO, buffer.toString( "UTF-8" ), true );
                    }
                }

                if( rootSymbol != null && rootSymbol.value instanceof SyntaxNode )
                {
                    state.syntaxRoot = ( SyntaxNode )( rootSymbol.value );
                }
            }
            catch( Exception ex )
            {
                state.errors.add( CompilerError.SYNTAX_ERROR, "Error parsing input file", ex );
                return false;
            }
        }
        catch( IOException ex )
        {
            state.errors.add( CompilerError.LEXICAL_ERROR, "Cannot open input file", ex );
            return false;
        }


        // if parser output is requested, write the parse results to the parse output file
        if( state.fParse != null )
        {
            // write parse results to output parse file
            // +   buffered writer constructor can throw an exception and not close the file writer!
            try( FileWriter fwParse = new FileWriter( state.fParse );
                 BufferedWriter bwParse = new BufferedWriter( fwParse );
            )
            {
                bwParse.write( syntaxTreeToString() );
            }
            catch( IOException ex )
            {
                state.errors.add( CompilerError.SYNTAX_ERROR, "Cannot open/write to output parse file", ex );
            }
        }

        // return if the parser encountered a fatal error
        return state.parser.noFatalErrors();
    }

    // semantic check the the syntax tree
    private boolean semanticAnalyse()
    {
        // if the syntax tree is missing, return
        if( state.syntaxRoot == null ) return false;

        state.logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ SEMANTIC" );
        state.logger.info( "Semantic checking:" );
        
        // if the semantic visitor hasn't already been created, create it
        if( state.semanticVisitor == null ) { state.semanticVisitor = new SemanticVisitor( state ); }
        // if the code generator hasn't already been created, create it
        // NOTE: needed for initializing the jump map
        if( state.codeGen == null ) { state.codeGen = new CodeGen( state ); }

        try
        {
            // do a semantic pass over the abstract syntax tree and fill in the symbol table
            state.syntaxRoot.traverseBottomUp( state.semanticVisitor );
        }
        catch( CompilerError err )
        {}
        finally
        {
            // log the source code, symbol table and the syntax tree
            state.logger.log( Log4J.INFO, sourceCodeToString(), true );
            state.logger.log( Log4J.INFO, symbolTableToString(), true );
            if( state.verbose ) state.logger.log( Log4J.INFO, syntaxTreeToString(), true );
            // reset the scope info list, so that the code generator can recreate it
            // +    this doesn't close the global scope
            state.symbolTable.closeScope();
        }

        // return if the semantic analysis finished successfully
        return state.errors.noErrors();
    }

    // compile the given syntax tree into microjava code
    private boolean generateCode()
    {
        // if the syntax tree or output file is missing, return
        if( state.syntaxRoot == null || state.fOutput == null ) return false;

        state.logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER" );
        state.logger.info( "Compiling code:" );

        // if the code gen visitor hasn't already been created, create it
        if( state.codeGenVisitor == null ) { state.codeGenVisitor = new CodeGenVisitor( state ); }
        // if the code generator hasn't already been created, create it
        if( state.codeGen == null ) { state.codeGen = new CodeGen( state ); }

        // generate code from the abstract syntax tree
        state.syntaxRoot.traverseBottomUp( state.codeGenVisitor );
        byte[] compiledCode = state.codeGen.compile();

        // write compiler results to output file
        try( FileOutputStream fWriter = new FileOutputStream( state.fOutput ); )
        {
            fWriter.write( compiledCode );
        }
        catch( IOException ex )
        {
            state.errors.add( CompilerError.SEMANTIC_ERROR, "Cannot open/write to output file", ex );
            return false;
        }
        catch( CompilerError err )
        {}
        finally
        {
            // log the updated symbol table, source code (again), the decompiled code and the output from the microjava virtual machine
            state.logger.log( Log4J.INFO, symbolTableToString(), true );
            state.logger.log( Log4J.INFO, sourceCodeToString(), true );
            state.logger.log( Log4J.INFO, decompiledCodeToString(), true );
        }

        // return if the code generator finished successfully
        return state.errors.noErrorsSinceLastCheck();
    }
    
    

    // return the source code as a string
    private String sourceCodeToString()
    {
        return "=========================SOURCE CODE============================\n"
            + state.lexer.toString();
    }

    // return the compiler's symbol table as a string
    private String symbolTableToString()
    {
        return state.symbolTable.asString();
    }

    // return the syntax tree as a string
    private String syntaxTreeToString()
    {
        if( state.syntaxRoot == null ) return null;
        String syntaxTree = "=========================SYNTAX TREE===========================\n"
                          + state.syntaxRoot.toString();
        return syntaxTree;
    }

    // return the decompiled code as a string
    private String decompiledCodeToString()
    {
        return "=========================DECOMPILED CODE========================\n"
            + state.codeGen.decompile( state.fOutput );
    }


    // set the compiler parameters
    // +   compile [-verbose] [-lex file] [-par file] [-o file] file
    // +   returns true if everything is ok
    private boolean setParams( String[] params )
    {
        state.logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER PARAMS" );
        state.logger.info( "Setting compiler parameters:" );
        state.logger.info( String.join( " ", params ) );

        // variables for storing file names
        String fnameLex = null;
        String fnameParse = null;
        String fnameOutput = null;
        String fnameInput = null;
        
        // clear errors
        state.errors.clear();
        
        // parse parameters
        for( int i = 0; i < params.length; i++ )
        {
            switch( params[ i ] )
            {
                case "-verbose":
                {
                    state.verbose = true;
                    break;
                }

                case "-lex":
                {
                    if( fnameLex != null )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, "Lexer output file already specified", CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, "Lexer output file not specified after the -lex flag", CompilerError.NO_INDEX, i );
                        break;
                    }

                    fnameLex = params[ ++i ];
                    if( !fnameLex.endsWith( ".lex" ) ) { fnameLex = fnameLex + ".lex"; }
                    
                    state.logger.info( "fnameLex = " + fnameLex );
                    break;
                }

                case "-par":
                {
                    if( fnameParse != null )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, "Parser output file already specified", CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, "Parser output file not specified after the -par flag", CompilerError.NO_INDEX, i );
                        break;
                    }

                    fnameParse = params[ ++i ];
                    if( !fnameParse.endsWith( ".par" ) ) { fnameParse = fnameParse + ".par"; }

                    state.logger.info( "fnameParse = " + fnameParse );
                    break;
                }

                case "-o":
                {
                    if( fnameOutput != null )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, "Output file already specified", CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, "Output file not specified after the -o flag", CompilerError.NO_INDEX, i );
                        break;
                    }
                    
                    fnameOutput = params[ ++i ];
                    if( !fnameOutput.endsWith( ".obj" ) ) { fnameOutput = fnameOutput + ".obj"; }

                    state.logger.info( "fnameOutput = " + fnameOutput );
                    break;
                }

                default:
                {
                    if( params[ i ].charAt( 0 ) == '-' )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, String.format( "Unknown option: '%s'", params[ i ] ), CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( fnameInput != null )
                    {
                        state.errors.add( CompilerError.ARGUMENTS_ERROR, String.format( "Cannot specify another input file: '%s'", params[ i ] ), CompilerError.NO_INDEX, i );
                        break;
                    }
                    
                    fnameInput = params[ i ];
                    if( !fnameInput.endsWith( ".mj" ) ) { fnameInput = fnameInput + ".mj"; }

                    state.logger.info( "fnameInput = " + fnameInput );
                    break;
                }
            }
        }

        // the input file must be specified
        if( fnameInput == null )
        {
            state.errors.add( CompilerError.ARGUMENTS_ERROR, "Input file not specified" );
        }

        // if there are errors, log them and return
        if( !state.errors.noErrorsSinceLastCheck() )
        {
            state.resetParams();
            return false;
        }


        // if the output file is missing and other switches are not specified, set it to be the input file with the .obj extension
        if( fnameOutput == null && fnameLex == null && fnameParse == null )
        {
            fnameOutput = fnameInput.substring( 0, fnameInput.length() - ".mj".length() ) + ".obj";
            state.logger.info( "fnameOutput = " + fnameOutput );
        }

        // open the given files
        if( fnameInput  != null ) state.fInput  = new File( fnameInput  );
        if( fnameLex    != null ) state.fLex    = new File( fnameLex    );
        if( fnameParse  != null ) state.fParse  = new File( fnameParse  );
        if( fnameOutput != null ) state.fOutput = new File( fnameOutput );

        if( state.verbose )
        {
            state.logger.info( "verbose = true" );
        }
        
        // check if the files exist and are readable/writable
        if( state.fInput != null )
        {
            if( !state.fInput.exists() )
            {
                state.errors.add( CompilerError.ARGUMENTS_ERROR, "Input file does not exist" );
            }
            else if( !state.fInput.canRead() )
            {
                state.errors.add( CompilerError.ARGUMENTS_ERROR, "Input file is not readable" );
            }
            
            state.logger.info( "fInput = " + state.fInput.getAbsolutePath() );
        }
        
        if( state.fLex != null )
        {
            if( state.fLex.exists() && !state.fLex.canWrite() )
            {
                state.errors.add( CompilerError.ARGUMENTS_ERROR, "Lexer output file exists and is not writable" );
            }
            
            state.logger.info( "fLex = " + state.fLex.getAbsolutePath() );
        }

        if( state.fParse != null )
        {
            if( state.fParse.exists() && !state.fParse.canWrite() )
            {
                state.errors.add( CompilerError.ARGUMENTS_ERROR, "Parser output file exists and is not writable" );
            }
            
            state.logger.info( "fParse = " + state.fParse.getAbsolutePath() );
        }

        if( state.fOutput != null )
        {
            if( state.fOutput.exists() && !state.fOutput.canWrite() )
            {
                state.errors.add( CompilerError.ARGUMENTS_ERROR, "Output file exists and is not writable" );
            }
            
            state.logger.info( "fOutput = " + state.fOutput.getAbsolutePath() );
        }

        // if there are errors log them and return
        if( !state.errors.noErrorsSinceLastCheck() )
        {
            state.resetParams();
            return false;
        }

        // return that the compiler params are successfully set
        state.logger.info( "Compiler parameters are valid" );
        return true;
    }
}

