package com.sanket.tools.passwordmanager.ui.screen.vault

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.domain.model.DecryptedField
import com.sanket.tools.passwordmanager.ui.component.FieldItem
import com.sanket.tools.passwordmanager.ui.component.TemplateChip
import com.sanket.tools.passwordmanager.ui.component.VaultEntryBadge
import com.sanket.tools.passwordmanager.ui.component.vaultBadgeText
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import androidx.compose.runtime.rememberCoroutineScope
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditUiState
import com.sanket.tools.passwordmanager.ui.viewmodel.CategoryTemplate
import com.sanket.tools.passwordmanager.ui.util.SecureClipboardManager
import com.sanket.tools.passwordmanager.ui.util.ClipboardFeatureFlags
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip

@Composable
internal fun AnimatedModalOverlay(
    waitForContent: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable BoxScope.(p: Float, triggerDismiss: () -> Unit, layout: AdaptiveLayoutSpec, maxWidth: Dp, rootSize: androidx.compose.ui.unit.IntSize, signalReady: () -> Unit) -> Unit
) {
    var isClosing by remember { mutableStateOf(false) }
    var isContentReady by remember { mutableStateOf(!waitForContent) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isClosing, isContentReady) {
        if (!isContentReady) return@LaunchedEffect
        
        if (!isClosing) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                )
            )
        } else {
            progress.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
            onDismiss()
        }
    }

    val triggerDismiss = { isClosing = true }

    Dialog(
        onDismissRequest = triggerDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .pointerInput(Unit) { detectTapGestures() },
            contentAlignment = Alignment.Center
        ) {
            val p = progress.value
            val clampedP = p.coerceIn(0f, 1f)
            val scrimAlpha = lerp(0f, 0.55f, clampedP)
            val layout = adaptiveLayoutSpec(maxWidth, maxHeight)
            val availableWidth = maxWidth
            val rootSize = androidx.compose.ui.unit.IntSize(constraints.maxWidth, constraints.maxHeight)

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = triggerDismiss
                    )
            )

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                content(p, triggerDismiss, layout, availableWidth, rootSize) { isContentReady = true }
            }
        }
    }
}

@Composable
internal fun VaultDetailDialog(
    item: CredentialItem,
    sourceBounds: Rect? = null,
    clipboardManager: SecureClipboardManager,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isClosing by remember(item.entryId) { mutableStateOf(false) }
    var rootBounds by remember { mutableStateOf(Rect.Zero) }
    var targetSize by remember(item.entryId) { mutableStateOf(IntSize.Zero) }
    val progress = remember(item.entryId) { Animatable(0f) }

    LaunchedEffect(targetSize, rootBounds, isClosing) {
        val hasRootBounds = rootBounds.width > 1f && rootBounds.height > 1f
        if (targetSize == IntSize.Zero || (sourceBounds != null && !hasRootBounds)) {
            return@LaunchedEffect
        }

        if (isClosing) {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(240, easing = FastOutSlowInEasing)
            )
            onDismiss()
        } else {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.86f,
                    stiffness = 420f
                )
            )
        }
    }

    val triggerDismiss = {
        if (!isClosing) {
            isClosing = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .onGloballyPositioned { coordinates ->
                rootBounds = coordinates.boundsInWindow()
            }
            .pointerInput(Unit) { detectTapGestures() },
        contentAlignment = Alignment.Center
    ) {
        val layout = adaptiveLayoutSpec(maxWidth, maxHeight)
        val compactWidth = if (maxWidth > 32.dp) maxWidth - 32.dp else maxWidth
        val dialogWidth = when (layout.widthClass) {
            AdaptiveWidthClass.Compact -> compactWidth
            AdaptiveWidthClass.Medium -> 600.dp
            AdaptiveWidthClass.Expanded -> 700.dp
        }
        val dialogMaxHeight = if (maxHeight > 64.dp) maxHeight - 48.dp else maxHeight
        val titleStyle = when (layout.widthClass) {
            AdaptiveWidthClass.Compact -> MaterialTheme.typography.headlineSmall
            AdaptiveWidthClass.Medium -> MaterialTheme.typography.headlineMedium
            AdaptiveWidthClass.Expanded -> MaterialTheme.typography.displaySmall
        }.copy(fontWeight = FontWeight.SemiBold)
        val p = progress.value.coerceIn(0f, 1f)
        val contentAlpha = ((p - 0.42f) / 0.58f).coerceIn(0f, 1f)
        val startCorner = when (layout.widthClass) {
            AdaptiveWidthClass.Compact -> 24f
            AdaptiveWidthClass.Medium -> 28f
            AdaptiveWidthClass.Expanded -> 32f
        }
        val entryColors = vaultEntryColorScheme(item.entryId)
        val animatedShape = RoundedCornerShape(lerp(startCorner, 32f, p).dp)
        val targetWidthPx = targetSize.width.takeIf { it > 0 } ?: constraints.maxWidth
        val targetHeightPx = targetSize.height.takeIf { it > 0 } ?: constraints.maxHeight
        val targetLeft = (constraints.maxWidth - targetWidthPx) / 2f
        val targetTop = (constraints.maxHeight - targetHeightPx) / 2f
        val relativeSourceBounds = sourceBounds
            ?.takeIf { bounds ->
                bounds.width > 1f &&
                    bounds.height > 1f &&
                    rootBounds.width > 1f &&
                    rootBounds.height > 1f
            }
            ?.let { bounds ->
                Rect(
                    left = bounds.left - rootBounds.left,
                    top = bounds.top - rootBounds.top,
                    right = bounds.right - rootBounds.left,
                    bottom = bounds.bottom - rootBounds.top
                )
            }
        val startBounds = relativeSourceBounds ?: Rect(
            left = targetLeft + targetWidthPx * 0.45f,
            top = targetTop + targetHeightPx * 0.45f,
            right = targetLeft + targetWidthPx * 0.55f,
            bottom = targetTop + targetHeightPx * 0.55f
        )
        val scaleX = lerp((startBounds.width / targetWidthPx).coerceIn(0.05f, 1.5f), 1f, p)
        val scaleY = lerp((startBounds.height / targetHeightPx).coerceIn(0.05f, 1.5f), 1f, p)
        val currentLeft = lerp(startBounds.left, targetLeft, p)
        val currentTop = lerp(startBounds.top, targetTop, p)

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = lerp(0f, 0.55f, p)))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = triggerDismiss
                )
        )

        Box(
            modifier = Modifier
                .width(dialogWidth)
                .onSizeChanged { targetSize = it }
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    translationX = currentLeft - targetLeft
                    translationY = currentTop - targetTop
                    alpha = if (targetSize == IntSize.Zero) 0f else 1f
                    shadowElevation = lerp(0f, 36f, p)
                    shape = animatedShape
                    clip = true
                }
                .background(
                    entryColors.container,
                    animatedShape
                )
                .clip(animatedShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Box(modifier = Modifier.graphicsLayer { alpha = contentAlpha }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = dialogMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.siteOrApp,
                                style = titleStyle,
                                color = entryColors.onContainer
                            )
                            Text(
                                text = "${item.fields.size} saved field${if (item.fields.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = entryColors.supportingText
                            )
                        }

                        Box {
                            VaultEntryBadge(
                                text = vaultBadgeText(item.iconEmoji, item.siteOrApp),
                                layout = layout,
                                containerColor = entryColors.badgeContainer,
                                contentColor = entryColors.badgeContent
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onEdit(item.entryId) },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = entryColors.fieldContainer,
                                contentColor = entryColors.onContainer
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }

                        FilledTonalButton(
                            onClick = { showDeleteConfirm = true },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = entryColors.destructiveContainer,
                                contentColor = entryColors.onDestructiveContainer,
                            ),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item.fields.forEach { field ->
                            VaultDetailFieldCard(
                                field = field,
                                entryColors = entryColors,
                                clipboardManager = clipboardManager
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = triggerDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = entryColors.onContainer
                            )
                        ) {
                            Text("Close")
                        }
                    }
                }

                if (showDeleteConfirm) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(entryColors.container.copy(alpha = 0.96f))
                            .pointerInput(Unit) { detectTapGestures() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(entryColors.fieldContainer, RoundedCornerShape(28.dp))
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Delete entry?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = entryColors.onContainer
                            )
                            Text(
                                text = "\"${item.siteOrApp}\" and all its fields will be permanently deleted. This cannot be undone.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = entryColors.supportingText
                            )
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showDeleteConfirm = false },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = entryColors.onContainer
                                    )
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        showDeleteConfirm = false
                                        onDelete(item.entryId)
                                    },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun VaultDetailFieldCard(
    field: DecryptedField,
    entryColors: VaultEntryColorScheme,
    clipboardManager: SecureClipboardManager
) {
    // Bug fix: unique key per field using both index and fieldId so that
    // unsaved fields (fieldId == 0) each get their own saveable slot
    var revealed by rememberSaveable(field.fieldId, field.label) { mutableStateOf(!field.isSecret) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = entryColors.fieldContainer,
            contentColor = entryColors.onContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = entryColors.supportingText
                )
                Text(
                    text = if (revealed) field.value else "\u2022".repeat(12),
                    style = MaterialTheme.typography.bodyLarge,
                    color = entryColors.onContainer
                )
            }

            if (field.isSecret) {
                IconButton(onClick = { revealed = !revealed }) {
                    Icon(
                        imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = entryColors.onContainer
                    )
                }
            }

            IconButton(
                onClick = {
                    scope.launch {
                        clipboardManager.copySecure(
                            label = field.label,
                            text = field.value,
                            isSensitive = ClipboardFeatureFlags.markAsSensitive,
                            autoClearMillis = ClipboardFeatureFlags.autoClearTimeoutMillis,
                            oneTimePaste = ClipboardFeatureFlags.oneTimePasteEnabled
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = entryColors.onContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VaultEditorDialog(
    state: AddEditUiState,
    onDismiss: () -> Unit,
    onSiteNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onAddField: () -> Unit,
    onRemoveField: (Int) -> Unit,
    onFieldChange: (Int, String?, String?, Boolean?) -> Unit,
    onTemplateSelected: (CategoryTemplate) -> Unit,
    onSave: () -> Unit
) {
    AnimatedModalOverlay(onDismiss = onDismiss) { p, triggerDismiss, layout, maxWidth, _, _ ->
        val dialogWidth = when (layout.widthClass) {
            AdaptiveWidthClass.Compact -> maxWidth
            AdaptiveWidthClass.Medium -> 720.dp
            AdaptiveWidthClass.Expanded -> 820.dp
        }

        Box(
            modifier = Modifier
                .width(dialogWidth)
                .graphicsLayer {
                    val scale = lerp(0.85f, 1f, p)
                    scaleX = scale
                    scaleY = scale
                    val clampedP = p.coerceIn(0f, 1f)
                    alpha = clampedP
                    shadowElevation = lerp(0f, 32f, p)
                    shape = RoundedCornerShape(lerp(16f, 32f, p).dp)
                    clip = true
                }
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            val contentAlpha = ((p - 0.5f) * 2f).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 850.dp)
                    .graphicsLayer { alpha = contentAlpha }
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text  = if (state.isEditMode) "Edit entry" else "New entry",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (state.isEditMode)
                        "Edit the fields below and save your changes."
                    else
                        "Keep everything in one vault screen. Update the fields here and save.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (layout.widthClass == AdaptiveWidthClass.Compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.emoji,
                            onValueChange = onEmojiChange,
                            label = { Text("Badge") },
                            modifier = Modifier.width(96.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        OutlinedTextField(
                            value = state.siteName,
                            onValueChange = onSiteNameChange,
                            label = { Text("Site or app") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.emoji,
                            onValueChange = onEmojiChange,
                            label = { Text("Badge") },
                            modifier = Modifier.width(96.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        OutlinedTextField(
                            value = state.siteName,
                            onValueChange = onSiteNameChange,
                            label = { Text("Site or app") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }

                if (!state.isEditMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Quick templates",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TemplateChip("Website") {
                                onTemplateSelected(CategoryTemplate.WEBSITE)
                            }
                            TemplateChip("Bank") {
                                onTemplateSelected(CategoryTemplate.BANK)
                            }
                            TemplateChip("SIM") {
                                onTemplateSelected(CategoryTemplate.SIM)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.fields.forEachIndexed { index, field ->
                        FieldItem(
                            label = field.label,
                            value = field.value,
                            isSecret = field.isSecret,
                            onLabelChange = { onFieldChange(index, it, null, null) },
                            onValueChange = { onFieldChange(index, null, it, null) },
                            onToggleSecret = {
                                onFieldChange(index, null, null, !field.isSecret)
                            },
                            onRemove = { onRemoveField(index) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = onAddField,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add custom field")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = triggerDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave()
                            triggerDismiss()
                        },
                        enabled = state.siteName.isNotBlank()
                    ) {
                        Text(if (state.isEditMode) "Save changes" else "Save entry")
                    }
                }
            }
        }
    }
}

@Composable
internal fun BackupPasswordDialog(
    mode: BackupDialogMode,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by rememberSaveable(mode) { mutableStateOf("") }
    var passwordVisible by rememberSaveable(mode) { mutableStateOf(false) }

    val title = when (mode) {
        BackupDialogMode.Export -> "Export encrypted backup"
        BackupDialogMode.Import -> "Import encrypted backup"
    }
    val description = when (mode) {
        BackupDialogMode.Export ->
            "Create a backup file you can move between mobile and laptop. Use a backup password you will remember."
        BackupDialogMode.Import ->
            "Choose the backup file from your other device, then enter the same backup password used during export."
    }

    AnimatedModalOverlay(onDismiss = onDismiss) { p, triggerDismiss, layout, maxWidth, _, _ ->
        val dialogWidth = when (layout.widthClass) {
            AdaptiveWidthClass.Compact -> maxWidth
            else -> 480.dp
        }

        Box(
            modifier = Modifier
                .width(dialogWidth)
                .graphicsLayer {
                    val scale = lerp(0.85f, 1f, p)
                    scaleX = scale
                    scaleY = scale
                    val clampedP = p.coerceIn(0f, 1f)
                    alpha = clampedP
                    shadowElevation = lerp(0f, 32f, p)
                    shape = RoundedCornerShape(lerp(16f, 32f, p).dp)
                    clip = true
                }
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            val contentAlpha = ((p - 0.5f) * 2f).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha }
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Backup password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = null
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )

                if (isBusy) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (mode == BackupDialogMode.Export) {
                                "Saving backup..."
                            } else {
                                "Importing backup..."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = triggerDismiss,
                        enabled = !isBusy
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(password) },
                        enabled = password.isNotBlank() && !isBusy
                    ) {
                        Text(if (mode == BackupDialogMode.Export) "Export" else "Import")
                    }
                }
            }
        }
    }
}
