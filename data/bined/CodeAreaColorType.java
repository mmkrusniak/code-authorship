/*
 * Copyright (C) ExBin Project
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
package org.exbin.bined.color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for code area color type.
 *
 * @version 0.2.0 2018/11/13
 * @author ExBin Project (https://exbin.org)
 */
public interface CodeAreaColorType {

    /**
     * Returns unique string identifier.
     *
     * Custom implementations should start with full package name to avoid
     * collisions.
     *
     * @return unique identification ID key
     */
    @Nonnull
    String getId();

    /**
     * Returns group which this color belongs to or null.
     *
     * @return group
     */
    @Nullable
    CodeAreaColorGroup getGroup();
}
