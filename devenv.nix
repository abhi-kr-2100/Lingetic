{ pkgs, lib, config, inputs, ... }:

{
  packages = with pkgs; [
    docker
    ffmpeg
    go
    graalvm-ce
    gradle
    nodejs
    nodePackages.pnpm
    uv
  ];

  env = {
    JAVA_HOME = "${pkgs.graalvm-ce.home}";
    DOCKER_HOST = "unix:///tmp/docker.sock";
  };

  processes = {
    docker.exec = "dockerd-rootless --host=unix:///tmp/docker.sock";
  };
}
