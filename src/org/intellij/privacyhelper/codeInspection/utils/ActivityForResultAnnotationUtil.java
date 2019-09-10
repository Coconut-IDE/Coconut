package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ActivityForResultAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        // This annotation util is for handling source APIs so method should not be called
        return null;
    }

    private boolean checkCreateChooserAPI(ArrayList<String> statements, PsiMethodCallExpression intentMethodCallExp) {
        PsiExpression intentExpression;
        PsiIdentifier currentMethodIdentifier =
                PsiTreeUtil.getChildOfType(intentMethodCallExp.getMethodExpression(), PsiIdentifier.class);
        if (currentMethodIdentifier != null &&
                "createChooser".equals(currentMethodIdentifier.getText())) {
            intentExpression = intentMethodCallExp.getArgumentList().getExpressions()[0];
            statements.clear();
            traceAllIntentOccurrences(intentExpression, statements, intentMethodCallExp);
            return true;
        } else {
            return false;
        }
    }

    private void traceAllIntentOccurrences(PsiExpression intentExpression, ArrayList<String> statements,
                                           PsiMethodCallExpression methodCallExpression) {
        if (intentExpression instanceof PsiReferenceExpression) {
            ArrayList<PsiElement> intentOccurrences = CodeInspectionUtil.getGlobalAndLocalRefExpsBeforeMethodExp(
                    (PsiReferenceExpression) intentExpression, methodCallExpression);
            for (PsiElement intentOccurrence : intentOccurrences) {
                boolean isChooserIntent = false;
                PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(intentOccurrence, PsiAssignmentExpression.class);
                PsiDeclarationStatement declarationStatement = PsiTreeUtil.getParentOfType(intentOccurrence, PsiDeclarationStatement.class);
                if (assignmentExpression != null && intentOccurrence.equals(assignmentExpression.getLExpression()) &&
                    assignmentExpression.getRExpression() != null && assignmentExpression.getRExpression() instanceof PsiMethodCallExpression) {
                        isChooserIntent = checkCreateChooserAPI(statements, (PsiMethodCallExpression) assignmentExpression.getRExpression());
                }
                if (!isChooserIntent) {
                    PsiStatement statement = PsiTreeUtil.getParentOfType(intentOccurrence, PsiStatement.class);
                    if (statement != null) {
                        statements.add(statement.getText());
                    }
                }
            }
        } else if (intentExpression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression intentMethodCallExp = (PsiMethodCallExpression) intentExpression;
            checkCreateChooserAPI(statements, intentMethodCallExp);
        } else if (intentExpression instanceof PsiNewExpression) {
            PsiStatement statement = PsiTreeUtil.getParentOfType(intentExpression, PsiStatement.class);
            if (statement != null) {
                statements.add(statement.getText());
            }
        }
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        //This set holds all the possible annotations that are needed for currentMethodCallExpression, but only one is needed per API
        Set<CoconutAnnotationType> possibleAnnotationTypes = new HashSet<>();
        //The actions as defined in the intent declarations
        ArrayList<String> statements = new ArrayList<>();

        //The intentReference refers to the variable that we need to analyze
        assert (source instanceof PsiMethodCallExpression);
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) source;
        // FIXME: now the position of the intent parameter is fixed to 0, need to expose it as a free parameter.
        PsiExpression intentExpression = methodCallExpression.getArgumentList().getExpressions()[0];

        traceAllIntentOccurrences(intentExpression, statements, methodCallExpression);

        // Note that we don't count MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
        // MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE, and MediaStore.INTENT_ACTION_VIDEO_CAMERA, because they
        // just invoke the system Camera app but won't return the result to the app.

        // TODO: handle new intent created in place
        // TODO: handle chooser wrapper
        // TODO: handle extra intents (more than one type of data), see https://stackoverflow.com/questions/16551851/android-intent-for-capturing-both-images-and-videos

        // Note: For actions ended with SECURE, applications responding to this intent must not expose any personal
        //  content like existing photos or videos on the device. The applications should be careful not to share any
        //  photo or video with other applications or internet.
        for(String statement : statements) {
            if (statement.contains("RECORD_SOUND_ACTION")) { // MediaStore.Audio.Media.*
                possibleAnnotationTypes.add(CoconutAnnotationType.MicrophoneAnnotation);
                possibleAnnotationTypes.add(CoconutAnnotationType.StorageAnnotation);
            }
            if (statement.contains("ACTION_IMAGE_CAPTURE") ||
                    statement.contains("ACTION_IMAGE_CAPTURE_SECURE")) { // MediaStore.*
                // TODO: Analyze EXTRA_OUTPUT. The caller may pass an extra EXTRA_OUTPUT to control where this image
                //  will be written. If the EXTRA_OUTPUT is not present, then a small sized image is returned as a
                //  Bitmap object in the extra field. This is useful for applications that only need a small image. If
                //  the EXTRA_OUTPUT is present, then the full-sized image will be written to the Uri value of
                //  EXTRA_OUTPUT.
                // e.g. Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                possibleAnnotationTypes.add(CoconutAnnotationType.CameraAnnotation);
            }
            if (statement.contains("ACTION_VIDEO_CAPTURE")) { // MediaStore.*
                // TODO: the developer may also manually mute the microphone to avoid capturing audio
                // https://stackoverflow.com/questions/55662577/record-video-without-sound-recording-with-default-video-intent-action-video-capt
                possibleAnnotationTypes.add(CoconutAnnotationType.MicrophoneAnnotation);
                possibleAnnotationTypes.add(CoconutAnnotationType.CameraAnnotation);
                possibleAnnotationTypes.add(CoconutAnnotationType.StorageAnnotation);
            }
            if (statement.contains("ACTION_GET_CONTENT") ||
                    statement.contains("ACTION_OPEN_DOCUMENT") ||
                    statement.contains("ACTION_OPEN_DOCUMENT_TREE")) { // Intent.*
                // TODO: verify the URI used for "ACTION_PICK" to determine what type of data is collected in this way
                possibleAnnotationTypes.add(CoconutAnnotationType.UserFileAnnotation);
            }
        }

        ArrayList<AnnotationHolder> annotationHolders = new ArrayList<>();
        for (CoconutAnnotationType annotationType : possibleAnnotationTypes) {
            // TODO: Analyze EXTRA_OUTPUT to generate the content for StorageAnnotation. If EXTRA_OUTPUT is not present
            //  the video will be written to the standard location for videos.
            annotationHolders.add(CodeInspectionUtil.createEmptyAnnotationHolderByType(annotationType));
        }
        return annotationHolders.toArray(new AnnotationHolder[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }

}
