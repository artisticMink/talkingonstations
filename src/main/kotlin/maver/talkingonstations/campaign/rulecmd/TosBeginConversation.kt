package maver.talkingonstations.campaign.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosSettings
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.httpapi.HttpApiRegistry
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown
import maver.talkingonstations.llm.mixins.CommsScene
import maver.talkingonstations.ui.trichat.TriChatCustomVisualPanel

/**
 * Hook, gets executed whenever a conversation with a market contact is started.
 * @see /data/campaign/rules.csv
 */
class TosBeginConversation : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token?>,
        memoryMap: MutableMap<String?, MemoryAPI?>?
    ): Boolean {
        if (dialog == null) return false

        val person = dialog.interactionTarget.activePerson

        // Hijack if this is a ToS person
        if (person == null || !person.memory.getBoolean(TosMemoryKeys.CHAT_ENABLED)) return false

        // We need at least one HTTP API to make use of the UI
        if (HttpApiRegistry.getDefaultApi() == null) return false

        // Set up the context
        val chat = Chat(
            Global.getSector().playerPerson,
            person,
            person.market,
        )

        val scene = CommsScene()
        scene.section = Section.SCENE
        chat.addMixin(scene)

        chat.beforeContinueAsPlayer = { message -> TosEveryFrameScriptQueue.add { dialog.textPanel.addParagraph(message, Misc.getBasePlayerColor()) } }
        chat.afterChatResponse = { message -> TosEveryFrameScriptQueue.add { dialog.textPanel.addPara(message) } }
        chat.onProgress = { message -> TosEveryFrameScriptQueue.add { dialog.textPanel.addPara(message.content, Misc.getHighlightColor()) } }

        // Set up the UI
        val chatUi = TriChatCustomVisualPanel(
            dialog.visualPanel,
            Global.getSector().playerPerson,
            person,
        )

        chatUi.onRetryButtonClick = { dialog.textPanel.replaceLastParagraph(""); chat.retryLastMessage() }
        chatUi.onSendButtonClick = { message -> chat.continueChatAsPlayer(message) }
        chatUi.onPlayerQuit = {
            // Persistent npc memory
            if (TosSettings.enableCharacterMemory) {
                chat.summarizeToNpcMemory()
            }

            dialog.dismiss()
        }

        // Disable ui buttons when npc terminates the conversation
        chat.onEnded = { message ->
            chatUi.markEnded()
            TosEveryFrameScriptQueue.add {
                dialog.textPanel.addPara(message)
                dialog.textPanel.addPara(
                    "Your comms officer informs you that the connection has been severed.",
                    Misc.getHighlightColor()
                )
            }
        }

        // Whether the npc opens the conversation
        if (TosSettings.npcStartsChat) {
            chatUi.launch {
                chat.continueChatAsSystem(markdown {
                    p("${Global.getSector().playerPerson.nameString} is calling you. You can:")
                    list(
                        listOf(
                            "Respond directly with a message.",
                            "Terminate the call permanently by using the end_conversation tool. This should only be done when ${person.nameString} does not want to talk."
                        )
                    )
                })
            }
        }

        dialog.interactionTarget.activePerson = person

        return true
    }
}