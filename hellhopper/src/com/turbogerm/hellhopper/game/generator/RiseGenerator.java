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
package com.turbogerm.hellhopper.game.generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.turbogerm.hellhopper.ResourceNames;
import com.turbogerm.hellhopper.dataaccess.PlatformData;
import com.turbogerm.hellhopper.dataaccess.RiseSectionData;
import com.turbogerm.hellhopper.dataaccess.RiseSectionDataReader;
import com.turbogerm.hellhopper.game.Rise;
import com.turbogerm.hellhopper.game.platforms.PlatformBase;
import com.turbogerm.hellhopper.game.platforms.PlatformFactory;

public final class RiseGenerator {
    
    private static final int RISE_SECTIONS_INITIAL_CAPACITY = 20;
    
    public static Rise generate(AssetManager assetManager) {
        Array<RiseSectionData> riseSections = new Array<RiseSectionData>(true, RISE_SECTIONS_INITIAL_CAPACITY);
//        riseSections.add(RiseSectionGenerator.generateBasicRiseSection(60, 60, 0, 0.1f));
//        riseSections.add(RiseSectionGenerator.generateBasicRiseSection(60, 45, 0, 0.1f));
//        riseSections.add(RiseSectionGenerator.generateBasicRiseSection(60, 30, 1, 0.1f));
//        riseSections.add(RiseSectionGenerator.generateBasicRiseSection(60, 20, 1, 0.1f));
//        riseSections.add(RiseSectionGenerator.generateBasicRiseSection(60, 0, 1, 0.1f));
        
//        RiseSectionData testRiseSection = RiseSectionDataReader.read(
//                Gdx.files.internal(ResourceNames.RISE_SECTION_TEST));
//        riseSections.add(testRiseSection);
        
        riseSections.add(RiseSectionGenerator.generateSpreadOutRiseSection(120,
                PlatformData.MAX_PLATFORM_DISTANCE_STEPS, PlatformData.MAX_PLATFORM_DISTANCE_STEPS,
                1.0f, 6.0f, 10.0f, 1.0f, 0.1f, 2));
        
        int totalNumPlatforms = 0;
        for (RiseSectionData riseSection : riseSections) {
            totalNumPlatforms += riseSection.getPlatformDataList().size;
        }
        
        Array<PlatformBase> platforms = new Array<PlatformBase>(true, totalNumPlatforms);
        int startStep = 0;
        for (RiseSectionData riseSection : riseSections) {
            addPlatforms(platforms, riseSection, startStep, assetManager);
            startStep += riseSection.getStepRange();
        }
        
        float riseHeight = startStep * PlatformData.STEP_HEIGHT;
        
        return new Rise(riseHeight, platforms);
    }
    
    private static void addPlatforms(Array<PlatformBase> platforms, RiseSectionData riseSection, int startStep,
            AssetManager assetManager) {
        Array<PlatformData> platformDataList = riseSection.getPlatformDataList();
        for (PlatformData platformData : platformDataList) {
            PlatformBase platform = PlatformFactory.create(platformData, startStep, assetManager);
            platforms.add(platform);
        }
    }
}