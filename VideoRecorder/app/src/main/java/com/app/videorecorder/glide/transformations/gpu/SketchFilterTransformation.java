package com.app.videorecorder.glide.transformations.gpu;

/**
 * Copyright (C) 2015 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;

import com.app.videorecorder.glide.Glide;
import com.app.videorecorder.glide.gpuimage.GPUImageSketchFilter;
import com.app.videorecorder.glide.load.engine.bitmap_recycle.BitmapPool;

public class SketchFilterTransformation extends GPUFilterTransformation {

  public SketchFilterTransformation(Context context) {
    this(context, Glide.get(context).getBitmapPool());
  }

  public SketchFilterTransformation(Context context, BitmapPool pool) {
    super(context, pool, new GPUImageSketchFilter());
  }

  @Override
  public String getId() {
    return "SketchFilterTransformation()";
  }
}
