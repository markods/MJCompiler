Push-Location "./MJCompiler/src" 
$CompilerCmd = "java -cp '../lib/cup_v10k.jar' java_cup.Main -destdir './rs/ac/bg/etf/pp1' -parser 'MJParser' '../spec/mjparser.cup'"
$CompilerCmd
Invoke-Expression -Command $CompilerCmd
Pop-Location

# -dump_states
