import architecture.Architecture;

public class TestCallOnlyFixed {
    public static void main(String[] args) {
        try {
            Architecture arch = new Architecture(false);
            
            // Test: Check if stackPush now stores the correct value
            System.out.println("=== Testing stackPush Fix ===");
            
            // Setup: PC = 0, then call should save PC+2 = 2
            System.out.println("Initial PC: " + arch.getPC());
            
            // Call with target 50
            arch.getExtbus1().put(50);  // Set target address
            arch.call();
            
            // Check stack state
            int stackTop = arch.getStackTop();
            System.out.println("Stack top after CALL: " + stackTop);
            
            // Check if correct value (2) is stored in memory at stack top
            arch.getExtbus1().put(stackTop);
            arch.getMemory().read();
            int storedValue = arch.getExtbus1().get();
            
            System.out.println("Value stored at stack top (" + stackTop + "): " + storedValue);
            System.out.println("Expected value (PC+2): 2");
            
            if (storedValue == 2) {
                System.out.println("SUCCESS: stackPush is working correctly!");
            } else {
                System.out.println("FAILURE: stackPush still storing wrong value!");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
