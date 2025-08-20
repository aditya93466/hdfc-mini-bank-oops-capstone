@echo off
setlocal enabledelayedexpansion
if not exist out mkdir out
for /r %%f in (*.java) do (
  if not defined files (
    set files="%%f"
  ) else (
    set files=!files! "%%f"
  )
)
javac -d out %files%
java -cp out com.hdfc.minibank.Main
