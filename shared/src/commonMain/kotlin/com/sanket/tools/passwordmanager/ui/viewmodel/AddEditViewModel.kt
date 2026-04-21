package com.sanket.tools.passwordmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.db.CredentialField
import com.sanket.tools.passwordmanager.data.db.PasswordEntry
import com.sanket.tools.passwordmanager.data.repository.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock

class AddEditViewModel(
    private val repository: PasswordRepository,
    private val cryptoEngine: CryptoEngine,
    private val session: PassworldSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState.empty())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun prepareNewEntry(template: CategoryTemplate? = null) {
        _uiState.value = AddEditUiState.empty()
        if (template != null) {
            applyTemplate(template)
        }
    }

    fun clearEditor() {
        _uiState.value = AddEditUiState.empty()
    }

    fun loadEntry(entryId: Long?) {
        if (entryId == null || entryId <= 0) {
            prepareNewEntry()
            return
        }

        _uiState.value = AddEditUiState.empty()

        viewModelScope.launch {
            val relation = repository.getEntryById(entryId).first() ?: return@launch
            val key = session.passworldKey.value ?: return@launch

            _uiState.value = AddEditUiState(
                isEditMode = true,
                entryId = entryId,
                siteName = relation.entry.siteOrApp,
                emoji = normalizeEntryBadge(relation.entry.iconEmoji),
                fields = relation.fields.sortedBy { it.sortOrder }.map { field ->
                    FieldState(
                        id = field.fieldId,
                        label = field.fieldLabel,
                        value = cryptoEngine.decryptField(field.encryptedValue, key),
                        isSecret = field.isSecret
                    )
                }
            )
        }
    }

    fun onSiteNameChange(name: String) {
        _uiState.value = _uiState.value.copy(siteName = name)
    }

    fun onEmojiChange(emoji: String) {
        _uiState.value = _uiState.value.copy(emoji = emoji)
    }

    fun addField() {
        _uiState.value = _uiState.value.copy(
            fields = _uiState.value.fields + FieldState()
        )
    }

    fun removeField(index: Int) {
        val currentFields = _uiState.value.fields
        if (index !in currentFields.indices) return

        _uiState.value = _uiState.value.copy(
            fields = currentFields.toMutableList().apply { removeAt(index) }
        )
    }

    fun onFieldChange(
        index: Int,
        label: String? = null,
        value: String? = null,
        isSecret: Boolean? = null
    ) {
        val currentFields = _uiState.value.fields
        if (index !in currentFields.indices) return

        val newFields = currentFields.toMutableList()
        val current = newFields[index]
        newFields[index] = current.copy(
            label = label ?: current.label,
            value = value ?: current.value,
            isSecret = isSecret ?: current.isSecret
        )
        _uiState.value = _uiState.value.copy(fields = newFields)
    }

    fun applyTemplate(template: CategoryTemplate) {
        val templateFields = when (template) {
            CategoryTemplate.WEBSITE -> listOf(
                FieldState(label = "Email"),
                FieldState(label = "Username"),
                FieldState(label = "Password", isSecret = true)
            )
            CategoryTemplate.BANK -> listOf(
                FieldState(label = "Account Number"),
                FieldState(label = "MPIN", isSecret = true),
                FieldState(label = "TPIN", isSecret = true),
                FieldState(label = "ATM PIN", isSecret = true)
            )
            CategoryTemplate.SIM -> listOf(
                FieldState(label = "Mobile Number"),
                FieldState(label = "PUK Code", isSecret = true),
                FieldState(label = "MNP Code", isSecret = true)
            )
        }

        val existingLabels = _uiState.value.fields
            .map { it.label.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()

        _uiState.value = _uiState.value.copy(
            fields = _uiState.value.fields + templateFields.filterNot { field ->
                field.label.trim().lowercase() in existingLabels
            }
        )
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        val key = session.passworldKey.value ?: return
        if (state.siteName.isBlank()) return

        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val normalizedFields = state.fields
                .filter { it.label.isNotBlank() || it.value.isNotBlank() }

            val entry = PasswordEntry(
                id = state.entryId ?: 0,
                siteOrApp = state.siteName.trim(),
                iconEmoji = normalizeEntryBadge(state.emoji),
                createdAt = if (state.isEditMode) 0 else now,
                updatedAt = now
            )

            val encryptedFields = normalizedFields.mapIndexed { index, field ->
                CredentialField(
                    fieldId = field.id,
                    entryId = state.entryId ?: 0,
                    fieldLabel = field.label.ifBlank { "Field ${index + 1}" },
                    encryptedValue = cryptoEngine.encryptField(field.value.trim(), key),
                    isSecret = field.isSecret,
                    sortOrder = index
                )
            }

            if (state.isEditMode) {
                repository.updateEntry(entry, encryptedFields)
            } else {
                repository.saveEntry(entry, encryptedFields)
            }

            clearEditor()
            onSuccess()
        }
    }
}

data class AddEditUiState(
    val isEditMode: Boolean = false,
    val entryId: Long? = null,
    val siteName: String = "",
    val emoji: String = "\uD83D\uDD10",
    val fields: List<FieldState> = emptyList()
) {
    companion object {
        fun empty(): AddEditUiState = AddEditUiState(
            fields = listOf(
                FieldState(label = "Username"),
                FieldState(label = "Password", isSecret = true)
            )
        )
    }
}

data class FieldState(
    val id: Long = 0,
    val label: String = "",
    val value: String = "",
    val isSecret: Boolean = false
)

enum class CategoryTemplate { WEBSITE, BANK, SIM }

private fun normalizeEntryBadge(rawBadge: String): String {
    val trimmed = rawBadge.trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.length > 4) return ""
    if (trimmed.any { it.isWhitespace() }) return ""
    if (trimmed.contains('@') || trimmed.contains('.') || trimmed.contains('/') || trimmed.contains('\\')) {
        return ""
    }
    return trimmed
}
