package com.dessalines.rankmyfavs.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(
    indices = [Index(value = ["name"], unique = true)],
)
data class FavList(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "description",
    )
    val description: String?,
)

data class FavListUpdate(
    val id: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "description",
    )
    val description: String?,
)

@Dao
interface FavListDao {
    @Query("SELECT * FROM FavList")
    fun getAll(): Flow<FavList>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favList: FavList)

    @Update
    suspend fun update(favList: FavList)

    @Delete
    suspend fun delete(favList: FavList)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class FavListRepository(
    private val favListDao: FavListDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val getAll = favListDao.getAll()

    @WorkerThread
    suspend fun insert(favList: FavList) = favListDao.insert(favList)

    @WorkerThread
    suspend fun update(favList: FavList) = favListDao.update(favList)

    @WorkerThread
    suspend fun delete(favList: FavList) = favListDao.delete(favList)
}

class FavListViewModel(
    private val repository: FavListRepository,
) : ViewModel() {
    val favList = repository.getAll

    fun insert(favList: FavList) =
        viewModelScope.launch {
            repository.insert(favList)
        }

    fun update(favList: FavList) =
        viewModelScope.launch {
            repository.update(favList)
        }

    fun delete(favList: FavList) =
        viewModelScope.launch {
            repository.delete(favList)
        }
}

class FavListViewModelFactory(
    private val repository: FavListRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
