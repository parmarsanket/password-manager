package com.sanket.tools.passwordmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.ExportPackage
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.db.PasswordEntry
import com.sanket.tools.passwordmanager.data.export.ExportManager
import com.sanket.tools.passwordmanager.data.export.ImportManager
import com.sanket.tools.passwordmanager.data.repository.PasswordRepository
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.domain.model.DecryptedField
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Handles the Main Passworld List screen.
 */
class PassworldViewModel(
    private val repository: PasswordRepository,
    private val cryptoEngine: CryptoEngine,
    private val session: PassworldSession,
    private val exportManager: ExportManager,
    private val importManager: ImportManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val items: StateFlow<List<CredentialItem>> = combine(
        repository.getAllEntries(),
        _searchQuery,
        session.passworldKey
    ) { rawEntries, query, key ->
        if (key == null) return@combine emptyList<CredentialItem>()

        rawEntries
            .filter { it.entry.siteOrApp.contains(query, ignoreCase = true) }
            .map { relation ->
                CredentialItem(
                    entryId = relation.entry.id,
                    siteOrApp = relation.entry.siteOrApp,
                    iconEmoji = relation.entry.iconEmoji,
                    createdAt = relation.entry.createdAt,
                    updatedAt = relation.entry.updatedAt,
                    fields = relation.fields.sortedBy { it.sortOrder }.map { field ->
                        DecryptedField(
                            fieldId = field.fieldId,
                            label = field.fieldLabel,
                            value = cryptoEngine.decryptField(field.encryptedValue, key),
                            isSecret = field.isSecret,
                            sortOrder = field.sortOrder
                        )
                    }
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearch(query: String) {
        _searchQuery.value = query
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteEntry(PasswordEntry(id = id, siteOrApp = ""))
        }
    }

    fun logout() {
        session.stop()
    }

    suspend fun exportVault(masterPassword: String): ExportPackage {
        return exportManager.createExportPackage(masterPassword)
    }

    suspend fun importVault(pkg: ExportPackage, masterPassword: String): Int {
        return importManager.importPackage(pkg, masterPassword)
    }
}
