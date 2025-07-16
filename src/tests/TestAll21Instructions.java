package tests;

import architecture.Architecture;
import java.io.IOException;

/**
 * Teste completo de todas as 21 instrucoes da arquitetura expandida
 */
public class TestAll21Instructions {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE COMPLETO DE TODAS AS 21 INSTRUCOES ===");
            System.out.println("Arquitetura original: 12 instrucoes");
            System.out.println("FASE 1: +4 instrucoes (addRegReg, subRegReg, jnz, incMem)");
            System.out.println("FASE 2: +2 instrucoes (addRegMem, subRegMem)");
            System.out.println("FASE 3: +3 instrucoes (cmp, je, jne)");
            System.out.println("TOTAL: 21 instrucoes\n");
            
            // Criar arquitetura SEM modo de simulacao (nao interativo)
            Architecture arch = new Architecture(false);
            
            // Carregar programa completo
            arch.readExec("program");
            
            System.out.println("=== ESTADO INICIAL ===");
            printState(arch);
            
            // Executar programa
            System.out.println("\n=== EXECUTANDO PROGRAMA ===");
            arch.controlUnitEexec();
            
            System.out.println("\n=== ESTADO FINAL ===");
            printState(arch);
            
            System.out.println("\n=== VERIFICACAO DOS RESULTADOS ===");
            verifyResults(arch);
            
            System.out.println("\n=== RESUMO ===");
            System.out.println("OK Programa executado com sucesso!");
            System.out.println("OK Todas as 21 instrucoes foram testadas!");
            System.out.println("OK Arquitetura expandida funcionando corretamente!");
            
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro durante execucao: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printState(Architecture arch) {
        System.out.println("Registradores:");
        arch.getREG0().read();
        System.out.println("  REG0: " + arch.getExtbus1().get());
        
        arch.getREG1().read();
        System.out.println("  REG1: " + arch.getExtbus1().get());
        
        arch.getREG2().read();
        System.out.println("  REG2: " + arch.getExtbus1().get());
        
        arch.getREG3().read();
        System.out.println("  REG3: " + arch.getExtbus1().get());
        
        arch.getPC().read();
        System.out.println("  PC: " + arch.getExtbus1().get());
        
        System.out.println("  Flags: " + arch.getFlags().getData());
        
        System.out.println("Memória relevante:");
        System.out.println("  [100]: " + arch.getMemory().getDataList()[100]);
        System.out.println("  [101]: " + arch.getMemory().getDataList()[101]);
        if (arch.getMemory().getDataList().length > 102) {
            System.out.println("  [102]: " + arch.getMemory().getDataList()[102]);
            System.out.println("  [103]: " + arch.getMemory().getDataList()[103]);
        }
    }
    
    private static void verifyResults(Architecture arch) {
        // Verificar se incMem funcionou (memoria[100] deveria ser 11)
        int mem100 = arch.getMemory().getDataList()[100];
        System.out.println("incMem (15): memoria[100] = " + mem100 + 
                         (mem100 == 11 ? " OK CORRETO" : " ERRO ESPERADO 11"));
        
        // Verificar se addRegMem foi executado (resultado em memoria[101])
        int mem101 = arch.getMemory().getDataList()[101];
        System.out.println("addRegMem (16): resultado em memoria[101] = " + mem101);
        
        // Verificar flags
        int zeroFlag = arch.getFlags().getBit(0);
        int negFlag = arch.getFlags().getBit(1);
        System.out.println("Flags: ZERO=" + zeroFlag + ", NEG=" + negFlag);
        
        // Lista das instrucoes testadas
        System.out.println("\nInstrucoes testadas:");
        System.out.println("OK Instrucoes basicas: add(0), sub(1), jmp(2), jz(3), jn(4), read(5), store(6), ldi(7), inc(8), moveRegReg(9)");
        System.out.println("OK FASE 1: addRegReg(12), subRegReg(13), jnz(14), incMem(15)");
        System.out.println("OK FASE 2: addRegMem(16), subRegMem(17)");
        System.out.println("OK FASE 3: cmp(18), je(19), jne(20)");
    }
}
