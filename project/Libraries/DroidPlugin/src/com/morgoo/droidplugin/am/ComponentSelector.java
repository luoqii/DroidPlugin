/*
**        DroidPlugin Project
**
** Copyright(c) 2015 bb.S <bangbang.song@gmail.com>
**
** This file is part of DroidPlugin.
**
** DroidPlugin is free software: you can redistribute it and/or
** modify it under the terms of the GNU Lesser General Public
** License as published by the Free Software Foundation, either
** version 3 of the License, or (at your option) any later version.
**
** DroidPlugin is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public
** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
**
**/


package com.morgoo.droidplugin.am;

import android.content.pm.ActivityInfo;

/**
 * Created by bb.S on 15-11-23.
 */
public class ComponentSelector {
    private static ComponentSelector sInstance;
    
    private Hook mHook;

    private ComponentSelector(){

    }

    public static ComponentSelector getInsance(){
        if (null == sInstance){
            sInstance = new ComponentSelector();
        }

        return sInstance;
    }

    public ActivityInfo selectStubActivityInfo(ActivityInfo targetActivityInfo) {
        ActivityInfo ai = null;
        if (null != mHook){
            ai = mHook.selectStubActivityInfo(targetActivityInfo);
        }
        
        return ai;
    }

    public void setHook(Hook hook){
        mHook = hook;
    }

    public static interface Hook {
        // TODO add other select method
        public ActivityInfo selectStubActivityInfo(ActivityInfo targetActivityInfo);
    }
}
