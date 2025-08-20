#!/usr/bin/env bash
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.hdfc.minibank.Main
