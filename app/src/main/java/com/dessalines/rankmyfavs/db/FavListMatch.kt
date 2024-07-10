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
        ForeignKey(
            entity = FavListItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("winner_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["item_id_1"], unique = false),
        Index(value = ["item_id_2"], unique = false),
        Index(value = ["winner_id"], unique = false),
    ],
)
@Keep
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
        name = "winner_id",
    )
    val winnerId: Int,
)

@Entity
data class FavListMatchInsert(
    @ColumnInfo(
        name = "item_id_1",
    )
    val itemId1: Int,
    @ColumnInfo(
        name = "item_id_2",
    )
    val itemId2: Int,
    @ColumnInfo(
        name = "winner_id",
    )
    val winnerId: Int,
)

@Dao
interface FavListMatchDao {
    // TODO do this in SQL, not in code
    @Query("SELECT * FROM FavListMatch where item_id_1 = :itemId or item_id_2 = :itemId")
    fun getMatchups(itemId: Int): List<FavListMatch>

    @Insert(entity = FavListMatch::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(match: FavListMatchInsert): Long

    @Delete
    suspend fun delete(match: FavListMatch)

    @Query(
        """
        DELETE FROM FavListMatch
        WHERE item_id_1 in ( select id from FavListItem WHERE fav_list_id = :favListId)
        or item_id_2 in ( select id from FavListItem WHERE fav_list_id = :favListId)
    """,
    )
    suspend fun deleteMatchesForList(favListId: Int)

    @Query(
        """
        DELETE FROM FavListMatch
        WHERE item_id_1 = :itemId
        or item_id_2 = :itemId
    """,
    )
    suspend fun deleteMatchesForItem(itemId: Int)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class FavListMatchRepository(
    private val favListDao: FavListMatchDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getMatchups(itemId: Int) = favListDao.getMatchups(itemId)

    fun insert(match: FavListMatchInsert) = favListDao.insert(match)

    @WorkerThread
    suspend fun delete(match: FavListMatch) = favListDao.delete(match)

    @WorkerThread
    suspend fun deleteMatchesForList(favListId: Int) = favListDao.deleteMatchesForList(favListId)

    @WorkerThread
    suspend fun deleteMatchesForItem(itemId: Int) = favListDao.deleteMatchesForItem(itemId)
}

class FavListMatchViewModel(
    private val repository: FavListMatchRepository,
) : ViewModel() {
    fun getMatchups(itemId: Int) = repository.getMatchups(itemId)

    fun insert(match: FavListMatchInsert) = repository.insert(match)

    fun delete(match: FavListMatch) =
        viewModelScope.launch {
            repository.delete(match)
        }

    fun deleteMatchesForList(favListId: Int) =
        viewModelScope.launch {
            repository.deleteMatchesForList(favListId)
        }

    fun deleteMatchesForItem(itemId: Int) =
        viewModelScope.launch {
            repository.deleteMatchesForItem(itemId)
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
