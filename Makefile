.PHONY: build install clean stop prod prod-install emulator

# Default task: Builds the debug APK
build:
	@echo "Building Debug APK..."
	./gradlew assembleDebug

prod:
	@echo "Building Production APK and copying it to desktop..."
	./gradlew assembleRelease
	cp app/build/outputs/apk/release/app-release.apk ~/Mutual/app-release.apk

# Launches the emulator if no device is connected and waits for boot
emulator:
	@if ! adb devices | grep -wq "device"; then \
		echo "No device detected. Starting emulator..."; \
		~/repos/scripts/emulator.sh; \
		echo "Waiting for emulator to initialize..."; \
		adb wait-for-device; \
		echo "Waiting for boot to complete..."; \
		until [ "$$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do \
			sleep 2; \
		done; \
		echo "Device ready."; \
	else \
		echo "Device/Emulator already running."; \
	fi

# Builds and installs the built APK to a connected device or emulator
dev-install: emulator
	@echo "Building and installing to device..."
	./gradlew installDebug

# Installs the production APK to a connected device or emulator
install: emulator
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