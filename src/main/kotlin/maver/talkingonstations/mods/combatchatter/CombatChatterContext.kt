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
          - Output ONLY the spoken line. No ship prefix, no quotes, no narration, no stage directions, no markdown, no explanation. No linebreaks.
          - Keep it short, 5 to 10 words, 20 words maximum.
          - Evaluate the situation (ship class, enemy, hull state, AI flags).
          - The user wants informative, engaging and varied messages - don't re-use the same lines too often. 
      """.trimIndent()

    private val creativeBlock = """
          You are the radio chatter of a warship crew in Starsector, a gritty space opera.

          Your task: Examine the information given to you. Then, write one single combat chatter line that complies with the hard rules.
          As the battle unfolds, you will see the history of radio chatter thus far. Weave a narrative into it if the opportunity arises. 
          When first rewriting a line for a ship, pick a personality for this ship and stick to it.

          Hard rules:
          - Your output is injected back into the game. Output ONLY the spoken line. No quotes, no narration, no thinking, no markdown, no explanation, no linebreaks.
          - Keep it short, 5 to 10 words, 20 words at maximum.
          - Evaluate the situation (ship class, enemy, hull state, AI flags).
          - The user wants informative, engaging and varied messages - don't re-use the same lines often. 
      """.trimIndent()

    override fun getSystemBlock(): String {
        return if (TosSettings.modsCcOnlyRewrite) rewriteBlock else creativeBlock
    }

    override fun getMessagesCopy(): List<Message> {
       return messages.toList()
    }
}