package org.intellij.privacyhelper.codeInspection.instances;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;

/**
 * Created by tianshi on 5/11/17.
 */
public class ThirdPartyLibInstance {
    SmartPsiElementPointer psiElementPointer;
    String gradleString;

    public ThirdPartyLibInstance(SmartPsiElementPointer smartPsiElementPointer) {
        this.psiElementPointer = smartPsiElementPointer;
        if (smartPsiElementPointer.getElement() != null) {
            this.gradleString = smartPsiElementPointer.getElement().getText();
        }
    }

    public SmartPsiElementPointer getPsiElementPointer() {
        return psiElementPointer;
    }

    public boolean isValid() {
        // TODO: (double-check) seems not the standard way
        return psiElementPointer.getRange() != null && psiElementPointer.getElement() != null;
    }
}
