package maver.talkingonstations.chat.context

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Skills
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.llm.ContextProviderInterface

class PlayerProvider() : ContextProviderInterface {
    private val player: PersonAPI = Global.getSector().playerPerson
    override var enabled: Boolean = false

    override fun canExecute(context: Chat.ChatContextInterface): Boolean = Global.getSector().playerPerson != null

    override fun getText(chatContext: Chat.ChatContextInterface): String {
        try {
            return "Player description (${playerName()})\n" +
                    "${playerName()} appears ${playerGender()}. Either by clothing or other means you can make out that ${player.heOrShe} ${playerFaction()}." +
                    "${playerName()} ${playerMarines()}. ${playerSkills()}"
        } catch (exception: Exception) {
            Global.getLogger(javaClass).error("Could not apply context provider. ${exception.message}")
            return ""
        }
    }

    private fun playerName() = player.name.fullName
    private fun playerGender() = when (player.name.gender) {
        FullName.Gender.ANY -> "without a clearly visible gender"
        FullName.Gender.FEMALE -> "female"
        FullName.Gender.MALE -> "male"
    }

    private fun playerFaction() =
        if (player.faction.displayNameLongWithArticle.equals("Your")) "does not appear to be visibly associated with any faction"
        else "is associated with ${player.faction.displayNameLongWithArticle}"

    private fun playerMarines() = when (Global.getSector().playerFleet.cargo.marines) {
        0 -> "is alone"
        1 -> "has a bodyguard accompanying ${player.himOrHer}"
        else -> "is followed by a more-or-less subtle entourage of armed marines"
    }

    private fun playerSkills(): String {
        val combatProficiency = getSkillLevelDescription(
            Skills.APT_COMBAT,
            arrayOf(
                "does not appear to be particularly intimidating",
                "appears to be able to hold up in a brawl - at least for a while",
                "appears to know how to handle the small-caliber gun hidden under ${player.hisOrHer} jacket",
                "appears fairly dangerous"
            )
        )

        val leadershipProficiency = getSkillLevelDescription(
            Skills.APT_LEADERSHIP,
            arrayOf(
                "appear to be ordinary at best",
                "seem less awkward than the usual suspects",
                "appear to have a way with words",
                "strike you as rather charismatic"
            )
        )

        val technologyProficiency = getSkillLevelDescription(
            Skills.APT_TECHNOLOGY,
            arrayOf(
                "rocking a standard-issue Tri-Pad that looks like it has witnessed the Askonia crisis first hand",
                "showing up distorted on the feed of your camera drone, showing that they deploy a decent face scrambler",
                ",for one reason or another, not appearing on your camera drones feed",
                "obviously not the person whose entry you pulled from the hegemony feed, probably just as fake as the non-existing person showing up on your camera drones video feed"
            )
        )

        val industryProficiency = getSkillLevelDescription(
            Skills.APT_INDUSTRY,
            arrayOf(
                "There's little indication that ${player.heOrShe} is accustomed to physical labour",
                "",
                "",
                "The frequency of market price updates passing trough the local relay tells you that you're talking to a little industry tycoon"
            )
        )

        return "In terms of combat ability the person in front of you $combatProficiency and are $technologyProficiency In terms of presence they $leadershipProficiency. $industryProficiency."
    }

    private fun getSkillLevelDescription(skill: String, levelDescription: Array<String>): String {
        val skillLevel = player.stats.getSkillLevel(skill)
        return when (skillLevel) {
            in 3f..5f -> levelDescription[1]
            in 6f..8f -> levelDescription[2]
            in 9f..99f -> levelDescription[3]
            else -> levelDescription[0]
        }
    }
}