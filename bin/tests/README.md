# Arquivos de Teste - Projeto OAC

Esta pasta contém todos os arquivos de teste organizados para o projeto da Arquitetura C.

## Estrutura dos Testes

### 📁 Testes Principais da Arquitetura

#### **TestArquiteturaC.java** ⭐
- **Descrição**: Teste abrangente de todos os componentes da Arquitetura C
- **Funcionalidades**: Instruções básicas, operações de stack, CALL/RET, componentes
- **Status**: ✅ Completo e funcional

#### **TestArchitecture.java** 
- **Descrição**: Testes unitários originais da arquitetura (JUnit)
- **Funcionalidades**: Testes individuais de cada instrução
- **Status**: ✅ Funcional (requer JUnit)

### 📁 Testes de Instruções Específicas

#### **TestBasicInstructions.java**
- **Descrição**: Análise detalhada das instruções básicas
- **Funcionalidades**: LDI, INC, ADD com debug detalhado
- **Status**: ✅ Funcional

#### **TestLDICorrect.java**
- **Descrição**: Teste correto da instrução LDI
- **Funcionalidades**: LDI, INC, ADD com setup adequado de memória
- **Status**: ✅ Funcional

### 📁 Testes de CALL/RET e Stack

#### **TestCallRetComplete.java**
- **Descrição**: Teste completo das instruções CALL e RET
- **Funcionalidades**: Cenários simples e complexos de subrrotinas
- **Status**: ✅ Funcional

#### **TestFinal.java**
- **Descrição**: Teste final de validação da funcionalidade CALL/RET
- **Status**: ✅ Funcional

#### Testes de Debug CALL/RET:
- **TestCallRet.java**: Teste básico CALL/RET
- **TestCallOnly.java**: Teste isolado da instrução CALL
- **TestCallOnlyFixed.java**: Versão corrigida do teste CALL
- **TestCallRetDebug.java**: Debug detalhado CALL/RET
- **TestCallRetDebug2.java**: Debug avançado
- **TestCallRetDebug3.java**: Debug final

### 📁 Testes de Stack

#### **TestStack.java**
- **Descrição**: Testes básicos do sistema de stack
- **Status**: ✅ Funcional

#### **TestStackMemory.java**
- **Descrição**: Testes de integração stack-memória
- **Status**: ✅ Funcional

#### **TestStackEdgeCases.java**
- **Descrição**: Testes de casos extremos do stack
- **Status**: ✅ Funcional

#### **TestStackPushDetailed.java**
- **Descrição**: Análise detalhada das operações push
- **Status**: ✅ Funcional

### 📁 Testes de Componentes

#### **TestUla.java**
- **Descrição**: Testes unitários da ULA (JUnit)
- **Funcionalidades**: Operações aritméticas, stack
- **Status**: ✅ Funcional (requer JUnit)

#### **TestMemory.java**
- **Descrição**: Testes unitários da memória (JUnit)
- **Status**: ✅ Funcional (requer JUnit)

#### **TestRegister.java**
- **Descrição**: Testes unitários dos registradores (JUnit)
- **Status**: ✅ Funcional (requer JUnit)

#### **TestBus.java**
- **Descrição**: Testes unitários dos barramentos (JUnit)
- **Status**: ✅ Funcional (requer JUnit)

#### **TestMemoryBasic.java**
- **Descrição**: Testes básicos de memória sem JUnit
- **Status**: ✅ Funcional

### 📁 Testes com Assembler

#### **testAssembler.java**
- **Descrição**: Testes unitários do assembler (JUnit)
- **Status**: ✅ Funcional (requer JUnit)

#### **TestAssemblerCallRet.java**
- **Descrição**: Testes de CALL/RET com assembler
- **Status**: ✅ Funcional

#### **TestWithAssembler.java**
- **Descrição**: Testes integrados usando assembler
- **Status**: ✅ Funcional

## Como Executar os Testes

### Compilação:
```bash
# Compilar todos os componentes principais
javac -d bin src\architecture\*.java src\components\*.java src\assembler\*.java

# Compilar um teste específico
javac -d bin -cp bin src\tests\TestArquiteturaC.java
```

### Execução:
```bash
# Executar teste principal
java -cp bin TestArquiteturaC

# Executar teste específico
java -cp bin TestLDICorrect
```

## Recomendações de Uso

### Para Validação Geral:
1. **TestArquiteturaC.java** - Validação completa da arquitetura
2. **TestLDICorrect.java** - Validação das instruções básicas

### Para Debug de CALL/RET:
1. **TestCallRetComplete.java** - Teste abrangente
2. **TestFinal.java** - Validação final

### Para Análise de Componentes:
1. **TestUla.java** - ULA e stack
2. **TestMemory.java** - Sistema de memória
3. **TestStack.java** - Operações de stack

## Status Geral: ✅ TODOS OS TESTES ORGANIZADOS E FUNCIONAIS
