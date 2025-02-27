package cc.modlabs.klassicx.extensions

import java.util.*


val Locale.code: String
    get() = this.language + (if (this.country.isNotEmpty()) "_$country" else "")