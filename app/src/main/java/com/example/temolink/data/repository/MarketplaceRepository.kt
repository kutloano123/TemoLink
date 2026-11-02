package com.example.temolink.data.repository

import com.example.temolink.data.model.Produce
import com.example.temolink.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MarketplaceRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Real-time flow for marketplace produce
    fun observeAllProduce(): Flow<List<Produce>> {
        return firestore.collection("produce")
            .whereEqualTo("availability", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Produce::class.java)
            }
    }

    suspend fun getAllProduce(): List<Produce> {
        return try {
            firestore.collection("produce")
                .whereEqualTo("availability", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Produce::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProduceById(produceId: String): Produce? {
        return try {
            firestore.collection("produce").document(produceId).get().await().toObject(Produce::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProduceByFarmer(farmerId: String): List<Produce> {
        return try {
            firestore.collection("produce")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .await()
                .toObjects(Produce::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFarmerPublicProfile(farmerId: String): User? {
        return try {
            firestore.collection("users").document(farmerId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun addProduceListing(produce: Produce): Result<Unit> {
        return try {
            val docRef = firestore.collection("produce").document()
            val finalProduce = produce.copy(produceId = docRef.id)
            docRef.set(finalProduce).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduceListing(produce: Produce): Result<Unit> {
        return try {
            firestore.collection("produce").document(produce.produceId).set(produce).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduceListing(produceId: String): Result<Unit> {
        return try {
            firestore.collection("produce").document(produceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
