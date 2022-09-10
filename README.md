# Interactive illustration for Quine-McCluskey algorithm
This repository contains code of an interactive version of
the [Quine-McCluskey algorithm](https://en.wikipedia.org/wiki/Quine%E2%80%93McCluskey_algorithm)
hosted at [podkopaev.net/qmc](https://podkopaev.net/qmc).

The project is written using Kotlin/JS and React.

### The project structure
The folder [src/main/kotlin/](src/main/kotlin) contains the following files
- [QMClogic.kt](src/main/kotlin/QMClogic.kt)&mdash;runs QMC and stores intermediate data structures to be presented on the webpage.
- [QMCui.kt](src/main/kotlin/QMCui.kt)&mdash;describes conversion from the QMC state to React primitives.
- [Client.kt](src/main/kotlin/Client.kt)&mdash;starts the webpage.

