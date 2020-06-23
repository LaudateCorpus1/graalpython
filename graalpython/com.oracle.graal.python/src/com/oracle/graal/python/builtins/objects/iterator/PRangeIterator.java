/*
 * Copyright (c) 2017, 2020, Oracle and/or its affiliates.
 * Copyright (c) 2013, Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.graal.python.builtins.objects.iterator;

import com.oracle.graal.python.builtins.objects.range.PRange;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public final class PRangeIterator extends PIntegerIterator {
    public final int start;
    public final int stop;
    public final int step;

    public PRangeIterator(Object clazz, DynamicObject storage, int start, int stop, int step) {
        super(clazz, storage);
        this.start = start;
        this.stop = stop;
        this.step = step;
        this.index = start;
    }

    public int getLength(ConditionProfile stepProfile, ConditionProfile positveRangeProfile) {
        if (stepProfile.profile(step > 0)) {
            if (positveRangeProfile.profile(index >= 0 && index < stop)) {
                return (stop - index - 1) / step + 1;
            }
            return PRange.getLenOfRange(index, stop, step);
        } else {
            return PRange.getLenOfRange(stop, index, -step);
        }
    }

    @Override
    public int next() {
        assert hasNext();
        int value = index;
        index += step;
        return value;
    }

    @Override
    public boolean hasNext() {
        return index < stop;
    }

    public static PRangeIterator require(Object value) {
        if (value instanceof PRangeIterator) {
            return (PRangeIterator) value;
        }
        CompilerDirectives.transferToInterpreter();
        throw new IllegalStateException("PRangeIterator required.");
    }
}
