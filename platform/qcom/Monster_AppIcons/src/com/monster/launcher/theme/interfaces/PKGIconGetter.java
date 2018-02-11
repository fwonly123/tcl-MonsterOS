package com.monster.launcher.theme.interfaces;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.Log;
import com.monster.launcher.theme.utils.PhotoUtils;
import com.monster.launcher.theme.utils.ResourceUtilites;
import com.monster.launcher.theme.utils.Utilites;

import java.util.HashMap;

/**
 * Created by antino on 16-11-8.
 */
public class PKGIconGetter extends IconGetterAbsImpl {
    private static final String THEME_PKG = "com.monster.launcher.theme";
    private PKGIconGetter(){}
    public PKGIconGetter(Context context){
//        init(context);
    }
    @Override
    public boolean init(Context cxt) {
        //first create context
        try{
            Context context = mPreContext = createThemeContext(cxt);
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            mIconDpi = activityManager.getLauncherLargeIconDensity();
            res = context.getResources();
            HashMap<String, String> packageClassMap = new HashMap<String, String>();
            final int resId = res.getIdentifier(Contents.LABEL_ICON_MAP_NAME, "array", THEME_PKG);
            final String[] packageClasseIcons = context.getResources()
                    .getStringArray(resId);
            for (String packageClasseIcon : packageClasseIcons) {
                Log.i(Contents.TAG,"packageClasseIcons = "+packageClasseIcon);
                String[] packageClasses_Icon = packageClasseIcon.split("#");
                if (packageClasses_Icon.length == 2) {
                    String[] packageClasses = packageClasses_Icon[0].split("\\|");
                    for (String s : packageClasses) {
                        packageClassMap.put(s.trim(), packageClasses_Icon[1]);
                        String[] packageClass = s.split("\\$");
                        if (packageClass.length == 2) {
                            packageClassMap.put(packageClass[0],
                                    packageClasses_Icon[1]);
                        }
                    }
                }
            }
            mLabel_Icons = packageClassMap;
            mPositionInBg.top = ResourceUtilites.getDimen(context,THEME_PKG,Contents.PTOP, 0);
            mPositionInBg.bottom = ResourceUtilites.getDimen(context,THEME_PKG,Contents.PBOTTOM, 0);
            mPositionInBg.right = ResourceUtilites.getDimen(context,THEME_PKG,Contents.PRIGHT,0 );
            mPositionInBg.left = ResourceUtilites.getDimen(context,THEME_PKG,Contents.PLEFT, 0);
            isHeteromorphicTheme = ResourceUtilites.getBoolean(context,THEME_PKG,Contents.HETEROMORPHIC_THEME, false);
        }catch (PackageManager.NameNotFoundException e){
            Log.e(Contents.TAG, "Create Res Apk Failed:NameNotFoundException.Please ensure the packageName = " + THEME_PKG + " is intalled.");
            return false;
        }catch (Exception e){
            Log.e(Contents.TAG, "Create Res Apk Failed:"+e.toString());
            return false;
        }
        return true;
    }

    private Context createThemeContext(Context context) throws Exception{
        Context themeContext = null;
        if(context!=null){
            if(context.getPackageName().equals(THEME_PKG)){
                Log.i(Contents.TAG,"Calling from theme app,so we needn't create context.");
                return context;
            }
            themeContext = context.createPackageContext(THEME_PKG, Context.CONTEXT_IGNORE_SECURITY);
        }
        return themeContext;
    }

    @Override
    protected Drawable getResurceDrawable(String iconName) {
        return ResourceUtilites.getIconDrawable(THEME_PKG, iconName, mPreContext, mIconDpi);
    }
}
