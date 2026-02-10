.PHONY: build install clean stop prod prod-install

# Default task: Builds the debug APK
build:
	@echo "Building Debug APK..."
	./gradlew assembleDebug

prod:
	@echo "Building Production APK and copying it to desktop..."
	./gradlew assembleRelease
	cp app/build/outputs/apk/release/app-release.apk ~/Mutual/app-release.apk

# Builds and installs the built APK to a connected device or emulator
install:
	@echo "Building and installing to device..."
	./gradlew installDebug

# Installs the production APK to a connected device or emulator
prod-install:
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