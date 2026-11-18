package com.sshhhdev.dbcommentsearch;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Search Everywhere 를 <b>DB Comment</b> 탭으로 바로 연다.
 * Settings | Keymap 에서 "Search DB Comment" 로 검색해 단축키를 지정할 수 있다.
 * <ul>
 *   <li>에디터에서 텍스트를 선택한 상태면 그 텍스트(한 줄)를 검색어로 채운다.</li>
 *   <li>Search Everywhere 가 이미 열려 있으면 DB Comment 탭으로 전환한다.</li>
 * </ul>
 */
public final class OpenDbCommentSearchAction extends AnAction implements DumbAware {

    private static final int MAX_PREFILL_LENGTH = 100;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        SearchEverywhereManager manager = SearchEverywhereManager.getInstance(project);
        String tabId = DbCommentSearchContributor.PROVIDER_ID;

        if (manager.isShown()) {
            if (!tabId.equals(manager.getSelectedTabID())) {
                manager.setSelectedTabID(tabId);
            }
            return;
        }

        manager.show(tabId, selectedText(e), e);
    }

    private static String selectedText(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return "";
        String text = editor.getSelectionModel().getSelectedText();
        if (text == null) return "";
        text = text.trim();
        // 여러 줄 선택이거나 너무 길면 검색어로 쓰지 않음
        if (text.isEmpty() || text.contains("\n") || text.length() > MAX_PREFILL_LENGTH) return "";
        return text;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
