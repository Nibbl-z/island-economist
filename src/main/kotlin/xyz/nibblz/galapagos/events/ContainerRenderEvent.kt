package xyz.nibblz.galapagos.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen

object ContainerRenderEvent {
    val EVENT: Event<ContainerRenderCallback> = EventFactory.createArrayBacked(
        ContainerRenderCallback::class.java
    ) { listeners ->
        ContainerRenderCallback {
            screen, graphics, x, y, w, h -> listeners.forEach { it.invoke(screen, graphics, x, y, w, h) }
        }
    }

    fun interface ContainerRenderCallback {
        fun invoke(screen: ContainerScreen, graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int)
    }
}