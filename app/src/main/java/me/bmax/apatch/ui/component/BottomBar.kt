package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBar(backdrop: LayerBackdrop) {
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPage = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    val availablePages = remember(kPatchReady, aPatchReady) {
        BottomBarDestination.entries.filter { d ->
            !(d.kPatchRequired && !kPatchReady) && !(d.aPatchRequired && !aPatchReady)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .blurEffect(backdrop)
                .padding(vertical = 12.dp)
                .sizeIn(minHeight = 56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            availablePages.forEachIndexed { index, destination ->
                val isSelected = selectedPage == index
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null, // 移除ripple，不引入material依赖
                            onClick = { handlePageChange(index) }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val iconColor = if (isSelected) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                    }
                    Icon(
                        imageVector = if (isSelected) destination.iconSelected else destination.iconNotSelected,
                        contentDescription = stringResource(destination.label),
                        tint = iconColor,
                        modifier = Modifier.sizeIn(maxHeight = 24.dp)
                    )
                    Text(
                        text = stringResource(destination.label),
                        style = MiuixTheme.textStyles.footnote2,
                        color = iconColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

enum class BottomBarDestination(
    @param:StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
    val kPatchRequired: Boolean,
    val aPatchRequired: Boolean,
) {
    Home(
        R.string.home,
        androidx.compose.material.icons.filled.Home,
        androidx.compose.material.icons.outlined.Home,
        false,
        false
    ),
    KModule(
        R.string.kpm,
        androidx.compose.material.icons.filled.Build,
        androidx.compose.material.icons.outlined.Build,
        true,
        false
    ),
    SuperUser(
        R.string.su_title,
        androidx.compose.material.icons.filled.Security,
        androidx.compose.material.icons.outlined.Security,
        true,
        false
    ),
    AModule(
        R.string.apm,
        androidx.compose.material.icons.filled.Extension,
        androidx.compose.material.icons.outlined.Extension,
        false,
        true
    ),
    Settings(
        R.string.settings,
        androidx.compose.material.icons.filled.Settings,
        androidx.compose.material.icons.outlined.Settings,
        false,
        false
    )
}
