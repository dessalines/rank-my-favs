package com.dessalines.rankmyfavs.db

import androidx.annotation.Keep
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
@Keep
data class FavList(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "description",
    )
    val description: String? = null,
)

@Entity
data class FavListInsert(
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "description",
    )
    val description: String? = null,
)

@Entity
data class FavListUpdate(
    val id: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "description",
    )
    val description: String? = null,
)

@Dao
interface FavListDao {
    @Query("SELECT * FROM FavList")
    fun getAll(): Flow<List<FavList>>

    @Query("SELECT * FROM FavList where id = :id")
    fun getById(id: Int): FavList

    @Insert(entity = FavList::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(favList: FavListInsert): Long

    @Update(entity = FavList::class)
    suspend fun update(favList: FavListUpdate)

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

    fun getById(id: Int) = favListDao.getById(id)

    fun insert(favList: FavListInsert) = favListDao.insert(favList)

    @WorkerThread
    suspend fun update(favList: FavListUpdate) = favListDao.update(favList)

    @WorkerThread
    suspend fun delete(favList: FavList) = favListDao.delete(favList)
}

class FavListViewModel(
    private val repository: FavListRepository,
) : ViewModel() {
    val getAll = repository.getAll

    fun getById(id: Int) = repository.getById(id)

    fun insert(favList: FavListInsert) = repository.insert(favList)

    fun update(favList: FavListUpdate) =
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
