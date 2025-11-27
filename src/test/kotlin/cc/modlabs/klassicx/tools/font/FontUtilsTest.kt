package cc.modlabs.klassicx.tools.font

import kotlin.test.Test
import kotlin.test.assertEquals

class FontUtilsTest {

    @Test
    fun toSmallCaps_basic_and_mixed_case() {
        // lower + upper, digits and punctuation should pass through
        assertEquals("ᴡᴏʀʟᴅ", toSmallCaps("world"))
        assertEquals("ᴀʙᴄ", toSmallCaps("AbC"))
        assertEquals("123-ᴛᴇsᴛ!", toSmallCaps("123-test!"))
    }

    @Test
    fun toSerif_basic_and_mixed_case() {
        assertEquals("𝑤𝑜𝑟𝑙𝑑", toSerif("world"))
        assertEquals("𝐴𝑏𝐶", toSerif("AbC"))
        assertEquals("123-𝑡𝑒𝑠𝑡!", toSerif("123-test!"))
    }

    @Test
    fun idempotency_when_applied_twice() {
        val smallOnce = toSmallCaps("Hello")
        val smallTwice = toSmallCaps(smallOnce)
        // Applying mapping twice should not change further
        assertEquals(smallOnce, smallTwice)

        val serifOnce = toSerif("Hello")
        val serifTwice = toSerif(serifOnce)
        assertEquals(serifOnce, serifTwice)
    }

    @Test
    fun unmapped_characters_passthrough() {
        val emojis = "😀🚀"
        assertEquals(emojis, toSmallCaps(emojis))
        assertEquals(emojis, toSerif(emojis))
    }
}
