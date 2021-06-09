package schauweg.tillitbreaks.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private final float scale = 0.8F;

    @Shadow
    public float zOffset;

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    public void onRenderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci){

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler)
            return;

        if (stack.isDamageable()){

            MatrixStack matrixTextInfo = new MatrixStack();
            matrixTextInfo.push();
            matrixTextInfo.translate(x, y, (double)(this.zOffset + 200.0F));
            matrixTextInfo.scale(scale, scale, 0F);
            float scaleOffset = 1F / scale;

            int textWidth;

            if (stack.getItem() == Items.BOW || stack.getItem() == Items.CROSSBOW){

                PlayerInventory inventory = player.getInventory();
                int arrowCounter = 0;
                boolean hasNormalArrows = false;
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack is = inventory.getStack(i);
                    if (is.getItem() == Items.ARROW || is.getItem() == Items.SPECTRAL_ARROW || is.getItem() == Items.TIPPED_ARROW){
                        arrowCounter += is.getCount();
                        if(is.getItem() == Items.ARROW) hasNormalArrows = true;
                    }
                }
                String totalArrows = String.valueOf(arrowCounter);

                if (stack.hasEnchantments() && arrowCounter > 0) {
                    Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
                    for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        //Get enchantmentname for a specific level
                        if (enchantment.equals(Enchantments.INFINITY) && hasNormalArrows) {
                            totalArrows = "∞";
                            break;
                        }
                    }
                }
                textWidth = renderer.getWidth(totalArrows);
                renderer.draw(matrixTextInfo, totalArrows, 16*scaleOffset-textWidth-1, 0, -1);
            }
            matrixTextInfo.push();
        }
    }
}

