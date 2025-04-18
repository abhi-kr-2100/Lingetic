{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    docker
    google-cloud-sdk
    graalvmPackages.graalvm-ce
    gradle
    jdk23
    nodejs
    nodePackages.pnpm
    ollama
    poppler-utils
    postgresql
    uv
  ];

  shellHook = ''
    export JAVA_HOME=${pkgs.jdk23.home}
    sed -i "s|/nix/store/[^/]\\+-openjdk-[^/]\\+/lib/openjdk|$JAVA_HOME|g" .vscode/settings.json
    export JAVA_HOME=${pkgs.graalvmPackages.graalvm-ce.home}

    echo "Setting up Docker and Ollama..."

    export DOCKER_HOST=unix:///tmp/docker.sock

    # Start Docker in rootless mode if not already running
    if ! docker info >/dev/null 2>&1; then
      echo "Starting Docker (rootless mode)..."
      dockerd-rootless --host=unix:///tmp/docker.sock > /dev/null 2>&1 &

      # Wait until Docker is responsive
      while ! docker info >/dev/null 2>&1; do
        sleep 1
      done
    fi

    # Start Ollama if not already running
    if ! pgrep -x ollama > /dev/null; then
      echo "Starting Ollama..."
      nohup ollama serve > /dev/null 2>&1 &
    fi

    echo "Ready!"
  '';
}
