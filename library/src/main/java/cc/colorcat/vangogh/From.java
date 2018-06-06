/*
 * Copyright 2018 cxx
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

package cc.colorcat.vangogh;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * Author: cxx
 * Date: 2017-07-10
 * GitHub: https://github.com/ccolorcat
 */
public enum From {
    MEMORY(Color.GREEN, From.POLICY_MEMORY),
    DISK(Color.BLUE, From.POLICY_DISK),
    NETWORK(Color.RED, From.POLICY_NETWORK),
    ANY(Color.TRANSPARENT, From.POLICY_MEMORY | From.POLICY_DISK | From.POLICY_NETWORK);

    final int debugColor;
    public final int policy;

    From(@ColorInt int debugColor, int policy) {
        this.debugColor = debugColor;
        this.policy = policy;
    }

    private static final int POLICY_MEMORY = 1;
    private static final int POLICY_DISK = 1 << 1;
    private static final int POLICY_NETWORK = 1 << 2;

    public static void checkFromPolicy(int fromPolicy) {
        if ((fromPolicy & From.ANY.policy) == 0) {
            throw new IllegalArgumentException("illegal fromPolicy = " + fromPolicy);
        }
    }
}
