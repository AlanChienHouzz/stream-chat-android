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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewContract
import io.getstream.chat.android.compose.ui.components.MediaPreviewPlaceHolder
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.RetryHash
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.uiutils.constant.AttachmentType
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * Displays a preview of single or multiple video or attachments.
 *
 * @param attachmentState The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param modifier The modifier used for styling.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param itemOverlayContent Represents the content overlaid above individual items.
 * By default it is used to display a play button over video previews.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun MediaAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    maximumNumberOfPreviewedItems: Int = 4,
    itemOverlayContent: @Composable (attachmentType: String?) -> Unit = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            PlayButton()
        }
    },
) {
    val (message, onLongItemClick, onMediaGalleryPreviewResult) = attachmentState
    val gridSpacing = ChatTheme.dimens.attachmentsContentMediaGridSpacing

    Row(
        modifier
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { onLongItemClick(message) }
            ),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        val attachments =
            message.attachments.filter {
                !it.hasLink() && (it.type == AttachmentType.IMAGE || it.type == AttachmentType.VIDEO)
            }
        val attachmentCount = attachments.size

        if (attachmentCount == 1) {
            val attachment = attachments.first()

            SingleMediaAttachment(
                attachment = attachment,
                message = message,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onLongItemClick = onLongItemClick,
                overlayContent = itemOverlayContent
            )
        } else {
            MultipleMediaAttachments(
                attachments = attachments,
                attachmentCount = attachmentCount,
                gridSpacing = gridSpacing,
                maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                message = message,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onLongItemClick = onLongItemClick,
                itemOverlayContent = itemOverlayContent
            )
        }
    }
}

/**
 * Displays a preview of a single image or video attachment.
 *
 * @param attachment The attachment that is previewed.
 * @param message The original message containing the attachment.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when an item is long clicked.
 * @param overlayContent Represents the content overlaid above attachment previews.
 * Usually used to display a play button over video previews.
 */
@Composable
internal fun SingleMediaAttachment(
    attachment: Attachment,
    message: Message,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onLongItemClick: (Message) -> Unit,
    overlayContent: @Composable (attachmentType: String?) -> Unit,
) {
    val isVideo = attachment.type == AttachmentType.VIDEO
    // Depending on the CDN, images might not contain their original dimensions
    val ratio: Float? by remember(key1 = attachment.originalWidth, key2 = attachment.originalHeight) {
        derivedStateOf {
            val width = attachment.originalWidth?.toFloat()
            val height = attachment.originalHeight?.toFloat()

            if (width != null && height != null) {
                width / height
            } else {
                null
            }
        }
    }

    MediaAttachmentContentItem(
        attachment = attachment,
        modifier = Modifier
            .heightIn(
                max = if (isVideo) {
                    ChatTheme.dimens.attachmentsContentVideoMaxHeight
                } else {
                    ChatTheme.dimens.attachmentsContentImageMaxHeight
                }
            )
            .width(
                if (isVideo) {
                    ChatTheme.dimens.attachmentsContentVideoWidth
                } else {
                    ChatTheme.dimens.attachmentsContentImageWidth
                }
            )
            .aspectRatio(ratio ?: EqualDimensionsRatio),
        message = message,
        attachmentPosition = 0,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onLongItemClick = onLongItemClick,
        overlayContent = overlayContent
    )
}

/**
 * Displays previews of multiple image and video attachment laid out in a grid.
 *
 * @param attachments The list of attachments that are to be previewed.
 * @param attachmentCount The number of attachments that are to be previewed.
 * @param gridSpacing Determines the spacing strategy between items.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param message The original message containing the attachments.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when an item is long clicked.
 * @param itemOverlayContent Represents the content overlaid above individual items.
 * Usually used to display a play button over video previews.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun RowScope.MultipleMediaAttachments(
    attachments: List<Attachment>,
    attachmentCount: Int,
    gridSpacing: Dp,
    maximumNumberOfPreviewedItems: Int = 4,
    message: Message,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onLongItemClick: (Message) -> Unit,
    itemOverlayContent: @Composable (attachmentType: String?) -> Unit,
) {

    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .width(ChatTheme.dimens.attachmentsContentGroupPreviewWidth / 2)
            .height(ChatTheme.dimens.attachmentsContentGroupPreviewHeight),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        for (attachmentIndex in 0 until maximumNumberOfPreviewedItems step 2) {
            if (attachmentIndex < attachmentCount) {
                MediaAttachmentContentItem(
                    attachment = attachments[attachmentIndex],
                    modifier = Modifier.weight(1f),
                    message = message,
                    attachmentPosition = attachmentIndex,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onLongItemClick = onLongItemClick,
                    overlayContent = itemOverlayContent
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .width(ChatTheme.dimens.attachmentsContentGroupPreviewWidth / 2)
            .height(ChatTheme.dimens.attachmentsContentGroupPreviewHeight),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        for (attachmentIndex in 1 until maximumNumberOfPreviewedItems step 2) {
            if (attachmentIndex < attachmentCount) {
                val attachment = attachments[attachmentIndex]
                val isUploading = attachment.uploadState is Attachment.UploadState.InProgress
                val lastItemInColumnIndex = (maximumNumberOfPreviewedItems - 1) - (maximumNumberOfPreviewedItems % 2)

                if (attachmentIndex == lastItemInColumnIndex && attachmentCount > maximumNumberOfPreviewedItems) {
                    Box(modifier = Modifier.weight(1f)) {
                        MediaAttachmentContentItem(
                            attachment = attachment,
                            message = message,
                            attachmentPosition = attachmentIndex,
                            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                            onLongItemClick = onLongItemClick,
                            overlayContent = itemOverlayContent
                        )

                        if (!isUploading) {
                            MediaAttachmentViewMoreOverlay(
                                mediaCount = attachmentCount,
                                maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    MediaAttachmentContentItem(
                        attachment = attachment,
                        modifier = Modifier.weight(1f),
                        message = message,
                        attachmentPosition = attachmentIndex,
                        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                        onLongItemClick = onLongItemClick,
                        overlayContent = itemOverlayContent
                    )
                }
            }
        }
    }
}

/**
 * Displays previews of image and video attachments.
 *
 * @param message The original message containing the attachments.
 * @param attachmentPosition The position of the attachment in the list
 * of attachments. Used to remember the item position when viewing it in a separate
 * activity.
 * @param attachment The attachment that is previewed.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when the item is long clicked.
 * @param modifier Modifier used for styling.
 * @param overlayContent Represents the content overlaid above attachment previews.
 * Usually used to display a play button over video previews.
 */
@Suppress("LongParameterList", "LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MediaAttachmentContentItem(
    message: Message,
    attachmentPosition: Int,
    attachment: Attachment,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    overlayContent: @Composable (attachmentType: String?) -> Unit,
) {
    val connectionState by ChatClient.instance().clientState.connectionState.collectAsState()
    val isImage = attachment.type == AttachmentType.IMAGE
    val isVideo = attachment.type == AttachmentType.VIDEO

    // Used as a workaround for Coil's lack of a retry policy.
    // See: https://github.com/coil-kt/coil/issues/884#issuecomment-975932886
    var retryHash by remember {
        mutableStateOf(0)
    }

    val data =
        if (isImage || (isVideo && ChatTheme.videoThumbnailsEnabled)) {
            attachment.imagePreviewUrl
        } else {
            null
        }

    val painter = rememberStreamImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .setParameter(key = RetryHash, value = retryHash)
            .build()
    )

    val mixedMediaPreviewLauncher = rememberLauncherForActivityResult(
        contract = MediaGalleryPreviewContract(),
        onResult = { result -> onMediaGalleryPreviewResult(result) }
    )

    // Used to refresh the request for the current page
    // if it has previously failed.
    if (data != null && connectionState == ConnectionState.CONNECTED &&
        painter.state is AsyncImagePainter.State.Error
    ) {
        retryHash++
    }

    val areVideosEnabled = ChatTheme.videoThumbnailsEnabled

    Box(
        modifier = modifier
            .background(Color.Black)
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = MutableInteractionSource(),
                indication = rememberRipple(),
                onClick = {
                    mixedMediaPreviewLauncher.launch(
                        MediaGalleryPreviewContract.Input(
                            message = message,
                            initialPosition = attachmentPosition,
                            videoThumbnailsEnabled = areVideosEnabled
                        )
                    )
                },
                onLongClick = { onLongItemClick(message) }
            ),
        contentAlignment = Alignment.Center
    ) {
        val backgroundColor =
            if (isImage) ChatTheme.colors.imageBackgroundMessageList
            else ChatTheme.colors.videoBackgroundMessageList

        Image(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        MediaPreviewPlaceHolder(
            asyncImagePainterState = painter.state,
            progressIndicatorStrokeWidth = 3.dp,
            progressIndicatorFillMaxSizePercentage = 0.25f,
            isImage = isImage,
            placeholderIconTintColor = ChatTheme.colors.disabled
        )

        if (painter.state !is AsyncImagePainter.State.Loading) {
            overlayContent(attachment.type)
        }
    }
}

/**
 * A simple play button that is overlaid above
 * video attachments.
 *
 * @param modifier The modifier used for styling.
 * @param contentDescription Used to describe the content represented by this composable.
 */
@Suppress("MagicNumber")
@Composable
internal fun PlayButton(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .alignBy { measured ->
                    // emulated offset as seen in the design specs,
                    // otherwise the button is visibly off to the start of the screen
                    -(measured.measuredWidth * 1 / 6)
                },
            painter = painterResource(id = R.drawable.stream_compose_ic_play),
            contentDescription = contentDescription,
        )
    }
}

/**
 * Represents an overlay that's shown on the last media attachment preview in the media attachment
 * item gallery.
 *
 * @param mediaCount The number of total media attachments.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun MediaAttachmentViewMoreOverlay(
    mediaCount: Int,
    maximumNumberOfPreviewedItems: Int,
    modifier: Modifier = Modifier,
) {
    val remainingMediaCount = mediaCount - maximumNumberOfPreviewedItems

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChatTheme.colors.overlay),
    ) {
        Text(
            modifier = modifier
                .wrapContentSize(),
            text = stringResource(
                id = R.string.stream_compose_remaining_media_attachments_count,
                remainingMediaCount
            ),
            color = ChatTheme.colors.barsBackground,
            style = ChatTheme.typography.title1,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Produces the same height as the width of the
 * Composable when calling [Modifier.aspectRatio].
 */
private const val EqualDimensionsRatio = 1f
