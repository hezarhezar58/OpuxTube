package dev.opux.tubeclient.feature.library.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.opux.tubeclient.feature.library.R
import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.ThemeMode
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.core.ui.component.HistoryCard
import dev.opux.tubeclient.core.ui.util.formatViewCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onHistoryClick: (WatchHistoryEntry) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onDownloadClick: (DownloadedVideo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    when {
                        selectedTab == 0 && state.history.isNotEmpty() -> {
                            IconButton(
                                onClick = viewModel::onClearHistory,
                                modifier = Modifier.testTag("library_clear"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteSweep,
                                    contentDescription = "Geçmişi temizle",
                                )
                            }
                        }
                        selectedTab == 2 -> {
                            IconButton(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.testTag("library_playlist_create"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Yeni liste",
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.library_tab_history)) },
                    modifier = Modifier.testTag("library_tab_history"),
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.library_tab_subscriptions)) },
                    modifier = Modifier.testTag("library_tab_subscriptions"),
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.library_tab_playlists)) },
                    modifier = Modifier.testTag("library_tab_playlists"),
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(stringResource(R.string.library_tab_downloads)) },
                    modifier = Modifier.testTag("library_tab_downloads"),
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text(stringResource(R.string.library_tab_settings)) },
                    modifier = Modifier.testTag("library_tab_settings"),
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                    selectedTab == 0 -> HistoryTab(
                        history = state.history,
                        onClick = onHistoryClick,
                    )
                    selectedTab == 1 -> SubscriptionsTab(
                        subscriptions = state.subscriptions,
                        onClick = onSubscriptionClick,
                    )
                    selectedTab == 2 -> PlaylistsTab(
                        playlists = state.playlists,
                        onClick = onPlaylistClick,
                        onDelete = viewModel::onDeletePlaylist,
                    )
                    selectedTab == 3 -> DownloadsTab(
                        downloads = state.downloads,
                        statuses = state.downloadStatuses,
                        onClick = onDownloadClick,
                        onDelete = viewModel::onDeleteDownload,
                    )
                    else -> SettingsTab(
                        enabled = state.sponsorBlockEnabled,
                        themeMode = state.themeMode,
                        downloadsCount = state.downloads.size,
                        cacheBytes = state.downloads.sumOf { it.fileSizeBytes },
                        onToggleCategory = viewModel::onToggleCategory,
                        onSelectThemeMode = viewModel::onSelectThemeMode,
                        onClearAllDownloads = viewModel::onClearAllDownloads,
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                viewModel.onCreatePlaylist(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun HistoryTab(
    history: List<WatchHistoryEntry>,
    onClick: (WatchHistoryEntry) -> Unit,
) {
    if (history.isEmpty()) {
        EmptyMessage(text = "Henüz izleme geçmişin yok")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = history,
            key = { _, entry -> entry.videoId },
        ) { index, entry ->
            HistoryCard(
                entry = entry,
                onClick = { onClick(entry) },
                modifier = Modifier.testTag("library_history_$index"),
            )
        }
    }
}

@Composable
private fun SubscriptionsTab(
    subscriptions: List<Subscription>,
    onClick: (Subscription) -> Unit,
) {
    if (subscriptions.isEmpty()) {
        EmptyMessage(text = "Henüz abone olduğun kanal yok")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = subscriptions,
            key = { _, sub -> sub.channelUrl },
        ) { index, sub ->
            SubscriptionRow(
                subscription = sub,
                onClick = { onClick(sub) },
                modifier = Modifier.testTag("library_subscription_$index"),
            )
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = subscription.avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subscription.subscriberCount > 0) {
                Text(
                    text = "${subscription.subscriberCount.formatViewCount()} abone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>,
    onClick: (Playlist) -> Unit,
    onDelete: (Long) -> Unit,
) {
    if (playlists.isEmpty()) {
        EmptyMessage(text = "Henüz liste oluşturmadın. Sağ üstteki + ile başla.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = playlists,
            key = { _, p -> p.id },
        ) { index, playlist ->
            PlaylistRow(
                playlist = playlist,
                onClick = { onClick(playlist) },
                onDelete = { onDelete(playlist.id) },
                modifier = Modifier.testTag("library_playlist_$index"),
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlaylistPlay,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (playlist.itemCount == 0) {
                    "Boş"
                } else {
                    "${playlist.itemCount} video"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("library_playlist_delete_${playlist.id}"),
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteSweep,
                contentDescription = "Listeyi sil",
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni liste") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Liste adı") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playlist_create_input"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("playlist_create_confirm"),
            ) { Text("Oluştur") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        },
    )
}

@Composable
private fun DownloadsTab(
    downloads: List<DownloadedVideo>,
    statuses: Map<String, DownloadStatus>,
    onClick: (DownloadedVideo) -> Unit,
    onDelete: (String) -> Unit,
) {
    val activeDownloads = statuses.filter { (id, status) ->
        // Only show in-flight rows when we don't already have a completed file for the
        // same id — once the DB row exists the entry list takes over.
        downloads.none { it.videoId == id } &&
            (status is DownloadStatus.InProgress || status is DownloadStatus.Queued)
    }
    if (downloads.isEmpty() && activeDownloads.isEmpty()) {
        EmptyMessage(text = "Henüz indirme yok. Bir videoda indirme simgesine dokun.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = activeDownloads.entries.toList(),
            key = { it.key },
        ) { (id, status) ->
            ActiveDownloadRow(
                videoId = id,
                status = status,
                modifier = Modifier.testTag("library_download_active_$id"),
            )
        }
        itemsIndexed(
            items = downloads,
            key = { _, d -> d.videoId },
        ) { index, d ->
            DownloadRow(
                downloaded = d,
                onClick = { onClick(d) },
                onDelete = { onDelete(d.videoId) },
                modifier = Modifier.testTag("library_download_$index"),
            )
        }
    }
}

@Composable
private fun ActiveDownloadRow(
    videoId: String,
    status: DownloadStatus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = if (status is DownloadStatus.Queued) "Sırada: $videoId" else "İndiriliyor: $videoId",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.size(4.dp))
        if (status is DownloadStatus.InProgress && status.totalBytes > 0) {
            LinearProgressIndicator(
                progress = { status.progress },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DownloadRow(
    downloaded: DownloadedVideo,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = downloaded.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${downloaded.channelName} · ${downloaded.fileSizeBytes.formatBytes()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("library_download_delete_${downloaded.videoId}"),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Silmek",
            )
        }
    }
}

private fun Long.formatBytes(): String {
    if (this <= 0L) return "0 B"
    val mb = this / (1024.0 * 1024.0)
    return if (mb >= 1024.0) {
        "%.1f GB".format(mb / 1024.0)
    } else {
        "%.1f MB".format(mb)
    }
}

@Composable
private fun SettingsTab(
    enabled: Set<SponsorBlockCategory>,
    themeMode: ThemeMode,
    downloadsCount: Int,
    cacheBytes: Long,
    onToggleCategory: (SponsorBlockCategory, Boolean) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onClearAllDownloads: () -> Unit,
) {
    var showConfirmClear by remember { mutableStateOf(false) }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "cache_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_cache_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (downloadsCount == 0) {
                        stringResource(R.string.settings_cache_empty)
                    } else {
                        stringResource(
                            R.string.settings_cache_summary,
                            downloadsCount,
                            cacheBytes.formatBytes(),
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (downloadsCount > 0) {
            item(key = "cache_clear") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showConfirmClear = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("library_clear_downloads"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.settings_cache_clear),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        item(key = "theme_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.settings_theme_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(items = ThemeMode.entries, key = { "theme_${it.name}" }) { mode ->
            ThemeModeRow(
                mode = mode,
                selected = mode == themeMode,
                onSelect = { onSelectThemeMode(mode) },
            )
        }
        item(key = "locale_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_locale_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.settings_locale_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item(key = "locale_options") {
            LocalePickerSection()
        }
        item(key = "sponsorblock_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_sponsorblock_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.settings_sponsorblock_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(
            items = SponsorBlockCategory.entries,
            key = { it.name },
        ) { category ->
            SponsorBlockCategoryRow(
                category = category,
                enabled = category in enabled,
                onToggle = { onToggleCategory(category, it) },
            )
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("İndirilenler silinecek") },
            text = {
                Text(
                    "Tüm indirilen videolar ve dosyalar kalıcı olarak silinecek. " +
                        "Bu işlem geri alınamaz.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllDownloads()
                        showConfirmClear = false
                    },
                    modifier = Modifier.testTag("library_clear_downloads_confirm"),
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) { Text("İptal") }
            },
        )
    }
}

@Composable
private fun ThemeModeRow(
    mode: ThemeMode,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("library_theme_${mode.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(
                when (mode) {
                    ThemeMode.LIGHT -> R.string.settings_theme_light
                    ThemeMode.DARK -> R.string.settings_theme_dark
                    ThemeMode.SYSTEM -> R.string.settings_theme_system
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SponsorBlockCategoryRow(
    category: SponsorBlockCategory,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("library_sb_${category.apiKey}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.label(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = category.description(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

private fun SponsorBlockCategory.label(): String = when (this) {
    SponsorBlockCategory.SPONSOR -> "Sponsor"
    SponsorBlockCategory.INTRO -> "Giriş"
    SponsorBlockCategory.OUTRO -> "Bitiş"
    SponsorBlockCategory.SELF_PROMO -> "Kanal tanıtımı"
    SponsorBlockCategory.INTERACTION -> "Etkileşim hatırlatması"
    SponsorBlockCategory.MUSIC_OFFTOPIC -> "Müzik dışı bölüm"
}

private fun SponsorBlockCategory.description(): String = when (this) {
    SponsorBlockCategory.SPONSOR -> "Ücretli sponsor reklamları"
    SponsorBlockCategory.INTRO -> "Açılış / hook bölümü"
    SponsorBlockCategory.OUTRO -> "Kapanış / endcard"
    SponsorBlockCategory.SELF_PROMO -> "Kendi ürün veya kanalını tanıtma"
    SponsorBlockCategory.INTERACTION -> "Beğen, abone ol gibi hatırlatmalar"
    SponsorBlockCategory.MUSIC_OFFTOPIC -> "Müzik videolarındaki konuşma kısımları"
}

@Composable
private fun LocalePickerSection() {
    // Three locale options: Turkish, English, and "follow system locale" (empty list).
    // AppCompat persists the choice in app metadata when autoStoreLocales is enabled,
    // and recreates active activities on change so the new resources kick in immediately.
    val current = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
    val activeTag: String = current.toLanguageTags().substringBefore(',').lowercase()

    @Composable
    fun row(
        labelRes: Int,
        tagToApply: String?,
        isActive: Boolean,
        testTag: String,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val list = if (tagToApply.isNullOrEmpty()) {
                        androidx.core.os.LocaleListCompat.getEmptyLocaleList()
                    } else {
                        androidx.core.os.LocaleListCompat.forLanguageTags(tagToApply)
                    }
                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(list)
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.RadioButton(selected = isActive, onClick = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        row(
            labelRes = R.string.settings_locale_system,
            tagToApply = null,
            isActive = activeTag.isEmpty(),
            testTag = "library_locale_system",
        )
        row(
            labelRes = R.string.settings_locale_tr,
            tagToApply = "tr",
            isActive = activeTag.startsWith("tr"),
            testTag = "library_locale_tr",
        )
        row(
            labelRes = R.string.settings_locale_en,
            tagToApply = "en",
            isActive = activeTag.startsWith("en"),
            testTag = "library_locale_en",
        )
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
