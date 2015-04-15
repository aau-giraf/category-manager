/*
 * Copyright 2014 Alex Curran
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
 * NOTICE: This file has been modified in order to enable custom size of the showcase and
 * custom positioning of text.
 */

package com.github.amlcurran.showcaseview.targets;

import android.app.Activity;
import android.graphics.Point;
import android.view.View;

/**
 * Target a view on the screen. This will centre the target on the view.
 */
public class ViewTarget extends Target {

    private final View mView;

    public ViewTarget(final View view) {
        this(view, 1.5f);
    }

    public ViewTarget(final int viewId, final Activity activity) {
        this(viewId, activity, 1.5f);
    }

    public ViewTarget(final View view, final float scaleMultiplier) {
        super(scaleMultiplier);
        mView = view;
    }

    public ViewTarget(final int viewId, final Activity activity, final float scaleMultiplier) {
        super(scaleMultiplier);
        mView = activity.findViewById(viewId);
    }

    @Override
    public Point getPoint() {
        final int[] location = new int[2];
        mView.getLocationInWindow(location);
        int x = location[0] + mView.getWidth() / 2;
        int y = location[1] + mView.getHeight() / 2;
        return new Point(x, y);
    }

    @Override
    public float getRadius() {
        return Math.max(mView.getMeasuredWidth(),  mView.getMeasuredHeight()) / 2;
    }
}
