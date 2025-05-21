# FMCv2

Flow Modifier Design Calculator

## Project Purpose

FMCv2 is a tool for designing flow modifiers. It calculates design parameters and dimensions for fluid flow modifications.

## Building

Use the Gradle wrapper to build the project. The wrapper downloads the required
Gradle distribution the first time it runs, so network access is needed unless
you supply the distribution zip in `gradle/wrapper`.

```bash
./gradlew build
```

The build output appears in the `build` directory.

## Running Tests

Execute the test suite with:

```bash
./gradlew test
```
The wrapper can run completely offline using the `--offline` flag:

```bash
./gradlew --offline test
```

Test results are written to `build/reports/tests`.

## Running the Application

Launch the JavaFX interface with:

```bash
./gradlew run
```

## License

This project is licensed under the [Apache License 2.0](LICENSE).
