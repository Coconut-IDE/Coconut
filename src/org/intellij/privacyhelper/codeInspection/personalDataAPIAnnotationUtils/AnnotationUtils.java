package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Created by tianshi on 2/4/18.
 */
public class AnnotationUtils {
    static public  <T extends PsiElement> T[] traverseChildrenOfPsiElementType(@Nullable PsiElement element, @NotNull Class<T> aClass) {
        ArrayList<T> results = new ArrayList<T>();
        if (aClass.isInstance(element) ) {
            results.add((T) element);
        }
        if (element == null) {
            return ArrayUtil.toObjectArray(results, aClass);
        } else if (element.getChildren().length == 0) {
            return ArrayUtil.toObjectArray(results, aClass);
        }
        for (PsiElement child : element.getChildren()) {
            T [] childResults = traverseChildrenOfPsiElementType(child, aClass);
            results.addAll(Arrays.asList(childResults));
        }
        return ArrayUtil.toObjectArray(results, aClass);
    }

    static public PsiElement[] trackNonPsiMethodResolvedElementsOfJavaType(@Nullable PsiElement element, String[] JavaTypePatterns) {
        ArrayList<PsiElement> results = new ArrayList<>();
        if (element == null) {
            return ArrayUtil.toObjectArray(results, PsiElement.class);
        } else if (element.getChildren().length == 0) {
            return ArrayUtil.toObjectArray(results, PsiElement.class);
        }
        PsiReferenceExpression[] childExpressions = traverseChildrenOfPsiElementType(PsiTreeUtil.getParentOfType(element, PsiStatement.class),
                PsiReferenceExpression.class);
        for (PsiReferenceExpression childExpression : childExpressions) {
            PsiElement resolvedVariable = childExpression.resolve();
            if (resolvedVariable == null) {
                continue;
            }
            if (resolvedVariable instanceof PsiMethod) {
                continue;
            }
            if (childExpression.getType() == null) {
                continue;
            }
            for (String pattern : JavaTypePatterns) {
                if (Pattern.compile(pattern, Pattern.DOTALL).matcher(childExpression.getType().getCanonicalText()).matches()) {
                    results.add(resolvedVariable);
                    if (JavaTypePatterns.length > 1) {
                        PsiReference[] references = ReferencesSearch.search(resolvedVariable).findAll().toArray(new PsiReference[0]);
                        for (PsiReference reference : references) {
                            Collections.addAll(results, trackNonPsiMethodResolvedElementsOfJavaType(reference.getElement(), Arrays.copyOfRange(JavaTypePatterns, 1, JavaTypePatterns.length)));
                        }
                        Collections.addAll(results, trackNonPsiMethodResolvedElementsOfJavaType(resolvedVariable, Arrays.copyOfRange(JavaTypePatterns, 1, JavaTypePatterns.length)));
                    }
                }
            }
        }

        return ArrayUtil.toObjectArray(results, PsiElement.class);
    }


    static public ArrayList<PsiMethodCallExpression> getRelevantMethodCallExpression(String methodCallNamePattern, String[] relevantVariableTypeList, PsiElement currentElement) {
        ArrayList<PsiMethodCallExpression> possibleFitList = new ArrayList<>();
        PsiMethodCallExpression[] methodCallExpressions = traverseChildrenOfPsiElementType(currentElement, PsiMethodCallExpression.class);
        for (PsiMethodCallExpression expression : methodCallExpressions) {
            if (!possibleFitList.contains(expression) && Pattern.compile(methodCallNamePattern, Pattern.DOTALL).matcher(expression.getMethodExpression().getText()).matches()) {
                possibleFitList.add(expression);
            }
        }
        PsiElement [] resolvedElements = trackNonPsiMethodResolvedElementsOfJavaType(currentElement, relevantVariableTypeList);
        for (PsiElement resolvedElement : resolvedElements) {
            methodCallExpressions = traverseChildrenOfPsiElementType(PsiTreeUtil.getParentOfType(resolvedElement, PsiStatement.class), PsiMethodCallExpression.class);
            for (PsiMethodCallExpression expression : methodCallExpressions) {
                if (!possibleFitList.contains(expression) && Pattern.compile(methodCallNamePattern, Pattern.DOTALL).matcher(expression.getMethodExpression().getText()).matches()) {
                    possibleFitList.add(expression);
                }
            }
            PsiReference [] references = ReferencesSearch.search(resolvedElement).findAll().toArray(new PsiReference[0]);
            for (PsiReference reference : references) {
                methodCallExpressions = traverseChildrenOfPsiElementType(PsiTreeUtil.getParentOfType(reference.getElement(), PsiStatement.class),
                        PsiMethodCallExpression.class);
                for (PsiMethodCallExpression expression : methodCallExpressions) {
                    if (!possibleFitList.contains(expression) && Pattern.compile(methodCallNamePattern, Pattern.DOTALL).matcher(expression.getMethodExpression().getText()).matches()) {
                        possibleFitList.add(expression);
                    }
                }
            }
        }
        return possibleFitList;
    }
}
