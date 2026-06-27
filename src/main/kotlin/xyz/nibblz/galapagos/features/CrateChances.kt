package xyz.nibblz.galapagos.features

import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import xyz.nibblz.galapagos.Galapagos
import xyz.nibblz.galapagos.Glyphs
import xyz.nibblz.galapagos.data.Rarity
import xyz.nibblz.galapagos.events.ContainerOpenEvent
import xyz.nibblz.galapagos.events.ContainerRenderEvent
import xyz.nibblz.galapagos.findLore

object CrateChances: Feature {
    override val id: String = "crate_chances"
    override val name: String = "Crate Chances"

    var isCrate = false
    var newCosmeticChance = 0.0f
    var currentTrophies = 0
    var totalTrophies = 0
    var newRepChance = 0.0f
    var currentRep = 0
    var totalRep = 0

    data class Cosmetic(
        var chance: Float,
        var isOwned: Boolean,
        var trophies: Int,
        var rep: Int
    )

    override fun init() {
        ContainerRenderEvent.EVENT.register { screen, graphics, x, y, w, h -> containerRender(screen, graphics, x, y, w, h) }
        ContainerOpenEvent.EVENT.register { packet -> containerOpen(packet) }
    }

    fun cosmeticFromItem(item: ItemStack): Cosmetic? {
        val chanceRegex = Regex("Chance: (?<chance>[\\d,.]+)%")
        val chanceString = item.findLore(chanceRegex)?.get("chance")?.value ?: return null
        val chance = chanceString.toFloat()

        val isOwned = item.findLore("Royal Donations:")
        var trophies = 10

        Rarity.entries.forEach {
            if (item.findLore(it.tooltipGlyph())) {
                trophies = it.trophies
            }
        }

        val isExclusive = item.findLore(Glyphs.getGlyph("_fonts/icon/tooltips/exclusive.png"))

        val donations = if (isOwned) {
            val repRegex = Regex("Royal Donations: (?<rep>\\d+)")
            val repString = item.findLore(repRegex)?.get("rep")?.value ?: return null
            repString.toInt()
        } else 0
        val trophiesPerDonation = trophies / (if (isExclusive) 5 else 10)

        val rep = donations * trophiesPerDonation

        Galapagos.logger.info("${item.itemName.string}, $chance, $isOwned, $trophies, $rep")

        return Cosmetic(
            chance = chance,
            isOwned = isOwned,
            trophies = trophies,
            rep = rep
        )
    }

    fun containerOpen(packet: ClientboundContainerSetContentPacket) {
        isCrate = false
        if (packet.items[35].itemName.string == "Next Page") return

        val cosmetics: MutableList<Cosmetic> = mutableListOf()

        packet.items.forEach {
            if (it.isEmpty) return@forEach
            if (it.itemName.string == "Back") return@forEach
            if (it.itemName.string == "Loot Preview") return@forEach

            if (!it.itemName.string.contains("Blueprint", true)) return

            val cosmetic = cosmeticFromItem(it) ?: return@forEach
            cosmetics.add(cosmetic)
        }

        newCosmeticChance = 0.0f
        currentTrophies = 0
        totalTrophies = 0
        newRepChance = 0.0f
        currentRep = 0
        totalRep = 0

        var totalNewCosmeticChance = 0.0f
        var totalNewRepChance = 0.0f

        cosmetics.forEach {
            if (!it.isOwned) {
                newCosmeticChance += it.chance
            } else {
                currentTrophies += it.trophies
            }

            if (it.trophies != it.rep) {
                newRepChance += it.chance
            }

            totalTrophies += it.trophies
            currentRep += it.rep
            totalRep += it.trophies

            totalNewCosmeticChance += it.chance
            totalNewRepChance += it.chance
        }

        // this is needed because the chances for some crates are rounded and don't add up to 100% (eg. the mythic crate)
        newCosmeticChance = (newCosmeticChance / totalNewCosmeticChance) * 100f
        newRepChance = (newRepChance / totalNewRepChance) * 100f

        isCrate = true
    }

    fun containerRender(screen: ContainerScreen, graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int) {
        if (!screen.title.string.contains("LOOT PREVIEW", true)) return
        if (!isCrate) return
        graphics.text(Minecraft.getInstance().font, Component.literal("New Cosmetic: ${"%.2f".format(newCosmeticChance)}%"), x + w + 2, y + 30, ARGB.opaque(0x66fc56), true)
        graphics.text(Minecraft.getInstance().font, Component.literal("Trophies: ${currentTrophies}/${totalTrophies}"), x + w + 2, y + 45, ARGB.opaque(0x66fc56), true)
        graphics.text(Minecraft.getInstance().font, Component.literal("New Rep: ${"%.2f".format(newRepChance)}%"), x + w + 2, y + 60, ARGB.opaque(0x9143f0), true)
        graphics.text(Minecraft.getInstance().font, Component.literal("Royal Rep: ${currentRep}/${totalRep}"), x + w + 2, y + 75, ARGB.opaque(0x9143f0), true)
    }
}