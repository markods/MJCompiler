$CompilerCmd = "java -cp '.\MJCompiler\lib\JFlex.jar' JFlex.Main -nobak -d '.\MJCompiler\src\rs\ac\bg\etf\pp1' '.\MJCompiler\spec\mjlexer.flex'"
$CompilerCmd
Invoke-Expression -Command $CompilerCmd
