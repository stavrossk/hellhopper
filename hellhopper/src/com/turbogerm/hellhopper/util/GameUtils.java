/*
 * Copyright (c) 2013 Goran Mrzljak
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.turbogerm.hellhopper.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public final class GameUtils {
    
    /* Math */
    public static float getPositiveModulus(float value, float mod) {
        return ((value % mod) + mod) % mod;
    }
    
    public static int getPositiveModulus(int value, int mod) {
        return ((value % mod) + mod) % mod;
    }
    
    public static Array<Integer> getRandomIndexes(int range, int numberOfIndexes) {
        Array<Integer> selectedList = new Array<Integer>(numberOfIndexes);
        
        Array<Integer> availableList = new Array<Integer>(true, range);
        for (int i = 0; i < range; i++) {
            availableList.add(i);
        }
        
        for (int i = 0; i < numberOfIndexes; i++) {
            int selectedIndex = MathUtils.random(availableList.size - 1);
            int selected = availableList.get(selectedIndex);
            selectedList.add(selected);
            availableList.removeIndex(selectedIndex);
        }
        
        selectedList.sort();
        
        return selectedList;
    }
    
    /* Graphics */
    public static void setSpriteAlpha(Sprite sprite, float alpha) {
        Color c = sprite.getColor();
        sprite.setColor(c.r, c.g, c.b, alpha);
    }
}
