## Usage

Create an `application.yml` file in the [places Spring Boot looks](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files) that contains mappings of application/repo name to source directory, such as:

```yaml
app:
  app-paths:
    rcon-cli: "C:\Users\user\git\rcon-cli"
```

Run it with:

```shell
.\gradlew bootRun
```