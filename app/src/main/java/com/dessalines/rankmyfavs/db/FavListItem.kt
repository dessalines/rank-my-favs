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
import kotlin.math.min

const val DEFAULT_WIN_RATE = 0F
const val DEFAULT_GLICKO_RATING = 1500F
const val DEFAULT_GLICKO_DEVIATION = 350F
const val DEFAULT_GLICKO_VOLATILITY = 0.06F
const val DEFAULT_MATCH_COUNT = 0

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
@Keep
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
    @ColumnInfo(
        name = "match_count",
        defaultValue = DEFAULT_MATCH_COUNT.toString(),
    )
    val matchCount: Int,
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
    val description: String? = null,
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
    @ColumnInfo(
        name = "match_count",
        defaultValue = DEFAULT_MATCH_COUNT.toString(),
    )
    val matchCount: Int,
)

@Keep
data class FavListItemWithTier(
    val id: Int,
    val tier: String,
)

private const val BY_ID_QUERY = "SELECT * FROM FavListItem where id = :favListItemId"

@Dao
interface FavListItemDao {
    @Query("SELECT * FROM FavListItem where fav_list_id = :favListId order by glicko_rating desc")
    fun getFromList(favListId: Int): Flow<List<FavListItem>>

    @Query("SELECT COUNT(*) FROM FavListItem where fav_list_id = :favListId")
    fun getCountByIdSync(favListId: Int): Int

    @Query(BY_ID_QUERY)
    fun getById(favListItemId: Int): Flow<FavListItem>

    @Query(BY_ID_QUERY)
    fun getByIdSync(favListItemId: Int): FavListItem

    @Query(
        """
        SELECT id,
        CASE
            WHEN glicko_rating >= (SELECT glicko_rating FROM FavListItem ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET (SELECT COUNT(*) FROM FavListItem) * 1 / 5) THEN 'S'
            WHEN glicko_rating >= (SELECT glicko_rating FROM FavListItem ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET (SELECT COUNT(*) FROM FavListItem) * 2 / 5) THEN 'A'
            WHEN glicko_rating >= (SELECT glicko_rating FROM FavListItem ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET (SELECT COUNT(*) FROM FavListItem) * 3 / 5) THEN 'B'
            WHEN glicko_rating >= (SELECT glicko_rating FROM FavListItem ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET (SELECT COUNT(*) FROM FavListItem) * 4 / 5) THEN 'C'
            ELSE 'D'
        END AS tier
        FROM FavListItem
        WHERE fav_list_id = :favListId
        ORDER BY glicko_rating DESC
    """,
    )
    fun getFromListTiered(favListId: Int): List<FavListItemWithTier>

    @Query(
        """
        SELECT id,
        CASE
            WHEN glicko_rating >= (SELECT glicko_rating FROM (
                SELECT glicko_rating FROM FavListItem 
                WHERE fav_list_id = :favListId
                ORDER BY glicko_rating DESC
                LIMIT :limit
            ) subquery ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET :limit * 1 / 5) THEN 'S'
            WHEN glicko_rating >= (SELECT glicko_rating FROM (
                SELECT glicko_rating FROM FavListItem 
                WHERE fav_list_id = :favListId
                ORDER BY glicko_rating DESC
                LIMIT :limit
            ) subquery ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET :limit * 2 / 5) THEN 'A'
            WHEN glicko_rating >= (SELECT glicko_rating FROM (
                SELECT glicko_rating FROM FavListItem 
                WHERE fav_list_id = :favListId
                ORDER BY glicko_rating DESC
                LIMIT :limit
            ) subquery ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET :limit * 3 / 5) THEN 'B'
            WHEN glicko_rating >= (SELECT glicko_rating FROM (
                SELECT glicko_rating FROM FavListItem 
                WHERE fav_list_id = :favListId
                ORDER BY glicko_rating DESC
                LIMIT :limit
            ) subquery ORDER BY glicko_rating DESC
                LIMIT 1 OFFSET :limit * 4 / 5) THEN 'C'
            ELSE 'D'
        END AS tier
        FROM (
            SELECT * FROM FavListItem 
            WHERE fav_list_id = :favListId
            ORDER BY glicko_rating DESC
            LIMIT :limit
        ) AS top_items
        ORDER BY glicko_rating DESC
    """,
    )
    fun getFromListTieredWithLimit(favListId: Int, limit: Int): List<FavListItemWithTier>

    // The first option is the one with the lowest glicko_deviation, and a stop gap.
    // The second option is a random one.

    @Query(
        """
        SELECT * FROM FavListItem
        WHERE fav_list_id = :favListId 
        AND glicko_deviation > (1500 * (1 - (SELECT min_confidence/100.0 from AppSettings)))
        ORDER BY RANDOM()
        LIMIT 1
    """,
    )
    fun leastTrained(favListId: Int): FavListItem?

    // Sort the second match by the closest neighbor, IE abs(difference)
    @Query(
        """
        SELECT * FROM FavListItem
        WHERE fav_list_id = :favListId 
        AND id <> :firstItemId
        ORDER BY ABS(glicko_rating - :firstGlickoRating), RANDOM()
        LIMIT 1
    """,
    )
    fun closestMatch(
        favListId: Int,
        firstItemId: Int,
        firstGlickoRating: Float,
    ): FavListItem?

    @Insert(entity = FavListItem::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(favListItem: FavListItemInsert): Long

    @Update(entity = FavListItem::class)
    suspend fun updateNameAndDesc(favListItem: FavListItemUpdateNameAndDesc)

    @Update(entity = FavListItem::class)
    suspend fun updateStats(favListItem: FavListItemUpdateStats)

    @Query(
        """
        UPDATE FavListItem
        SET win_rate = $DEFAULT_WIN_RATE,
        glicko_rating = $DEFAULT_GLICKO_RATING,
        glicko_deviation = $DEFAULT_GLICKO_DEVIATION,
        glicko_volatility = $DEFAULT_GLICKO_VOLATILITY
        WHERE fav_list_id = :favListId
    """,
    )
    suspend fun clearStatsForList(favListId: Int)

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

    fun getByIdSync(favListItemId: Int) = favListItemDao.getByIdSync(favListItemId)

    fun getFromListTiered(favListId: Int, limit: Int? = null): Map<String, List<FavListItem>> {
        val tierList =
            limit?.let { lim ->
                favListItemDao.getFromListTieredWithLimit(
                    favListId,
                    min(lim, favListItemDao.getCountByIdSync(favListId))
                )
            }
                ?: favListItemDao.getFromListTiered(favListId)

        return tierList.groupBy(
            keySelector = { it.tier },
            valueTransform = { getByIdSync(it.id) }
        )
    }

    fun leastTrained(favListId: Int) = favListItemDao.leastTrained(favListId)

    fun closestMatch(
        favListId: Int,
        firstItemId: Int,
        firstGlickoRating: Float,
    ) = favListItemDao.closestMatch(favListId, firstItemId, firstGlickoRating)

    fun insert(favListItem: FavListItemInsert) = favListItemDao.insert(favListItem)

    @WorkerThread
    suspend fun updateNameAndDesc(favListItem: FavListItemUpdateNameAndDesc) =
        favListItemDao.updateNameAndDesc(favListItem)

    @WorkerThread
    suspend fun updateStats(favListItem: FavListItemUpdateStats) =
        favListItemDao.updateStats(favListItem)

    @WorkerThread
    suspend fun clearStatsForList(favListId: Int) = favListItemDao.clearStatsForList(favListId)

    @WorkerThread
    suspend fun delete(favListItem: FavListItem) = favListItemDao.delete(favListItem)
}

class FavListItemViewModel(
    private val repository: FavListItemRepository,
) : ViewModel() {
    fun getFromList(favListId: Int) = repository.getFromList(favListId)

    fun getById(favListItemId: Int) = repository.getById(favListItemId)

    fun getByIdSync(favListItemId: Int) = repository.getByIdSync(favListItemId)

    fun getFromListTiered(favListId: Int, limit: Int? = null) =
        repository.getFromListTiered(favListId, limit)

    fun leastTrained(favListId: Int) = repository.leastTrained(favListId)

    fun closestMatch(
        favListId: Int,
        firstItemId: Int,
        firstGlickoRating: Float,
    ) = repository.closestMatch(favListId, firstItemId, firstGlickoRating)

    fun insert(favListItem: FavListItemInsert) = repository.insert(favListItem)

    fun updateNameAndDesc(favListItem: FavListItemUpdateNameAndDesc) =
        viewModelScope.launch {
            repository.updateNameAndDesc(favListItem)
        }

    fun updateStats(favListItem: FavListItemUpdateStats) =
        viewModelScope.launch {
            repository.updateStats(favListItem)
        }

    fun clearStatsForList(favListId: Int) =
        viewModelScope.launch {
            repository.clearStatsForList(favListId)
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
        matchCount = 5,
    )
