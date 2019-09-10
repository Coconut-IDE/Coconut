package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.util.CharTable;

/**
 * Created by tianshi on 1/18/18.
 */
public class CoconutUIUtil {
    public static void navigateMainEditorToPsiElement(SmartPsiElementPointer element) {
        VirtualFile virtualFile = element.getVirtualFile();
        Project myProject = element.getProject();
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(myProject, virtualFile,
                element.getRange().getStartOffset());

        ApplicationManager.getApplication()
                          .runWriteAction(() -> openFileDescriptor.navigate(false));
    }

    public static void navigateMainEditorToPsiElement(PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        Project myProject = element.getProject();
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(myProject, virtualFile,
                element.getTextRange().getStartOffset());

        ApplicationManager.getApplication()
                          .runWriteAction(() -> openFileDescriptor.navigate(false));
    }

    public static PsiWhiteSpace nl(PsiElement context)
    {
        PsiWhiteSpace newline;
        while (!(context instanceof ASTNode))
        {
            context = context.getFirstChild ();
            if (context == null)
                return null;
        }

        CharTable
                charTable = SharedImplUtil.findCharTableByTree (
                (ASTNode) context);
        newline = (PsiWhiteSpace) Factory
                .createSingleLeafElement(TokenType.WHITE_SPACE, "\n",
                        charTable, PsiManager.getInstance (context.getProject()));

        return newline;
    }


}
