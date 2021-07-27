package rs.ac.bg.etf.pp1.test;

public class CompilerError {
	
	public enum CompilerErrorType { LEXICAL_ERROR, SYNTAX_ERROR, SEMANTIC_ERROR };
	
	private int line;
	private String message;
	private CompilerErrorType type;
	
	public CompilerError(int line, String message, CompilerErrorType type) {
		this.line = line;
		this.message = message;
		this.type = type;
	}
	
	public void setLine(int line) {
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public String getMessage() {
		return message;
	}
	
	public CompilerErrorType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return String.format("[Line #%-2d]  %s: %s", line, type, message);
	}
}
