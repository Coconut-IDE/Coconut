package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.quickfixes.AdaptCodeToAnnotationQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.ModifyFieldValueAndCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataAPI;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataSourceAPIList;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataGroup;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 1/27/18.
 */
public class UniqueIdentifierAnnotationUtil extends PersonalDataAPIAnnotationUtil {

    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.UniqueIdentifierAnnotation);
        String UIDType = null;
        String scope = null;
        String resettability = null;
        PersonalDataAPI[] UIDAPIList = PersonalDataSourceAPIList.getAPIListByDataGroup(PersonalDataGroup.UNIQUE_IDENTIFIER, false);
        for (PersonalDataAPI api : UIDAPIList) {
            if (api.psiElementMethodCallMatched(source)) {
                if ("Android ID".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", android_id);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", reset_after_factory_reset);
                } else if ("UUID".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", self_generated_uid);
                } else if ("GUID (Customed globally unique ID)".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", self_generated_uid);
                    scope = String.format("UIDScope.%s", per_app_scope);
                } else if ("IMEI for GSM phone, MEID for CDMA phones".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", telephony_device_id);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", persist_after_factory_reset);
                } else if ("IMEI".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", telephony_device_id);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", persist_after_factory_reset);
                } else if ("MEID".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", telephony_device_id);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", persist_after_factory_reset);
                } else if ("Wi-Fi MAC Address".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", mac_address);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", persist_after_factory_reset);
                } else if ("Bluetooth MAC Address".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", mac_address);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", persist_after_factory_reset);
                } else if ("Line1(Phone) number".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", line1_phone_number);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", persist_after_factory_reset);
                } else if ("Google Instance ID".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", instance_id);
                    scope = String.format("UIDScope.%s", per_app_scope);
                    resettability = String.format("UIDResettability.%s", reset_when_reinstall);
                } else if ("Google Advertising ID".equals(api.getDisplayName())) {
                    UIDType = String.format("UIDType.%s", advertising_id);
                    scope = String.format("UIDScope.%s", per_device_scope);
                    resettability = String.format("UIDResettability.%s", user_resettable_in_system_settings);
                }
            }
        }
        if (UIDType != null) {
            annotationHolder.put("uidType", UIDType);
        }
        if (scope != null) {
            annotationHolder.put("scope", scope);
        }
        if (resettability != null) {
            annotationHolder.put("resettability", resettability);
        }
        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    private void useInstanceID(PsiMethodCallExpression callExpression) {
        PsiElementFactory javaFactory = JavaPsiFacade.getInstance(callExpression.getProject()).getElementFactory();
        PsiJavaFile currentFile = PsiTreeUtil.getParentOfType(callExpression, PsiJavaFile.class);
        if (currentFile != null) {
            PsiImportList [] importList = PsiTreeUtil.getChildrenOfType(currentFile, PsiImportList.class);
            if (importList != null && importList.length > 0) {
                PsiImportList firstImportList = importList[0];
                firstImportList.add(javaFactory.createImportStatementOnDemand("com.google.android.gms.iid"));
            }
        }
        callExpression.replace(javaFactory.createExpressionFromText("InstanceID.getInstance(CONTEXT_PLACEHOLDER).getId()", null));
        File file = new File(String.format("%s/app/build.gradle", callExpression.getProject().getBasePath()));
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (virtualFile == null) {
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(callExpression.getProject()).findFile(virtualFile);
        GrMethodCallExpressionImpl[] grMethodCallExpressions = PsiTreeUtil.getChildrenOfType(psiFile, GrMethodCallExpressionImpl.class);
        if (grMethodCallExpressions == null) {
            return;
        }
        for (GrMethodCallExpressionImpl grMethodCallExpression : grMethodCallExpressions) {
            if (Pattern.compile(".*dependencies.*", Pattern.DOTALL).matcher(grMethodCallExpression.getText()).matches()) {
                GrApplicationStatement[] statements = PsiTreeUtil.getChildrenOfType(grMethodCallExpression.getClosureArguments()[0], GrApplicationStatement.class);
                if (statements == null || statements.length == 0) {
                    continue;
                }
                GroovyPsiElementFactory groovyFactory = GroovyPsiElementFactory.getInstance(callExpression.getProject());
                // TODO: (urgent) check whether this lib is already in the gradle file
                PsiElement newLib = groovyFactory.createStatementFromText("compile 'com.google.android.gms:play-services-iid:11.8.0'");
                assert statements[statements.length - 1] != null;
                statements[statements.length - 1].getParent().addBefore(newLib, statements[statements.length - 1]);
            }
        }
    }

    private void useUUID(PsiMethodCallExpression callExpression) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(callExpression.getProject()).getElementFactory();
        PsiJavaFile currentFile = PsiTreeUtil.getParentOfType(callExpression, PsiJavaFile.class);
        if (currentFile != null) {
            PsiImportList [] importList = PsiTreeUtil.getChildrenOfType(currentFile, PsiImportList.class);
            if (importList != null && importList.length > 0) {
                PsiImportList firstImportList = importList[0];
                firstImportList.add(factory.createImportStatementOnDemand("java.util"));
            }
        }
        callExpression.replace(factory.createExpressionFromText("UUID.randomUUID()", null));
    }

    @Nullable
    @Override
    public LocalQuickFix [] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        if (fieldValue == null || fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        if (String.format("UIDType.%s", self_generated_uid).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use Self-generated ID (reset when reinstall, app-specific)", methodCallExpression,
                    this::useUUID));
        } else if (String.format("UIDType.%s", instance_id).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use Google Instance ID (reset when reinstall, app-specific)", methodCallExpression,
                    this::useInstanceID));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        if (fieldValue == null || fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        if (String.format("UIDType.%s", self_generated_uid).equals(value)) {
            localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use Self-generated ID (reset when reinstall, app-specific)", methodCallExpression, nameValuePair, fieldValue,
                    this::useUUID));
        } else if (String.format("UIDType.%s", instance_id).equals(value)) {
            localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use Google Instance ID (reset when reinstall, app-specific)", methodCallExpression, nameValuePair, fieldValue,
                    this::useInstanceID));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, HashMap<PsiNameValuePair, ArrayList<String>> annotationFieldChangeMap) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        String targetUIDType = null;
        for (Map.Entry<PsiNameValuePair, ArrayList<String>> entry : annotationFieldChangeMap.entrySet()) {
            PsiNameValuePair nameValuePair = entry.getKey();
            ArrayList<String> targetValueList = entry.getValue();
            if ("uidType".equals(nameValuePair.getName())) {
                targetUIDType = targetValueList.get(0);
            }
        }
        if (targetUIDType == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        if (String.format("UIDType.%s", self_generated_uid).equals(targetUIDType)) {
            localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use Self-generated ID (reset when reinstall, app-specific)", methodCallExpression, annotationFieldChangeMap,
                    this::useUUID));
        } else if (String.format("UIDType.%s", instance_id).equals(targetUIDType)) {
            localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use Google Instance ID (reset when reinstall, app-specific)", methodCallExpression, annotationFieldChangeMap,
                    this::useInstanceID));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }
}
