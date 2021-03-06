/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.collect.impl.hash;

import net.openhft.collect.HashConfig;
import net.openhft.collect.HashOverflowException;
import net.openhft.collect.impl.AbstractContainer;

import static java.lang.Math.max;


public abstract class MutableDHash extends AbstractContainer implements DHash {

    private static int minFreeSlots(int capacity, int size) {
        // See "Tombstones purge from hashtable: theory and practice" wiki page
        double load = (double) size / (double) capacity;
        double rehashLoad = 0.49 + 0.866 * load - 0.363 * load * load;
        // Need at least one free slot for open addressing
        return max(1, (int) ((double) capacity * (1.0 - rehashLoad)));
    }

    ////////////////////////////
    // Fields

    private HashConfigWrapper configWrapper;


    /** The current number of occupied slots in the hash. */
    private int size;

    private int maxSize;

    /** The current number of free slots in the hash. */
    private int freeSlots;


    private int minFreeSlots;


    private int removedSlots;


    private int modCount = 0;


    /////////////////////////////
    // Getters

    @Override
    public final HashConfig hashConfig() {
        return configWrapper.config();
    }

    @Override
    public final HashConfigWrapper configWrapper() {
        return configWrapper;
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public abstract int capacity();

    @Override
    public final boolean noRemoved() {
        return removedSlots == 0;
    }

    @Override
    public final int freeSlots() {
        return freeSlots;
    }

    @Override
    public final int removedSlots() {
        return removedSlots;
    }

    @Override
    public final int modCount() {
        return modCount;
    }

    final void incrementModCount() {
        modCount++;
    }

    @Override
    public final float currentLoad() {
        // Division in double to minimize precision loss
        return (float) (((double) (size + removedSlots)) / (double) capacity());
    }


    ////////////////////////
    // Initialization and construction operations

    /**
     * Root operation for copy constructors
     *
     * @param hash Mutable or Immutable DHash instance
     */
    final void copy(DHash hash) {
        this.configWrapper = hash.configWrapper();
        int size = this.size = hash.size();
        int capacity = hash.capacity();
        this.maxSize = configWrapper.maxSize(capacity);
        int freeSlots = this.freeSlots = hash.freeSlots();
        int minFreeSlots = this.minFreeSlots = minFreeSlots(capacity, size);
        // see #initSlotCounts()
        if (freeSlots < minFreeSlots) this.minFreeSlots = (freeSlots + 1) / 2;
        this.removedSlots = hash.removedSlots();
    }

    /**
     * Creates data structures with a prime capacity at or near the minimum
     * needed to hold {@code size} elements without triggering a rehash.
     *
     * <p>Should be called only in constructors and externalization code.
     */
    final void init(HashConfigWrapper configWrapper, int size) {
        this.configWrapper = configWrapper;
        this.size = 0;
        int capacity = DHashCapacities.capacity(configWrapper, size);
        minFreeSlots = minFreeSlots(capacity, size);
        internalInit(capacity);
    }

    private void internalInit(int capacity) {
        initSlotCounts(capacity);
        allocateArrays(capacity);
    }

    /**
     * Allocates arrays of {@code capacity} size to hold states, elements, keys
     * or values in.
     *
     * <p>Subclasses should override this method, but SHOULD NOT call it. This
     * method is called in MutableDHash from initForRehash() and init() methods.
     *
     * @param capacity size of arrays, comprising the hash
     */
    abstract void allocateArrays(int capacity);

    private void initSlotCounts(int capacity) {
        maxSize = configWrapper.maxSize(capacity);
        int freeSlots = this.freeSlots = capacity - size;
        // free could be less than minFreeSlots only in case when capacity
        // is not sufficient to comply load factor (due to saturation with
        // Java array size limit). Set minFreeSlots to a half of free to avoid
        // too often (instant) rehashing in this case.
        if (freeSlots < minFreeSlots) this.minFreeSlots = (freeSlots + 1) / 2;
        removedSlots = 0;
    }

    /**
     * Moves elements to the new arrays of {@code newCapacity} size.
     *
     * <p>This method should be implemented as follows:
     *
     *  1. Copy references to the old arrays comprising the hash from fields
     *     to local variables
     *
     *  2. Call {@link #initForRehash(int)}
     *
     *  3. Move elements, entries, etc. from the old arrays to the new ones.
     *
     * <p>Subclasses should implement, but MUST NOT call this method. This method is called
     * in DHash from postInsertHooks, {@link #ensureCapacity(long)} and {@link #shrink()} methods.
     */
    abstract void rehash(int newCapacity);

    /**
     * This method just increments modification count (see {@link #modCount()})
     * and calls {@link #internalInit(int)}. Should be called by subclasses in
     * {@link #rehash(int)} implementation.
     */
    final void initForRehash(int newCapacity) {
        modCount++;
        internalInit(newCapacity);
    }

    
    //////////////////////////////
    // Roots of chain operations

    /**
     * Empties the hash.
     */
    public void clear() {
        modCount++;
        size = 0;
        freeSlots = capacity();
        removedSlots = 0;
    }

    
    abstract void removeAt( int index );
    

    /////////////////////////////
    // Modification hooks and rehash logic

    @Override
    public boolean shrink() {
        int newCapacity = DHashCapacities.capacity(configWrapper, size);
        if (removedSlots > 0 || newCapacity < capacity()) {
            rehash(newCapacity);
            return true;
        } else {
            return false;
        }
    }

    private boolean tryRehashForExpansion(int newCapacity) {
        // No sense in rehashing for expansion if we already reached Java array
        // size limit.
        if (newCapacity > capacity() || removedSlots > 0) {
            rehash(newCapacity);
            return true;
        } else {
            if (freeSlots < minFreeSlots)
                minFreeSlots = (freeSlots + 1) / 2;
            return false;
        }
    }

    @Override
    public final boolean ensureCapacity(long minSize) {
        int intMinSize = (int) Math.min(minSize, Integer.MAX_VALUE);
        if (minSize < 0L)
            throw new IllegalArgumentException(
                    "Min size should be positive, " + minSize + " given.");
        int additionalSize = intMinSize - size;
        if (additionalSize <= 0)
            return false;
        int lowFreeEstimate;
        if (removedSlots == 0) {
            lowFreeEstimate = freeSlots - additionalSize;
        } else {
            int nonFull = freeSlots + removedSlots;
            float freeFraction =  ((float) freeSlots) / nonFull;
            // Precise free estimate = free * (nonFull - addSize) /
            //                        (nonFull - (1 - freeFraction) * addSize)
            // Because freeEstimate'(addSize) =
            //            - (freeEstimate(addSize) / (nonFull - addSize)) ^ 2
            //
            // This estimate is even lower:
            lowFreeEstimate = (int) ((nonFull - additionalSize) * freeFraction);
        }
        if (lowFreeEstimate < minFreeSlots) {
            return tryRehashForExpansion(DHashCapacities.capacity(configWrapper, intMinSize));
        } else {
            return false;
        }
    }

    final void postRemoveHook() {
        modCount++;
        size--;
        removedSlots++;
    }
//
//    /**
//     * After insertion, this hook is called to adjust the size/free
//     * values of the hash and to perform rehashing if necessary.
//     *
//     * @param usedFreeSlot the slot
//     */
//    final void postInsertHook(boolean usedFreeSlot) {
//        modCount++;
//        size++;
//        if (usedFreeSlot) {
//            if (--freeSlots < minFreeSlots) {
//                if (!tryRehashIfTooFewFreeSlots() && freeSlots == 0) {
//                    throw new HashOverflowException();
//                }
//            }
//        } else {
//            removedSlots--;
//        }
//    }

    final void postFreeSlotInsertHook() {
        modCount++;
        if (++size > maxSize) {
            if (tryRehashForExpansion(grownCapacity()))
                return;
        }
        if (--freeSlots < minFreeSlots) {
            if (!tryRehashIfTooFewFreeSlots() && freeSlots == 0) {
                throw new HashOverflowException();
            }
        }
    }

    final void postRemovedSlotInsertHook() {
        modCount++;
        if (++size > maxSize) {
            if (tryRehashForExpansion(grownCapacity()))
                return;
        }
        removedSlots--;
    }

    private boolean tryRehashIfTooFewFreeSlots() {
        if (removedSlots > 0) {
            rehash(DHashCapacities.capacity(configWrapper, size));
            return true;
        } else {
            return tryRehashForExpansion(configWrapper.grow(capacity()));
        }
    }

    private int grownCapacity() {
        return DHashCapacities.nearestGreaterCapacity(configWrapper.grow(capacity()), size);
    }
}