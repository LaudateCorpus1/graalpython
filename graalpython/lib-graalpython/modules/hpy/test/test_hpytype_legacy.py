# MIT License
# 
# Copyright (c) 2021, Oracle and/or its affiliates.
# Copyright (c) 2019 pyhandle
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

""" HPyType tests on legacy types. """

import pytest

from .support import HPyTest
from .test_hpytype import PointTemplate, TestType as _TestType


class LegacyPointTemplate(PointTemplate):
    """
    Override PointTemplate to instead define a legacy point type that
    still provides access to PyObject_HEAD.
    """

    _STRUCT_BEGIN_FORMAT = """
        #include <Python.h>
        typedef struct {{
            PyObject_HEAD
    """

    _STRUCT_END_FORMAT = """
        }} {struct_name};
        HPyType_LEGACY_HELPERS({struct_name})
    """

    _IS_LEGACY = ".legacy = true,"


@pytest.mark.usefixtures('skip_nfi')
class TestLegacyType(_TestType):

    ExtensionTemplate = LegacyPointTemplate


@pytest.mark.usefixtures('skip_nfi')
class TestCustomLegacyFeatures(HPyTest):

    def test_legacy_methods(self):
        mod = self.make_module("""
            #include <Python.h>

            static PyObject *f(PyObject *self, PyObject *args)
            {
                return PyLong_FromLong(1234);
            }
            static PyObject *g(PyObject *self, PyObject *arg)
            {
                long x = PyLong_AsLong(arg);
                return PyLong_FromLong(x * 2);
            }
            static PyObject *h(PyObject *self, PyObject *args)
            {
                long a, b, c;
                if (!PyArg_ParseTuple(args, "lll", &a, &b, &c))
                    return NULL;
                return PyLong_FromLong(100*a + 10*b + c);
            }
            static PyObject *k(PyObject *self, PyObject *args, PyObject *kwargs)
            {
                static char *kwlist[] = { "a", "b", "c", NULL };
                long a, b, c;
                if (!PyArg_ParseTupleAndKeywords(args, kwargs, "lll", kwlist, &a, &b, &c))
                    return NULL;
                return PyLong_FromLong(100*a + 10*b + c);
            }

            static PyMethodDef my_legacy_methods[] = {
                {"f", (PyCFunction)f, METH_NOARGS},
                {"g", (PyCFunction)g, METH_O},
                {"h", (PyCFunction)h, METH_VARARGS},
                {"k", (PyCFunction)k, METH_VARARGS | METH_KEYWORDS},
                {NULL}
            };

            @EXPORT_LEGACY(my_legacy_methods)
            @INIT
        """)
        assert mod.f() == 1234
        assert mod.g(45) == 90
        assert mod.h(4, 5, 6) == 456
        assert mod.k(c=6, b=5, a=4) == 456

    def test_legacy_inherits_from_pure_raises(self):
        import pytest
        mod_src = """
            static HPyType_Spec PureType_spec = {
                .name = "mytest.PureType",
                .flags = HPy_TPFLAGS_DEFAULT | HPy_TPFLAGS_BASETYPE,
            };

            static HPyType_Spec LegacyType_spec = {
                .name = "mytest.LegacyType",
                .legacy = true,
            };

            static void make_Types(HPyContext *ctx, HPy module)
            {
                HPy h_PureType = HPyType_FromSpec(ctx, &PureType_spec, NULL);
                if (HPy_IsNull(h_PureType)) {
                    return;
                }

                HPyType_SpecParam LegacyType_param[] = {
                    { HPyType_SpecParam_Base, h_PureType },
                    { 0 }
                };
                HPy h_LegacyType = HPyType_FromSpec(
                    ctx, &LegacyType_spec, LegacyType_param);
                if (HPy_IsNull(h_LegacyType)) {
                    HPy_Close(ctx, h_PureType);
                    return;
                }
                HPy_Close(ctx, h_LegacyType);
                HPy_Close(ctx, h_PureType);
            }
            @EXTRA_INIT_FUNC(make_Types)
            @INIT
        """
        with pytest.raises(TypeError) as err:
            self.make_module(mod_src)
        assert str(err.value) == (
            "A legacy type should not inherit its memory layout from a"
            " pure type")
