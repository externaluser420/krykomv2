package com.veil.shared.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val ciphertext: ByteArray,
    val sentAtEpochMs: Long,
    val status: String,
    val localPlaintext: String?,
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val participantIds: String,
    val title: String?,
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val identityId: String,
    val deviceId: String,
    val displayName: String?,
    val verified: Boolean,
)

@Entity(tableName = "blocked_identities")
data class BlockedEntity(
    @PrimaryKey val identityId: String,
)

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY sentAtEpochMs ASC")
    suspend fun getByConversation(conversationId: String): List<MessageEntity>

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(messageId: String, status: String)
}

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<ConversationEntity>
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Query("SELECT * FROM contacts")
    suspend fun getAll(): List<ContactEntity>
}

@Dao
interface BlockedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blocked: BlockedEntity)

    @Query("SELECT identityId FROM blocked_identities")
    suspend fun getAll(): List<String>
}

@Database(
    entities = [
        MessageEntity::class,
        ConversationEntity::class,
        ContactEntity::class,
        BlockedEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class VeilDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    abstract fun conversationDao(): ConversationDao

    abstract fun contactDao(): ContactDao

    abstract fun blockedDao(): BlockedDao
}
