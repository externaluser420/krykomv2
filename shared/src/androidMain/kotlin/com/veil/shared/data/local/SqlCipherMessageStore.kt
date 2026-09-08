package com.veil.shared.data.local

import android.content.Context
import androidx.room.Room
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.model.Conversation
import com.veil.shared.domain.model.ConversationId
import com.veil.shared.domain.model.ConversationType
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.Message
import com.veil.shared.domain.model.MessageStatus
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

class SqlCipherMessageStore(
    private val context: Context,
    private val secureKeyStore: SecureKeyStore,
) : MessageStore {
    @Volatile
    private var database: VeilDatabase? = null
    private val dbMutex = Mutex()

    private suspend fun db(): VeilDatabase {
        val existing = database
        if (existing != null) return existing
        return dbMutex.withLock {
            database ?: buildDatabase().also { database = it }
        }
    }

    private suspend fun buildDatabase(): VeilDatabase {
        val passphrase = secureKeyStore.getDatabasePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(context.applicationContext, VeilDatabase::class.java, "veil_encrypted.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    override suspend fun saveMessage(message: Message) =
        withContext(Dispatchers.IO) {
            db().messageDao().insert(message.toEntity())
        }

    override suspend fun getMessages(conversationId: ConversationId): List<Message> =
        withContext(Dispatchers.IO) {
            db().messageDao().getByConversation(conversationId.value).map { it.toDomain() }
        }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) =
        withContext(Dispatchers.IO) {
            db().messageDao().updateStatus(messageId, status.name)
        }

    override suspend fun saveConversation(conversation: Conversation) =
        withContext(Dispatchers.IO) {
            db().conversationDao().insert(conversation.toEntity())
        }

    override suspend fun getConversations(): List<Conversation> =
        withContext(Dispatchers.IO) {
            db().conversationDao().getAll().map { it.toDomain() }
        }

    override suspend fun saveContact(contact: Contact) =
        withContext(Dispatchers.IO) {
            db().contactDao().insert(contact.toEntity())
        }

    override suspend fun getContacts(): List<Contact> =
        withContext(Dispatchers.IO) {
            db().contactDao().getAll().map { it.toDomain() }
        }

    override suspend fun getBlockedIdentities(): List<IdentityId> =
        withContext(Dispatchers.IO) {
            db().blockedDao().getAll().map { IdentityId(it) }
        }

    override suspend fun clearAllData(): Result<Unit> =
        withContext(Dispatchers.IO) {
            dbMutex.withLock {
                try {
                    database?.close()
                    database = null
                    context.applicationContext.deleteDatabase("veil_encrypted.db")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
}

private fun Message.toEntity() =
    MessageEntity(
        id = id,
        conversationId = conversationId.value,
        senderId = senderId.value,
        ciphertext = ciphertext,
        sentAtEpochMs = sentAtEpochMs,
        status = status.name,
        localPlaintext = localPlaintext,
    )

private fun MessageEntity.toDomain() =
    Message(
        id = id,
        conversationId = ConversationId(conversationId),
        senderId = IdentityId(senderId),
        ciphertext = ciphertext,
        sentAtEpochMs = sentAtEpochMs,
        status = MessageStatus.valueOf(status),
        localPlaintext = localPlaintext,
    )

private fun Conversation.toEntity() =
    ConversationEntity(
        id = id.value,
        type = type.name,
        participantIds = participantIds.joinToString(",") { it.value },
        title = title,
    )

private fun ConversationEntity.toDomain() =
    Conversation(
        id = ConversationId(id),
        type = ConversationType.valueOf(type),
        participantIds = participantIds.split(",").filter { it.isNotBlank() }.map { IdentityId(it) },
        title = title,
    )

private fun Contact.toEntity() =
    ContactEntity(
        identityId = identityId.value,
        deviceId = deviceId.value,
        displayName = displayName,
        verified = verified,
    )

private fun ContactEntity.toDomain() =
    Contact(
        identityId = IdentityId(identityId),
        deviceId = DeviceId(deviceId),
        displayName = displayName,
        verified = verified,
    )
