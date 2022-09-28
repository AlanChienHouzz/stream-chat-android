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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.utils.Result

internal class ThreadQueryListenerComposite(
    private val threadQueryListenerList: List<ThreadQueryListener>,
) : ThreadQueryListener {
    override suspend fun onGetRepliesRequest(messageId: String, limit: Int) {
        threadQueryListenerList.forEach { threadQueryListener ->
            threadQueryListener.onGetRepliesRequest(messageId, limit)
        }
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) {
        threadQueryListenerList.forEach { threadQueryListener ->
            threadQueryListener.onGetRepliesResult(result, messageId, limit)
        }
    }

    override suspend fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int) {
        threadQueryListenerList.forEach { threadQueryListener ->
            threadQueryListener.onGetRepliesMoreRequest(messageId, firstId, limit)
        }
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ) {
        threadQueryListenerList.forEach { threadQueryListener ->
            threadQueryListener.onGetRepliesMoreResult(result, messageId, firstId, limit)
        }
    }

    override suspend fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> {
        return threadQueryListenerList.map { threadQueryListener ->
            threadQueryListener.onGetRepliesPrecondition(messageId, limit)
        }.foldResults()
    }

    override suspend fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> {
        return threadQueryListenerList.map { threadQueryListener ->
            threadQueryListener.onGetRepliesMorePrecondition(messageId, firstId, limit)
        }.foldResults()
    }
}
