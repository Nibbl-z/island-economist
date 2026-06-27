package xyz.nibblz.galapagos.features

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import xyz.nibblz.galapagos.data.BlueprintLootPreview
import xyz.nibblz.galapagos.data.Cosmetic
import xyz.nibblz.galapagos.events.ContainerOpenEvent
import xyz.nibblz.galapagos.events.ContainerRenderEvent
import xyz.nibblz.galapagos.getCosmetic
import xyz.nibblz.galapagos.render
import xyz.nibblz.galapagos.update

object CrateChances: Feature {
    override val id: String = "crate_chances"
    override val name: String = "Crate Chances"

    var isCrate = false
    var data = BlueprintLootPreview()

    override fun init() {
        ContainerRenderEvent.EVENT.register { screen, graphics, x, y, w, h -> containerRender(screen, graphics, x, y, w, h) }
        ContainerOpenEvent.EVENT.register { packet -> containerOpen(packet) }
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

            val cosmetic = it.getCosmetic() ?: return@forEach
            cosmetics.add(cosmetic)
        }

        data.update(cosmetics)

        isCrate = true
    }

    fun containerRender(screen: ContainerScreen, graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int) {
        if (!screen.title.string.contains("LOOT PREVIEW", true)) return
        if (!isCrate) return

        data.render(graphics, x, y, w)
    }
}