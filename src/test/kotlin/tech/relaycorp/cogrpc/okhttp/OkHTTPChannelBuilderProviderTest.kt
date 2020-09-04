package tech.relaycorp.cogrpc.okhttp

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.grpc.okhttp.OkHttpChannelBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tech.relaycorp.relaynet.cogrpc.client.PrivateSubnetTrustManager
import java.net.InetSocketAddress
import java.security.SecureRandom
import javax.net.ssl.SSLContext

class OkHTTPChannelBuilderProviderTest {
    private val address = InetSocketAddress("example.com", 8080)

    @Test
    fun `SSL Context should use the TLS protocol`() {
        val context = OkHTTPChannelBuilderProvider().makeSSLContext()

        assertEquals(context.protocol, "TLS")
    }

    @Test
    fun `Address should be honored`() {
        val builder = OkHTTPChannelBuilderProvider.makeBuilder(address, null)

        assertEquals("${address.hostName}:${address.port}", builder.build().authority())
    }

    @Test
    fun `No custom SSL socket factory should be set if no trust manager was passed`() {
        val spiedProvider = spy(OkHTTPChannelBuilderProvider())

        spiedProvider.makeBuilder(address, null)

        verify(spiedProvider, never()).makeSSLContext()
    }

    @Test
    fun `Custom trust manager should be honored`() {
        val spiedProvider = spy(OkHTTPChannelBuilderProvider())
        val spiedSSLContext = spy(SSLContext.getInstance("TLS"))
        whenever(spiedProvider.makeSSLContext()).thenReturn(spiedSSLContext)
        val spiedBuilder = spy(OkHttpChannelBuilder.forAddress(address.hostName, address.port))
        whenever(spiedProvider.initChannelBuilder(address)).thenReturn(spiedBuilder)

        spiedProvider.makeBuilder(address, PrivateSubnetTrustManager.INSTANCE)

        verify(spiedProvider).makeSSLContext()
        verify(spiedSSLContext)
            .init(eq(null), eq(arrayOf(PrivateSubnetTrustManager.INSTANCE)), isA<SecureRandom>())
        verify(spiedSSLContext).socketFactory
        verify(spiedBuilder).sslSocketFactory(any())
    }
}
