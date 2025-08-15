#!/bin/bash

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker Desktop first."
    echo "Visit https://www.docker.com/products/docker-desktop/"
    exit 1
fi

# Check if XQuartz is installed
if ! command -v xquartz-check &> /dev/null && ! [ -d "/Applications/Utilities/XQuartz.app" ]; then
    echo "XQuartz is required but not installed. Installing XQuartz..."
    brew install --cask xquartz || {
        echo "Failed to install XQuartz. Please install manually:"
        echo "brew install --cask xquartz"
        exit 1
    }
    echo "Please log out and log back in to complete XQuartz installation."
    exit 1
fi

# Build application
echo "Building FileWatcher application..."
mvn clean package

# Download JavaFX modules
echo "Downloading JavaFX modules..."
mkdir -p ./jfx-libs
mvn dependency:copy-dependencies -DoutputDirectory=./jfx-libs -DincludeGroupIds=org.openjfx

# Build Docker image
echo "Building FileWatcher Docker image..."
cp -r ./jfx-libs/* ./target/lib/ || mkdir -p ./target/lib/
docker build -t filewatcher:latest .

# Create launcher script
cat > filewatcher << EOF
#!/bin/bash

# Check if XQuartz is running
if ! ps -e | grep -q "[X]Quartz"; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 3
fi

# Configure XQuartz to allow connections
xhost + 127.0.0.1

# Run the Docker container
docker run --rm -e DISPLAY=host.docker.internal:0 -v "\$HOME/.filewatcher:/app/data" filewatcher:latest
EOF

chmod +x filewatcher

# Install the launcher
echo "Installing FileWatcher..."
sudo mkdir -p /usr/local/bin
sudo mv filewatcher /usr/local/bin/

# Create macOS application bundle
mkdir -p ~/Applications/FileWatcher.app/Contents/MacOS
mkdir -p ~/Applications/FileWatcher.app/Contents/Resources

# Copy icon 
cp target/classes/icons/FileWatcherIcons.png ~/Applications/FileWatcher.app/Contents/Resources/icon.icns

# Create Info.plist
cat > ~/Applications/FileWatcher.app/Contents/Info.plist << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>filewatcher-launcher</string>
    <key>CFBundleIconFile</key>
    <string>icon.icns</string>
    <key>CFBundleIdentifier</key>
    <string>com.tcss.filewatcher</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>FileWatcher</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
</dict>
</plist>
EOF

# Create app launcher script
cat > ~/Applications/FileWatcher.app/Contents/MacOS/filewatcher-launcher << EOF
#!/bin/bash
/usr/local/bin/filewatcher
EOF

chmod +x ~/Applications/FileWatcher.app/Contents/MacOS/filewatcher-launcher

echo ""
echo "=== Installation Complete! ==="
echo "You can run FileWatcher by:"
echo "1. Typing 'filewatcher' in Terminal"
echo "2. Opening ~/Applications/FileWatcher.app"
echo ""
echo "Note: Make sure Docker Desktop is running before launching the application."

