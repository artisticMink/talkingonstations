package maver.talkingonstations.characters

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import maver.talkingonstations.extensions.randomWeighted
import kotlin.random.Random
import kotlin.random.asJavaRandom

class RandomPerson {
    companion object {
        fun create(faction: String = Factions.INDEPENDENT): PersonAPI {
            return Global.getSector()
                .getFaction(faction)
                .createRandomPerson(randomGender(), Random.asJavaRandom()) as PersonAPI
        }

        private fun randomGender(): Gender {
            return listOf(
                Gender.MALE to 50.0,
                Gender.FEMALE to 50.0,
                //Gender.ANY to 10.0 ToDo: Check if there are neutral pronouns for npcs
            ).randomWeighted()
        }
    }
}