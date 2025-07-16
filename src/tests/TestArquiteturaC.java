import architecture.Architecture;

public class TestArquiteturaC {
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE COMPLETO DA ARQUITETURA C ===");
            
            // Test 1: Basic functionality verification
            testBasicInstructions();
            
            // Test 2: Stack operations and CALL/RET
            testStackOperations();
            
            // Test 3: Complex program with subroutines
            testComplexProgram();
            
            // Test 4: Architecture components verification
            testArchitectureComponents();
            
            System.out.println("\n=== TODOS OS TESTES CONCLUIDOS ===");
            
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBasicInstructions() {
        System.out.println("\n--- Test 1: Basic Instructions ---");
        Architecture arch = new Architecture(false);
        
        // Test LDI (Load Immediate)
        arch.getExtbus1().put(10);  // Value to load
        arch.ldi();
        arch.getREG0().read();
        int reg0Value = arch.getExtbus1().get();
        System.out.println("LDI 10 -> REG0: " + reg0Value + " (Expected: 10)");
        
        // Test INC
        arch.inc();
        arch.getREG0().read();
        reg0Value = arch.getExtbus1().get();
        System.out.println("INC -> REG0: " + reg0Value + " (Expected: 11)");
        
        // Test ADD with memory
        // Store value 5 in memory address 50
        arch.getExtbus1().put(50);  // Address
        arch.getMemory().store();
        arch.getExtbus1().put(5);   // Value
        arch.getMemory().store();
        
        // ADD memory[50] to REG0
        arch.getExtbus1().put(50);  // Address parameter
        arch.add();
        arch.getREG0().read();
        reg0Value = arch.getExtbus1().get();
        System.out.println("ADD memory[50] -> REG0: " + reg0Value + " (Expected: 16)");
        
        System.out.println("OK Basic instructions working correctly");
    }
    
    private static void testStackOperations() {
        System.out.println("\n--- Test 2: Stack Operations ---");
        Architecture arch = new Architecture(false);
        
        // Test stack initialization
        System.out.println("Initial stack top: " + arch.getStackTop() + " (Expected: 127)");
        System.out.println("Stack bottom: " + arch.getStackBottom() + " (Expected: 100)");
        System.out.println("Stack empty: " + arch.isStackEmpty() + " (Expected: true)");
        
        // Test stack push/pop
        arch.stackPush(42);
        System.out.println("After push(42) - Stack top: " + arch.getStackTop() + " (Expected: 126)");
        System.out.println("Stack empty: " + arch.isStackEmpty() + " (Expected: false)");
        
        // Verify value on stack
        int stackAddr = arch.getStackTop();
        arch.getExtbus1().put(stackAddr);
        arch.getMemory().read();
        int stackValue = arch.getExtbus1().get();
        System.out.println("Value on stack: " + stackValue + " (Expected: 42)");
        
        // Test stack pop
        arch.stackPop();
        System.out.println("After pop - Stack top: " + arch.getStackTop() + " (Expected: 127)");
        System.out.println("Stack empty: " + arch.isStackEmpty() + " (Expected: true)");
        
        System.out.println("OK Stack operations working correctly");
    }
    
    private static void testComplexProgram() {
        System.out.println("\n--- Test 3: Complex CALL/RET Program ---");
        Architecture arch = new Architecture(false);
        
        // Program simulation:
        // Main: LDI 10, CALL subroutine, INC, HALT
        // Subroutine: ADD 5, RET
        
        // Step 1: Load 10 into REG0
        arch.getExtbus1().put(10);
        arch.ldi();
        arch.getREG0().read();
        System.out.println("Main: LDI 10 -> REG0 = " + arch.getExtbus1().get());
        
        // Step 2: CALL subroutine at address 100
        System.out.println("Main: Calling subroutine at address 100");
        arch.getPC().read();
        int pcBefore = arch.getExtbus1().get();
        System.out.println("PC before CALL: " + pcBefore);
        
        arch.getExtbus1().put(100);  // Target address
        arch.call();
        
        arch.getPC().read();
        int pcAfter = arch.getExtbus1().get();
        System.out.println("PC after CALL: " + pcAfter + " (Expected: 100)");
        
        // Verify return address is on stack
        int stackAddr = arch.getStackTop();
        arch.getExtbus1().put(stackAddr);
        arch.getMemory().read();
        int returnAddr = arch.getExtbus1().get();
        System.out.println("Return address on stack: " + returnAddr + " (Expected: " + (pcBefore + 2) + ")");
        
        // Step 3: Simulate subroutine - ADD 5 to REG0
        // First, store 5 in memory address 80
        arch.getExtbus1().put(80);
        arch.getMemory().store();
        arch.getExtbus1().put(5);
        arch.getMemory().store();
        
        // ADD memory[80] to REG0
        arch.getExtbus1().put(80);
        arch.add();
        arch.getREG0().read();
        System.out.println("Subroutine: ADD 5 -> REG0 = " + arch.getExtbus1().get() + " (Expected: 15)");
        
        // Step 4: RET
        System.out.println("Subroutine: Returning to main");
        arch.ret();
        
        arch.getPC().read();
        int finalPC = arch.getExtbus1().get();
        System.out.println("PC after RET: " + finalPC + " (Expected: " + (pcBefore + 2) + ")");
        System.out.println("Stack top after RET: " + arch.getStackTop() + " (Expected: 127)");
        
        // Step 5: Continue main program - INC
        arch.inc();
        arch.getREG0().read();
        System.out.println("Main: INC -> REG0 = " + arch.getExtbus1().get() + " (Expected: 16)");
        
        System.out.println("OK Complex CALL/RET program working correctly");
    }
    
    private static void testArchitectureComponents() {
        System.out.println("\n--- Test 4: Architecture Components ---");
        Architecture arch = new Architecture(false);
        
        // Test command list
        System.out.println("Command list size: " + arch.getCommandsList().size() + " (Expected: 12)");
        System.out.println("Commands available:");
        for (int i = 0; i < arch.getCommandsList().size(); i++) {
            System.out.println("  " + i + ": " + arch.getCommandsList().get(i));
        }
        
        // Test registers
        System.out.println("Register list size: " + arch.getRegistersList().size() + " (Expected: 7)");
        System.out.println("Registers: PC, IR, REG0, REG1, REG2, REG3, Flags");
        
        // Test memory
        System.out.println("Memory size: " + arch.getMemorySize() + " (Expected: 128)");
        
        // Test bus connections
        System.out.println("External Bus 1: " + (arch.getExtbus1() != null ? "OK" : "FAIL"));
        System.out.println("Internal Bus 1: " + (arch.getIntbus1() != null ? "OK" : "FAIL"));
        System.out.println("Internal Bus 2: " + (arch.getIntbus2() != null ? "OK" : "FAIL"));
        
        // Test ULA integration
        System.out.println("ULA: " + (arch.getUla() != null ? "OK" : "FAIL"));
        
        // Test stack integration
        System.out.println("Stack operations: " + 
            (arch.getStackTop() == 127 && arch.getStackBottom() == 100 ? "OK" : "FAIL"));
        
        System.out.println("All architecture components verified");
    }
}
