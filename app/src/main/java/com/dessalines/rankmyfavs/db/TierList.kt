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
    indices = [
        Index(value = ["fav_list_id"], unique = false),
        // Uniqueness is hard to deal with when swapping tier orders so I kept it false
        Index(value = ["tier_order"], unique = false),
    ],
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

    @Query("SELECT * FROM TierList where tier_order = :tierOrder")
    fun getByTierOrder(tierOrder: Int): TierList?

    @Query(
        """
        UPDATE TierList
        SET tier_order = CASE
            WHEN id = :tier1Id THEN :tier2Order
            WHEN id = :tier2Id THEN :tier1Order
            ELSE tier_order
        END
        WHERE id IN (:tier1Id, :tier2Id)
    """,
    )
    suspend fun swapTierOrders(
        tier1Id: Int,
        tier1Order: Int,
        tier2Id: Int,
        tier2Order: Int,
    )

    @Insert(entity = TierList::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(tierList: TierListInsert): Long

    @Update(entity = TierList::class)
    suspend fun update(tierList: TierListUpdate)

    @Delete
    suspend fun delete(tierList: TierList)

    @Query(
        """
        UPDATE TierList
        SET tier_order = tier_order - 1
        WHERE tier_order > :deletedTierOrder
        AND fav_list_id = :favListId
    """,
    )
    suspend fun decrementHigherTierOrders(
        deletedTierOrder: Int,
        favListId: Int,
    )
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TierListRepository(
    private val tierListDao: TierListDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getFromList(favListId: Int) = tierListDao.getFromList(favListId)

    fun getByTierOrder(tierOrder: Int) = tierListDao.getByTierOrder(tierOrder)

    @WorkerThread
    suspend fun swapTierOrders(
        tierList1: TierList,
        tierList2: TierList,
    ) = tierListDao.swapTierOrders(
        tierList1.id,
        tierList1.tierOrder,
        tierList2.id,
        tierList2.tierOrder,
    )

    fun insert(tierList: TierListInsert) = tierListDao.insert(tierList)

    @WorkerThread
    suspend fun update(tierList: TierListUpdate) = tierListDao.update(tierList)

    @WorkerThread
    suspend fun delete(tierList: TierList) = tierListDao.delete(tierList)

    @WorkerThread
    suspend fun decrementHigherTierOrders(
        deletedTierOrder: Int,
        favListId: Int,
    ) = tierListDao.decrementHigherTierOrders(deletedTierOrder, favListId)
}

class TierListViewModel(
    private val repository: TierListRepository,
) : ViewModel() {
    fun getFromList(favListId: Int) = repository.getFromList(favListId)

    fun getByTierOrder(tierOrder: Int) = repository.getByTierOrder(tierOrder)

    fun swapTierOrders(
        tierList: TierList,
        relativeTierOrder: Int,
    ) = viewModelScope.launch {
        getByTierOrder(tierList.tierOrder + relativeTierOrder)?.let {
            repository.swapTierOrders(tierList, it)
        }
    }

    fun insert(tierList: TierListInsert) = repository.insert(tierList)

    fun update(tierList: TierListUpdate) =
        viewModelScope.launch {
            repository.update(tierList)
        }

    fun delete(tierList: TierList) =
        viewModelScope.launch {
            repository.delete(tierList)
            repository.decrementHigherTierOrders(tierList.tierOrder, tierList.favListId)
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
