#!/usr/bin/env bash
cd "$(dirname "$0")"
echo "Iniciando o PTU... a pagina do Mestre abre sozinha no navegador."
echo "Para encerrar, feche esta janela ou aperte Ctrl+C."
exec ./runtime/bin/java -Dptu.release=true --enable-native-access=ALL-UNNAMED -cp "app/ptu.jar:lib/sqlite-jdbc.jar" Server.Server
