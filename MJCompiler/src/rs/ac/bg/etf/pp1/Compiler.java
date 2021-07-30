package rs.ac.bg.etf.pp1;

import java.util.List;

public interface Compiler {
	
	List<CompilerError> compile(String sourceFilePath, String outputFilePath);
}
