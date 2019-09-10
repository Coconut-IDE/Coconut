package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianshi on 4/27/17.
 */
public class AdmobAnnotationHolder extends AnnotationHolder {
    public static final Map<PersonalDataGroup, String> STATIC_DESCRIPTION_PER_GROUP_MAP =
            Collections.unmodifiableMap(new HashMap<PersonalDataGroup, String>() {{
                put(PersonalDataGroup.LOCATION, "Use location data to build user profile for ad personalizing");
                put(PersonalDataGroup.UNIQUE_IDENTIFIER, "Use advertising ID to track Location data");
            }});

    public static final Map<AndroidPermission, String> STATIC_DESCRIPTION_PER_PERMISSION_MAP =
            Collections.unmodifiableMap(new HashMap<AndroidPermission, String>() {{
                put(AndroidPermission.ACCESS_COARSE_LOCATION, "Use coarse-grained location data to build user profile for ad personalizing. (Users can opt out of Ads Personalization in the settings (Google -> Ads).)");
                put(AndroidPermission.ACCESS_FINE_LOCATION, "Use fine-grained location data to build user profile for ad personalizing. (Users can opt out of Ads Personalization in the settings (Google -> Ads).");
            }});

    public AdmobAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.AdmobAnnotation;
//        if (nameValuePairs.containsKey("LocationBasedAdDisabled")) {
//            if (nameValuePairs.get("LocationBasedAdDisabled").equals("true")) {
//                myPersonalDataBasedAdDisabled.put(AndroidPermission.ACCESS_COARSE_LOCATION, true);
//                myPersonalDataBasedAdDisabled.put(AndroidPermission.ACCESS_FINE_LOCATION, true);
//            } else {
//                myPersonalDataBasedAdDisabled.put(AndroidPermission.ACCESS_COARSE_LOCATION, false);
//                myPersonalDataBasedAdDisabled.put(AndroidPermission.ACCESS_FINE_LOCATION, false);
//            }
//        } else {
//            myPersonalDataBasedAdDisabled.put(AndroidPermission.ACCESS_COARSE_LOCATION, false);
//            myPersonalDataBasedAdDisabled.put(AndroidPermission.ACCESS_FINE_LOCATION, false);
//        }
    }

    public AdmobAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.AdmobAnnotation;
        initAllFields();
    }

    // TODO: (long-term) compiles the STATIC_DESCRIPTION_PER_GROUP_MAP into one sentence
    @Override
    public String getDescription() {
        // TODO (long-term) return purpose
        return "";
    }

    @Override
    public String getDescriptionByGroup(PersonalDataGroup group) {
        return STATIC_DESCRIPTION_PER_GROUP_MAP.get(group);
    }

    @Override
    public String getDescriptionByPermission(AndroidPermission permission) {
        return STATIC_DESCRIPTION_PER_PERMISSION_MAP.get(permission);
    }
}
