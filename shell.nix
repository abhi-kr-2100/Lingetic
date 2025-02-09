{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = [
    pkgs.nodejs
    pkgs.nodePackages.pnpm
    pkgs.jdk23
    pkgs.gradle
  ];

  shellHook = ''
    export JAVA_HOME=${pkgs.jdk23}/lib/openjdk
    export PATH=${pkgs.jdk23}/bin:$PATH

    echo "Node.js version: $(node --version)"
    echo "PNPM version: $(pnpm --version)"
    echo "Java version: $(java --version)"
    echo "Gradle version: $(gradle --version)"
  '';
}
