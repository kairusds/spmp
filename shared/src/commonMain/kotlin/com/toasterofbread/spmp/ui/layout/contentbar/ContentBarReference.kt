package com.toasterofbread.spmp.ui.layout.contentbar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.toasterofbread.spmp.ui.layout.contentbar.InternalContentBar
import com.toasterofbread.spmp.platform.AppContext

@Serializable
data class ContentBarReference(val type: Type, val index: Int) {
    enum class Type {
        INTERNAL,
        CUSTOM,
        TEMPLATE
    }

    fun getBar(context: AppContext, custom_bars: List<CustomContentBar>? = null): ContentBar? =
        when (type) {
            Type.INTERNAL -> InternalContentBar.ALL.getOrNull(index)
            Type.CUSTOM -> {
                val bars: List<CustomContentBar> = custom_bars ?: context.settings.layout.CUSTOM_BARS.get()
                bars.getOrNull(index)
            }
            Type.TEMPLATE -> CustomContentBarTemplate.entries[index].getContentBar()
        }

    companion object {
        fun ofInternalBar(internal_bar: InternalContentBar): ContentBarReference =
            ContentBarReference(Type.INTERNAL, internal_bar.index)

        fun ofCustomBar(bar_index: Int): ContentBarReference =
            ContentBarReference(Type.CUSTOM, bar_index)

        fun ofTemplate(template: CustomContentBarTemplate): ContentBarReference =
            ContentBarReference(Type.TEMPLATE, template.ordinal)
    }
}
