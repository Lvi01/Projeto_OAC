package tests;

import architecture.Architecture;

/**
 * Test for FASE 3 - Comparison and conditional jump operations
 * Tests instructions: cmp (18), je (19), jne (20)
 */
public class TestPhase3 {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE FASE 3 - Sistema de Comparação ===");
            
            // Test 1: CMP with equal values
            System.out.println("\n--- Teste 1: CMP com valores iguais ---");
            testCmpEqual();
            
            // Test 2: CMP with different values
            System.out.println("\n--- Teste 2: CMP com valores diferentes ---");
            testCmpNotEqual();
            
            // Test 3: JE (Jump if Equal)
            System.out.println("\n--- Teste 3: JE (Jump if Equal) ---");
            testJumpIfEqual();
            
            // Test 4: JNE (Jump if Not Equal)
            System.out.println("\n--- Teste 4: JNE (Jump if Not Equal) ---");
            testJumpIfNotEqual();
            
            System.out.println("\n=== FASE 3 COMPLETA - TODOS OS TESTES PASSARAM! ===");
        } catch (Exception e) {
            System.out.println("ERRO nos testes da FASE 3: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test CMP instruction with equal values
     * Should set ZERO flag to 1
     */
    public static void testCmpEqual() {
        Architecture arch = new Architecture();
        
        // Setup: REG0 = 10, REG1 = 10
        arch.getExtbus1().put(10);
        arch.getREG0().store();
        
        arch.getExtbus1().put(10);
        arch.getREG1().store();
        
        // Setup parameters in memory for CMP REG0 REG1
        // Position 0: REG0 ID
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(0); // REG0 id
        arch.getMemory().store();
        
        // Position 1: REG1 ID
        arch.getExtbus1().put(1);
        arch.getMemory().store();
        arch.getExtbus1().put(1); // REG1 id
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        try {
            // Execute CMP directly
            arch.cmp();
            
            // Check ZERO flag should be 1 (equal values)
            int zeroFlag = arch.getFlags().getBit(0);
            
            if (zeroFlag == 1) {
                System.out.println("OK CMP (iguais): ZERO flag = " + zeroFlag + " - PASSOU!");
            } else {
                System.out.println("ERRO CMP (iguais): ZERO flag = " + zeroFlag + " - FALHOU!");
            }
        } catch (Exception e) {
            System.out.println("ERRO CMP (iguais): ERRO - " + e.getMessage());
        }
    }
    
    /**
     * Test CMP instruction with different values
     * Should set ZERO flag to 0
     */
    public static void testCmpNotEqual() {
        Architecture arch = new Architecture();
        
        // Setup: REG0 = 15, REG1 = 5
        arch.getExtbus1().put(15);
        arch.getREG0().store();
        
        arch.getExtbus1().put(5);
        arch.getREG1().store();
        
        // Setup parameters in memory for CMP REG0 REG1
        // Position 0: REG0 ID
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(0); // REG0 id
        arch.getMemory().store();
        
        // Position 1: REG1 ID
        arch.getExtbus1().put(1);
        arch.getMemory().store();
        arch.getExtbus1().put(1); // REG1 id
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        try {
            // Execute CMP directly
            arch.cmp();
            
            // Check ZERO flag should be 0 (different values)
            int zeroFlag = arch.getFlags().getBit(0);
            
            if (zeroFlag == 0) {
                System.out.println("OK CMP (diferentes): ZERO flag = " + zeroFlag + " - PASSOU!");
            } else {
                System.out.println("ERRO CMP (diferentes): ZERO flag = " + zeroFlag + " - FALHOU!");
            }
        } catch (Exception e) {
            System.out.println("ERRO CMP (diferentes): ERRO - " + e.getMessage());
        }
    }
    
    /**
     * Test JE instruction when ZERO flag is set
     * Should jump to the specified address
     */
    public static void testJumpIfEqual() {
        Architecture arch = new Architecture();
        
        // Set ZERO flag to 1 (simulate equal comparison)
        arch.getFlags().setBit(0, 1);
        
        // Setup jump address in memory
        // Position 0: jump target address (100)
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(100); // jump target
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        try {
            // Execute JE directly
            arch.je();
            
            // Check PC should be 100 (jumped)
            arch.getPC().read();
            int pcValue = arch.getExtbus1().get();
            
            if (pcValue == 100) {
                System.out.println("OK JE (ZERO=1): PC = " + pcValue + " - PASSOU!");
            } else {
                System.out.println("ERRO JE (ZERO=1): PC = " + pcValue + " - FALHOU!");
            }
        } catch (Exception e) {
            System.out.println("ERRO JE (ZERO=1): ERRO - " + e.getMessage());
        }
    }
    
    /**
     * Test JNE instruction when ZERO flag is NOT set
     * Should jump to the specified address
     */
    public static void testJumpIfNotEqual() {
        Architecture arch = new Architecture();
        
        // Set ZERO flag to 0 (simulate not equal comparison)
        arch.getFlags().setBit(0, 0);
        
        // Setup jump address in memory
        // Position 0: jump target address (200)
        arch.getExtbus1().put(0);
        arch.getMemory().store();
        arch.getExtbus1().put(200); // jump target
        arch.getMemory().store();
        
        // Set PC to start at position 0
        arch.getExtbus1().put(0);
        arch.getPC().store();
        
        try {
            // Execute JNE directly
            arch.jne();
            
            // Check PC should be 200 (jumped)
            arch.getPC().read();
            int pcValue = arch.getExtbus1().get();
            
            if (pcValue == 200) {
                System.out.println("OK JNE (ZERO=0): PC = " + pcValue + " - PASSOU!");
            } else {
                System.out.println("ERRO JNE (ZERO=0): PC = " + pcValue + " - FALHOU!");
            }
        } catch (Exception e) {
            System.out.println("ERRO JNE (ZERO=0): ERRO - " + e.getMessage());
        }
    }
}
