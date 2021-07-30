package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.util.Log4JUtils;

public class LexerTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws IOException {
		Logger logger = Logger.getLogger(LexerTest.class);
		Reader br = null;
		try {
			
			File sourceCode = new File("test/program.mj");	
			logger.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			
			Yylex lexer = new Yylex(br);
			Symbol currToken = null;
			while ((currToken = lexer.next_token()).sym != sym.EOF) {
				if (currToken != null && currToken.value != null)
                {
                    if( currToken.sym != sym.error )
                    {
                        logger.info( String.format( "%-15s %s", sym.getSymbolName( currToken.sym ), currToken.value.toString() ) );
                    }
                    else
                    {
                        logger.error( String.format( "Leksicka greska na liniji %d kolona %d:\n\t%-15s %s",
                                currToken.left, currToken.right, sym.getSymbolName( currToken.sym ), currToken.value.toString() ) );
                    }
                }
			}
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { logger.error(e1.getMessage(), e1); }
		}
	}
	
}
