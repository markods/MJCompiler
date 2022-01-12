# ________________________________________________________________________________________________________________
# Manual
#
# + Setup:
#    + Add Ant to the environment variable 'path' (if it isn't already there):
#       + .../NetBeans/netbeans/extide/ant/bin
#    + If the powershell won't run the script because of the execution policy, run this command:
#       + Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser
#    + Enjoy!
#
# + Examples:
#    + build =help
#    + build =jflex =cup =clean =build =test
#    + build =build   =compile -o codeC.obj codeC.mj   =run -debug codeC.obj
#    + build =build   =compile -o codeA.obj codeA.mj   =run -debug codeA.obj
#
# + PowerShell deep dives:
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-hashtable?view=powershell-7.1
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-arrays?view=powershell-7.1
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-hashtable?view=powershell-7.1#saving-a-nested-hashtable-to-file
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/ps101/06-flow-control?view=powershell-7.1
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/ps101/09-functions?view=powershell-7.1
#
# + Specific stuff:
#    + https://docs.microsoft.com/en-us/dotnet/api/system.collections.arraylist?view=net-5.0
#    + https://powershellexplained.com/2017-11-20-Powershell-stringBuilder/
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-null?view=powershell-7.1
#    + https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.management/remove-item?view=powershell-7.1#example-4--delete-files-in-subfolders-recursively
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-switch?view=powershell-7.1
#    + https://docs.microsoft.com/en-us/powershell/scripting/learn/deep-dives/everything-about-hashtable?view=powershell-7.1#splatting-hashtables-at-cmdlets
#    + https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_operators?view=powershell-7.1
#
# + Important:
#    + https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_classes?view=powershell-7.2#output-in-class-methods
#    + https://github.com/PowerShell/PowerShell/issues/4616

using namespace System.Collections.Generic;
using namespace System.Management.Automation;
using namespace System.Collections.Specialized;
using namespace System.Text.Json;





# ________________________________________________________________________________________________________________
# Framework

[string] $script:ProjectRoot = Split-Path -Path $MyInvocation.MyCommand.Path -Parent;
[int] $script:LastStatusCode = 0;

[string] $script:StageSep = "------------------------------------------------------------------------------------------------------ <<< {0}";
[string] $script:LineSep  = "------------------";

# NOTE: leave powershell array constructor ( @() ) if there is only one argument (otherwise it won't be a powershell array due to unpacking)
[string[][]] $script:DefaultArgs =
    ( "=jflex", "=cup", "=clean", "=build", "=test" ),
    ( "=jflex", "=cup", "=build" ),
    ( "=clean", "=build", "=test" );

[string] $script:HelpMessage = @"
build   [[-]-help] [-0][-1][-2]   [=jflex ...] [=cup ...]   [=clean] [=build] [=test]   [=compile ...] [=disasm ...] [=run ...]

Default:             build   --help

Switches:
    -help            show the help menu
    -0               use the default parameters 0:   build $( $script:DefaultArgs[ 0 ] -Join ' ')
    -1               use the default parameters 1:   build $( $script:DefaultArgs[ 1 ] -Join ' ')
    -2               use the default parameters 2:   build $( $script:DefaultArgs[ 2 ] -Join ' ')
     

    =cup             run the CUP tool on the cup specification and generate the abstract syntax tree
      -dump_grammar  +   shows the used parser grammar
      -dump_states   +   shows the constructed parser states
      ...            +   ...
    =jflex           run the JFlex tool on the flex specification
      ...            +   ...

    =clean           clean project (except source files generated by the jflex and cup tools)
    =build           build project
    =test            run all available unit tests

    =compile         compile the MJ program with the given parameters
      -verbose       +   enable verbose compiler output
      -lex file      +   write lexer output to file.lex
      -par file      +   write parser output to file.par
      -o file        +   write compiled output to file.obj (<input file name>.obj by default if this flag is missing)
      file           +   specify the input file name (file.mj, .mj is appended if missing)
      ...            +   ...
    =disasm          disassemble the given .obj file
      file           +   specify the input .obj file name
      ...            +   ...
    =run             run the .obj file on the MicroJava virtual machine
      -debug         +   run in debug mode
      file           +   specify the input .obj file name
      ...            +   ...

"@;

[scriptblock] $script:StageScript_Default =
{
    param( [Stage] $Stage )

    if( $Stage.CmdPartArr.Count -eq 0 ) { $script:LastStatusCode = -1; return; }

    # print the stage name
    $script:StageSep -f $Stage.Name | Write-Output;
    # print the stage command
    $Command = $Stage.GetCommand();
    $Command | Write-Output;

    # if the subcommand doesn't accept arguments but they were given anyway (if the subcommand is simple)
    # IMPORTANT: && and || are pipeline chain operators!, not logical operators (-and and -or)
    $CmdArgArr = $Stage.CmdArgArr;
    if( !$Stage.AcceptsArgs   -and   $CmdArgArr.Count -gt 0 )
    {
        "Subcommand does not accept arguments" | Write-Output;
        $script:LastStatusCode = 400; return;
    }

    # invoke the stage's command
    Invoke-Expression -Command $Command;
    # if the command invocation failed, return
    if( $? -ne $true )
    {
        "Subcommand invocation failure" | Write-Output;
        $script:LastStatusCode = 400; return;
    }
    # if an error occured in the command, return
    if( $LASTEXITCODE -ne 0 )
    {
        "Subcommand invocation failure" | Write-Output;
        $script:LastStatusCode = $LASTEXITCODE; return;
    }
    
    $script:LastStatusCode = 0; return;
}



# class FileUtil
# {
    function FileUtil_MoveItem
    {
        param( [string] $Path, [string] $Destination )

        [bool] $PathExists = Test-Path $Path -PathType "Any";
        if( $? -ne $true )
        {
            "Could not test if item exists: '{0}'" -f $Path | Write-Output;
            $script:LastStatusCode = -1; return;
        }
        if( !$PathExists ) { $script:LastStatusCode = 0; return; }

        Move-Item -Path $Path -Destination $Destination *>&1 | Out-Null;
        if( $? -ne $true )
        {
            "Could not move item: '{0}'" -f $Path | Write-Output;
            $script:LastStatusCode = -1; return ;
        }

        $script:LastStatusCode = 0; return ;
    }

    function FileUtil_RemoveFolder
    {
        param( [string] $Path )

        [bool] $PathExists = Test-Path $Path -PathType "Container";
        if( $? -ne $true )
        {
            "Could not test if folder exists: '{0}'" -f $Path | Write-Output;
            $script:LastStatusCode = -1; return;
        }
        if( !$PathExists ) { $script:LastStatusCode = 0; return; }
        
        Remove-Item $Path -Recurse *>&1 | Out-Null;
        if( $? -ne $true )
        {
            "Could not remove folder: '{0}'" -f $Path | Write-Output;
            $script:LastStatusCode = -1; return;
        }

        $script:LastStatusCode = 0; return;
    }
    function FileUtil_RemoveFiles
    {
        param( [string] $Path, [string] $Pattern )

        [bool] $PathExists = Test-Path $Path -PathType "Container";
        if( $? -ne $true )
        {
            "Could not test if folder exists: '{0}'" -f $Path | Write-Output;
            $script:LastStatusCode = -1; return;
        }
        if( !$PathExists ) { $script:LastStatusCode = 0; return; }

        # Warning: When it is used with the Include parameter, the Recurse parameter might not delete all subfolders or all child items. This is a known issue.
        # As a workaround, try piping results of the Get-ChildItem -Recurse command to Remove-Item, as described in "Example 4" in this topic.
        $Files = Get-ChildItem -Path $Path -Include $Pattern -File -Recurse;
        if( $? -ne $true )
        {
            "Could not get list of files to remove" | Write-Output;
            $script:LastStatusCode = -1; return;
        }

        [string[]] $CouldNotRemoveList = @();
        foreach( $File in $Files )
        {
            Remove-Item $File *>&1 | Out-Null;
            if( $? -ne $true ) { $CouldNotRemoveList += $File; }
        }

        if( $CouldNotRemoveList.Count -ne 0 )
        {
            "Could not remove files:`n{0}" -f $CouldNotRemoveList | Write-Output;
            $script:LastStatusCode = -1; return;
        }

        $script:LastStatusCode = 0; return;
    }
# }



class Stage
{
    [string] $Name = "default";      # stage name
    [scriptblock] $StageScript = $script:StageScript_Default;   # the stage script block to be executed
    [string[]] $CmdPartArr = @();    # the main command parts, used in some cases in the stage script
    [string[]] $CmdArgArr = @();     # +   the main command arguments
    [string] $WorkDir = $null;       # working directory that should be used to run the command
    [bool] $AcceptsArgs = $true;     # if the command accepts arguments
    [bool] $ShouldExec = $false;     # if the stage script should be executed

    # IMPORTANT: if $null is passed to string it gets converted to empty string ([string]::Empty == "")
    # +   https://github.com/PowerShell/PowerShell/issues/4616
    Stage(
        [string] $Name,
        [scriptblock] $StageScript,
        [string[]] $CmdPartArr,
        [string] $WorkDir,
        [bool] $AcceptsArgs )
    {
        $this.Name = $Name;
        $this.StageScript = $StageScript;
        $this.CmdPartArr = $CmdPartArr;
        $this.WorkDir = $WorkDir;
        $this.AcceptsArgs = $AcceptsArgs;
    }
    Stage(
        [string] $Name,
        [scriptblock] $StageScript,
        [string] $WorkDir,
        [bool] $AcceptsArgs )
    {
        $this.Name = $Name;
        $this.StageScript = $StageScript;
        $this.WorkDir = $WorkDir;
        $this.AcceptsArgs = $AcceptsArgs;
    }
    Stage(
        [string] $Name,
        [string[]] $CmdPartArr,
        [string] $WorkDir,
        [bool] $AcceptsArgs )
    {
        $this.Name = $Name;
        $this.CmdPartArr = $CmdPartArr;
        $this.WorkDir = $WorkDir;
        $this.AcceptsArgs = $AcceptsArgs;
    }

    [void] AddCmdArg( [string] $CmdArg )
    {
        $this.AddCmdArgs( $CmdArg );
    }
    [void] AddCmdArgs( [string[]] $CmdArgs )
    {
        $this.CmdArgArr += $CmdArgs;
    }

    [string] GetCommand()
    {
        return $this.GetCommandArr() -join ' ';
    }
    [string[]] GetCommandArr()
    {
        return $this.CmdPartArr + $this.CmdArgArr;
    }
}
    function Stage_ExecuteScript
    {
        param( [scriptblock] $Script, [Stage] $Stage )

      # [System.Collections.ArrayList] $OutputStream = $null;
      # [System.Collections.ArrayList] $ErrorStream = $null;

      # Invoke-Command -ScriptBlock $Script -ArgumentList $Stage -OutVariable 'OutputStream' -ErrorVariable 'ErrorStream';
      # $ReturnValue = $OutputStream[ $OutputStream.Count - 1 ];
      # $OutputStream.RemoveAt( $OutputStream.Count - 1 );

      # IMPORTANT: this doesn't work as a class method because the class method's output doesn't go to the output pipeline (only the return statement's output goes to the output pipeline)
      # +   https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_classes?view=powershell-7.2#output-in-class-methods
      # $OutputStream | Write-Output;
      # $ErrorStream | Write-Error;
        
        Invoke-Command -ScriptBlock $Script -ArgumentList $Stage;
    }
# }

class Pipeline
{
    [OrderedDictionary] $StageMap = $null;

    # IMPORTANT: hashtable should be ordered! ( using the ordered attribute, e.g. [ordered]@{...} )
    Pipeline( [OrderedDictionary] $StageMap )
    {
        $this.StageMap = $StageMap;
    }

    [Stage[]] StageList()
    {
        return $this.StageMap.Values;
    }

    [Stage] Stage( [string] $StageName )
    {
        return $this.StageMap[ $StageName ];
    }

    [int] StageIdx( [string] $StageName )
    {
        $i = 0;
        foreach( $Key in $this.StageMap.Keys )
        {
            if( $Key -eq $StageName ) { return $i; }
            $i++;
        }
        return -1;
    }
}

    function Pipeline_Execute
    {
        param( [Pipeline] $Pipeline )

        foreach( $Stage in $Pipeline.StageList() )
        {
            # skip the stage if it shouldn't be executed
            if( !$Stage.ShouldExec ) { continue; }

            # set current working directory to the one specified in the pipeline stage
            # IMPORTANT: if $null is passed to string it gets converted to empty string ([string]::Empty == "")
            # +   https://github.com/PowerShell/PowerShell/issues/4616
            if( $null -ne $Stage.WorkDir   -and   "" -ne $Stage.WorkDir )
            {
                Push-Location $Stage.WorkDir *>&1 | Out-Null;
                if( $? -ne $true )
                {
                    "Could not set the pipeline stage's working directory!" | Write-Output;
                    $script:LastStatusCode = 400; return;
                }
            }

            try
            {
                # execute the pipeline stage
                Stage_ExecuteScript $Stage.StageScript $Stage | Write-Output;
                if( $script:LastStatusCode -ne 0 ) { return; }
            }
            finally
            {
                # restore the previous working directory
                if( $null -ne $Stage.WorkDir   -and   "" -ne $Stage.WorkDir )
                {
                    Pop-Location *>&1 | Out-Null;
                    # if an error occured while restoring the previous woking directory, return
                    if( $? -ne $true )
                    {
                        "Could not restore the previous working directory!" | Write-Output;
                        $script:LastStatusCode = 400;
                    }
                }
            }

            # IMPORTANT: keep this line here since you cannot 'return' from the finally block
            if( $script:LastStatusCode -ne 0 ) { return; }
        }

        $script:LastStatusCode = 0; return;
    }
# }

# class Parser
# {
    function Parser_Parse
    {
        param( [Pipeline] $Pipeline, [string[]] $TokenArr )

        $CurrStage = $Pipeline.Stage( "=script" );
        $CurrStageIdx = 0;
        $PrevStageIdx = 0;
        $DefaultArgs_Idx = -1;

        switch -Regex ( $TokenArr )
        {
            '^='
            {
                if( $DefaultArgs_Idx -ge 0 )
                {
                    "No subcommands allowed after specifying '{0}'" -f '-def' | Write-Output;
                    $script:LastStatusCode = 400; return;
                }

                $CurrStage = $Pipeline.Stage( $_ );
                if( $null -eq $CurrStage )
                {
                    "Unknown subcommand: '{0}'" -f $_ | Write-Output;
                    $script:LastStatusCode = 400; return;
                }

                $CurrStageIdx = $Pipeline.StageIdx( $_ );
                if( $CurrStageIdx -le $PrevStageIdx )
                {
                    "Invalid placement for subcommand '{0}'; view help for subcommand ordering." -f $_ | Write-Output;
                    $script:LastStatusCode = 400; return;
                }

                $CurrStage.ShouldExec = $true;
                continue;
            }
            default
            {
                if( $CurrStageIdx -eq 0 )
                {
                    if( $_ -match '-\d+' )
                    {
                        if( $DefaultArgs_Idx -ge 0 )
                        {
                            "Cannot specify more than one default parameter list: '{0}'" -f $_ | Write-Output;
                            $script:LastStatusCode = 400; return;
                        }

                        $DefaultArgs_Idx = -( $_ -as [int] );
                        if( $DefaultArgs_Idx -lt 0   -or  $DefaultArgs_Idx -ge $script:DefaultArgs.Count )
                        {
                            "Unknown parameter: '{0}'" -f $_ | Write-Output;
                            $script:LastStatusCode = 400; return;
                        }

                        continue;
                    }

                    $CurrStage.ShouldExec = $true;
                }

                $CurrStage.AddCmdArg( $_ );
                continue;
            }
        }

        if( $DefaultArgs_Idx -ge 0 )
        {
            Parser_Parse $Pipeline $script:DefaultArgs[ $DefaultArgs_Idx ] | Write-Output;
            return;
        }
        
        $script:LastStatusCode = 0; return;
    }
# }





# ________________________________________________________________________________________________________________
# Resources

[Stage] $script:ScriptStg = [Stage]::new(
    "SCRIPT PARAMS",
    {
        param( [Stage] $Stage )
    
        # switch script parameters
        switch( $Stage.GetCommandArr() )
        {
            "--help"
            {
                $script:HelpMessage | Write-Output;
                continue;
            }
            "-help"
            {
                $script:HelpMessage | Write-Output;
                continue;
            }
            default
            {
                "Unknown parameter: {0}" -f $_ | Write-Output;
                $script:LastStatusCode = -1; return;
            }
        }

        $script:LastStatusCode = 0; return;
    },
    $null,
    $true
);

[Stage] $script:CupStg = [Stage]::new(
    "CUP",
    {
        param( [Stage] $Stage )

        # the abstract syntax tree's folder path
        $AstPath = "./rs/ac/bg/etf/pp1/ast"

        # rename the ast directory
        FileUtil_MoveItem "$AstPath" "$AstPath.old" | Write-Output;
        if( $script:LastStatusCode -ne 0 ) { return; }

        # invoke the default stage script on this stage
        Stage_ExecuteScript $script:StageScript_Default $Stage | Write-Output;
        if( $script:LastStatusCode -ne 0 )
        {
            FileUtil_RemoveFolder "$AstPath" | Write-Output;
            FileUtil_MoveItem "$AstPath.old", "$AstPath" | Write-Output;
            return;
        }

        # remove previously generated ast code
        FileUtil_RemoveFolder "$AstPath.old" | Write-Output;
        if( $script:LastStatusCode -ne 0 ) { return; }

        $script:LastStatusCode = 0; return;
    },
    @(
        # the current working directory should be the 'src' folder, since the ast_cup tool can't find the 'ast' folder otherwise
        # NOTE: cup command paths should be relative to the project 'src' folder
        "java",                                      # invoke jvm
        "-cp '../lib/cup_v10k.jar' java_cup.Main",   # call the CUP tool
        "-destdir './rs/ac/bg/etf/pp1'",             # set the parser destination directory
        "-parser 'Parser'",                          # set the parser file name
        "-interface -symbols 'ITokenCode'",          # generate a java interface! between cup and jflex (instead of a class), also set its name
        "-ast rs.ac.bg.etf.pp1.ast -buildtree",      # generate the abstract syntax tree classes in the given package
        "'../spec/mjparser.cup'"<#,#>                # set the CUP specification input file
        <#"-dump_grammar",#>                         # write cup grammar in human readable format to output
        <#"-dump_states"#>;                          # write cup states in human readable format to output
    ),
    "${script:ProjectRoot}/MJCompiler/src",
    $true
);

[Stage] $script:JflexStg = [Stage]::new(
    "JFLEX",
    @(
        # NOTE: jflex command paths should be relative to the project 'src' folder
        "java",                                      # invoke jvm
        "-cp '../lib/jflex-1.4.3.jar' JFlex.Main",   # call the jflex tool
        "-nobak",                                    # prevent backup files from being generated
        "-d './rs/ac/bg/etf/pp1'",                   # set the lexer output directory
        "'../spec/mjlexer.flex'"                     # set jflex specification location
    ),
    "${script:ProjectRoot}/MJCompiler/src",
    $true
);

[Stage] $script:ProjCleanStg = [Stage]::new(
    "PROJECT CLEAN",
    {
        param( [Stage] $Stage )

        # invoke the default stage script on this stage
        Stage_ExecuteScript $script:StageScript_Default $Stage | Write-Output;
        if( $script:LastStatusCode -ne 0 ) { return; }

        $ItemsToRemove = @(
            # remove 'ast.old' directory (cleanup unused code)
            [PSCustomObject]@{ Path="./MJCompiler/rs/ac/bg/etf/pp1/ast.old"; Filter=""; },
            # remove the generated cup specification files from the 'spec' directory
            [PSCustomObject]@{ Path="./MJCompiler/spec"; Filter="*_astbuild.cup"; },
            # remove 'logs' directory
            [PSCustomObject]@{ Path="./MJCompiler/logs";       Filter=""; },
            [PSCustomObject]@{ Path="./MJCompiler/.logs";      Filter=""; },
            [PSCustomObject]@{ Path="./MJCompiler/test/logs";  Filter=""; },
            [PSCustomObject]@{ Path="./MJCompiler/test/.logs"; Filter=""; },
            # remove all .lex, .par and .obj files from the test directory
            [PSCustomObject]@{ Path="./MJCompiler/test/build"; Filter=""; },
            [PSCustomObject]@{ Path="./MJCompiler/test"; Filter="*.lex"; },
            [PSCustomObject]@{ Path="./MJCompiler/test"; Filter="*.par"; },
            [PSCustomObject]@{ Path="./MJCompiler/test"; Filter="*.obj"; },
            # remove compiled java code directories
            [PSCustomObject]@{ Path ="./MJCompiler/bin";   Filter=""; },
            [PSCustomObject]@{ Path ="./MJCompiler/build"; Filter=""; },
            [PSCustomObject]@{ Path ="./MJCompiler/dist" ; Filter=""; }
        );

        foreach( $Item in $ItemsToRemove )
        {
            if( "" -eq $Item.Filter ) { FileUtil_RemoveFolder $Item.Path | Write-Output; }
            else                      { FileUtil_RemoveFiles $Item.Path $Item.Filter | Write-Output; }

            if( $script:LastStatusCode -ne 0 ) { return; }
        }

        $script:LastStatusCode = 0; return;
    },
    @(
        "ant -quiet",
        "-f ./MJCompiler",
        "-Dnb.internal.action.name='clean'"
    ),
    "${script:ProjectRoot}/",
    $false
);

# build project
[Stage] $script:ProjBuildStg = [Stage]::new(
    "PROJECT BUILD",
    @(
        "ant -quiet",
        "-f ./MJCompiler",
        "-Dnb.internal.action.name='build jar'"
    ),
    "${script:ProjectRoot}/",
    $false
);

# test project
[Stage] $script:ProjTestStg = [Stage]::new(
    "PROJECT TEST",
    {
        # NOTE: command paths should be relative to the project 'test' folder
        param( [Stage] $Stage )

        # print the stage name
        $script:StageSep -f $Stage.Name | Write-Output;

        # if the subcommand doesn't accept arguments but they were given anyway (if the subcommand is simple)
        # IMPORTANT: && and || are pipeline chain operators!, not logical operators (-and and -or)
        $CmdArgArr = $Stage.CmdArgArr;
        if( !$Stage.AcceptsArgs   -and   $CmdArgArr.Count -gt 0 )
        {
            "Subcommand does not accept arguments" | Write-Output;
            $script:LastStatusCode = 400; return;
        }

        # MicroJava compile and run scripts
        $MJCompileScript =
        {
            param( [string] $MJFilePath, [string] $ObjFilePath )
            $ScriptOutput = @( java -cp '../dist/MJCompiler.jar' 'rs.ac.bg.etf.pp1.Compiler' -o "$ObjFilePath" "$MJFilePath" *>&1 );
            $ScriptOutput += $LASTEXITCODE;   # append the exit code to the output
            Write-Output $ScriptOutput;
        }
        $MJRunScript =
        {
            param( [string] $ObjFilePath, [string] $TestInput )
            $ScriptOutput = @( $TestInput | java -cp '../lib/mj-runtime-1.1.jar' 'rs.etf.pp1.mj.runtime.Run' "$ObjFilePath" *>&1 );   # make the result into an array
            $ScriptOutput[ -1 ] = $LASTEXITCODE;   # replace the last output line (which is always present) with the exit code
            Write-Output $ScriptOutput;
        };
        # some unicode symbols used in printing
        $TestGroupSym = "❖";
        $TestResultSym = "`u{274C}", "`u{2705}";   # "🗴", "✓"
        # classes for working with the .json test batch
        class TestUnit
        {
            [string] $TestName;         # the name of the unit test for the given file
            [string[]] $TestInput;      # given input lines (separated by '\n')
            [string[]] $ExTestOutput;   # expected output lines (seperated by '\n')
            [int] $ExExitCode;          # expected exit code
        }
        class TestGroup
        {
            [string] $TestGroupName;       # the name of the test group for the given file
            [string] $FilePath;            # the file which will be compiled and unit tested
            [string[]] $ExCompileOutput;   # expected compile output
            [int] $ExExitCode;             # expected exit code
            [TestUnit[]] $TestUnitList;    # list of unit tests for the file
        }
        class TestBatch
        {
            [string] $BatchName;            # the name of the test batch
            [TestGroup[]] $TestGroupList;   # list of test groups
        }

        # get the list of test batch files
        $TestBatchFileList = Get-ChildItem -Path $Stage.WorkDir -Include "*.test.json" -File -Recurse;
        if( $? -ne $true )
        {
            "Could not get list of test batch files" | Write-Output;
            $script:LastStatusCode = -1; return;
        }


        # reset the last status code
        $script:LastStatusCode = 0;
        # for all test batches
        foreach( $TestBatchFile in $TestBatchFileList )
        {
            [TestBatch] $TestBatch = $null;
            [int] $TestBatchStatusCode = 0;
            try
            {
                # setup the json serializer options
                $JsonSerializerOptions = [JsonSerializerOptions]::new();
                $JsonSerializerOptions.AllowTrailingCommas = $true;
                $JsonSerializerOptions.ReadCommentHandling = [JsonCommentHandling]::Skip;
                
                # get the file's json contents
                $Json = Get-Content -Raw $TestBatchFile;
                $TestBatch = [JsonSerializer]::Deserialize( $Json, [TestBatch], $JsonSerializerOptions );
            }
            catch [JsonException]
            {
                $TestBatchStatusCode = -1;
                continue;
            }
            finally
            {
                # print the test batch name
                if( $TestBatchStatusCode -eq 0 )
                {
                    "{0}`n{1} {2}`n{0}" -f $script:LineSep, $TestGroupSym, $TestBatch.BatchName | Write-Output;
                }
                else
                {
                    "{0}`n{1} '{2}'`n{0}`n{3} Test batch's format is not valid .json" -f $script:LineSep, $TestGroupSym, $TestBatchFile.FullName, $TestResultSym[ $false ] | Write-Output;
                }
            }

            # for all test groups in the test batch
            foreach( $TestGroup in $TestBatch.TestGroupList )
            {
                # get the MJ file path relative to the project test folder, and the .obj file path which will be located in the .../test/build subdirectory
                $MJFilePath = "{0}/{1}" -f $TestBatchFile.Directory, $TestGroup.FilePath;
                $MJFilePath = [System.IO.Path]::GetRelativePath( $Stage.WorkDir, $MJFilePath ) -replace '\\', '/';
                $ObjFilePath = $MJFilePath -replace '.mj$', '.obj';
              # $ObjFilePath = "build/${ObjFilePath}";
                $ObjFilePath = "${ObjFilePath}";
                
                # start the test group's input file compilation as a background job
                [Job] $MJCompileJob = Start-Job -ScriptBlock $MJCompileScript -ArgumentList $MJFilePath, $ObjFilePath;
                # wait for the compilation to finish and get the compilation output
                Wait-Job $MJCompileJob -Timeout 5 | Out-Null;   # in seconds
                $CompileOutput = ( $MJCompileJob | Receive-Job ) -join "`n";
                # HACK: if there is an error with an empty message in the error stream, when calling ToString on the error it outputs its type as string
                # +   this happens a lot in the compiler, since it writes newlines on stderr
                $CompileOutput = $CompileOutput -replace "System.Management.Automation.RemoteException","";
                # stop the background job if it's still running
                Stop-Job $MJCompileJob;

                # get the expected compilation output
                $ExCompileOutput = $TestGroup.ExCompileOutput + $TestGroup.ExExitCode;
                $ExCompileOutput = $ExCompileOutput -join "`n";

                # if the compilation failed, save the status code
                if( $MJCompileJob.Error.Count -ne 0   -or   $CompileOutput -ne $ExCompileOutput )
                {
                    "{0} {1}" -f $TestResultSym[ $false ], $TestGroup.TestGroupName | Write-Output;
                    $script:LastStatusCode = -1; continue;
                }

                # if there are no tests in the test group, then only the compilation was tested, continue
                if( $TestGroup.TestUnitList.Count -eq 0 )
                {
                    "{0} {1}" -f $TestResultSym[ $true ], $TestGroup.TestGroupName | Write-Output;
                    continue;
                }


                # for all unit tests in the test group
                foreach( $TestUnit in $TestGroup.TestUnitList )
                {
                    # start the test with the given input
                    [Job] $MJRunJob = Start-Job -ScriptBlock $MJRunScript -ArgumentList $ObjFilePath, $TestUnit.TestInput;
                    # wait for the test to finish and get its output
                    Wait-Job $MJRunJob -Timeout 5 | Out-Null;   # in seconds
                    $TestOutput = ( $MJRunJob | Receive-Job ) -join "`n";
                    # HACK: if there is an error with an empty message in the error stream, when calling ToString on the error it outputs its type as string
                    $TestOutput = $TestOutput -replace "System.Management.Automation.RemoteException","";
                    # stop the background job if it's still running
                    Stop-Job $MJRunJob;

                    # get the expected test output
                    $ExTestOutput = $TestUnit.ExTestOutput + $TestGroup.ExExitCode;
                    $ExTestOutput = $ExTestOutput -join "`n";
                    # save the test exit code and update the stage status code
                    $TestExitCode = ( $MJRunJob.Error.Count -ne 0 -or $TestOutput -ne $ExTestOutput ) ? -1 : 0;
                    if( $TestExitCode -ne 0 ) { $script:LastStatusCode = -1; }

                    # print the test results
                    "{0} {1}" -f $TestResultSym[ $TestExitCode -eq 0 ], $TestUnit.TestName | Write-Output;
                }
            }
        }
    },
    "${script:ProjectRoot}/MJCompiler/test",
    $false
);

# compile the given MicroJava source code
[Stage] $script:MJCompileStg = [Stage]::new(
    "MJ COMPILE",
    @(
        # NOTE: command paths should be relative to the project 'test' folder
        "java",
        "-cp '../dist/MJCompiler.jar' rs.ac.bg.etf.pp1.Compiler"
    ),
    "${script:ProjectRoot}/MJCompiler/test",
    $true
);

# disassemble the given MicroJava .obj file
[Stage] $script:MJDisasmStg = [Stage]::new(
    "MJ DISASSEMBLE",
    @(
        # NOTE: command paths should be relative to the project 'test' folder
        "java",
        "-cp '../lib/mj-runtime-1.1.jar' rs.etf.pp1.mj.runtime.disasm"
    ),
    "${script:ProjectRoot}/MJCompiler/test",
    $true
);

# run the given MicroJava compiled code
[Stage] $script:MJRunStg = [Stage]::new(
    "MJ RUN",
    @(
        # NOTE: command paths should be relative to the project 'test' folder
        "java",
        "-cp '../lib/mj-runtime-1.1.jar' rs.etf.pp1.mj.runtime.Run"
    ),
    "${script:ProjectRoot}/MJCompiler/test",
    $true
);

[Pipeline] $script:Pipeline = [Pipeline]::new(
    [ordered]@{
        "=script"  = $ScriptStg
        "=cup"     = $CupStg
        "=jflex"   = $JflexStg
        "=clean"   = $ProjCleanStg
        "=build"   = $ProjBuildStg
        "=test"    = $ProjTestStg
        "=compile" = $MJCompileStg
        "=disasm"  = $MJDisasmStg
        "=run"     = $MJRunStg
    }
);





# ________________________________________________________________________________________________________________
# Script

# main function
function Build-V2
{
    $ScriptArgs = $args.Count -ne 0 ? $args : $( "--help" );

    Parser_Parse $script:Pipeline $ScriptArgs;
    if( $script:LastStatusCode -ne 0 ) { return; }
    
    Pipeline_Execute $script:Pipeline;
    if( $script:LastStatusCode -ne 0 ) { return; }
}

# call the build script
# +   @ - array splatting operator; used here to pass script arguments to the build function
Build-V2 @args *>&1 | Tee-Object -FilePath "${script:ProjectRoot}/build.log" -Append;
# exit with the last exit code
exit $script:LastStatusCode;




