package com.toasterofbread.composesettings.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasterofbread.composesettings.ui.SettingsPage
import com.toasterofbread.spmp.platform.ProjectPreferences
import com.toasterofbread.spmp.ui.theme.Theme

// TODO Styling
class SettingsTextFieldItem(
    val state: BasicSettingsValueState<String>,
    val title: String?,
    val subtitle: String?,
    val single_line: Boolean = true
): SettingsItem() {
    override fun initialiseValueStates(prefs: ProjectPreferences, default_provider: (String) -> Any) {
        state.init(prefs, default_provider)
    }

    override fun resetValues() {
        state.reset()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun GetItem(theme: Theme, openPage: (Int) -> Unit, openCustomPage: (SettingsPage) -> Unit) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            ItemTitleText(title, theme)
            ItemText(subtitle, theme)
            TextField(state.value, { state.value = it }, Modifier.fillMaxWidth(), singleLine = single_line)
        }
    }
}