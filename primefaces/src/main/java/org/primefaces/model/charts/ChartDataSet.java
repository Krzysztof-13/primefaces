/*
 * The MIT License
 *
 * Copyright (c) 2009-2024 PrimeTek Informatics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.model.charts;

import java.io.IOException;
import java.io.Serializable;

/**
 * Abstract base class for all ChartJS datasets.
 *
 */
public abstract class ChartDataSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean hidden;

    public String encode() throws IOException {
        return null;
    }

    /**
     * Marks this dataset as hidden. Default is false.
     *
     * @return true if this dataset is hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Marks this dataset as hidden. Default is false.
     *
     * @param hidden true to hide the dataset
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
