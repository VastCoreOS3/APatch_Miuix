package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.LocalHandlePageChange
import me.bmax.apatch.ui.LocalSelectedPage
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icons.MiuixIcons
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBar(
    backdrop: LayerBackdrop,
    blurActive: Boolean = true,
    blurRadius: Float = 25f
) {
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPageRaw: Int = LocalSelectedPage.current
    val handlePageChange: (Int) -> Unit = LocalHandlePageChange.current

    val availablePages: List<BottomBarDestination> = remember(kPatchReady, aPatchReady) {
        BottomBarDestination.entries.filter { d ->
            !(d.kPatchRequired && !kPatchReady) && !(d.aPatchRequired && !aPatchReady)
        }
    }

    val rawToFilterIndex: Map<Int, Int> = remember(availablePages) {
        val map = mutableMapOf<Int, Int>()
        availablePages.forEachIndexed { filterIdx, dest ->
            map[dest.ordinal] = filterIdx
        }
        map
    }

    val currentFilterIndex: Int? = rawToFilterIndex[selectedPageRaw]

    LaunchedEffect(currentFilterIndex) {
        if (currentFilterIndex == null) {
            handlePageChange(0)
        }
    }

    val displayIndex: Int = currentFilterIndex ?: 0

    val labelList: List<String> = remember(availablePages) {
        availablePages.map { stringResource(it.label) }
    }
    val iconsSelected: List<ImageVector> = remember(availablePages) {
        availablePages.map { it.iconSelected }
    }
    val iconsUnselected: List<ImageVector> = remember(availablePages) {
        availablePages.map { it.iconNotSelected }
    }

    FloatingBottomNavigationBarAdapt(
        items = labelList,
        iconsSelected = iconsSelected,
        iconsUnselected = iconsUnselected,
        selectedIndex = displayIndex,
        backdrop = backdrop,
        blurActive = blurActive,
        blurRadius = blurRadius,
        onItemSelected = { filterIndex ->
            val targetDest = availablePages[filterIndex]
            handlePageChange(targetDest.ordinal)
        }
    )
}

@Composable
fun FloatingBottomNavigationBarAdapt(
    items: List<String>,
    iconsSelected: List<ImageVector>,
    iconsUnselected: List<ImageVector>,
    selectedIndex: Int,
    backdrop: LayerBackdrop?,
    blurActive: Boolean,
    blurRadius: Float,
    onItemSelected: (Int) -> Unit,
) {
    val floatingBarColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer
    val floatingBarShape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingNavigationBar(
            modifier = if (blurActive && backdrop != null) {
                Modifier
                    .widthIn(max = 440.dp)
                    .textureBlur(
                        backdrop = backdrop,
                        shape = floatingBarShape,
                        blurRadius = blurRadius,
                        colors = BlurDefaults.blurColors(
                            blendColors = listOf(
                                BlendColorEntry(color = MiuixTheme.colorScheme.surfaceContainer.copy(0.4f))
                            )
                        )
                    )
            } else {
                Modifier.widthIn(max = 440.dp)
            },
            color = floatingBarColor
        ) {
            items.forEachIndexed { index, label ->
                val currentIcon: ImageVector = if (selectedIndex == index) iconsSelected[index] else iconsUnselected[index]
                FloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = currentIcon,
                    label = label,
                    enabled = true
                )
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
        MiuixIcons.Filled.Home,
        MiuixIcons.Outlined.Home,
        false,
        false
    ),
    KModule(
        R.string.kpm,
        MiuixIcons.Filled.Construction,
        MiuixIcons.Outlined.Construction,
        true,
        false
    ),
    SuperUser(
        R.string.su_title,
        MiuixIcons.Filled.Shield,
        MiuixIcons.Outlined.Shield,
        true,
        false
    ),
    AModule(
        R.string.apm,
        MiuixIcons.Filled.Extension,
        MiuixIcons.Outlined.Extension,
        false,
        true
    ),
    Settings(
        R.string.settings,
        MiuixIcons.Filled.Settings,
        MiuixIcons.Outlined.Settings,
        false,
        false
    );
}
