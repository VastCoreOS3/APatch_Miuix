package me.bmax.apatch.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun rememberBlurBackdrop(enableBlur: Boolean): LayerBackdrop? {
    if (!enableBlur || !isRenderEffectSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
fun LayerBackdrop?.getAppBarColor(): Color =
    this?.let { Color.Transparent } ?: MiuixTheme.colorScheme.surface

@Composable
fun Modifier.blurEffect(
    backdrop: LayerBackdrop?,
    enabled: Boolean = true,
    blurRadius: Float = 25f,
    shape: Shape = RectangleShape
): Modifier {
    if (!enabled || backdrop == null) return this

    // 记住配置，避免每次重组新建对象
    val blurColors = remember(MiuixTheme.colorScheme.isDark, MiuixTheme.colorScheme.surface) {
        if (MiuixTheme.colorScheme.isDark) {
            BlurColors(
                blendColors = listOf(
                    // 第一层：基础底色
                    BlendColorEntry(
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.72f),
                        mode = BlendBlendMode.SrcOver
                    ),
                    // 第二层：轻微滤色提亮，模拟玻璃反光
                    BlendColorEntry(
                        color = Color.White.copy(alpha = 0.06f),
                        mode = BlendBlendMode.Screen
                    )
                ),
                brightness = 0.03f,
                contrast = 1.08f,
                saturation = 1.12f
            )
        } else {
            BlurColors(
                blendColors = listOf(
                    BlendColorEntry(
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.82f),
                        mode = BlendBlendMode.SrcOver
                    ),
                    BlendColorEntry(
                        color = Color.White.copy(alpha = 0.09f),
                        mode = BlendBlendMode.Screen
                    )
                ),
                brightness = 0.02f,
                contrast = 1.05f,
                saturation = 1.10f
            )
        }
    }

    return this.then(
        Modifier.textureBlur(
            backdrop = backdrop,
            shape = shape,
            blurRadius = blurRadius,
            colors = blurColors
        )
    )
}
