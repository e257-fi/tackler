/*
 * Copyright 2019 E257.FI
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
 *
 */
package fi.e257.tackler.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class TacklerErrorHandler extends DefaultErrorStrategy {

    private String getErrString(Parser recognizer) {
        if (recognizer == null ||
                recognizer.getContext() == null ||
                recognizer.getContext().getStart() == null) {
            return "Can not parse input, but no detailed error information is available.";
        }

        String errMsg = ""
                + "   Can not parse input\n"
                + "   on line: " + recognizer.getContext().getStart().getLine() + "\n"
                + "   no viable alternative at input '" + recognizer.getContext().getStart().getText() + "'";

        return errMsg;
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new ParseCancellationException(getErrString(recognizer), e);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new ParseCancellationException(getErrString(recognizer));
    }

    @Override
    public void sync(Parser recognizer) {
    }
}
