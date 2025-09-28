import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.source
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

// Data classes for API response
data class ScanResponse(
    val success: Boolean,
    val result: HairAnalysisResult?,
    val timestamp: String?
)

data class HairAnalysisResult(
    val predicted_hair_type: String,
    val confidence: Float,
    val confidence_percentage: String
)

// Custom RequestBody for URI
class UriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val contentType: String = "image/*"
) : RequestBody() {

    override fun contentType() = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    override fun writeTo(sink: okio.BufferedSink) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            sink.writeAll(inputStream.source())
        }
    }
}

// Retrofit API interface
interface DocumentScannerApi {
    @Multipart
    @POST("classify") // Replace with your actual endpoint
    suspend fun uploadDocument(
        @Part image: MultipartBody.Part
    ): Response<ScanResponse>
}

// API Service class
class DocumentScannerApiService {

    companion object {
        private const val TAG = "DocumentScannerApi"
        private const val BASE_URL = "https://l3mmsdmx-3000.inc1.devtunnels.ms/" // Replace with your actual base URL
        private const val TIMEOUT_SECONDS = 30L
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(DocumentScannerApi::class.java)

    /**
     * Upload document image to the server directly from URI
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @return ScanResponse or null if failed
     */
    suspend fun uploadDocument(
        context: Context,
        imageUri: Uri
    ): ScanResponse? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting document upload for URI: $imageUri")

                // Create RequestBody directly from URI
                val requestBody = UriRequestBody(context, imageUri, "image/*")

                // Get filename from URI or use default
                val fileName = getFileName(context, imageUri) ?: "image.jpg"
                Log.d(TAG, "Upload filename: $fileName")

                // Create MultipartBody.Part
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    fileName,
                    requestBody
                )

                Log.d(TAG, "Making API call...")

                // Make the API call
                val response = api.uploadDocument(imagePart)

                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    Log.d(TAG, "API call successful")

                    // Log response details
                    scanResponse?.let { result ->
                        Log.d(TAG, "Success: ${result.success}")
                        Log.d(TAG, "Message: ${result.timestamp}")
                        result.result?.let { data ->
                            Log.d(TAG, "Predicted Hair Type: ${data.predicted_hair_type}")
                            Log.d(TAG, "confidence percentage: ${data.confidence_percentage}")
                            Log.d(TAG, "Confidence: ${data.confidence}")
                        }
                    }

                    return@withContext scanResponse
                } else {
                    Log.e(TAG, "API call failed with code: ${response.code()}")
                    Log.e(TAG, "Error message: ${response.message()}")
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    return@withContext null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call", e)
                return@withContext null
            }
        }
    }

    /**
     * Get filename from URI
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get filename from URI: ${e.message}")
            null
        }
    }
}