package tests;

import architecture.Architecture;

/**
 * Debug test for JE and JNE instructions
 */
public class TestDebugJump {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG JE e JNE ===");
        
        testJZForComparison();
        testJEDebug();
        testJNZForComparison();
        testJNEDebug();
    }
    
    public static void testJZForComparison() {
        System.out.println("\n--- Teste JZ (referencia) ---");
        Architecture arch = new Architecture();
        
        // Set ZERO flag to 1
        arch.getFlags().setBit(0, 1);
        
        // Setup jump address in memory at position 0: target = 150
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(150);
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        // Show initial state
        arch.getPC().read();
        System.out.println("PC inicial: " + arch.getExtbus1().get());
        System.out.println("ZERO flag: " + arch.getFlags().getBit(0));
        
        // Execute JZ
        arch.jz();
        
        // Check result
        arch.getPC().read();
        int pcValue = arch.getExtbus1().get();
        System.out.println("PC final: " + pcValue);
        System.out.println("Esperado: 150, Obtido: " + pcValue);
    }
    
    public static void testJEDebug() {
        System.out.println("\n--- Teste JE (debug) ---");
        Architecture arch = new Architecture();
        
        // Set ZERO flag to 1 (same as JZ test)
        arch.getFlags().setBit(0, 1);
        
        // Setup jump address in memory at position 0: target = 150
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(150);
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        // Show initial state
        arch.getPC().read();
        System.out.println("PC inicial: " + arch.getExtbus1().get());
        System.out.println("ZERO flag: " + arch.getFlags().getBit(0));
        
        // Execute JE
        arch.je();
        
        // Check result
        arch.getPC().read();
        int pcValue = arch.getExtbus1().get();
        System.out.println("PC final: " + pcValue);
        System.out.println("Esperado: 150, Obtido: " + pcValue);
    }
    
    public static void testJNZForComparison() {
        System.out.println("\n--- Teste JNZ (referencia) ---");
        Architecture arch = new Architecture();
        
        // Set ZERO flag to 0
        arch.getFlags().setBit(0, 0);
        
        // Setup jump address in memory at position 0: target = 250
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(250);
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        // Show initial state
        arch.getPC().read();
        System.out.println("PC inicial: " + arch.getExtbus1().get());
        System.out.println("ZERO flag: " + arch.getFlags().getBit(0));
        
        // Execute JNZ
        arch.jnz();
        
        // Check result
        arch.getPC().read();
        int pcValue = arch.getExtbus1().get();
        System.out.println("PC final: " + pcValue);
        System.out.println("Esperado: 250, Obtido: " + pcValue);
    }
    
    public static void testJNEDebug() {
        System.out.println("\n--- Teste JNE (debug) ---");
        Architecture arch = new Architecture();
        
        // Set ZERO flag to 0 (same as JNZ test)
        arch.getFlags().setBit(0, 0);
        
        // Setup jump address in memory at position 0: target = 250
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(250);
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        // Show initial state
        arch.getPC().read();
        System.out.println("PC inicial: " + arch.getExtbus1().get());
        System.out.println("ZERO flag: " + arch.getFlags().getBit(0));
        
        // Execute JNE
        arch.jne();
        
        // Check result
        arch.getPC().read();
        int pcValue = arch.getExtbus1().get();
        System.out.println("PC final: " + pcValue);
        System.out.println("Esperado: 250, Obtido: " + pcValue);
    }
}
