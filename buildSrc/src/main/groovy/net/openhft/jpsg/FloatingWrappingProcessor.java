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

package net.openhft.jpsg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FloatingWrappingProcessor extends TemplateProcessor {

    private static final Pattern WRAPPING_P = RegexpUtils.compile(
            "/[\\*/]\\s*(?<op>wrap|unwrap)\\s+(?<dim>[a-z0-9]+)\\s*[\\*/]/" +
            "((?<closed>(?<closedBody>[^/]+)/[\\*/][\\*/]/)|(?<openBody>[^\\s\\{\\};/\\*]+))");

    @Override
    protected void process(Context source, Context target, String template) {
        StringBuffer sb = new StringBuffer();
        Matcher m = WRAPPING_P.matcher(template);
        while (m.find()) {
            String body = m.group(m.group("closed") != null ? "closedBody" : "openBody");
            Option targetType = target.getOption(m.group("dim"));
            boolean wrap = m.group("op").equals("wrap");
            String repl = body;
            if (targetType == PrimitiveType.FLOAT) {
                repl = "Float." + (wrap ? "intBitsToFloat" : "floatToIntBits") + "(" + repl + ")";
            } else if (targetType == PrimitiveType.DOUBLE) {
                repl = "Double." + (wrap ? "longBitsToDouble" : "doubleToLongBits") + "(" +
                        repl + ")";
            }
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        postProcess(source, target, sb.toString());
    }
}
