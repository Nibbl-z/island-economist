package xyz.nibblz.galapagos.data

import xyz.nibblz.galapagos.Glyphs

enum class Rarity(val label: String, val trophies: Int) {
    COMMON("Common", 10),
    UNCOMMON("Uncommon", 20),
    RARE("Rare", 30),
    EPIC("Epic", 50),
    LEGENDARY("Legendary", 70),
    MYTHIC("Mythic", 100);

    fun tooltipGlyph(): String {
        return Glyphs.getGlyph("_fonts/icon/tooltips/${this.name.lowercase()}.png")
    }
}