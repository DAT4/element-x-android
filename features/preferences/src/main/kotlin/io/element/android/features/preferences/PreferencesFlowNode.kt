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

package io.element.android.features.preferences

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import io.element.android.features.preferences.root.PreferencesRootNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import kotlinx.parcelize.Parcelize

class PreferencesFlowNode(
    buildContext: BuildContext,
    private val onOpenBugReport: () -> Unit,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<PreferencesFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
) {

    private val preferencesRootNodeCallback = object : PreferencesRootNode.Callback {
        override fun onOpenBugReport() {
            onOpenBugReport.invoke()
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Root : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> createNode<PreferencesRootNode>(buildContext, plugins = listOf(preferencesRootNodeCallback))
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler()
        )
    }
}
