package maver.talkingonstations

import java.lang.ref.WeakReference

sealed class TosStrings {
    object ModConfig {
        const val ID = "maver_talkingonstations"
    }

    object Loader {
        const val PATH = "data/config/tos/"
        const val CSV_ID_COLUMN = "id"
        const val CLASS_ID_COLUMN = "fullyQualifiedClassName"
    }

    object Messages {
    }
}