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

import static com.oracle.graal.python.nodes.ErrorMessages.S_EXPECTED_SD_ARGS_GOT_D;
import static com.oracle.graal.python.nodes.ErrorMessages.UNPACKED_TUPLE_SHOULD_HAVE_D_ELEMS;

import com.oracle.graal.python.builtins.PythonBuiltinClassType;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;

/**
 * Equivalent of CPython's {@code _PyArg_CheckPositional}.
 */
@GenerateUncached
public abstract class PyArgCheckPositionalNode extends Node {
    public final boolean execute(String name, Object[] args, int min, int max) {
        return execute(name, args.length, min, max);
    }

    public abstract boolean execute(String name, int nargs, int min, int max);

    @Specialization
    static boolean doGeneric(String name, int nargs, int min, int max,
                    @Cached PRaiseNode raiseNode) {
        assert min >= 0;
        assert min <= max;

        if (nargs < min) {
            if (name != null) {
                throw raiseNode.raise(PythonBuiltinClassType.TypeError, S_EXPECTED_SD_ARGS_GOT_D,
                                name, (min == max ? "" : "at least "), min, min == 1 ? "" : "s", nargs);
            } else {
                throw raiseNode.raise(PythonBuiltinClassType.TypeError, UNPACKED_TUPLE_SHOULD_HAVE_D_ELEMS,
                                (min == max ? "" : "at least "), min, min == 1 ? "" : "s", nargs);
            }
        }

        if (nargs == 0) {
            return true;
        }

        if (nargs > max) {
            if (name != null) {
                throw raiseNode.raise(PythonBuiltinClassType.TypeError, S_EXPECTED_SD_ARGS_GOT_D,
                                name, (min == max ? "" : "at most "), max, max == 1 ? "" : "s", nargs);
            } else {
                throw raiseNode.raise(PythonBuiltinClassType.TypeError, UNPACKED_TUPLE_SHOULD_HAVE_D_ELEMS,
                                (min == max ? "" : "at most "), max, max == 1 ? "" : "s", nargs);
            }
        }

        return true;
    }
}
