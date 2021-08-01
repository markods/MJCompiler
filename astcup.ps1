Push-Location "./MJCompiler/src" 
$CompilerCmd = "java -cp '../lib/cup_v10k.jar' java_cup.Main -destdir './rs/ac/bg/etf/pp1' -parser 'MJParser' -ast rs.ac.bg.etf.pp1.ast -buildtree '../spec/mjparser.cup'"
$CompilerCmd
Invoke-Expression -Command $CompilerCmd
Pop-Location

# -dump_states
