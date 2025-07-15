package components;

public class Register {
    private String name;
    private int value;
    private Bus extbus; // Referência ao barramento externo
    private Bus intbus1; // Referência ao barramento interno 1 (se o registrador for conectado a ele)
    private Bus intbus2; // Referência ao barramento interno 2 (se o registrador for conectado a ele)

    // Construtor para registradores de uso geral que se conectam a todos os barramentos
    public Register(String name, Bus extbus, Bus intbus1, Bus intbus2) {
        this.name = name;
        this.value = 0;
        this.extbus = extbus;
        this.intbus1 = intbus1;
        this.intbus2 = intbus2;
    }

    // Sobrecarga do construtor para o registrador Flags, que no seu exemplo
    // parece se conectar apenas ao intbus2 para leitura de seu valor, e ao extbus
    // para colocar um bit específico.
    // E, mais importante, ele pode ser de um tamanho menor (ex: 2 bits).
    // O construtor original em Architecture.java era: Flags = new Register(2, intbus2);
    // Assumo que o "2" é o ID do registrador, e não o tamanho.
    // Se "2" for o ID, então é um registrador normal.
    // No entanto, seu setStatusFlags usa Flags.setBit(index, value), o que indica que Flags
    // é um tipo de registrador com lógica de bits.
    // Vamos criar um construtor específico para o Registrador de Flags que não tem o name.
    
    // Construtor específico para o Registrador de FLAGS
    // Assumimos que o Register de Flags precisa apenas do intbus2 para ler resultados da ULA
    // e do extbus para colocar bits de flag.
    public Register(Bus extbus, Bus intbus2) { // Não precisa de 'name' aqui, se for sempre "Flags"
        this.name = "Flags"; // Nome fixo
        this.value = 0; // Inicializa todas as flags como 0
        this.extbus = extbus;
        this.intbus1 = null; // Flags não usa intbus1 diretamente para receber entradas
        this.intbus2 = intbus2; // ULA escreve resultados aqui, e Flags "lê" para setar bits
    }
    // O construtor original do seu Architecture.java para Flags era "new Register(2, intbus2)".
    // Isso é um pouco ambíguo. Se o "2" for o tamanho dos bits, teríamos um construtor para isso.
    // Pela sua implementação de setStatusFlags (Flags.setBit(0,1)),
    // parece que "Flags" é um registrador que guarda múltiplos bits de status.
    // Vou manter o construtor acima, assumindo que Flags é um tipo de Register especial.
    // Se o "2" for ID, então é um Register normal.

    // --- Métodos de Leitura/Escrita de Dados ---
    // Estes métodos simulam a interação com os barramentos.

    /**
     * Coloca o valor do registrador no barramento externo (extbus).
     * Usado para enviar dados do registrador para a memória ou outro componente via extbus.
     * Corresponde a `RPG.read();` ou `PC.read();` no seu `Architecture.java`.
     */
    public void read() {
        extbus.put(this.value);
        // System.out.println(name + " leu " + this.value + " para extbus."); // Para depuração
    }

    /**
     * Armazena o valor do barramento externo (extbus) no registrador.
     * Usado para receber dados da memória ou outro componente via extbus.
     * Corresponde a `RPG.store();` ou `PC.store();` no seu `Architecture.java`.
     */
    public void store() {
        this.value = extbus.get();
        // System.out.println(name + " armazenou " + this.value + " de extbus."); // Para depuração
    }

    /**
     * Coloca o valor do registrador no barramento interno 1 (intbus1).
     * Usado para enviar dados do registrador como primeiro operando para a ULA.
     * Corresponde a `RPG.internalRead();` no seu `Architecture.java`.
     */
    public void internalRead() {
        if (intbus1 == null) {
            System.err.println("Atenção: " + name + " tentou usar intbus1 mas não está conectado.");
            return;
        }
        intbus1.put(this.value);
        // System.out.println(name + " leu " + this.value + " para intbus1."); // Para depuração
    }
    
    /**
     * Armazena o valor do barramento interno 2 (intbus2) no registrador.
     * Usado para receber dados da ULA (resultado) ou de outros registradores via intbus2.
     * Corresponde a `PC.internalStore();` no seu `Architecture.java`.
     */
    public void internalStore() {
        if (intbus2 == null) {
            System.err.println("Atenção: " + name + " tentou usar intbus2 para store mas não está conectado.");
            return;
        }
        this.value = intbus2.get();
        // System.out.println(name + " armazenou " + this.value + " de intbus2."); // Para depuração
    }


    // --- Métodos de Acesso e Modificação de Valor ---

    public String getRegisterName() { // Renomeado de getName() para evitar conflito com Architecture.java RPG.getRegisterName()
        return name;
    }

    public int getData() { // Renomeado de get() para evitar conflito com Architecture.java RPG.getData()
        return value;
    }

    public void setData(int value) { // Renomeado de set()
        this.value = value;
    }

    public void inc() {
        this.value++;
    }

    public void dec() {
        this.value--;
    }

    // --- Métodos para Manipulação de Bits (Especialmente para o Registrador de Flags) ---
    /**
     * Define um bit específico no registrador.
     * Usado principalmente para o registrador de Flags.
     * @param bitIndex O índice do bit (0 para Zero Flag, 1 para Negative Flag, etc.)
     * @param bitValue O valor do bit (0 ou 1).
     */
    public void setBit(int bitIndex, int bitValue) {
        if (bitValue == 1) {
            this.value |= (1 << bitIndex); // Seta o bit para 1
        } else {
            this.value &= ~(1 << bitIndex); // Seta o bit para 0
        }
    }

    /**
     * Obtém o valor de um bit específico no registrador.
     * Usado principalmente para o registrador de Flags.
     * @param bitIndex O índice do bit.
     * @return 1 se o bit estiver setado, 0 caso contrário.
     */
    public int getBit(int bitIndex) {
        return (this.value >> bitIndex) & 1;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}