package maver.talkingonstations.config

/**
 * In-memory view of one campaign's profile file
 *
 * @see /saves/common/talkingonstations/profiles/[TosCampaignId].json
 */
class TosProfileConfig {
    var profile: String? = null
    var commissionBackground: String? = null
    var factionBackground: String? = null

    var model: String? = null
    var reasoningEffort: String? = null
    var temperature: Double? = null
    var topP: Double? = null
    var topK: Double? = null
    var frequencyPenalty: Double? = null
    var presencePenalty: Double? = null
    var repetitionPenalty: Double? = null
    var maxTokens: Int? = null
}
