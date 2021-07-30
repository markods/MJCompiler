$CompilerCmd = "java -cp './MJCompiler/lib/jflex-1.4.3.jar' JFlex.Main -nobak -d './MJCompiler/src/rs/ac/bg/etf/pp1' './MJCompiler/spec/mjlexer.flex'"
$CompilerCmd
Invoke-Expression -Command $CompilerCmd
