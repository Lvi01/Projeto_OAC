package components;

public class Bus {
    private int data; // O valor que o barramento está transportando no momento

    public Bus() {
        this.data = 0; // Inicializa o barramento com 0
    }

    /**
     * Coloca um valor no barramento. Simula a escrita de um componente no barramento.
     * @param value O valor a ser colocado no barramento.
     */
    public void put(int value) {
        this.data = value;
        // System.out.println("DEBUG: Bus (put) -> " + value); // Para depuração
    }

    /**
     * Obtém o valor atualmente no barramento. Simula a leitura de um componente do barramento.
     * @return O valor atualmente no barramento.
     */
    public int get() {
        // System.out.println("DEBUG: Bus (get) -> " + this.data); // Para depuração
        return this.data;
    }

    // Os métodos anteriores (transferToInttbus1, transferToInttbus2, transferToExtbus, moveData)
    // que eram "pass-through" não serão mais usados diretamente.
    // Agora, os componentes (Registradores, ULA, Memória) chamarão diretamente
    // bus.put() e bus.get() para simular o fluxo de dados.
    // Mantê-los como placeholders pode ser confuso. Vou removê-los.
    // Se precisar de funcionalidades de "transferência com log" ou similares,
    // elas podem ser recriadas ou a depuração pode ser feita nos put/get.

    @Override
    public String toString() {
        return "Bus [data=" + data + "]";
    }
}