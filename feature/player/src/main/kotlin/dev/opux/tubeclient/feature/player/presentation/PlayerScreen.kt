package dev.opux.tubeclient.feature.player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import dev.opux.tubeclient.core.ui.util.LocalIsInPipMode
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import dev.opux.tubeclient.core.domain.model.Comment
import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.feature.player.R
import dev.opux.tubeclient.core.ui.component.VideoCard
import dev.opux.tubeclient.core.ui.util.formatRelativeUploadDate
import dev.opux.tubeclient.core.ui.util.formatViewCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onRelatedClick: (VideoPreview) -> Unit,
    onChannelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playback by viewModel.playbackState.collectAsStateWithLifecycle()
    val player by viewModel.playerFlow.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val commentsState by viewModel.comments.collectAsStateWithLifecycle()
    val repliesState by viewModel.replies.collectAsStateWithLifecycle()
    val isInPip = LocalIsInPipMode.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Snapshot the activity's orientation request the first time we land on this screen so
    // we can put things back exactly the way we found them on exit. Without this, exiting
    // fullscreen on devices with auto-rotate disabled can leave the OS pinned to landscape.
    val initialOrientation = remember {
        (context as? Activity)?.requestedOrientation
            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    DisposableEffect(isFullscreen) {
        val activity = context as? Activity
        val window = activity?.window
        if (activity != null && window != null) {
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                activity.requestedOrientation = initialOrientation
                WindowCompat.setDecorFitsSystemWindows(window, true)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (activity != null && window != null) {
                activity.requestedOrientation = initialOrientation
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    BackHandler(enabled = isFullscreen) { isFullscreen = false }

    LaunchedEffect(viewModel) {
        viewModel.skipEvents.collect { event ->
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.player_skipped_segment,
                    event.category.toTurkishLabel(),
                    (event.durationMs / 1000L).toInt(),
                ),
                duration = SnackbarDuration.Short,
            )
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.playlistAddedEvents.collect { name ->
            snackbarHostState.showSnackbar(
                message = if (name.isEmpty()) {
                    context.getString(R.string.player_playlist_added_generic)
                } else {
                    context.getString(R.string.player_playlist_added, name)
                },
                duration = SnackbarDuration.Short,
            )
        }
    }

    if (isInPip) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            if (player != null) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("player_surface"),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        }
                    },
                    update = {
                        it.player = player
                        it.useController = false
                    },
                )
            }
        }
        return
    }

    if (isFullscreen) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            if (player != null) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("player_surface"),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                            controllerAutoShow = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        }
                    },
                    update = { it.player = player },
                )
            }
            IconButton(
                onClick = { isFullscreen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .testTag("player_fullscreen_exit"),
            ) {
                Icon(
                    imageVector = Icons.Filled.FullscreenExit,
                    contentDescription = stringResource(R.string.player_fullscreen_exit),
                    tint = Color.White,
                )
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.detail?.title ?: stringResource(R.string.player_title_fallback),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("player_back"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.player_back),
                        )
                    }
                },
                actions = {
                    val detail = uiState.detail
                    if (detail != null) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, detail.url)
                                    putExtra(Intent.EXTRA_SUBJECT, detail.title)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        context.getString(R.string.player_share_chooser),
                                    ),
                                )
                            },
                            modifier = Modifier.testTag("player_share"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.player_share),
                            )
                        }
                        IconButton(
                            onClick = viewModel::onDownload,
                            enabled = downloadStatus !is DownloadStatus.InProgress &&
                                downloadStatus !is DownloadStatus.Queued,
                            modifier = Modifier.testTag("player_download"),
                        ) {
                            Icon(
                                imageVector = when (downloadStatus) {
                                    is DownloadStatus.Completed -> Icons.Filled.CheckCircle
                                    is DownloadStatus.InProgress,
                                    is DownloadStatus.Queued -> Icons.Filled.Downloading
                                    else -> Icons.Filled.Download
                                },
                                contentDescription = stringResource(R.string.player_download),
                            )
                        }
                        IconButton(
                            onClick = { showSleepSheet = true },
                            modifier = Modifier.testTag("player_sleep_timer"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = stringResource(R.string.player_sleep_timer),
                                tint = if (uiState.sleepTimerEndAtMs != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    androidx.compose.material3.LocalContentColor.current
                                },
                            )
                        }
                        IconButton(
                            onClick = { showSpeedSheet = true },
                            modifier = Modifier.testTag("player_speed"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = stringResource(R.string.player_speed),
                            )
                        }
                        IconButton(
                            onClick = { showQualitySheet = true },
                            modifier = Modifier.testTag("player_quality"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HighQuality,
                                contentDescription = stringResource(R.string.player_quality),
                            )
                        }
                        IconButton(
                            onClick = { showPlaylistSheet = true },
                            modifier = Modifier.testTag("player_playlist_add"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = stringResource(R.string.player_playlist_add),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            VideoSurface(
                player = player,
                onEnterFullscreen = { isFullscreen = true },
            )

            when {
                uiState.isLoading -> CenteredLoading()
                uiState.error != null -> ErrorView(
                    message = uiState.error ?: stringResource(R.string.player_error_generic),
                    onRetry = viewModel::retry,
                )
                uiState.detail != null -> DetailContent(
                    detail = uiState.detail!!,
                    comments = commentsState,
                    replies = repliesState,
                    onToggleReplies = viewModel::onToggleReplies,
                    onRelatedClick = onRelatedClick,
                    onChannelClick = onChannelClick,
                )
            }

            if (uiState.detail != null && playback.isBuffering) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            }
        }
    }

    if (showPlaylistSheet) {
        PlaylistPickerSheet(
            playlists = playlists,
            onPick = { id ->
                viewModel.addCurrentVideoToPlaylist(id)
                showPlaylistSheet = false
            },
            onCreateNew = {
                showPlaylistSheet = false
                showCreatePlaylistDialog = true
            },
            onDismiss = { showPlaylistSheet = false },
        )
    }
    if (showCreatePlaylistDialog) {
        NewPlaylistDialog(
            onConfirm = { name ->
                viewModel.createPlaylistAndAddCurrent(name)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false },
        )
    }
    if (showQualitySheet) {
        val detail = uiState.detail
        if (detail != null) {
            val autoLabel = stringResource(R.string.player_quality_auto)
            QualityPickerSheet(
                options = buildQualityOptions(detail, autoLabel),
                selected = uiState.qualityOverride,
                onPick = { stream ->
                    viewModel.onSelectQuality(stream)
                    showQualitySheet = false
                },
                onDismiss = { showQualitySheet = false },
            )
        }
    }
    if (showSpeedSheet) {
        SpeedPickerSheet(
            current = uiState.playbackSpeed,
            onPick = { speed ->
                viewModel.onSelectSpeed(speed)
                showSpeedSheet = false
            },
            onDismiss = { showSpeedSheet = false },
        )
    }
    if (showSleepSheet) {
        SleepTimerSheet(
            activeEndAtMs = uiState.sleepTimerEndAtMs,
            onPick = { durationMs ->
                viewModel.onSetSleepTimer(durationMs)
                showSleepSheet = false
            },
            onDismiss = { showSleepSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    activeEndAtMs: Long?,
    onPick: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val options: List<Pair<String, Long?>> = listOf(
        stringResource(R.string.player_sleep_off) to null,
        stringResource(R.string.player_sleep_5) to 5 * 60_000L,
        stringResource(R.string.player_sleep_15) to 15 * 60_000L,
        stringResource(R.string.player_sleep_30) to 30 * 60_000L,
        stringResource(R.string.player_sleep_60) to 60 * 60_000L,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("player_sleep_sheet"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.player_sleep_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            if (activeEndAtMs != null) {
                val remainingMin = ((activeEndAtMs - System.currentTimeMillis()).coerceAtLeast(0L) /
                    60_000L).toInt()
                Text(
                    text = stringResource(R.string.player_sleep_remaining, remainingMin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            HorizontalDivider()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(options, key = { _, opt -> opt.first }) { index, (label, durationMs) ->
                    val isActive = durationMs == null && activeEndAtMs == null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(durationMs) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("player_sleep_$index"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedPickerSheet(
    current: Float,
    onPick: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val options = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("player_speed_sheet"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.player_speed_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(options, key = { _, s -> s }) { index, speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(speed) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("player_speed_$index"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (speed == 1.0f) {
                                stringResource(R.string.player_speed_normal)
                            } else {
                                "${speed}x"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (speed == current) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

private fun buildQualityOptions(detail: VideoDetail, autoLabel: String): List<QualityOption> {
    val all = (detail.videoStreams + detail.videoOnlyStreams)
        .filter { it.height > 0 }
        .distinctBy { it.height }
        .sortedByDescending { it.height }
    return buildList {
        add(QualityOption(label = autoLabel, stream = null))
        addAll(all.map { QualityOption(label = "${it.height}p", stream = it) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualityPickerSheet(
    options: List<QualityOption>,
    selected: VideoStream?,
    onPick: (VideoStream?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("player_quality_sheet"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.player_quality_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(options, key = { _, opt -> opt.label }) { index, option ->
                    val isSelected = option.stream?.url == selected?.url
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option.stream) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("player_quality_$index"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistPickerSheet(
    playlists: List<Playlist>,
    onPick: (Long) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("player_playlist_sheet"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.player_playlist_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreateNew)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("player_playlist_create_new"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.player_playlist_create_new),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider()
            if (playlists.isEmpty()) {
                Text(
                    text = stringResource(R.string.player_playlist_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(playlists, key = { _, p -> p.id }) { index, p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(p.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("player_playlist_pick_$index"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (p.itemCount == 0) {
                                        stringResource(R.string.player_playlist_empty_label)
                                    } else {
                                        stringResource(
                                            R.string.player_playlist_video_count,
                                            p.itemCount,
                                        )
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun NewPlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.player_playlist_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.player_playlist_dialog_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_playlist_create_input"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("player_playlist_create_confirm"),
            ) { Text(stringResource(R.string.player_playlist_dialog_create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.player_cancel)) }
        },
    )
}

@Composable
private fun VideoSurface(
    player: Player?,
    onEnterFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .testTag("player_surface"),
    ) {
        if (player != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        controllerAutoShow = true
                        setShowSubtitleButton(true)
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                update = { view -> view.player = player },
            )
            DisposableEffect(player) {
                // Player is owned by the playback service via MediaController; the
                // PlayerView is just a remote-control surface, never release the player here.
                onDispose { }
            }
            IconButton(
                onClick = onEnterFullscreen,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .testTag("player_fullscreen_enter"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Fullscreen,
                    contentDescription = stringResource(R.string.player_fullscreen_enter),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: VideoDetail,
    comments: CommentsUiState,
    replies: Map<String, RepliesState>,
    onToggleReplies: (String, String?) -> Unit,
    onRelatedClick: (VideoPreview) -> Unit,
    onChannelClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = detail.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ChannelChip(
                    detail = detail,
                    onClick = { onChannelClick(detail.channel.url) },
                )
                val viewCountLabel = if (detail.viewCount > 0) {
                    stringResource(
                        R.string.player_view_count,
                        detail.viewCount.formatViewCount(),
                    )
                } else {
                    ""
                }
                val meta = buildString {
                    if (viewCountLabel.isNotEmpty()) append(viewCountLabel)
                    detail.uploadedAt.formatRelativeUploadDate()?.let {
                        if (isNotEmpty()) append(" · ")
                        append(it)
                    }
                }
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (detail.description.isNotBlank()) {
                    Text(
                        text = detail.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        item(key = "comments_header") {
            CommentsSection(
                state = comments,
                replies = replies,
                onToggleReplies = onToggleReplies,
            )
        }
        if (detail.relatedVideos.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.player_related_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            itemsIndexed(
                items = detail.relatedVideos,
                key = { _, item -> item.id.ifEmpty { item.url } },
            ) { index, related ->
                VideoCard(
                    video = related,
                    onClick = { onRelatedClick(related) },
                    modifier = Modifier.testTag("player_related_$index"),
                )
            }
        }
    }
}

@Composable
private fun CommentsSection(
    state: CommentsUiState,
    replies: Map<String, RepliesState>,
    onToggleReplies: (String, String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("player_comments"),
    ) {
        Text(
            text = if (state.items.isEmpty()) {
                stringResource(R.string.player_comments_title)
            } else {
                stringResource(R.string.player_comments_title_with_count, state.items.size)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.size(4.dp))
        when {
            state.isLoading -> Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.player_comments_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.error != null -> Text(
                text = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            state.items.isEmpty() -> Text(
                text = stringResource(R.string.player_comments_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.items.take(MAX_COMMENT_PREVIEW).forEachIndexed { index, comment ->
                    CommentRow(
                        comment = comment,
                        replies = replies[comment.id],
                        onToggleReplies = { onToggleReplies(comment.id, comment.repliesToken) },
                        modifier = Modifier.testTag("player_comment_$index"),
                    )
                }
                if (state.items.size > MAX_COMMENT_PREVIEW) {
                    Text(
                        text = stringResource(
                            R.string.player_comments_more,
                            state.items.size - MAX_COMMENT_PREVIEW,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    replies: RepliesState?,
    onToggleReplies: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = comment.authorAvatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val uploadedAt = comment.uploadedAt
                    if (!uploadedAt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uploadedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (comment.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📌",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                if (comment.likeCount > 0) {
                    Text(
                        text = "👍 ${comment.likeCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (comment.replyCount > 0 && comment.repliesToken != null) {
                    val expanded = replies is RepliesState.Loaded
                    Text(
                        text = if (expanded) {
                            stringResource(R.string.player_replies_hide)
                        } else {
                            stringResource(R.string.player_replies_show, comment.replyCount)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable(onClick = onToggleReplies)
                            .testTag("player_comment_replies_${comment.id}"),
                    )
                } else if (comment.replyCount > 0) {
                    Text(
                        text = stringResource(R.string.player_reply_count, comment.replyCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        when (replies) {
            is RepliesState.Loading -> Row(
                modifier = Modifier
                    .padding(start = 40.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.player_replies_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is RepliesState.Failed -> Text(
                text = replies.message,
                modifier = Modifier.padding(start = 40.dp, top = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            is RepliesState.Loaded -> Column(
                modifier = Modifier.padding(start = 40.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                replies.items.forEach { reply ->
                    ReplyRow(reply)
                }
            }
            null -> Unit
        }
    }
}

@Composable
private fun ReplyRow(reply: Comment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = reply.authorAvatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reply.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val uploadedAt = reply.uploadedAt
                if (!uploadedAt.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uploadedAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = reply.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (reply.likeCount > 0) {
                Text(
                    text = "👍 ${reply.likeCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val MAX_COMMENT_PREVIEW = 8

@Composable
private fun ChannelChip(
    detail: VideoDetail,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("player_channel_chip")
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = detail.channel.avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = detail.channel.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CenteredLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRetry) { Text(stringResource(R.string.player_retry)) }
        }
    }
}
