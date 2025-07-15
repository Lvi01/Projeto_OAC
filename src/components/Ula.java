package components;

public class ULA {
    private int internalReg0; // Equivalente ao operando que vem de intbus1 (primeiro parâmetro da ULA)
    private int internalReg1; // Equivalente ao operando/resultado que vem de intbus2 (segundo parâmetro/resultado da ULA)

    private Bus intbus1; // Barramento interno 1 (entrada para ULA)
    private Bus intbus2; // Barramento interno 2 (entrada/saída da ULA)

    public ULA(Bus intbus1, Bus intbus2) {
        this.intbus1 = intbus1;
        this.intbus2 = intbus2;
        this.internalReg0 = 0; // Inicializa registradores internos
        this.internalReg1 = 0; // Inicializa registradores internos
    }

    /**
     * Armazena um valor de um barramento interno em um registrador interno da ULA.
     * @param internalRegIndex O índice do registrador interno da ULA (0 ou 1).
     * 0: Armazena o valor de intbus1 em internalReg0.
     * 1: Armazena o valor de intbus2 em internalReg1.
     */
    public void store(int internalRegIndex) {
        if (internalRegIndex == 0) {
            this.internalReg0 = intbus1.get();
        } else if (internalRegIndex == 1) {
            this.internalReg1 = intbus2.get(); // Note: This internalReg1 is sourced from intbus2 for regular store
        } else {
            throw new IllegalArgumentException("ULA: Índice de registrador interno inválido: " + internalRegIndex);
        }
    }
    
    /**
     * Armazena um valor de um barramento interno em um registrador interno da ULA.
     * Usado para casos onde a entrada é do intbus2 (como no seu microcódigo `ula.internalStore(1);`).
     * Note: A sua API original tem `store(0)` e `internalStore(1)`. Estou mantendo a distinção.
     * @param internalRegIndex O índice do registrador interno da ULA (apenas 1 é esperado aqui, pelo seu uso).
     */
    public void internalStore(int internalRegIndex) {
        if (internalRegIndex == 0) { // Pode ser usado se houver um caminho intbus1 -> internalReg0 via internalStore.
            this.internalReg0 = intbus1.get();
        } else if (internalRegIndex == 1) {
            this.internalReg1 = intbus2.get(); // This internalReg1 is sourced from intbus2 for internalStore
        } else {
            throw new IllegalArgumentException("ULA: Índice de registrador interno inválido para internalStore: " + internalRegIndex);
        }
    }


    /**
     * Coloca o valor de um registrador interno da ULA em um barramento interno.
     * @param internalRegIndex O índice do registrador interno da ULA (0 ou 1).
     * 0: Coloca o valor de internalReg0 em intbus1.
     * 1: Coloca o valor de internalReg1 em intbus2.
     */
    public void internalRead(int internalRegIndex) {
        if (internalRegIndex == 0) {
            intbus1.put(this.internalReg0);
        } else if (internalRegIndex == 1) {
            intbus2.put(this.internalReg1);
        } else {
            throw new IllegalArgumentException("ULA: Índice de registrador interno inválido: " + internalRegIndex);
        }
    }
    
    /**
     * Realiza uma operação de adição: internalReg1 <- internalReg0 + internalReg1.
     * O resultado é armazenado em internalReg1.
     */
    public void add() {
        this.internalReg1 = this.internalReg0 + this.internalReg1;
    }

    /**
     * Realiza uma operação de subtração: internalReg1 <- internalReg0 - internalReg1.
     * O resultado é armazenado em internalReg1.
     */
    public void sub() {
        this.internalReg1 = this.internalReg0 - this.internalReg1;
    }

    /**
     * Realiza uma operação de incremento: internalReg1 <- internalReg1 + 1.
     * O resultado é armazenado em internalReg1.
     */
    public void inc() {
        this.internalReg1++;
    }

    /**
     * Realiza uma comparação subtraindo operand2 de operand1 e armazena o resultado em internalReg1.
     * Útil para instruções de comparação (JEQ, JGT, JLW).
     * A ULA APENAS realiza a operação; a Architecture é responsável por ler o resultado e setar as Flags.
     * @param operand1 O primeiro operando (minuendo).
     * @param operand2 O segundo operando (subtraendo).
     */
    public void compare(int operand1, int operand2) {
        this.internalReg0 = operand1; // Coloca o primeiro operando em internalReg0
        this.internalReg1 = operand2; // Coloca o segundo operando em internalReg1 (pois ULA.sub() usa ambos)
        // Agora, podemos chamar sub() para realizar a operação
        this.internalReg1 = this.internalReg0 - this.internalReg1; // Resultado em internalReg1
    }
}