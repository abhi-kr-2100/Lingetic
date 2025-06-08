{ pkgs, lib, config, inputs, ... }:

{
  packages = with pkgs; [
    ffmpeg
    go
    graalvm-ce
    nodejs
    nodePackages.pnpm
    uv
  ];

  env = {
    JAVA_HOME = "${pkgs.graalvm-ce.home}";
  };

  dotenv.enable = true;
}
