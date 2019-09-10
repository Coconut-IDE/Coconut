package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 *
 * @author Elijah Neundorfer 6/17/19
 * @version 6/21/19
 */
public class SensorAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.SensorAnnotation);
        if (!(source instanceof PsiMethodCallExpression)) {
            source = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        }
        PsiExpression sensor;
        switch(PsiTreeUtil.getChildOfType(((PsiMethodCallExpression)source).getMethodExpression(), PsiIdentifier.class).getText()) {
            case "createDirectChannel":

                //Stores the sensors that we need to analyze
                ArrayList<PsiExpression> sensors = new ArrayList<>();

                //Gets the SensorDirectChannel that later connects us to the Sensor we need to analyze
                PsiReferenceExpression sensorDirectChannelReference = PsiTreeUtil.getChildOfType(source.getParent(), PsiReferenceExpression.class);
                PsiElement sensorDirectChannel = sensorDirectChannelReference.resolve();
                if (sensorDirectChannel != null) {
                    //References to the SensorDirectChannel. >=1 of these should link to the Sensor we need to analyze. If not, we can't speculate from the Sensor
                    Collection<PsiReference> references = ReferencesSearch.search(sensorDirectChannel).findAll();
                    //Iterating over every reference
                    for (PsiReference reference : references) {
                        //Ensures we're only looking at method calls
                        if (PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class) != null) {
                            PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class);
                            //if we end up at the same method call as our source, we don't care
                            if (source.equals(methodCallExpression)) {
                                continue;
                            }
                            //Tests to find the "configure" method call
                            if (Pattern.compile(".*configure", Pattern.DOTALL)
                                    .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                                //Adds the sensor from the configure method call to our list
                                sensors.add(PsiTreeUtil.getChildOfType(methodCallExpression.getArgumentList(), PsiReferenceExpression.class));
                            }
                        }
                    }
                    //Analyzes the sensor(s) found
                    if (!sensors.isEmpty()) {
                        SensorUtils.analyzeSensors(sensors, annotationHolder);
                    }
                }
                break;
            case "registerListener":
                //Uses same code as requestTriggerSensor
            case "requestTriggerSensor":
                sensor = ((PsiMethodCallExpression)source).getArgumentList().getExpressions()[1];
                SensorUtils.analyzeSensor(sensor, annotationHolder);
                break;
        }
        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return null;
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return null;
    }
}
