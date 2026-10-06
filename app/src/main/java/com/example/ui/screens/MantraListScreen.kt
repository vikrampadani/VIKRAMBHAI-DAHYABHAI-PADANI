package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MantraEntity
import com.example.util.AppStrings

@Composable
fun MantraListScreen(
    isGujarati: Boolean,
    mantras: List<MantraEntity>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onMantraSelect: (Long) -> Unit,
    onToggleFavorite: (MantraEntity) -> Unit,
    onAddMantra: (String, String, String, String) -> Unit,
    onUpdateMantra: (MantraEntity) -> Unit,
    onDeleteMantra: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var mantraToEdit by remember { mutableStateOf<MantraEntity?>(null) }
    var mantraToDelete by remember { mutableStateOf<MantraEntity?>(null) }

    val filterOptions = listOf(
        "ALL" to AppStrings.get(AppStrings.Key.ALL_MANTRAS, isGujarati),
        "FAVORITE" to AppStrings.get(AppStrings.Key.FAVORITES_ONLY, isGujarati)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("fab_add_mantra")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = AppStrings.get(AppStrings.Key.ADD_MANTRA, isGujarati),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = AppStrings.get(AppStrings.Key.MY_MANTRAS, isGujarati),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${mantras.size} ${AppStrings.get(AppStrings.Key.MY_MANTRAS, isGujarati)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = if (selectedFilter == "FAVORITE") 1 else 0,
                edgePadding = 20.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                filterOptions.forEachIndexed { index, (key, label) ->
                    val selected = (key == "ALL" && selectedFilter == "ALL") ||
                            (key == "FAVORITE" && selectedFilter == "FAVORITE")
                    Tab(
                        selected = selected,
                        onClick = { onFilterChange(key) },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mantra List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(mantras, key = { it.id }) { mantra ->
                    MantraCard(
                        mantra = mantra,
                        isGujarati = isGujarati,
                        onClick = { onMantraSelect(mantra.id) },
                        onFavoriteClick = { onToggleFavorite(mantra) },
                        onEditClick = { mantraToEdit = mantra },
                        onDeleteClick = { mantraToDelete = mantra }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Add Mantra Dialog
    if (showAddDialog) {
        MantraFormDialog(
            isGujarati = isGujarati,
            title = AppStrings.get(AppStrings.Key.ADD_MANTRA, isGujarati),
            initialName = "",
            initialTransliteration = "",
            initialMeaning = "",
            initialDeity = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, transliteration, meaning, deity ->
                onAddMantra(name, transliteration, meaning, deity)
                showAddDialog = false
            }
        )
    }

    // Edit Mantra Dialog
    mantraToEdit?.let { mantra ->
        MantraFormDialog(
            isGujarati = isGujarati,
            title = AppStrings.get(AppStrings.Key.EDIT_MANTRA, isGujarati),
            initialName = mantra.name,
            initialTransliteration = mantra.transliteration,
            initialMeaning = mantra.meaning,
            initialDeity = mantra.deity,
            onDismiss = { mantraToEdit = null },
            onConfirm = { name, transliteration, meaning, deity ->
                onUpdateMantra(
                    mantra.copy(
                        name = name,
                        transliteration = transliteration,
                        meaning = meaning,
                        deity = deity
                    )
                )
                mantraToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    mantraToDelete?.let { mantra ->
        AlertDialog(
            onDismissRequest = { mantraToDelete = null },
            title = {
                Text(
                    text = AppStrings.get(AppStrings.Key.DELETE, isGujarati),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${AppStrings.get(AppStrings.Key.CONFIRM_DELETE, isGujarati)}\n\n\"${mantra.name}\"",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMantra(mantra.id)
                        mantraToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(AppStrings.get(AppStrings.Key.DELETE, isGujarati))
                }
            },
            dismissButton = {
                TextButton(onClick = { mantraToDelete = null }) {
                    Text(AppStrings.get(AppStrings.Key.CANCEL, isGujarati))
                }
            }
        )
    }
}

@Composable
private fun MantraCard(
    mantra: MantraEntity,
    isGujarati: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("mantra_card_${mantra.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (mantra.deity.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = mantra.deity,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = mantra.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (mantra.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (mantra.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!mantra.isDefault) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (mantra.meaning.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mantra.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start Japa Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.get(AppStrings.Key.START_JAPA, isGujarati),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MantraFormDialog(
    isGujarati: Boolean,
    title: String,
    initialName: String,
    initialTransliteration: String,
    initialMeaning: String,
    initialDeity: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, transliteration: String, meaning: String, deity: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var transliteration by remember { mutableStateOf(initialTransliteration) }
    var meaning by remember { mutableStateOf(initialMeaning) }
    var deity by remember { mutableStateOf(initialDeity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.get(AppStrings.Key.MANTRA_NAME, isGujarati)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = deity,
                    onValueChange = { deity = it },
                    label = { Text(AppStrings.get(AppStrings.Key.DEITY, isGujarati)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text(AppStrings.get(AppStrings.Key.MEANING, isGujarati)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, transliteration, meaning, deity)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(AppStrings.get(AppStrings.Key.SAVE_MANTRA, isGujarati))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get(AppStrings.Key.CANCEL, isGujarati))
            }
        }
    )
}
