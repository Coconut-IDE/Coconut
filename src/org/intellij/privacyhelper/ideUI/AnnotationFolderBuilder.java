package org.intellij.privacyhelper.ideUI;

import android.icu.text.CaseMap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnnotationFolderBuilder extends FoldingBuilderEx {
    static final private List<String> COCONUT_ANNOTATIONS = Arrays.asList(
            "@LocationAnnotation",
            "@NetworkAnnotation",
            "@StorageAnnotation",
            "@UniqueIdentifierAnnotation",
            "@NotPersonalData",
            "@AdmobAnnotation",
            "@UndefinedPersonalDataTypeAnnotation"
    );

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
        Collection<PsiAnnotation> annotations =
                PsiTreeUtil.findChildrenOfType(root, PsiAnnotation.class);

        for(final PsiAnnotation annotation : annotations) {
            if(isCoconutAnnotation(annotation.getText())) {
                descriptors.add(createFoldingDescriptor(annotation));
            }
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private boolean isCoconutAnnotation(String annotationText) {
        for(final String annotation : COCONUT_ANNOTATIONS) {
            if(annotationText.contains(annotation)) {
                return true;
            }
        }

        return false;
    }

    private FoldingDescriptor createFoldingDescriptor(PsiAnnotation annotation) {
        return new FoldingDescriptor(annotation.getNode(), annotation.getTextRange()) {
            @Nullable
            @Override
            public String getPlaceholderText() {
                // IMPORTANT: keys can come with no values, so a test for null is needed
                // IMPORTANT: Convert embedded \n to backslash n, so that the string will look like it has LF embedded
                // in it and embedded " to escaped "
                return trim(annotation) + " ...";
            }

            private String trim(PsiAnnotation annotationToTrim) {
                ASTNode[] nodes = annotationToTrim.getNode().getChildren(TokenSet.ANY);

                try {
                    return nodes[1].getText();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                return annotationToTrim.getQualifiedName()
                                       .replaceAll("me.tianshili.annotationlib", "");
            }
        };
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }
}