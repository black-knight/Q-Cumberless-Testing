// Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
// 3. The name of the author may not be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.trollsahead.qcumberless.gui.elements;

import com.trollsahead.qcumberless.engine.Engine;
import com.trollsahead.qcumberless.gui.CumberlessMouseListener;
import com.trollsahead.qcumberless.gui.EditBox;
import com.trollsahead.qcumberless.util.ElementHelper;
import com.trollsahead.qcumberless.util.Util;

import java.awt.*;

public class Table {
    private static final int MINIMUM_CELL_WIDTH = 50;

    private static final int CELL_PADDING_HORIZONTAL = 10;
    private static final int CELL_PADDING_VERTICAL = 2;

    private static final Color COLOR_LINE = new Color(0.0f, 0.0f, 0.0f, 0.3f);
    private static final Color COLOR_TEXT = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private static final Color COLOR_BG_HIGHLIGHT = new Color(0.0f, 0.0f, 0.0f, 0.2f);

    public int rows = 2;
    public int cols = 2;

    public int highlightedRow;
    public int highlightedCol;

    private int width;
    private int height;

    private Cell[][] cells;
    private int[] colWidth;
    private int[] colCharWidth;
    private int rowHeight;

    private Cell editedCell = null;

    private BaseBarElement parent;

    public Table(BaseBarElement parent) {
        this.parent = parent;
        colWidth = new int[cols];
        colCharWidth = new int[cols];
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell();
            }
        }
        calculateCellSize();
    }

    public Table(BaseBarElement parent, int cols, int rows) {
        this.parent = parent;
        this.cols = cols;
        this.rows = rows;
        colWidth = new int[cols];
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell();
            }
        }
        calculateCellSize();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void render(Graphics2D g, int offsetX, int offsetY) {
        calculateCellBounds(offsetX, offsetY);
        findHighlightedCell();
        renderHighlightedCell(g);
        renderGrid(g, offsetX, offsetY);
        renderText(g);
    }

    private void calculateCellBounds(int x, int y) {
        int h = y;
        for (int i = 0; i < rows; i++) {
            int w = x;
            for (int j = 0; j < cols; j++) {
                cells[i][j].setBounds(w, h, colWidth[j], rowHeight);
                w += colWidth[j];
            }
            h += rowHeight;
        }
    }

    private void findHighlightedCell() {
        highlightedCol = -1;
        highlightedRow = -1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = (int) parent.animation.moveAnimation.renderX + cells[i][j].x;
                int y = (int) parent.animation.moveAnimation.renderY + cells[i][j].y;
                cells[i][j].highlighted =
                        CumberlessMouseListener.mouseX >= x && CumberlessMouseListener.mouseX < x + cells[i][j].width &&
                        CumberlessMouseListener.mouseY >= y && CumberlessMouseListener.mouseY < y + cells[i][j].height;
                if (cells[i][j].highlighted) {
                    highlightedCol = j;
                    highlightedRow = i;
                }
            }
        }
    }

    private void renderHighlightedCell(Graphics2D g) {
        if (highlightedCol == -1 || highlightedRow == -1) {
            return;
        }
        g.setColor(Util.blendColorKeepAlpha(parent.getBackgroundColor(), COLOR_BG_HIGHLIGHT));
        g.fillRect(cells[highlightedRow][highlightedCol].x, cells[highlightedRow][highlightedCol].y,
                   cells[highlightedRow][highlightedCol].width, cells[highlightedRow][highlightedCol].height);
    }

    private void renderGrid(Graphics2D g, int x, int y) {
        g.setColor(Util.blendColorKeepAlpha(parent.getBackgroundColor(), COLOR_LINE));
        for (int i = 0; i < rows + 1; i++) {
            int h = i * rowHeight;
            g.drawLine(x, y + h, x + width, y + h);
        }
        int w = 0;
        for (int j = 0; j < cols + 1; j++) {
            g.drawLine(x + w, y, x + w, y + height);
            w += j < cols ? colWidth[j] : 0;
        }
    }

    private void renderText(Graphics2D g) {
        g.setColor(COLOR_TEXT);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                g.drawString(cells[i][j].text, cells[i][j].x + CELL_PADDING_HORIZONTAL, cells[i][j].y + Engine.fontMetrics.getHeight() - 3 + CELL_PADDING_VERTICAL);
            }
        }
    }

    public void calculateCellSize() {
        width = 0;
        for (int j = 0; j < cols; j++) {
            colWidth[j] = MINIMUM_CELL_WIDTH;
            colCharWidth[j] = 0;
            for (int i = 0; i < rows; i++) {
                int width = cells[i][j].getWidth();
                if (width > colWidth[j]) {
                    colWidth[j] = width;
                }
                int charWidth = !Util.isEmpty(cells[i][j].text) ? cells[i][j].text.length() : 0;
                if (charWidth > colCharWidth[j]) {
                    colCharWidth[j] = charWidth;
                }
            }
            width += colWidth[j];
        }
        rowHeight = Engine.fontMetrics.getHeight() + (CELL_PADDING_VERTICAL * 2);
        height = rows * rowHeight;
    }

    public boolean click() {
        if (highlightedCol == -1 || highlightedRow == -1) {
            return false;
        }
        editedCell = cells[highlightedRow][highlightedCol];
        EditBox.showEditTable(this);
        return false;
    }

    public void setCellText(int col, int row, String text) {
        cells[row][col].text = text;
        calculateCellSize();
    }

    public String getEditedCellText() {
        return editedCell.text;
    }

    public void setEditedCellText(String text) {
        editedCell.text = text;
        calculateCellSize();
    }

    public StringBuilder buildFeature() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append(ElementHelper.EXPORT_INDENT).append(ElementHelper.EXPORT_INDENT);
            sb.append("  |");
            for (int j = 0; j < cols; j++) {
                if (!Util.isEmpty(cells[i][j].text)) {
                    sb.append(" ");
                    sb.append(cells[i][j].text);
                    sb.append(Util.fillChar(' ', colCharWidth[j] + 1 - cells[i][j].text.length()));
                } else {
                    sb.append(Util.fillChar(' ', colCharWidth[j] + 2));
                }
                sb.append("|");
            }
            sb.append("\n");
        }
        return sb;
    }

    private class Cell {
        String text = "";
        boolean highlighted;
        int x;
        int y;
        int width;
        int height;

        public int getWidth() {
            if (!Util.isEmpty(text)) {
                return Math.max(Engine.fontMetrics.stringWidth(text) + (CELL_PADDING_HORIZONTAL * 2), MINIMUM_CELL_WIDTH);
            } else {
                return MINIMUM_CELL_WIDTH;
            }
        }

        public void setBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
