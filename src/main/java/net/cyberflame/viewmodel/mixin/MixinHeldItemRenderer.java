package net.cyberflame.viewmodel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static net.cyberflame.viewmodel.settings.SettingType.*;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    protected abstract void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, PlayerRenderState playerState);

    @Shadow
    protected abstract void renderTwoHandedMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float xRot, float inverseArmHeight, float attackValue, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state);

    @Shadow
    protected abstract void renderOneHandedMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, HumanoidArm arm, float attackValue, ItemStack map, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state);

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight);

    @Shadow
    protected abstract void applyEatTransform(PoseStack poseStack, float partialTicks, HumanoidArm arm, float useItemRemainingTicks, int useDuration);

    @Shadow
    protected abstract void applyBrushTransform(PoseStack poseStack, float partialTicks, HumanoidArm arm, float useItemRemainingTicks);

    @Shadow
    protected abstract void swingArm(float animation, PoseStack poseStack, int invert, HumanoidArm arm);

    /**
     * @author CyberFlame
     * @reason The inject would always cancel and therefore can cause incompatibilities with other mods.
     */
    @Overwrite
    private void submitArmWithItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        if (!state.isScoping) {
            AvatarRenderState avatarRenderState = playerState.avatarRenderState;
            if (avatarRenderState != null) {
                boolean isMainHand = hand == InteractionHand.MAIN_HAND;
                HumanoidArm arm = isMainHand ? avatarRenderState.mainArm : avatarRenderState.mainArm.getOpposite();
                int useDuration = isMainHand ? state.mainHandUseDuration : state.offHandUseDuration;
                int chargeDuration = isMainHand ? state.mainHandChargeDuration : state.offHandChargeDuration;
                poseStack.pushPose();
                if (POS.isTrue()) {
                    poseStack.translate(POS_X.getFloatValue() * 0.1, POS_Y.getFloatValue() * 0.1, POS_Z.getFloatValue() * 0.1);
                }
                if (ROTATION.isTrue()) {
                    poseStack.rotateDegrees(Axis.YP, ROTATION_Y.getFloatValue());
                    poseStack.rotateDegrees(Axis.XP, ROTATION_X.getFloatValue());
                    poseStack.rotateDegrees(Axis.ZP, ROTATION_Z.getFloatValue());
                }
                if (SCALE.isTrue()) {
                    poseStack.scale(1 - (1 - SCALE_X.getFloatValue()) * 0.1F, 1 - (1 - SCALE_Y.getFloatValue()) * 0.1F, 1 - (1 - SCALE_Z.getFloatValue()) * 0.1F);
                }
                if (itemStack.isEmpty()) {
                    if (isMainHand && !avatarRenderState.isInvisible) {
                        this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack, arm, playerState);
                    }
                } else if (itemStack.has(DataComponents.MAP_ID)) {
                    if (isMainHand && state.offHandItem.isEmpty()) {
                        this.renderTwoHandedMap(poseStack, submitNodeCollector, lightCoords, xRot, inverseArmHeight, attack, playerState, state);
                    } else {
                        this.renderOneHandedMap(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, arm, attack, itemStack, playerState, state);
                    }
                } else if (itemStack.is(Items.CROSSBOW)) {
                    this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                    boolean charged = CrossbowItem.isCharged(itemStack);
                    boolean isRightArm = arm == HumanoidArm.RIGHT;
                    int invert = isRightArm ? 1 : -1;
                    if (avatarRenderState.isUsingItem && state.useItemRemainingTicks > 0 && avatarRenderState.useItemHand == hand && !charged) {
                        poseStack.translate((float) invert * -0.4785682F, -0.094387F, 0.05731531F);
                        poseStack.rotateDegrees(Axis.XP, -11.935F);
                        poseStack.rotateDegrees(Axis.YP, (float) invert * 65.3F);
                        poseStack.rotateDegrees(Axis.ZP, (float) invert * -9.785F);
                        float timeHeld = (float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F);
                        float power = timeHeld / (float) chargeDuration;
                        if (power > 1.0F) {
                            power = 1.0F;
                        }

                        if (power > 0.1F) {
                            float shakeOffset = Mth.sin((timeHeld - 0.1F) * 1.3F);
                            float shakeIntensity = power - 0.1F;
                            float shake = shakeOffset * shakeIntensity;
                            poseStack.translate(shake * 0.0F, shake * 0.004F, shake * 0.0F);
                        }

                        poseStack.translate(power * 0.0F, power * 0.0F, power * 0.04F);
                        poseStack.scale(1.0F, 1.0F, 1.0F + power * 0.2F);
                        poseStack.rotateDegrees(Axis.YN, (float) invert * 45.0F);
                    } else {
                        this.swingArm(attack, poseStack, invert, arm);
                        if (charged && attack < 0.001F && isMainHand) {
                            poseStack.translate((float) invert * -0.641864F, 0.0F, 0.0F);
                            poseStack.rotateDegrees(Axis.YP, (float) invert * 10.0F);
                        }
                    }

                    (isMainHand ? state.mainHandRenderState : state.offHandRenderState)
                            .submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
                } else {
                    boolean isRightArm = arm == HumanoidArm.RIGHT;
                    int invert = isRightArm ? 1 : -1;
                    if (avatarRenderState.isUsingItem && state.useItemRemainingTicks > 0 && avatarRenderState.useItemHand == hand) {
                        ItemUseAnimation useAnimation = itemStack.getUseAnimation();
                        if (!useAnimation.hasCustomArmTransform()) {
                            this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                        }

                        switch (useAnimation) {
                            case NONE, SPYGLASS, TOOT_HORN -> {}
                            case EAT, DRINK -> {
                                this.applyEatTransform(poseStack, partialTicks, arm, state.useItemRemainingTicks, useDuration);
                                this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                            }
                            case BLOCK -> {
                                if (!(itemStack.getItem() instanceof ShieldItem)) {
                                    poseStack.translate((float) invert * -0.14142136F, 0.08F, 0.14142136F);
                                    poseStack.rotateDegrees(Axis.XP, -102.25F);
                                    poseStack.rotateDegrees(Axis.YP, (float) invert * 13.365F);
                                    poseStack.rotateDegrees(Axis.ZP, (float) invert * 78.05F);
                                }
                            }
                            case BOW -> {
                                poseStack.translate((float) invert * -0.2785682F, 0.18344387F, 0.15731531F);
                                poseStack.rotateDegrees(Axis.XP, -13.935F);
                                poseStack.rotateDegrees(Axis.YP, (float) invert * 35.3F);
                                poseStack.rotateDegrees(Axis.ZP, (float) invert * -9.785F);
                                float timeHeld = (float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F);
                                float power = timeHeld / 20.0F;
                                power = (power * power + power * 2.0F) / 3.0F;
                                if (power > 1.0F) {
                                    power = 1.0F;
                                }

                                if (power > 0.1F) {
                                    float shakeOffset = Mth.sin((timeHeld - 0.1F) * 1.3F);
                                    float shakeIntensity = power - 0.1F;
                                    float shake = shakeOffset * shakeIntensity;
                                    poseStack.translate(shake * 0.0F, shake * 0.004F, shake * 0.0F);
                                }

                                poseStack.translate(power * 0.0F, power * 0.0F, power * 0.04F);
                                poseStack.scale(1.0F, 1.0F, 1.0F + power * 0.2F);
                                poseStack.rotateDegrees(Axis.YN, (float) invert * 45.0F);
                            }
                            case TRIDENT -> {
                                poseStack.translate((float) invert * -0.5F, 0.7F, 0.1F);
                                poseStack.rotateDegrees(Axis.XP, -55.0F);
                                poseStack.rotateDegrees(Axis.YP, (float) invert * 35.3F);
                                poseStack.rotateDegrees(Axis.ZP, (float) invert * -9.785F);
                                float timeHeld = (float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F);
                                float power = timeHeld / 10.0F;
                                if (power > 1.0F) {
                                    power = 1.0F;
                                }

                                if (power > 0.1F) {
                                    float shakeOffset = Mth.sin((timeHeld - 0.1F) * 1.3F);
                                    float shakeIntensity = power - 0.1F;
                                    float shake = shakeOffset * shakeIntensity;
                                    poseStack.translate(shake * 0.0F, shake * 0.004F, shake * 0.0F);
                                }

                                poseStack.translate(0.0F, 0.0F, power * 0.2F);
                                poseStack.scale(1.0F, 1.0F, 1.0F + power * 0.2F);
                                poseStack.rotateDegrees(Axis.YN, (float) invert * 45.0F);
                            }
                            case BRUSH -> this.applyBrushTransform(poseStack, partialTicks, arm, state.useItemRemainingTicks);
                            case BUNDLE, CROSSBOW -> this.swingArm(attack, poseStack, invert, arm);
                            case SPEAR -> {
                                poseStack.translate((float) invert * 0.56F, -0.52F, -0.72F);
                                float timeHeld = (float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F);
                                SpearAnimations.firstPersonUse(avatarRenderState.ticksSinceKineticHitFeedback, poseStack, timeHeld, arm, itemStack);
                            }
                        }
                    } else if (avatarRenderState.isAutoSpinAttack) {
                        this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                        if (!CHANGE_SWING.isTrue()) {
                            poseStack.translate((float) invert * -0.4F, 0.8F, 0.3F);
                        }
                        poseStack.rotateDegrees(Axis.YP, (float) invert * 65.0F);
                        poseStack.rotateDegrees(Axis.ZP, (float) invert * -85.0F);
                    } else {
                        this.applyItemArmTransform(poseStack, arm, inverseArmHeight);
                        LivingEntity.SwingDescription currentSwing = avatarRenderState.currentSwing;
                        if (currentSwing != null && hand == currentSwing.hand()) {
                            switch (currentSwing.animation().type()) {
                                case NONE -> {}
                                case WHACK -> this.swingArm(attack, poseStack, invert, arm);
                                case STAB -> SpearAnimations.firstPersonAttack(attack, poseStack, invert, arm);
                            }
                        }
                    }

                    (isMainHand ? state.mainHandRenderState : state.offHandRenderState)
                            .submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
                }

                poseStack.popPose();
            }
        }
    }
}
