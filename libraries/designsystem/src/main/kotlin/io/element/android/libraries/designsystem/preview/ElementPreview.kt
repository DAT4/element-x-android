/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.theme.ElementTheme

@Composable
fun ElementPreviewLight(
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementPreview(
        darkTheme = false,
        showBackground = showBackground,
        content = content
    )
}

@Composable
fun ElementPreviewDark(
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementPreview(
        darkTheme = true,
        showBackground = showBackground,
        content = content
    )
}

@Composable
private fun ElementPreview(
    darkTheme: Boolean,
    showBackground: Boolean,
    content: @Composable () -> Unit
) {
    ElementTheme(darkTheme = darkTheme) {
        if (showBackground) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                content()
            }
        } else {
            content()
        }
    }
}

