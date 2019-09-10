/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.privacyhelper.panelUI;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HighlightedRegion;
import com.intellij.usageView.UsageTreeColors;
import com.intellij.usageView.UsageTreeColorsScheme;
import org.intellij.privacyhelper.codeInspection.instances.*;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tianshi on 1/11/17.
 */
public class PersonalDataGroupItemNode extends BaseNode implements HighlightedRegionProvider {
  PersonalDataInstance myAPICallInstance;
  private PersonalDataGroup myTopLevelGroup;

  private final ArrayList<HighlightedRegion> myHighlightedRegions;

  protected PersonalDataGroupItemNode(Project project, PersonalDataInstance instance, AbstractTreeBuilder builder,
                                      PersonalDataGroup topLevelGroup) {
    super(project, instance, builder);
    myHighlightedRegions = new ArrayList<>(1);
    myAPICallInstance = instance;
    myTopLevelGroup = topLevelGroup;
  }

  @Override
  public Iterable<HighlightedRegion> getHighlightedRegions() {
    return myHighlightedRegions;
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    return new ArrayList<>();
  }

  @Override
  protected void update(PresentationData presentationData) {
    String apiName = myAPICallInstance.getPersonalDataAPI().getDisplayName();
    String descriptionText = "";
    if (myAPICallInstance.getAnnotationMetaDataList() != null) {
      descriptionText = myAPICallInstance.getDescriptionByGroup(myTopLevelGroup);
    }
    myHighlightedRegions.clear();
    myHighlightedRegions.add(new HighlightedRegion(0, apiName.length(), new TextAttributes()));
    EditorColorsScheme colorsScheme = UsageTreeColorsScheme.getInstance().getScheme();
    myHighlightedRegions.add(new HighlightedRegion(
            apiName.length() + 1, apiName.length() + 1 + descriptionText.length(),
            colorsScheme.getAttributes(UsageTreeColors.NUMBER_OF_USAGES)));

    presentationData.setPresentableText(apiName + " " + descriptionText);
  }
}
