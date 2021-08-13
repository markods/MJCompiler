package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import org.apache.log4j.Priority;
import rs.ac.bg.etf.pp1.CompilerError.CompilerErrorType;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class Compiler
{
    static
    {
        DOMConfigurator.configure( Log4JUtils.instance().findLoggerConfigFile() );
        Log4JUtils.instance().prepareLogFile( Logger.getRootLogger() );
        
        logger = Logger.getLogger( Compiler.class );
        errors = new CompilerErrors();
    }
    
    private static Logger logger;
    private static CompilerErrors errors;

    private static File fInput = null;
    private static File fLex = null;
    private static File fParse = null;
    private static File fOutput = null;

    // private constructor
    private Compiler() {}

    public static CompilerErrors getErrors() { return errors; }
    public static boolean hasErrors() { return errors.hasErrors(); }



    // compile [-lex file] [-par file] [-o file] file
    public static void main( String[] args )
    {
        if( !Compiler.compile( args ) )
        {
            System.err.println( Compiler.getErrors().toString() );
        }
    }
    

    // set the compiler parameters
    // +   compile [-lex file] [-par file] [-o file] file
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
                case "-lex":
                {
                    if( fnameLex != null )
                    {
                        errors.add( i, "Lexer output file already specified", CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( i, "Lexer output file not specified after the -lex flag", CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
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
                        errors.add( i, "Parser output file already specified", CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( i, "Parser output file not specified after the -par flag", CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
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
                        errors.add( i, "Output file already specified", CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
                        break;
                    }
                    if( i+1 >= params.length )
                    {
                        errors.add( i, "Output file not specified after the -o flag", CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
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
                        errors.add( i, String.format( "Unknown option: '%s'", params[ i ] ), CompilerErrorType.ARGUMENTS_ERROR );
                        logger.error( errors.getLast().toString() );
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
            errors.add( -1, "Input file not specified", CompilerErrorType.ARGUMENTS_ERROR );
            logger.error( errors.getLast().toString() );
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

        // check if the files exist and are readable/writable
        if( fInput != null )
        {
            if( !fInput.exists() )
            {
                errors.add( -1, "Input file does not exist", CompilerErrorType.ARGUMENTS_ERROR );
                logger.error( errors.getLast().toString() );
            }
            else if( !fInput.canRead() )
            {
                errors.add( -1, "Input file is not readable", CompilerErrorType.ARGUMENTS_ERROR );
                logger.error( errors.getLast().toString() );
            }
            
            logger.info( "fInput = " + fInput.getAbsolutePath() );
        }
        
        if( fLex != null )
        {
            if( fLex.exists() && !fLex.canWrite() )
            {
                errors.add( -1, "Lexer output file exists and is not writable", CompilerErrorType.ARGUMENTS_ERROR );
                logger.error( errors.getLast().toString() );
            }
            
            logger.info( "fLex = " + fLex.getAbsolutePath() );
        }

        if( fParse != null )
        {
            if( fParse.exists() && !fParse.canWrite() )
            {
                errors.add( -1, "Parser output file exists and is not writable", CompilerErrorType.ARGUMENTS_ERROR );
                logger.error( errors.getLast().toString() );
            }
            
            logger.info( "fParse = " + fParse.getAbsolutePath() );
        }

        if( fOutput != null )
        {
            if( fOutput.exists() && !fOutput.canWrite() )
            {
                errors.add( -1, "Output file exists and is not writable", CompilerErrorType.ARGUMENTS_ERROR );
                logger.error( errors.getLast().toString() );
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

    // reset the compiler parameters
    private static void resetParams()
    {
        fInput = null;
        fLex = null;
        fParse = null;
        fOutput = null;
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
            logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ LEXER" );
            logger.info( "Lexing input file:" );

            Symbol token = null;
            StringBuilder lex = new StringBuilder( "// MJ lexer output:\n" );


            // read file and lex it
            // +   buffered reader constructor can throw an exception and not close the file reader!
            try( FileReader frInput = new FileReader( fInput );
                 BufferedReader brInput = new BufferedReader( frInput );
            )
            {
                Yylex lexer = new Yylex( brInput );

                // lex the input .mj file
                try
                {
                    while( true )
                    {
                        token = lexer.next_token();

                        lex.append( sym.symbolToString( token ) ).append( "\n" );
                        logger.info( sym.symbolToString( token ) );

                        if( token == null )
                        {
                            errors.add( -1, "Invalid token", CompilerErrorType.LEXICAL_ERROR );
                            logger.error( errors.getLast().toString() );
                        }
                        else if( token.value instanceof CompilerError )
                        {
                            errors.add( ( CompilerError )token.value );
                            logger.error( errors.getLast().toString() );
                        }

                        if( token == null || token.sym == sym.EOF ) break;
                    }
                }
                catch( IOException ex )
                {
                    errors.add( ( token != null ? token.left : -1 ), "Error lexing current token", CompilerErrorType.LEXICAL_ERROR );
                    logger.error( errors.getLast().toString(), ex );
                }
            }
            catch( IOException ex )
            {
                errors.add( -1, "Cannot open input file", CompilerErrorType.LEXICAL_ERROR );
                logger.error( errors.getLast().toString(), ex );
            }

            // return if there are lexing errors
            if( errors.hasErrors() ) return false;



            // write lex results to output lex file
            // +   buffered writer constructor can throw an exception and not close the file writer!
            try( FileWriter fwLex = new FileWriter( fLex );
                 BufferedWriter bwLex = new BufferedWriter( fwLex );
            )
            {
                bwLex.append( lex );
            }
            catch( IOException ex )
            {
                errors.add( -1, "Cannot open/write to output lex file", CompilerErrorType.LEXICAL_ERROR );
                logger.error( errors.getLast().toString(), ex );
            }

            // return if there are lexing errors
            if( errors.hasErrors() ) return false;
        }



        // if parse output file or the object file is specified
        if( fParse != null || fOutput != null )
        {
            logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ PARSER" );
            logger.info( "Parsing input file:" );

            SyntaxNode rootNode = null;



            // read file and parse it
            // +   buffered reader constructor can throw an exception and not close the file reader!
            try( FileReader frInput = new FileReader( fInput );
                 BufferedReader brInput = new BufferedReader( frInput );
            )
            {
                try
                {
                    Yylex lexer = new Yylex( brInput );
                    MJParser parser = new MJParser( lexer );
                    
                    // parse the input file
                    Symbol rootSymbol = parser.parse();
                    rootNode = ( SyntaxNode )( rootSymbol.value );
                    
                    if( parser.hasErrors() || rootNode == null )
                    {
                        errors.add( -1, "Error parsing input file", CompilerErrorType.SYNTAX_ERROR );
                        logger.error( errors.getLast().toString() );
                    }
                }
                catch( Exception ex )
                {
                    errors.add( -1, "Error parsing input file", CompilerErrorType.SYNTAX_ERROR );
                    logger.error( errors.getLast().toString(), ex );
                }
            }
            catch( IOException ex )
            {
                errors.add( -1, "Cannot open input file", CompilerErrorType.LEXICAL_ERROR );
                logger.error( errors.getLast().toString(), ex );
            }

            // return if there are parsing errors
            if( errors.hasErrors() ) return false;



            // if parser output is requested, write the parse results to the parse output file
            if( fParse != null )
            {
                // write parse results to output parse file
                // +   buffered writer constructor can throw an exception and not close the file writer!
                try( FileWriter fwParse = new FileWriter( fParse );
                     BufferedWriter bwParse = new BufferedWriter( fwParse );
                )
                {
                    bwParse.write( rootNode.toString() );
                }
                catch( IOException ex )
                {
                    errors.add( -1, "Cannot open/write to output parse file", CompilerErrorType.SYNTAX_ERROR );
                    logger.error( errors.getLast().toString(), ex );
                }

                // return if there are parsing errors
                if( errors.hasErrors() ) return false;
            }



            // if compiler output is requested, write the parse results to the parse output file
            if( fOutput != null )
            {
                logger.info( "---------------------------------------------------------------------------------------------------------------- <<< MJ COMPILER" );
                logger.info( "Semantic checking and compiling abstract syntax tree" );
                
                // initialize global ("universal") scope in the symbol table
                Tab.init();
                // do a semantic pass over the abstract syntax tree and fill in the symbol table
                SemanticPass semanticCheck = new SemanticPass();
                rootNode.traverseBottomUp( semanticCheck );
                
                // print the symbol table
                {
                    String symbolTable = tsdump();
                    if( symbolTable == null ) symbolTable = "";
                    
                    String[] lines = symbolTable.split( "\\R", -1 );
                    for( String line: lines )
                    {
                        logger.info( line );
                    }
                }

                // return if the semantic check failed
                if( !semanticCheck.passed() ) return false;
    

                // generate code from the abstract syntax tree
                CodeGenerator codeGenerator = new CodeGenerator();
                rootNode.traverseBottomUp( codeGenerator );
                Code.dataSize = semanticCheck.nVars;
                Code.mainPc = codeGenerator.getMainPc();
                
                // write compiler results to output file
                try( FileOutputStream fosOutput = new FileOutputStream( fOutput ); )
                {
                    Code.write( fosOutput );
                }
                catch( IOException ex )
                {
                    errors.add( -1, "Cannot open/write to output file", CompilerErrorType.SEMANTIC_ERROR );
                    logger.error( errors.getLast().toString(), ex );
                }

                // return if there are compiling errors
                if( errors.hasErrors() ) return false;
            }
        }
        

        // return true if there are no errors during compilation
        return errors.hasErrors();
    }
    
    
    // return the compiler's symbol table as string
    public static String tsdump()
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
            logger.error( "IOException during conversion of symbol table to string", ex );
        }
        finally
        {
            // restore the previous print stream
            System.setOut( stdout );
        }
        
        return output;
    }
}
