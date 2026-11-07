/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;

public record AfterSkyEvent(LevelRenderState getLevelRenderState, @Nullable PoseStack getPoseStack, Matrix4fc getModelViewMatrix) {
}
