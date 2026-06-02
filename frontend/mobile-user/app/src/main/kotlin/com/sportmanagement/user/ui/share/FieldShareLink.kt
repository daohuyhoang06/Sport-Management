package com.sportmanagement.user.ui.share

import android.net.Uri
import com.sportmanagement.user.BuildConfig

object FieldShareLink {
    data class MomoPaymentReturn(
        val orderId: String?,
        val requestId: String?,
        val resultCode: Int?,
        val message: String?
    )

    fun webFieldLink(fieldId: Int): String {
        val base = BuildConfig.SHARE_WEB_BASE_URL.trimEnd('/')
        return "$base/field/$fieldId"
    }

    fun appFieldLink(fieldId: Int): String {
        val scheme = BuildConfig.DEEP_LINK_SCHEME
        return "$scheme://field/$fieldId"
    }

    fun momoReturnLink(): String {
        val scheme = BuildConfig.DEEP_LINK_SCHEME
        return "$scheme://payment/momo"
    }

    fun parseFieldId(uri: Uri?): Int? {
        if (uri == null) return null

        val customScheme = BuildConfig.DEEP_LINK_SCHEME
        if (uri.scheme.equals(customScheme, ignoreCase = true) &&
            uri.host.equals("field", ignoreCase = true)
        ) {
            return uri.pathSegments.firstOrNull()?.toIntOrNull()
        }

        if ((uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
            uri.pathSegments.size >= 2 &&
            uri.pathSegments[0].equals("field", ignoreCase = true)
        ) {
            return uri.pathSegments[1].toIntOrNull()
        }

        if ((uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
            uri.pathSegments.size >= 3 &&
            uri.pathSegments[0].equals("l", ignoreCase = true) &&
            uri.pathSegments[1].equals("field", ignoreCase = true)
        ) {
            return uri.pathSegments[2].toIntOrNull()
        }

        return null
    }

    fun parseMomoPaymentReturn(uri: Uri?): MomoPaymentReturn? {
        if (uri == null) return null
        val customScheme = BuildConfig.DEEP_LINK_SCHEME
        if (!uri.scheme.equals(customScheme, ignoreCase = true)) {
            return null
        }
        if (!uri.host.equals("payment", ignoreCase = true)) {
            return null
        }
        if (uri.pathSegments.firstOrNull()?.equals("momo", ignoreCase = true) != true) {
            return null
        }

        return MomoPaymentReturn(
            orderId = uri.getQueryParameter("orderId") ?: uri.getQueryParameter("order_id"),
            requestId = uri.getQueryParameter("requestId") ?: uri.getQueryParameter("request_id"),
            resultCode = uri.getQueryParameter("resultCode")?.toIntOrNull()
                ?: uri.getQueryParameter("result_code")?.toIntOrNull(),
            message = uri.getQueryParameter("message")
                ?: uri.getQueryParameter("momoMessage")
        )
    }
}
