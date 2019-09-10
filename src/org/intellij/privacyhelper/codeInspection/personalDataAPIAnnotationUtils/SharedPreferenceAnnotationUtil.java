package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by tianshi on 2/2/18.
 */
public class SharedPreferenceAnnotationUtil extends PersonalDataAPIAnnotationUtil {
//    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//    SharedPreferences.Editor editor = settings.edit();
//      editor.putBoolean("silentMode", mSilentMode);
//
//    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//    settings.edit().putBoolean("silentMode", mSilentMode);
//
//    getSharedPreferences(PREFS_NAME, 0).edit().putBoolean("silentMode", mSilentMode);

    private static final Map<String, Integer> accessModeParameterPositionMap =
            Collections.unmodifiableMap(new HashMap<String, Integer>() {{
                put(".*getSharedPreferences", 1);
                put(".*getPreferences", 0);
                put(".*getDefaultSharedPreferences", -1);
                put(".*setSharedPreferencesMode", 0);
            }});

    private static final String[] relevantVariableTypeList = new String[] {".*SharedPreferences\\.Editor", ".*SharedPreferences", ".*PreferenceManager"};


    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        return StorageUtils.createAPIAnnotationSpeculationByModeParameter(source, relevantVariableTypeList, accessModeParameterPositionMap);
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix [] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return null;
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }
}
