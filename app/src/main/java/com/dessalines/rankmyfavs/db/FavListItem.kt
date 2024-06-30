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
    val description: String? = null,
)

@Entity
data class FavListItemInsert(
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

@Entity
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
    fun getFromList(favListId: Int): Flow<List<FavListItem>>

    @Query("SELECT * FROM FavListItem where id = :favListItemId")
    fun getById(favListItemId: Int): FavListItem

    // TODO this is a random match, to be gotten rid of later
    @Query("SELECT * FROM FavListItem where fav_list_id = :favListId ORDER BY RANDOM() LIMIT 2")
    fun randomMatch(favListId: Int): List<FavListItem>

    @Insert(entity = FavListItem::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(favList: FavListItemInsert): Long

    @Update(entity = FavListItem::class)
    suspend fun update(favList: FavListItemUpdate)

    @Delete
    suspend fun delete(favList: FavListItem)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class FavListItemRepository(
    private val favListItemDao: FavListItemDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getFromList(favListId: Int) = favListItemDao.getFromList(favListId)

    fun getById(favListItemId: Int) = favListItemDao.getById(favListItemId)

    fun randomMatch(favListId: Int) = favListItemDao.randomMatch(favListId)

    fun insert(favListItem: FavListItemInsert) = favListItemDao.insert(favListItem)

    @WorkerThread
    suspend fun update(favListItem: FavListItemUpdate) = favListItemDao.update(favListItem)

    @WorkerThread
    suspend fun delete(favListItem: FavListItem) = favListItemDao.delete(favListItem)
}

class FavListItemViewModel(
    private val repository: FavListItemRepository,
) : ViewModel() {
    fun getFromList(favListId: Int) = repository.getFromList(favListId)

    fun getById(favListItemId: Int) = repository.getById(favListItemId)

    fun randomMatch(favListId: Int) = repository.randomMatch(favListId)

    fun insert(favListItem: FavListItemInsert) = repository.insert(favListItem)

    fun update(favListItem: FavListItemUpdate) =
        viewModelScope.launch {
            repository.update(favListItem)
        }

    fun delete(favListItem: FavListItem) =
        viewModelScope.launch {
            repository.delete(favListItem)
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
