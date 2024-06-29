package com.dessalines.rankmyfavs.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FavList::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("fav_list_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["fav_list_id", "name"], unique = true)],
)
// TODO should the stats live here, or in another table?
data class FavListItem(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "fav_list_id",
    )
    val favListId: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "description",
    )
    val description: String?,
)

data class FavListItemUpdate(
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
interface FavListItemDao {
    @Query("SELECT * FROM FavListItem where fav_list_id = :favListId")
    fun getFromList(favListId: Int): Flow<FavListItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favList: FavListItem)

    @Update
    suspend fun update(favList: FavListItem)

    @Delete
    suspend fun delete(favList: FavListItem)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class FavListItemRepository(
    private val favListDao: FavListItemDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getFromList(favListId: Int) = favListDao.getFromList(favListId)

    @WorkerThread
    suspend fun insert(favList: FavListItem) = favListDao.insert(favList)

    @WorkerThread
    suspend fun update(favList: FavListItem) = favListDao.update(favList)

    @WorkerThread
    suspend fun delete(favList: FavListItem) = favListDao.delete(favList)
}

class FavListItemViewModel(
    private val repository: FavListItemRepository,
) : ViewModel() {
    fun getFromList(favListId: Int) = repository.getFromList(favListId)

    fun insert(favList: FavListItem) =
        viewModelScope.launch {
            repository.insert(favList)
        }

    fun update(favList: FavListItem) =
        viewModelScope.launch {
            repository.update(favList)
        }

    fun delete(favList: FavListItem) =
        viewModelScope.launch {
            repository.delete(favList)
        }
}

class FavListItemViewModelFactory(
    private val repository: FavListItemRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavListItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavListItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
