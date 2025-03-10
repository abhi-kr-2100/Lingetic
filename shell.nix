{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    nodejs
    nodePackages.pnpm
    jdk23
    gradle
    uv
    ollama
    docker
    postgresql
  ];

  shellHook = ''
    export JAVA_HOME=${pkgs.jdk23}/lib/openjdk
    export PATH=${pkgs.jdk23}/bin:$PATH

    echo "Node.js version: $(node --version)"
    echo "PNPM version: $(pnpm --version)"
    echo "Java version: $(java --version)"
    echo "Gradle version: $(gradle --version)"
    echo "UV version: $(uv --version)"
    echo "Ollama version: $(ollama --version)"
    echo "Docker version: $(docker --version)"
    echo "Docker Compose version: $(docker compose version)"
    echo "PostgreSQL version: $(psql --version)"

    # Start Docker if not running
    if ! systemctl is-active --quiet docker; then
      sudo systemctl start docker
    fi

    # Ensure user is in the docker group
    if ! groups | grep -q "\bdocker\b"; then
      echo "You are not in the docker group. Add yourself with: sudo usermod -aG docker $USER"
    fi
  '';
}
