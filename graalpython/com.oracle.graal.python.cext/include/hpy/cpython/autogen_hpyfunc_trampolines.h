/* MIT License
 *
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates.
 * Copyright (c) 2019 pyhandle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


/*
   DO NOT EDIT THIS FILE!

   This file is automatically generated by hpy.tools.autogen.hpyfunc.autogen_cpython_hpyfunc_trampoline_h
   See also hpy.tools.autogen and hpy/tools/public_api.h

   Run this to regenerate:
       make autogen

*/

#define _HPyFunc_TRAMPOLINE_HPyFunc_UNARYFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_BINARYFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, cpy_PyObject *arg1) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_TERNARYFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, cpy_PyObject *arg1, cpy_PyObject *arg2) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1), _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_INQUIRY(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_LENFUNC(SYM, IMPL) \
    static HPy_ssize_t SYM(cpy_PyObject *arg0) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SSIZEARGFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, HPy_ssize_t arg1) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), arg1)); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SSIZESSIZEARGFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, HPy_ssize_t arg1, HPy_ssize_t arg2) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), arg1, arg2)); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SSIZEOBJARGPROC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, HPy_ssize_t arg1, cpy_PyObject *arg2) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), arg1, _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SSIZESSIZEOBJARGPROC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, HPy_ssize_t arg1, HPy_ssize_t arg2, cpy_PyObject *arg3) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), arg1, arg2, _py2h(arg3))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_OBJOBJARGPROC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, cpy_PyObject *arg1, cpy_PyObject *arg2) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1), _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_FREEFUNC(SYM, IMPL) \
    static void SYM(void *arg0) \
    { \
        return (IMPL(_HPyGetContext(), arg0)); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_GETATTRFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, char *arg1) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), arg1)); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_GETATTROFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, cpy_PyObject *arg1) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SETATTRFUNC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, char *arg1, cpy_PyObject *arg2) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), arg1, _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SETATTROFUNC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, cpy_PyObject *arg1, cpy_PyObject *arg2) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1), _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_REPRFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_HASHFUNC(SYM, IMPL) \
    static HPy_hash_t SYM(cpy_PyObject *arg0) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_GETITERFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_ITERNEXTFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_DESCRGETFUNC(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, cpy_PyObject *arg1, cpy_PyObject *arg2) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1), _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_DESCRSETFUNC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, cpy_PyObject *arg1, cpy_PyObject *arg2) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1), _py2h(arg2))); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_GETTER(SYM, IMPL) \
    static cpy_PyObject *SYM(cpy_PyObject *arg0, void *arg1) \
    { \
        return _h2py(IMPL(_HPyGetContext(), _py2h(arg0), arg1)); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_SETTER(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, cpy_PyObject *arg1, void *arg2) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1), arg2)); \
    }
#define _HPyFunc_TRAMPOLINE_HPyFunc_OBJOBJPROC(SYM, IMPL) \
    static int SYM(cpy_PyObject *arg0, cpy_PyObject *arg1) \
    { \
        return (IMPL(_HPyGetContext(), _py2h(arg0), _py2h(arg1))); \
    }
