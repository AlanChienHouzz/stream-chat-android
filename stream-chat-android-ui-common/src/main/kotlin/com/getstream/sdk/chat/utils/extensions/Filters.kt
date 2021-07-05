package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun Filters.defaultChannelListFilter(user: User?): FilterObject {
    return if (user == null) {
        Filters.and(
            Filters.eq("type", "messaging"),
            Filters.neutral(),
            Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),
        )
    } else {
        Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(user.id)),
            Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),
        )
    }
}
