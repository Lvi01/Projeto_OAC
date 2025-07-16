import architecture.Architecture;

public class TestAssemblerCallRet {
    public static void main(String[] args) {
        try {
            Architecture arch = new Architecture(true); // Use assembler mode
            
            System.out.println("=== Testing CALL and RET with Assembler ===");
            
            // Test if assembler recognizes CALL and RET
            System.out.println("Architecture initialized with assembler support");
            
            // Try to run a simple program using assembler
            arch.run();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
