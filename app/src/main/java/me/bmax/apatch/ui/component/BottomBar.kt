package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.LocalHandlePageChange
import me.bmax.apatch.ui.LocalSelectedPage
import me.bmax.apatch.ui.theme.blurEffect
import me.bmax.apatch.ui.theme.getAppBarColor
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val PREF_BAR_CONFIG = "bottom_bar_visibility"
private const val KEY_SHOW_KMODULE = "show_kmodule"
private const val KEY_SHOW_SUPERUSER = "show_superuser"
private const val KEY_SHOW_AMODULE = "show_amodule"

@Composable
fun BottomBar(backdrop: LayerBackdrop) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sp = remember { context.getSharedPreferences(PREF_BAR_CONFIG, android.content.Context.MODE_PRIVATE) }

    var showKModule by remember { mutableStateOf(sp.getBoolean(KEY_SHOW_KMODULE, true)) }
    var showSuperUser by remember { mutableStateOf(sp.getBoolean(KEY_SHOW_SUPERUSER, true)) }
    var showAModule by remember { mutableStateOf(sp.getBoolean(KEY_SHOW_AMODULE, true)) }

    var showConfigureDialog by remember { mutableStateOf(false) }

    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPage = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    val availablePages = remember(kPatchReady, aPatchReady, showKModule, showSuperUser, showAModule) {
        BottomBarDestination.entries.filter { dest ->
            val systemOk = !(dest.kPatchRequired && !kPatchReady) && !(dest.aPatchRequired && !aPatchReady)
            val userVisible = when (dest) {
                BottomBarDestination.KModule -> showKModule
                BottomBarDestination.SuperUser -> showSuperUser
                BottomBarDestination.AModule -> showAModule
                else -> true
            }
            systemOk && userVisible
        }
    }

    val realSelectedIndex = remember(selectedPage, availablePages) {
        if (selectedPage >= availablePages.size) 0 else selectedPage
    }
    if (selectedPage >= availablePages.size) {
        handlePageChange(0)
    }

    NavigationBar(
        modifier = Modifier
            .blurEffect(backdrop)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showConfigureDialog = true })
            },
        color = backdrop.getAppBarColor()
    ) {
        availablePages.forEachIndexed { index, destination ->
            val isSelected = realSelectedIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { handlePageChange(index) },
                icon = if (isSelected) destination.iconSelected else destination.iconNotSelected,
                label = stringResource(destination.label)
            )
        }
    }

    if (showConfigureDialog) {
        AlertDialog(
            onDismissRequest = { showConfigureDialog = false },
            title = { Text("导航栏显示设置") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.kpm))
                        Checkbox(checked = showKModule, onCheckedChange = { showKModule = it })
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.su_title))
                        Checkbox(checked = showSuperUser, onCheckedChange = { showSuperUser = it })
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.apm))
                        Checkbox(checked = showAModule, onCheckedChange = { showAModule = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        sp.edit()
                            .putBoolean(KEY_SHOW_KMODULE, showKModule)
                            .putBoolean(KEY_SHOW_SUPERUSER, showSuperUser)
                            .putBoolean(KEY_SHOW_AMODULE, showAModule)
                            .apply()
                    }
                    showConfigureDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigureDialog = false }) {
                    Text("取消")
                }
            }
        )
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
