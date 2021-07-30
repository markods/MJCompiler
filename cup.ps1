$CompilerCmd = "java -cp './MJCompiler/lib/cup_v10k.jar' java_cup.Main -dump_states -destdir './MJCompiler/src/rs/ac/bg/etf/pp1' -parser 'MJParser' -ast 'MJCompiler/src/rs/ac/bg/etf/pp1/ast' -buildtree './MJCompiler/spec/mjparser.cup'"
$CompilerCmd
Invoke-Expression -Command $CompilerCmd
