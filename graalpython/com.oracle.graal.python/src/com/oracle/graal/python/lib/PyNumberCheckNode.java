/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.python.lib;

import com.oracle.graal.python.builtins.PythonBuiltinClassType;
import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.PythonAbstractObject;
import com.oracle.graal.python.builtins.objects.type.SpecialMethodSlot;
import com.oracle.graal.python.nodes.PNodeWithContext;
import com.oracle.graal.python.nodes.attributes.LookupCallableSlotInMRONode;
import com.oracle.graal.python.nodes.object.GetClassNode;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;

/**
 * Equivalent of CPython's {@code PyNumber_Check}. Returns true if the argument provides numeric
 * protocols, and false otherwise.
 */
@GenerateUncached
@ImportStatic(SpecialMethodSlot.class)
public abstract class PyNumberCheckNode extends PNodeWithContext {
    public abstract boolean execute(Frame frame, Object object);

    public final boolean execute(Object object) {
        return execute(null, object);
    }

    @Specialization
    static boolean doString(@SuppressWarnings("unused") String object) {
        return false;
    }

    @Specialization
    static boolean doDouble(Double object) {
        return true;
    }

    @Specialization
    static boolean doInt(Integer object) {
        return true;
    }

    @Specialization
    static boolean doLong(Long object) {
        return true;
    }

    @Specialization
    static boolean doBoolean(Boolean object) {
        return true;
    }

    @Specialization
    static boolean doNone(PNone object) {
        return false;
    }

    @Specialization
    static boolean doPythonObject(PythonAbstractObject object,
                    @Cached GetClassNode getClassNode,
                    @Cached(parameters = "Index") LookupCallableSlotInMRONode lookupIndex,
                    @Cached(parameters = "Float") LookupCallableSlotInMRONode lookupFloat,
                    @Cached(parameters = "Int") LookupCallableSlotInMRONode lookupInt,
                    @Cached PyComplexCheckNode checkComplex) {
        Object type = getClassNode.execute(object);
        return lookupIndex.execute(type) != PNone.NO_VALUE || lookupInt.execute(type) != PNone.NO_VALUE || lookupFloat.execute(type) != PNone.NO_VALUE || checkComplex.execute(object);
    }

    @Specialization(replaces = "doPythonObject", guards = {"!isDouble(object)", "!isInteger(object)", "!isBoolean(object)", "!isNone(object)", "!isString(object)"})
    static boolean doObject(VirtualFrame frame, Object object,
                    @CachedLibrary(limit = "3") InteropLibrary interopLibrary,
                    @Cached GetClassNode getClassNode,
                    @Cached(parameters = "Index") LookupCallableSlotInMRONode lookupIndex,
                    @Cached(parameters = "Float") LookupCallableSlotInMRONode lookupFloat,
                    @Cached(parameters = "Int") LookupCallableSlotInMRONode lookupInt,
                    @Cached PyComplexCheckNode checkComplex) {
        Object type = getClassNode.execute(object);
        if (type == PythonBuiltinClassType.ForeignObject) {
            return interopLibrary.isNumber(object);
        }
        return lookupIndex.execute(type) != PNone.NO_VALUE || lookupInt.execute(type) != PNone.NO_VALUE || lookupFloat.execute(type) != PNone.NO_VALUE || checkComplex.execute(object);
    }

    public static PyNumberCheckNode getUncached() {
        return PyNumberCheckNodeGen.getUncached();
    }

}
