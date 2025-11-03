package maver.talkingonstations.characters.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.characters.MarketPersonInterface

class CharacterFactory {
    companion object {
        fun create(market: MarketAPI): PersonAPI {

            val person: PersonAPI = Global.getFactory().createPerson()
            return person

            /**
                if (market != null) {
                    PersonAPI admin = Global.getFactory().createPerson();
                    admin.setId("scalar_Yurika");
                    admin.setFaction("scalartech");
                    admin.setGender(FullName.Gender.FEMALE);
                    admin.setPostId(Ranks.POST_FACTION_LEADER);
                    admin.setRankId(Ranks.FACTION_LEADER);
                    admin.getName().setFirst("Yurika");
                    admin.getName().setLast("Kusanagi");
                    admin.setImportance(PersonImportance.VERY_HIGH);
                    admin.setPersonality(Personalities.CAUTIOUS);
                    admin.setVoice(Voices.BUSINESS);
                    admin.setPortraitSprite("graphics/tahlan/portraits/yurika.png");

                    admin.getMemoryWithoutUpdate().set("$nex_preferredAdmin", true);
                    admin.getMemoryWithoutUpdate().set("$nex_preferredAdmin_factionId", "scalartech");
                    admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
                    admin.getStats().setLevel(1);

                    market.setAdmin(admin);
                    market.getCommDirectory().addPerson(admin, 0);
                    market.addPerson(admin);
                    PersonAPI silvys = Global.getFactory().createPerson();
                    silvys.setId("scalar_Silvys");
                    silvys.setFaction("scalartech");
                    silvys.setGender(FullName.Gender.FEMALE);
                    silvys.setPostId("scalar_headResearch");
                    silvys.setRankId("scalar_headResearch");
                    silvys.getName().setFirst("Silvys");
                    silvys.getName().setLast("Renham");
                    silvys.setPortraitSprite("graphics/tahlan/portraits/silvys.png");
                    silvys.setPersonality(Personalities.STEADY);
                    silvys.setVoice(Voices.SCIENTIST);
                    silvys.setImportance(PersonImportance.VERY_HIGH);
                    silvys.addTag(Tags.CONTACT_SCIENCE);
                    silvys.addTag(Tags.CONTACT_MILITARY);

                    silvys.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
                    silvys.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
                    silvys.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
                    silvys.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
                    silvys.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
                    silvys.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
                    silvys.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
                    silvys.getStats().setLevel(7);

                    market.getCommDirectory().addPerson(silvys,1);
                    market.addPerson(silvys);
                **/
        }
    }
}