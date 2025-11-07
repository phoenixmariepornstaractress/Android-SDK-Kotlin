package com.example.zarinpal.data.remote.dto.create

import androidx.annotation.Keep
import com.example.zarinpal.data.remote.dto.Config
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Represents the request data required to create a payment.
 */
@Keep
@Serializable
data class CreatePaymentRequest(
    @SerialName("merchant_id")
    val merchantId: String? = null,

    val sandBox: Boolean? = null,
    val description: String,

    @SerialName("callback_url")
    val callbackUrl: String,

    val amount: Int,
    val metadata: Metadata? = null,

    @SerialName("referrer_id")
    val referrerId: String? = null,

    val currency: String? = null,
    val cardPan: String? = null,
    val wages: List<WagesPaymentRequest>? = null,
) {

    /**
     * Returns a copy of this request with merchantId and sandBox populated from [config] if they are null.
     */
    fun copyWithConfig(config: Config): CreatePaymentRequest {
        return copy(
            merchantId = merchantId ?: config.merchantId,
            sandBox = sandBox ?: config.sandBox
        )
    }

    constructor(
        merchantId: String,
        sandBox: Boolean? = null,
        description: String,
        callbackUrl: String,
        amount: Int,
        mobile: String? = null,
        email: String? = null,
        referrerId: String? = null,
        currency: String? = null,
        cardPan: String? = null,
        wages: List<WagesPaymentRequest>? = null
    ) : this(
        merchantId = merchantId,
        sandBox = sandBox,
        description = description,
        callbackUrl = callbackUrl,
        amount = amount,
        metadata = if (!mobile.isNullOrBlank() || !email.isNullOrBlank()) Metadata(mobile, email) else null,
        referrerId = referrerId,
        currency = currency,
        cardPan = cardPan,
        wages = wages
    )

    override fun toString(): String = toJson(pretty = true)
}

@Keep
@Serializable
data class Metadata(
    val mobile: String? = null,
    val email: String? = null
)

@Keep
@Serializable
data class WagesPaymentRequest(
    val iban: String,
    val amount: Int,
    val description: String
)

/* ---------------------------------------------------------------- */
/*                       <<<  helper additions  >>>                 */
/* ---------------------------------------------------------------- */

// CreatePaymentRequest helpers

fun CreatePaymentRequest.isSandboxMode(): Boolean =
    sandBox == true

fun CreatePaymentRequest.addWage(iban: String, amount: Int, description: String): CreatePaymentRequest {
    val newWages = (wages ?: emptyList()) + WagesPaymentRequest(iban, amount, description)
    return copy(wages = newWages)
}

fun CreatePaymentRequest.removeWageByIban(iban: String): CreatePaymentRequest {
    val newWages = wages?.filterNot { it.iban == iban }
    return copy(wages = newWages)
}

fun CreatePaymentRequest.shortInfo(): String =
    "Payment(amount=$amount, desc=$description, merchant=$merchantId)"

fun CreatePaymentRequest.toJson(pretty: Boolean = false): String {
    val json = if (pretty) Json { prettyPrint = true } else Json
    return json.encodeToString(this)
}

fun CreatePaymentRequest.wagesTotal(): Int =
    wages?.sumOf { it.amount } ?: 0

fun CreatePaymentRequest.sumMatches(): Boolean =
    wagesTotal() == amount

fun CreatePaymentRequest.isValidBasic(): Boolean =
    merchantId?.isNotBlank() == true &&
    description.isNotBlank() &&
    callbackUrl.isNotBlank() &&
    amount > 0

fun CreatePaymentRequest.isValidStrict(): Boolean =
    isValidBasic() && sumMatches()

// Metadata helpers

fun Metadata.hasAnyContact(): Boolean =
    !mobile.isNullOrBlank() || !email.isNullOrBlank()

fun Metadata.merge(other: Metadata): Metadata =
    Metadata(
        mobile = other.mobile ?: this.mobile,
        email = other.email ?: this.email
    )

fun Metadata.toSingleLine(): String =
    listOfNotNull(mobile, email).joinToString(" | ")

// WagesPaymentRequest helpers

fun WagesPaymentRequest.isValid(): Boolean =
    iban.isNotBlank() && amount > 0 && description.isNotBlank()

fun WagesPaymentRequest.withAmount(newAmount: Int): WagesPaymentRequest =
    copy(amount = newAmount)

fun WagesPaymentRequest.shortLabel(): String =
    "$amount to $iban"

/* ---------------------------------------------------------------- */
/*                           builder DSL                            */
/* ---------------------------------------------------------------- */

class CreatePaymentBuilder {
    private var merchantId: String? = null
    private var sandbox: Boolean? = null
    private var description: String = ""
    private var callbackUrl: String = ""
    private var amount: Int = 0
    private var metadata: Metadata? = null
    private var referrerId: String? = null
    private var currency: String? = null
    private var cardPan: String? = null
    private val wages = mutableListOf<WagesPaymentRequest>()

    fun merchant(id: String) = apply { merchantId = id }
    fun sandbox(isSandbox: Boolean) = apply { sandbox = isSandbox }
    fun desc(text: String) = apply { description = text }
    fun callback(url: String) = apply { callbackUrl = url }
    fun amount(value: Int) = apply { amount = value }
    fun metadata(mobile: String?, email: String?) = apply { this.metadata = Metadata(mobile, email) }
    fun referrer(id: String?) = apply { referrerId = id }
    fun currency(code: String?) = apply { currency = code }
    fun cardPan(pan: String?) = apply { cardPan = pan }
    fun wage(iban: String, amount: Int, desc: String) = apply {
        wages += WagesPaymentRequest(iban, amount, desc)
    }

    fun build(): CreatePaymentRequest =
        CreatePaymentRequest(
            merchantId = merchantId,
            sandBox = sandbox,
            description = description,
            callbackUrl = callbackUrl,
            amount = amount,
            metadata = metadata,
            referrerId = referrerId,
            currency = currency,
            cardPan = cardPan,
            wages = if (wages.isEmpty()) null else wages.toList()
        )
}

/* ---------------------------------------------------------------- */
/*                          JSON factory                            */
/* ---------------------------------------------------------------- */

fun CreatePaymentRequest.Companion.fromJson(text: String): CreatePaymentRequest =
    Json.decodeFromString(text)
