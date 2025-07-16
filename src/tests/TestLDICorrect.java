import architecture.Architecture;

public class TestLDICorrect {
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE CORRETO DA INSTRUCAO LDI ===");
            
            testLDICorrect();
            testINCCorrect();
            testADDCorrect();
            
            System.out.println("\n=== ANALISE FINAL ===");
            
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testLDICorrect() {
        System.out.println("\n--- Teste Correto LDI ---");
        Architecture arch = new Architecture(false);
        
        // Setup the scenario properly for LDI
        // PC will point to position 10, parameter (immediate value) will be at position 11
        
        // Store immediate value 42 in memory position 11
        arch.getExtbus1().put(11);
        arch.getMemory().store();
        arch.getExtbus1().put(42);
        arch.getMemory().store();
        
        // Set PC to point to position 10 (instruction location)
        arch.getExtbus1().put(10);
        arch.getPC().store();
        
        System.out.println("Setup completo:");
        System.out.println("  PC aponta para: 10");
        System.out.println("  Memoria[11] contem: 42 (parametro imediato)");
        
        // Check initial REG0
        arch.getREG0().read();
        System.out.println("  REG0 inicial: " + arch.getExtbus1().get());
        
        // Execute LDI
        System.out.println("\nExecutando LDI:");
        arch.ldi();
        
        // Check results
        arch.getREG0().read();
        int reg0Value = arch.getExtbus1().get();
        System.out.println("REG0 apos LDI: " + reg0Value + " (Expected: 42)");
        
        arch.getPC().read();
        int pcValue = arch.getExtbus1().get();
        System.out.println("PC apos LDI: " + pcValue + " (Expected: 12)");
        
        if (reg0Value == 42 && pcValue == 12) {
            System.out.println("✓ LDI funcionando corretamente");
        } else {
            System.out.println("✗ LDI com problema");
        }
    }
    
    private static void testINCCorrect() {
        System.out.println("\n--- Teste Correto INC ---");
        Architecture arch = new Architecture(false);
        
        // Setup: Store value 15 in memory position 21 for LDI
        arch.getExtbus1().put(21);
        arch.getMemory().store();
        arch.getExtbus1().put(15);
        arch.getMemory().store();
        
        // Set PC to position 20 and execute LDI first
        arch.getExtbus1().put(20);
        arch.getPC().store();
        arch.ldi();  // This should load 15 into REG0
        
        arch.getREG0().read();
        System.out.println("REG0 apos LDI: " + arch.getExtbus1().get());
        
        // Now execute INC
        System.out.println("Executando INC:");
        arch.inc();
        
        // Check result
        arch.getREG0().read();
        int result = arch.getExtbus1().get();
        System.out.println("REG0 apos INC: " + result + " (Expected: 16)");
        
        if (result == 16) {
            System.out.println("✓ INC funcionando corretamente");
        } else {
            System.out.println("✗ INC com problema");
        }
    }
    
    private static void testADDCorrect() {
        System.out.println("\n--- Teste Correto ADD ---");
        Architecture arch = new Architecture(false);
        
        // Setup: Load initial value into REG0 using LDI
        // Store immediate value 10 in memory position 31
        arch.getExtbus1().put(31);
        arch.getMemory().store();
        arch.getExtbus1().put(10);
        arch.getMemory().store();
        
        // Set PC to position 30 and execute LDI
        arch.getExtbus1().put(30);
        arch.getPC().store();
        arch.ldi();  // Load 10 into REG0
        
        arch.getREG0().read();
        System.out.println("REG0 inicial (apos LDI): " + arch.getExtbus1().get());
        
        // Setup for ADD: Store value 7 in memory address 60
        arch.getExtbus1().put(60);
        arch.getMemory().store();
        arch.getExtbus1().put(7);
        arch.getMemory().store();
        
        // Store ADD parameter (address 60) in memory position 33
        arch.getExtbus1().put(33);
        arch.getMemory().store();
        arch.getExtbus1().put(60);
        arch.getMemory().store();
        
        // Set PC to position 32 for ADD instruction
        arch.getExtbus1().put(32);
        arch.getPC().store();
        
        System.out.println("Setup ADD:");
        System.out.println("  Memoria[60] = 7 (valor a somar)");
        System.out.println("  Memoria[33] = 60 (parametro do ADD)");
        System.out.println("  PC aponta para: 32");
        
        // Execute ADD
        System.out.println("\nExecutando ADD:");
        arch.add();
        
        // Check result
        arch.getREG0().read();
        int result = arch.getExtbus1().get();
        System.out.println("REG0 apos ADD: " + result + " (Expected: 17)");
        
        if (result == 17) {
            System.out.println("✓ ADD funcionando corretamente");
        } else {
            System.out.println("✗ ADD com problema");
        }
    }
}
