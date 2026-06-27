package xyz.nibblz.galapagos.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket

object ContainerSetSlotEvent {
    val EVENT: Event<ContainerSetSlotCallback> = EventFactory.createArrayBacked(
        ContainerSetSlotCallback::class.java
    ) { listeners ->
        ContainerSetSlotCallback {
            packet -> listeners.forEach { it.invoke(packet) }
        }
    }

    fun interface ContainerSetSlotCallback {
        fun invoke(packet: ClientboundContainerSetSlotPacket)
    }
}