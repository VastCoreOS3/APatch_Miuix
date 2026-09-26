package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.LocalHandlePageChange
import me.bmax.apatch.ui.LocalSelectedPage
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.mClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings

@Composable
fun BottomBar(backdrop: LayerBackdrop) {
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPage: Int = LocalSelectedPage.current
    val handlePageChange: (Int) -> Unit = LocalHandlePageChange.current

    val availablePages = remember(kPatchReady, aPatchReady) {
        BottomBarDestination.entries.filter { destination ->
            !(destination.kPatchRequired && !kPatchReady) && !(destination.aPatchRequired && !aPatchReady)
        }
    }

    // 防止索引越界保护
    val safeSelectedIndex = remember(selectedPage, availablePages) {
        if (availablePages.isEmpty()) 0 else selectedPage.coerceIn(0, availablePages.lastIndex)
    }

    val floatingBarShape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingNavigationBar(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .textureBlur(
                    backdrop = backdrop,
                    shape = floatingBarShape,
                    blurRadius = 25f,
                    colors = BlurDefaults.blurColors(
                        blendColors = listOf(
                            BlendColorEntry(MiuixTheme.colorScheme.surface.copy(0.35f)),
                            BlendColorEntry(MiuixTheme.colorScheme.surfaceContainer.copy(0.2f))
                        )
                    )
                ),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                availablePages.forEachIndexed { index, destination ->
                    val isSelected = safeSelectedIndex == index
                    val labelText = stringResource(destination.label)
                    val iconVector = if (isSelected) destination.iconSelected else destination.iconNotSelected
                    val textColor = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceContainerVariant
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .mClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { handlePageChange(index) }
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = labelText,
                            tint = textColor
                        )
                        Text(
                            text = labelText,
                            color = textColor,
                            style = MiuixTheme.textStyles.body2
                        )
                    }
                }
            }
        }
    }
}

enum class BottomBarDestination(
    @StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
    val kPatchRequired: Boolean,
    val aPatchRequired: Boolean,
) {
    Home(
        R.string.home,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        false,
        false
    ),
    KModule(
        R.string.kpm,
        Icons.Filled.Build,
        Icons.Outlined.Build,
        true,
        false
    ),
    SuperUser(
        R.string.su_title,
        Icons.Filled.Security,
        Icons.Outlined.Security,
        true,
        false
    ),
    AModule(
        R.string.apm,
        Icons.Filled.Extension,
        Icons.Outlined.Extension,
        false,
        true
    ),
    Settings(
        R.string.settings,
        Icons.Filled.Settings,
        Icons.Outlined.Settings,
        false,
        false
    )
}
