# Talking on Stations
A comprehensive LLM integration for Starsector

## Settings

### Luna Settings

Settings located here affect all saves and contain fundamental settings like the choice of API, tool calling options and other mod integrations. 

### Profile

Settings located here only affect the current savegame and will only persist when the game is saved, with exception API Keys which are saved separately.

To open the Profile, go to the ability panel in the main campagne screen and add the 'Open TriChat' ability. By using this ability the characters profile opens.

#### Personal Tab

Everything in background field will be sent to the llm as custom character lore.

#### Faction Tab

Here you can add custom info about the player commission (i.e. got awarded the purple lobster medal for bravery in the face of a fuel shortage) or the player faction should it exists.

#### Settings Tab

Allows you to set up the api endpoint that will be used for all tasks. Please read the [API KEY SAFETY] section.

### API KEY SAFETY

Any api keys are saved to `Starsector/saves/common/talkingonstations/credentials.json.data`. This is a regular text file that can be read by other starsector mods which then can phone it home. There is no reasonable way to prevent this, to mitigate this consider:
* Limit the rights of the API keys you use to only the necessary
* Limit the usage of your API key to something you deem reasonable.

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

Running the http request within a IO context and returning user-facing error messages is not necessary but strongly encouraged for a good user experience. An example implementation can be found here: [OpenrouterHttpApi](src/main/kotlin/maver/talkingonstations/httpapi/OpenrouterHttpApi.kt)

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