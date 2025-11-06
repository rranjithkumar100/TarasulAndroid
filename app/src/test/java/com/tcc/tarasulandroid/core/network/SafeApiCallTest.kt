package com.tcc.tarasulandroid.core.network

import com.tcc.tarasulandroid.core.network.utils.ApiError
import com.tcc.tarasulandroid.core.network.utils.Result
import com.tcc.tarasulandroid.core.network.utils.safeApiCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.io.IOException

@ExperimentalCoroutinesApi
class SafeApiCallTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var testApi: TestApi
    private val testDispatcher = StandardTestDispatcher()

    interface TestApi {
        @GET("/")
        suspend fun test(): String
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        testApi = retrofit.create(TestApi::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `safeApiCall returns Success when api call is successful`() = runTest(testDispatcher) {
        mockWebServer.enqueue(MockResponse().setBody("\"Success\""))

        val result = safeApiCall(testDispatcher) { testApi.test() }

        assertTrue(result is Result.Success)
        assertEquals("Success", (result as Result.Success).data)
    }

    @Test
    fun `safeApiCall returns Error with HttpException details when api call fails`() = runTest(testDispatcher) {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val result = safeApiCall(testDispatcher) { testApi.test() }

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertEquals(404, error.statusCode)
        assertEquals("Not Found", error.message)
    }

    @Test
    fun `safeApiCall returns Error with IOException message when network error occurs`() = runTest(testDispatcher) {
        // To simulate an IOException, we can shut down the server before the call
        mockWebServer.shutdown()

        val result = safeApiCall(testDispatcher) { testApi.test() }

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertEquals("Network Error", error.message)
    }
}
