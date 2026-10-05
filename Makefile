.PHONY: build install clean stop prod prod-install emulator test test-unit coverage coverage-report detekt check screenshots

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

# Runs fast local unit, integration & UI tests on JVM
test-unit:
	@echo "Running local unit & integration tests..."
	./gradlew testDebugUnitTest

# Runs tests and prints coverage summary to terminal
coverage:
	@echo "Computing test coverage..."
	./gradlew koverLogDebug

# Generates HTML and XML coverage reports
coverage-report:
	@echo "Generating Kover HTML & XML coverage reports..."
	./gradlew koverHtmlReportDebug koverXmlReportDebug
	@echo "HTML report generated at: app/build/reports/kover/htmlDebug/index.html"

# Runs instrumented tests on connected device/emulator
test:
	@echo "Testing..."
	./gradlew connectedAndroidTest --rerun-tasks

# Static analysis: complexity, naming, dead code. Baseline-ratcheted,
# so only NEW violations fail the build. See config/detekt.yml
detekt:
	@echo "Running detekt..."
	./gradlew detekt

# Full pre-commit gate: static analysis + lint + unit tests
check:
	@echo "Running detekt, lint and unit tests..."
	./gradlew detekt lintDebug testDebugUnitTest

# Captures README screenshots (timeline, monthly, graph, settings) at 1080x2400
# in dark mode with Material You colors. Needs one running Android 12+ emulator;
# set ANDROID_SERIAL when several devices are attached. Uses a temporary
# emulator user so existing app data stays untouched.
screenshots:
	@echo "Capturing screenshots..."
	./scripts/screenshots.sh