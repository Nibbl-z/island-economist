package xyz.nibblz.islandeconomist.features

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.world.item.Items
import xyz.nibblz.islandeconomist.IslandEconomist
import xyz.nibblz.islandeconomist.findLore
import xyz.nibblz.islandeconomist.mixin.accessor.HoveredSlotAccessor

object CoinTracking : Feature {
    override val id = "coin_tracking"
    override val name = "Coin Tracking"

    override fun init() {}

    var price = 0

    fun handleContainerContent(packet: ClientboundContainerSetContentPacket) {
        val screen = Minecraft.getInstance().screen
        if (screen?.title?.string?.contains("USING COINS?") == false) return

        for (it in packet.items) {
            if (!it.`is`(Items.ECHO_SHARD)) continue
            val regex = Regex("(?<=/)\\d[\\d,]*")
            val match = it.findLore(regex) ?: continue
            val priceString = match[0] ?: continue

            val cleanedPriceString = priceString.value.replace(",", "")
            price = cleanedPriceString.toInt()
            break
        }

        IslandEconomist.logger.info(price.toString())
    }

    fun mouseClicked(event: MouseButtonEvent, screen: ContainerScreen) {
        val item = (screen as HoveredSlotAccessor).`islandeconomist$hoveredSlot`()
        if (item.index in 46..48) {
            IslandEconomist.logger.info("this item has apparently been purchased! i've spent $price coins!")
            price = 0
        }
    }

    fun handleContainerClose() {
        price = 0
        IslandEconomist.logger.info("this ui has apparently been closed! (which is probably any ui)")
    }
}