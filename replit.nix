{ pkgs }: {
    deps = [
      pkgs.iputils
      pkgs.iproute
      pkgs.nano
      pkgs.openssh_hpn
        pkgs.graalvm17-ce
        pkgs.maven
        pkgs.replitPackages.jdt-language-server
        pkgs.replitPackages.java-debug
    ];
}