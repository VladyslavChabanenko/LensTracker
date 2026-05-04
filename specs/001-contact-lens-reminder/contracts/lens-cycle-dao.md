# Contract: LensCycleDao

**Type**: Room DAO (commonMain)
**Technology**: androidx.room3 (KMP), accessed via `LensCycleRepository`

---

## Interface

```kotlin
// commonMain: com.chb.lenstracker.data.db.LensCycleDao
@Dao
interface LensCycleDao {

    /** Observe the current lens cycle. Emits null when no cycle has been saved yet. */
    @Query("SELECT * FROM lens_cycle WHERE id = 1")
    fun observeLensCycle(): Flow<LensCycleEntity?>

    /** Read the current lens cycle once (suspend). Returns null if not set. */
    @Query("SELECT * FROM lens_cycle WHERE id = 1")
    suspend fun getLensCycle(): LensCycleEntity?

    /**
     * Insert or replace the lens cycle (singleton row, id always = 1).
     * Use @Upsert to handle both first-save and update cases.
     */
    @Upsert
    suspend fun upsertLensCycle(lensCycle: LensCycleEntity)
}
```

---

## Usage contract

- **Read path**: UI layer collects `observeLensCycle()` via `LensCycleRepository`, which exposes it as `Flow<LensCycleEntity?>` to `MainViewModel`.
- **Write path**: `LensCycleRepository.save(lensCycle)` calls `upsertLensCycle()`; then triggers `NotificationScheduler.schedule()` with the new next trigger time.
- **Reset path**: `LensCycleRepository.markAsChanged()` creates a new `LensCycleEntity` with `startDate = today` (same interval and reminderTime), calls `upsertLensCycle()`, then reschedules the notification.
- **No delete**: The singleton row is never deleted; it is always replaced.

---

## Auto-migrations

Schema version starts at 1. When fields are added in future versions, add to `@Database`:

```kotlin
autoMigrations = [
    AutoMigration(from = 1, to = 2)  // example
]
```

Schema JSON files are committed to `composeApp/schemas/` (already configured in `build.gradle.kts`).
