package maver.talkingonstations.characters.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PersonImportance
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Voices
import maver.talkingonstations.TosSettings
import maver.talkingonstations.characters.market.dto.MarketPersonData
import maver.talkingonstations.llm.ContextMixinInterface

class MarketPerson(market: MarketAPI, person: PersonAPI): PersonAPI by person {

    private var instructions: String = ""
    private var lore: String = ""
    private var knowledge: List<ContextMixinInterface> = listOf()

    init {
        market.commDirectory.addPerson(person);
    }

    companion object {
        fun create(market: MarketAPI, data: MarketPersonData): MarketPerson {
            val person = Global.getFactory().createPerson().apply {
                id = "tos_${data.name}_${data.hashCode()}"
                setFaction(data.faction)
                gender = FullName.Gender.valueOf(data.gender.name)
                postId = data.post ?: Ranks.POST_CITIZEN
                rankId = data.rank ?: Ranks.CITIZEN
                name.apply {
                    first = data.name
                    last = data.surename
                }
                portraitSprite = data.portrait
                voice = data.voice ?: Voices.SPACER
                importance = PersonImportance.MEDIUM
            }

            return MarketPerson(market, person).apply {
                instructions = data.tosInstructions ?: ""
                lore = data.tosLore ?: ""
                knowledge = data.tosKnowledge?.split(",")?.map { TosSettings.getContextMixins()
                    .first { mixin -> mixin.getKey() == it} } ?: listOf()

            }
        }
    }
}