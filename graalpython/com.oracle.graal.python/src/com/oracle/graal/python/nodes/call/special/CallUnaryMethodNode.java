/*
 * Copyright (c) 2018, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.python.nodes.call.special;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.function.BuiltinMethodDescriptor;
import com.oracle.graal.python.builtins.objects.function.BuiltinMethodDescriptor.BinaryBuiltinDescriptor;
import com.oracle.graal.python.builtins.objects.function.BuiltinMethodDescriptor.TernaryBuiltinDescriptor;
import com.oracle.graal.python.builtins.objects.function.BuiltinMethodDescriptor.UnaryBuiltinDescriptor;
import com.oracle.graal.python.builtins.objects.function.PArguments;
import com.oracle.graal.python.builtins.objects.function.PBuiltinFunction;
import com.oracle.graal.python.builtins.objects.function.PKeyword;
import com.oracle.graal.python.builtins.objects.method.PBuiltinMethod;
import com.oracle.graal.python.nodes.call.CallNode;
import com.oracle.graal.python.nodes.call.GenericInvokeNode;
import com.oracle.graal.python.nodes.call.special.MaybeBindDescriptorNode.BoundDescriptor;
import com.oracle.graal.python.nodes.function.builtins.PythonBinaryBuiltinNode;
import com.oracle.graal.python.nodes.function.builtins.PythonTernaryBuiltinNode;
import com.oracle.graal.python.nodes.function.builtins.PythonUnaryBuiltinNode;
import com.oracle.graal.python.util.PythonUtils;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism.Megamorphic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ConditionProfile;

@GenerateUncached
public abstract class CallUnaryMethodNode extends CallSpecialMethodNode {

    public static CallUnaryMethodNode create() {
        return CallUnaryMethodNodeGen.create();
    }

    public static CallUnaryMethodNode getUncached() {
        return CallUnaryMethodNodeGen.getUncached();
    }

    public abstract int executeInt(Frame frame, Object callable, int receiver) throws UnexpectedResultException;

    public abstract long executeLong(Frame frame, Object callable, long receiver) throws UnexpectedResultException;

    public abstract double executeDouble(Frame frame, Object callable, double receiver) throws UnexpectedResultException;

    public abstract boolean executeBoolean(Frame frame, Object callable, boolean receiver) throws UnexpectedResultException;

    public abstract boolean executeBoolean(Frame frame, Object callable, int receiver) throws UnexpectedResultException;

    public abstract boolean executeBoolean(Frame frame, Object callable, long receiver) throws UnexpectedResultException;

    public abstract boolean executeBoolean(Frame frame, Object callable, double receiver) throws UnexpectedResultException;

    public abstract int executeInt(Frame frame, Object callable, Object receiver) throws UnexpectedResultException;

    public abstract long executeLong(Frame frame, Object callable, Object receiver) throws UnexpectedResultException;

    public abstract double executeDouble(Frame frame, Object callable, Object receiver) throws UnexpectedResultException;

    public abstract boolean executeBoolean(Frame frame, Object callable, Object receiver) throws UnexpectedResultException;

    public abstract Object executeObject(Frame frame, Object callable, Object receiver);

    public final Object executeObject(Object callable, Object receiver) {
        return executeObject(null, callable, receiver);
    }

    @Specialization(guards = "cachedInfo == info", limit = "getCallSiteInlineCacheMaxDepth()")
    Object callUnarySpecialMethodSlotInlined(VirtualFrame frame, @SuppressWarnings("unused") UnaryBuiltinDescriptor info, Object receiver,
                    @SuppressWarnings("unused") @Cached("info") UnaryBuiltinDescriptor cachedInfo,
                    @Cached("cachedInfo.createNode()") PythonUnaryBuiltinNode node) {
        return node.call(frame, receiver);
    }

    @Specialization(guards = "cachedInfo == info", limit = "getCallSiteInlineCacheMaxDepth()")
    Object callBinarySpecialMethodSlotInlined(VirtualFrame frame, @SuppressWarnings("unused") BinaryBuiltinDescriptor info, Object receiver,
                    @SuppressWarnings("unused") @Cached("info") BinaryBuiltinDescriptor cachedInfo,
                    @Cached("cachedInfo.createNode()") PythonBinaryBuiltinNode node) {
        return node.call(frame, receiver, PNone.NO_VALUE);
    }

    @Specialization(guards = "cachedInfo == info", limit = "getCallSiteInlineCacheMaxDepth()")
    Object callTernarySpecialMethodSlotInlined(VirtualFrame frame, @SuppressWarnings("unused") TernaryBuiltinDescriptor info, Object receiver,
                    @SuppressWarnings("unused") @Cached("info") TernaryBuiltinDescriptor cachedInfo,
                    @Cached("cachedInfo.createNode()") PythonTernaryBuiltinNode node) {
        return node.call(frame, receiver, PNone.NO_VALUE, PNone.NO_VALUE);
    }

    @Specialization(guards = "isBuiltinDescriptor(info)", replaces = {"callUnarySpecialMethodSlotInlined", "callBinarySpecialMethodSlotInlined", "callTernarySpecialMethodSlotInlined"})
    Object callSpecialMethodSlotCallTarget(VirtualFrame frame, BuiltinMethodDescriptor info, Object receiver,
                    @CachedLanguage PythonLanguage language,
                    @Cached GenericInvokeNode invokeNode) {
        RootCallTarget callTarget = language.getDescriptorCallTarget(info);
        Object[] arguments = PArguments.create(1);
        PArguments.setArgument(arguments, 0, receiver);
        return invokeNode.execute(frame, callTarget, arguments);
    }

    @Specialization(guards = {"func == cachedFunc",
                    "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class, assumptions = "singleContextAssumption()")
    int callIntSingle(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, int receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinFunction cachedFunc,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callInt(frame, receiver);
    }

    @Specialization(guards = {"func.getCallTarget() == ct", "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class)
    int callInt(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, int receiver,
                    @SuppressWarnings("unused") @Cached(value = "func.getCallTarget()") RootCallTarget ct,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callInt(frame, receiver);
    }

    @Specialization(guards = {"func == cachedFunc",
                    "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class, assumptions = "singleContextAssumption()")
    long callLongSingle(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, long receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinFunction cachedFunc,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callLong(frame, receiver);
    }

    @Specialization(guards = {"func.getCallTarget() == ct", "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class)
    long callLong(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, long receiver,
                    @SuppressWarnings("unused") @Cached(value = "func.getCallTarget()") RootCallTarget ct,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callLong(frame, receiver);
    }

    @Specialization(guards = {"func == cachedFunc",
                    "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class, assumptions = "singleContextAssumption()")
    double callDoubleSingle(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, double receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinFunction cachedFunc,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callDouble(frame, receiver);
    }

    @Specialization(guards = {"func.getCallTarget() == ct", "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class)
    double callDouble(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, double receiver,
                    @SuppressWarnings("unused") @Cached(value = "func.getCallTarget()") RootCallTarget ct,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callDouble(frame, receiver);
    }

    @Specialization(guards = {"func == cachedFunc",
                    "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class, assumptions = "singleContextAssumption()")
    boolean callBoolSingle(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, boolean receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinFunction cachedFunc,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callBool(frame, receiver);
    }

    @Specialization(guards = {"func.getCallTarget() == ct", "builtinNode != null",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", rewriteOn = UnexpectedResultException.class)
    boolean callBool(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, boolean receiver,
                    @SuppressWarnings("unused") @Cached(value = "func.getCallTarget()") RootCallTarget ct,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) throws UnexpectedResultException {
        return builtinNode.callBool(frame, receiver);
    }

    @Specialization(guards = {"func == cachedFunc", "builtinNode != null", "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", assumptions = "singleContextAssumption()")
    Object callObjectSingle(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, Object receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinFunction cachedFunc,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, receiver);
    }

    @Specialization(guards = {"func.getCallTarget() == ct", "builtinNode != null", "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()")
    Object callObject(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinFunction func, Object receiver,
                    @SuppressWarnings("unused") @Cached(value = "func.getCallTarget()") RootCallTarget ct,
                    @Cached("getUnary(frame, func)") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, receiver);
    }

    @Specialization(guards = {"func == cachedFunc", "builtinNode != null", "!takesSelfArg",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", assumptions = "singleContextAssumption()")
    Object callMethodSingleContext(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinMethod func, Object receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinMethod cachedFunc,
                    @SuppressWarnings("unused") @Cached("takesSelfArg(func)") boolean takesSelfArg,
                    @Cached("getUnary(frame, func.getFunction())") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, receiver);
    }

    @Specialization(guards = {"func == cachedFunc", "builtinNode != null", "takesSelfArg",
                    "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()", assumptions = "singleContextAssumption()")
    Object callSelfMethodSingleContext(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinMethod func, Object arg,
                    @SuppressWarnings("unused") @Cached(value = "func", weak = true) PBuiltinMethod cachedFunc,
                    @SuppressWarnings("unused") @Cached("takesSelfArg(func)") boolean takesSelfArg,
                    @Cached("getBinary(frame, func.getFunction())") PythonBinaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, func.getSelf(), arg);
    }

    @Specialization(guards = {"builtinNode != null", "getCallTarget(func) == ct", "!takesSelfArg", "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()")
    Object callMethod(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinMethod func, Object receiver,
                    @SuppressWarnings("unused") @Cached("getCallTarget(func)") RootCallTarget ct,
                    @SuppressWarnings("unused") @Cached("takesSelfArg(func)") boolean takesSelfArg,
                    @Cached("getUnary(frame, func.getFunction())") PythonUnaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, receiver);
    }

    @Specialization(guards = {"builtinNode != null", "getCallTarget(func) == ct", "takesSelfArg", "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()")
    Object callSelfMethod(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinMethod func, Object arg,
                    @SuppressWarnings("unused") @Cached("getCallTarget(func)") RootCallTarget ct,
                    @SuppressWarnings("unused") @Cached("takesSelfArg(func)") boolean takesSelfArg,
                    @Cached("getBinary(frame, func.getFunction())") PythonBinaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, func.getSelf(), arg);
    }

    @Specialization(guards = { //
                    "func == cachedFunc", //
                    "builtinNode != null", //
                    "!takesSelfArg", //
                    "minArgs == 1", //
                    "frame != null || unusedFrame"}, //
                    limit = "getCallSiteInlineCacheMaxDepth()", //
                    assumptions = "singleContextAssumption()")
    static Object callBinaryMethodSingleContext(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinMethod func, Object receiver,
                    @SuppressWarnings("unused") @Cached("func") PBuiltinMethod cachedFunc,
                    @SuppressWarnings("unused") @Cached("takesSelfArg(func)") boolean takesSelfArg,
                    @SuppressWarnings("unused") @Cached("getMinArgs(func)") int minArgs,
                    @Cached("getBinary(frame, func.getFunction())") PythonBinaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, receiver, PNone.NO_VALUE);
    }

    /**
     * In case the function takes at least 1 argument (so is <it>at least</it> unary) we also try
     * higher orders.
     */
    @Specialization(guards = {"builtinNode != null", "minArgs == 1", "getCallTarget(func) == ct", "!takesSelfArg", "frame != null || unusedFrame"}, limit = "getCallSiteInlineCacheMaxDepth()")
    static Object callBinaryMethod(VirtualFrame frame, @SuppressWarnings("unused") PBuiltinMethod func, Object arg,
                    @SuppressWarnings("unused") @Cached("getCallTarget(func)") RootCallTarget ct,
                    @SuppressWarnings("unused") @Cached("takesSelfArg(func)") boolean takesSelfArg,
                    @SuppressWarnings("unused") @Cached("getMinArgs(func)") int minArgs,
                    @Cached("getBinary(frame, func.getFunction())") PythonBinaryBuiltinNode builtinNode,
                    @SuppressWarnings("unused") @Cached("frameIsUnused(builtinNode)") boolean unusedFrame) {
        return builtinNode.call(frame, arg, PNone.NO_VALUE);
    }

    @Specialization(guards = "!isBuiltinDescriptor(func)", replaces = {"callIntSingle", "callInt", "callLongSingle", "callLong", "callDoubleSingle", "callDouble", "callBoolSingle", "callBool",
                    "callObjectSingle", "callObject",
                    "callMethodSingleContext", "callSelfMethodSingleContext", "callMethod", "callSelfMethod", "callBinaryMethodSingleContext", "callBinaryMethod"})
    @Megamorphic
    static Object call(VirtualFrame frame, Object func, Object receiver,
                    @Cached CallNode callNode,
                    @Cached ConditionProfile isBoundProfile) {
        if (isBoundProfile.profile(func instanceof BoundDescriptor)) {
            return callNode.execute(frame, ((BoundDescriptor) func).descriptor, PythonUtils.EMPTY_OBJECT_ARRAY, PKeyword.EMPTY_KEYWORDS);
        } else {
            return callNode.execute(frame, func, new Object[]{receiver}, PKeyword.EMPTY_KEYWORDS);
        }
    }
}
