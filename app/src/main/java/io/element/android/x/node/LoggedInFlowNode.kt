package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.x.architecture.createNode
import io.element.android.x.architecture.viewmodel.viewModelSupportNode
import io.element.android.x.features.messages.MessagesScreen
import io.element.android.x.features.preferences.PreferencesFlowNode
import io.element.android.x.features.roomlist.RoomListNode
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.core.SessionId
import kotlinx.parcelize.Parcelize

class LoggedInFlowNode(
    buildContext: BuildContext,
    val sessionId: SessionId,
    private val onOpenBugReport: () -> Unit,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.RoomList,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<LoggedInFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
) {

    private val roomListCallback = object : RoomListNode.Callback {
        override fun onRoomClicked(roomId: RoomId) {
            backstack.push(NavTarget.Messages(roomId))
        }

        override fun onSettingsClicked() {
            backstack.push(NavTarget.Settings)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Messages(val roomId: RoomId) : NavTarget

        @Parcelize
        object Settings : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomList -> {
                createNode<RoomListNode>(buildContext, plugins = listOf(roomListCallback))
            }
            is NavTarget.Messages -> viewModelSupportNode(buildContext) {
                MessagesScreen(
                    roomId = navTarget.roomId.value,
                    onBackPressed = { backstack.pop() }
                )
            }
            NavTarget.Settings -> {
                PreferencesFlowNode(buildContext, onOpenBugReport)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backstack)
    }
}
