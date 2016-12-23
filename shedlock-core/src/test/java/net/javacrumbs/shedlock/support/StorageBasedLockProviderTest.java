package net.javacrumbs.shedlock.support;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.support.StorageBasedLockProvider.StorageAccessor;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageBasedLockProviderTest {
    private static final LockConfiguration LOCK_CONFIGURATION = new LockConfiguration("name", Instant.now().plus(5, ChronoUnit.MINUTES));

    private StorageAccessor storageAccessor = mock(StorageAccessor.class);

    private final StorageBasedLockProvider lockProvider = new StorageBasedLockProvider(storageAccessor);

    @Test
    public void newRecordShouldOnlyBeInserted() {
        when(storageAccessor.insertRecord(LOCK_CONFIGURATION)).thenReturn(true);
        assertThat(lockProvider.lock(LOCK_CONFIGURATION)).isNotEmpty();
        verify(storageAccessor, never()).updateRecord(LOCK_CONFIGURATION);

        // Should update directly without insert
        reset(storageAccessor);
        when(storageAccessor.updateRecord(LOCK_CONFIGURATION)).thenReturn(true);
        assertThat(lockProvider.lock(LOCK_CONFIGURATION)).isNotEmpty();
        verify(storageAccessor, never()).insertRecord(LOCK_CONFIGURATION);
        verify(storageAccessor).updateRecord(LOCK_CONFIGURATION);
    }

    @Test
    public void updateOnDuplicateKey() {
        when(storageAccessor.insertRecord(LOCK_CONFIGURATION)).thenReturn(false);
        when(storageAccessor.updateRecord(LOCK_CONFIGURATION)).thenReturn(true);
        assertThat(lockProvider.lock(LOCK_CONFIGURATION)).isNotEmpty();
        verify(storageAccessor).updateRecord(LOCK_CONFIGURATION);

        // Should update directly without insert
        reset(storageAccessor);
        when(storageAccessor.updateRecord(LOCK_CONFIGURATION)).thenReturn(true);
        assertThat(lockProvider.lock(LOCK_CONFIGURATION)).isNotEmpty();
        verify(storageAccessor, never()).insertRecord(LOCK_CONFIGURATION);
        verify(storageAccessor).updateRecord(LOCK_CONFIGURATION);
    }

    @Test
    public void doNotReturnLockIfUpdatedZeroRows() {
        when(storageAccessor.insertRecord(LOCK_CONFIGURATION)).thenReturn(false);
        when(storageAccessor.updateRecord(LOCK_CONFIGURATION)).thenReturn(false);
        assertThat(lockProvider.lock(LOCK_CONFIGURATION)).isEmpty();
    }
}