package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class MessagePlainTextViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val markdown: ChatMarkdown,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemMessagePlainTextBinding =
        StreamUiItemMessagePlainTextBinding.inflate(
            parent.streamThemeInflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            configMargins(style)

            root.setOnClickListener {
                listeners.messageClickListener.onMessageClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            avatarView.setOnClickListener {
                listeners.userClickListener.onUserClick(data.message.user)
            }
            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = listeners.linkClickListener::onLinkClick
            )
        }
    }

    private fun StreamUiItemMessagePlainTextBinding.configMargins(style: MessageListItemStyle) {
        messageContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = style.messageStartMargin
            marginEnd = style.messageEndMargin
        }

        footnote.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = style.messageStartMargin
            marginEnd = style.messageEndMargin
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        with(binding) {
            markdown.setText(messageText, data.message.text)
            messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                horizontalBias = if (data.isTheirs) 0f else 1f
            }
        }
    }
}
