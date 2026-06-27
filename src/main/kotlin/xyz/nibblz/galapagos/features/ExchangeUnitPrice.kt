package xyz.nibblz.galapagos.features

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import xyz.nibblz.galapagos.Glyphs
import xyz.nibblz.galapagos.events.ContainerOpenEvent
import xyz.nibblz.galapagos.events.ContainerSetSlotEvent
import xyz.nibblz.galapagos.events.SlotClickEvent
import xyz.nibblz.galapagos.findLore
import xyz.nibblz.galapagos.mixin.accessor.HoveredSlotAccessor

object ExchangeUnitPrice : Feature {
    override val id: String = "exchange_unit_price"
    override val name: String = "Island Exchange Unit Price"

    override fun init() {
        ItemTooltipCallback.EVENT.register { stack, context, flag, components -> tooltipAdd(stack, context, flag, components) }
        ContainerOpenEvent.EVENT.register { packet -> containerOpen(packet) }
        ContainerSetSlotEvent.EVENT.register { packet -> containerSetSlot(packet) }
        SlotClickEvent.EVENT.register { screen, input -> slotClick(screen, input) }
    }

    val perUnitPrices: MutableMap<ItemStack, Int> = mutableMapOf()

    fun containerOpen(packet: ClientboundContainerSetContentPacket) {
        val screen = Minecraft.getInstance().screen ?: return
        if (!screen.title.string.contains("ISLAND EXCHANGE", false)) return

        perUnitPrices.clear()

        packet.items.forEach {
            getPerUnitPrice(it)
        }
    }

    fun getPerUnitPrice(item: ItemStack) {
        val regex = Regex("Listed Price: .(?<price>[\\d,]+)")
        val priceString = item.findLore(regex)?.get("price")?.value ?: return
        val cleanedString = priceString.replace(",", "")
        val price = cleanedString.toInt()

        perUnitPrices[item] = (price / item.count)
    }

    fun containerSetSlot(packet: ClientboundContainerSetSlotPacket) {
        val screen = Minecraft.getInstance().screen ?: return
        if (!screen.title.string.contains("ISLAND EXCHANGE", false)) return

        val item = packet.item
        if (item.count == 1) return

        getPerUnitPrice(item)
    }

    fun slotClick(screen: ContainerScreen, type: ContainerInput) {
        if (!screen.title.string.contains("ISLAND EXCHANGE", false)) return
        val slot = (screen as HoveredSlotAccessor).`galapagos$hoveredSlot`() ?: return

        if (slot.item.itemName.string.contains("Back")) perUnitPrices.clear()
        if (slot.item.itemName.string.contains("Main Filter")) perUnitPrices.clear()
        if (slot.item.itemName.string.contains("Filter by Rarity")) perUnitPrices.clear()
        if (slot.item.itemName.string.contains("Filter by Type")) perUnitPrices.clear()
        if (slot.item.itemName.string.contains("Refresh Listings")) perUnitPrices.clear()
        if (slot.item.itemName.string.contains("Next Page")) perUnitPrices.clear()
        if (slot.item.itemName.string.contains("Previous Page")) perUnitPrices.clear()
    }

    fun tooltipAdd(item: ItemStack, context: Item.TooltipContext, flag: TooltipFlag, list: MutableList<Component>) {
        val screen = Minecraft.getInstance().screen ?: return
        if (!screen.title.string.contains("ISLAND EXCHANGE", false)) return
        if (item.count == 1) return

        val listedPriceIndex = list.indexOfFirst {
            it.string.contains("Listed Price")
        }
        if (listedPriceIndex == -1) return

        list.add(listedPriceIndex + 1,
            Component.literal("Unit Price: ").withColor(ChatFormatting.GRAY.color!!)
                .append(Glyphs.getGlyphComponent("_fonts/icon/coin.png"))
                .append(Component.literal("%,d".format(perUnitPrices[item])).withColor(0xffffff))
        )
    }
}