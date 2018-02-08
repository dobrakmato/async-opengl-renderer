if exist "C:\Users\Matej\Downloads\runtime\jre-x64\1.8.0_25\bin\java.exe" (
    @REM matov pc s vela javami
    "C:\Users\Matej\Downloads\runtime\jre-x64\1.8.0_25\bin\java.exe" -jar  -Djava.library.path="${release.natives}" "${finalName}"
) else (
    @REM normalny pc
    java.exe -jar -Djava.library.path="${release.natives}" "${finalName}"
)

