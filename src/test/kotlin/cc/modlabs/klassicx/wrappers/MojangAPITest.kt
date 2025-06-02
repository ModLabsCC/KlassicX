package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.extensions.getLogger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.expect

class MojangAPITest {

    @Test
    fun getUser() {
        val userResponse = MojangAPI.getUser("LiamXSage")
        getLogger().info("Mojang API returned: {}", userResponse)
        assertNotNull(userResponse)
        expect( "03094b1335f643de8d13c37c3a9b941a") {
            return@expect userResponse!!.rawId
        }
    }

}