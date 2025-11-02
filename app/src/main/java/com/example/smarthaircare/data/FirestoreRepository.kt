package com.example.smarthaircare.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Data class for storing scan results
data class ScanResult(
    val userId: String = "",
    val hairType: String = "",
    val confidence: String = "",
    val dandruffLevel: String = "",
    val hairLossStage: String = "",
    val recommendedOils: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val scanId: String = UUID.randomUUID().toString()
) {
    // Convert timestamp to readable format
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Convert to map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "hairType" to hairType,
            "confidence" to confidence,
            "dandruffLevel" to dandruffLevel,
            "hairLossStage" to hairLossStage,
            "recommendedOils" to recommendedOils,
            "timestamp" to timestamp,
            "scanId" to scanId
        )
    }
}

// Repository class for Firestore operations
class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FirestoreRepository"
        private const val COLLECTION_SCAN_RESULTS = "scanResults"
    }

    /**
     * Save scan result to Firestore
     * @return true if successful, false otherwise
     */
    suspend fun saveScanResult(
        hairType: String,
        confidence: String,
        dandruffLevel: String,
        hairLossStage: String,
        recommendedOils: List<String>
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return false
            }

            val scanResult = ScanResult(
                userId = currentUser.uid,
                hairType = hairType,
                confidence = confidence,
                dandruffLevel = dandruffLevel,
                hairLossStage = hairLossStage,
                recommendedOils = recommendedOils,
                timestamp = System.currentTimeMillis()
            )

            // Save to Firestore
            firestore.collection(COLLECTION_SCAN_RESULTS)
                .document(scanResult.scanId)
                .set(scanResult.toMap())
                .await()

            Log.d(TAG, "Scan result saved successfully with ID: ${scanResult.scanId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving scan result", e)
            false
        }
    }

    /**
     * Get all scan results for current user, sorted by timestamp (newest first)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserScanHistory(): List<ScanResult> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return emptyList()
            }

            val snapshot = firestore.collection(COLLECTION_SCAN_RESULTS)
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    ScanResult(
                        userId = doc.getString("userId") ?: "",
                        hairType = doc.getString("hairType") ?: "",
                        confidence = doc.getString("confidence") ?: "",
                        dandruffLevel = doc.getString("dandruffLevel") ?: "",
                        hairLossStage = doc.getString("hairLossStage") ?: "",
                        recommendedOils = doc.get("recommendedOils") as? List<String> ?: emptyList(),
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        scanId = doc.getString("scanId") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user scan history", e)
            emptyList()
        }
    }

    /**
     * Get the most recent scan result for current user
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLatestScanResult(): ScanResult? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return null
            }

            val snapshot = firestore.collection(COLLECTION_SCAN_RESULTS)
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.let { doc ->
                ScanResult(
                    userId = doc.getString("userId") ?: "",
                    hairType = doc.getString("hairType") ?: "",
                    confidence = doc.getString("confidence") ?: "",
                    dandruffLevel = doc.getString("dandruffLevel") ?: "",
                    hairLossStage = doc.getString("hairLossStage") ?: "",
                    recommendedOils = doc.get("recommendedOils") as? List<String> ?: emptyList(),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    scanId = doc.getString("scanId") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching latest scan result", e)
            null
        }
    }

    /**
     * Delete a specific scan result
     */
    suspend fun deleteScanResult(scanId: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return false
            }

            // Verify the scan belongs to the current user before deleting
            val doc = firestore.collection(COLLECTION_SCAN_RESULTS)
                .document(scanId)
                .get()
                .await()

            if (doc.getString("userId") == currentUser.uid) {
                firestore.collection(COLLECTION_SCAN_RESULTS)
                    .document(scanId)
                    .delete()
                    .await()
                Log.d(TAG, "Scan result deleted successfully")
                true
            } else {
                Log.e(TAG, "Unauthorized: User does not own this scan result")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting scan result", e)
            false
        }
    }

    /**
     * Get total number of scans for current user
     */
    suspend fun getUserScanCount(): Int {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return 0
            }

            val snapshot = firestore.collection(COLLECTION_SCAN_RESULTS)
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting scan count", e)
            0
        }
    }
}