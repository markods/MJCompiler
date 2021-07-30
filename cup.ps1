$CompilerCmd = "java -cp './MJCompiler/lib/cup_v10k.jar' java_cup.Main -destdir './MJCompiler/src/rs/ac/bg/etf/pp1' -parser 'MJParser' -ast ast -buildtree './MJCompiler/spec/mjparser.cup'"
$CompilerCmd
Invoke-Expression -Command $CompilerCmd

# dump_states