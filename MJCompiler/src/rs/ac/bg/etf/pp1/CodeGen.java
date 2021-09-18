package rs.ac.bg.etf.pp1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.mj.runtime.disasm;

public class CodeGen
{
    // IMPORTANT: copied over from the Code class, since it is private in that class for some reason (but the buffer isn't)
    private static final int bufsz = 8192;
    private static final byte[] buf = Code.buf;
    
    public static int _pc32() { return Code.pc; };
    private static void _pc32( int value_32 ) { Code.pc = value_32; };
    private static void _pc32Inc() { Code.pc++; };

    private static int _mainAddr32() { return Code.mainPc; };
    private static void _mainAddr32( int value_32 ) { Code.mainPc = value_32; };
    public static void _mainAddr32Set() { Code.mainPc = Code.pc; };

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
        _mainAddr32( 0 );
        _staticSize32( staticSize );

        // initialize the code segment
        // +   don't do this here, because the microjava virtual machine expects the code to start at 0 (the microjava header is prepended at the end of compilation)
        // /*00*/put8( 'M' );
        // /*01*/put8( 'J' );
        // /*02*/put32( _pc32() );
        // /*06*/put32( _staticSize32() );
        // /*10*/put32( _mainAddr32() );
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
    public static String compile()
    {
        String output = "";
        int codeSize = _pc32();

        try( ByteArrayOutputStream buffer = new ByteArrayOutputStream(); )
        {
            // HACK: append the header to the end of the file (the pc, static segment size and the program entry point)
            // +   first output the header, and then the actual file
            // +   afterwards, restore the old pc value

            /*00*/put8( 'M' );
            /*01*/put8( 'J' );
            /*02*/put32( _pc32() );
            /*06*/put32( _staticSize32() );
            /*10*/put32( _mainAddr32() );

            // write the header and actual code to the buffer
            buffer.write( buf, codeSize, _pc32() - codeSize );
            buffer.write( buf, 0, codeSize );

            // return the buffer as string
            output = buffer.toString( "UTF-8" );
        }
        catch( IOException ex )
        {}
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
        catch( IOException ex )
        {
            return "";
        }
        
        return output;
    }



    
    
    // ________________________________________________________________________________________________
    // convenience methods


    // load the symbol's value/address onto the expression stack
    public static void loadSymbol( Symbol symbol )
    {
        // +   the <CONST case> also works for null (reference type), since it is a constant 0
        // +   the <ARRAY_ELEM case> requires that the array element's index was previously loaded onto the expression stack
        // +   the <FIELD case> also works for @pVirtualTable (field whose value is the virtual table's starting address)
        switch( symbol._kind() )
        {
            case Symbol.CONST: if( !symbol.isThis() )  { loadConst  ( symbol._value()     ); break; }
                               else                    { loadLocal  ( 0/*'this' param*/   ); break; }
            case Symbol.VAR:   if( symbol.isGlobal() ) { i_getstatic( symbol._address()   ); break; }
                               else                    { loadLocal  ( symbol._varIdx()    ); break; }
            case Symbol.FORMAL_PARAM:                  { loadLocal  ( symbol._varIdx()    ); break; }
            case Symbol.STATIC_FIELD:                  { i_getstatic( symbol._address()   ); break; }
            case Symbol.FIELD:                         { i_getfield ( symbol._memberIdx() ); break; }
         // case Symbol.METHOD:                        { /*TODO*/ break; }
         // case Symbol.FUNCTION:                      { /*TODO*/ break; }
         // case Symbol.TYPE:                          { /*TODO*/ break; }
            case Symbol.ARRAY_ELEM:                    { loadArrayElem( symbol._type().isChar() ); break; }

            default: report_fatal( "Unsupported source symbol for loading onto the expression stack" );
        }
    }
    // store the value on the expression stack into the given symbol
    public static void storeSymbol( Symbol symbol )
    {
        switch( symbol._kind() )
        {
         // case Symbol.CONST:                                        { /*not allowed*/ break; }
            case Symbol.VAR:   if( symbol.isGlobal() )                { i_putstatic( symbol._address()   ); break; }
                               else                                   { storeLocal ( symbol._varIdx()    ); break; }
            case Symbol.FORMAL_PARAM:                                 { storeLocal ( symbol._varIdx()    ); break; }
            case Symbol.STATIC_FIELD:                                 { i_putstatic( symbol._address()   ); break; }
            case Symbol.FIELD:                                        { i_putfield ( symbol._memberIdx() ); break; }
         // case Symbol.METHOD:                                       { /*not allowed*/ break; }
         // case Symbol.FUNCTION:                                     { /*not allowed*/ break; }
         // case Symbol.TYPE:                                         { /*not allowed*/ break; }
            case Symbol.ARRAY_ELEM:                                   { storeArrayElem( symbol._type().isChar() ); break; }

            default: report_fatal( "Unsupported destination symbol for storing from the expression stack" );
        }
    }



    // get the class's virtual table size in words (i32)
    public static int getVirtualTableSize( Symbol classSymbol )
    {
        // size of the virtual table
        int vtSize = 0;

        // for all the class's methods
        for( Symbol method : classSymbol._type()._members() )
        {
            // if the symbol is not a method, skip it
            if( !method.isMethod() ) continue;

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
        for( Symbol method : classSymbol._type()._members() )
        {
            // if the symbol is not a method, skip it
            if( !method.isMethod() ) continue;
            
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


    // set the jump's offset so that after jumping we end up at the instruction the current pc points to
    public static void setJumpOffset( int address_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        // +   address + 1 because the first byte is the instruction's opcode
        put16( address_16+1, _pc32() - address_16 );
    }



    // append the given byte (i8) to the code segment
    private static void put8( int value_8 )
    {
        if( _pc32() >= bufsz ) report_fatal( String.format( "Code segment larger than MicroJava virtual machine permits: %d", bufsz ) );

        buf[ _pc32() ] = ( byte )( value_8 & 0xFF );
        _pc32Inc();
    }
    // append the given short (i16) to the code segment
    private static void put16( int value_16 )
    {
        put8( value_16 >> 8 );
        put8( value_16 );
    }
    // overwrite the given short (i16) in the code segment with the given value
    private static void put16( int address_16, int value_16 )
    {
        int pc_old = _pc32();
        _pc32( address_16 );
        put16( value_16 );
        _pc32( pc_old );
    }
    // append the given word (i32) to the code segment
    private static void put32( int value_32 )
    {
        put16( value_32 >> 16 );
        put16( value_32 );
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

    ////// accessing local variables (from the stack frame)

    // 1    load b     ExprStack=[... -> ..., val]    epush(fp[b]);
    public static void i_load( int localIdx_8 ) { put8( i_load ); put8( localIdx_8 ); }

    // 2    load_0     ExprStack=[... -> ..., val]    epush(fp[0]);
    public static void i_load_0() { put8( i_load_0 ); }
    // 3    load_1     ExprStack=[... -> ..., val]    epush(fp[1]);
    public static void i_load_1() { put8( i_load_1 ); }
    // 4    load_2     ExprStack=[... -> ..., val]    epush(fp[2]);
    public static void i_load_2() { put8( i_load_2 ); }
    // 5    load_3     ExprStack=[... -> ..., val]    epush(fp[3]);
    public static void i_load_3() { put8( i_load_3 ); }

    // convenience method for loading a word (i32) from the stack frame onto the expression stack
    public static void loadLocal( int localIdx_8 )
    {
        switch( localIdx_8 )
        {
            case  0: i_load_0();  break;
            case  1: i_load_1();  break;
            case  2: i_load_2();  break;
            case  3: i_load_3();  break;
            default: i_load( localIdx_8 ); break;
        }
    }


    // 6    store b    ExprStack=[..., val -> ...]    fp[b] = epop();
    public static void i_store( int localIdx_8 ) { put8( i_store ); put8( localIdx_8 ); }

    // 7    store_0    ExprStack=[..., val -> ...]    fp[0] = epop();
    public static void i_store_0() { put8( i_store_0 ); }
    // 8    store_1    ExprStack=[..., val -> ...]    fp[1] = epop();
    public static void i_store_1() { put8( i_store_1 ); }
    // 9    store_2    ExprStack=[..., val -> ...]    fp[2] = epop();
    public static void i_store_2() { put8( i_store_2 ); }
    // 10   store_3    ExprStack=[..., val -> ...]    fp[3] = epop();
    public static void i_store_3() { put8( i_store_3 ); }

    // convenience method for storing a word (i32) from the expression stack to the stack frame
    public static void storeLocal( int localIdx_8 )
    {
        switch( localIdx_8 )
        {
            case  0: i_store_0();  break;
            case  1: i_store_1();  break;
            case  2: i_store_2();  break;
            case  3: i_store_3();  break;
            default: i_store( localIdx_8 ); break;
        }
    }

    // 39   epop      ExprStack=[..., val -> ...]                                   dummy = epop();
    public static void i_epop() { put8( i_epop ); }
    // 40   dup       ExprStack=[..., val -> ..., val, val]                         x = epop(); epush(x); epush(x);
    public static void i_dup() { put8( i_dup ); }
    // 41   dup2      ExprStack=[..., v1, v2 -> ..., v1, v2, v1, v2]                y = epop(); x = epop(); epush(x); epush(y); epush(x); epush(y);
    public static void i_dup2() { put8( i_dup2 ); }
    // 59   dup_x1    ExprStack=[...,valA, valB -> ...,valB, valA, valB]            y = epop(); x = epop(); epush(y); epush(x); epush(y);
    public static void i_dup_x1() { put8( i_dup_x1 ); }
    // 60   dup_x2    ExprStack=[valA, valB, valC -> ...,valC, valA, valB, valC]    z = epop(); y = epop(); x = epop(); epush(z); epush(x); epush(y); epush(z);
    public static void i_dup_x2() { put8( i_dup_x2 ); }



    ////// accessing global and static variables

    // 11   getstatic s    ExprStack=[... -> ..., val]    epush(data[s]);
    public static void i_getstatic( int staticAddress_16 ) { put8( i_getstatic ); put16( staticAddress_16 ); }
    // 12   putstatic s    ExprStack=[..., val -> ...]    data[s] = epop();
    public static void i_putstatic( int staticAddress_16 ) { put8( i_putstatic ); put16( staticAddress_16 ); }



    ////// accessing class members

    // 13   getfield s    ExprStack=[..., adr -> ..., val]    adr = epop()/4; epush(heap[adr+s]);
    public static void i_getfield( int fieldIdx_16 ) { put8( i_getfield ); put16( fieldIdx_16 ); }
    // 14   putfield s    ExprStack=[..., adr, val -> ...]    val = epop(); adr = epop()/4; heap[adr+s] = val;
    public static void i_putfield( int fieldIdx_16 ) { put8( i_putfield ); put16( fieldIdx_16 ); }

    // 32   new s         ExprStack=[... -> ..., adr]       <alloc s bytes>; <initialize with 0>; epush(adr( &start ));
    public static void i_new( int byteCount_16/*B*/ ) { put8( i_new ); put16( byteCount_16 ); }



    ////// accessing array elements

    // 34   aload          ExprStack=[..., adr, index -> ..., val]    i = epop(); adr = epop()/4+1; epush(heap[adr+i]);                          // load array element + bounds checking
    public static void i_aload() { put8( i_aload ); }
    // 35   astore         ExprStack=[..., adr, index, val -> ...]    val = epop(); i = epop(); adr = epop()/4+1; heap[adr+i] = val;             // store array element + bounds checking
    public static void i_astore() { put8( i_astore ); }
    // 36   baload         ExprStack=[..., adr, index -> ..., val]    i = epop(); adr = epop()/4+1; x = heap[adr+i/4]; epush(<byte i%4 of x>);   // load byte array element + bounds checking
    public static void i_baload() { put8( i_baload ); }
    // 37   bastore        ExprStack=[..., adr, index, val -> ...]    val = epop(); i = epop(); adr = epop()/4+1; x = heap[adr+i/4]; <set byte i%4 in x>; heap[adr+i/4] = x;   // store byte array element + bounds checking
    public static void i_bastore() { put8( i_bastore ); }

    // convenience method for loading the given array element on the expression stack
    public static void loadArrayElem( boolean isCharArray )
    {
        if( !isCharArray ) i_aload();
        else               i_baload();
    }
    // convenience method for storing the value on the expression stack to the given array element
    public static void storeArrayElem( boolean isCharArray )
    {
        if( !isCharArray ) i_astore();
        else               i_bastore();
    }
    
    // 38   arraylength    ExprStack=[..., adr -> ..., len]           adr = epop(); epush(heap[adr]);
    public static void i_arraylength() { put8( i_arraylength ); }

    // 33   newarray b     ExprStack=[..., n -> ..., adr]             n = epop(); arr = (b != 0) ? <alloc n*i32> : <alloc n*i8>; epush(adr( arr ));
    public static void i_newarray( boolean isCharArray ) { put8( i_newarray ); put8( ( !isCharArray ) ? 1 : 0 ); }



    ////// accessing constants

    // 15   const_0    ExprStack=[... -> ...,  0]    epush(0);
    public static void i_const_0() { put8( i_const_0 ); }
    // 16   const_1    ExprStack=[... -> ...,  1]    epush(1);
    public static void i_const_1() { put8( i_const_1 ); }
    // 17   const_2    ExprStack=[... -> ...,  2]    epush(2);
    public static void i_const_2() { put8( i_const_2 ); }
    // 18   const_3    ExprStack=[... -> ...,  3]    epush(3);
    public static void i_const_3() { put8( i_const_3 ); }
    // 19   const_4    ExprStack=[... -> ...,  4]    epush(4);
    public static void i_const_4() { put8( i_const_4 ); }
    // 20   const_5    ExprStack=[... -> ...,  5]    epush(5);
    public static void i_const_5() { put8( i_const_5 ); }
    
    // 21   const_m1   ExprStack=[... -> ..., -1]    epush(-1);
    public static void i_const_m1() { put8( i_const_m1 ); }

    // 22   const w    ExprStack=[... -> ..., val]   epush(w)
    public static void i_const( int value_32 ) { put8( i_const ); put32( value_32 ); }

    // convenience method for loading a constant onto the expression stack
    public static void loadConst( int value_32 )
    {
        switch( value_32 )
        {
            case -1: i_const_m1(); break;
            case  0: i_const_0();  break;
            case  1: i_const_1();  break;
            case  2: i_const_2();  break;
            case  3: i_const_3();  break;
            case  4: i_const_4();  break;
            case  5: i_const_5();  break;
            default: i_const( value_32 ); break;
        }
    }



    ////// arithmetic operations

    // 23   add           ExprStack=[..., valA, valB -> ..., valA+valB]    epush(epop() + epop());
    public static void i_add() { put8( i_add ); }
    // 24   sub           ExprStack=[..., valA, valB -> ..., valA‐valB]    epush(‐epop() + epop());
    public static void i_sub() { put8( i_sub ); }
    // 25   mul           ExprStack=[..., valA, valB -> ..., valA*valB]    epush(epop() * epop());
    public static void i_mul() { put8( i_mul ); }
    // 26   div           ExprStack=[..., valA, valB -> ..., valA/valB]    x = epop(); epush(epop() / x);
    public static void i_div() { put8( i_div ); }
    // 27   rem           ExprStack=[..., valA, valB -> ..., valA%valB]    x = epop(); epush(epop() % x);
    public static void i_rem() { put8( i_rem ); }
    // 28   neg           ExprStack=[..., val -> ..., ‐val]                epush(‐epop());
    public static void i_neg() { put8( i_neg ); }
    // 29   shl           ExprStack=[..., val -> ..., val<<1]              x = epop(); epush(epop() << x);   // signed! shift left
    public static void i_shl() { put8( i_shl ); }
    // 30   shr           ExprStack=[..., val -> ..., val>>1]              x = epop(); epush(epop() >> x);   // signed! shift right
    public static void i_shr() { put8( i_shr ); }
    // 31   inc b1, b2    ExprStack=[... -> ...]                           fp[b1] = fp[b1] + b2;             // works for local variables (on the stack frame)
    public static void i_inc( int localIdx_8, int by_8 ) { put8( i_inc ); put8( localIdx_8 ); put8( by_8 ); }



    ////// jumps
    // IMPORTANT: the jump address is relative to the first byte of the jump instruction (more like the branch instruction on real cpu-s)

    // 42   jmp s                                          pc = pc + s;
    public static void i_jmp( int pcOffset_16 ) { put8( i_jmp ); put16( pcOffset_16 ); }

    // 43   jeq s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static void i_jeq( int pcOffset_16 ) { put8( i_jeq ); put16( pcOffset_16 ); }
    // 44   jne s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static void i_jne( int pcOffset_16 ) { put8( i_jne ); put16( pcOffset_16 ); }
    // 45   jlt s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static void i_jlt( int pcOffset_16 ) { put8( i_jlt ); put16( pcOffset_16 ); }
    // 46   jle s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static void i_jle( int pcOffset_16 ) { put8( i_jle ); put16( pcOffset_16 ); }
    // 47   jgt s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static void i_jgt( int pcOffset_16 ) { put8( i_jgt ); put16( pcOffset_16 ); }
    // 48   jge s    ExprStack=[..., valA, valB -> ...]    y = epop(); x = epop(); if(x relop y) pc = pc + s;
    public static void i_jge( int pcOffset_16 ) { put8( i_jge ); put16( pcOffset_16 ); }

    // convenience method for jumping to an absolute location if the current comparison on the expression stack is true
    public static void jumpIf( int relop, int address_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        int pcOffset_16 = address_16 - _pc32();
        switch( relop )
        {
            case TokenCode.dot: i_jmp( pcOffset_16 ); break;   // dot <=> no relop given
            case TokenCode.eq: i_jeq( pcOffset_16 ); break;
            case TokenCode.ne: i_jne( pcOffset_16 ); break;
            case TokenCode.lt: i_jlt( pcOffset_16 ); break;
            case TokenCode.le: i_jle( pcOffset_16 ); break;
            case TokenCode.gt: i_jgt( pcOffset_16 ); break;
            case TokenCode.ge: i_jge( pcOffset_16 ); break;
            default: report_fatal( "Invalid relational operator given" );
        }
    }

    // convenience method for jumping to an absolute location if the current comparison on the expression stack is false
    public static void jumpIfNot( int relop, int address_16 )
    {
        // IMPORTANT: offset is calculated between the start of instructions (based on the microjava virtual machine specification)
        int pcOffset_16 = address_16 - _pc32();
        switch( relop )
        {
            case TokenCode.dot:                      break;   // dot <=> no relop given
            case TokenCode.eq: i_jne( pcOffset_16 ); break;
            case TokenCode.ne: i_jeq( pcOffset_16 ); break;
            case TokenCode.lt: i_jge( pcOffset_16 ); break;
            case TokenCode.le: i_jgt( pcOffset_16 ); break;
            case TokenCode.gt: i_jle( pcOffset_16 ); break;
            case TokenCode.ge: i_jlt( pcOffset_16 ); break;
            default: report_fatal( "Invalid relational operator given" );
        }
    }



    ////// method calls
    // IMPORTANT: push and pop work on the stack!, not the expression stack
    // +   the <caller function> uses 'call' and 'return', and the <callee function> uses 'enter' and 'exit' instructions

    // 49   call s              push(pc+3); pc := pc + s;
    public static void i_call( int pcOffset_16 ) { put8( i_call ); put16( pcOffset_16 ); }
    // 50   return              pc = pop();
    public static void i_return() { put8( i_return ); }

    // 51   enter b1, b2        psize = b1/**4B*/; lsize = b2/**4B*/; push(fp); fp = sp; sp = sp + lsize; <init stack frame with zeros>; for( i=psize‐1; i>=0; i‐‐) fp[i] = pop();
    public static void i_enter( int paramCount_8, int paramAndLocalCount_8 ) { put8( i_enter ); put8( paramCount_8 ); put8( paramAndLocalCount_8 ); }
    // 52   exit                sp = fp; fp = pop();
    public static void i_exit() { put8( i_exit ); }

    // 58   invokevirtual w1,w2,...,wn,wn+1    [..., adr -> ...]   // w1..wn - the name of the class's method, wn+1 has to be -1
    // +    find the virtual method in the virtual method table by name, and jump to the beginning of the method body
    // +    adr is the virtual table start address in the static memory zone
    public static void i_invokevirtual( String methodName )
    {
        put8( i_invokevirtual );
        for( int i = 0; i < methodName.length(); i++ )
        {
            put8( methodName.charAt( i ) );
        }
        // end the method name with -1, as per microjava virtual machine specification
        put8( -1 );
    }



    ////// input and output

    // 53   read      ExprStack=[... -> ..., val]           readInt(x); epush(x);                       // read word from stdin
    public static void i_read() { put8( i_read ); }
    // 54   print     ExprStack=[..., val, width -> ...]    width = epop(); writeInt(epop(), width);    // write word to stdout
    public static void i_print() { put8( i_print ); }
    // 55   bread     ExprStack=[... -> ..., val]           readChar(ch); epush(ch);                    // read char from stdin
    public static void i_bread() { put8( i_bread ); }
    // 56   bprint    ExprStack=[..., val, width -> ...]    width = epop(); writeChar(epop(), width);   // write char to stdout
    public static void i_bprint() { put8( i_bprint ); }

    // convenience method for reading from input to the expression stack
    public static void input( boolean isChar )
    {
        if( !isChar ) i_read();
        else          i_bread();
    }
    // convenience method for writing from the expression stack to output
    public static void print( boolean isChar )
    {
        if( !isChar ) i_print();
        else          i_bprint();
    }

    // 57   trap b
    public static void i_trap( int trapCode_8 ) { put8( i_trap ); put8( trapCode_8 ); }

}
