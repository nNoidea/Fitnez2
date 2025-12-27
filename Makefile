.PHONY: build install clean stop

# Default task: Builds the debug APK
build:
	@echo "Building Debug APK..."
	./gradlew assembleDebug

# Installs the built APK to a connected device or emulator
install:
	@echo "Installing to device..."
	./gradlew installDebug

# Cleans the build directory to save disk space
clean:
	@echo "Cleaning build artifacts..."
	./gradlew clean

stop:
	@echo "Stopping gradle daemon..."
	./gradlew --stop
