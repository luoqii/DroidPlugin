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
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

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

    public String getProcessName(String targetProcessName) {
        if (null != mHook){
            return mHook.getProcessName(targetProcessName);
        }
        return null;
    }

    public void setHook(Hook hook){
        mHook = hook;
    }

    public ServiceInfo selectStubServiceInfo(ServiceInfo targetInfo) {
        if (null != mHook){
            return mHook.selectStubServiceInfo(targetInfo);
        }
        return null;
    }

    public ProviderInfo selectStubProviderInfo(ProviderInfo targetInfo) {
        if (null != mHook){
            return mHook.selectStubProviderInfo(targetInfo);
        }
        return null;
    }


    public static interface Hook {
        public String getProcessName(String stubProcessName);

        // TODO add other select method
        public ActivityInfo selectStubActivityInfo(ActivityInfo targetActivityInfo);
        public ServiceInfo selectStubServiceInfo(ServiceInfo targetActivityInfo);
        public ProviderInfo selectStubProviderInfo(ProviderInfo targetActivityInfo);
    }
}
