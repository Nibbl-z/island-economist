package xyz.nibblz.islandeconomist.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nibblz.islandeconomist.features.CoinTracking;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        ContainerScreen screen = Minecraft.getInstance().screen instanceof ContainerScreen s ? s : null;
        if (screen == null) return;
        CoinTracking.INSTANCE.mouseClicked(event, screen);
    }
}
