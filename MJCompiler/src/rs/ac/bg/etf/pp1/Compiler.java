package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import rs.etf.pp1.mj.runtime.Code;

public class Compiler
{
    public static final Log4J logger = Log4J.getLogger( Compiler.class );

    public static final CompilerErrorList errors = new CompilerErrorList();
    public static final TokenList tokens = new TokenList();

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
            TokenList tokens = lex( fInput, fLex );
            // if the lexer could not lex the input file, return
            if( tokens == null ) return false;
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
                SemanticVisitor semanticCheck = semanticAnalysis( syntaxRoot );
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
    private static TokenList lex( File fInput, File fLex )
    {
        // if the input file is missing, return
        if( fInput == null ) return null;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ LEXER" );
        logger.info( "Lexing input file:" );

        StringBuilder output = new StringBuilder( "========================LEXER OUTPUT===========================\n" );


        // read file and lex it
        // +   buffered reader constructor can throw an exception and not close the file reader!
        try( FileReader frInput = new FileReader( fInput );
             BufferedReader brInput = new BufferedReader( frInput );
        )
        {
            BufferedLexer lexer = new BufferedLexer( brInput );
            // HACK: copy the reference to the lexer's tokens over to the global tokens list
            // +   meaning they are the same object (identical)
            // +   important for correct error reporting
            tokens.assign( lexer.getTokens() );
            Token token = null;

            // lex the input .mj file
            try
            {
                while( true )
                {
                    token = ( Token )( lexer.next_token() );

                    output.append( token.toString() ).append( "\n" );
                    logger.info( token.toString() );

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
                errors.add( CompilerError.LEXICAL_ERROR, "Error lexing input file", ex );
                tokens.clear();
                return null;
            }
        }
        catch( IOException ex )
        {
            errors.add( CompilerError.LEXICAL_ERROR, "Cannot open input file", ex );
            Compiler.tokens.clear();
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
                errors.add( CompilerError.LEXICAL_ERROR, "Cannot open/write to output lex file", ex );
                return null;
            }
        }

        // return the lexed tokens
        return tokens;
    }

    // parse the input file
    // +   set the root 
    private static SyntaxNode parse( File fInput, File fParse, boolean verbose )
    {
        // if the input file is missing, return
        if( fInput == null ) return null;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ PARSER" );
        logger.info( "Parsing input file:" );
        logger.info( "========================PARSER OUTPUT===========================" );

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
                BufferedLexer lexer;

                // if the lexer hasn't already lexed the entire file
                if( tokens.size() == 0 )
                {
                    // create a lexer on the input file
                    lexer = new BufferedLexer( brInput );

                    // HACK: copy the reference from the global tokens list over to the lexer's tokens
                    // +   meaning they are the same object (identical)
                    // +   important for correct error reporting
                    tokens.assign( lexer.getTokens() );
                }
                // otherwise,
                else
                {
                    // create a lexer on the (already lexed file's) token list

                    // HACK: copy the reference from the global tokens list over to the lexer's tokens
                    // +   meaning they are the same object (identical)
                    // +   important for correct error reporting
                    lexer = new BufferedLexer( tokens );
                }

                parser = new Parser( lexer );
                

                // parse the input file
                java_cup.runtime.Symbol rootSymbol = null;
                
                if( !verbose )
                {
                    rootSymbol = parser.parse();
                }
                else
                {
                    try( FileOutputStream fsLogger = new FileOutputStream( Log4J.getLogFile(), true );
                         SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDERR, fsLogger );
                    )
                    {
                        // workaround since debug parse method only outputs to System.out
                        rootSymbol = parser.debug_parse();
                    }
                }

                if( rootSymbol != null && rootSymbol.value instanceof SyntaxNode )
                {
                    syntaxRoot = ( SyntaxNode )( rootSymbol.value );
                }
                
                // if the syntax tree is missing but no errors are reported (should never happen)
                if( ( !parser.hasErrors() && syntaxRoot == null ) )
                {
                    errors.add( CompilerError.SYNTAX_ERROR, "Syntax tree missing" );
                    return null;
                }

            }
            catch( Exception ex )
            {
                errors.add( CompilerError.SYNTAX_ERROR, "Error parsing input file", ex );
                return null;
            }
        }
        catch( IOException ex )
        {
            errors.add( CompilerError.LEXICAL_ERROR, "Cannot open input file", ex );
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
                errors.add( CompilerError.SYNTAX_ERROR, "Cannot open/write to output parse file", ex );
            }
        }

        // if the parser encountered a fatal error, return
        if( parser.hasFatalError() ) return null;

        // return the syntax tree
        return syntaxRoot;
    }

    // semantic check the the syntax tree
    private static SemanticVisitor semanticAnalysis( SyntaxNode syntaxRoot )
    {
        // if the syntax tree is missing, return
        if( syntaxRoot == null ) return null;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ SEMANTIC" );
        logger.info( "Semantic checking" );
        
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
    private static boolean generateCode( SyntaxNode syntaxRoot, SemanticVisitor semanticCheck, File fOutput )
    {
        // if the syntax tree or output file is missing, return
        if( syntaxRoot == null || fOutput == null ) return false;

        logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER" );
        logger.info( "Compiling code" );

        // generate code from the abstract syntax tree
        CodeGenVisitor codeGenerator = new CodeGenVisitor();
        syntaxRoot.traverseBottomUp( codeGenerator );
        Code.dataSize = semanticCheck.getVarCount();
        Code.mainPc = codeGenerator.getMainPc();
        
        // write compiler results to output file
        try( FileOutputStream fosOutput = new FileOutputStream( fOutput ); )
        {
            Code.write( fosOutput );
        }
        catch( IOException ex )
        {
            errors.add( CompilerError.SEMANTIC_ERROR, "Cannot open/write to output file", ex );
            return false;
        }

        // return if there are errors during code generation
        return !errors.hasErrors();
    }
    
    

    // return the compiler's symbol table as string
    private static String tsdump()
    {
        return SymbolTable.dump();
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
                        errors.add( CompilerError.ARGUMENTS_ERROR, "Lexer output file already specified", CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( CompilerError.ARGUMENTS_ERROR, "Lexer output file not specified after the -lex flag", CompilerError.NO_INDEX, i );
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
                        errors.add( CompilerError.ARGUMENTS_ERROR, "Parser output file already specified", CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( CompilerError.ARGUMENTS_ERROR, "Parser output file not specified after the -par flag", CompilerError.NO_INDEX, i );
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
                        errors.add( CompilerError.ARGUMENTS_ERROR, "Output file already specified", CompilerError.NO_INDEX, i );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( CompilerError.ARGUMENTS_ERROR, "Output file not specified after the -o flag", CompilerError.NO_INDEX, i );
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
                        errors.add( CompilerError.ARGUMENTS_ERROR, String.format( "Unknown option: '%s'", params[ i ] ), CompilerError.NO_INDEX, i );
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
            errors.add( CompilerError.ARGUMENTS_ERROR, "Input file not specified" );
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
                errors.add( CompilerError.ARGUMENTS_ERROR, "Input file does not exist" );
            }
            else if( !fInput.canRead() )
            {
                errors.add( CompilerError.ARGUMENTS_ERROR, "Input file is not readable" );
            }
            
            logger.info( "fInput = " + fInput.getAbsolutePath() );
        }
        
        if( fLex != null )
        {
            if( fLex.exists() && !fLex.canWrite() )
            {
                errors.add( CompilerError.ARGUMENTS_ERROR, "Lexer output file exists and is not writable" );
            }
            
            logger.info( "fLex = " + fLex.getAbsolutePath() );
        }

        if( fParse != null )
        {
            if( fParse.exists() && !fParse.canWrite() )
            {
                errors.add( CompilerError.ARGUMENTS_ERROR, "Parser output file exists and is not writable" );
            }
            
            logger.info( "fParse = " + fParse.getAbsolutePath() );
        }

        if( fOutput != null )
        {
            if( fOutput.exists() && !fOutput.canWrite() )
            {
                errors.add( CompilerError.ARGUMENTS_ERROR, "Output file exists and is not writable" );
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

