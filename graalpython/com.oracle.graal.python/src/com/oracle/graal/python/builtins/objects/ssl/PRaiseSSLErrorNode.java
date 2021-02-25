package com.oracle.graal.python.builtins.objects.ssl;

import java.util.IllegalFormatException;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.builtins.objects.exception.PBaseException;
import com.oracle.graal.python.builtins.objects.object.PythonObjectLibrary;
import static com.oracle.graal.python.builtins.objects.ssl.SSLErrorCode.ERROR_CERT_VERIFICATION;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.attributes.WriteAttributeToObjectNode;
import com.oracle.graal.python.runtime.PythonOptions;
import com.oracle.graal.python.runtime.exception.PException;
import com.oracle.graal.python.runtime.formatting.ErrorMessageFormatter;
import com.oracle.graal.python.runtime.object.PythonObjectFactory;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;

@GenerateUncached
public abstract class PRaiseSSLErrorNode extends Node {
    protected abstract PException execute(Node node, SSLErrorCode type, String message, Object[] args);

    public final PException raise(SSLErrorCode type, String message, Object... args) {
        return execute(this, type, message, args);
    }

    public static PException raiseUncached(Node node, SSLErrorCode type, String message, Object... args) {
        return PRaiseSSLErrorNodeGen.getUncached().execute(node, type, message, args);
    }

    @Specialization
    static PException raise(Node node, SSLErrorCode type, String format, Object[] formatArgs,
                    @CachedLanguage PythonLanguage language,
                    @Cached PythonObjectFactory factory,
                    @Cached WriteAttributeToObjectNode writeAttribute) {
        String message = getFormattedMessage(format, formatArgs);
        PBaseException exception = factory.createBaseException(type.getType(), factory.createTuple(new Object[]{type.getErrno(), message}));
        writeAttribute.execute(exception, "errno", type.getErrno());
        writeAttribute.execute(exception, "strerror", message);
        // TODO properly populate reason/lib attrs, this are dummy values
        writeAttribute.execute(exception, "reason", message);
        writeAttribute.execute(exception, "library", "[SSL]");
        if (type == ERROR_CERT_VERIFICATION) {
            // not trying to be 100% correct,
            // use code = 1 (X509_V_ERR_UNSPECIFIED) and msg from jdk exception instead
            // see openssl x509_txt.c#X509_verify_cert_error_string
            writeAttribute.execute(exception, "verify_code", 1);
            writeAttribute.execute(exception, "verify_message", message);
        }
        return PRaiseNode.raise(node, exception, PythonOptions.isPExceptionWithJavaStacktrace(language));
    }

    @TruffleBoundary
    private static String getFormattedMessage(String format, Object... args) {
        try {
            // pre-format for custom error message formatter
            if (ErrorMessageFormatter.containsCustomSpecifier(format)) {
                return new ErrorMessageFormatter().format(PythonObjectLibrary.getUncached(), format, args);
            }
            return String.format(format, args);
        } catch (IllegalFormatException e) {
            throw CompilerDirectives.shouldNotReachHere("error while formatting \"" + format + "\"", e);
        }
    }
}
