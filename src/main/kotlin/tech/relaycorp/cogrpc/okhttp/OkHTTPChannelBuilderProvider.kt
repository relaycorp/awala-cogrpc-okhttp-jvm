package tech.relaycorp.cogrpc.okhttp

import io.grpc.okhttp.OkHttpChannelBuilder
import tech.relaycorp.relaynet.cogrpc.client.PrivateSubnetTrustManager
import java.net.InetSocketAddress
import java.security.SecureRandom
import javax.net.ssl.SSLContext

class OkHTTPChannelBuilderProvider internal constructor() {
    internal fun makeSSLContext() = SSLContext.getInstance("TLS")

    internal fun initChannelBuilder(address: InetSocketAddress) =
        OkHttpChannelBuilder.forAddress(address.hostName, address.port)

    internal fun makeBuilder(
        address: InetSocketAddress,
        trustManager: PrivateSubnetTrustManager?
    ) = initChannelBuilder(address).let {
        if (trustManager != null) {
            val sslContext = makeSSLContext()
            sslContext.init(null, arrayOf(trustManager), SecureRandom())
            it.sslSocketFactory(sslContext.socketFactory)
            it
        } else {
            it
        }
    }

    companion object {
        private val INSTANCE = OkHTTPChannelBuilderProvider()

        fun makeBuilder(
            address: InetSocketAddress,
            trustManager: PrivateSubnetTrustManager?
        ): OkHttpChannelBuilder = INSTANCE.makeBuilder(address, trustManager)
    }
}
