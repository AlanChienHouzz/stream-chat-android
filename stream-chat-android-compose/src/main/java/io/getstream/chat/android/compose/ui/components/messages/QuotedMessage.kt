/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.utils.extensions.isMine
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Wraps the quoted message into a component that shows only the sender avatar, text and single attachment preview.
 *
 * @param message Message to show.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param modifier Modifier for styling.
 * @param leadingContent The content shown at the start of the quoted message. By default we provide
 * [DefaultQuotedMessageLeadingContent] which shows the sender avatar in case the sender is not the current user.
 * @param centerContent The content shown at the center of the quoted message. By default we provide
 * [DefaultQuotedMessageCenterContent] which shows single attachment preview and message text inside a bubble.
 * @param trailingContent The content shown at the end of the quoted message. By default we provide
 * [DefaultQuotedMessageTrailingContent] which shows the sender avatar in case the sender is the current user.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun QuotedMessage(
    message: Message,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (Message) -> Unit = { DefaultQuotedMessageLeadingContent(message = it) },
    centerContent: @Composable RowScope.(Message) -> Unit = { DefaultQuotedMessageCenterContent(it) },
    trailingContent: @Composable (Message) -> Unit = { DefaultQuotedMessageTrailingContent(message = it) },
) {
    Row(
        modifier = modifier.combinedClickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
            onLongClick = { onLongItemClick(message) },
            onClick = { onQuotedMessageClick(message) }
        ),
        verticalAlignment = Alignment.Bottom
    ) {
        leadingContent(message)

        centerContent(message)

        trailingContent(message)
    }
}

/**
 * Represents the default content show at the start of the quoted message.
 *
 * By default we show the user avatar if the message doesn't belong to the current user.
 *
 * @param message The quoted message.
 */
@Composable
internal fun DefaultQuotedMessageLeadingContent(message: Message) {
    if (!message.isMine(ChatClient.instance())) {
        Avatar(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(24.dp),
            imageUrl = message.user.image,
            initials = message.user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )

        Spacer(modifier = Modifier.size(8.dp))
    }
}

/**
 * Represents the default content show at the end of the quoted message.
 *
 * By default we show the user avatar if the message belongs to the current user.
 *
 * @param message The quoted message.
 */
@Composable
internal fun DefaultQuotedMessageTrailingContent(message: Message) {
    if (message.isMine(ChatClient.instance())) {
        Spacer(modifier = Modifier.size(8.dp))

        Avatar(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(24.dp),
            imageUrl = message.user.image,
            initials = message.user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )
    }
}

/**
 * Represents the default content shown in the center of the quoted message wrapped inside a message bubble.
 *
 * @param message The quoted message.
 */
@Composable
public fun RowScope.DefaultQuotedMessageCenterContent(
    message: Message,
) {
    QuotedMessageContent(
        message = message,
        modifier = Modifier.weight(1f, fill = false)
    )
}
