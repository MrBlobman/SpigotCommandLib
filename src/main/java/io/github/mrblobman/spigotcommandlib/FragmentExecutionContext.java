/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 MrBlobman
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
package io.github.mrblobman.spigotcommandlib;

/**
 * Used to store state information that will be carried through
 * fragment handlers. (See: {@link FragmentedCommandHandle}).
 * The default implementation just handles state but registration
 * may be on a class that extends this base.
 */
public class FragmentExecutionContext {
    public static final int DEFAULT_STATE = 0;

    private int state;

    public FragmentExecutionContext() {
        this.state = 0;
    }

    /**
     * Get the current state that the executor bound to
     * this context is in.
     * <p>
     * The executor must be in the correct state in order for
     * execution of a fragment command to occur.
     *
     * @return the state of the executor bound to this context.
     */
    public final int getState() {
        return this.state;
    }

    /**
     * Set the current state that the executor bound to this context
     * is in.
     *
     * @param state the new state for this executor's context.
     */
    public final void setState(int state) {
        this.state = state;
    }
}
