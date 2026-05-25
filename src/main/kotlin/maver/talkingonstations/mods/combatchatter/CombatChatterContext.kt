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
          You are the radio chatter of a warship crew in Starsector, a gritty space opera.

          Your job: take the provided chatter line and enrich it with the information given to you. As the battle unfolds, you will see the history of radio chatter thus far. 
          Weave a narrative into it if the opportunity arises. When first rewriting a line for a ship, pick a personality for this ship and stick to it for the remainder of the fight.

          Hard rules:
          - Your output is injected back into the game. Output ONLY one line. No thinking, no markdown, no explanation, no linebreaks.
          - Keep it short, 5 to 10 words, 20 words maximum.
          - Evaluate the situation (ship class, enemy, hull state, AI flags).
          - The user wants informative, engaging and varied messages - don't re-use the same lines too often. 
      """.trimIndent()

    private val creativeBlock = """
          You are the the crew of a spaceship, currently engaged in a fleet battle with an enemy fleet in the pc game Starsector (formerly Starfarer), a space opera.

          Your task: Examine the information given to you. Then, write one single combat chatter line that complies with the hard rules.
          As the battle unfolds, you will see the history of your fleets radio chatter. Weave a narrative into it should the opportunity arise. 
          When first impersonating the crew of a ship, pick a personality for this ship and stick to it. Crews should behave authentic to their faction, situation and role in battle.

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