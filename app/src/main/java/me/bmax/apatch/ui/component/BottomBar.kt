package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import me.bmax.apatch.ui.theme.blurEffect
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.ripple.miuixRipple
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBar(backdrop: LayerBackdrop?) {
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

    val safeSelectedIndex = remember(selectedPage, availablePages) {
        if (availablePages.isEmpty()) 0 else selectedPage.coerceIn(0, availablePages.lastIndex)
    }

    val floatingBarShape = MiuixTheme.shapes.squircleLarge
    val dockBlurRadius = 30f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingNavigationBar(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .then(
                    if (backdrop != null) {
                        Modifier.blurEffect(
                            backdrop = backdrop,
                            enabled = true,
                            blurRadius = dockBlurRadius,
                            shape = floatingBarShape
                        )
                    } else {
                        // 模糊不可用时，纯色兜底，shape保持一致
                        Modifier.background(
                            MiuixTheme.colorScheme.surface.copy(0.9f),
                            floatingBarShape
                        )
                    }
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
                            .clickable(
                                interactionSource = interactionSource,
                                indication = miuixRipple(),
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
