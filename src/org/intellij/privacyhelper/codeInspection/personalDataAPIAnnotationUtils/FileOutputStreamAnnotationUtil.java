package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianshi on 2/3/18.
 */
public class FileOutputStreamAnnotationUtil extends PersonalDataAPIAnnotationUtil {
//    internal storage:
//    String FILENAME = "hello_file";
//    String string = "hello world!";
//
//    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
//    fos.write(string.getBytes());
//    fos.close();

//    external storage:
//    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/"+Constant.DIR_DOWNLOAD+"/"+filePath);
//    if(!file.getParentFile().exists()){
//        file.getParentFile().mkdirs();
//    }
//    if(!file.exists())file.createNewFile();
//    LogUtils.d("pic path:"+file.getAbsolutePath());
//    fileOutputStream = new FileOutputStream(file);
//    fileOutputStream.write(buffer);


    private static final Map<String, Integer> accessModeParameterPositionMap =
            Collections.unmodifiableMap(new HashMap<String, Integer>() {{
                put(".*openFileOutput", 0);
                put(".*getDir", 1);
            }});

    private static final String[] accessModeRelevantVariableTypeList = new String[] {".*OutputStream", ".*File"};


    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder modeParameterAnnotationHolder =  StorageUtils.createAPIAnnotationSpeculationByModeParameter(source, accessModeRelevantVariableTypeList, accessModeParameterPositionMap);
        AnnotationHolder externalStorageAnnotationHolder = StorageUtils.createAnnotationSpeculationByAPIName(source);
        ArrayList<AnnotationHolder> annotationHolders = new ArrayList<>();
        annotationHolders.add(modeParameterAnnotationHolder);
        annotationHolders.add(externalStorageAnnotationHolder);
        AnnotationHolder mergedResult = CodeInspectionUtil.combineVirtualAnnotationHolders(annotationHolders);
        return mergedResult;
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
        return null;
    }
}
