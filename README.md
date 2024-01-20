## Usage

Create an `application.yml` (or `application.properties`) file in the [default places Spring Boot looks](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files) or in `$HOME/.github-releases-proxy/` that contains mappings of application/repo name to source directory, such as:

```yaml
app:
  app-paths:
    rcon-cli: "C:\Users\user\git\rcon-cli"
```

In addition to explicitly configuring the application paths, the requested app name will be located in the current working directory. For example, if the app requested is `mc-server-runner` and the current directory contains the cloned repo in the directory `mc-server-runner`, then no explicit configuration is needed.

Run it with:

```shell
.\gradlew bootRun
```