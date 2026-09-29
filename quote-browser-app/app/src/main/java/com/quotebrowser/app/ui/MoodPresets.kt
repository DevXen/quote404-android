package com.quotebrowser.app.ui

/**
 * v1.66 mood presets: one-tap category sets shared by Search and Random.
 * Category names match the seeded category names (matched by name in
 * every database, like the category filter itself).
 */
data class MoodPreset(
    val label: String,
    val emoji: String,
    val categories: List<String>
)

val MOOD_PRESETS = listOf(
    MoodPreset(
        label = "Make me laugh",
        emoji = "\uD83D\uDE02",
        categories = listOf("Funny", "Dark humor", "Puns", "Ironic")
    ),
    MoodPreset(
        label = "Make me think",
        emoji = "\uD83E\uDD14",
        categories = listOf("Philosophy", "Science")
    ),
    MoodPreset(
        label = "Disturb me",
        emoji = "\uD83D\uDE08",
        categories = listOf("Sexual", "Dark humor")
    ),
    MoodPreset(
        label = "Geek me out",
        emoji = "\uD83E\uDD13",
        categories = listOf("Programming", "Linux", "Tech", "Gaming", "Science")
    ),
    MoodPreset(
        label = "Warm my heart",
        emoji = "\uD83D\uDC9B",
        categories = listOf("Relationships", "Animals", "Music")
    ),
    MoodPreset(
        label = "Roast me",
        emoji = "\uD83D\uDD25",
        categories = listOf("Insults", "Ironic", "Dark humor")
    )
)

/**
 * v1.71: moods are additive toggle filters. A mood is "active" when all of
 * its categories are in the current selection; toggling an active mood off
 * removes its categories but keeps any that belong to another still-active
 * mood (e.g. "Dark humor" survives when both "Disturb me" and "Roast me"
 * are on and only one is toggled off). Pure functions so Search and Random
 * share the exact same behavior.
 */
fun moodNameToId(categories: List<com.quotebrowser.app.data.Category>): Map<String, Long> =
    categories.associate { it.name.trim().lowercase() to it.id }

fun isMoodActive(
    currentIds: Set<Long>,
    preset: MoodPreset,
    nameToId: Map<String, Long>
): Boolean {
    val ids = preset.categories.mapNotNull { nameToId[it.trim().lowercase()] }.toSet()
    return ids.isNotEmpty() && ids.all { it in currentIds }
}

fun toggleMoodSelection(
    currentIds: Set<Long>,
    preset: MoodPreset,
    nameToId: Map<String, Long>
): Set<Long> {
    fun idsOf(p: MoodPreset): Set<Long> =
        p.categories.mapNotNull { nameToId[it.trim().lowercase()] }.toSet()
    val presetIds = idsOf(preset)
    if (presetIds.isEmpty()) return currentIds
    if (!isMoodActive(currentIds, preset, nameToId)) return currentIds + presetIds
    val keep = MOOD_PRESETS
        .filter { it.label != preset.label }
        .filter { other -> idsOf(other).let { o -> o.isNotEmpty() && o.all { it in currentIds } } }
        .flatMapTo(mutableSetOf()) { idsOf(it) }
    return (currentIds - presetIds) + keep
}
