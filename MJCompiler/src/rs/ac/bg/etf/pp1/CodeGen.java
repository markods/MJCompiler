package rs.ac.bg.etf.pp1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.mj.runtime.Run;
import rs.etf.pp1.mj.runtime.disasm;

public class CodeGen
{
    public static final int NO_ADDRESS = 0;
    public static final int FALSE = 0;
    public static final int TRUE  = 1;

    private static final ByteBuffer code = ByteBuffer.wrap( Code.buf ).order( ByteOrder.BIG_ENDIAN );
    // constant copied over from class Code
    private static final int codesz = 8192;
    
    public static int _pc32() { return Code.pc; }
    public static void _pc32( int value_32 ) { Code.pc = value_32; }
    private static int _pc32Inc( int amount ) { int pc = Code.pc; Code.pc += amount; return pc; }

    private static int _entryAddr32() { return Code.mainPc; }
    private static void _entryAddr32( int value_32 ) { Code.mainPc = value_32; }
    public static void _entryAddr32Set() { Code.mainPc = Code.pc; }

    private static int _staticSize32() { return Code.dataSize; }
    private static void _staticSize32( int value_32 ) { Code.dataSize = value_32; }

    static
    {
        init( 0 );
    }

    // initialize the code generator
    public static void init( int staticSize )
    {
        // initialize registers
        _pc32( 0 );
        _entryAddr32( 0 );
        _staticSize32( staticSize );

        // initialize the code segment
        // +   don't write the header here, because the microjava virtual machine expects the code to start at 0 (the microjava header is prepended at the end of compilation)
        // /*00*/put8( 'M' );
        // /*01*/put8( 'J' );
        // /*02*/put32( codeSize );
        // /*06*/put32( _staticSize32() );
        // /*10*/put32( _entryAddr32() );
    }



    // report an error
    private static void report_basic( String message )
    {
        report_error( message, false );
    }
    // report an error and throw an exception
    private static void report_fatal( String message )
    {
        report_error( message, true );
    }
    // report an error and throw an exception if requested
    private static void report_error( String message, boolean throwError )
    {
        Compiler.errors.add( CompilerError.COMPILE_ERROR, message );
        if( throwError ) throw Compiler.errors.getLast();
    }



    // return the compiled code as a string
    public static byte[] compile()
    {
        byte[] output = null;
        int codeSize = _pc32();

        try( ByteArrayOutputStream buffer = new ByteArrayOutputStream(); )
        {
            // HACK: append the header to the end of the file (the pc, static segment size and the program entry point)
            // +   first output the header, and then the actual file
            // +   afterwards, restore the old pc value

            /*00*/put8( 'M' );
            /*01*/put8( 'J' );
            /*02*/put32( codeSize );
            /*06*/put32( _staticSize32() );
            /*10*/put32( _entryAddr32() );

            // write the header and actual code to the buffer
            buffer.write( Code.buf, codeSize, _pc32() - codeSize );
            buffer.write( Code.buf, 0, codeSize );

            // return the buffer as a byte array
            output = buffer.toByteArray();
        }
        catch( IOException ex )
        {
            Compiler.logger.error( "Compilation unsuccessful", ex );
            return null;
        }
        finally
        {
            // reset the pc to its previous value
            _pc32( codeSize );
        }

        return output;
    }

    // return the decompiled code as a string
    public static String decompile( File fOutput )
    {
        String output = "";
        
        try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDOUT, buffer );
        )
        {
            // workaround since disasm method only outputs to System.out
            disasm.main( new String[] { fOutput.getAbsolutePath() } );
            output = buffer.toString( "UTF-8" );
        }
        catch( Exception ex )
        {
            Compiler.logger.error( "Decompilation unsuccessful", ex );
            return "";
        }
        
        return output;
    }
    // run the code on the microjava virtual machine and return the output as string
    public static String runCode( File fOutput, boolean debug )
    {
        String output = "";
        
        try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDOUT, buffer );
        )
        {
            // workaround since disasm method only outputs to System.out
            if( debug ) Run.main( new String[] { fOutput.getAbsolutePath(), "-debug" } );
            else        Run.main( new String[] { fOutput.getAbsolutePath() } );
            output = buffer.toString( "UTF-8" );
        }
        catch( Exception ex )
        {
            Compiler.logger.error( "Decompilation unsuccessful", ex );
            return "";
        }
        
        return output;
    }



    
    
    // ________________________________________________________________________________________________
    // convenience methods


    // append the given byte (i8) to the code segment
    private static void put8( int value_8 )
    {
        try
        {
            code.put( _pc32Inc( 1/*B*/ ), ( byte )value_8 );
            if( _pc32() < codesz ) return;
        }
        catch( BufferOverflowException ex )
        {}

        report_fatal( String.format( "Code segment overflow (MJ virtual machine allows %d bytes of instructions)", codesz ) );
    }
    // append the given short (i16) to the code segment
    private static void put16( int value_16 )
    {
        try
        {
            code.putShort( _pc32Inc( 2/*B*/ ), ( short )value_16 );
            if( _pc32() < codesz ) return;
        }
        catch( BufferOverflowException ex )
        {}

        report_fatal( String.format( "Code segment overflow (MJ virtual machine allows %d bytes of instructions)", codesz ) );
    }
    // append the given word (i32) to the code segment
    private static void put32( int value_32 )
    {
        try
        {
            code.putInt( _pc32Inc( 4/*B*/ ), value_32 );
            if( _pc32() < codesz ) return;
        }
        catch( BufferOverflowException ex )
        {}

        report_fatal( String.format( "Code segment overflow (MJ virtual machine allows %d bytes of instructions)", codesz ) );
    }
    // overwrite the given short (i16) in the code segment with the given value
    private static void put16( int address_16, int value_16 )
    {
        try
        {
            code.putShort( address_16, ( short )value_16 );
            return;
        }
        catch( BufferOverflowException ex )
        {}

        report_fatal( String.format( "Code segment overflow (MJ virtual machine allows %d bytes of instructions)", codesz ) );
    }

    // get the byte (i8) from the given address in the code segment
    private static int get8( int address_16 )
    {
        return code.get( address_16 );
    }
    // get the short (i16) from the given address in the code segment
    private static int get16( int address_16 )
    {
        return code.getShort( address_16 );
    }
    // get the word (i32) from the given address in the code segment
    private static int get32( int address_16 )
    {
        return code.getInt( address_16 );
    }



    // load the symbol's value onto the expression stack
    public static int loadSymbolValue( Symbol symbol )
    {
        // +   the <CONST case> also works for constants 'null', 'this' and 'super', since their values are 0
        // +   the <ARRAY_ELEM case> requires that the array element's index was previously loaded onto the expression stack
        // +   the <FIELD case> also works for @pVirtualTable (field whose value is the virtual table's starting address)
        // +   the <TYPE case> does nothing, since this is a way to access the class's static fields outside the class's scope
        switch( symbol._kind() )
        {
            case Symbol.CONST: if( symbol.isThis()
                                || symbol.isSuper() )  { return loadLocal  ( symbol._value()/*0*/); }
                               else                    { return loadConst  ( symbol._value()     ); }
            case Symbol.VAR:   if( symbol.isGlobal() ) { return i_getstatic( symbol._address()   ); }
                               else                    { return loadLocal  ( symbol._varIdx()    ); }
            case Symbol.FORMAL_PARAM:                  { return loadLocal  ( symbol._varIdx()    ); }
            case Symbol.FIELD:                         { return i_getfield ( symbol._memberIdx() ); }
            case Symbol.STATIC_FIELD:                  { return i_getstatic( symbol._address()   ); }
            case Symbol.METHOD:                        { return /*ignore symbol*/_pc32(); }
            case Symbol.STATIC_METHOD:                 { return /*ignore symbol*/_pc32(); }
            case Symbol.FUNCTION:                      { return /*ignore symbol*/_pc32(); }
            case Symbol.TYPE:                          { return /*ignore symbol*/_pc32(); }
            case Symbol.ARRAY_ELEM:                    { return loadArrayElem( symbol._type().isChar() ); }
            /* miscellaneous symbol types */
         // case Symbol.ACTIV_PARAM:                   { return /*not allowed*/; }
         // case Symbol.PROGRAM:                       { return /*not allowed*/; }

            default: { report_fatal( "Symbol not supported or doesn't have a value for loading onto the expression stack" ); return _pc32(); }
        }
    }
    // check if the symbol needs designation by another symbol (whose value must be placed onto the expression stack in order for this symbol to be accessed)
    public static boolean needsPrevDesignatorValue( Symbol symbol )
    {
        // +   the <CONST case> also works for constants 'null', 'this' and 'super', since their values are 0
        // +   the <ARRAY_ELEM case> requires that the array element's index was previously loaded onto the expression stack
        // +   the <FIELD case> also works for @pVirtualTable (field whose value is the virtual table's starting address)
        // +   the <TYPE case> does nothing, since this is a way to access the class's static fields outside the class's scope
        switch( symbol._kind() )
        {
            case Symbol.CONST:         { return false; }
            case Symbol.VAR:           { return false; }
            case Symbol.FORMAL_PARAM:  { return false; }
            case Symbol.FIELD:         { return true;  }
            case Symbol.STATIC_FIELD:  { return false; }
            case Symbol.METHOD:        { return true;  }
            case Symbol.STATIC_METHOD: { return false; }
            case Symbol.FUNCTION:      { return false; }
            case Symbol.TYPE:          { return false; }
            case Symbol.ARRAY_ELEM:    { return true;  }
            /* miscellaneous symbol types */
         // case Symbol.ACTIV_PARAM:   { return /*not allowed*/; }
         // case Symbol.PROGRAM:       { return /*not allowed*/; }

            default: { report_fatal( "Symbol not supported" ); return false; }
        }
    }
    // store the value on the expression stack into the given symbol
    // +   used as the last part of the <loadSymbolValue method call> sequence in the designator
    public static int storeSymbolValue( Symbol symbol )
    {
        // +   the <ARRAY_ELEM case> requires that the array element's index was previously loaded onto the expression stack
        switch( symbol._kind() )
        {
         // case Symbol.CONST:                         { return /*not allowed*/; }
            case Symbol.VAR:   if( symbol.isGlobal() ) { return i_putstatic( symbol._address()   ); }
                               else                    { return storeLocal ( symbol._varIdx()    ); }
            case Symbol.FORMAL_PARAM:                  { return storeLocal ( symbol._varIdx()    ); }
            case Symbol.FIELD:                         { return i_putfield ( symbol._memberIdx() ); }
            case Symbol.STATIC_FIELD:                  { return i_putstatic( symbol._address()   ); }
         // case Symbol.METHOD:                        { return /*not allowed*/; }
         // case Symbol.STATIC_METHOD:                 { return /*not allowed*/; }
         // case Symbol.FUNCTION:                      { return /*not allowed*/; }
         // case Symbol.TYPE:                          { return /*not allowed*/; }
            case Symbol.ARRAY_ELEM:                    { return storeArrayElem( symbol._type().isChar() ); }
            /* miscellaneous symbol types */
         // case Symbol.ACTIV_PARAM:                   { return /*not allowed*/; }
         // case Symbol.PROGRAM:                       { return /*not allowed*/; }

            default: { report_fatal( "Symbol not supported or doesn't have a location for storing the expression stack's value" ); return _pc32(); }
        }
    }



    // get the class's virtual table size in words (i32)
    public static int getVirtualTableSize( Symbol classSymbol )
    {
        // size of the virtual table
        int vtSize = 0;

        // for all the class's methods
        for( Symbol method : classSymbol._type()._methods() )
        {
            // if the method is not explicitly callable, skip it
            if( method.isDummySym() ) continue;

            // reserve an entry in the class's virtual table
            // +   reserve one word (i32) for each char in the method name
            // +   reserve one word for -1 at the end of the method name
            // +   reserve one word for the method's address
            vtSize += method._name().length() + 1 + 1;
        }
        // add the -2 at the end of the virtual table
        vtSize++;

        // return the size of the virtual table
        return vtSize;
    }
    // initialize the class's virtual table
    // +   call this method when the address for every class's method is known
    public static void initVirtualTable( Symbol classSymbol )
    {
        // get the virtual table pointer's starting address from the class symbol
        int pVirtualTable = classSymbol._address();

        // for all the class's methods
        for( Symbol method : classSymbol._type()._methods() )
        {
            // if the method is not explicitly callable, skip it
            if( method.isDummySym() ) continue;
            
            // for all characters in the method name
            String methodName = method._name();
            for( int i = 0; i < methodName.length(); i++ )
            {
                // save the current character of the method name in the virtual table
                loadConst( methodName.charAt( i ) ); i_putstatic( pVirtualTable++ );
            }
            // end the method name with -1
            loadConst( -1 ); i_putstatic( pVirtualTable++ );

            // save the method's address in the virtual table
            loadConst( method._address() ); i_putstatic( pVirtualTable++ );
        }

        // end the virtual table with -2
        loadConst( -2 ); i_putstatic( pVirtualTable++ );
    }


    // set the source jump's offset so that after jumping we end up at the destination instruction
    public static void fixJumpOffset( int srcAddress_16, int destAddress_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        // +   address + 1 because the first byte is the instruction's opcode
        put16( srcAddress_16+1, destAddress_16 - srcAddress_16 );
    }
    // set the jump's offset so that after jumping we end up at the instruction the current pc points to
    public static void fixJumpOffset( int srcAddress_16 )
    {
        fixJumpOffset( srcAddress_16, _pc32() );
    }

    
    
    

    // ________________________________________________________________________________________________
    // legend

    //          CODE (i8)                STATIC (i32)             HEAP (i32)               STACK (i32)              EXPR STACK (i32)
    //          ---------                ---------                ---------                ---------                ---------       
    //    code> | instr |          data> | statA |          heap> | A[0]  |        pstack> |  ...  |        estack> | instr |       
    //          | operA |                | statB |                | A[1]  |                | arg[0]|                | operA |       
    //          |  ...  |                |  ...  |                |  ...  |          fp-1> | &ret  |                |  ...  |       
    //          | instr |                | C.a   |                | c.a   |            fp> | &dl   |                | instr |       
    //          ---------                ---------                ---------                ---------                ---------       
    //      pc> |       |                |       |          free> |       |            sp> |       |                | res   |       
    //          |       |                |       |                |       |                |       |           esp> |       |       
    //          v                        v                        v                        v                        v               

    // Code:
    // +   contains function and method code
    // +   pc -- register that contains the current executed instruction (in runtime)
    // +   mainpc -- register that contains the program starting address (void main();)
    // Static:
    // +   contains the program's global and class' static data
    // Heap:
    // +   contains dynamically allocated objects/arrays
    // +   currently no garbage collection or deallocation (deallocation happens when the program exits)
    // +   arrays begin with an invisible i32 constant (arr[-1]) that contains the size of the array
    // +   char[] arrays begin with the i32 constant, but their elements are i8
    // +   variables on the heap are always initialized with 0|'\0'|nullType when allocated
    // Stack:
    // +   contains function call frames
    // +   the newly 'allocated' stack frame is initialized with zeros
    // +   ret -- return address
    // +   dl -- dynamic link <=> previous frame pointer saved to the stack
    // +   fp -- register that contains the frame pointer (base pointer); points to the dynamic link
    // +   sp -- register that contains the stack pointer
    // Expression stack:
    // +   used for calculating the result of an expression, as well as holding the callee return value for the caller
    // +   also used for passing caller arguments to the callee
    // +   res -- expression/callee result

    // Operand size:
    // b - byte  (8  bits)
    // s - short (16 bits)
    // w - word  (32 bits)

    private static final int
    /*        = 0*/    i_store_3   = 10,     i_const_5  = 20,     i_shr         = 30,     i_dup  = 40,     i_return        = 50,     i_dup_x2 = 60,
    i_load    = 1,     i_getstatic = 11,     i_const_m1 = 21,     i_inc         = 31,     i_dup2 = 41,     i_enter         = 51,
    i_load_0  = 2,     i_putstatic = 12,     i_const    = 22,     i_new         = 32,     i_jmp  = 42,     i_exit          = 52,
    i_load_1  = 3,     i_getfield  = 13,     i_add      = 23,     i_newarray    = 33,     i_jeq  = 43,     i_read          = 53,
    i_load_2  = 4,     i_putfield  = 14,     i_sub      = 24,     i_aload       = 34,     i_jne  = 44,     i_print         = 54,
    i_load_3  = 5,     i_const_0   = 15,     i_mul      = 25,     i_astore      = 35,     i_jlt  = 45,     i_bread         = 55,
    i_store   = 6,     i_const_1   = 16,     i_div      = 26,     i_baload      = 36,     i_jle  = 46,     i_bprint        = 56,
    i_store_0 = 7,     i_const_2   = 17,     i_rem      = 27,     i_bastore     = 37,     i_jgt  = 47,     i_trap          = 57,
    i_store_1 = 8,     i_const_3   = 18,     i_neg      = 28,     i_arraylength = 38,     i_jge  = 48,     i_invokevirtual = 58,
    i_store_2 = 9,     i_const_4   = 19,     i_shl      = 29,     i_epop        = 39,     i_call = 49,     i_dup_x1        = 59;





    // ________________________________________________________________________________________________
    // instructions
    // IMPORTANT: all instructions return their starting address in the code segment

    ////// accessing local variables (from the stack frame)

    // 1    load b     ExprStack=[... -> ..., val]    epush(fp[b]);
    public static int i_load( int localIdx_8 ) { int pc32 = _pc32(); put8( i_load ); put8( localIdx_8 ); return pc32; }

    // 2    load_0     ExprStack=[... -> ..., val]    epush(fp[0]);
    public static int i_load_0() { int pc32 = _pc32(); put8( i_load_0 ); return pc32; }
    // 3    load_1     ExprStack=[... -> ..., val]    epush(fp[1]);
    public static int i_load_1() { int pc32 = _pc32(); put8( i_load_1 ); return pc32; }
    // 4    load_2     ExprStack=[... -> ..., val]    epush(fp[2]);
    public static int i_load_2() { int pc32 = _pc32(); put8( i_load_2 ); return pc32; }
    // 5    load_3     ExprStack=[... -> ..., val]    epush(fp[3]);
    public static int i_load_3() { int pc32 = _pc32(); put8( i_load_3 ); return pc32; }

    // convenience method for loading a word (i32) from the stack frame onto the expression stack
    public static int loadLocal( int localIdx_8 )
    {
        switch( localIdx_8 )
        {
            case  0: return i_load_0();
            case  1: return i_load_1();
            case  2: return i_load_2();
            case  3: return i_load_3();
            default: return i_load( localIdx_8 );
        }
    }


    // 6    store b    ExprStack=[..., val -> ...]    fp[b] = epop();
    public static int i_store( int localIdx_8 ) { int pc32 = _pc32(); put8( i_store ); put8( localIdx_8 ); return pc32; }

    // 7    store_0    ExprStack=[..., val -> ...]    fp[0] = epop();
    public static int i_store_0() { int pc32 = _pc32(); put8( i_store_0 ); return pc32; }
    // 8    store_1    ExprStack=[..., val -> ...]    fp[1] = epop();
    public static int i_store_1() { int pc32 = _pc32(); put8( i_store_1 ); return pc32; }
    // 9    store_2    ExprStack=[..., val -> ...]    fp[2] = epop();
    public static int i_store_2() { int pc32 = _pc32(); put8( i_store_2 ); return pc32; }
    // 10   store_3    ExprStack=[..., val -> ...]    fp[3] = epop();
    public static int i_store_3() { int pc32 = _pc32(); put8( i_store_3 ); return pc32; }

    // convenience method for storing a word (i32) from the expression stack to the stack frame
    public static int storeLocal( int localIdx_8 )
    {
        switch( localIdx_8 )
        {
            case  0: return i_store_0();
            case  1: return i_store_1();
            case  2: return i_store_2();
            case  3: return i_store_3();
            default: return i_store( localIdx_8 );
        }
    }



    ////// accessing global and static variables

    // 11   getstatic s    ExprStack=[... -> ..., val]    epush(data[s]);
    public static int i_getstatic( int staticAddress_16 ) { int pc32 = _pc32(); put8( i_getstatic ); put16( staticAddress_16 ); return pc32; }
    // 12   putstatic s    ExprStack=[..., val -> ...]    data[s] = epop();
    public static int i_putstatic( int staticAddress_16 ) { int pc32 = _pc32(); put8( i_putstatic ); put16( staticAddress_16 ); return pc32; }



    ////// accessing class members

    // 13   getfield s    ExprStack=[..., adr -> ..., val]    adr = epop()/4; epush(heap[adr+s]);
    public static int i_getfield( int fieldIdx_16 ) { int pc32 = _pc32(); put8( i_getfield ); put16( fieldIdx_16 ); return pc32; }
    // 14   putfield s    ExprStack=[..., adr, val -> ...]    val = epop(); adr = epop()/4; heap[adr+s] = val;
    public static int i_putfield( int fieldIdx_16 ) { int pc32 = _pc32(); put8( i_putfield ); put16( fieldIdx_16 ); return pc32; }

    // 32   new s         ExprStack=[... -> ..., adr]       <alloc s bytes>; <initialize with 0>; epush(adr( &start ));
    public static int i_new( int byteCount_16/*B*/ ) { int pc32 = _pc32(); put8( i_new ); put16( byteCount_16 ); return pc32; }



    ////// accessing array elements

    // 34   aload          ExprStack=[..., adr, index -> ..., val]    i = epop(); adr = epop()/4+1; epush(heap[adr+i]);                          // load array element + bounds checking
    public static int i_aload() { int pc32 = _pc32(); put8( i_aload ); return pc32; }
    // 35   astore         ExprStack=[..., adr, index, val -> ...]    val = epop(); i = epop(); adr = epop()/4+1; heap[adr+i] = val;             // store array element + bounds checking
    public static int i_astore() { int pc32 = _pc32(); put8( i_astore ); return pc32; }
    // 36   baload         ExprStack=[..., adr, index -> ..., val]    i = epop(); adr = epop()/4+1; x = heap[adr+i/4]; epush(<byte i%4 of x>);   // load byte array element + bounds checking
    public static int i_baload() { int pc32 = _pc32(); put8( i_baload ); return pc32; }
    // 37   bastore        ExprStack=[..., adr, index, val -> ...]    val = epop(); i = epop(); adr = epop()/4+1; x = heap[adr+i/4]; <set byte i%4 in x>; heap[adr+i/4] = x;   // store byte array element + bounds checking
    public static int i_bastore() { int pc32 = _pc32(); put8( i_bastore ); return pc32; }

    // convenience method for loading the given array element on the expression stack
    public static int loadArrayElem( boolean isCharArray )
    {
        if( !isCharArray ) return i_aload();
        else               return i_baload();
    }
    // convenience method for storing the value on the expression stack to the given array element
    public static int storeArrayElem( boolean isCharArray )
    {
        if( !isCharArray ) return i_astore();
        else               return i_bastore();
    }
    
    // 38   arraylength    ExprStack=[..., adr -> ..., len]           adr = epop(); epush(heap[adr]);
    public static int i_arraylength() { int pc32 = _pc32(); put8( i_arraylength ); return pc32; }

    // 33   newarray b     ExprStack=[..., n -> ..., adr]             n = epop(); arr = (b != 0) ? <alloc n*i32> : <alloc n*i8>; epush(adr( arr ));
    public static int i_newarray( boolean isCharArray ) { int pc32 = _pc32(); put8( i_newarray ); put8( ( !isCharArray ) ? 1 : 0 ); return pc32; }



    ////// accessing constants

    // 15   const_0    ExprStack=[... -> ...,  0]    epush(0);
    public static int i_const_0() { int pc32 = _pc32(); put8( i_const_0 ); return pc32; }
    // 16   const_1    ExprStack=[... -> ...,  1]    epush(1);
    public static int i_const_1() { int pc32 = _pc32(); put8( i_const_1 ); return pc32; }
    // 17   const_2    ExprStack=[... -> ...,  2]    epush(2);
    public static int i_const_2() { int pc32 = _pc32(); put8( i_const_2 ); return pc32; }
    // 18   const_3    ExprStack=[... -> ...,  3]    epush(3);
    public static int i_const_3() { int pc32 = _pc32(); put8( i_const_3 ); return pc32; }
    // 19   const_4    ExprStack=[... -> ...,  4]    epush(4);
    public static int i_const_4() { int pc32 = _pc32(); put8( i_const_4 ); return pc32; }
    // 20   const_5    ExprStack=[... -> ...,  5]    epush(5);
    public static int i_const_5() { int pc32 = _pc32(); put8( i_const_5 ); return pc32; }
    
    // 21   const_m1   ExprStack=[... -> ..., -1]    epush(-1);
    public static int i_const_m1() { int pc32 = _pc32(); put8( i_const_m1 ); return pc32; }

    // 22   const w    ExprStack=[... -> ..., val]   epush(w)
    public static int i_const( int value_32 ) { int pc32 = _pc32(); put8( i_const ); put32( value_32 ); return pc32; }

    // convenience method for loading a constant onto the expression stack
    public static int loadConst( int value_32 )
    {
        switch( value_32 )
        {
            case -1: return i_const_m1();
            case  0: return i_const_0();
            case  1: return i_const_1();
            case  2: return i_const_2();
            case  3: return i_const_3();
            case  4: return i_const_4();
            case  5: return i_const_5();
            default: return i_const( value_32 );
        }
    }



    ////// arithmetic operations

    // 23   add           ExprStack=[..., valA, valB -> ..., valA+valB]    epush(epop() + epop());
    public static int i_add() { int pc32 = _pc32(); put8( i_add ); return pc32; }
    // 24   sub           ExprStack=[..., valA, valB -> ..., valA‐valB]    epush(‐epop() + epop());
    public static int i_sub() { int pc32 = _pc32(); put8( i_sub ); return pc32; }
    // 25   mul           ExprStack=[..., valA, valB -> ..., valA*valB]    epush(epop() * epop());
    public static int i_mul() { int pc32 = _pc32(); put8( i_mul ); return pc32; }
    // 26   div           ExprStack=[..., valA, valB -> ..., valA/valB]    x = epop(); epush(epop() / x);
    public static int i_div() { int pc32 = _pc32(); put8( i_div ); return pc32; }
    // 27   rem           ExprStack=[..., valA, valB -> ..., valA%valB]    x = epop(); epush(epop() % x);
    public static int i_rem() { int pc32 = _pc32(); put8( i_rem ); return pc32; }
    // 28   neg           ExprStack=[..., val -> ..., ‐val]                epush(‐epop());
    public static int i_neg() { int pc32 = _pc32(); put8( i_neg ); return pc32; }
    // 29   shl           ExprStack=[..., val -> ..., val<<1]              x = epop(); epush(epop() << x);   // signed! shift left
    public static int i_shl() { int pc32 = _pc32(); put8( i_shl ); return pc32; }
    // 30   shr           ExprStack=[..., val -> ..., val>>1]              x = epop(); epush(epop() >> x);   // signed! shift right
    public static int i_shr() { int pc32 = _pc32(); put8( i_shr ); return pc32; }
    // 31   inc b1, b2    ExprStack=[... -> ...]                           fp[b1] = fp[b1] + b2;             // works for local variables (on the stack frame)
    public static int i_inc( int localIdx_8, int by_8 ) { int pc32 = _pc32(); put8( i_inc ); put8( localIdx_8 ); put8( by_8 ); return pc32; }

    // 39   epop      ExprStack=[..., val -> ...]                                   dummy = epop();
    public static int i_epop()   { int pc32 = _pc32(); put8( i_epop   ); return pc32; }
    // 40   dup       ExprStack=[..., val -> ..., val, val]                         x = epop(); epush(x); epush(x);
    public static int i_dup()    { int pc32 = _pc32(); put8( i_dup    ); return pc32; }
    // 41   dup2      ExprStack=[..., v1, v2 -> ..., v1, v2, v1, v2]                y = epop(); x = epop(); epush(x); epush(y); epush(x); epush(y);
    public static int i_dup2()   { int pc32 = _pc32(); put8( i_dup2   ); return pc32; }
    // 59   dup_x1    ExprStack=[...,valA, valB -> ...,valB, valA, valB]            y = epop(); x = epop(); epush(y); epush(x); epush(y);
    public static int i_dup_x1() { int pc32 = _pc32(); put8( i_dup_x1 ); return pc32; }
    // 60   dup_x2    ExprStack=[valA, valB, valC -> ...,valC, valA, valB, valC]    z = epop(); y = epop(); x = epop(); epush(z); epush(x); epush(y); epush(z);
    public static int i_dup_x2() { int pc32 = _pc32(); put8( i_dup_x2 ); return pc32; }



    ////// jumps
    // IMPORTANT: the jump address is relative to the first byte of the jump instruction (more like the branch instruction on real cpu-s)

    // 42   jmp s                                          pc = pc + s;
    public static int i_jmp( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jmp ); put16( pcOffset_16 ); return pc32; }

    // 43   jeq s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static int i_jeq( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jeq ); put16( pcOffset_16 ); return pc32; }
    // 44   jne s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static int i_jne( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jne ); put16( pcOffset_16 ); return pc32; }
    // 45   jlt s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static int i_jlt( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jlt ); put16( pcOffset_16 ); return pc32; }
    // 46   jle s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static int i_jle( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jle ); put16( pcOffset_16 ); return pc32; }
    // 47   jgt s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static int i_jgt( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jgt ); put16( pcOffset_16 ); return pc32; }
    // 48   jge s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static int i_jge( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_jge ); put16( pcOffset_16 ); return pc32; }

    // convenience method for jumping to an absolute location
    public static int jump( int address_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        int pcOffset_16 = address_16 - _pc32();
        return i_jmp( pcOffset_16 );
    }

    // convenience method for jumping to an absolute location if the current comparison on the expression stack is true
    public static int jumpIf( int relop, int address_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        int pcOffset_16 = address_16 - _pc32();
        switch( relop )
        {
            case TokenCode.eq:  return i_jeq( pcOffset_16 );
            case TokenCode.ne:  return i_jne( pcOffset_16 );
            case TokenCode.lt:  return i_jlt( pcOffset_16 );
            case TokenCode.le:  return i_jle( pcOffset_16 );
            case TokenCode.gt:  return i_jgt( pcOffset_16 );
            case TokenCode.ge:  return i_jge( pcOffset_16 );
            default: { report_fatal( "Invalid relational operator given" ); return _pc32(); }
        }
    }

    // convenience method for jumping to an absolute location if the current comparison on the expression stack is false
    public static int jumpIfNot( int relop, int address_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        int pcOffset_16 = address_16 - _pc32();
        switch( relop )
        {
            case TokenCode.eq:  return i_jne( pcOffset_16 );
            case TokenCode.ne:  return i_jeq( pcOffset_16 );
            case TokenCode.lt:  return i_jge( pcOffset_16 );
            case TokenCode.le:  return i_jgt( pcOffset_16 );
            case TokenCode.gt:  return i_jle( pcOffset_16 );
            case TokenCode.ge:  return i_jlt( pcOffset_16 );
            default: { report_fatal( "Invalid relational operator given" ); return _pc32(); }
        }
    }



    ////// method calls
    // IMPORTANT: push and pop work on the stack!, not the expression stack
    // +   the <caller function> uses 'call' and 'return', and the <callee function> uses 'enter' and 'exit' instructions

    // 49   call s              push(pc+3); pc := pc + s;
    public static int i_call( int pcOffset_16 ) { int pc32 = _pc32(); put8( i_call ); put16( pcOffset_16 ); return pc32; }
    // 50   return              pc = pop();
    public static int i_return() { int pc32 = _pc32(); put8( i_return ); return pc32; }

    // 51   enter b1, b2        psize = b1/**4B*/; lsize = b2/**4B*/; push(fp); fp = sp; sp = sp + lsize; <init stack frame with zeros>; for( i=psize‐1; i>=0; i‐‐) fp[i] = pop();
    public static int i_enter( int paramCount_8, int paramAndLocalCount_8 ) { int pc32 = _pc32(); put8( i_enter ); put8( paramCount_8 ); put8( paramAndLocalCount_8 ); return pc32; }
    // 52   exit                sp = fp; fp = pop();
    public static int i_exit() { int pc32 = _pc32(); put8( i_exit ); return pc32; }

    // 58   invokevirtual w1,w2,...,wn,wn+1    [..., adr -> ...]   // w1..wn - the name of the class's method, wn+1 has to be -1
    // +    find the virtual method in the virtual method table by name, and jump to the beginning of the method body
    // +    adr is the virtual table start address in the static memory zone
    public static int i_invokevirtual( String methodName )
    {
        int pc32 = _pc32();

        put8( i_invokevirtual );
        for( int i = 0; i < methodName.length(); i++ )
        {
            put32( methodName.charAt( i ) );
        }
        // end the method name with -1, as per microjava virtual machine specification
        put32( -1 );

        return pc32;
    }



    ////// input and output

    // 53   read      ExprStack=[... -> ..., val]           readInt(x); epush(x);                       // read word from stdin
    public static int i_read() { int pc32 = _pc32(); put8( i_read ); return pc32; }
    // 54   print     ExprStack=[..., val, width -> ...]    width = epop(); writeInt(epop(), width);    // write word to stdout
    public static int i_print() { int pc32 = _pc32(); put8( i_print ); return pc32; }
    // 55   bread     ExprStack=[... -> ..., val]           readChar(ch); epush(ch);                    // read char from stdin
    public static int i_bread() { int pc32 = _pc32(); put8( i_bread ); return pc32; }
    // 56   bprint    ExprStack=[..., val, width -> ...]    width = epop(); writeChar(epop(), width);   // write char to stdout
    public static int i_bprint() { int pc32 = _pc32(); put8( i_bprint ); return pc32; }

    // convenience method for reading from input to the expression stack
    public static int read( SymbolType symbolType )
    {
        if( symbolType.isInt()  ) { return i_read();  }
        if( symbolType.isChar() ) { return i_bread(); }
        if( symbolType.isBool() )
        {
            int pointA = i_read();
                         loadConst( FALSE );
            int pointB = jumpIf( TokenCode.ne, NO_ADDRESS );   // jump to D
                         loadConst( FALSE );
            int pointC = jump( NO_ADDRESS );   // jump to E
            int pointD = loadConst( TRUE );
            
            int pointE = _pc32();

            // fix the jump addresses for the jump instructions
            fixJumpOffset( pointB, pointD );
            fixJumpOffset( pointC, pointE );

            return pointA;
        }

        report_fatal( "Cannot read non-primitive type" ); return _pc32();
    }
    // convenience method for writing from the expression stack to output
    public static int print( SymbolType symbolType )
    {
        if( symbolType.isInt()  ) { return i_print();  }
        if( symbolType.isChar() ) { return i_bprint(); }
        if( symbolType.isBool() ) { return i_print();  }

        report_fatal( "Cannot print non-primitive type" ); return _pc32();
    }

    // 57   trap b
    public static int i_trap( int trapCode_8 ) { int pc32 = _pc32(); put8( i_trap ); put8( trapCode_8 ); return pc32; }

}
