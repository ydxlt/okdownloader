package com.billbook.lib.downloader

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * @author xluotong@gmail.com
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    beans: List<ResourceBean>,
    viewModel: MainViewModel = hiltViewModel()
) {
    val states by viewModel.states.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Samples") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarState) }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(items = beans) { item ->
                val state = states.getOrElse(item.url) { DownloadState.IDLE }
                ListItem(
                    item = item,
                    state = state,
                    onClick = {
                        when (state) {
                            DownloadState.IDLE, DownloadState.PAUSE, DownloadState.ERROR -> {
                                viewModel.download(item)
                            }

                            is DownloadState.DOWNLOADING -> {
                                viewModel.cancel(item)
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
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        AsyncImage(
            modifier = Modifier.size(80.dp),
            model = item.icon,
            contentDescription = "Icon",
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "size: ${Formatter.formatFileSize(LocalContext.current, item.size)}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(20.dp))
            if(state is DownloadState.DOWNLOADING){
                LinearProgressIndicator(progress = state.progress)
            }
        }
        TextButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = onClick
        ) {
            Text(
                text = when (state) {
                    DownloadState.IDLE -> "Download"
                    DownloadState.FINISH -> "Success"
                    DownloadState.ERROR -> "Retry"
                    DownloadState.RETRYING -> "Retrying"
                    DownloadState.CHECKING -> "Checking"
                    DownloadState.PAUSE -> "Continue"
                    DownloadState.WAIT -> "Waiting"
                    is DownloadState.DOWNLOADING -> "Downloading"
                }
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen(beans = FakeData.resources)
}