/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.nbmediation.sdk.mobileads;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

public class PluginApplication extends Application {

    private static PluginApplication sInstance;

    public boolean isOnCreate;

    public static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        sInstance = this;
        isOnCreate = true;
        super.onCreate();
    }

    public static PluginApplication getInstance() {
        return sInstance;
    }

}
