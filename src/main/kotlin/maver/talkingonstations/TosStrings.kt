package maver.talkingonstations

sealed class TosStrings {
    object ModConfig {
        const val ID = "maver_talkingonstations"
    }

    object Loader {
        const val CSV_ID_COLUMN = "id"
        const val CLASS_ID_COLUMN = "fullyQualifiedClassName"
    }

    object Path {
        const val CONFIG_FOLDER = "data/config/tos/"
    }

    object Prompt {
        const val PREAMBLE = "You exist within the pc game Starsector (formerly Starfarer) and must act as an entity within the Starsector universe. \n\n" +
                "Below you will find instructions containing a helpful framework as well as an exhaustive dossier of references and information."
        const val TOOL_CALLING = "## Tool Calling\n\n" +
                "You can use tool calls to fetch additional information or initiate narrative events. \n\n" +
                "Separate complex tasks into multiple sequential tool calls."

        const val SUMMARY = "((ooc: Quick intermission - We are pausing the scene for now. To pick it up next time, please write a summary of the events thus far. Focus on the chat history and assume that all background information will be provided in the next chat. Beginn with a brief description of when {{player}} and {{npc}} last interacted, along the timestamp of {{sectorDate}}. Also work the last summary into it so we get a coherent chain of events. Make sure to stay within the budget of {{summaryBudget}} words. Thanks! ))."
    }
}