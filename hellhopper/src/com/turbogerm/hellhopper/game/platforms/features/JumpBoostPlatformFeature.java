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
package com.turbogerm.hellhopper.game.platforms.features;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.turbogerm.hellhopper.ResourceNames;
import com.turbogerm.hellhopper.dataaccess.PlatformData;
import com.turbogerm.hellhopper.dataaccess.PlatformFeatureData;
import com.turbogerm.hellhopper.game.CollisionEffect;
import com.turbogerm.hellhopper.game.GameCharacter;
import com.turbogerm.hellhopper.util.GameUtils;

final class JumpBoostPlatformFeature extends PlatformFeatureBase {
    
    private static final float CRATER_LOW_WIDTH = 0.5f;
    private static final float CRATER_MEDIUM_WIDTH = 0.75f;
    private static final float CRATER_HIGH_WIDTH = 1.0f;
    private static final float CRATER_HEIGHT = 0.2f;
    
    private static final float DISCHARGE_LOW_WIDTH = 0.6f;
    private static final float DISCHARGE_LOW_HEIGHT = 0.5f;
    private static final float DISCHARGE_MEDIUM_WIDTH = 0.9f;
    private static final float DISCHARGE_MEDIUM_HEIGHT = 0.75f;
    private static final float DISCHARGE_HIGH_WIDTH = 1.2f;
    private static final float DISCHARGE_HIGH_HEIGHT = 1.0f;
    
    private static final float LOW_POWER_MULTIPLIER = 1.3f;
    private static final float MEDIUM_POWER_MULTIPLIER = 1.6f;
    private static final float HIGH_POWER_MULTIPLIER = 1.9f;
    
    private static final float DISCHARGE_DURATION = 0.4f;
    
    private final Sprite mCraterSprite;
    private final Vector2 mCraterOffset;
    
    private final Sprite mDischargeSprite;
    private final Vector2 mDischargeInitialOffset;
    private float mDischargeElapsed;
    
    private final float mCraterWidth;
    private final float mJumpBoostSpeed;
    
    public JumpBoostPlatformFeature(PlatformFeatureData featureData, AssetManager assetManager) {
        
        String powerString = featureData.getProperty(PlatformFeatureData.JUMP_BOOST_POWER_PROPERTY);
        JumpPowerData powerData = getJumpPowerData(powerString);
        
        mCraterWidth = powerData.craterWidth;
        
        Texture craterTexture = assetManager.get(powerData.craterTextureName);
        mCraterSprite = new Sprite(craterTexture);
        mCraterSprite.setSize(mCraterWidth, CRATER_HEIGHT);
        
        Texture dischargeTexture = assetManager.get(powerData.dischargeTextureName);
        mDischargeSprite = new Sprite(dischargeTexture);
        mDischargeSprite.setSize(powerData.dischargeWidth, powerData.dischargeHeight);
        
        mJumpBoostSpeed = powerData.speed;
        
        float positionFraction = Float.parseFloat(featureData
                .getProperty(PlatformFeatureData.JUMP_BOOST_POSITION_PROPERTY));
        mCraterOffset = new Vector2(
                (PlatformData.PLATFORM_WIDTH - mCraterWidth) * positionFraction,
                PlatformData.PLATFORM_HEIGHT);
        
        mDischargeInitialOffset = new Vector2(
                mCraterOffset.x + (mCraterWidth - powerData.dischargeWidth) / 2.0f,
                mCraterOffset.y + CRATER_HEIGHT);
        
        mDischargeElapsed = DISCHARGE_DURATION;
    }
    
    @Override
    public void render(SpriteBatch batch, Vector2 platformPosition, float alpha, float delta) {
        
        if (mDischargeElapsed < DISCHARGE_DURATION) {
            mDischargeElapsed += delta;
            
            float dischargeAlpha = 1.0f - mDischargeElapsed / DISCHARGE_DURATION;
            
            mDischargeSprite.setPosition(
                    platformPosition.x + mDischargeInitialOffset.x,
                    platformPosition.y + mDischargeInitialOffset.y);
            mDischargeSprite.draw(batch, dischargeAlpha);
        }
        
        mCraterSprite.setPosition(
                platformPosition.x + mCraterOffset.x,
                platformPosition.y + mCraterOffset.y);
        GameUtils.setSpriteAlpha(mCraterSprite, alpha);
        mCraterSprite.draw(batch);
    }
    
    @Override
    public boolean isContact(float relativeCollisionPointX) {
        float charX1 = relativeCollisionPointX + GameCharacter.COLLISION_WIDTH_OFFSET;
        float charX2 = charX1 + GameCharacter.COLLISION_WIDTH;
        
        float featureX1 = mCraterOffset.x;
        float featureX2 = featureX1 + mCraterWidth;
        
        // http://eli.thegreenplace.net/2008/08/15/intersection-of-1d-segments/
        return charX2 >= featureX1 && featureX2 >= charX1;
    }
    
    @Override
    public void applyContact(CollisionEffect collisionEffect) {
        collisionEffect.set(CollisionEffect.JUMP_BOOST, mJumpBoostSpeed);
        startDischarge();
    }
    
    private void startDischarge() {
        mDischargeElapsed = 0.0f;
    }
    
    private static JumpPowerData getJumpPowerData(String powerString) {
        
        if (PlatformFeatureData.JUMP_BOOST_POWER_LOW_PROPERTY_VALUE.equals(powerString)) {
            return new JumpPowerData(
                    ResourceNames.PLATFORM_JUMP_BOOST_CRATER_LOW_TEXTURE,
                    ResourceNames.PLATFORM_JUMP_BOOST_DISCHARGE_LOW_TEXTURE,
                    CRATER_LOW_WIDTH,
                    DISCHARGE_LOW_WIDTH,
                    DISCHARGE_LOW_HEIGHT,
                    GameCharacter.JUMP_SPEED * LOW_POWER_MULTIPLIER);
        } else if (PlatformFeatureData.JUMP_BOOST_POWER_MEDIUM_PROPERTY_VALUE.equals(powerString)) {
            return new JumpPowerData(
                    ResourceNames.PLATFORM_JUMP_BOOST_CRATER_MEDIUM_TEXTURE,
                    ResourceNames.PLATFORM_JUMP_BOOST_DISCHARGE_MEDIUM_TEXTURE,
                    CRATER_MEDIUM_WIDTH,
                    DISCHARGE_MEDIUM_WIDTH,
                    DISCHARGE_MEDIUM_HEIGHT,
                    GameCharacter.JUMP_SPEED * MEDIUM_POWER_MULTIPLIER);
        } else {
            return new JumpPowerData(
                    ResourceNames.PLATFORM_JUMP_BOOST_CRATER_HIGH_TEXTURE,
                    ResourceNames.PLATFORM_JUMP_BOOST_DISCHARGE_HIGH_TEXTURE,
                    CRATER_HIGH_WIDTH,
                    DISCHARGE_HIGH_WIDTH,
                    DISCHARGE_HIGH_HEIGHT,
                    GameCharacter.JUMP_SPEED * HIGH_POWER_MULTIPLIER);
        }
    }
    
    private static class JumpPowerData {
        final String craterTextureName;
        final String dischargeTextureName;
        final float craterWidth;
        final float dischargeWidth;
        final float dischargeHeight;
        final float speed;
        
        public JumpPowerData(
                String craterTextureName,
                String dischargeTextureName,
                float craterWidth,
                float dischargeWidth,
                float dischargeHeight,
                float speed) {
            
            this.craterTextureName = craterTextureName;
            this.dischargeTextureName = dischargeTextureName;
            this.craterWidth = craterWidth;
            this.dischargeWidth = dischargeWidth;
            this.dischargeHeight = dischargeHeight;
            this.speed = speed;
        }
    }
}
