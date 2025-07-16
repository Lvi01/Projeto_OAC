import architecture.Architecture;

public class TestBasicInstructions {
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE DETALHADO DAS INSTRUCOES BASICAS ===");
            
            testLDI();
            testINC();
            testADD();
            
            System.out.println("\n=== ANALISE COMPLETA ===");
            
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testLDI() {
        System.out.println("\n--- Teste Detalhado LDI ---");
        Architecture arch = new Architecture(false);
        
        // Debug: Check initial state
        System.out.println("Estado inicial:");
        arch.getREG0().read();
        System.out.println("  REG0 inicial: " + arch.getExtbus1().get());
        arch.getPC().read();
        System.out.println("  PC inicial: " + arch.getExtbus1().get());
        
        // Test LDI step by step
        System.out.println("\nExecutando LDI 10:");
        arch.getExtbus1().put(10);  // Put value 10 on external bus
        System.out.println("  Valor 10 colocado no extbus1");
        
        arch.ldi();  // Execute LDI instruction
        System.out.println("  Instrucao LDI executada");
        
        // Check REG0 after LDI
        arch.getREG0().read();
        int reg0Value = arch.getExtbus1().get();
        System.out.println("  REG0 apos LDI: " + reg0Value + " (Expected: 10)");
        
        // Check if PC was incremented
        arch.getPC().read();
        System.out.println("  PC apos LDI: " + arch.getExtbus1().get());
        
        if (reg0Value == 10) {
            System.out.println("✓ LDI funcionando corretamente");
        } else {
            System.out.println("✗ LDI com problema");
        }
    }
    
    private static void testINC() {
        System.out.println("\n--- Teste Detalhado INC ---");
        Architecture arch = new Architecture(false);
        
        // Setup: Put value 5 in REG0 first
        arch.getExtbus1().put(5);
        arch.ldi();
        arch.getREG0().read();
        System.out.println("REG0 inicial (apos LDI 5): " + arch.getExtbus1().get());
        
        // Execute INC
        System.out.println("Executando INC:");
        arch.inc();
        
        // Check result
        arch.getREG0().read();
        int result = arch.getExtbus1().get();
        System.out.println("REG0 apos INC: " + result + " (Expected: 6)");
        
        if (result == 6) {
            System.out.println("✓ INC funcionando corretamente");
        } else {
            System.out.println("✗ INC com problema");
        }
    }
    
    private static void testADD() {
        System.out.println("\n--- Teste Detalhado ADD ---");
        Architecture arch = new Architecture(false);
        
        // Setup: Put value 10 in REG0
        arch.getExtbus1().put(10);
        arch.ldi();
        arch.getREG0().read();
        System.out.println("REG0 inicial: " + arch.getExtbus1().get());
        
        // Setup: Store value 5 in memory address 50
        System.out.println("Armazenando valor 5 no endereco 50:");
        arch.getExtbus1().put(50);  // Address
        arch.getMemory().store();   // Store address in memory's MAR
        arch.getExtbus1().put(5);   // Value
        arch.getMemory().store();   // Store value at address 50
        
        // Verify memory content
        arch.getExtbus1().put(50);
        arch.getMemory().read();
        System.out.println("Conteudo da memoria[50]: " + arch.getExtbus1().get());
        
        // Execute ADD with memory[50]
        System.out.println("Executando ADD 50:");
        arch.getExtbus1().put(50);  // Address parameter for ADD
        arch.add();
        
        // Check result
        arch.getREG0().read();
        int result = arch.getExtbus1().get();
        System.out.println("REG0 apos ADD: " + result + " (Expected: 15)");
        
        if (result == 15) {
            System.out.println("✓ ADD funcionando corretamente");
        } else {
            System.out.println("✗ ADD com problema");
        }
    }
}
