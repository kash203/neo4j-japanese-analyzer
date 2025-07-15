# Use the official Microsoft DevContainer base image with Java
FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

# Set environment variables
ENV JAVA_HOME=/usr/local/sdkman/candidates/java/current
ENV MAVEN_HOME=/usr/share/maven
ENV PATH=$PATH:$MAVEN_HOME/bin

# Install additional tools and dependencies
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        curl \
        wget \
        unzip \
        git \
        vim \
        nano \
        tree \
        htop \
        jq \
    && apt-get autoremove -y && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Install Maven (if not already installed by the base image)
ARG MAVEN_VERSION=3.9.10
RUN if [ ! -f "/usr/bin/mvn" ]; then \
        wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
        && tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt \
        && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
        && ln -s /opt/maven/bin/mvn /usr/bin/mvn \
        && rm apache-maven-${MAVEN_VERSION}-bin.tar.gz; \
    fi

# Create workspace directory
WORKDIR /workspace

# Set up Maven local repository directory
RUN mkdir -p /home/vscode/.m2 && chown -R vscode:vscode /home/vscode/.m2

# Switch to vscode user
USER vscode

# Create Maven settings.xml with UTF-8 encoding support
RUN mkdir -p /home/vscode/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /home/vscode/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /home/vscode/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /home/vscode/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0' >> /home/vscode/.m2/settings.xml && \
    echo '                              http://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /home/vscode/.m2/settings.xml && \
    echo '  <localRepository>/home/vscode/.m2/repository</localRepository>' >> /home/vscode/.m2/settings.xml && \
    echo '  <profiles>' >> /home/vscode/.m2/settings.xml && \
    echo '    <profile>' >> /home/vscode/.m2/settings.xml && \
    echo '      <id>default</id>' >> /home/vscode/.m2/settings.xml && \
    echo '      <properties>' >> /home/vscode/.m2/settings.xml && \
    echo '        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>' >> /home/vscode/.m2/settings.xml && \
    echo '        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>' >> /home/vscode/.m2/settings.xml && \
    echo '      </properties>' >> /home/vscode/.m2/settings.xml && \
    echo '    </profile>' >> /home/vscode/.m2/settings.xml && \
    echo '  </profiles>' >> /home/vscode/.m2/settings.xml && \
    echo '  <activeProfiles>' >> /home/vscode/.m2/settings.xml && \
    echo '    <activeProfile>default</activeProfile>' >> /home/vscode/.m2/settings.xml && \
    echo '  </activeProfiles>' >> /home/vscode/.m2/settings.xml && \
    echo '</settings>' >> /home/vscode/.m2/settings.xml

# Set default shell to bash
SHELL ["/bin/bash", "-c"]

# Add helpful aliases
RUN echo 'alias ll="ls -la"' >> /home/vscode/.bashrc && \
    echo 'alias la="ls -la"' >> /home/vscode/.bashrc && \
    echo 'alias mvn-test="mvn clean test"' >> /home/vscode/.bashrc && \
    echo 'alias mvn-package="mvn clean package"' >> /home/vscode/.bashrc && \
    echo 'alias mvn-install="mvn clean install"' >> /home/vscode/.bashrc

# Expose Neo4j ports (for future Neo4j integration testing)
EXPOSE 7474 7687

# Set the default command
CMD ["/bin/bash"]
