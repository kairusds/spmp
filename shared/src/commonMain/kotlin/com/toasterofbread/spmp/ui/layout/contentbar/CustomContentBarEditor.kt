package com.toasterofbread.spmp.ui.layout.contentbar

import LocalPlayerState
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.toasterofbread.composekit.platform.composable.platformClickable
import com.toasterofbread.composekit.settings.ui.Theme
import com.toasterofbread.composekit.utils.common.*
import com.toasterofbread.composekit.utils.composable.*
import com.toasterofbread.spmp.resources.getString
import com.toasterofbread.spmp.service.playercontroller.PlayerState
import com.toasterofbread.spmp.ui.layout.apppage.mainpage.appTextField
import com.toasterofbread.spmp.ui.layout.contentbar.element.*
import com.toasterofbread.spmp.ui.layout.nowplaying.maintab.vertical
import kotlin.math.roundToInt

private const val BAR_SIZE_DP_STEP: Float = 10f
private const val PREVIEW_BAR_SIZE_DP: Float = 60f
private const val VERTICAL_PREVIEW_FILL_ELEMENT_HEIGHT_DP: Float = 100f

internal abstract class CustomContentBarEditor() {
    private var _bar: CustomContentBar? by mutableStateOf(null)
    private var selected_element: Int? by mutableStateOf(null)
    private var show_template_selector: Boolean by mutableStateOf(false)

    private var bar: CustomContentBar
        get() = _bar!!
        set(value) { _bar = value }

    abstract fun commit(edited_bar: CustomContentBar)

    private fun useVerticalBarLayout(): Boolean = true

    private fun editElementData(action: MutableList<ContentBarElement>.() -> Unit) {
        val data: MutableList<ContentBarElement> = bar.elements.toMutableList()
        action(data)
        bar = bar.copy(elements = data)
        commit(bar)
    }

    private fun deleteElement(index: Int) {
        if (selected_element == index) {
            selected_element = null
        }
        else {
            selected_element?.also { element ->
                if (element > index) {
                    selected_element = element - 1
                }
            }
        }

        editElementData {
            removeAt(index)
        }
    }

    private fun onElementClicked(index: Int) {
        if (selected_element == index) {
            selected_element = null
        }
        else {
            selected_element = index
        }
    }

    @Composable
    fun Editor(initial_bar: CustomContentBar) {
        val player: PlayerState = LocalPlayerState.current
        val density: Density = LocalDensity.current

        if (_bar == null) {
            _bar = initial_bar
        }

        LaunchedEffect(Unit) {
            selected_element = null
            show_template_selector = false
        }

        LaunchedEffect(initial_bar) {
            bar = initial_bar
        }

        if (show_template_selector) {
            CustomContentBarTemplate.SelectionDialog { template ->
                show_template_selector = false
                if (template == null) {
                    return@SelectionDialog
                }

                editElementData {
                    clear()
                    addAll(template.getElements())
                }
            }
        }

        StickyHeightColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BarConfig(bar) {
                bar = it
                commit(bar)
            }

            val vertical_bar: Boolean = useVerticalBarLayout()
            RowOrColumn(
                vertical_bar,
                Modifier
                    .background(player.theme.vibrant_accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(15.dp),
                alignment = -1
            ) {
                CompositionLocalProvider(LocalContentColor provides player.theme.vibrant_accent.getContrasted()) {
                    if (vertical_bar) {
                        var column_height: Dp by remember { mutableStateOf(0.dp) }
                        BarPreview(
                            Modifier.padding(end = 40.dp),
                            Modifier.requiredHeightIn(min = column_height)
                        )

                        Column(
                            Modifier
                                .weight(1f)
                                .wrapContentHeight(unbounded = true)
                                .onSizeChanged {
                                    column_height = with (density) { it.height.toDp() }
                                }
                        ) {
                            BarContentButtons()
                            SelectedElementOptions()
                        }
                    }
                    else {
                        BarContentButtons(
                            Modifier.weight(1f).padding(bottom = 20.dp)
                        )
                        BarPreview(Modifier.padding(bottom = 30.dp))
                        SelectedElementOptions()
                    }
                }
            }
        }
    }

    @Composable
    private fun SelectedElementOptions(modifier: Modifier = Modifier) {
        val player: PlayerState = LocalPlayerState.current

        var element_index: Int by remember { mutableStateOf(selected_element ?: -1) }
        LaunchedEffect(selected_element) {
            selected_element?.also {
                element_index = it
            }
        }

        NullableValueAnimatedVisibility(
            selected_element?.let { bar.elements.getOrNull(it) },
            modifier,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) { element ->
            if (element == null) {
                return@NullableValueAnimatedVisibility
            }

            Box(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .background(player.theme.background, RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides player.theme.on_background) {
                    ElementEditor(
                        element,
                        element_index,
                        move = { to ->
                            if (to < 0 || to > bar.elements.size - 1) {
                                return@ElementEditor
                            }

                            editElementData {
                                add(to, removeAt(element_index))
                                selected_element = to
                            }
                        }
                    ) { edited ->
                        editElementData {
                            this[element_index] = edited
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun BarContentButtons(modifier: Modifier = Modifier) {
        val player: PlayerState = LocalPlayerState.current
        val button_colours: ButtonColors = ButtonDefaults.buttonColors(
            containerColor = player.theme.background,
            contentColor = player.theme.on_background
        )

        FlowRow(
            modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ElementSelector(button_colours, Modifier.fillMaxWidth().weight(1f)) { element_type ->
                bar = bar.copy(
                    elements = listOf(element_type.createElement()) + bar.elements
                )
                selected_element = 0

                commit(bar)
            }

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CustomContentBarCopyPasteButtons(
                    bar.elements,
                    Modifier.align(Alignment.CenterVertically)
                ) {
                    bar = bar.copy(elements = it)
                    commit(bar)
                }

                Button(
                    { show_template_selector = !show_template_selector },
                    colors = button_colours,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.ViewQuilt, null, Modifier.padding(end = 10.dp))
                    Text(getString("content_bar_editor_pick_template"), softWrap = false)
                }
            }
        }
    }

    @Composable
    private fun BarPreview(modifier: Modifier = Modifier, size_modifier: Modifier = Modifier) {
        val player: PlayerState = LocalPlayerState.current
        val vertical_bar: Boolean = useVerticalBarLayout()
        val delete_button_offset: Dp = 42.dp

        var text_height: Dp by remember { mutableStateOf(0.dp) }
        var bar_height: Dp by remember { mutableStateOf(0.dp) }

        Box(
            modifier
                .then(size_modifier)
                .background(player.theme.background, RoundedCornerShape(16.dp))
        ) {
            bar.CustomBarContent(
                scrolling = !vertical_bar,
                vertical = vertical_bar,
                background_colour = Theme.Colour.BACKGROUND,
                content_padding = PaddingValues(),
                apply_size = false,
                selected_element_override = selected_element?.takeIf { bar.elements[it] !is ContentBarElementSpacer } ?: -1,
                modifier = size_modifier
                    .run {
                        if (vertical_bar) width(PREVIEW_BAR_SIZE_DP.dp)
                        else fillMaxWidth().height(PREVIEW_BAR_SIZE_DP.dp)
                    }
                    .padding(5.dp),
                getSpacerElementModifier = { index, spacer -> with(spacer) {
                    Modifier
                        .clickable { onElementClicked(index) }
                        .border(2.dp, player.theme.vibrant_accent)
                        .thenIf(index == selected_element) {
                            background(player.theme.vibrant_accent)
                        }
                }},
                shouldShowButton = { true },
                getFillLengthModifier = {
                    if (vertical_bar) Modifier.height(VERTICAL_PREVIEW_FILL_ELEMENT_HEIGHT_DP.dp)
                    else Modifier.weight(1f).fillMaxWidth()
                },
                buttonContent = { index, element, size ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .platformClickable(onClick = { onElementClicked(index) }),
                        contentAlignment = Alignment.Center
                    ) {
                        element.Element(
                            vertical_bar,
                            size,
                            enable_interaction = false
                        )
                    }

                    IconButton(
                        { deleteElement(index) },
                        Modifier
                            .run {
                                if (vertical_bar) offset(x = delete_button_offset)
                                else offset(y = delete_button_offset)
                            }
                            .wrapContentSize(unbounded = true)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = player.theme.background)
                    }
                }
            )

            if (bar.elements.isEmpty()) {
                Text(
                    getString("content_bar_editor_no_elements_added"),
                    Modifier
                        .align(Alignment.Center)
                        .thenIf(vertical_bar) {
                            rotate(-90f).vertical().padding(horizontal = 10.dp)
                        },
                    color = player.theme.on_background
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ElementEditor(
        element: ContentBarElement,
        index: Int,
        move: (Int) -> Unit,
        modifier: Modifier = Modifier,
        onModification: (ContentBarElement) -> Unit
    ) {
        Column(
            modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                WidthShrinkText(
                    getString("content_bar_editor_configure_element_\$x").replace("\$x", (index + 1).toString()),
                    Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.fillMaxWidth().weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        getString("content_bar_editor_move_element"),
                        Modifier.align(Alignment.CenterVertically),
                        softWrap = false
                    )

                    IconButton({ move(index - 1) }) {
                        Icon(
                            if (useVerticalBarLayout()) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowLeft,
                            null
                        )
                    }

                    IconButton({ move(index + 1) }) {
                        Icon(
                            if (useVerticalBarLayout()) Icons.Default.KeyboardArrowDown
                            else Icons.Default.KeyboardArrowRight,
                            null
                        )
                    }
                }
            }

            element.ConfigurationItems(onModification = onModification)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ElementSelector(
    button_colours: ButtonColors,
    modifier: Modifier = Modifier,
    onSelected: (ContentBarElement.Type) -> Unit
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Text(getString("content_bar_editor_add_element"))
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (type in ContentBarElement.Type.entries.filter { it.isAvailable() }) {
                Button(
                    { onSelected(type) },
                    colors = button_colours
                ) {
                    Icon(type.getIcon(), null)
                    Text(type.getName(), softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun BarConfig(bar: CustomContentBar, onEdit: (CustomContentBar) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Bar name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Text(getString("content_bar_editor_bar_name"))

            TextField(
                bar.bar_name,
                { onEdit(bar.copy(bar_name = it)) },
                Modifier.fillMaxWidth().weight(1f).appTextField(),
                singleLine = true
            )
        }

        // Bar size
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(getString("content_bar_editor_bar_size"))
            Spacer(Modifier.fillMaxWidth().weight(1f))

            IconButton({
                onEdit(bar.copy(size_dp = (bar.size_dp - BAR_SIZE_DP_STEP).coerceAtLeast(BAR_SIZE_DP_STEP)))
            }) {
                Icon(Icons.Default.Remove, null)
            }

            Text(bar.size_dp.roundToInt().toString() + "dp")

            IconButton({
                onEdit(bar.copy(size_dp = bar.size_dp + BAR_SIZE_DP_STEP))
            }) {
                Icon(Icons.Default.Add, null)
            }

            IconButton(
                {
                    onEdit(bar.copy(size_dp = CUSTOM_CONTENT_BAR_DEFAULT_SIZE_DP))
                },
                Modifier.padding(start = 10.dp)
                ) {
                Icon(Icons.Default.Refresh, null)
            }
        }
    }
}
