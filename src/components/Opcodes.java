package components;

import java.util.HashMap;
import java.util.Map;

public final class Opcodes {
    // Nesta nova arquitetura, os opcodes são simples inteiros,
    // correspondendo à ordem em que aparecem no `commandsList` da Architecture.java.
    // O Architecture.java não decodifica o opcode a partir de bits complexos,
    // mas sim usa o valor direto lido da memória como o "comando".

    // --- Aritméticas e Lógicas ---
    public static final int ADD        = 0; // add <addr> (rpg <- rpg + addr)
    public static final int SUB        = 1; // sub <addr> (rpg <- rpg - addr)
    public static final int JMP        = 2; // jmp <addr> (pc <- addr)
    public static final int JZ         = 3; // jz <addr>  (se bitZero pc <- addr)
    public static final int JN         = 4; // jn <addr>  (se bitneg pc <- addr)
    public static final int READ       = 5; // read <addr> (rpg <- addr)
    public static final int STORE      = 6; // store <addr>  (addr <- rpg)
    public static final int LDI        = 7; // ldi <x>   (rpg <- x. x must be an integer)
    public static final int INC        = 8; // inc       (rpg++)
    public static final int MOVE_REG_REG = 9; // move regA regB (regA <- regB)

    // --- Outras instruções do Assembly original que precisam ser adicionadas ---
    // A arquitetura fornecida em Architecture.java não tem estas, mas seu assembly original tinha.
    // Se elas forem implementadas, precisarão de novos opcodes sequenciais.
    // Por enquanto, não estão no switch da Architecture, então não podem ser executadas.
    // Precisamos decidir se essas instruções extras serão mapeadas aqui ou se a arquitetura
    // foi simplificada. Dado o `commandsList` e o `switch` na Architecture.java, parece que sim.

    // A arquitetura tem RPG e RPG1. Mas as instruções `add addr`, `sub addr`, etc.
    // operam implicitamente em RPG.
    // O `moveRegReg` opera em regA e regB.
    // Isso implica que no formato da instrução, alguns opcodes vêm com um parâmetro (o endereço/imediato)
    // e outros com dois parâmetros (IDs de registradores).

    // --- Instruções ausentes na commandsList e no switch da Architecture.java ---
    // As instruções do assembly original que ainda não estão mapeadas para os opcodes 0-9:
    // add %<regA> <mem>
    // sub %<regA> <mem>
    // inc <mem>
    // jnz <mem>
    // jeq %<regA> %<regB> <mem>
    // jgt %<regA> %<regB> <mem>
    // jlw %<regA> %<regB> <mem>
    // call <mem>
    // ret
    // imul %<regA> %<regB> (ou outros formatos)
    // halt (não é comando assembly, mas é para controle)

    // Para fins de completude, vou definir os próximos opcodes sequencialmente,
    // mas eles precisarão ser implementados em Architecture.java.
    public static final int JNZ = 10;
    public static final int JEQ = 11;
    public static final int JGT = 12;
    public static final int JLW = 13;
    public static final int CALL = 14;
    public static final int RET = 15;
    public static final int IMUL = 16;
    public static final int HALT = 17; // HALT é uma instrução comum para parar a execução

    // Nomes simbólicos para os registradores, se o Assembler precisar (ex: para %REG0)
    public static final String REG_PREFIX = "%reg"; // Para parsing no assembler
    public static final String REG0_NAME = "%reg0";
    public static final String REG1_NAME = "%reg1";
    public static final String REG2_NAME = "%reg2";
    public static final String REG3_NAME = "%reg3";

    private static final Map<String, Integer> OPCODE_MAP = new HashMap<>();
    private static final Map<Integer, String> INSTRUCTION_NAMES = new HashMap<>();

    static {
        // Mapeamentos para os 10 comandos da Architecture.java
        OPCODE_MAP.put("add", ADD);
        INSTRUCTION_NAMES.put(ADD, "add addr");

        OPCODE_MAP.put("sub", SUB);
        INSTRUCTION_NAMES.put(SUB, "sub addr");

        OPCODE_MAP.put("jmp", JMP);
        INSTRUCTION_NAMES.put(JMP, "jmp addr");

        OPCODE_MAP.put("jz", JZ);
        INSTRUCTION_NAMES.put(JZ, "jz addr");

        OPCODE_MAP.put("jn", JN);
        INSTRUCTION_NAMES.put(JN, "jn addr");

        OPCODE_MAP.put("read", READ);
        INSTRUCTION_NAMES.put(READ, "read addr");

        OPCODE_MAP.put("store", STORE);
        INSTRUCTION_NAMES.put(STORE, "store addr");

        OPCODE_MAP.put("ldi", LDI);
        INSTRUCTION_NAMES.put(LDI, "ldi immediate");

        OPCODE_MAP.put("inc", INC);
        INSTRUCTION_NAMES.put(INC, "inc");

        OPCODE_MAP.put("moveregreg", MOVE_REG_REG); // O loader/assembler precisará converter "move %regA %regB"
                                                    // para este opcode e seus 2 parâmetros.
                                                    // O Architecture.java chama `moveRegReg()`
        INSTRUCTION_NAMES.put(MOVE_REG_REG, "move %regA %regB");

        // Mapeamentos para as instruções restantes do seu assembly original (se forem implementadas)
        OPCODE_MAP.put("jnz", JNZ);
        INSTRUCTION_NAMES.put(JNZ, "jnz mem");

        OPCODE_MAP.put("jeq", JEQ);
        INSTRUCTION_NAMES.put(JEQ, "jeq %regA %regB mem");

        OPCODE_MAP.put("jgt", JGT);
        INSTRUCTION_NAMES.put(JGT, "jgt %regA %regB mem");

        OPCODE_MAP.put("jlw", JLW);
        INSTRUCTION_NAMES.put(JLW, "jlw %regA %regB mem");

        OPCODE_MAP.put("call", CALL);
        INSTRUCTION_NAMES.put(CALL, "call mem");

        OPCODE_MAP.put("ret", RET);
        INSTRUCTION_NAMES.put(RET, "ret");

        OPCODE_MAP.put("imul", IMUL);
        INSTRUCTION_NAMES.put(IMUL, "imul %regA %regB"); // Ou outro formato para IMUL
        
        OPCODE_MAP.put("halt", HALT);
        INSTRUCTION_NAMES.put(HALT, "halt");

        // Removi os opcodes específicos como ADD_MEM_REG, ADD_REG_MEM etc.,
        // porque o `Architecture.java` não usa esses opcodes diferenciados no switch.
        // Ele usa apenas `ADD`, `SUB`, `MOVE_REG_REG`, e o microprograma trata os operandos.
        // Se `add <mem> %regA` for implementado, ele ainda teria que ser `ADD` no opcode,
        // mas o Assembler e o Architecture precisariam de lógica para diferenciar os operandos.
        // Dado seu `commandsList` e `switch`, parece que `add` é sempre `add addr`,
        // `sub` é sempre `sub addr`, etc.
        // Se as outras variações de ADD/SUB/MOVE/INC forem implementadas,
        // elas precisarão de novos opcodes sequenciais e microprogramas na Architecture.
    }

    /**
     * Retorna o opcode numérico para um dado nome de instrução assembly (lower case).
     * Útil para o Assembler.
     * @param instructionName O nome da instrução (ex: "add", "jmp", "moveregreg").
     * @return O opcode correspondente, ou null se não encontrado.
     */
    public static Integer getOpcode(String instructionName) {
        return OPCODE_MAP.get(instructionName.toLowerCase());
    }

    /**
     * Retorna o nome da instrução assembly para um dado opcode numérico.
     * Útil para depuração.
     * @param opcode O valor numérico do opcode.
     * @return O nome da instrução, ou "UNKNOWN" se não encontrado.
     */
    public static String getInstructionName(int opcode) {
        return INSTRUCTION_NAMES.getOrDefault(opcode, "UNKNOWN");
    }

    /**
     * Retorna o ID numérico de um registrador a partir do seu nome (ex: "%reg0" -> 0).
     * Útil para o Assembler.
     * @param regName O nome do registrador (ex: "%reg0").
     * @return O ID numérico do registrador.
     * @throws IllegalArgumentException Se o nome do registrador for inválido.
     */
    public static int getRegisterId(String regName) {
        if (regName.startsWith(REG_PREFIX) && regName.length() == REG_PREFIX.length() + 1) {
            char idChar = regName.charAt(REG_PREFIX.length());
            if (Character.isDigit(idChar)) {
                int id = Character.getNumericValue(idChar);
                if (id >= 0 && id <= 3) { // Supondo REG0 a REG3
                    return id;
                }
            }
        }
        throw new IllegalArgumentException("Nome de registrador inválido: " + regName);
    }
}