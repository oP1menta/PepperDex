#!/usr/bin/env bash
# Builds the self-contained package (own Java runtime inside: the Master installs nothing) for the OS this runs on.
# Needs a JDK 21+ (javac, jar, jlink). Output: dist/PTU-Mestre/   (the GitHub workflow zips it per system)
set -euo pipefail
cd "$(dirname "$0")/.."

OUT=dist/PTU-Mestre
rm -rf build dist
mkdir -p build/classes "$OUT/app" "$OUT/lib"

# the program (plain classpath build: module-info.java is only for Eclipse)
javac --release 21 -d build/classes -cp lib/sqlite-jdbc.jar $(find src -name '*.java' ! -name module-info.java)
[ -d resources ] && cp -r resources/. build/classes/
jar --create --file "$OUT/app/ptu.jar" -C build/classes .
cp lib/sqlite-jdbc.jar "$OUT/lib/"

# a small Java runtime with only what the program uses (jdk.crypto.ec = https, for downloading the tunnel tool)
jlink --add-modules java.base,java.sql,java.logging,java.naming,jdk.httpserver,java.net.http,jdk.crypto.ec,jdk.unsupported \
      --strip-debug --no-header-files --no-man-pages --compress=zip-6 --output "$OUT/runtime"

# data, pages and launchers
cp -r web "$OUT/web"
cp pokemon.json "$OUT/"
cp packaging/Iniciar.bat packaging/iniciar.sh packaging/LEIA-ME.txt "$OUT/"
chmod +x "$OUT/iniciar.sh"

echo "Ready: $OUT ($(du -sh "$OUT" | cut -f1))"
