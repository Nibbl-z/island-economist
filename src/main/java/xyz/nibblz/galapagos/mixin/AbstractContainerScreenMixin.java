package xyz.nibblz.galapagos.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nibblz.galapagos.UtilKt;
import xyz.nibblz.galapagos.events.ContainerOpenEvent;
import xyz.nibblz.galapagos.events.ContainerRenderEvent;
import xyz.nibblz.galapagos.events.SlotClickEvent;
import xyz.nibblz.galapagos.features.CoinTracking;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    @Inject(method = "slotClicked", at = @At("HEAD"))
    private void slotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        ContainerScreen screen = Minecraft.getInstance().screen instanceof ContainerScreen s ? s : null;
        if (!UtilKt.onIsland()) return;
        if (screen == null) return;

        SlotClickEvent.INSTANCE.getEVENT().invoker().invoke(screen, containerInput);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        ContainerScreen screen = Minecraft.getInstance().screen instanceof ContainerScreen s ? s : null;
        if (!UtilKt.onIsland()) return;
        if (screen == null) return;

        ContainerRenderEvent.INSTANCE.getEVENT().invoker().invoke(screen, graphics, leftPos, topPos, imageWidth, imageHeight);
    }
}
