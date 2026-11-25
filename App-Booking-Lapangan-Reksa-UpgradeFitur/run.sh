#!/bin/bash
# Compile dan jalankan aplikasi (butuh JDK 17+)
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name '*.java') && java -cp out Main
