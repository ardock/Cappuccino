package com.metova.capuccino;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;

import java.util.HashMap;
import java.util.Map;

public class Capuccino {

    private static boolean mIsTesting = true;

    // TODO find an elegant way to remove items from this map once they are no longer needed
    private static final Map<String, CapuccinoResourceWatcher> mResourceWatcherRegistry = new HashMap<>();

    private static final Map<String, CapuccinoIdlingResource> mIdlingResourceRegistry = new HashMap<>();

    /**
     * Returns a new {@code CapuccinoResourceWatcher}, which will be associated internally with a name derived from
     * {@param resource}. Internally, this uses either the canonical or simple name of the resource's class,
     * but this may change. Will return either a {@link OperatingCapuccinoResourceWatcher} if we're testing, or a
     * {@link NoOpCapuccinoResourceWatcher} if we're in production. See {@link #isTesting()}.
     *
     * @param resource The object for which this {@link CapuccinoResourceWatcher} will be a member.
     * @return an {@link CapuccinoResourceWatcher}.
     */
    @NonNull
    public static CapuccinoResourceWatcher newIdlingResourceWatcher(@NonNull Object resource) {
        return newIdlingResourceWatcher(nameOf(resource));
    }

    /**
     * Returns a new {@code CapuccinoResourceWatcher}, which will be associated internally with the name supplied.
     * Will return either a {@link OperatingCapuccinoResourceWatcher} if we're testing, or a {@link NoOpCapuccinoResourceWatcher}
     * if we're in production. See {@link #isTesting()}.
     *
     * @param name The name of this {@link CapuccinoResourceWatcher}.
     * @return an {@link CapuccinoResourceWatcher}.
     */
    @NonNull
    public static CapuccinoResourceWatcher newIdlingResourceWatcher(@NonNull String name) {
        CapuccinoResourceWatcher watcher;
        if (isTesting()) {
            watcher = new OperatingCapuccinoResourceWatcher();
        } else {
            watcher = new NoOpCapuccinoResourceWatcher();
        }
        mResourceWatcherRegistry.put(name, watcher);
        return watcher;
    }

    /**
     * Returns a name for the supplied {@code object}. Uses either {@code object.getClass().getCanonicalName()},
     * or {@code object.getClass().getSimpleName()} if the former returns null.
     *
     * @param object The {@code object} for which to generate a name.
     * @return a name for the supplied {@code object}.
     */
    @NonNull
    public static String nameOf(@NonNull Object object) {
        String name = object.getClass().getCanonicalName();
        name = name != null ? name : object.getClass().getSimpleName();
        return name;
    }

    /**
     * Returns the {@code CapuccinoResourceWatcher}, from the internal registry, associated
     * with the given {@param object}.
     *
     * @param object The object associated with the {@link CapuccinoResourceWatcher}.
     * @return the {@code CapuccinoResourceWatcher}, from the internal registry, associated
     * with the given {@param object}.
     * @throws IllegalArgumentException if there is no {@code CapuccinoResourceWatcher} associated
     *                                  with the given {@param object}.
     */
    @NonNull
    public static CapuccinoResourceWatcher getResourceWatcher(@NonNull Object object) {
        return getResourceWatcher(nameOf(object));
    }

    /**
     * Returns the {@code CapuccinoResourceWatcher}, from the internal registry, associated
     * with the given {@param name}.
     *
     * @param name The name associated with the {@link CapuccinoResourceWatcher}.
     * @return the {@code CapuccinoResourceWatcher}, from the internal registry, associated
     * with the given {@param name}.
     * @throws IllegalArgumentException if there is no {@code CapuccinoResourceWatcher} associated
     *                                  with the given {@param name}.
     */
    @NonNull
    public static CapuccinoResourceWatcher getResourceWatcher(@NonNull String name) {
        if (!mResourceWatcherRegistry.containsKey(name)) {
            throw new CapuccinoException(
                    String.format("There is no %s associated with the name %s", CapuccinoResourceWatcher.class.getSimpleName(), name));
        }

        return mResourceWatcherRegistry.get(name);
    }

    /**
     * Convenience method for {@link Espresso#registerIdlingResources(IdlingResource...)}, which first
     * instantiates an {@link CapuccinoIdlingResource}, then registers it with {@code Espresso}.
     *
     * @param object The object from which to generate an {@code CapuccinoIdlingResource}.
     */
    public static void registerIdlingResource(@NonNull Object object) {
        registerIdlingResource(nameOf(object));
    }

    /**
     * Convenience method for {@link Espresso#registerIdlingResources(IdlingResource...)}, which first
     * instantiates an {@link CapuccinoIdlingResource}, then registers it with {@code Espresso}.
     *
     * @param name The name from which to generate an {@code CapuccinoIdlingResource}.
     */
    public static void registerIdlingResource(@NonNull String name) {
        CapuccinoIdlingResource idlingResource = new CapuccinoIdlingResource(name);
        mIdlingResourceRegistry.put(name, idlingResource);
        Espresso.registerIdlingResources(idlingResource);
    }

    /**
     * Convenience method for {@link Espresso#unregisterIdlingResources(IdlingResource...)}, which
     * is the twin of {@link #registerIdlingResource(Object)}.
     *
     * @param object The object associated with the {@link CapuccinoIdlingResource} you wish to
     *               unregister.
     */
    public static void unregisterIdlingResource(@NonNull Object object) {
        unregisterIdlingResource(nameOf(object));
    }

    /**
     * Convenience method for {@link Espresso#unregisterIdlingResources(IdlingResource...)}, which
     * is the twin of {@link #registerIdlingResource(String)}.
     *
     * @param name The name associated with the {@link CapuccinoIdlingResource} you wish to
     *             unregister.
     */
    public static void unregisterIdlingResource(@NonNull String name) {
        CapuccinoIdlingResource idlingResource = mIdlingResourceRegistry.get(name);
        Espresso.unregisterIdlingResources(idlingResource);
        mIdlingResourceRegistry.remove(name);
    }

    /**
     * Returns true if we're testing; false otherwise. If true
     *
     * @return true if we're testing; false otherwise.
     */
    private static boolean isTesting() {
        return mIsTesting;
    }

    /**
     * Set to true if testing / during debug; false in production.
     *
     * @param isTesting True if testing / debug; false for production.
     */
    public static void setIsTesting(boolean isTesting) {
        mIsTesting = isTesting;
    }

    /**
     * Resets {@code Capuccino}'s internal state, for use in a {@code tearDown()}-type method during testing.
     */
    @VisibleForTesting
    public static void reset() {
        mResourceWatcherRegistry.clear();
        mIdlingResourceRegistry.clear();
        mIsTesting = true;
    }
}