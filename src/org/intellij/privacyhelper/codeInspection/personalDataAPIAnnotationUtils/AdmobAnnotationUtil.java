package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.AdaptCodeToAnnotationQuickfix;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataAPI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 2/4/18.
 */
public class AdmobAnnotationUtil extends PersonalDataAPIAnnotationUtil {
//    AdRequest request = new AdRequest.Builder()
//            .build();

//    AdRequest.Builder builder = new AdRequest.Builder();
//    AdRequest request = builder.build();

//    AdRequest.Builder builder = new AdRequest.Builder();
//    builder.setLocation(location);
//    AdRequest request = builder.build();

//    AdRequest request = new AdRequest.Builder()
//            .setLocation(location)
//            .build();

//    If a user has granted your app location permissions, AdMob automatically passes this location data to the SDK. The SDK uses this data to improve ad targeting without requiring any code changes in your app.
//    There are additional location methods for Android and iOS that can be implemented in an app’s code. These methods will only be used in the event that user location data can’t be accessed or you’ve disabled location data for ads in the app’s Settings tab.
//    If location data for ads is enabled in the app’s Settings tab, we will default to the location passed automatically to the SDK.


    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        // N/A
        return null;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        Project openProject = source.getProject();
        ArrayList<AnnotationHolder> speculatedAnnotationHolders = new ArrayList<>();
        PsiElement resolvedTargetVariable = api.getResolvedTargetVariable(source);
        PsiAnnotation[] annotations = CodeInspectionUtil.getAllAnnotations(resolvedTargetVariable);
        AnnotationHolder AdmobAnnotationHolderInstance = null;
        AnnotationHolder AdmobAnnotationSpeculation = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.AdmobAnnotation);
        AnnotationHolder LocationAnnotationSpeculation = null;
        AnnotationHolder UniqueIdentifierAnnotationSpeculation = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.UniqueIdentifierAnnotation);
        AnnotationHolder NetworkAnnotationSpeculation = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.NetworkAnnotation);
        NetworkAnnotationSpeculation.put("destination", "\"Google Admob\"");
        NetworkAnnotationSpeculation.put("purposeDescription", "\"Offload user personal data to remote servers to personalize ads.\"");
        NetworkAnnotationSpeculation.put("encryptedInTransmission", "true");
        NetworkAnnotationSpeculation.put("retentionTime", "\"Not clear from the Google Admob privacy policy\"");
        speculatedAnnotationHolders.add(UniqueIdentifierAnnotationSpeculation);
        speculatedAnnotationHolders.add(AdmobAnnotationSpeculation);
        speculatedAnnotationHolders.add(NetworkAnnotationSpeculation);
        UniqueIdentifierAnnotationSpeculation.put("purpose", String.format("UIDPurpose.%s", purpose_uid_tracking_user_data_collected_from_multiple_apps_on_this_device));
        UniqueIdentifierAnnotationSpeculation.put("purposeDescription", "\"Google may use the advertising ID from the device on which the ad is serving to generate interests and demographics (for example, 'sports enthusiasts'). Interests, demographics, and other data may be used to serve better targeted ads to the user. Additionally, your app's privacy policy may need to be updated to reflect the use of personalized advertising.\"");
        UniqueIdentifierAnnotationSpeculation.put("uidType", String.format("UIDType.%s", advertising_id));
        UniqueIdentifierAnnotationSpeculation.put("scope", String.format("UIDScope.%s", per_device_scope));
        UniqueIdentifierAnnotationSpeculation.put("resettability", String.format("UIDResettability.%s", user_resettable_in_system_settings));
        for (PsiAnnotation annotation : annotations) {
            if (CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotation) == CoconutAnnotationType.AdmobAnnotation) {
                AdmobAnnotationHolderInstance = CodeInspectionUtil.parseAnnotation(annotation);
            }
        }
        boolean isLocationDisabled = false;
        if (AdmobAnnotationHolderInstance != null) {
            if (AdmobAnnotationHolderInstance.containsPair("isLocationDataEnabledInAppSettings", "false")) {
                isLocationDisabled = true;
            }
        }
        // Then check the global permission
        boolean hasLocationPermission = (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_FINE_LOCATION) ||
                PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_COARSE_LOCATION));
        if (!isLocationDisabled && hasLocationPermission) {
            // Then check the setLocation function, if the actual type of location is not included in the speculation
            LocationAnnotationSpeculation = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.LocationAnnotation);
            String [] JavaTypePatterns = new String[]{".*AdRequest", ".*AdRequest.Builder"};
            String methodCallNamePattern = ".*setLocation";
            PsiMethodCallExpression sourceMethodCallExp;
            if (!(source instanceof PsiMethodCallExpression)) {
                sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
            } else {
                sourceMethodCallExp = (PsiMethodCallExpression) source;
            }
            ArrayList<PsiMethodCallExpression> setLocationMethodCallExpressions =
                    AnnotationUtils.getRelevantMethodCallExpression(methodCallNamePattern, JavaTypePatterns, sourceMethodCallExp);
            LocationAnnotationSpeculation.put("purpose", String.format("LocationPurpose.%s", purpose_location_data_collection_for_advertising));
            LocationAnnotationSpeculation.put("purposeDescription", "\"If a user has granted your app location permissions, AdMob automatically passes this location data to the SDK. The SDK uses this data to improve ad targeting without requiring any code changes in your app.\"");
            if (setLocationMethodCallExpressions.isEmpty()) {
                if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_FINE_LOCATION)) {
                    LocationAnnotationSpeculation.put("dataType", String.format("LocationDataType.%s", fine_grained_latitude_longitude));
                } else if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_COARSE_LOCATION)) {
                    LocationAnnotationSpeculation.put("dataType", String.format("LocationDataType.%s", coarse_grained_latitude_longitude));
                }
            }
            speculatedAnnotationHolders.add(LocationAnnotationSpeculation);
        }
        return speculatedAnnotationHolders.toArray(new AnnotationHolder[0]);
    }

    @Override
    @Nullable
    public LocalQuickFix [] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        // TODO: check field name first
        return LocationUtils.getChangePermissionQuickfixes(methodCallExpression, fieldValue);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<PersonalDataInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        // FIXME: temporary workaround
        return LocationUtils.getChangePermissionQuickfixes((PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement(), fieldValue);
    }

    @Override
    @Nullable
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return LocationUtils.getChangePermissionQuickfixes(methodCallExpression, nameValuePair, new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", coarse_grained_latitude_longitude))));
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<PersonalDataInstance> instances, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        // FIXME: this is just a temporary hotfix
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        return LocationUtils.getChangePermissionQuickfixes(methodCallExpression, nameValuePair, new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", coarse_grained_latitude_longitude))));
    }

}
