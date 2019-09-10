package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;

import java.util.*;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * @author Elijah Neundorfer 6/18/19
 * @version 6/18/19
 */
public class SensorUtils {

    /**
     * Takes an expression and determines the value at first initialization
     *
     *
     * @param expression
     * @return
     * @throws ClassCastException Bypasses any types not handled, such as arrays
     */
    private static PsiExpression resolveInitValue(PsiExpression expression) throws ClassCastException {

        if(expression instanceof PsiMethodCallExpression) {
            return expression;
        }

        PsiExpression initValue = null;
        if (((PsiReferenceExpression) expression).resolve() == null) {
            return initValue;
        }
        if (((PsiReferenceExpression) expression).resolve() instanceof PsiLocalVariable) {
            PsiLocalVariable localVariable = (PsiLocalVariable) ((PsiReferenceExpression) expression).resolve();
            initValue = PsiTreeUtil.getChildOfType(localVariable, PsiExpression.class);
            if (initValue == null) {
                Collection<PsiReference> references = ReferencesSearch.search(localVariable).findAll();

                //Iterating over every PsiReference in references
                for (PsiReference reference : references) {
                    //If the element in the referent is an Assignment, we add the action in the contructor to the set of actions
                    if (PsiTreeUtil.getParentOfType(reference.getElement(), PsiAssignmentExpression.class) != null) {
                        PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiAssignmentExpression.class);
                        assert assignmentExpression != null;
                        initValue = assignmentExpression.getRExpression();
                        break;
                    }
                }
            }
        } else if (((PsiReferenceExpression) expression).resolve() instanceof PsiField) {
            PsiField field = (PsiField) ((PsiReferenceExpression) expression).resolve();
            initValue = PsiTreeUtil.getChildOfType(field, PsiExpression.class);
            if (initValue == null) {
                Collection<PsiReference> references = ReferencesSearch.search(field).findAll();

                //Iterating over every PsiReference in references
                for (PsiReference reference : references) {
                    //If the element in the referent is an Assignment, we add the action in the contructor to the set of actions
                    if (PsiTreeUtil.getParentOfType(reference.getElement(), PsiAssignmentExpression.class) != null) {
                        PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiAssignmentExpression.class);
                        assert assignmentExpression != null;
                        initValue = assignmentExpression.getRExpression();
                        break;
                    }
                }
            }
        }
        return initValue;
    }


    /**
     * Facilitates analyzing multiple sensors
     *
     * @param sensors The PsiExpressions representing the parameters that either are or point to the sensor in the relevant method call.
     * @param annotationHolder The annotation holder for the relevant method call
     */
    static void analyzeSensors(List<PsiExpression> sensors, AnnotationHolder annotationHolder) {
        for(PsiExpression sensor : sensors) {
            analyzeSensor(sensor, annotationHolder);
        }
    }

    /**
     * Analyzes the sensor to determine any necessary data type speculations we should add to the annotation holder.
     *
     * We choose only to speculate data types, not purposes.
     *
     * The logic is very basic by simply determining what sensor they are using. We may want to develop the speculation further.
     *
     * @param sensor The PsiExpression representing the parameter that either is or points to the sensor in the relevant method call.
     * @param annotationHolder The annotation holder for the relevant method call
     */
    static void analyzeSensor(PsiExpression sensor, AnnotationHolder annotationHolder) {

        //These sets will contain the speculations to add to the annotation holders.
        //We use sets to avoid duplicates
        Set<String> dataTypeSpeculations = new HashSet<>();

        //Block of code gets a string representation of the sensor type from the initial initialization of the sensor object
        PsiExpression initialSensorValue;
        try {
            //Getting the initial value of the sensor object
            initialSensorValue = resolveInitValue(sensor);
        } catch (ClassCastException e) {
            //System.err.println("Exception caught: " + e);
            return;
        }
        //Getting the extracted argument (the sensor)
        PsiExpression extractedArgument = PsiTreeUtil.getChildOfType(((PsiMethodCallExpressionImpl) initialSensorValue).getArgumentList(), PsiReferenceExpression.class);
        //Stripping the argument down to just the sensor type
        String sensorTypeString = PsiTreeUtil.getChildOfType(extractedArgument, PsiIdentifier.class).getText();

        //This switch case handles the main determinations of which data type speculations to add to our annotation holders
        switch (sensorTypeString) {
            case "TYPE_ACCELEROMETER":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", acceleration));
                dataTypeSpeculations.add(String.format("SensorDataType.%s", gravity));
                break;
            case "TYPE_ACCELEROMETER_UNCALIBRATED":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", acceleration));
                dataTypeSpeculations.add(String.format("SensorDataType.%s", gravity));
                break;
            case "TYPE_AMBIENT_TEMPERATURE":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", temperature));
                break;
            case "TYPE_DEVICE_PRIVATE_BASE":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", custom_sensor_data));
                break;
            case "TYPE_GAME_ROTATION_VECTOR":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", rotation));
                break;
            case "TYPE_GEOMAGNETIC_ROTATION_VECTOR":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", rotation));
                break;
            case "TYPE_GRAVITY":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", gravity));
                break;
            case "TYPE_GYROSCOPE":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", rotation));
                break;
            case "TYPE_GYROSCOPE_UNCALIBRATED":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", rotation));
                break;
            case "TYPE_HEART_BEAT":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", heart_beat));
                break;
            case "TYPE_HEART_RATE":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", heart_rate));
                break;
            case "TYPE_LIGHT":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", light));
                break;
            case "TYPE_LINEAR_ACCELERATION":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", acceleration));
                break;
            case "TYPE_LOW_LATENCY_OFFBODY_DETECT":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", low_latency_offbody_detect));
                break;
            case "TYPE_MAGNETIC_FIELD":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", magnetic_field_strength));
                break;
            case "TYPE_MOTION_DETECT":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", motion));
                break;
            case "TYPE_POSE_6DOF":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", rotation));
                break;
            case "TYPE_PRESSURE":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", pressure));
                break;
            case "TYPE_PROXIMITY":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", proximity));
                break;
            case "TYPE_RELATIVE_HUMIDITY":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", humidity));
                break;
            case "TYPE_ROTATION_VECTOR":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", rotation));
                break;
            case "TYPE_SIGNIFICANT_MOTION":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", motion));
                break;
            case "TYPE_STATIONARY_DETECT":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", motion));
                break;
            case "TYPE_STEP_COUNTER":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", number_of_steps));
                break;
            case "TYPE_STEP_DETECTOR":
                dataTypeSpeculations.add(String.format("SensorDataType.%s", motion));
                break;
        }

        //Finally, we add our speculations to the annotation holder
        if (!dataTypeSpeculations.isEmpty()) {
            ArrayList<String> tempList = new ArrayList<>(dataTypeSpeculations);
            annotationHolder.put("dataType", tempList);
        }
    }
}
