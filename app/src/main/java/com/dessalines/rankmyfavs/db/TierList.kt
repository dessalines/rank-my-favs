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
    indices = [Index(value = ["fav_list_id"], unique = false)],
)
@Keep
data class TierList(
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
        name = "color",
    )
    val color: Int,
    @ColumnInfo(
        name = "tier_order",
    )
    val tierOrder: Int,
)

@Entity
data class TierListInsert(
    @ColumnInfo(
        name = "fav_list_id",
    )
    val favListId: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "color",
    )
    val color: Int,
    @ColumnInfo(
        name = "tier_order",
    )
    val tierOrder: Int,
)

@Entity
data class TierListUpdate(
    val id: Int,
    @ColumnInfo(
        name = "fav_list_id",
    )
    val favListId: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "color",
    )
    val color: Int,
    @ColumnInfo(
        name = "tier_order",
    )
    val tierOrder: Int,
)

@Dao
interface TierListDao {
    @Query("SELECT * FROM TierList where fav_list_id = :favListId ORDER BY tier_order ASC")
    fun getFromList(favListId: Int): Flow<List<TierList>>

    @Query("SELECT * FROM TierList where fav_list_id = :favListId ORDER BY tier_order ASC")
    fun getFromListSync(favListId: Int): List<TierList>

    @Query("SELECT * FROM TierList where id = :tierListId ORDER BY tier_order ASC")
    fun getById(tierListId: Int): Flow<TierList>

    @Query("SELECT * FROM TierList where id = :tierListId ORDER BY tier_order ASC")
    fun getByIdSync(tierListId: Int): TierList

    @Insert(entity = TierList::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(tierList: TierListInsert): Long

    @Update(entity = TierList::class)
    suspend fun update(tierList: TierListUpdate)

    @Delete
    suspend fun delete(tierList: TierList)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TierListRepository(
    private val tierListDao: TierListDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getFromList(favListId: Int) = tierListDao.getFromList(favListId)

    fun getFromListSync(favListId: Int) = tierListDao.getFromListSync(favListId)

    fun getById(tierListId: Int) = tierListDao.getById(tierListId)

    fun getByIdSync(tierListId: Int) = tierListDao.getByIdSync(tierListId)

    fun insert(tierList: TierListInsert) = tierListDao.insert(tierList)

    @WorkerThread
    suspend fun update(tierList: TierListUpdate) = tierListDao.update(tierList)

    @WorkerThread
    suspend fun delete(tierList: TierList) = tierListDao.delete(tierList)
}

class TierListViewModel(
    private val repository: TierListRepository,
) : ViewModel() {
    fun getFromList(favListId: Int) = repository.getFromList(favListId)

    fun getFromListSync(favListId: Int) = repository.getFromListSync(favListId)

    fun getById(tierListId: Int) = repository.getById(tierListId)

    fun getByIdSync(tierListId: Int) = repository.getByIdSync(tierListId)

    fun insert(tierList: TierListInsert) = repository.insert(tierList)

    fun update(tierList: TierListUpdate) =
        viewModelScope.launch {
            repository.update(tierList)
        }

    fun delete(tierList: TierList) =
        viewModelScope.launch {
            repository.delete(tierList)
        }
}

class TierListViewModelFactory(
    private val repository: TierListRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TierListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TierListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
