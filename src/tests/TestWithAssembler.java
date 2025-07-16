import architecture.Architecture;

public class TestWithAssembler {
    public static void main(String[] args) {
        try {
            Architecture arch = new Architecture(false);
            
            System.out.println("=== Testing CALL and RET with Assembler ===");
            
            // Simulating a simple program:
            // 0: MOV REG0, 10  (move 10 to REG0)
            // 2: CALL 6       (call subroutine at address 6) 
            // 4: ADD REG0, 5  (add 5 to REG0 - should execute after return)
            // 6: MUL REG0, 2  (subroutine: multiply REG0 by 2)
            // 8: RET          (return to caller)
            
            // Set up the program in memory manually
            int[] program = {
                0,   // MOV instruction code
                10,  // value for MOV
                10,  // CALL instruction code
                6,   // target address for CALL
                1,   // ADD instruction code  
                5,   // value for ADD
                3,   // MUL instruction code
                2,   // value for MUL
                11   // RET instruction code
            };
            
            // Load program into memory
            for (int i = 0; i < program.length; i++) {
                arch.getExtbus1().put(i);     // address
                arch.getMemory().store();     // set target address
                arch.getExtbus1().put(program[i]); // value
                arch.getMemory().store();     // store value
            }
            
            System.out.println("Program loaded in memory:");
            System.out.println("0-1: MOV REG0, 10");
            System.out.println("2-3: CALL 6");
            System.out.println("4-5: ADD REG0, 5");
            System.out.println("6-7: MUL REG0, 2");
            System.out.println("8: RET");
            
            // Execute program step by step
            System.out.println("\n=== Execution Trace ===");
            
            // Step 1: Execute MOV REG0, 10
            System.out.println("Step 1: MOV REG0, 10");
            arch.controlFlow();  // This will execute instruction at PC=0
            arch.getExtbus2().put(0); // Select REG0
            arch.demux().read();      // Get REG0 value
            System.out.println("REG0 = " + arch.getExtbus2().get());
            
            // Step 2: Execute CALL 6
            System.out.println("\nStep 2: CALL 6");
            int pcBeforeCall = arch.getPC().readInt(); // Get PC value before CALL
            System.out.println("PC before CALL: " + pcBeforeCall);
            arch.controlFlow();  // This will execute CALL instruction
            System.out.println("PC after CALL: " + arch.getPC().readInt());
            System.out.println("Stack top: " + arch.getStackTop());
            
            // Step 3: Execute MUL REG0, 2 (in subroutine)
            System.out.println("\nStep 3: MUL REG0, 2 (subroutine)");
            arch.controlFlow();  // This will execute MUL instruction
            arch.getExtbus2().put(0); // Select REG0
            arch.demux().read();      // Get REG0 value
            System.out.println("REG0 after MUL = " + arch.getExtbus2().get());
            
            // Step 4: Execute RET
            System.out.println("\nStep 4: RET");
            arch.controlFlow();  // This will execute RET instruction
            System.out.println("PC after RET: " + arch.getPC().readInt());
            System.out.println("Stack top: " + arch.getStackTop());
            
            // Step 5: Execute ADD REG0, 5 (after return)
            System.out.println("\nStep 5: ADD REG0, 5 (after return)");
            arch.controlFlow();  // This will execute ADD instruction
            arch.getExtbus2().put(0); // Select REG0
            arch.demux().read();      // Get REG0 value
            System.out.println("Final REG0 = " + arch.getExtbus2().get());
            
            System.out.println("\n=== Test Complete ===");
            System.out.println("Expected final REG0: 25 (10 * 2 + 5)");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
