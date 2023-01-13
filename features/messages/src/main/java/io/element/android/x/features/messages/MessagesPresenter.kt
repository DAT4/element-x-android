package io.element.android.x.features.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.messages.actionlist.ActionListPresenter
import io.element.android.x.features.messages.actionlist.TimelineItemAction
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent
import io.element.android.x.features.messages.textcomposer.MessageComposerEvents
import io.element.android.x.features.messages.textcomposer.MessageComposerPresenter
import io.element.android.x.features.messages.textcomposer.MessageComposerState
import io.element.android.x.features.messages.timeline.TimelinePresenter
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.ui.MatrixItemHelper
import io.element.android.x.textcomposer.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MessagesPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val room: MatrixRoom,
    private val composerPresenter: MessageComposerPresenter,
    private val timelinePresenter: TimelinePresenter,
    private val actionListPresenter: ActionListPresenter,
) : Presenter<MessagesState> {

    private val matrixItemHelper = MatrixItemHelper(matrixClient)

    @Composable
    override fun present(): MessagesState {
        val localCoroutineScope = rememberCoroutineScope()
        val composerState = composerPresenter.present()
        val timelineState = timelinePresenter.present()
        val actionListState = actionListPresenter.present()

        val syncUpdateFlow = room.syncUpdateFlow().collectAsState(0L)
        val roomName: MutableState<String?> = rememberSaveable {
            mutableStateOf(null)
        }
        val roomAvatar: MutableState<AvatarData?> = remember {
            mutableStateOf(null)
        }
        LaunchedEffect(syncUpdateFlow) {
            roomAvatar.value =
                matrixItemHelper.loadAvatarData(
                    room = room,
                    size = AvatarSize.SMALL
                )
            roomName.value = room.name
        }

        fun handleEvents(event: MessagesEvents) {
            when (event) {
                is MessagesEvents.HandleAction -> localCoroutineScope.handleTimelineAction(event.action, event.messageEvent, composerState)
            }
        }
        return MessagesState(
            roomId = room.roomId,
            roomName = roomName.value,
            roomAvatar = roomAvatar.value,
            composerState = composerState,
            timelineState = timelineState,
            actionListState = actionListState,
            eventSink = ::handleEvents
        )
    }

    fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: MessagesTimelineItemState.MessageEvent,
        composerState: MessageComposerState,
    ) = launch {
        when (action) {
            TimelineItemAction.Copy -> notImplementedYet()
            TimelineItemAction.Forward -> notImplementedYet()
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit -> handleActionEdit(targetEvent, composerState)
            TimelineItemAction.Reply -> handleActionReply(targetEvent, composerState)
        }
    }

    private fun notImplementedYet() {
        Timber.v("NotImplementedYet")
    }

    private suspend fun handleActionRedact(event: MessagesTimelineItemState.MessageEvent) {
        room.redactEvent(event.id)
    }

    private fun handleActionEdit(targetEvent: MessagesTimelineItemState.MessageEvent, composerState: MessageComposerState) {
        val composerMode = MessageComposerMode.Edit(
            targetEvent.id,
            (targetEvent.content as? MessagesTimelineItemTextBasedContent)?.body.orEmpty()
        )
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }

    private fun handleActionReply(targetEvent: MessagesTimelineItemState.MessageEvent, composerState: MessageComposerState) {
        val composerMode = MessageComposerMode.Reply(targetEvent.safeSenderName, targetEvent.id, "")
        composerState.eventSink(
            MessageComposerEvents.SetMode(composerMode)
        )
    }
}
