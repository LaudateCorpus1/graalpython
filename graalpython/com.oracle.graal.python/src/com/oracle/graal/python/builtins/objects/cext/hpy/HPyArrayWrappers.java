/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.graal.python.builtins.objects.cext.hpy;

import java.util.Arrays;

import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.cext.capi.InvalidateNativeObjectsAllManagedNode;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodes.HPyAsHandleNode;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodes.HPyCloseHandleNode;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodes.HPyEnsureHandleNode;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodes.PCallHPyFunction;
import com.oracle.graal.python.builtins.objects.ints.PInt;
import com.oracle.graal.python.runtime.GilNode;
import com.oracle.graal.python.util.OverflowException;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.llvm.spi.NativeTypeLibrary;

public class HPyArrayWrappers {

    @ExportLibrary(InteropLibrary.class)
    abstract static class HPyObjectArrayWrapper implements TruffleObject {

        final GraalHPyContext hpyContext;

        private Object[] delegate;
        private Object nativePointer;

        HPyObjectArrayWrapper(GraalHPyContext hpyContext, Object[] delegate) {
            this.hpyContext = hpyContext;
            this.delegate = delegate;
        }

        HPyObjectArrayWrapper(GraalHPyContext hpyContext, int capacity) {
            this.hpyContext = hpyContext;
            this.delegate = new Object[capacity];
        }

        public final Object[] getDelegate() {
            return delegate;
        }

        final void setDelegate(Object[] delegate) {
            this.delegate = delegate;
        }

        final void setNativePointer(Object nativePointer) {
            this.nativePointer = nativePointer;
        }

        final Object getNativePointer() {
            return this.nativePointer;
        }

        @Override
        public int hashCode() {
            CompilerAsserts.neverPartOfCompilation();
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(delegate);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            // n.b.: (tfel) This is hopefully fine here, since if we get to this
            // code path, we don't speculate that either of those objects is
            // constant anymore, so any caching on them won't happen anyway
            return delegate == ((HPyObjectArrayWrapper) obj).delegate;
        }

        @ExportMessage
        boolean hasArrayElements() {
            return true;
        }

        @ExportMessage
        static final class GetArraySize {

            @Specialization(guards = "delegate != null")
            static long doManaged(HPyObjectArrayWrapper receiver,
                            @Exclusive @Cached GilNode gil,
                            @Bind("gil.acquire()") boolean mustRelease,
                            @Bind("receiver.getDelegate()") Object[] delegate) {
                try {
                    assert receiver.getNativePointer() == null : "HPyObjectArrayWrapper is managed but there is a native pointer.";
                    return delegate.length;
                } finally {
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "receiver.getNativePointer() != null", replaces = "doManaged")
            static long doNative(HPyObjectArrayWrapper receiver,
                            @Exclusive @Cached GilNode gil,
                            @Bind("gil.acquire()") boolean mustRelease,
                            @CachedLibrary(limit = "1") InteropLibrary lib) throws UnsupportedMessageException {
                try {
                    assert receiver.getDelegate() == null : "HPyObjectArrayWrapper is native but managed object wasn't free'd.";
                    return lib.getArraySize(receiver.getNativePointer());
                } finally {
                    gil.release(mustRelease);
                }
            }

            @Specialization(replaces = {"doManaged", "doNative"})
            static long doGeneric(HPyObjectArrayWrapper receiver,
                            @Exclusive @Cached GilNode gil,
                            @Bind("gil.acquire()") boolean mustRelease,
                            @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib) throws UnsupportedMessageException {
                try {
                    Object[] delegate = receiver.getDelegate();
                    if (delegate != null) {
                        return delegate.length;
                    }
                    return lib.getArraySize(receiver.getNativePointer());
                } finally {
                    gil.release(mustRelease);
                }
            }
        }

        @ExportMessage
        boolean isArrayElementReadable(long idx,
                        @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib,
                        @Exclusive @Cached GilNode gil) {
            boolean mustRelease = gil.acquire();
            try {
                if (delegate != null) {
                    return 0 <= idx && idx < delegate.length;
                }
                return lib.isArrayElementReadable(nativePointer, idx);
            } finally {
                gil.release(mustRelease);
            }
        }

        @ExportMessage
        boolean isArrayElementModifiable(long idx,
                        @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib,
                        @Exclusive @Cached GilNode gil) {
            return isArrayElementReadable(idx, lib, gil);
        }

        @ExportMessage
        boolean isArrayElementInsertable(@SuppressWarnings("unused") long idx) {
            return false;
        }

        @ExportMessage
        Object readArrayElement(@SuppressWarnings("unused") long idx) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new AbstractMethodError();
        }

        @ExportMessage
        void writeArrayElement(long idx, Object value,
                        @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib,
                        @Exclusive @Cached GilNode gil)
                        throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {
            boolean mustRelease = gil.acquire();
            try {
                if (!isPointer()) {
                    delegate[(int) idx] = value;
                } else {
                    lib.writeArrayElement(nativePointer, idx, value);
                }
            } finally {
                gil.release(mustRelease);
            }
        }

        @ExportMessage
        boolean isPointer() {
            return nativePointer != null;
        }

        @ExportMessage
        long asPointer(
                        @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib) throws UnsupportedMessageException {
            return lib.asPointer(nativePointer);
        }
    }

    /**
     * Wraps a sequence object (like a list) such that it behaves like a {@code HPy} array (C type
     * {@code HPy *}).
     */
    @ExportLibrary(InteropLibrary.class)
    @ExportLibrary(value = NativeTypeLibrary.class, useForAOT = false)
    static final class HPyArrayWrapper extends HPyObjectArrayWrapper {

        public HPyArrayWrapper(GraalHPyContext hpyContext, Object[] delegate) {
            super(hpyContext, delegate);
        }

        @ExportMessage
        static final class ReadArrayElement {

            @Specialization(guards = "delegate != null", rewriteOn = OverflowException.class)
            static Object doManaged(HPyArrayWrapper receiver, long index,
                            @Bind("receiver.getDelegate()") Object[] delegate,
                            @Shared("isHandleProfile") @Cached("createCountingProfile()") ConditionProfile isHandleProfile,
                            @Shared("asHandleNode") @Cached HPyAsHandleNode asHandleNode,
                            @Exclusive @Cached GilNode gil) throws OverflowException {
                boolean mustRelease = gil.acquire();
                try {
                    assert receiver.getNativePointer() == null : "HPyObjectArrayWrapper is managed but there is a native pointer.";
                    int i = PInt.intValueExact(index);
                    Object object = delegate[i];
                    if (!isHandleProfile.profile(object instanceof GraalHPyHandle)) {
                        object = asHandleNode.execute(receiver.hpyContext, object);
                        delegate[i] = object;
                    }
                    return object;
                } finally {
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "delegate != null", replaces = "doManaged")
            static Object doManagedOvf(HPyArrayWrapper receiver, long index,
                            @Bind("receiver.getDelegate()") Object[] delegate,
                            @Shared("isHandleProfile") @Cached("createCountingProfile()") ConditionProfile isHandleProfile,
                            @Shared("asHandleNode") @Cached HPyAsHandleNode asHandleNode,
                            @Exclusive @Cached GilNode gil) throws InvalidArrayIndexException {
                try {
                    return doManaged(receiver, index, delegate, isHandleProfile, asHandleNode, gil);
                } catch (OverflowException e) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw InvalidArrayIndexException.create(index);
                }
            }

            @Specialization(guards = "receiver.getNativePointer() != null", replaces = {"doManaged", "doManagedOvf"})
            static Object doNative(HPyArrayWrapper receiver, long index,
                            @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib,
                            @Shared("ensureHandleNode") @Cached HPyEnsureHandleNode ensureHandleNode,
                            @Exclusive @Cached GilNode gil) throws UnsupportedMessageException, InvalidArrayIndexException {
                boolean mustRelease = gil.acquire();
                try {
                    assert receiver.getDelegate() == null : "HPyObjectArrayWrapper is native but managed object wasn't free'd.";
                    // read the array element; this will return a pointer to an HPy struct
                    try {
                        Object element = lib.readArrayElement(receiver.getNativePointer(), index);
                        return ensureHandleNode.execute(receiver.hpyContext, lib.readMember(element, GraalHPyHandle.I));
                    } catch (UnknownIdentifierException e) {
                        throw CompilerDirectives.shouldNotReachHere();
                    }
                } finally {
                    gil.release(mustRelease);
                }
            }

            @Specialization(replaces = {"doManaged", "doNative"})
            static Object doGeneric(HPyArrayWrapper receiver, long index,
                            @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib,
                            @Shared("ensureHandleNode") @Cached HPyEnsureHandleNode ensureHandleNode,
                            @Exclusive @Cached GilNode gil) throws UnsupportedMessageException, InvalidArrayIndexException {
                boolean mustRelease = gil.acquire();
                try {
                    Object[] delegate = receiver.getDelegate();
                    if (delegate != null) {
                        try {
                            return delegate[PInt.intValueExact(index)];
                        } catch (OverflowException e) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            throw InvalidArrayIndexException.create(index);
                        }
                    }
                    try {
                        Object element = lib.readArrayElement(receiver.getNativePointer(), index);
                        return ensureHandleNode.execute(receiver.hpyContext, lib.readMember(element, GraalHPyHandle.I));
                    } catch (UnknownIdentifierException e) {
                        throw CompilerDirectives.shouldNotReachHere();
                    }
                } finally {
                    gil.release(mustRelease);
                }
            }
        }

        @ExportMessage
        void toNative(
                        @Cached GraalHPyNodes.PCallHPyFunction callToArrayNode,
                        @Cached.Exclusive @Cached InvalidateNativeObjectsAllManagedNode invalidateNode,
                        @Exclusive @Cached GilNode gil) {
            boolean mustRelease = gil.acquire();
            try {
                invalidateNode.execute();
                if (!isPointer()) {
                    setNativePointer(callToArrayNode.call(hpyContext, GraalHPyNativeSymbol.GRAAL_HPY_ARRAY_TO_NATIVE, this, (long) getDelegate().length));
                    setDelegate(null);
                }
            } finally {
                gil.release(mustRelease);
            }
        }

        @ExportMessage
        @SuppressWarnings("static-method")
        boolean hasNativeType() {
            return true;
        }

        @ExportMessage
        Object getNativeType() {
            return hpyContext.getHPyArrayNativeType();
        }
    }

    abstract static class HPyCloseArrayWrapperNode extends Node {

        public abstract void execute(GraalHPyContext hpyContext, HPyArrayWrapper wrapper);

        @Specialization(guards = {"cachedLen == size(lib, wrapper)", "cachedLen <= 8"}, limit = "1")
        @ExplodeLoop
        static void doCachedLen(GraalHPyContext hpyContext, HPyArrayWrapper wrapper,
                        @CachedLibrary("wrapper") InteropLibrary lib,
                        @Cached("size(lib, wrapper)") int cachedLen,
                        @Cached HPyCloseHandleNode closeHandleNode,
                        @Cached(value = "createProfiles(cachedLen)", dimensions = 1) ConditionProfile[] profiles) {
            try {
                for (int i = 0; i < cachedLen; i++) {
                    Object element = lib.readArrayElement(wrapper, i);
                    if (profiles[i].profile(element instanceof GraalHPyHandle)) {
                        closeHandleNode.execute(hpyContext, element);
                    }
                }
            } catch (InteropException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
        }

        @Specialization(replaces = "doCachedLen", limit = "1")
        static void doLoop(GraalHPyContext hpyContext, HPyArrayWrapper wrapper,
                        @CachedLibrary("wrapper") InteropLibrary lib,
                        @Cached HPyCloseHandleNode closeHandleNode,
                        @Cached ConditionProfile profile) {
            int n = size(lib, wrapper);
            try {
                for (int i = 0; i < n; i++) {
                    Object element = lib.readArrayElement(wrapper, i);
                    if (profile.profile(element instanceof GraalHPyHandle)) {
                        closeHandleNode.execute(hpyContext, element);
                    }
                }
            } catch (InteropException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
        }

        static int size(InteropLibrary lib, HPyArrayWrapper wrapper) {
            try {
                return PInt.intValueExact(lib.getArraySize(wrapper));
            } catch (OverflowException e) {
                // we know that the length should always fit into an integer
                throw CompilerDirectives.shouldNotReachHere("array length does not fit into int");
            } catch (UnsupportedMessageException e) {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        static ConditionProfile[] createProfiles(int n) {
            ConditionProfile[] profiles = new ConditionProfile[n];
            for (int i = 0; i < profiles.length; i++) {
                profiles[i] = ConditionProfile.create();
            }
            return profiles;
        }
    }

    @ExportLibrary(value = NativeTypeLibrary.class, useForAOT = false)
    @ExportLibrary(InteropLibrary.class)
    static final class PtrArrayWrapper extends HPyObjectArrayWrapper {

        PtrArrayWrapper(GraalHPyContext hpyContext, int capacity) {
            super(hpyContext, capacity);
        }

        @ExportMessage
        Object readArrayElement(long idx,
                        @Shared("lib") @CachedLibrary(limit = "1") InteropLibrary lib,
                        @Exclusive @Cached GilNode gil) throws InvalidArrayIndexException, UnsupportedMessageException {
            boolean mustRelease = gil.acquire();
            try {
                if (!isPointer()) {
                    Object result = getDelegate()[(int) idx];
                    if (result == null) {
                        // TODO(fa): not sure if this is a good idea but it will do the job since it
                        // reports `isNull == true`
                        return PNone.NO_VALUE;
                    }
                    return result;
                }
                return lib.readArrayElement(getNativePointer(), idx);
            } finally {
                gil.release(mustRelease);
            }
        }

        @ExportMessage
        @SuppressWarnings("static-method")
        boolean hasNativeType() {
            return false;
        }

        @ExportMessage
        @SuppressWarnings("static-method")
        Object getNativeType() {
            return null;
        }

        @ExportMessage
        void toNative(
                        @Cached PCallHPyFunction callHPyFunction,
                        @Exclusive @Cached GilNode gil) {
            boolean mustRelease = gil.acquire();
            try {
                if (!isPointer()) {
                    setNativePointer(callHPyFunction.call(hpyContext, GraalHPyNativeSymbol.GRAAL_HPY_POINTER_ARRAY_TO_NATIVE, this, (long) getDelegate().length));
                    setDelegate(null);
                }
            } finally {
                gil.release(mustRelease);
            }
        }
    }
}
