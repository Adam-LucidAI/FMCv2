# FMCv2

Flow Modifier Design Calculator

## Project Purpose

FMCv2 is a tool for designing flow modifiers. It calculates design parameters and dimensions for fluid flow modifications.

## Building

Use the Gradle wrapper to build the project. The required Gradle distribution is
included in `gradle/wrapper`, so no network access is needed.

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
