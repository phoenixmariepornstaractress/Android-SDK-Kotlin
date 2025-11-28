package com.example.zarinpal.data.remote

import com.example.zarinpal.data.remote.dto.Config
import com.example.zarinpal.data.remote.dto.create.CreatePaymentDataResponse
import com.example.zarinpal.data.remote.dto.create.CreatePaymentRequest
import com.example.zarinpal.data.remote.dto.inquiry.PaymentInquiryDataResponse
import com.example.zarinpal.data.remote.dto.inquiry.PaymentInquiryRequest
import com.example.zarinpal.data.remote.dto.refund.PaymentRefundRequest
import com.example.zarinpal.data.remote.dto.refund.PaymentRefundResponse
import com.example.zarinpal.data.remote.dto.reverse.PaymentReverseDataResponse
import com.example.zarinpal.data.remote.dto.reverse.PaymentReverseRequest
import com.example.zarinpal.data.remote.dto.transaction.Session
import com.example.zarinpal.data.remote.dto.transaction.TransactionRequest
import com.example.zarinpal.data.remote.dto.unVerified.PaymentUnVerifiedDataResponse
import com.example.zarinpal.data.remote.dto.unVerified.PaymentUnVerifiedRequest
import com.example.zarinpal.data.remote.dto.verification.PaymentVerificationDataResponse
import com.example.zarinpal.data.remote.dto.verification.PaymentVerifyRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Defines operations available for interacting with ZarinPal API.
 */
interface PaymentService {

    suspend fun createPayment(request: CreatePaymentRequest): CreatePaymentDataResponse?
    suspend fun paymentVerify(request: PaymentVerifyRequest): PaymentVerificationDataResponse?
    suspend fun paymentInquiry(request: PaymentInquiryRequest): PaymentInquiryDataResponse?
    suspend fun paymentUnVerified(request: PaymentUnVerifiedRequest): PaymentUnVerifiedDataResponse?
    suspend fun paymentReverse(request: PaymentReverseRequest): PaymentReverseDataResponse?
    suspend fun getTransactions(request: TransactionRequest): List<Session>?
    suspend fun paymentRefund(request: PaymentRefundRequest): PaymentRefundResponse?

    companion object {

        fun create(config: Config): PaymentService {

            val client = HttpClient(Android) {

                install(Logging) {
                    level = LogLevel.ALL
                }

                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = false
                        }
                    )
                }

                defaultRequest {
                    header("User-Agent", "ZarinPalSdk/v1.0.1 (android-kotlin)")
                    contentType(ContentType.Application.Json)
                }
            }

            return PaymentServiceImpl(
                config = config,
                client = client
            )
        }
    }
}
