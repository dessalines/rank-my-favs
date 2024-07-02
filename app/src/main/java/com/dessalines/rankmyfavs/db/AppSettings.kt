package com.dessalines.rankmyfavs.db

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.dessalines.rankmyfavs.utils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DEFAULT_THEME = 0
const val DEFAULT_THEME_COLOR = 0
const val DEFAULT_MIN_CONFIDENCE = 85

@Entity
data class AppSettings(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "theme",
        defaultValue = DEFAULT_THEME.toString(),
    )
    val theme: Int,
    @ColumnInfo(
        name = "theme_color",
        defaultValue = DEFAULT_THEME_COLOR.toString(),
    )
    val themeColor: Int,
    @ColumnInfo(
        name = "last_version_code_viewed",
        defaultValue = "0",
    )
    val lastVersionCodeViewed: Int,
    @ColumnInfo(
        name = "min_confidence",
        defaultValue = DEFAULT_MIN_CONFIDENCE.toString(),
    )
    val minConfidence: Int,
)

data class SettingsUpdate(
    val id: Int,
    @ColumnInfo(
        name = "theme",
    )
    val theme: Int,
    @ColumnInfo(
        name = "theme_color",
    )
    val themeColor: Int,
    @ColumnInfo(
        name = "min_confidence",
        defaultValue = DEFAULT_MIN_CONFIDENCE.toString(),
    )
    val minConfidence: Int,
)

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM AppSettings limit 1")
    fun getSettings(): Flow<AppSettings>

    @Update
    suspend fun updateAppSettings(appSettings: AppSettings)

    @Update(entity = AppSettings::class)
    fun updateSettings(settings: SettingsUpdate)

    @Query("UPDATE AppSettings SET last_version_code_viewed = :versionCode")
    suspend fun updateLastVersionCode(versionCode: Int)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class AppSettingsRepository(
    private val appSettingsDao: AppSettingsDao,
) {
    private val _changelog = MutableStateFlow("")
    val changelog = _changelog.asStateFlow()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val appSettings = appSettingsDao.getSettings()

    @WorkerThread
    suspend fun update(appSettings: AppSettings) {
        appSettingsDao.updateAppSettings(appSettings)
    }

    @WorkerThread
    fun updateSettings(settings: SettingsUpdate) {
        appSettingsDao.updateSettings(settings)
    }

    @WorkerThread
    suspend fun updateLastVersionCodeViewed(versionCode: Int) {
        appSettingsDao.updateLastVersionCode(versionCode)
    }

    @WorkerThread
    suspend fun updateChangelog(ctx: Context) {
        withContext(Dispatchers.IO) {
            try {
                val releasesStr =
                    ctx.assets
                        .open("RELEASES.md")
                        .bufferedReader()
                        .use { it.readText() }
                _changelog.value = releasesStr
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load changelog: $e")
            }
        }
    }
}

class AppSettingsViewModel(
    private val repository: AppSettingsRepository,
) : ViewModel() {
    val appSettings = repository.appSettings
    val changelog = repository.changelog

    fun update(appSettings: AppSettings) =
        viewModelScope.launch {
            repository.update(appSettings)
        }

    fun updateSettings(settings: SettingsUpdate) =
        viewModelScope.launch {
            repository.updateSettings(settings)
        }

    fun updateLastVersionCodeViewed(versionCode: Int) =
        viewModelScope.launch {
            repository.updateLastVersionCodeViewed(versionCode)
        }

    fun updateChangelog(ctx: Context) =
        viewModelScope.launch {
            repository.updateChangelog(ctx)
        }
}

class AppSettingsViewModelFactory(
    private val repository: AppSettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
