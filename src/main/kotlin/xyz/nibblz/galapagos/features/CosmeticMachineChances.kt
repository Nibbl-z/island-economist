package xyz.nibblz.galapagos.features

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.Style
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import xyz.nibblz.galapagos.Galapagos
import xyz.nibblz.galapagos.Glyphs
import xyz.nibblz.galapagos.data.BlueprintLootPreview
import xyz.nibblz.galapagos.data.Cosmetic
import xyz.nibblz.galapagos.data.Rarity
import xyz.nibblz.galapagos.events.ContainerCloseEvent
import xyz.nibblz.galapagos.events.ContainerOpenEvent
import xyz.nibblz.galapagos.events.ContainerRenderEvent
import xyz.nibblz.galapagos.events.ContainerSetSlotEvent
import xyz.nibblz.galapagos.events.SlotClickEvent
import xyz.nibblz.galapagos.getCosmetic
import xyz.nibblz.galapagos.mixin.accessor.HoveredSlotAccessor
import xyz.nibblz.galapagos.render
import xyz.nibblz.galapagos.update
import kotlin.math.round

object CosmeticMachineChances : Feature {
    override val id: String = "cosmetic_machine_chances"
    override val name: String = "Cosmetic Machine Chances"

    override fun init() {
        ContainerOpenEvent.EVENT.register { packet -> containerOpen(packet) }
        ContainerCloseEvent.EVENT.register { containerClose() }
        ContainerSetSlotEvent.EVENT.register { packet -> containerSetSlot(packet) }
        SlotClickEvent.EVENT.register { screen, input -> slotClick(screen, input) }
        ItemTooltipCallback.EVENT.register { stack, context, flag, components -> tooltipAdd(stack, context, flag, components) }
        ContainerRenderEvent.EVENT.register { screen, graphics, x, y, w, h -> containerRender(screen, graphics, x, y, w, h) }
    }

    var inCosmeticMachine = false
    var isUltimate = false

    var basicCosmetics: MutableMap<String, Cosmetic> = mutableMapOf()
    var ultimateCosmetics: MutableMap<String, Cosmetic> = mutableMapOf()

    var basicData = BlueprintLootPreview()
    var ultimateData = BlueprintLootPreview()

    fun slotClick(screen: ContainerScreen, type: ContainerInput) {
        val slot = (screen as HoveredSlotAccessor).`galapagos$hoveredSlot`() ?: return

        if (slot.item.itemName.string.contains("Basic Pull")) isUltimate = false
        if (slot.item.itemName.string.contains("Ultimate Pull")) isUltimate = true
    }

    fun containerOpen(packet: ClientboundContainerSetContentPacket) { updateItems(packet.items) }
    fun containerSetSlot(packet: ClientboundContainerSetSlotPacket) { updateItems(listOf(packet.item))}

    fun updateItems(items: List<ItemStack>) {
        val screen = Minecraft.getInstance().screen ?: return
        if (screen.title.string.contains("COSMETIC MACHINE")) inCosmeticMachine = true
        if (!screen.title.string.contains("LOOT PREVIEW")) return
        if (!inCosmeticMachine) return


        items.forEach {
            if (it.isEmpty) return@forEach
            if (it.itemName.string == "Back") return@forEach
            if (it.itemName.string == "Loot Preview") return@forEach
            if (it.itemName.string == "Next Page") return@forEach
            if (it.itemName.string == "Previous Page") return@forEach

            val cosmetic = it.getCosmetic() ?: return@forEach
            if (isUltimate) {
                ultimateCosmetics[it.itemName.string] = cosmetic
            } else {
                basicCosmetics[it.itemName.string] = cosmetic
            }
        }

        basicData.update(basicCosmetics.entries.map {it.value})
        ultimateData.update(ultimateCosmetics.entries.map {it.value})
    }

    fun containerClose() {
        inCosmeticMachine = false
    }

    fun getModifiedChanceTooltip(rarity: Rarity, exclusiveChance: Float, chance: Float, arcaneChance: Float = 0.0f): Component {
        val fixedChance = round((chance * (1.0f - exclusiveChance - arcaneChance) * 100.0f) * 1000) / 1000f
        val fixedExclusiveChance = round(chance * exclusiveChance * 100.0f * 1000) / 1000f

        return Component.literal(" • ").withColor(ChatFormatting.DARK_GRAY.color!!)
                .append(Component.literal("$fixedChance%").withColor(ChatFormatting.GRAY.color!!))
                .append(Component.literal(" - ").withColor(ChatFormatting.DARK_GRAY.color!!))
                .append(Component.literal(rarity.label).withColor(rarity.color)
                .append(Component.literal(" [").withColor(ChatFormatting.DARK_GRAY.color!!))
                .append(Component.literal("$fixedExclusiveChance% ").withColor(ChatFormatting.GRAY.color!!))
                .append(Component.literal("\uE000").withColor(0xFFFFFF).withStyle(Style.EMPTY.withFont(Galapagos.font)))
                .append(Component.literal("]").withColor(ChatFormatting.DARK_GRAY.color!!)))
    }

    fun tooltipAdd(item: ItemStack, context: Item.TooltipContext, flag: TooltipFlag, list: MutableList<Component>) {
        if (!inCosmeticMachine) return

        if(item.itemName.string == "Basic Pull") {
            list[7] = getModifiedChanceTooltip(Rarity.RARE, 0.1f, 0.15f)
            list[8] = getModifiedChanceTooltip(Rarity.EPIC, 0.1f, 0.08f)
            list[9] = getModifiedChanceTooltip(Rarity.LEGENDARY, 0.1f, 0.02f)

            // devcmb told me to use repeat (2) but idont wanna
            list.removeAt(11)
            list.removeAt(11)
        }

        if(item.itemName.string == "Ultimate Pull") {
            list[5] = getModifiedChanceTooltip(Rarity.RARE, 0.3f, 0.5f)
            list[6] = getModifiedChanceTooltip(Rarity.EPIC, 0.3f, 0.35f)
            list[7] = getModifiedChanceTooltip(Rarity.LEGENDARY, 0.3f, 0.12f)
            list[8] = getModifiedChanceTooltip(Rarity.MYTHIC, 0.3f, 0.03f, 0.01f)
            list.add(9, Component.literal(" • ").withColor(ChatFormatting.DARK_GRAY.color!!)
                .append(Component.literal("0.03%").withColor(ChatFormatting.GRAY.color!!))
                .append(Component.literal(" - ").withColor(ChatFormatting.DARK_GRAY.color!!))
                .append(Glyphs.getGlyphComponent("_fonts/icon/tooltips/arcane.png")))

            list.removeAt(11)
            list.removeAt(11)
            list.removeAt(11)
        }
    }

    fun containerRender(screen: ContainerScreen, graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int) {
        if (!screen.title.string.contains("LOOT PREVIEW", true)) return
        if (!inCosmeticMachine) return

        val data = if (isUltimate) ultimateData else basicData

        data.render(graphics, x, y, w)
        graphics.text(Minecraft.getInstance().font, Glyphs.getGlyphComponent("_fonts/icon/warning_blue.png"), x + w + 2, y, ARGB.opaque(0xffffff), true)
        graphics.text(Minecraft.getInstance().font, Component.literal("Make sure to scroll through"), x + w + 14, y, ARGB.opaque(0x42b9f5), true)
        graphics.text(Minecraft.getInstance().font, Component.literal("all pages to make data accurate!"), x + w + 2, y + 10, ARGB.opaque(0x42b9f5), true)

    }
}