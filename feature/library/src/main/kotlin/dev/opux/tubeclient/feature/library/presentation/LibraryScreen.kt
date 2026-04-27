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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.core.ui.component.HistoryCard
import dev.opux.tubeclient.core.ui.util.formatViewCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onHistoryClick: (WatchHistoryEntry) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Kitaplık", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (selectedTab == 0 && state.history.isNotEmpty()) {
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
                    text = { Text("Geçmiş") },
                    modifier = Modifier.testTag("library_tab_history"),
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Abonelikler") },
                    modifier = Modifier.testTag("library_tab_subscriptions"),
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Ayarlar") },
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
                    else -> SettingsTab(
                        enabled = state.sponsorBlockEnabled,
                        onToggleCategory = viewModel::onToggleCategory,
                    )
                }
            }
        }
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
private fun SettingsTab(
    enabled: Set<SponsorBlockCategory>,
    onToggleCategory: (SponsorBlockCategory, Boolean) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "sponsorblock_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "SponsorBlock kategorileri",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Atlamak istediğin segment türlerini seç",
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
