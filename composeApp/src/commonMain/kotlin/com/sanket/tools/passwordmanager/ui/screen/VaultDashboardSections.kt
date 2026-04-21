package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import co.touchlab.kermit.Logger
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  Layout helpers — eliminate duplicated Row+Sidebar+ContentPane blocks
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Two-pane layout used for both TabletLandscape (sidebarModifier = full width)
 * and PhoneLandscape (sidebarModifier = narrow compactSidebarWidth).
 * The only difference between the two callers is [sidebarModifier].
 */
@Composable
internal fun TwoPaneLayout(
    layout: AdaptiveLayoutSpec,
    sidebarModifier: Modifier,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    items: List<CredentialItem>,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onEntrySelected: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = layout.horizontalPadding,
                vertical   = layout.verticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing),
    ) {
        VaultSidebar(
            modifier       = sidebarModifier.fillMaxHeight(),
            layout         = layout,
            heroTitleStyle = heroTitleStyle,
            searchQuery    = searchQuery,
            totalEntries   = totalEntries,
            totalSecrets   = totalSecrets,
            onSearchChange = onSearchChange,
            onImport       = onImport,
            onExport       = onExport,
            onLogout       = onLogout,
        )
        VaultContentPane(
            modifier        = Modifier.weight(1f),
            layout          = layout,
            items           = items,
            onEntrySelected = onEntrySelected,
        )
    }
}


/**
 * Single-column layout.
 * - Phone portrait (centered=false) → CollapsiblePhonePortraitLayout:
 *   hero scrolls away, collapsed TopAppBar animates in with search + actions menu.
 * - Tablet portrait (centered=true)  → centered, width-constrained Column (unchanged).
 */
@Composable
internal fun SinglePaneLayout(
    layout: AdaptiveLayoutSpec,
    centered: Boolean,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    items: List<CredentialItem>,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onEntrySelected: (Long) -> Unit,
) {

        CollapsiblePhonePortraitLayout(
            layout         = layout,
            heroTitleStyle = heroTitleStyle,
            searchQuery    = searchQuery,
            totalEntries   = totalEntries,
            totalSecrets   = totalSecrets,
            items          = items,
            onSearchChange = onSearchChange,
            onImport       = onImport,
            onExport       = onExport,
            onLogout       = onLogout,
            onEntrySelected = onEntrySelected,
        )
}

/**
 * Phone portrait collapsible layout.
 *
 * Hero card collapses with parallax + scale + fade via [HeroScrollState].
 * A compact overflow ⋮ menu fades in once the hero is mostly collapsed.
 * The credential list sits below and receives leftover scroll via nested-scroll.
 *
 * All progress-derived floats use [derivedStateOf] so downstream reads
 * skip recomposition when the computed value hasn't actually changed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CollapsiblePhonePortraitLayout(
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    items: List<CredentialItem>,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onEntrySelected: (Long) -> Unit,
) {
    val density   = LocalDensity.current

    val maxCollapsePx = remember(density) {
        with(density) { (220.dp - 80.dp).toPx() }
    }

    val scope      = rememberCoroutineScope()
    val heroState  = remember { HeroScrollState(maxCollapsePx, scope) }
    val connection = rememberHeroScrollConnection(heroState)

    // ── Derived UI scalars — recomposition-safe ──────────────────────────

    val collapseUi = rememberHeroCollapseUiState(heroState, maxCollapsePx)
    val heroAreaScrollState = rememberScrollableState { it ->  0f }
    // Overflow menu state — hoisted so it survives alpha threshold crossings


    Column(modifier = Modifier.fillMaxSize().nestedScroll(connection)  .scrollable(
        state       = heroAreaScrollState,
        orientation = Orientation.Vertical,
    )) {

        // ── Hero + Search area ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()

                .padding(horizontal = layout.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero body — collapses with parallax + scale + fade
            Column(
                modifier = Modifier
                    .collapseByProgress(collapseUi.progress)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha        = collapseUi.heroAlpha
                        translationY = collapseUi.heroTranslationY
                        scaleX       = collapseUi.heroScale
                        scaleY       = collapseUi.heroScale
                    },
            ) {
                VaultHeroCard(
                    layout         = layout,
                    heroTitleStyle = heroTitleStyle,
                    totalEntries   = totalEntries,
                    totalSecrets   = totalSecrets,
                    onImport       = onImport,
                    onExport       = onExport,
                    onLogout       = onLogout,
                )
                Spacer(modifier = Modifier.height(layout.contentSpacing))
            }

            // Search + compact overflow menu row
            Row(verticalAlignment = Alignment.CenterVertically) {
                VaultSearchField(
                    value         = searchQuery,
                    onValueChange = onSearchChange,
                    modifier      = Modifier.weight(1f),
                )
                VaultCollapsedMenu(collapseUi.overflowAlpha,  onImport, onExport, onLogout)
            }
            Spacer(modifier = Modifier.height(layout.verticalPadding / 2))
        }
        // ── Credential list ─────────────────────────────────────────────
        VaultContentPane(
            modifier        = Modifier.weight(1f),
            layout          = layout,
            items           = items,
            onEntrySelected = onEntrySelected,
        )

    }
}

@Stable
class HeroCollapseUiState(
    heroState: HeroScrollState,
    maxCollapsePx: Float,
) {
    val progress by derivedStateOf { heroState.progress }

    val heroAlpha by derivedStateOf {
        val p = heroState.progress
        1f - (p * p)
    }

    val heroTranslationY by derivedStateOf {
        -(heroState.progress * maxCollapsePx * 0.4f)
    }

    val heroScale by derivedStateOf {
        lerp(1f, 0.50f, heroState.progress)
    }

    val overflowAlpha by derivedStateOf {
        ((heroState.progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
    }

}

@Composable
fun rememberHeroCollapseUiState(
    heroState: HeroScrollState,
    maxCollapsePx: Float,
): HeroCollapseUiState = remember(heroState, maxCollapsePx) {
    HeroCollapseUiState(heroState, maxCollapsePx)
}

@Composable
private fun VaultCollapsedMenu(
    overflowAlpha: Float,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit
) {
    val menuExpanded = remember { mutableStateOf(false) }
    if (overflowAlpha > 0.01f) {
        Box(modifier = Modifier.graphicsLayer { alpha = overflowAlpha }) {
            IconButton(onClick = { menuExpanded.value = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions")
            }
            DropdownMenu(
                expanded = menuExpanded.value,
                onDismissRequest = { menuExpanded.value = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Import") },
                    leadingIcon = { Icon(Icons.Default.Download, null) },
                    onClick = { menuExpanded.value = false; onImport() },
                )
                DropdownMenuItem(
                    text = { Text("Export") },
                    leadingIcon = { Icon(Icons.Default.FileUpload, null) },
                    onClick = { menuExpanded.value = false; onExport() },
                )
                DropdownMenuItem(
                    text = { Text("Lock Vault") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    onClick = { menuExpanded.value = false; onLogout() },
                )
            }
        }
    }
}

/**
 * Compact top bar shown when the hero card has scrolled off-screen in phone portrait.
 * Contains: title, inline search field, and a ⋮ menu with Import / Export / Lock Vault.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultCollapsedTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color          = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp,
        modifier       = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Text(
                        text  = "Vault",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                        DropdownMenu(
                            expanded          = menuExpanded,
                            onDismissRequest  = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text         = { Text("Import") },
                                leadingIcon  = { Icon(Icons.Default.Download, null) },
                                onClick      = { menuExpanded = false; onImport() },
                            )
                            DropdownMenuItem(
                                text         = { Text("Export") },
                                leadingIcon  = { Icon(Icons.Default.FileUpload, null) },
                                onClick      = { menuExpanded = false; onExport() },
                            )
                            DropdownMenuItem(
                                text         = { Text("Lock Vault") },
                                leadingIcon  = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                                onClick      = { menuExpanded = false; onLogout() },
                            )
                        }
                    }
                },
            )
            VaultSearchField(
                value         = searchQuery,
                onValueChange = onSearchChange,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Toggleable Sidebar — NavigationRail ↔ Full Sidebar
// ─────────────────────────────────────────────────────────────────────────────

/** Width of the collapsed icon-only rail. */
private val RailWidth = 72.dp

/**
 * Sidebar used in two-pane layouts.
 *
 * - **PhoneLandscape**: always shows the compact [VaultPhoneLandscapeSidebar]
 *   (screen is too short for a rail toggle to be useful).
 * - **Tablet / Desktop**: toggles between a slim [VaultNavigationRail] (icon-only)
 *   and the full expanded sidebar ([VaultHeroCard] + [VaultSearchField]).
 *   Width animates with a spring, content cross-fades.
 */
@Composable
internal fun VaultSidebar(
    modifier: Modifier,
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(true) }
    val targetWidth = if (isExpanded) layout.sidebarWidth else RailWidth
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "sidebar-width",
    )

    if (layout.posture == AdaptivePosture.PhoneLandscape) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(tween(90)))
                    .using(SizeTransform(clip = false))
            },
            label = "sidebar-content",
        ) { expanded ->
            if (expanded) {

                VaultPhoneLandscapeSidebar(
                    modifier = modifier,
                    searchQuery = searchQuery,
                    totalEntries = totalEntries,
                    totalSecrets = totalSecrets,
                    onSearchChange = onSearchChange,
                    onImport = onImport,
                    onExport = onExport,
                    onLogout = onLogout,
                    onCollapse = { isExpanded = false }
                )
            } else {
                Surface(
                    modifier = Modifier,
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 1.dp,
                ) {
                    VaultNavigationRail(
                        onExpand = { isExpanded = true },
                        onImport = onImport,
                        onExport = onExport,
                        onLogout = onLogout,
                    )
                }

            }
        }

    } else {
        // Toggle state — starts expanded


        // Animate width with a spring for a premium feel
        Surface(
            modifier = Modifier.width(animatedWidth),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {

        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(tween(90)))
                    .using(SizeTransform(clip = false))
            },
            label = "sidebar-content",
        ) { expanded ->
            if (expanded) {
                // ── Full sidebar ─────────────────────────────────
                VaultExpandedSidebarContent(
                    layout = layout,
                    heroTitleStyle = heroTitleStyle,
                    searchQuery = searchQuery,
                    totalEntries = totalEntries,
                    totalSecrets = totalSecrets,
                    onSearchChange = onSearchChange,
                    onImport = onImport,
                    onExport = onExport,
                    onLogout = onLogout,
                    onCollapse = { isExpanded = false },
                )
            } else {
                // ── Icon-only rail ───────────────────────────────
                VaultNavigationRail(
                    onExpand = { isExpanded = true },
                    onImport = onImport,
                    onExport = onExport,
                    onLogout = onLogout,
                )
            }
        }
    }
}
}
/**
 * Full expanded sidebar content — hero card, search field, and a collapse toggle.
 */
@Composable
private fun VaultExpandedSidebarContent(
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onCollapse: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(state = rememberScrollState())
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(layout.contentSpacing),
    ) {
        // Collapse toggle at the top
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.MenuOpen,
                contentDescription = "Collapse sidebar",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        VaultHeroCard(
            layout         = layout,
            heroTitleStyle = heroTitleStyle,
            totalEntries   = totalEntries,
            totalSecrets   = totalSecrets,
            onImport       = onImport,
            onExport       = onExport,
            onLogout       = onLogout,
        )
        VaultSearchField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Icon-only NavigationRail — shows toggle, shield badge, import, export, lock.
 *
 * Custom implementation (not M3 NavigationRail) because we only need action
 * icons, not destination-based navigation with selection state.
 */
@Composable
private fun VaultNavigationRail(
    onExpand: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(state = rememberScrollState())
            .width(RailWidth)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Expand toggle
        IconButton(onClick = onExpand) {
            Icon(
                imageVector        = Icons.Default.Menu,
                contentDescription = "Expand sidebar",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Shield brand icon
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Icon(
                imageVector        = Icons.Default.Shield,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.padding(10.dp).size(22.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        // Action icons — bottom-anchored for easy thumb reach
        VaultRailIconButton(
            icon    = Icons.Default.Download,
            label   = "Import",
            onClick = onImport,
        )
        VaultRailIconButton(
            icon    = Icons.Default.FileUpload,
            label   = "Export",
            onClick = onExport,
        )
        VaultRailIconButton(
            icon    = Icons.AutoMirrored.Filled.ExitToApp,
            label   = "Lock",
            onClick = onLogout,
        )
    }
}

/**
 * Single icon button in the navigation rail with a small label below.
 */
@Composable
private fun VaultRailIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(22.dp),
            )
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * Compact sidebar for PhoneLandscape (~220 dp wide, ~360 dp tall).
 * Replaces the full VaultHeroCard (which is too tall for landscape) with:
 * - App title + shield icon (one line)
 * - Entry / secret counts (compact chips)
 * - Search field
 * - Import / Export / Lock as small full-width buttons
 * All wrapped in a verticalScroll so nothing is cut off.
 */
@Composable
private fun VaultPhoneLandscapeSidebar(
    modifier: Modifier,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onCollapse:() -> Unit

) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){


            // Compact title row
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp).size(18.dp),
                        )
                    }
                    Text(
                        text = "Vault",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                // Compact stats
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "$totalEntries",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = { Icon(Icons.Default.Key, null, Modifier.size(14.dp)) },
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "$totalSecrets",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = { Icon(Icons.Default.Lock, null, Modifier.size(14.dp)) },
                    )
                }
            }
            IconButton(onClick = onCollapse )
            {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = "Collapse sidebar",
//                    modifier = Modifier.size(25.dp)
                )
            }
        }

        // Search
        VaultSearchField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(4.dp))

        // ── Action buttons — compact, always visible ───────────────────────
        FilledTonalButton(
            onClick  = onImport,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Download, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Import", style = MaterialTheme.typography.labelMedium)
        }
        FilledTonalButton(
            onClick  = onExport,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.FileUpload, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Export", style = MaterialTheme.typography.labelMedium)
        }
        FilledTonalButton(
            onClick  = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Lock", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun VaultSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        placeholder = { Text("Search your sanctuary") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VaultHeroCard(
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    totalEntries: Int,
    totalSecrets: Int,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit
) {
    val heroPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 14.dp    // tighter on phones
        AdaptiveWidthClass.Medium   -> 24.dp
        AdaptiveWidthClass.Expanded -> 28.dp
    }
    val innerSpacing = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 10.dp    // was 18 — saves vertical space
        AdaptiveWidthClass.Medium   -> 16.dp
        AdaptiveWidthClass.Expanded -> 18.dp
    }
    // On phones, suppress the description to keep the card compact.
    // The tagline is redundant when the card scrolls away anyway.
    val showDescription = layout.widthClass != AdaptiveWidthClass.Compact
    val descriptionStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> MaterialTheme.typography.bodySmall
        AdaptiveWidthClass.Medium   -> MaterialTheme.typography.bodyLarge
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.headlineSmall
    }
    val shieldPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 8.dp
        AdaptiveWidthClass.Medium   -> 12.dp
        AdaptiveWidthClass.Expanded -> 14.dp
    }

    Card(
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f),
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
                .padding(heroPadding),
            verticalArrangement = Arrangement.spacedBy(innerSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Bug fix: weight(1f) prevents the title column from pushing
                // the shield icon off-screen on narrow Compact layouts
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text  = "Vault",
                        style = heroTitleStyle,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    // Description hidden on phones — card is compact + scrolls away
                    if (showDescription) {
                        Text(
                            text  = "Your private sanctuary for passwords, cards, and everyday credentials.",
                            style = descriptionStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(shieldPadding)
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VaultStatChip(icon = Icons.Default.Key, label = "$totalEntries entries")
                VaultStatChip(icon = Icons.Default.Lock, label = "$totalSecrets secrets")
            }

            VaultHeroActions(
                layout = layout,
                onImport = onImport,
                onExport = onExport,
                onLogout = onLogout
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VaultHeroActions(
    layout: AdaptiveLayoutSpec,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit
) {
    when (layout.widthClass) {
        // Compact – stack Import/Export side-by-side, Lock Vault full width below
        AdaptiveWidthClass.Compact -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onImport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import")
                    }
                    FilledTonalButton(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export")
                    }
                }
                FilledTonalButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Vault")
                }
            }
        }

        // Medium – all three actions in a wrapping row, full label text
        AdaptiveWidthClass.Medium -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(onClick = onImport, shape = RoundedCornerShape(22.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
                FilledTonalButton(onClick = onExport, shape = RoundedCornerShape(22.dp)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
                FilledTonalButton(onClick = onLogout, shape = RoundedCornerShape(22.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Vault")
                }
            }
        }

        // Expanded – compact labels (sidebar is narrow, every dp matters)
        AdaptiveWidthClass.Expanded -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(onClick = onImport) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
                FilledTonalButton(onClick = onExport) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
                FilledTonalButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock")
                }
            }
        }
    }
}

@Composable
internal fun VaultStatChip(
    icon: ImageVector,
    label: String,
) {
    // M3 SuggestionChip gives correct ripple, theming and accessibility for free
    SuggestionChip(
        onClick = {},
        label   = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        icon    = {
            Icon(
                imageVector     = icon,
                contentDescription = null,
                tint            = MaterialTheme.colorScheme.primary,
                modifier        = Modifier.size(18.dp),
            )
        },
    )
}

@Composable
internal fun VaultContentPane(
    modifier: Modifier,
    layout: AdaptiveLayoutSpec,
    items: List<CredentialItem>,
    onEntrySelected: (Long) -> Unit
) {
    val cardSpacing = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 14.dp
        AdaptiveWidthClass.Medium -> 16.dp
        AdaptiveWidthClass.Expanded -> 18.dp
    }

    Box(modifier = modifier) {
        when {
            // Empty vault – show placeholder regardless of width class
            items.isEmpty() -> EmptyState(modifier = Modifier.fillMaxSize())

            // Compact – single-column list
            layout.widthClass == AdaptiveWidthClass.Compact -> {
                LazyColumn(
                    state          = rememberLazyListState(),
                    contentPadding = PaddingValues(
                        start  = layout.horizontalPadding,
                        end    = layout.horizontalPadding,
                        top    = 0.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(layout.contentSpacing),
                ) {

                        items(items, key = { it.entryId }, contentType = { "credential" }) { item ->
                            VaultCredentialCard(
                                layout    = layout,
                                item      = item,
                                cardIndex = item.entryId,
                                onClick   = { onEntrySelected(item.entryId) },
                            )
                        }
                    }

            }

            // Medium & Expanded – adaptive grid
            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(layout.listMinCellSize),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                    verticalItemSpacing = cardSpacing
                ) {
                    items(
                        items = items,
                        key = { it.entryId }
                    ) { item ->
                        VaultCredentialCard(
                            layout = layout,
                            item = item,
                            cardIndex = item.entryId,
                            onClick = { onEntrySelected(item.entryId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun VaultCredentialCard(
    layout: AdaptiveLayoutSpec,
    item: CredentialItem,
    cardIndex: Long,
    onClick: () -> Unit
) {
    val cardPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 18.dp
        AdaptiveWidthClass.Medium -> 20.dp
        AdaptiveWidthClass.Expanded -> 24.dp
    }
    val cardShape = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 24.dp
        AdaptiveWidthClass.Medium   -> 28.dp
        AdaptiveWidthClass.Expanded -> 32.dp
    }
    val titleStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> MaterialTheme.typography.titleLarge
        AdaptiveWidthClass.Medium -> MaterialTheme.typography.headlineSmall
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.headlineMedium
    }.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium
    )
    val previewFields = item.fields.take(2)
    // Bug fix: use .mod(size) on hashCode() so very large Long entryIds
    // don't overflow Int and produce a negative mod result
    val cardColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.68f),
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.68f),
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
    )
    val colorIndex = item.entryId.hashCode().and(0x7FFF_FFFF).mod(cardColors.size)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cardShape),
        colors = CardDefaults.cardColors(
            containerColor = cardColors[colorIndex]
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VaultEntryBadge(
                    text = vaultBadgeText(item.iconEmoji, item.siteOrApp),
                    layout = layout
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.siteOrApp,
                        style = titleStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.fields.size} saved field${if (item.fields.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (previewFields.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.82f)
                    ) {
                        Text(
                            text = "No fields saved yet",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    previewFields.forEach { field ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.82f)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                VaultFieldPreview(
                                    layout = layout,
                                    label = field.label,
                                    value = if (field.isSecret) "\u2022".repeat(10) else field.value
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Tap or click to view, copy, edit, or delete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun VaultEntryBadge(
    text: String,
    layout: AdaptiveLayoutSpec
) {
    val badgeSize = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 52.dp
        AdaptiveWidthClass.Medium -> 56.dp
        AdaptiveWidthClass.Expanded -> 60.dp
    }
    // Bug fix: check size-class THEN char length so emojis and
    // short labels render at the right scale per screen class.
    val badgeTextStyle = when {
        layout.widthClass == AdaptiveWidthClass.Expanded ->
            if (text.length >= 3) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.headlineMedium
        layout.widthClass == AdaptiveWidthClass.Medium ->
            if (text.length >= 3) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.headlineSmall
        else -> // Compact
            if (text.length >= 3) MaterialTheme.typography.labelLarge
            else MaterialTheme.typography.headlineSmall
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f)
    ) {
        Box(
            modifier = Modifier.size(badgeSize),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = badgeTextStyle,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun vaultBadgeText(rawBadge: String, siteOrApp: String): String {
    val normalized = normalizedBadge(rawBadge)
    if (normalized.isNotBlank()) {
        return normalized
    }

    val fallback = siteOrApp
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()

    return fallback ?: "\uD83D\uDD10"
}

internal fun normalizedBadge(rawBadge: String): String {
    val trimmed = rawBadge.trim()
    if (trimmed.isBlank()) return ""
    // Use Unicode code-point count so multi-code-unit emojis (e.g. flag emojis)
    // are counted as 1 character, not 2+ UTF-16 code units.
    val codePointCount = trimmed.codePointCount(0, trimmed.length)
    if (codePointCount > 4) return ""
    if (trimmed.any { it.isWhitespace() }) return ""
    if (trimmed.contains('@') || trimmed.contains('.') || trimmed.contains('/') || trimmed.contains('\\')) {
        return ""
    }
    return trimmed
}

@Composable
internal fun VaultFieldPreview(
    layout: AdaptiveLayoutSpec,
    label: String,
    value: String
) {
    val valueStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> MaterialTheme.typography.bodyMedium
        AdaptiveWidthClass.Medium -> MaterialTheme.typography.bodyLarge
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.titleMedium
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = valueStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



fun Modifier.collapseByProgress(progress: Float): Modifier = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        // Natural height × (1 - progress): full → 0 automatically
        val collapsedHeight = (placeable.height * (1f - progress)).roundToInt()
        layout(placeable.width, collapsedHeight) {
            placeable.placeRelative(0, 0)
        }
    }

@Stable
class HeroScrollState(
    val maxCollapsePx: Float,
    private val scope: CoroutineScope,
) {
    var offset by mutableFloatStateOf(0f)
        private set

    val progress: Float
        get() = (-offset / maxCollapsePx).coerceIn(0f, 1f)

    private val isSettled: Boolean
        get() = progress <= 0.01f || progress >= 0.99f

    private var settleJob: Job? = null

    /**
     * Consume [delta] pixels of scroll for hero collapse/expand.
     * Cancels any in-flight settle animation so the user stays in control.
     */
    fun consumeScroll(delta: Float): Float {
        settleJob?.cancel()
        settleJob = null
        val newOffset = (offset + delta).coerceIn(-maxCollapsePx, 0f)
        val consumed = newOffset - offset
        offset = newOffset
        return consumed
    }

    /**
     * Debounced snap-to-boundary.  After 150 ms of scroll-idle the hero
     * animates to the nearest boundary (fully expanded or fully collapsed).
     * A new scroll event cancels the pending settle via [consumeScroll].
     */
    fun requestSettle() {
        settleJob?.cancel()
        if (isSettled) return          // already at a boundary
        settleJob = scope.launch {
            delay(150)                 // debounce — wait for scroll to stop
            if (isSettled) return@launch
            val target = if (progress < 0.5f) 0f else -maxCollapsePx
            val anim = Animatable(offset)
            anim.animateTo(
                targetValue   = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
            ) {
                // Update offset on every animation frame
                offset = value
            }
        }
    }
}



@Composable
fun rememberHeroScrollConnection(state: HeroScrollState) =
    remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                val deltaY = available.y

                // 👇 collapse on scroll up
                if (deltaY < 0) {
                    val consumed = state.consumeScroll(deltaY)
                    state.requestSettle()
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                val deltaY = available.y

                // 👇 expand on scroll down
                if (deltaY > 0) {
                    val consumedY = state.consumeScroll(deltaY)
                    state.requestSettle()
                    return Offset(0f, consumedY)
                }

                return Offset.Zero
            }
        }
    }