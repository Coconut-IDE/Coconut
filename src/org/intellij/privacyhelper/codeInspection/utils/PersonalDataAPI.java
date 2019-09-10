package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

import java.util.*;
import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.coarse_grained_latitude_longitude;
import static org.intellij.privacyhelper.codeInspection.utils.Constants.fine_grained_latitude_longitude;

/**
 * Created by tianshi on 11/13/17.
 */
public abstract class PersonalDataAPI {
    PersonalDataAPIType personalDataAPIType;
    public PersonalDataAPIAnnotationUtil config;
    PersonalTargetVariableTracker targetVariableTracker;

    String displayName;
    public String fullAPINamePattern;
    public String returnValueTypeCanonicalText;
    public String [] parameterTypeCanonicalTextRestriction;
    public String [] parameterValueTextRestriction;

    public AndroidPermission [][] requiredPermissions;
    public AndroidPermission [][] libImplicitVoluntaryPermissions; // will access the data implicitly

    public boolean isThirdPartyAPI;
    PersonalDataGroup [] groups;
    private CoconutAnnotationType annotationType;

    boolean isPersonalDataSource;
    boolean isPersonalDataSink;

    /**
     *
     * @param annotationType
     * @param personalDataAPIType
     * @param displayName string representing the name of the given API
     * @param fullAPINamePattern
     * @param returnValueTypeCanonicalText
     * @param parameterTypeCanonicalTextRestriction
     * @param parameterValueTextRestriction
     * @param requiredPermissions
     * @param libImplicitVoluntaryPermissions
     * @param isThirdPartyAPI
     * @param groups
     * @param config
     * @param targetVariableTracker
     * @param isPersonalDataSource
     * @param isPersonalDataSink
     */
    PersonalDataAPI(CoconutAnnotationType annotationType, PersonalDataAPIType personalDataAPIType,
                    String displayName, String fullAPINamePattern, String returnValueTypeCanonicalText,
                    String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                    AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                    boolean isThirdPartyAPI,
                    PersonalDataGroup[] groups,
                    PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                    boolean isPersonalDataSource, boolean isPersonalDataSink) {
        this.annotationType = annotationType;
        this.personalDataAPIType = personalDataAPIType;
        this.displayName = displayName;
        this.fullAPINamePattern = fullAPINamePattern;
        this.returnValueTypeCanonicalText = returnValueTypeCanonicalText;
        this.parameterTypeCanonicalTextRestriction = parameterTypeCanonicalTextRestriction;
        this.parameterValueTextRestriction = parameterValueTextRestriction;
        this.requiredPermissions = requiredPermissions;
        this.libImplicitVoluntaryPermissions = libImplicitVoluntaryPermissions;
        this.isThirdPartyAPI = isThirdPartyAPI;
        this.groups = groups;
        this.config = config;
        config.setAPI(this);
        this.targetVariableTracker = targetVariableTracker;
        this.isPersonalDataSource = isPersonalDataSource;
        this.isPersonalDataSink = isPersonalDataSink;
    }

    public boolean isPersonalDataSource() {
        return isPersonalDataSource;
    }

    public boolean isPersonalDataSink() {
        return isPersonalDataSink;
    }

    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        return config.createAnnotationInferenceFromSource(source);
    }

    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return config.createAnnotationInferencesFromSource(source);
    }

    public PsiElement getTargetVariable(PsiElement source) {
        return targetVariableTracker.getTargetVariable(source);
    }

    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return targetVariableTracker.getResolvedTargetVariable(source);
    }

    public PersonalDataGroup [] getGroups() {
        return groups;
    }

    public boolean permissionUsedInAPI (AndroidPermission permission) {
        return explicitlyUsedRequiredPermissionInAPI(permission) || implicitlyUsedVoluntaryPermissionInAPI(permission);
    }

    public boolean explicitlyUsedRequiredPermissionInAPI(AndroidPermission permission) {
        for (AndroidPermission [] requriedPermissionGroup : requiredPermissions) {
            for (AndroidPermission requiredPermission : requriedPermissionGroup) {
                if (permission.equals(requiredPermission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean implicitlyUsedVoluntaryPermissionInAPI(AndroidPermission permission) {
        for (AndroidPermission [] permissionGroup : libImplicitVoluntaryPermissions) {
            for (AndroidPermission permission1 : permissionGroup) {
                if (permission.equals(permission1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Compares a PsiElement in the form of a method call to this object and determines if they are the
     * same method call.
     *
     * @param dataElement The method call we're attempting check
     * @return boolean - true if they are the same method, false otherwise
     */
    public boolean psiElementMethodCallMatched(PsiElement dataElement) {
        //Checks to see if PsiElement is a method call
        if (dataElement instanceof PsiMethodCallExpression) {
            //If so parses to PsiMethodCallExpression
            PsiMethodCallExpression expression = (PsiMethodCallExpression) dataElement;
            //Uses another method to determine if the method call matches
            return CodeInspectionUtil.checkMatchedMethodCall(expression, returnValueTypeCanonicalText, fullAPINamePattern,
                    parameterTypeCanonicalTextRestriction, parameterValueTextRestriction);
        } else {
            // If either the dataElement is not a method call, method call does
            // not match this API, or the annotation is not correct, we return false
            return false;
        }
    }

    public CoconutAnnotationType getAnnotationType() {
        return annotationType;
    }

    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return config.getAdaptCodeToAnnotationQuickfix(methodCallExpression, fieldName, fieldValue);
    }

    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<PersonalDataInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        return config.getAdaptCodeToAnnotationQuickfix(instances, fieldName, fieldValue);
    }

    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression,
            PsiNameValuePair nameValuePair, ArrayList<String> targetFieldValue) {
        return config.getModifyFieldValueAndCodeQuickfixList(methodCallExpression, nameValuePair, targetFieldValue);
    }

    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<PersonalDataInstance> instances,
                                                                  PsiNameValuePair nameValuePair, ArrayList<String> targetFieldValue) {
        return config.getModifyFieldValueAndCodeQuickfixList(instances, nameValuePair, targetFieldValue);
    }

    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression,
                                                                  HashMap<PsiNameValuePair, ArrayList<String>> annotationFieldChangeMap) {
        return config.getModifyFieldValueAndCodeQuickfixList(methodCallExpression, annotationFieldChangeMap);
    }

    public boolean targetVariableFromCallback() {
        return (personalDataAPIType == PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK);
    }

    public boolean targetVariableFromIntent() {
        return (personalDataAPIType == PersonalDataAPIType.PERSONAL_DATA_FROM_INTENT);
    }

}

