@echo off
cd /d "%~dp0"
title PTU Mestre
echo Iniciando o PTU... a pagina do Mestre abre sozinha no navegador.
echo Para encerrar, feche esta janela.
"runtime\bin\java.exe" -Dptu.release=true --enable-native-access=ALL-UNNAMED -cp "app\ptu.jar;lib\sqlite-jdbc.jar" Server.Server
pause
