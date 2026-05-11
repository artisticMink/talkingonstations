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
}