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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FavListItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("item_id_1"),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FavListItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("item_id_2"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["item_id_1"], unique = false),
        Index(value = ["item_id_2"], unique = false),
    ],
)
data class FavListMatch(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "item_id_1",
    )
    val itemId1: Int,
    @ColumnInfo(
        name = "item_id_2",
    )
    val itemId2: Int,
    /**
     * This can be either 1 or 2
     */
    @ColumnInfo(
        name = "winner",
    )
    val winner: Int,
)

@Dao
interface FavListMatchDao {
    @Query("SELECT * FROM FavListMatch where item_id_1 = :itemId or item_id_2 = :itemId")
    fun getMatchups(itemId: Int): Flow<FavListMatch>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favList: FavListMatch)

    @Delete
    suspend fun delete(favList: FavListMatch)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class FavListMatchRepository(
    private val favListDao: FavListMatchDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getMatchups(itemId: Int) = favListDao.getMatchups(itemId)

    @WorkerThread
    suspend fun insert(favList: FavListMatch) = favListDao.insert(favList)

    @WorkerThread
    suspend fun delete(favList: FavListMatch) = favListDao.delete(favList)
}

class FavListMatchViewModel(
    private val repository: FavListMatchRepository,
) : ViewModel() {
    fun getMatchups(itemId: Int) = repository.getMatchups(itemId)

    fun insert(favList: FavListMatch) =
        viewModelScope.launch {
            repository.insert(favList)
        }

    fun delete(favList: FavListMatch) =
        viewModelScope.launch {
            repository.delete(favList)
        }
}

class FavListMatchViewModelFactory(
    private val repository: FavListMatchRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavListMatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavListMatchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
