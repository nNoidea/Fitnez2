.PHONY: build install clean stop prod prod-install emulator check-device

# Default task: Builds the debug APK
build:
	@echo "Building Debug APK..."
	./gradlew assembleDebug

prod:
	@echo "Building Production APK and copying it to desktop..."
	./gradlew assembleRelease
	cp app/build/outputs/apk/release/app-release.apk ~/Mutual/app-release.apk

# Launches the emulator
emulator:
	@echo "Launching emulator..."
	~/repos/scripts/emulator.sh

# Checks if a device or emulator is connected
check-device:
	@adb devices | grep -wq "device" || (echo "Error: No device or emulator connected. Run 'make emulator' to start one." && exit 1)

# Builds and installs the built APK to a connected device or emulator
install: check-device
	@echo "Building and installing to device..."
	./gradlew installDebug

# Installs the production APK to a connected device or emulator
prod-install: check-device
	@echo "Installing Production APK..."
	./gradlew installRelease

# Cleans the build directory to save disk space
clean:
	@echo "Cleaning build artifacts..."
	./gradlew clean

stop:
	@echo "Stopping gradle daemon..."
	./gradlew --stop

test:
	@echo "Testing..."
	./gradlew connectedAndroidTest --rerun-tasks