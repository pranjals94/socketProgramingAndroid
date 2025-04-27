# Set the name of the Kotlin file (without extension)
$fileName = "Main"

# Compile Kotlin source code to a runnable JAR
Write-Host "Compiling $fileName.kt ..."
kotlinc "$fileName.kt" -include-runtime -d "$fileName.jar"

# Check if the JAR was created successfully
if (Test-Path "$fileName.jar") {
    Write-Host "Compilation successful. Running program..."
    Write-Host "-----------------------------------------"
    java -jar "$fileName.jar"
} else {
    Write-Host "Compilation failed. Please check your code."
}