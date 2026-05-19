# talkingonstations
A comprehensive LLM integration for Starsector

## Build the mod

### Dependencies

* JDK17
* Gradle 8.14+
* LazyLib
* LunaLib
* ConsoleCommands

### Setup

Create a folder called libs/ and move these jars inside:

* starfarer-api.jar
* log4j-1.2.9.jar
* json.jar
* LunaLib.jar
* LazyLib-Kotlin.jar
* lw_Console.jar
* lwjgl.jar
* lwjgl_util.jar

Create a file `gradle.properites` in the root directory and add `starsectorModFolder=<absolute-path-to-your-mod-folder>`

Right-Click `build.gradle-kts` in intelliJ and sync the configuration or install Gradle manually. 

Execute the packageMod configuration from the intelliJ interface or manually run` gradle wrapper && ./gradlew packageMod`