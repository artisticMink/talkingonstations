# talkingonstations
A comprehensive LLM integration for Starsector

## Build the mod

### Dependencies

* JDK17
* Gradle 8+
* Lazy Lib
* Luna Lib
* Combat Chatter
* Console Commands

### HowTo

Execute the packageMod configuration from the intelliJ interface or manually run` gradle wrapper && ./gradlew packageMod`. The following requirements must be present:

A folder called libs/ with these jars:
* starfarer-api.jar
* log4j-1.2.9.jar
* json.jar
* LunaLib.jar
* LazyLib-Kotlin.jar
* lw_Console.jar
* CombatChatter.jar
* lwjgl.jar
* lwjgl_util.jar

A file `gradle.properites` in the root directory with `starsectorModFolder=<absolute-path-to-your-mod-folder>`