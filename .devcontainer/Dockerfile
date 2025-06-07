FROM ghcr.io/astral-sh/uv:0.7 AS uv_installer

FROM node:lts-alpine AS graphite_installer
ARG GRAPHITE_INSTALL_DIR=/opt/graphite-cli
RUN npm install -g @withgraphite/graphite-cli@stable && \
    mv /usr/local/lib/node_modules/@withgraphite/graphite-cli ${GRAPHITE_INSTALL_DIR}

FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04 AS graalvm_installer
ARG GRAALVM_DOWNLOAD_URL=https://download.oracle.com/graalvm/23/archive/graalvm-jdk-23_linux-x64_bin.tar.gz
ARG GRAALVM_INSTALL_DIR=/opt/graalvm-jdk
RUN curl -L "${GRAALVM_DOWNLOAD_URL}" -o "/tmp/graalvm.tar.gz" && \
    mkdir -p "/tmp/graalvm" && \
    tar -xzf "/tmp/graalvm.tar.gz" -C "/tmp/graalvm" && \
    rm "/tmp/graalvm.tar.gz" && \
    DIRECTORY=$(ls "/tmp/graalvm") && \
    mv "/tmp/graalvm/${DIRECTORY}" "${GRAALVM_INSTALL_DIR}" && \
    rm -rf /tmp/graalvm


FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04 AS devcontainer

# Required by tts.py
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Install uv
COPY --from=uv_installer /uv /uvx /bin/

# Install Graphite CLI
ARG GRAPHITE_INSTALL_DIR=/opt/graphite-cli
COPY --from=graphite_installer /opt/graphite-cli ${GRAPHITE_INSTALL_DIR}
RUN ln -s ${GRAPHITE_INSTALL_DIR}/graphite.js /usr/local/bin/gt

# Install GraalVM
ARG GRAALVM_INSTALL_DIR=/opt/graalvm-jdk
COPY --from=graalvm_installer /opt/graalvm-jdk ${GRAALVM_INSTALL_DIR}
ENV JAVA_HOME=${GRAALVM_INSTALL_DIR}
ENV PATH="${GRAALVM_INSTALL_DIR}/bin:${PATH}"
