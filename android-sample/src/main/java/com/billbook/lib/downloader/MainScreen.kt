package com.billbook.lib.downloader

import android.text.format.Formatter
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * @author xluotong@gmail.com
 */
@Composable
internal fun MainScreen(
    beans: List<ResourceBean>,
    viewModel: MainViewModel = hiltViewModel()
) {
    val states by viewModel.states.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarState) }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(items = beans) { item ->
                val state = states.getOrElse(item.url) { DownloadState.IDLE }
                ListItem(
                    item = item,
                    state = state,
                    onClick = {
                        when (state) {
                            DownloadState.IDLE, is DownloadState.ERROR -> {
                                viewModel.download(item)
                            }

                            is DownloadState.DOWNLOADING -> {
                                viewModel.pause(item)
                            }

                            DownloadState.FINISH -> {
                                viewModel.redownload(item)
                            }

                            else -> {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ListItem(item: ResourceBean, state: DownloadState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp)),
            model = item.icon,
            contentDescription = "Icon",
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .height(70.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "size: ${Formatter.formatFileSize(LocalContext.current, item.size)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .width(110.dp),
                    onClick = onClick,
                    enabled = (state is DownloadState.RETRYING || state is DownloadState.CHECKING || state is DownloadState.WAIT).not()
                ) {
                    Text(
                        text = when (state) {
                            DownloadState.IDLE -> "Download"
                            DownloadState.FINISH -> "Success"
                            is DownloadState.ERROR -> "Retry"
                            DownloadState.RETRYING -> "Retrying"
                            DownloadState.CHECKING -> "Checking"
                            DownloadState.WAIT -> "Waiting"
                            is DownloadState.DOWNLOADING -> "Pause"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            when (state) {
                is DownloadState.DOWNLOADING -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            modifier = Modifier.weight(1f),
                            progress = state.progress
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format("%.2f", state.speed)} M/s",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                is DownloadState.ERROR -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "error code: ${state.code}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                DownloadState.FINISH -> {
                    Text(
                        text = "click to download again",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                else -> {}
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen(beans = FakeData.resources)
}