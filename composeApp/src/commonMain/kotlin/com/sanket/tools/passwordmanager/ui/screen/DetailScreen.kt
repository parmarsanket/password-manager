//package com.sanket.tools.passwordmanager.ui.screen
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxWithConstraints
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.widthIn
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ContentCopy
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.sanket.tools.passwordmanager.domain.model.CredentialItem
//import com.sanket.tools.passwordmanager.domain.model.DecryptedField
//import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
//import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
//import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel
//import org.koin.compose.koinInject
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DetailScreen(
//    entryId: Long,
//    viewModel: PassworldViewModel,
//    onEdit: (Long) -> Unit,
//    onBack: () -> Unit
//) {
//    val items by viewModel.items.collectAsState()
//    val credential = items.find { it.entryId == entryId }
//    val clipboardManager: ClipboardManager = koinInject()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Details") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { onEdit(entryId) }) {
//                        Icon(Icons.Default.Edit, contentDescription = "Edit")
//                    }
//                    IconButton(onClick = {
//                        viewModel.deleteEntry(entryId)
//                        onBack()
//                    }) {
//                        Icon(
//                            Icons.Default.Delete,
//                            contentDescription = "Delete",
//                            tint = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//            )
//        },
//        containerColor = MaterialTheme.colorScheme.background
//    ) { padding ->
//        BoxWithConstraints(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            val layout = adaptiveLayoutSpec(maxWidth, maxHeight)
//
//            if (credential == null) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(
//                            horizontal = layout.horizontalPadding,
//                            vertical = layout.verticalPadding
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "Entry not available.",
//                        style = MaterialTheme.typography.bodyLarge,
//                        textAlign = TextAlign.Center
//                    )
//                }
//                return@BoxWithConstraints
//            }
//
//            if (layout.useTwoPaneLayout) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(
//                            horizontal = layout.horizontalPadding,
//                            vertical = layout.verticalPadding
//                        ),
//                    horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing)
//                ) {
//                    DetailHeaderCard(
//                        item = credential,
//                        modifier = Modifier.width(300.dp)
//                    )
//
//                    LazyColumn(
//                        modifier = Modifier
//                            .weight(1f)
//                            .fillMaxHeight(),
//                        contentPadding = PaddingValues(bottom = layout.verticalPadding),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        items(credential.fields, key = { it.fieldId }) { field ->
//                            DetailFieldItem(field, clipboardManager)
//                        }
//                    }
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .widthIn(max = layout.contentMaxWidth)
//                        .align(Alignment.TopCenter),
//                    contentPadding = PaddingValues(
//                        start = layout.horizontalPadding,
//                        top = layout.verticalPadding,
//                        end = layout.horizontalPadding,
//                        bottom = layout.verticalPadding
//                    ),
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    item {
//                        DetailHeaderCard(item = credential)
//                    }
//
//                    items(credential.fields, key = { it.fieldId }) { field ->
//                        DetailFieldItem(field, clipboardManager)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun DetailHeaderCard(
//    item: CredentialItem,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(24.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp, vertical = 28.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Surface(
//                modifier = Modifier.size(88.dp),
//                shape = RoundedCornerShape(24.dp),
//                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
//            ) {
//                Box(contentAlignment = Alignment.Center) {
//                    Text(text = item.iconEmoji, fontSize = 42.sp)
//                }
//            }
//            Spacer(modifier = Modifier.height(18.dp))
//            Text(
//                text = item.siteOrApp,
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "${item.fields.size} saved field${if (item.fields.size == 1) "" else "s"}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}
//
//@Composable
//fun DetailFieldItem(field: DecryptedField, clipboardManager: ClipboardManager) {
//    var revealed by rememberSaveable(field.fieldId) { mutableStateOf(!field.isSecret) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = field.label,
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.primary
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = if (revealed) field.value else "********",
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            if (field.isSecret) {
//                IconButton(onClick = { revealed = !revealed }) {
//                    Icon(
//                        imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                        contentDescription = null
//                    )
//                }
//            }
//
//            IconButton(onClick = {
//                clipboardManager.copyToClipboard(field.label, field.value)
//            }) {
//                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
//            }
//        }
//    }
//}
