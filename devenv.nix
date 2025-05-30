{ pkgs, lib, config, inputs, ... }:

{
  packages = with pkgs; [
    awscli2
    docker
    ffmpeg
    go
    google-cloud-sdk
    graalvm-ce
    gradle
    jetbrains.idea-community
    nodejs
    nodePackages.pnpm
    ollama
    postgresql
    uv
  ];

  env = {
    JAVA_HOME = "${pkgs.graalvm-ce.home}";
    DOCKER_HOST = "unix:///tmp/docker.sock";
  };

  dotenv.enable = true;

  processes = {
    docker.exec = "dockerd-rootless --host=unix:///tmp/docker.sock";
    ollama.exec = "ollama serve";
  };
}
