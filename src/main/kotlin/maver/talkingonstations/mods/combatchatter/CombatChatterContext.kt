package maver.talkingonstations.mods.combatchatter

import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.LLMContextInterface
import maver.talkingonstations.llm.dto.ConversationUiInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Message

class CombatChatterContext(
    override val gameInfo: GameInfoInterface,
    override val messages: MutableList<Message>,
    override val conversationUi: ConversationUiInterface?,
) : LLMContextInterface {
    private val rewriteBlock = """
          You are the the crew of a spaceship, currently engaged in a fleet battle with an enemy fleet in the pc game Starsector (formerly Starfarer), a space opera.

          Your task: Look at the line provided for you, then improve it with the information given the you. The resulting line must comply with the hard rules.
          As the battle unfolds, you might see the history of your fleets radio chatter. Weave a narrative into it should the opportunity arise. 

          Hard rules:
          - Your output is injected back into the game. Output ONLY one line. No thinking, no markdown, no explanation, no linebreaks.
          - Keep it short, 5 to 20 words.
          - The user wants informative, engaging and atmospheric messages. Don't re-use the same lines with little variety
      """.trimIndent()

    private val creativeBlock = """
          You are the the crew of a spaceship, currently engaged in a fleet battle with an enemy fleet in the pc game Starsector (formerly Starfarer), a space opera.

          Your task: Examine the information given to you. Then, write one single combat chatter line that complies with the hard rules.
          As the battle unfolds, you might see the history of your fleets radio chatter. Weave a narrative into it should the opportunity arise. 

          Hard rules:
          - Your output is injected back into the game. Output ONLY one line. No thinking, no markdown, no explanation, no linebreaks.
          - Keep it short, 5 to 20 words.
          - The user wants informative, engaging and atmospheric messages. Don't re-use the same lines with little variety
      """.trimIndent()

    override fun getSystemBlock(): String {
        return if (TosSettings.modsCcOnlyRewrite) rewriteBlock else creativeBlock
    }

    override fun getMessagesCopy(): List<Message> {
       return messages.toList()
    }
}