package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff

public class GiphyViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer? = null,
    internal val binding: StreamUiItemMessageGiphyBinding = StreamUiItemMessageGiphyBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        data.message.attachments.firstOrNull()?.let(binding.mediaAttachmentView::showAttachment)

        binding.giphyTextLabel.text = trimText(data.message.text)

        listenerContainer?.also { listeners ->
            binding.cardView.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            binding.cancelButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(
                    data.message,
                    GiphyAction.CANCEL
                )
            }
            binding.sendButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(
                    data.message,
                    GiphyAction.SEND
                )
            }
            binding.nextButton.setOnClickListener {
                listeners.giphySendListener.onGiphySend(
                    data.message,
                    GiphyAction.SHUFFLE
                )
            }
        }
    }

    private fun trimText(text: String): String {
        return "\"${text.replace(GIPHY_PREFIX, "")}\""
    }

    private companion object {
        private const val GIPHY_PREFIX = "/giphy "
    }
}
