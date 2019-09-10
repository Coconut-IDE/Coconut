package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.AdaptCodeToAnnotationQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.ModifyFieldValueAndCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.CoconutUIUtil;
import org.jetbrains.annotations.NotNull;
import sun.nio.ch.Net;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.utils.AndroidPermission.ACCESS_FINE_LOCATION;
import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 1/21/18.
 */
public class LocationUtils {

    private static PsiExpression resolveInitValue(PsiExpression expression) {
        PsiExpression initValue = null;
        if (((PsiReferenceExpression) expression).resolve() == null) {
            return initValue;
        }
        if (((PsiReferenceExpression) expression).resolve() instanceof PsiLocalVariable) {
            PsiLocalVariable localVariable = (PsiLocalVariable) ((PsiReferenceExpression) expression).resolve();
            initValue = PsiTreeUtil.getChildOfType(localVariable, PsiExpression.class);
        } else if (((PsiReferenceExpression) expression).resolve() instanceof PsiField) {
            PsiField field = (PsiField) ((PsiReferenceExpression) expression).resolve();
            initValue = PsiTreeUtil.getChildOfType(field, PsiExpression.class);
        }
        return initValue;
    }

    static void analyzeProvider(PsiExpression provider, AnnotationHolder annotationHolder) {
        Project openProject = provider.getProject();
        // TODO: (urgent) add explanations of how these speculations are made
        boolean GPSProvider = false;
        boolean NetworkProvider = false;
        ArrayList<String> dataTypeSpeculations = new ArrayList<>();
        if (provider instanceof PsiReferenceExpression) {
            if ("LocationManager.GPS_PROVIDER".equals(provider.getText())) {
                GPSProvider = true;
            } else if ("LocationManager.NETWORK_PROVIDER".equals(provider.getText())) {
                NetworkProvider = true;
            } else {
                PsiExpression initValue = resolveInitValue(provider);
                if (initValue != null) {
                    if ("LocationManager.GPS_PROVIDER".equals(initValue.getText()) || "\"gps\"".equals(initValue.getText())) {
                        GPSProvider = true;
                    } else if ("LocationManager.NETWORK_PROVIDER".equals(initValue.getText()) || "\"network\"".equals(initValue.getText())) {
                        NetworkProvider = true;
                    }
                }
            }
        } else if (provider instanceof PsiLiteralExpression) {
            if ("\"gps\"".equals(provider.getText())) {
                GPSProvider = true;
            } else if ("\"network\"".equals(provider.getText())) {
                NetworkProvider = true;
            }
        } else {
            if ("LocationManager.GPS_PROVIDER".equals(provider.getText())) {
                GPSProvider = true;
            } else if ("LocationManager.NETWORK_PROVIDER".equals(provider.getText())) {
                NetworkProvider = true;
            }
        }
        if (GPSProvider || NetworkProvider) {
            if (GPSProvider) {
                dataTypeSpeculations.add(String.format("LocationDataType.%s", fine_grained_latitude_longitude));
            }
            if (NetworkProvider) {
                dataTypeSpeculations.add(String.format("LocationDataType.%s", coarse_grained_latitude_longitude));
            }
        } else {
            if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_COARSE_LOCATION)) {
                NetworkProvider = true;
            }
            if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_FINE_LOCATION)) {
                GPSProvider = true;
            }
            if (GPSProvider) {
                dataTypeSpeculations.add(String.format("LocationDataType.%s", fine_grained_latitude_longitude));
            }
            if (NetworkProvider) {
                dataTypeSpeculations.add(String.format("LocationDataType.%s", coarse_grained_latitude_longitude));
            }
        }
        if (!dataTypeSpeculations.isEmpty()) {
            annotationHolder.put("dataType", dataTypeSpeculations);
        }
    }

    static void analyzeFrequency(PsiExpression minTime, PsiExpression minDistance, AnnotationHolder annotationHolder) {
        long minTimeValue = -1;
        float minDistanceValue = -1;
        if (minTime instanceof PsiReferenceExpression) {
            PsiExpression initValue = resolveInitValue(minTime);
            if (initValue != null) {
                minTimeValue = Long.valueOf(initValue.getText());
            }

        } else if (minTime instanceof PsiLiteralExpression) {
            minTimeValue = Long.valueOf(minTime.getText());
        }

        if (minDistance instanceof PsiReferenceExpression) {
            PsiExpression initValue = resolveInitValue(minDistance);
            if (initValue != null) {
                minDistanceValue = Float.valueOf(initValue.getText());
            }
        } else if (minDistance instanceof PsiLiteralExpression) {
            minDistanceValue = Float.valueOf(minDistance.getText());
        }

        ArrayList<String> frequencySpeculations = new ArrayList<>();
        if (minTimeValue == 0 && minDistanceValue == 0) {
            frequencySpeculations.add("\"The location will be updated as fast as possible\"");
        } else {
            if (minTimeValue > 0 && minDistanceValue > 0) {
                frequencySpeculations.add(String.format("\"The location update can be received if time interval >= %d milliseconds (%d seconds, ~ %f minutes) AND distance change >= %f meters\"",
                        minTimeValue, minTimeValue / 1000, minTimeValue / 60000.0, minDistanceValue));
            } else if (minTimeValue > 0 && minDistanceValue <= 0) {
                frequencySpeculations.add(String.format("\"The location update can be received if time interval >= %d milliseconds (%d seconds, ~ %f minutes)\"",
                        minTimeValue, minTimeValue / 1000, minTimeValue / 60000.0));
            } else if (minDistanceValue > 0 && minTimeValue <= 0) {
                frequencySpeculations.add(String.format("\"The location update can be received if distance change >= %f meters\"",
                        minDistanceValue));
            } else if (minTimeValue == 0 && minDistanceValue < 0) {
                frequencySpeculations.add("\"The location will be updated as fast as possible\"");
            } else if (minDistanceValue == 0 && minTimeValue < 0) {
                frequencySpeculations.add("\"The location will be updated as fast as possible\"");
            }
        }
        if (!frequencySpeculations.isEmpty()) {
            annotationHolder.put("frequency", frequencySpeculations);
        }
    }

    static boolean possibleValueMatch(String valuePattern, PsiExpression expression) {
        if (expression == null) {
            return false;
        }
        if (Pattern.compile(valuePattern, Pattern.DOTALL).matcher(expression.getText()).matches()) {
            return true;
        }
        if (expression instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression) expression).resolve();
            if (resolve == null) {
                return false;
            }
            PsiReference[] references = ReferencesSearch.search(resolve).findAll().toArray(new PsiReference[0]);
            for (int i = references.length - 1 ; i >= 0 ; i--) {
                PsiReference reference = references[i];
                PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiAssignmentExpression.class);
                if (assignmentExpression != null && assignmentExpression.getLExpression().equals(reference.getElement())) {
                    return possibleValueMatch(valuePattern, assignmentExpression.getRExpression());
                }
            }
            return possibleValueMatch(valuePattern, resolveInitValue(expression));
        } else {
            return false;
        }
    }

    public static void analyzeCriteria(PsiMethodCallExpression currentMethodCallExp, PsiExpression criteria, @NotNull AnnotationHolder annotationHolder) {
        String accuracy = null;
        boolean altitudeRequired = false;
        boolean speedRequired = false;
        boolean bearingRequired = false;
        if (criteria == null) {
            return;
        }
        if (criteria instanceof PsiReferenceExpression) {
            PsiElement criteriaDeclaration = ((PsiReferenceExpression) criteria).resolve();
            if (criteriaDeclaration == null) {
                return;
            }
            Collection<PsiReference> criteriaReferences = ReferencesSearch.search(criteriaDeclaration).findAll();
            for (PsiReference reference : criteriaReferences) {
                if (PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class) != null) {
                    PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class);
                    // Only consider set expressions before the location API call that uses this criteria
                    if (currentMethodCallExp.equals(methodCallExpression)) {
                        break;
                    }
                    PsiExpressionList argumentList = methodCallExpression.getArgumentList();
                    if (Pattern.compile(".*setAccuracy", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches() ||
                        Pattern.compile(".*setHorizontalAccuracy", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression accuracyExpression = argumentList.getExpressions()[0];
                        if (possibleValueMatch("1", accuracyExpression) ||
                                possibleValueMatch(".*ACCURACY_FINE", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", fine_grained_latitude_longitude);
                        } else if (possibleValueMatch("3", accuracyExpression) ||
                                possibleValueMatch(".*ACCURACY_HIGH", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", fine_grained_latitude_longitude);
                        } else if (possibleValueMatch("2", accuracyExpression) ||
                                possibleValueMatch(".*ACCURACY_COARSE", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", coarse_grained_latitude_longitude);
                        } else if (possibleValueMatch("1", accuracyExpression) ||
                                possibleValueMatch(".*ACCURACY_LOW", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", coarse_grained_latitude_longitude);
                        } else if (possibleValueMatch("2", accuracyExpression) ||
                                possibleValueMatch(".*ACCURACY_MEDIUM", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", coarse_grained_latitude_longitude);
                        }
                    }
                    if (Pattern.compile(".*setAltitudeRequired", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression altitudeRequiredExpression = argumentList.getExpressions()[0];
                        if (possibleValueMatch("true", altitudeRequiredExpression)) {
                            altitudeRequired = true;
                        } else if (possibleValueMatch("false", altitudeRequiredExpression)) {
                            altitudeRequired = false;
                        }
                    }
                    if (Pattern.compile(".*setSpeedRequired", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression speedRequiredExpression = argumentList.getExpressions()[0];
                        if (possibleValueMatch("true", speedRequiredExpression)) {
                            speedRequired = true;
                        } else if (possibleValueMatch("false", speedRequiredExpression)) {
                            speedRequired = false;
                        }
                    }
                    if (Pattern.compile(".*setBearingRequired", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression bearingRequiredExpression = argumentList.getExpressions()[0];
                        if (possibleValueMatch("true", bearingRequiredExpression)) {
                            bearingRequired = true;
                        } else if (possibleValueMatch("false", bearingRequiredExpression)) {
                            bearingRequired = false;
                        }
                    }
                }
            }
        }
        ArrayList<String> dataTypeSpeculations = new ArrayList<>();
        if (accuracy != null) {
            dataTypeSpeculations.add(accuracy);
        }
        if (altitudeRequired) {
            dataTypeSpeculations.add("LocationDataType.ALTITUDE");
        }
        if (speedRequired) {
            dataTypeSpeculations.add("LocationDataType.SPEED");
        }
        if (bearingRequired) {
            dataTypeSpeculations.add("LocationDataType.BEARING");
        }
        if (!dataTypeSpeculations.isEmpty()) {
            annotationHolder.put("dataType", dataTypeSpeculations);
        }
    }

    public static void analyzeLocationRequest(PsiMethodCallExpression currentMethodCallExp, PsiExpression locationRequest, AnnotationHolder annotationHolder) {
        Project openProject = currentMethodCallExp.getProject();
        String accuracy = null;
        long timeInterval = -1;
        long fastestTimeInterval = -1; // An interval of 0 is allowed, but not recommended, since location updates may be extremely fast on future implementations.
        long maxWaitTimeInterval = -1; // The locations update frequency is decided by the interval/fastest time interval you specified, but it can be delivered in batch to save power, which is decided by this max wait time interval
        int updateNum = -1;
        float minDistance = 0;
        if (locationRequest instanceof PsiReferenceExpression) {
            PsiElement criteriaDeclaration = ((PsiReferenceExpression) locationRequest).resolve();
            if (criteriaDeclaration == null) {
                return;
            }
            Collection<PsiReference> criteriaReferences = ReferencesSearch.search(criteriaDeclaration).findAll();
            for (PsiReference reference : criteriaReferences) {
                if (PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class) != null) {
                    PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class);
                    // Only consider set expressions before the location API call that uses this criteria
                    if (currentMethodCallExp.equals(methodCallExpression)) {
                        break;
                    }
                    PsiExpressionList argumentList = methodCallExpression.getArgumentList();
                    if (argumentList.getExpressions() == null || argumentList.getExpressions().length == 0) {
                        continue;
                    }
                    if (Pattern.compile(".*setPriority", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression accuracyExpression = argumentList.getExpressions()[0];
                        if (possibleValueMatch("100", accuracyExpression) ||
                                possibleValueMatch(".*PRIORITY_HIGH_ACCURACY", accuracyExpression)) {
                            if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_COARSE_LOCATION)) {
                                accuracy = String.format("LocationDataType.%s", coarse_grained_latitude_longitude);
                            } else {
                                accuracy = String.format("LocationDataType.%s", fine_grained_latitude_longitude);
                            }
                        } else if (possibleValueMatch("105", accuracyExpression) ||
                                possibleValueMatch(".*PRIORITY_NO_POWER", accuracyExpression)) {
                            if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_COARSE_LOCATION)) {
                                accuracy = String.format("LocationDataType.%s", coarse_grained_latitude_longitude);
                            } else {
                                accuracy = String.format("LocationDataType.%s", fine_grained_latitude_longitude);
                            }
                        } else if (possibleValueMatch("102", accuracyExpression) ||
                                possibleValueMatch(".*PRIORITY_BALANCED_POWER_ACCURACY", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", block_level_latitude_longitude);
                        } else if (possibleValueMatch("104", accuracyExpression) ||
                                possibleValueMatch(".*PRIORITY_LOW_POWER", accuracyExpression)) {
                            accuracy = String.format("LocationDataType.%s", city_level_latitude_longitude);
                        }
                    }
                    if (Pattern.compile(".*setInterval", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression intervalExpression = argumentList.getExpressions()[0];
                        try {
                            timeInterval = Long.valueOf(intervalExpression.getText());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Pattern.compile(".*setFastestInterval", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression fastestIntervalExpression = argumentList.getExpressions()[0];
                        try {
                            fastestTimeInterval = Long.valueOf(fastestIntervalExpression.getText());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Pattern.compile(".*setMaxWaitTime", Pattern.DOTALL)
                            .matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression maxWaitTimeExpression = argumentList.getExpressions()[0];
                        try {
                            maxWaitTimeInterval = Long.valueOf(maxWaitTimeExpression.getText());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Pattern.compile(".*setNumUpdates", Pattern.DOTALL).
                            matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression updateNumExpression = argumentList.getExpressions()[0];
                        try {
                            updateNum = Integer.valueOf(updateNumExpression.getText());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Pattern.compile(".*setSmallestDisplacement", Pattern.DOTALL).
                            matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiExpression smallestDisplacementExpression = argumentList.getExpressions()[0];
                        try {
                            minDistance = Float.valueOf(smallestDisplacementExpression.getText());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (accuracy != null) {
            annotationHolder.put("dataType", accuracy);
        }
        ArrayList<String> frequencyDescriptions = new ArrayList<>();
        if (updateNum > 0) {
            frequencyDescriptions.add(String.format("\"There will be at most %d location updates\"", updateNum));
        } else {
            frequencyDescriptions.add(String.format("\"locations are continuously updated until the request is explicitly removed.\""));
        }
        if (timeInterval != -1 || fastestTimeInterval != -1) {
            long interval = -1;
            if (timeInterval != -1) {
                interval = timeInterval;
            }
            if (fastestTimeInterval != -1) {
                if (interval < fastestTimeInterval) {
                    interval = fastestTimeInterval;
                }
            }
            if (interval > 0) {
                frequencyDescriptions.add(String.format("\"The minimum time interval to update location is %d milliseconds (%d seconds, ~ %f minutes)\"",
                        interval, interval / 1000, interval / 60000.0));
            }
        }
        if (minDistance > 0) {
            frequencyDescriptions.add(String.format("\"The minimum distance to update location is %f meters\"", minDistance));
        }
        if (maxWaitTimeInterval != -1) {
            frequencyDescriptions.add(String.format("\"The location updates will be received in batch every %d milliseconds (%d seconds, ~ %f minutes)\"",
                    maxWaitTimeInterval, maxWaitTimeInterval / 1000, maxWaitTimeInterval / 60000.0));
        }

        if (!frequencyDescriptions.isEmpty()) {
            annotationHolder.put("frequency", frequencyDescriptions);
        }
    }

    static void providerBasedChangeCodeFunction(int providerParameterPosition, String newProvider, PsiMethodCallExpression methodCallExpression) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(methodCallExpression.getProject()).getElementFactory();
        if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= providerParameterPosition) {
            return;
        }
        PsiExpression provider = methodCallExpression.getArgumentList().getExpressions()[providerParameterPosition];
        provider.replace(factory.createExpressionFromText(newProvider, null));
        CoconutUIUtil.navigateMainEditorToPsiElement(methodCallExpression);
    }

    static void criteriaBasedChangeCodeFunction(String criteriaName, String statementPattern, PsiMethodCallExpression methodCallExpression) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(methodCallExpression.getProject()).getElementFactory();
        PsiStatement setAccuracyStatement = factory.createStatementFromText(String.format(statementPattern, criteriaName), null);
        PsiStatement currentStatement = PsiTreeUtil.getParentOfType(methodCallExpression, PsiStatement.class);
        if (currentStatement != null) {
            currentStatement.getParent().addBefore(setAccuracyStatement, currentStatement);
            CoconutUIUtil.navigateMainEditorToPsiElement(methodCallExpression);
        }
    }

    static void locationRequestBasedChangeCodeFunction(String requestName, String statementPattern, PsiMethodCallExpression methodCallExpression) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(methodCallExpression.getProject()).getElementFactory();
        PsiStatement setAccuracyStatement = factory.createStatementFromText(String.format(statementPattern, requestName), null);
        PsiStatement currentStatement = PsiTreeUtil.getParentOfType(methodCallExpression, PsiStatement.class);
        if (currentStatement != null) {
            currentStatement.getParent().addBefore(setAccuracyStatement, currentStatement);
            CoconutUIUtil.navigateMainEditorToPsiElement(methodCallExpression);
        }
    }

    static void changePermissionChangeCodeFunction(AndroidPermission prevPersmission, AndroidPermission newPermission, PsiMethodCallExpression callExpression) {
        File file = new File(String.format("%s/app/src/main/AndroidManifest.xml", callExpression.getProject().getBasePath()));
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        Project openProject = callExpression.getProject();
        if (virtualFile == null) {
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(callExpression.getProject()).findFile(virtualFile);
        if (psiFile == null) {
            return;
        }
        boolean prevProvider = PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(prevPersmission);
        boolean newProvider = PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(newPermission);
        if (prevProvider) {
            psiFile.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    super.visitElement(element);
                    if (element instanceof XmlTag) {
                        XmlTag tag = (XmlTag) element;
                        if (tag.getAttributes().length == 0) {
                            return;
                        }
                        String xmlAttribute = tag.getAttributes()[0].getDisplayValue();
                        if (String.format("android.permission.%s", prevPersmission.toString()).equals(xmlAttribute)) {
                            tag.delete();
                        }
                    }
                }
            });
        }
        if (!newProvider) {
            XmlTag tag = XmlElementFactory.getInstance(callExpression.getProject()).createXHTMLTagFromText(String.format("<uses-permission android:name=\"android.permission.%s\"/>\n", newPermission.toString()));
            PsiElement[] children = psiFile.getChildren();
            if (children.length > 0 && children[0] != null &&
                    children[0].getChildren().length > 1 && children[0].getChildren()[1] != null) {
                children[0].getChildren()[1].add(tag);
            }
        }
    }

    static LocalQuickFix[] getChangePermissionQuickfixes(PsiMethodCallExpression methodCallExpression, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        if (fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        if (String.format("LocationDataType.%s", coarse_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) replace ACCESS_FINE_LOCATION with ACCESS_COARSE_LOCATION in the manifest file", methodCallExpression,
                    (callExpression) -> LocationUtils.changePermissionChangeCodeFunction(AndroidPermission.ACCESS_FINE_LOCATION, AndroidPermission.ACCESS_COARSE_LOCATION, callExpression)));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) replace ACCESS_COARSE_LOCATION with ACCESS_FINE_LOCATION in the manifest file", methodCallExpression,
                    (callExpression) -> LocationUtils.changePermissionChangeCodeFunction(AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION, callExpression)));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    static LocalQuickFix[] getChangePermissionQuickfixes(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        if (fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        if (String.format("LocationDataType.%s", coarse_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) replace ACCESS_FINE_LOCATION with ACCESS_COARSE_LOCATION in the manifest file", methodCallExpression,
                    nameValuePair, fieldValue,
                    (callExpression) -> LocationUtils.changePermissionChangeCodeFunction(AndroidPermission.ACCESS_FINE_LOCATION, AndroidPermission.ACCESS_COARSE_LOCATION, callExpression)));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) replace ACCESS_COARSE_LOCATION with ACCESS_FINE_LOCATION in the manifest file", methodCallExpression,
                    nameValuePair, fieldValue,
                    (callExpression) -> LocationUtils.changePermissionChangeCodeFunction(AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION, callExpression)));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    // FIXME: This is just a temporary hotfix
    public static void combineMultipleRequestLocationUpdateChangeCodeFunction(ArrayList<PersonalDataInstance> instances, String value) {
        for (PersonalDataInstance instance : instances) {
            if (!(instance.getPsiElementPointer().getElement() instanceof PsiMethodCallExpression)) {
                continue;
            }
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instance.getPsiElementPointer().getElement();
            if (String.format("LocationDataType.%s", coarse_grained_latitude_longitude).equals(value)) {
                if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestLocationUpdate1AnnotationUtil) {
                    providerBasedChangeCodeFunction(0, "LocationManager.NETWORK_PROVIDER", methodCallExpression);
                } else if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestLocationUpdate2AnnotationUtil) {
                    if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= 2) {
                        continue;
                    }
                    PsiExpression criteria = methodCallExpression.getArgumentList().getExpressions()[2];
                    if (criteria == null) {
                        continue;
                    }
                    String criteriaName = criteria.getText();
                    if (criteriaName == null) {
                        continue;
                    }
                    criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_COARSE);\n", methodCallExpression);
                } else if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestSingleUpdate1AnnotationUtil) {
                    providerBasedChangeCodeFunction(0, "LocationManager.NETWORK_PROVIDER", methodCallExpression);
                } else if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestSingleUpdate2AnnotationUtil) {
                    if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= 0) {
                        continue;
                    }
                    PsiExpression criteria = methodCallExpression.getArgumentList().getExpressions()[0];
                    if (criteria == null) {
                        continue;
                    }
                    String criteriaName = criteria.getText();
                    if (criteriaName == null) {
                        continue;
                    }
                    criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_COARSE);\n", methodCallExpression);
                }
            } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
                if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestLocationUpdate1AnnotationUtil) {
                    providerBasedChangeCodeFunction(0, "LocationManager.GPS_PROVIDER", methodCallExpression);
                } else if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestLocationUpdate2AnnotationUtil) {
                    if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= 2) {
                        continue;
                    }
                    PsiExpression criteria = methodCallExpression.getArgumentList().getExpressions()[2];
                    if (criteria == null) {
                        continue;
                    }
                    String criteriaName = criteria.getText();
                    if (criteriaName == null) {
                        continue;
                    }
                    criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_FINE);\n", methodCallExpression);
                } else if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestSingleUpdate1AnnotationUtil) {
                    providerBasedChangeCodeFunction(0, "LocationManager.GPS_PROVIDER", methodCallExpression);
                } else if (instance.getPersonalDataAPI().config instanceof LocationManagerRequestSingleUpdate2AnnotationUtil) {
                    if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= 0) {
                        continue;
                    }
                    PsiExpression criteria = methodCallExpression.getArgumentList().getExpressions()[0];
                    if (criteria == null) {
                        continue;
                    }
                    String criteriaName = criteria.getText();
                    if (criteriaName == null) {
                        continue;
                    }
                    criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_FINE);\n", methodCallExpression);
                }
            }

        }
    }

}
