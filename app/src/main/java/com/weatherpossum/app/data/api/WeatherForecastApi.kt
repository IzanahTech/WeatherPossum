import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

interface WeatherForecastApi {
    @GET("forecast.php")
    suspend fun getWeatherForecast(): String

    companion object {
        private const val MAX_RETRIES = 3

        private class RetryInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                var retryCount = 0
                var response: Response? = null
                var exception: IOException? = null

                while (retryCount < MAX_RETRIES) {
                    try {
                        response = chain.proceed(chain.request())
                        if (response.isSuccessful) {
                            return response
                        } else {
                            response.close()
                        }
                    } catch (e: IOException) {
                        exception = e
                    }
                    retryCount++
                    if (retryCount < MAX_RETRIES) {
                        Thread.sleep(2000L * retryCount) // Exponential backoff
                    }
                }

                // If we got here, all retries failed
                response?.let { return it }
                throw exception ?: IOException("Request failed after $MAX_RETRIES retries")
            }
        }

        fun create(): WeatherForecastApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(RetryInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://weather.gov.dm/")
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(WeatherForecastApi::class.java)
        }
    }
} 