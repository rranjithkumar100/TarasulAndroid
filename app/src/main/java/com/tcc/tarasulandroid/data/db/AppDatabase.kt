package com.tcc.tarasulandroid.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        MediaEntity::class
    ],
    version = 4,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun contactsDao(): ContactsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun mediaDao(): MediaDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create media table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `media` (
                        `mediaId` TEXT NOT NULL PRIMARY KEY,
                        `messageId` TEXT NOT NULL,
                        `serverUrl` TEXT,
                        `localPath` TEXT,
                        `thumbnailPath` TEXT,
                        `fileName` TEXT,
                        `mimeType` TEXT,
                        `fileSize` INTEGER,
                        `checksum` TEXT,
                        `width` INTEGER,
                        `height` INTEGER,
                        `durationMs` INTEGER,
                        `storedInAppFiles` INTEGER NOT NULL DEFAULT 1,
                        `downloadStatus` TEXT NOT NULL,
                        `downloadProgress` INTEGER NOT NULL DEFAULT 0,
                        `downloadedAt` INTEGER
                    )
                """.trimIndent())
                
                // Create indices for media table
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_messageId` ON `media` (`messageId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_downloadStatus` ON `media` (`downloadStatus`)")
                
                // Add new columns to messages table
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'TEXT'")
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `mediaId` TEXT")
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `deliveredAt` INTEGER")
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `readAt` INTEGER")
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'PENDING'")
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `direction` TEXT NOT NULL DEFAULT 'OUTGOING'")
                
                // Create indices for messages table
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId_timestamp` ON `messages` (`conversationId`, `timestamp`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_status` ON `messages` (`status`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_mediaId` ON `messages` (`mediaId`)")
                
                // Migrate existing data to use new status/direction fields
                database.execSQL("""
                    UPDATE messages SET 
                        status = CASE 
                            WHEN isRead = 1 THEN 'READ'
                            WHEN isDelivered = 1 THEN 'DELIVERED'
                            WHEN isSent = 1 THEN 'SENT'
                            ELSE 'PENDING'
                        END,
                        direction = CASE WHEN isMine = 1 THEN 'OUTGOING' ELSE 'INCOMING' END
                """.trimIndent())
            }
        }
        
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add replyToMessageId column to messages table
                database.execSQL("ALTER TABLE `messages` ADD COLUMN `replyToMessageId` TEXT")
                
                // Create index for replies
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_replyToMessageId` ON `messages` (`replyToMessageId`)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarasul_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
