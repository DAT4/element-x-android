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

package io.element.android.libraries.matrix.ui.media

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.media.MediaResolver
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

fun AvatarData.toMetadata(): MediaResolver.Meta {
    val mediaSource = url?.let { mediaSourceFromUrl(it) }
    return MediaResolver.Meta(source = mediaSource, kind = MediaResolver.Kind.Thumbnail(size.value))
}
