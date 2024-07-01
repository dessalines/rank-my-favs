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

const val DEFAULT_WIN_RATE = 0F
const val DEFAULT_GLICKO_RATING = 1500F
const val DEFAULT_GLICKO_DEVIATION = 200F
const val DEFAULT_GLICKO_VOLATILITY = 0.06F
const val GLICKO_DEVIATION_MIN = 150F

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
    @ColumnInfo(
        name = "win_rate",
        defaultValue = DEFAULT_WIN_RATE.toString(),
    )
    val winRate: Float,
    @ColumnInfo(
        name = "glicko_rating",
        defaultValue = DEFAULT_GLICKO_RATING.toString(),
    )
    val glickoRating: Float,
    @ColumnInfo(
        name = "glicko_deviation",
        defaultValue = DEFAULT_GLICKO_DEVIATION.toString(),
    )
    val glickoDeviation: Float,
    @ColumnInfo(
        name = "glicko_volatility",
        defaultValue = DEFAULT_GLICKO_VOLATILITY.toString(),
    )
    val glickoVolatility: Float,
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
data class FavListItemUpdateNameAndDesc(
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

@Entity
data class FavListItemUpdateStats(
    val id: Int,
    @ColumnInfo(
        name = "win_rate",
        defaultValue = DEFAULT_WIN_RATE.toString(),
    )
    val winRate: Float,
    @ColumnInfo(
        name = "glicko_rating",
        defaultValue = DEFAULT_GLICKO_RATING.toString(),
    )
    val glickoRating: Float,
    @ColumnInfo(
        name = "glicko_deviation",
        defaultValue = DEFAULT_GLICKO_DEVIATION.toString(),
    )
    val glickoDeviation: Float,
    @ColumnInfo(
        name = "glicko_volatility",
        defaultValue = DEFAULT_GLICKO_VOLATILITY.toString(),
    )
    val glickoVolatility: Float,
)

@Dao
interface FavListItemDao {
    @Query("SELECT * FROM FavListItem where fav_list_id = :favListId order by glicko_rating desc")
    fun getFromList(favListId: Int): Flow<List<FavListItem>>

    @Query("SELECT * FROM FavListItem where id = :favListItemId")
    fun getById(favListItemId: Int): FavListItem

    // TODO
//    @Query("SELECT * FROM FavListItem where fav_list_id = :favListId ORDER BY RANDOM() LIMIT 2")
    // The deviation decreases after each
    // The first option is the one with the lowest glicko_deviation, and a stop gap.
    // The second option is a random one.

    @Query(
        """
        SELECT * FROM FavListItem
        WHERE fav_list_id = :favListId 
        AND glicko_deviation > $GLICKO_DEVIATION_MIN
        ORDER BY glicko_deviation DESC
        LIMIT 1
    """,
    )
    fun leastTrained(favListId: Int): FavListItem?

    @Query(
        """
        SELECT * FROM FavListItem
        WHERE fav_list_id = :favListId 
        AND id <> :firstFavListItemId
        ORDER BY RANDOM()
        LIMIT 1
    """,
    )
    fun randomAndNot(
        favListId: Int,
        firstFavListItemId: Int,
    ): FavListItem?

    @Insert(entity = FavListItem::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(favListItem: FavListItemInsert): Long

    @Update(entity = FavListItem::class)
    suspend fun updateNameAndDesc(favListItem: FavListItemUpdateNameAndDesc)

    @Update(entity = FavListItem::class)
    suspend fun updateStats(favListItem: FavListItemUpdateStats)

    @Delete
    suspend fun delete(favListItem: FavListItem)
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

    fun leastTrained(favListId: Int) = favListItemDao.leastTrained(favListId)

    fun randomAndNot(
        favListId: Int,
        favListItemId: Int,
    ) = favListItemDao.randomAndNot(favListId, favListItemId)

    fun insert(favListItem: FavListItemInsert) = favListItemDao.insert(favListItem)

    @WorkerThread
    suspend fun updateNameAndDesc(favListItem: FavListItemUpdateNameAndDesc) = favListItemDao.updateNameAndDesc(favListItem)

    @WorkerThread
    suspend fun updateStats(favListItem: FavListItemUpdateStats) = favListItemDao.updateStats(favListItem)

    @WorkerThread
    suspend fun delete(favListItem: FavListItem) = favListItemDao.delete(favListItem)
}

class FavListItemViewModel(
    private val repository: FavListItemRepository,
) : ViewModel() {
    fun getFromList(favListId: Int) = repository.getFromList(favListId)

    fun getById(favListItemId: Int) = repository.getById(favListItemId)

    fun leastTrained(favListId: Int) = repository.leastTrained(favListId)

    fun randomAndNot(
        favListId: Int,
        favListItemId: Int,
    ) = repository.randomAndNot(favListId, favListItemId)

    fun insert(favListItem: FavListItemInsert) = repository.insert(favListItem)

    fun updateNameAndDesc(favListItem: FavListItemUpdateNameAndDesc) =
        viewModelScope.launch {
            repository.updateNameAndDesc(favListItem)
        }

    fun updateStats(favListItem: FavListItemUpdateStats) =
        viewModelScope.launch {
            repository.updateStats(favListItem)
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

val sampleFavListItem =
    FavListItem(
        id = 1,
        favListId = 1,
        name = "Fav List 1",
        description = "ok",
        winRate = 66.5F,
        glickoRating = 1534F,
        glickoDeviation = 150F,
        glickoVolatility = 0.06F,
    )
