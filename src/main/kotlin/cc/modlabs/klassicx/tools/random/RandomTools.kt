package cc.modlabs.klassicx.tools.random

import kotlin.random.Random

private const val UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
private const val DIGITS = "0123456789"

private fun buildMixedCaseAlphabet(randomizer: Random): String = buildString(UPPERCASE_LETTERS.length) {
    for (index in UPPERCASE_LETTERS.indices) {
        val upper = UPPERCASE_LETTERS[index]
        val lower = LOWERCASE_LETTERS[index]
        append(if (randomizer.nextBoolean()) upper else lower)
    }
}

fun randomTag(
    size: Int = 5,
    prefix: CharSequence? = "#",
    case: RandomTagType = RandomTagType.ONLY_UPPERCASE,
    randomizer: Random = Random(Random.nextLong()),
): String {
    val letters = when (case) {
        RandomTagType.ONLY_UPPERCASE -> UPPERCASE_LETTERS
        RandomTagType.ONLY_LOWERCASE -> LOWERCASE_LETTERS
        RandomTagType.MIXED_CASE -> buildMixedCaseAlphabet(randomizer)
    }
    val alphabet = letters + DIGITS

    return buildString((prefix?.length ?: 0) + size) {
        prefix?.let(::append)
        repeat(size) {
            append(alphabet[randomizer.nextInt(alphabet.length)])
        }
    }
}

enum class RandomTagType {
    ONLY_UPPERCASE,
    ONLY_LOWERCASE,
    MIXED_CASE,
}

fun randomSeedSet(amount: Int, random: Random = Random): Set<Long> = buildSet {
    repeat(amount) {
        add(random.nextLong())
    }
}
