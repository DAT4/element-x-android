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

package io.element.android.features.rageshake.detection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.rageshake.preferences.RageshakePreferencesEvents
import io.element.android.features.rageshake.preferences.RageshakePreferencesPresenter
import io.element.android.features.rageshake.rageshake.RageShake
import io.element.android.features.rageshake.screenshot.ImageResult
import io.element.android.features.rageshake.screenshot.ScreenshotHolder
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RageshakeDetectionPresenter @Inject constructor(
    private val screenshotHolder: ScreenshotHolder,
    private val rageShake: RageShake,
    private val preferencesPresenter: RageshakePreferencesPresenter,
) : Presenter<RageshakeDetectionState> {

    @Composable
    override fun present(): RageshakeDetectionState {
        val localCoroutineScope = rememberCoroutineScope()
        val preferencesState = preferencesPresenter.present()
        val isStarted = rememberSaveable {
            mutableStateOf(false)
        }
        val takeScreenshot = rememberSaveable {
            mutableStateOf(false)
        }
        val showDialog = rememberSaveable {
            mutableStateOf(false)
        }

        fun handleEvents(event: RageshakeDetectionEvents) {
            when (event) {
                RageshakeDetectionEvents.Disable -> {
                    preferencesState.eventSink(RageshakePreferencesEvents.SetIsEnabled(false))
                    showDialog.value = false
                }
                RageshakeDetectionEvents.StartDetection -> isStarted.value = true
                RageshakeDetectionEvents.StopDetection -> isStarted.value = false
                is RageshakeDetectionEvents.ProcessScreenshot -> localCoroutineScope.processScreenshot(takeScreenshot, showDialog, event.imageResult)
                RageshakeDetectionEvents.Dismiss -> showDialog.value = false
            }
        }

        val state = remember(preferencesState, isStarted.value, takeScreenshot.value, showDialog.value) {
            RageshakeDetectionState(
                isStarted = isStarted.value,
                takeScreenshot = takeScreenshot.value,
                showDialog = showDialog.value,
                preferenceState = preferencesState,
                eventSink = ::handleEvents
            )
        }

        LaunchedEffect(preferencesState.sensitivity) {
            rageShake.setSensitivity(preferencesState.sensitivity)
        }
        val shouldStart = preferencesState.isEnabled &&
            preferencesState.isSupported &&
            isStarted.value &&
            !takeScreenshot.value &&
            !showDialog.value

        LaunchedEffect(shouldStart) {
            handleRageShake(shouldStart, state, takeScreenshot)
        }
        return state
    }

    private fun handleRageShake(start: Boolean, state: RageshakeDetectionState, takeScreenshot: MutableState<Boolean>) {
        if (start) {
            rageShake.start(state.preferenceState.sensitivity)
            rageShake.interceptor = {
                takeScreenshot.value = true
            }
        } else {
            rageShake.stop()
            rageShake.interceptor = null
        }
    }

    private fun CoroutineScope.processScreenshot(takeScreenshot: MutableState<Boolean>, showDialog: MutableState<Boolean>, imageResult: ImageResult) = launch {
        screenshotHolder.reset()
        when (imageResult) {
            is ImageResult.Error -> {
                Timber.e(imageResult.exception, "Unable to write screenshot")
            }
            is ImageResult.Success -> {
                screenshotHolder.writeBitmap(imageResult.data)
            }
        }
        takeScreenshot.value = false
        showDialog.value = true
    }
}
