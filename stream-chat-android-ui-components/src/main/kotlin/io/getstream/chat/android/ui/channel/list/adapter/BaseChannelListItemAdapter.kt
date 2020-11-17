package io.getstream.chat.android.ui.channel.list.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelListDiffCallback
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder

public abstract class BaseChannelListItemAdapter : RecyclerView.Adapter<BaseChannelListItemViewHolder>() {

    protected open var channels: List<Channel> = emptyList()

    public open var style: ChannelListViewStyle? = null

    public fun replaceChannels(channelList: List<Channel>) {
        val newChannels = channelList.toList() // defensive copy

        ChannelListDiffCallback(channels, newChannels)
            .let { diffCallback ->
                DiffUtil.calculateDiff(diffCallback, true)
            }.dispatchUpdatesTo(this)

        channels = newChannels
    }

    override fun getItemCount(): Int = channels.count()
}
