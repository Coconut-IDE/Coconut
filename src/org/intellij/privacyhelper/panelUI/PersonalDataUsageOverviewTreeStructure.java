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

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by tianshi on 1/9/17.
 */
public final class PersonalDataUsageOverviewTreeStructure extends AbstractTreeStructureBase {
  protected PersonalDataUsageOverviewTreeBuilder myBuilder;
  protected AbstractTreeNode myRootElement;

  public PersonalDataUsageOverviewTreeStructure(final Project project) {
    super(project);
  }

  final void setTreeBuilder(PersonalDataUsageOverviewTreeBuilder builder) {
    myBuilder = builder;
    myRootElement = createRootElement();
  }

  @Nullable
  @Override
  public List<TreeStructureProvider> getProviders() {
    return null;
  }

  @Override
  public Object getRootElement() {
    return myRootElement;
  }

  public AbstractTreeNode createRootElement() {
    return new RootNode(myProject, new Object(), myBuilder);
  }

  @Override
  public void commit() {

  }

  @Override
  public boolean hasSomethingToCommit() {
    return false;
  }
}
