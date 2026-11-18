package com.sshhhdev.dbcommentsearch;

import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.util.DasUtil;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Search Everywhere 에 "DB Comment" 탭을 추가한다.
 * Database 도구 창의 모든 데이터 소스에서 테이블·뷰·컬럼의 comment(또는 이름)로 검색한다.
 */
public final class DbCommentSearchContributor implements WeightedSearchEverywhereContributor<DbCommentItem> {

    /** Search Everywhere 탭 ID. {@link OpenDbCommentSearchAction}에서 이 탭을 바로 열 때 사용. */
    public static final String PROVIDER_ID = "DbCommentSearch";

    private static final int MAX_RESULTS = 300;

    private final Project project;

    public DbCommentSearchContributor(@NotNull Project project) {
        this.project = project;
    }

    // ---------------------------------------------------------------- 기본 정보

    @Override
    public @NotNull String getSearchProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public @NotNull String getGroupName() {
        return "DB Comment";
    }

    @Override
    public @Nullable String getAdvertisement() {
        return "테이블·컬럼 comment로 검색합니다. 공백으로 여러 단어 AND 검색";
    }

    @Override
    public int getSortWeight() {
        return 900;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public boolean isShownInSeparateTab() {
        return true;
    }

    @Override
    public boolean isEmptyPatternSupported() {
        return false;
    }

    // ---------------------------------------------------------------- 검색

    @Override
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator indicator,
                              @NotNull Processor<? super DbCommentItem> consumer) {
        fetchWeightedElements(pattern, indicator, d -> consumer.process(d.getItem()));
    }

    @Override
    public void fetchWeightedElements(@NotNull String pattern, @NotNull ProgressIndicator indicator,
                                      @NotNull Processor<? super FoundItemDescriptor<DbCommentItem>> consumer) {
        String query = pattern.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty() || project.isDisposed()) return;
        String[] tokens = query.split("\\s+");

        List<FoundItemDescriptor<DbCommentItem>> found =
                ReadAction.compute(() -> collect(query, tokens, indicator));

        found.sort(Comparator.<FoundItemDescriptor<DbCommentItem>>comparingInt(FoundItemDescriptor::getWeight)
                .reversed()
                .thenComparing(d -> d.getItem().displayName));

        int count = 0;
        for (FoundItemDescriptor<DbCommentItem> d : found) {
            indicator.checkCanceled();
            if (!consumer.process(d) || ++count >= MAX_RESULTS) break;
        }
    }

    private List<FoundItemDescriptor<DbCommentItem>> collect(String query, String[] tokens,
                                                             ProgressIndicator indicator) {
        List<FoundItemDescriptor<DbCommentItem>> result = new ArrayList<>();
        for (DbDataSource ds : DbPsiFacade.getInstance(project).getDataSources()) {
            for (DasTable table : DasUtil.getTables(ds)) {
                indicator.checkCanceled();

                String tableName = table.getName();
                String tableComment = StringUtil.notNullize(table.getComment());
                int w = score(query, tokens, tableName, tableComment);
                if (w > 0) {
                    result.add(new FoundItemDescriptor<>(
                            toItem(ds, table, tableName, tableComment, false), w + 50));
                }

                for (DasColumn column : DasUtil.getColumns(table)) {
                    String colComment = StringUtil.notNullize(column.getComment());
                    // 컬럼은 comment가 있는 것만 대상 (이름 검색은 기본 기능으로 충분)
                    if (colComment.isEmpty()) continue;
                    int cw = score(query, tokens, column.getName(), colComment);
                    if (cw > 0) {
                        result.add(new FoundItemDescriptor<>(
                                toItem(ds, column, tableName + "." + column.getName(), colComment, true), cw));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 0 이면 불일치. comment 일치가 이름 일치보다 높은 점수.
     */
    private static int score(String query, String[] tokens, String name, String comment) {
        String c = comment.toLowerCase(Locale.ROOT);
        String n = StringUtil.notNullize(name).toLowerCase(Locale.ROOT);

        if (!c.isEmpty()) {
            if (c.equals(query)) return 1000;
            if (c.startsWith(query)) return 800;
            if (c.contains(query)) return 600;
        }
        if (n.equals(query)) return 500;

        String all = n + " " + c;
        for (String t : tokens) {
            if (!all.contains(t)) return 0;
        }
        return c.isEmpty() ? 200 : 400;
    }

    private static DbCommentItem toItem(DbDataSource ds, DasObject obj, String displayName,
                                        String comment, boolean column) {
        Icon icon = null;
        DbElement element = ds.findElement(obj);
        if (element != null) icon = element.getIcon(0);

        String schema = DasUtil.getSchema(obj);
        String location = StringUtil.isEmpty(schema) ? ds.getName() : ds.getName() + " / " + schema;
        return new DbCommentItem(ds, obj, displayName, comment, location, icon, column);
    }

    // ---------------------------------------------------------------- 선택 / 표시

    @Override
    public boolean processSelectedItem(@NotNull DbCommentItem item, int modifiers, @NotNull String searchText) {
        DbElement element = ReadAction.compute(() -> item.dataSource.findElement(item.dasObject));
        if (element != null && element.canNavigate()) {
            element.navigate(true); // Database 도구 창의 해당 객체로 이동
        }
        return true;
    }

    @Override
    public @NotNull ListCellRenderer<? super DbCommentItem> getElementsRenderer() {
        return new ColoredListCellRenderer<DbCommentItem>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends DbCommentItem> list, DbCommentItem item,
                                                 int index, boolean selected, boolean hasFocus) {
                if (item == null) return;
                setIcon(item.icon);
                append(item.displayName, item.column
                        ? SimpleTextAttributes.REGULAR_ATTRIBUTES
                        : SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                if (!item.comment.isEmpty()) {
                    append("  " + item.comment, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                }
                append("   " + item.location, SimpleTextAttributes.GRAYED_ATTRIBUTES);
            }
        };
    }

    @Override
    public @Nullable Object getDataForItem(@NotNull DbCommentItem element, @NotNull String dataId) {
        return null;
    }

    // ---------------------------------------------------------------- Factory

    public static final class Factory implements SearchEverywhereContributorFactory<DbCommentItem> {
        @Override
        public @NotNull SearchEverywhereContributor<DbCommentItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new DbCommentSearchContributor(java.util.Objects.requireNonNull(initEvent.getProject()));
        }
    }
}
