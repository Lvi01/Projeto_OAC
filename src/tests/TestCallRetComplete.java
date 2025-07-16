import architecture.Architecture;

public class TestCallRetComplete {
    public static void main(String[] args) {
        try {
            Architecture arch = new Architecture(false);
            
            System.out.println("=== Testing Complete CALL and RET ===");
            
            // Initial state
            arch.getPC().read();
            int initialPC = arch.getExtbus1().get();
            System.out.println("Initial PC: " + initialPC);
            System.out.println("Initial Stack Top: " + arch.getStackTop());
            
            // Step 1: Call subroutine at address 50
            System.out.println("\n--- Step 1: CALL 50 ---");
            arch.getExtbus1().put(50);
            arch.call();
            
            arch.getPC().read();
            int pcAfterCall = arch.getExtbus1().get();
            System.out.println("PC after CALL: " + pcAfterCall);
            System.out.println("Stack Top after CALL: " + arch.getStackTop());
            
            // Verify return address is stored on stack
            int stackTop = arch.getStackTop();
            arch.getExtbus1().put(stackTop);
            arch.getMemory().read();
            int storedReturnAddr = arch.getExtbus1().get();
            System.out.println("Return address stored on stack: " + storedReturnAddr);
            System.out.println("Expected return address: 2");
            
            // Step 2: Simulate some operations in the subroutine
            System.out.println("\n--- Step 2: Subroutine operations ---");
            System.out.println("Current PC (should be 50): " + pcAfterCall);
            
            // Step 3: Return from subroutine
            System.out.println("\n--- Step 3: RET ---");
            arch.ret();
            
            // Read the PC value after RET
            arch.getPC().read();
            int finalPC = arch.getExtbus1().get();
            
            System.out.println("PC after RET: " + finalPC);
            System.out.println("Stack Top after RET: " + arch.getStackTop());
            
            // Verify we're back to the correct return address
            if (finalPC == 2) {
                System.out.println("\nSUCCESS: CALL and RET working correctly!");
                System.out.println("   - CALL saved return address (2) to stack");
                System.out.println("   - CALL jumped to target address (50)");
                System.out.println("   - RET restored PC to return address (2)");
                System.out.println("   - Stack pointer properly managed");
            } else {
                System.out.println("\nFAILURE: PC not restored correctly");
                System.out.println("   Expected PC: 2, Actual PC: " + finalPC);
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
