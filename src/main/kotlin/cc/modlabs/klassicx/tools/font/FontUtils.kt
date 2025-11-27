package cc.modlabs.klassicx.tools.font

import kotlin.text.iterator

/**
 * Mapping from regular characters to small caps Unicode characters
 * Small caps: ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘꞯʀsᴛᴜᴠᴡxʏᴢ
 */
private val SMALL_CAPS_MAP: Map<Char, String> = mapOf(
    'a' to "ᴀ", 'b' to "ʙ", 'c' to "ᴄ", 'd' to "ᴅ", 'e' to "ᴇ",
    'f' to "ғ", 'g' to "ɢ", 'h' to "ʜ", 'i' to "ɪ", 'j' to "ᴊ",
    'k' to "ᴋ", 'l' to "ʟ", 'm' to "ᴍ", 'n' to "ɴ", 'o' to "ᴏ",
    'p' to "ᴘ", 'q' to "ꞯ", 'r' to "ʀ", 's' to "s", 't' to "ᴛ",
    'u' to "ᴜ", 'v' to "ᴠ", 'w' to "ᴡ", 'x' to "x", 'y' to "ʏ",
    'z' to "ᴢ",
    'A' to "ᴀ", 'B' to "ʙ", 'C' to "ᴄ", 'D' to "ᴅ", 'E' to "ᴇ",
    'F' to "ғ", 'G' to "ɢ", 'H' to "ʜ", 'I' to "ɪ", 'J' to "ᴊ",
    'K' to "ᴋ", 'L' to "ʟ", 'M' to "ᴍ", 'N' to "ɴ", 'O' to "ᴏ",
    'P' to "ᴘ", 'Q' to "ꞯ", 'R' to "ʀ", 'S' to "s", 'T' to "ᴛ",
    'U' to "ᴜ", 'V' to "ᴠ", 'W' to "ᴡ", 'X' to "x", 'Y' to "ʏ",
    'Z' to "ᴢ",
)

/**
 * Mapping from regular characters to serif/mathematical italic Unicode characters
 * Serif: 𝑎𝑏𝑐𝑑𝑒𝑓𝑔ℎ𝑖𝑗𝑘𝑙𝑚𝑛𝑜𝑝𝑞𝑟𝑠𝑡𝑢𝑣𝑤𝑥𝑦𝑧
 */
private val SERIF_MAP: Map<Char, String> = mapOf(
    'a' to "𝑎", 'b' to "𝑏", 'c' to "𝑐", 'd' to "𝑑", 'e' to "𝑒",
    'f' to "𝑓", 'g' to "𝑔", 'h' to "ℎ", 'i' to "𝑖", 'j' to "𝑗",
    'k' to "𝑘", 'l' to "𝑙", 'm' to "𝑚", 'n' to "𝑛", 'o' to "𝑜",
    'p' to "𝑝", 'q' to "𝑞", 'r' to "𝑟", 's' to "𝑠", 't' to "𝑡",
    'u' to "𝑢", 'v' to "𝑣", 'w' to "𝑤", 'x' to "𝑥", 'y' to "𝑦",
    'z' to "𝑧",
    'A' to "𝐴", 'B' to "𝐵", 'C' to "𝐶", 'D' to "𝐷", 'E' to "𝐸",
    'F' to "𝐹", 'G' to "𝐺", 'H' to "𝐻", 'I' to "𝐼", 'J' to "𝐽",
    'K' to "𝐾", 'L' to "𝐿", 'M' to "𝑀", 'N' to "𝑁", 'O' to "𝑂",
    'P' to "𝑃", 'Q' to "𝑄", 'R' to "𝑅", 'S' to "𝑆", 'T' to "𝑇",
    'U' to "𝑈", 'V' to "𝑉", 'W' to "𝑊", 'X' to "𝑋", 'Y' to "𝑌",
    'Z' to "𝑍",
)

/**
 * Converts text to small caps using Unicode small caps characters
 */
fun toSmallCaps(text: String): String {
    val out = StringBuilder(text.length)
    for (ch in text) {
        out.append(SMALL_CAPS_MAP[ch] ?: ch)
    }
    return out.toString()
}

/**
 * Converts text to serif/mathematical italic using Unicode characters
 */
fun toSerif(text: String): String {
    val out = StringBuilder(text.length)
    for (ch in text) {
        out.append(SERIF_MAP[ch] ?: ch)
    }
    return out.toString()
}