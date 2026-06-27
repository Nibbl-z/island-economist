package xyz.nibblz.galapagos.data

import xyz.nibblz.galapagos.Glyphs

enum class Rarity(val label: String, val trophies: Int, val color: Int) {
    COMMON("Common", 10, 0xffffff),
    UNCOMMON("Uncommon", 20, 0x1eff0c),
    RARE("Rare", 30, 0x0070ff),
    EPIC("Epic", 50, 0x9d3af5),
    LEGENDARY("Legendary", 70, 0xff8000),
    MYTHIC("Mythic", 100, 0xf94242);

    fun tooltipGlyph(): String {
        return Glyphs.getGlyph("_fonts/icon/tooltips/${this.name.lowercase()}.png")
    }
}