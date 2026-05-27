# Talking on Stations
A comprehensive LLM integration for Starsector

## Extending the mod

Most features of Talking on Stations are realized via [TosCsvLoader](src/main/kotlin/maver/talkingonstations/TosCsvLoader.kt) which uses Starsectors getMergedSpreadsheetDataForMod(). To extend any of the following features, a csv of the same name has to be placed in `<your_mod_folder>/data/config/tos/`

Merged loads: 
* Api.csv
* Archetypes.csv
* ContextMixin.csv
* MarketPerson.csv
* Tools.csv

## Custom HTTP API

### The HttpApiInterface

Every HTTP API must implement [HttpApiInterface](src/main/kotlin/maver/talkingonstations/httpapi/HttpApiInterface.kt). Please consult the interface documentation.

The functionally necessary convention is that send() must return an instance of [Message](src/main/kotlin/maver/talkingonstations/llm/dto/Message.kt), which in most circumstances will look similar to this:

```kotlin
return Message(
    role = ChatRoles.ASSISTANT,
    content = "The answer is: 42"
)
```

Running the http request within a IO context and returning user-facing error messages is not necessary but strongly encouraged for a good user experience. An example implementation can be found here: [OpenrouterHttpApi](src/main/kotlin/maver/talkingonstations/httpapi/Openrouterkt)

### Register your API

For your API to become available, an Api.csv must be present with two columns:
* fullyQualifiedClassName - As the name implies, the full name of your class. I.e.  maver.talkingonstations.httpapi.OpenrouterHttpApi
* supportsToolCalling - Whether the API can process tool calls. Either true or false. If in doubt, set false.

### Fetch your API

After loading a save, your API will be available via [HttpApiRegistry](src/main/kotlin/maver/talkingonstations/httpapi/HttpApiRegistry.kt)
```kotlin
// Get your API
val myApi: HttpApiInterface = HttpApiRegistry.getApi("Your APIs getName() result")
// Get all API names
val names: List<String>  = HttpApiRegistry.getApiNames()
```

## Build the mod

### Dependencies

* JDK17
* Gradle 8+
* Lazy Lib
* Luna Lib
* Combat Chatter
* Console Commands

### HowTo

Execute the packageMod configuration from the intelliJ interface or manually run` gradle wrapper && ./gradlew packageMod`. The following requirements must be met:

A folder named libs/ with these jars:
* starfarer-api.jar
* log4j-1.2.9.jar
* json.jar
* LunaLib.jar
* LazyLib-Kotlin.jar
* lw_Console.jar
* CombatChatter.jar
* lwjgl.jar
* lwjgl_util.jar

A file `gradle.properties.` in the root directory with `starsectorModFolder=<absolute-path-to-your-mod-folder>`