package com.myguide.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.myguide.colorScheme
import com.myguide.dialog
import com.myguide.toolbar

@Composable
fun MyDialog() {
    Dialog(
        onDismissRequest = { dialog.postValue(false) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { dialog.value = false }
                )
                .padding(top = 86.dp, start = 8.dp, end = 8.dp), // Adjust top padding as needed
            contentAlignment = Alignment.TopCenter // Aligns content to the top center
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surface,
                modifier = Modifier.padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    val list = toolbar.items.subList(1, toolbar.items.lastIndex.dec())
                    items(list.size) {
                        Text(
                            text = list[it].title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!toolbar.lock) {
                                        toolbar.lock = true
                                        toolbar.goto(it.inc())
                                        dialog.value = false
                                    }
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
