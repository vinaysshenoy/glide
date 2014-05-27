package com.bumptech.glide.resize;

import android.os.Handler;
import com.bumptech.glide.resize.cache.ResourceCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class EngineJobTest {
    private static final String ID = "asdfas";
    private EngineJobHarness harness;

    @Before
    public void setUp() {
        harness = new EngineJobHarness();
    }

    @Test
    public void testOnResourceReadyPassedToCallbacks() throws Exception {
        harness.job.onResourceReady(harness.resource);

        Robolectric.runUiThreadTasks();
        verify(harness.cb).onResourceReady(eq(harness.resource));
    }

    @Test
    public void testListenerNotifiedJobCompleteOnOnResourceReady() {
        harness.job.onResourceReady(harness.resource);

        Robolectric.runUiThreadTasks();

        verify(harness.listener).onEngineJobComplete(eq(ID));
    }

    @Test
    public void testResourceAddedToCacheOnResourceReady() {
        harness.job.onResourceReady(harness.resource);

        Robolectric.runUiThreadTasks();
        verify(harness.resourceCache).put(eq(ID), eq(harness.resource));
    }

    @Test
    public void testOnExceptionPassedToCallbacks() throws Exception {
        Exception exception = new Exception("Test");

        harness.job.onException(exception);

        Robolectric.runUiThreadTasks();
        verify(harness.cb).onException(eq(exception));
    }

    @Test
    public void testListenerNotifiedJobCompleteOnException() {
        harness.job.onException(new Exception("test"));

        Robolectric.runUiThreadTasks();
        verify(harness.listener).onEngineJobComplete(eq(ID));
    }

    @Test
    public void testListenerNotifiedOfCancelOnCancel() {
        harness.job.cancel();

        verify(harness.listener).onEngineJobCancelled(eq(ID));
    }

    @Test
    public void testOnResourceReadyNotDeliveredAfterCancel() {
        harness.job.cancel();

        harness.job.onResourceReady(harness.resource);

        Robolectric.runUiThreadTasks();
        verify(harness.cb, never()).onResourceReady(eq(harness.resource));
    }

    @Test
    public void testOnExceptionNotDeliveredAfterCancel() {
        harness.job.cancel();

        harness.job.onException(new Exception("test"));

        Robolectric.runUiThreadTasks();
        verify(harness.cb, never()).onException(any(Exception.class));
    }

    @Test
    public void testRemovingAllCallbacksCancelsRunner() {
        harness.job.removeCallback(harness.cb);

        assertTrue(harness.job.isCancelled());
    }

    @Test
    public void removingSomeCallbacksDoesNotCancelRunner() {
        harness.job.addCallback(mock(ResourceCallback.class));
        harness.job.removeCallback(harness.cb);

        assertFalse(harness.job.isCancelled());
    }

    @SuppressWarnings("unchecked")
    private static class EngineJobHarness {
        ResourceRunner runner = mock(ResourceRunner.class);
        ResourceCache resourceCache = mock(ResourceCache.class);
        Handler mainHandler = new Handler();
        ResourceCallback<Object> cb = mock(ResourceCallback.class);
        Resource<Object> resource = mock(Resource.class);
        EngineJobListener listener = mock(EngineJobListener.class);

        EngineJob <Object> job = new EngineJob<Object>(ID, resourceCache, mainHandler, listener, cb);

        public EngineJobHarness() {
            job.addCallback(cb);
        }
    }
}