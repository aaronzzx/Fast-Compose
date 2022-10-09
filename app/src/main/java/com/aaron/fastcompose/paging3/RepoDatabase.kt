package com.aaron.fastcompose.paging3

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/27
 */
@Database(
    entities = [Repo::class],
    version = 1,
    exportSchema = false
)
abstract class RepoDatabase : RoomDatabase() {

    companion object {
        private lateinit var instance: RepoDatabase

        @JvmStatic
        fun get(context: Context): RepoDatabase {
            if (::instance.isInitialized.not()) {
                instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        RepoDatabase::class.java, "paging-db"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }

    abstract fun getRepoDao(): RepoDao
}