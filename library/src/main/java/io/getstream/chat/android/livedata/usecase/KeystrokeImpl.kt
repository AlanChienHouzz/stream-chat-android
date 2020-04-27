package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import java.security.InvalidParameterException

interface Keystroke {
    operator fun invoke(cid: String): Call2<Boolean>
}

class KeystrokeImpl(var domainImpl: ChatDomainImpl) : Keystroke {
    override operator fun invoke(cid: String): Call2<Boolean> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }
        val channelController = domainImpl.channel(cid)

        var runnable = suspend {
            channelController.keystroke()
        }
        return CallImpl2<Boolean>(runnable, channelController.scope)
    }
}
