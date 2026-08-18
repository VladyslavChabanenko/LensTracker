package com.chb.lenstracker.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.action_cancel
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import org.jetbrains.compose.resources.stringResource

/** Reusable Yes/No confirmation dialog — used by "Mark as Changed" and any future destructive action. */
@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelLabel: String = stringResource(Res.string.action_cancel)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        }
    )
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    LensTrackerTheme {
        ConfirmDialog(
            title = "Mark as Changed?",
            body = "This resets your replacement cycle to start today.",
            confirmLabel = "Mark as Changed",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
