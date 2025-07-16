@echo off
echo ===============================================
echo  SCRIPT DE TESTES - PROJETO ARQUITETURA C
echo ===============================================

echo.
echo [1/3] Compilando componentes principais...
javac -d bin src\architecture\*.java src\components\*.java src\assembler\*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao dos componentes principais
    pause
    exit /b 1
)

echo [2/3] Compilando testes...
javac -d bin -cp bin src\tests\TestArquiteturaC.java src\tests\TestLDICorrect.java src\tests\TestCallRetComplete.java

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao dos testes
    pause
    exit /b 1
)

echo [3/3] Executando testes principais...
echo.

echo ===============================================
echo  TESTE 1: ARQUITETURA C COMPLETA
echo ===============================================
java -cp bin TestArquiteturaC

echo.
echo ===============================================
echo  TESTE 2: INSTRUCOES BASICAS
echo ===============================================
java -cp bin TestLDICorrect

echo.
echo ===============================================
echo  TESTE 3: CALL/RET COMPLETO
echo ===============================================
java -cp bin TestCallRetComplete

echo.
echo ===============================================
echo  TODOS OS TESTES CONCLUIDOS
echo ===============================================
pause
