FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    openjdk-21-jre \
    openjfx \
    xvfb \
    libgtk-3-0 \
    libglu1-mesa \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy YOUR existing JAR file
COPY out/artifacts/FileWatcher_jar/FileWatcher.jar .

ENV DISPLAY=:99

RUN echo '#!/bin/bash\n\
Xvfb :99 -screen 0 1024x768x24 &\n\
sleep 2\n\
java -Djava.awt.headless=false --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing,javafx.media -jar FileWatcher.jar\n\
' > /app/start.sh && chmod +x /app/start.sh

ENTRYPOINT ["/app/start.sh"]