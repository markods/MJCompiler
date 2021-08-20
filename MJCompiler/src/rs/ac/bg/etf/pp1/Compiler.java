package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4J;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class Compiler
{
    static
    {
        Log4J.configure();
    }
    
    public static final Log4J logger = Log4J.getLogger( Compiler.class );

    public static final CompilerErrorList errors = new CompilerErrorList();
    public static final SymbolList symbols = new SymbolList();

    private static boolean verbose = false;
    private static File fInput = null;
    private static File fLex = null;
    private static File fParse = null;
    private static File fOutput = null;

    // private constructor
    private Compiler() {}

    // reset the compiler parameters
    private static void resetParams()
    {
        verbose = false;
        fInput = null;
        fLex = null;
        fParse = null;
        fOutput = null;
    }



    // compile [-lex file] [-par file] [-o file] file
    public static void main( String[] args )
    {
        if( !Compiler.compile( args ) )
        {
            System.err.println( Compiler.errors.toString() );
            System.exit( -1 );
        }
    }

    // compile the given source program
    // +   produce the lexer and parser intermediary files if requested
    public static boolean compile( String[] args )
    {
        // if there are argument errors, skip compilation
        if( !Compiler.setParams( args ) ) return false;
        
        // if lexer output file is specified
        if( fLex != null )
        {
            // save the lex results to the output file
            SymbolList symbols = lex( fInput, fLex );
            // if the lexer could not lex the input file, return
            if( symbols == null ) return false;
        }

        // if parse output file or the object file is specified
        if( fParse != null || fOutput != null )
        {
            // parse the input file and create the syntax tree
            SyntaxNode syntaxRoot = parse( fInput, fParse, verbose );
            // if the syntax tree could not be created, return
            if( syntaxRoot == null ) return false;

            // if compiler output is requested, write the parse results to the parse output file
            if( fOutput != null )
            {
                // do the semantic pass
                SemanticVisitor semanticCheck = checkSemantics( syntaxRoot );
                // if there is a semantic problem in the syntax tree, return
                if( semanticCheck == null ) return false;

                // if the code generation failed, return
                if( !generateCode( syntaxRoot, semanticCheck, fOutput ) ) return false;
            }
        }
        
        // return if there were compilation errors
        return !errors.hasErrors();
    }


    
    // lex the input file
    // +   write the results to the given lex file
    // +   only used to show the lexer results, the parser already does lexing on its own using the same lexer class
    private static SymbolList lex( File fInput, File fLex )
    {
        // if the input file is missing, return
        if( fInput == null ) return null;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ LEXER" );
        logger.info( "Lexing input file:" );

        SymbolList symbols = new SymbolList();
        StringBuilder output = new StringBuilder( "========================LEXER OUTPUT===========================\n" );


        // read file and lex it
        // +   buffered reader constructor can throw an exception and not close the file reader!
        try( FileReader frInput = new FileReader( fInput );
             BufferedReader brInput = new BufferedReader( frInput );
        )
        {
            Lexer lexer = new Lexer( brInput );
            Symbol token = null;

            // lex the input .mj file
            try
            {
                while( true )
                {
                    token = ( Symbol )( lexer.next_token() );
                    symbols.add( token );

                    output.append( token.toString() ).append( "\n" );
                    logger.info( token.toString() );

                    if( token.sym == SymbolCode.EOF ) break;
                }
            }
            catch( IOException ex )
            {
                errors.add( ( token != null ? token.left  : -1 ),
                            ( token != null ? token.right : -1 ), "Error lexing current token", CompilerError.LEXICAL_ERROR, ex );
                return null;
            }
        }
        catch( IOException ex )
        {
            errors.add( -1, -1, "Cannot open input file", CompilerError.LEXICAL_ERROR, ex );
            return null;
        }


        // if the lexer output is requested, write the lexer results to the lexer output file
        if( fLex != null )
        {
            // write lex results to output lex file
            // +   buffered writer constructor can throw an exception and not close the file writer!
            try( FileWriter fwLex = new FileWriter( fLex );
                 BufferedWriter bwLex = new BufferedWriter( fwLex );
            )
            {
                bwLex.append( output );
            }
            catch( IOException ex )
            {
                errors.add( -1, -1, "Cannot open/write to output lex file", CompilerError.LEXICAL_ERROR, ex );
                return null;
            }
        }

        // return the lexed symbols
        return symbols;
    }

    // parse the input file
    // +   set the root 
    private static SyntaxNode parse( File fInput, File fParse, boolean verbose )
    {
        // if the input file is missing, return
        if( fInput == null ) return null;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ PARSER" );
        logger.info( "Parsing input file:" );

        SyntaxNode syntaxRoot = null;
        Parser parser = null;


        // read file and parse it
        // +   buffered reader constructor can throw an exception and not close the file reader!
        try( FileReader frInput = new FileReader( fInput );
             BufferedReader brInput = new BufferedReader( frInput );
        )
        {
            try
            {
                Lexer lexer = new Lexer( brInput );
                parser = new Parser( lexer );
                
                // parse the input file
                java_cup.runtime.Symbol rootSymbol = null;
                
                if( !verbose ) rootSymbol = parser.parse();
                else           rootSymbol = parser.debug_parse();

                if( rootSymbol != null )
                {
                    syntaxRoot = ( SyntaxNode )( rootSymbol.value );
                }
                
                // if the syntax tree is missing but no errors are reported (should never happen)
                if( ( !parser.hasErrors() && syntaxRoot == null ) )
                {
                    errors.add( -1, -1, "Syntax tree missing", CompilerError.SYNTAX_ERROR );
                    return null;
                }

            }
            catch( Exception ex )
            {
                errors.add( -1, -1, "Error parsing input file", CompilerError.SYNTAX_ERROR, ex );
                return null;
            }
        }
        catch( IOException ex )
        {
            errors.add( -1, -1, "Cannot open input file", CompilerError.LEXICAL_ERROR, ex );
            return null;
        }


        // if parser output is requested, write the parse results to the parse output file
        if( fParse != null )
        {
            // write parse results to output parse file
            // +   buffered writer constructor can throw an exception and not close the file writer!
            try( FileWriter fwParse = new FileWriter( fParse );
                 BufferedWriter bwParse = new BufferedWriter( fwParse );
            )
            {
                bwParse.write( syntaxTree( syntaxRoot ) );
            }
            catch( IOException ex )
            {
                errors.add( -1, -1, "Cannot open/write to output parse file", CompilerError.SYNTAX_ERROR, ex );
            }
        }

        // if the parser encountered a fatal error, return
        if( parser.hasFatalError() ) return null;

        // return the syntax tree
        return syntaxRoot;
    }

    // semantic check the the syntax tree
    private static SemanticVisitor checkSemantics( SyntaxNode syntaxRoot )
    {
        // if the syntax tree is missing, return
        if( syntaxRoot == null ) return null;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ SEMANTIC" );
        logger.info( "Semantic checking" );
        
        // initialize global ("universal") scope in the symbol table
        Tab.init();
        // create a semantic check visitor
        SemanticVisitor semanticVisitor = new SemanticVisitor();

        // do a semantic pass over the abstract syntax tree and fill in the symbol table
        syntaxRoot.traverseBottomUp( semanticVisitor );
        
        // log the symbol table and the syntax tree
        logger.log( Log4J.INFO, tsdump(), true );
        logger.log( Log4J.INFO, syntaxTree( syntaxRoot ), true );

        // if there are syntax or semantic errors, return
        if( errors.hasErrors() ) return null;

        return semanticVisitor;
    }

    // compile the given syntax tree into microjava code
    public static boolean generateCode( SyntaxNode syntaxRoot, SemanticVisitor semanticCheck, File fOutput )
    {
        // if the syntax tree or output file is missing, return
        if( syntaxRoot == null || fOutput == null ) return false;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER" );
        logger.info( "Compiling code" );

        // generate code from the abstract syntax tree
        CodeGenVisitor codeGenerator = new CodeGenVisitor();
        syntaxRoot.traverseBottomUp( codeGenerator );
        Code.dataSize = semanticCheck.nVars;
        Code.mainPc = codeGenerator.getMainPc();
        
        // write compiler results to output file
        try( FileOutputStream fosOutput = new FileOutputStream( fOutput ); )
        {
            Code.write( fosOutput );
        }
        catch( IOException ex )
        {
            errors.add( -1, -1, "Cannot open/write to output file", CompilerError.SEMANTIC_ERROR, ex );
            return false;
        }

        // return if there are errors during code generation
        return !errors.hasErrors();
    }
    
    

    // return the compiler's symbol table as string
    private static String tsdump()
    {
        PrintStream stdout = System.out;
        String output = null;
        
        try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream( buffer, true );
        )
        {
            // workaround since symbol table dump method only outputs to System.out
            System.setOut( printStream );
            Tab.dump();
            printStream.flush();
            output = buffer.toString( "UTF-8" );
        }
        catch( IOException ex )
        {
            errors.add( -1, -1, "Error during conversion of symbol table to string", CompilerError.SEMANTIC_ERROR, ex );
        }
        finally
        {
            // restore the previous print stream
            System.setOut( stdout );
        }
        
        return output;
    }

    // return the syntax tree as a string
    private static String syntaxTree( SyntaxNode syntaxRoot )
    {
        if( syntaxRoot == null ) return null;
        String syntaxTree = "========================SYNTAX TREE============================\n"
                          + syntaxRoot.toString();
        return syntaxTree;
    }



    // set the compiler parameters
    // +   compile [-verbose] [-lex file] [-par file] [-o file] file
    // +   returns true if everything is ok
    private static boolean setParams( String[] params )
    {
        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER PARAMS" );
        logger.info( "Setting compiler parameters:" );
        logger.info( String.join( " ", params ) );

        // variables for storing file names
        String fnameLex = null;
        String fnameParse = null;
        String fnameOutput = null;
        String fnameInput = null;
        
        // clear errors
        errors.clear();
        
        // parse parameters
        for( int i = 0; i < params.length; i++ )
        {
            switch( params[ i ] )
            {
                case "-verbose":
                {
                    verbose = true;
                    break;
                }

                case "-lex":
                {
                    if( fnameLex != null )
                    {
                        errors.add( -1, i, "Lexer output file already specified", CompilerError.ARGUMENTS_ERROR );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( -1, i, "Lexer output file not specified after the -lex flag", CompilerError.ARGUMENTS_ERROR );
                        break;
                    }

                    fnameLex = params[ ++i ];
                    if( !fnameLex.endsWith( ".lex" ) ) { fnameLex = fnameLex + ".lex"; }
                    
                    logger.info( "fnameLex = " + fnameLex );
                    break;
                }

                case "-par":
                {
                    if( fnameParse != null )
                    {
                        errors.add( -1, i, "Parser output file already specified", CompilerError.ARGUMENTS_ERROR );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( -1, i, "Parser output file not specified after the -par flag", CompilerError.ARGUMENTS_ERROR );
                        break;
                    }

                    fnameParse = params[ ++i ];
                    if( !fnameParse.endsWith( ".par" ) ) { fnameParse = fnameParse + ".par"; }

                    logger.info( "fnameParse = " + fnameParse );
                    break;
                }

                case "-o":
                {
                    if( fnameOutput != null )
                    {
                        errors.add( -1, i, "Output file already specified", CompilerError.ARGUMENTS_ERROR );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( -1, i, "Output file not specified after the -o flag", CompilerError.ARGUMENTS_ERROR );
                        break;
                    }
                    
                    fnameOutput = params[ ++i ];
                    if( !fnameOutput.endsWith( ".obj" ) ) { fnameOutput = fnameOutput + ".obj"; }

                    logger.info( "fnameOutput = " + fnameOutput );
                    break;
                }

                default:
                {
                    if( fnameInput != null )
                    {
                        errors.add( -1, i, String.format( "Unknown option: '%s'", params[ i ] ), CompilerError.ARGUMENTS_ERROR );
                        break;
                    }
                    
                    fnameInput = params[ i ];
                    if( !fnameInput.endsWith( ".mj" ) ) { fnameInput = fnameInput + ".mj"; }

                    logger.info( "fnameInput = " + fnameInput );
                    break;
                }
            }
        }

        // the input file must be specified
        if( fnameInput == null )
        {
            errors.add( -1, -1, "Input file not specified", CompilerError.ARGUMENTS_ERROR );
        }

        // if there are errors log them and return
        if( errors.hasErrors() )
        {
            resetParams();
            return false;
        }


        // if the output file is missing and other switches are not specified, set it to be the input file with the .obj extension
        if( fnameOutput == null && fnameLex == null && fnameParse == null )
        {
            fnameOutput = fnameInput.substring( 0, fnameInput.length() - ".mj".length() ) + ".obj";
            logger.info( "fnameOutput = " + fnameOutput );
        }

        // open the given files
        if( fnameInput  != null ) fInput  = new File( fnameInput  );
        if( fnameLex    != null ) fLex    = new File( fnameLex    );
        if( fnameParse  != null ) fParse  = new File( fnameParse  );
        if( fnameOutput != null ) fOutput = new File( fnameOutput );

        if( verbose )
        {
            logger.info( "verbose = true" );
        }
        
        // check if the files exist and are readable/writable
        if( fInput != null )
        {
            if( !fInput.exists() )
            {
                errors.add( -1, -1, "Input file does not exist", CompilerError.ARGUMENTS_ERROR );
            }
            else if( !fInput.canRead() )
            {
                errors.add( -1, -1, "Input file is not readable", CompilerError.ARGUMENTS_ERROR );
            }
            
            logger.info( "fInput = " + fInput.getAbsolutePath() );
        }
        
        if( fLex != null )
        {
            if( fLex.exists() && !fLex.canWrite() )
            {
                errors.add( -1, -1, "Lexer output file exists and is not writable", CompilerError.ARGUMENTS_ERROR );
            }
            
            logger.info( "fLex = " + fLex.getAbsolutePath() );
        }

        if( fParse != null )
        {
            if( fParse.exists() && !fParse.canWrite() )
            {
                errors.add( -1, -1, "Parser output file exists and is not writable", CompilerError.ARGUMENTS_ERROR );
            }
            
            logger.info( "fParse = " + fParse.getAbsolutePath() );
        }

        if( fOutput != null )
        {
            if( fOutput.exists() && !fOutput.canWrite() )
            {
                errors.add( -1, -1, "Output file exists and is not writable", CompilerError.ARGUMENTS_ERROR );
            }
            
            logger.info( "fOutput = " + fOutput.getAbsolutePath() );
        }

        // if there are errors log them and return
        if( errors.hasErrors() )
        {
            resetParams();
            return false;
        }

        // return that the compiler params are successfully set
        logger.info( "Compiler parameters are valid" );
        return true;
    }
}
