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

package net.openhft.jpsg.collect;

import net.openhft.jpsg.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;


public abstract class MethodGenerator {

    public static String primitiveHash(PrimitiveType type, String value) {
        switch (type) {
            case BYTE:
            case SHORT:
            case CHAR:
            case INT:
            case FLOAT:
                return value;
            case LONG:
            case DOUBLE:
                return format("((int) (%s ^ (%s >>> 32)))", value, value);
            default:
                throw new IllegalStateException();
        }
    }

    public static String wrap(MethodContext cxt, Option opt, String v) {
        if (cxt.internalVersion()) {
            // don't wrap, if internal version
            return v;
        }
        if (opt == PrimitiveType.FLOAT)
            v = "Float.intBitsToFloat(" + v + ")";
        if (opt == PrimitiveType.DOUBLE)
            v = "Double.longBitsToDouble(" + v + ")";
        return v;
    }

    public static String unwrap(MethodContext cxt, Option opt, String v) {
        if (cxt.internalVersion())
            return v;
        if (opt == PrimitiveType.FLOAT)
            v = "Float.floatToIntBits(" + v + ")";
        if (opt == PrimitiveType.DOUBLE)
            v = "Double.doubleToLongBits(" + v + ")";
        return v;
    }

    private static final SimpleOption AS = new SimpleOption("as");

    public static String defaultMethodName(MethodContext cxt, Method method) {
        String name = method.getClass().getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        if (cxt.isPrimitiveValue()) {
            Option suffix = cxt.getOption("suffix");
            if (AS.equals(suffix)) {
                name += "As" + ((PrimitiveType) cxt.mapValueOption()).title;
            }
        }
        return name;
    }

    // Useful utils

    protected static int countOccurrences(String s, String sub) {
        Pattern p = Pattern.compile(Pattern.quote(sub));
        Matcher m = p.matcher(s);
        int count = 0;
        while (m.find()) {
            count += 1;
        }
        return count;
    }

    protected static String replaceAll(String s, String sub, String repl) {
        return Pattern.compile(Pattern.quote(sub)).matcher(s).replaceAll(repl);
    }

    protected static String replaceFirst(String s, String sub, String repl) {
        return Pattern.compile(Pattern.quote(sub)).matcher(s).replaceFirst(repl);
    }

    protected List<String> lines = new ArrayList<>();
    protected String indent = "";

    public final MethodGenerator lines(String... lines) {
        for (String line : lines) {
            this.lines.add(indent + line);
        }
        return this;
    }


    protected EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
    protected MethodContext cxt;

    public final String generate(MethodContext cxt, String indent, Method method) {
        this.cxt = cxt;
        method.init(this, this.cxt);
        this.indent = indent;
        generateLines(method);
        if (cxt.immutable() && !permissions.isEmpty())
            return this.indent + "throw new java.lang.UnsupportedOperationException();\n";
        if (!this.indent.equals(indent))
            throw new IllegalStateException(
                    "Indent of start and end of the generated method doesn't match");
        String body = "";
        for (String line : lines) {
            body += line + "\n";
        }
        return body;
    }

    protected abstract void generateLines(Method method);


    protected String wrapKey(String key) {
        return wrap(cxt, cxt.keyOption(), key);
    }

    protected String unwrapKey(String key) {
        return unwrap(cxt, cxt.keyOption(), key);
    }

    protected String wrapValue(String value) {
        return wrap(cxt, cxt.mapValueOption(), value);
    }

    protected String unwrapValue(String value) {
        return unwrap(cxt, cxt.mapValueOption(), value);
    }


    public final MethodGenerator indent() {
        indent += "    ";
        return this;
    }

    public final MethodGenerator unIndent() {
        indent = indent.substring(4);
        return this;
    }

    public final MethodGenerator block() {
        String lastLine = lines.get(lines.size() - 1);
        lines.set(lines.size() - 1, lastLine + " {");
        indent();
        return this;
    }

    public final MethodGenerator ifBlock(String condition) {
        return lines("if (" + condition + ")").block();
    }

    public final MethodGenerator elseBlock() {
        return unIndent().lines("} else").block();
    }

    public final MethodGenerator elseIf(String condition) {
        return unIndent().lines("} else if (" + condition + ")").block();
    }

    public final MethodGenerator blockEnd() {
        return unIndent().lines("}");
    }

    public final void ret(String ret) {
        lines("return " + ret + ";");
    }

    public final void ret(boolean ret) {
        ret(ret + "");
    }

    public void concurrentMod() {
        lines("throw new java.util.ConcurrentModificationException();");
    }

    public void illegalState() {
        lines("throw new java.lang.IllegalStateException();");
    }

    public void unsupportedOperation() {
        lines("throw new java.lang.UnsupportedOperationException();");
    }
}
