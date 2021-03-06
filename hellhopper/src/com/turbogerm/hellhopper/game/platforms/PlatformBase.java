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
package com.turbogerm.hellhopper.game.platforms;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.turbogerm.hellhopper.ResourceNames;
import com.turbogerm.hellhopper.dataaccess.PlatformData;
import com.turbogerm.hellhopper.dataaccess.PlatformFeatureData;
import com.turbogerm.hellhopper.game.CollisionEffect;
import com.turbogerm.hellhopper.game.GameCharacter;
import com.turbogerm.hellhopper.game.GameArea;
import com.turbogerm.hellhopper.game.PlatformToCharCollisionData;
import com.turbogerm.hellhopper.game.platforms.features.PlatformFeatureBase;
import com.turbogerm.hellhopper.game.platforms.features.PlatformFeatureFactory;
import com.turbogerm.hellhopper.game.platforms.movement.PlatformMovementBase;
import com.turbogerm.hellhopper.game.platforms.movement.PlatformMovementFactory;
import com.turbogerm.hellhopper.util.GameUtils;
import com.turbogerm.hellhopper.util.Pools;

public abstract class PlatformBase {
    
    protected final Sprite mSprite;
    protected float mAlpha;
    
    private final PlatformMovementBase mPlatformMovement;
    private final boolean mHasVerticalMovement;
    
    private final Array<PlatformFeatureBase> mPlatformFeatures;
    
    public PlatformBase(PlatformData platformData, Vector2 initialPosition, AssetManager assetManager) {
        
        Texture texture = assetManager.get(getTexturePath(platformData));
        mSprite = new Sprite(texture);
        mSprite.setBounds(initialPosition.x, initialPosition.y,
                PlatformData.PLATFORM_WIDTH, PlatformData.PLATFORM_HEIGHT);
        mAlpha = 1.0f;
        
        mPlatformMovement = PlatformMovementFactory.create(platformData.getMovementData(), initialPosition,
                assetManager);
        mHasVerticalMovement = mPlatformMovement.hasVerticalMovement();
        
        mPlatformFeatures = getPlatformFeatures(platformData.getFeaturesData(), assetManager);
    }
    
    public final void update(float delta, Vector2 c1, Vector2 c2, PlatformToCharCollisionData collisionData) {
        // if platform can move up, additional platform to char collision must be checked
        if (mHasVerticalMovement && collisionData.isEnabled) {
            Vector2 position = getPosition();
            Vector2 p1 = Pools.obtainVector();
            p1.set(position.x, position.y + PlatformData.PLATFORM_HEIGHT);
            
            updateImpl(delta, c1, c2, collisionData);
            
            position = getPosition();
            Vector2 p2 = Pools.obtainVector();
            p2.set(position.x, position.y + PlatformData.PLATFORM_HEIGHT);
            
            // only check for collision when platform is going up, and character is going down
            if (p2.y > p1.y) {
                collisionData.isCollision = Intersector.intersectSegments(
                        c1, c2, p1, p2, collisionData.collisionPoint);
                if (collisionData.isCollision) {
                    collisionData.collisionPlatform = this;
                    collisionData.collisionPoint.y = p2.y;
                }
            }
            
            Pools.freeVector(p1);
            Pools.freeVector(p2);
        } else {
            updateImpl(delta, c1, c2, collisionData);
        }
    }
    
    protected void updateImpl(float delta, Vector2 c1, Vector2 c2, PlatformToCharCollisionData collisionData) {
        if (isMovingInternal()) {
            mPlatformMovement.updatePosition(delta);
        }
    }
    
    public final void render(SpriteBatch batch, float delta) {
        
        if (mPlatformFeatures != null) {
            for (PlatformFeatureBase platformFeature : mPlatformFeatures) {
                platformFeature.render(batch, getPosition(), mAlpha, delta);
            }
        }
        renderImpl(batch, delta);
        mPlatformMovement.render(batch, mAlpha, isMovingInternal() ? delta : 0.0f);
    }
    
    protected void renderImpl(SpriteBatch batch, float delta) {
        Vector2 position = getPosition();
        mSprite.setPosition(position.x, position.y);
        GameUtils.setSpriteAlpha(mSprite, mAlpha);
        mSprite.draw(batch);
    }
    
    public boolean isCollision(Vector2 c1, Vector2 c2, Vector2 intersection) {
        
        Vector2 position = getPosition();
        Vector2 p1 = Pools.obtainVector();
        Vector2 p2 = Pools.obtainVector();
        
        float pY = position.y + PlatformData.PLATFORM_HEIGHT;
        p1.set(position.x - GameCharacter.COLLISION_LINE_LENGTH, pY);
        p2.set(position.x + PlatformData.PLATFORM_WIDTH - GameCharacter.COLLISION_WIDTH_OFFSET, pY);
        
        boolean isIntersection = Intersector.intersectSegments(c1, c2, p1, p2, intersection);
        
        Pools.freeVector(p1);
        Pools.freeVector(p2);
        
        return isIntersection;
    }
    
    public boolean isActive(float visibleAreaPosition, float activePlatformsAreaPadding) {
        if (!isActiveInternal()) {
            return false;
        }
        
        Vector2 position = getPosition();
        float activeRangeLower = visibleAreaPosition - PlatformData.PLATFORM_HEIGHT - activePlatformsAreaPadding;
        float activeRangeUpper = visibleAreaPosition + GameArea.GAME_AREA_HEIGHT + activePlatformsAreaPadding;
        return position.y >= activeRangeLower && position.y <= activeRangeUpper;
    }
    
    protected boolean isActiveInternal() {
        return true;
    }
    
    protected boolean isMovingInternal() {
        return true;
    }
    
    public void fillCollisionEffect(float collisionPointX, CollisionEffect collisionEffect) {
        if (mPlatformFeatures != null) {
            float relativeCollisionPointX = collisionPointX - getPosition().x;
            for (PlatformFeatureBase feature : mPlatformFeatures) {
                if (feature.isContact(relativeCollisionPointX)) {
                    feature.applyContact(collisionEffect);
                    return;
                }
            }
        }
        
        collisionEffect.set(CollisionEffect.NONE);
    }
    
    public Vector2 getPosition() {
        return mPlatformMovement.getPosition();
    }
    
    private static String getTexturePath(PlatformData platformData) {
        String platformType = platformData.getPlatformType();
        if (PlatformData.CRUMBLE.equals(platformType)) {
            return ResourceNames.PLATFORM_CRUMBLE_TEXTURE;
        } else if (PlatformData.FLAME.equals(platformType)) {
            return ResourceNames.getRandomPlatformFlameTexture();
        } else {
            return ResourceNames.getRandomPlatformNormalTexture();
        }
    }
    
    private static Array<PlatformFeatureBase> getPlatformFeatures(Array<PlatformFeatureData> featuresData,
            AssetManager assetManager) {
        
        if (featuresData == null) {
            return null;
        }
        
        Array<PlatformFeatureBase> platformFeatures = new Array<PlatformFeatureBase>(true, featuresData.size);
        for (PlatformFeatureData featureData : featuresData) {
            PlatformFeatureBase platformFeature = PlatformFeatureFactory.create(featureData, assetManager);
            platformFeatures.add(platformFeature);
        }
        
        return platformFeatures;
    }
}
