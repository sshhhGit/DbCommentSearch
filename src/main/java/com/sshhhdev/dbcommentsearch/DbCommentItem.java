package com.sshhhdev.dbcommentsearch;

import com.intellij.database.model.DasObject;
import com.intellij.database.psi.DbDataSource;

import javax.swing.Icon;

/**
 * Search Everywhere 목록에 표시되는 한 줄(테이블·뷰·컬럼).
 * 검색 시점에 표시용 문자열을 미리 계산해 두고, 선택 시 {@link #dasObject}로 Database 트리 요소를 다시 찾는다.
 */
public final class DbCommentItem {
    final DbDataSource dataSource;
    final DasObject dasObject;
    final String displayName;   // TABLE  또는  TABLE.COLUMN
    final String comment;       // null 아님 (없으면 "")
    final String location;      // 데이터 소스 / 스키마
    final Icon icon;
    final boolean column;

    DbCommentItem(DbDataSource dataSource, DasObject dasObject, String displayName,
                  String comment, String location, Icon icon, boolean column) {
        this.dataSource = dataSource;
        this.dasObject = dasObject;
        this.displayName = displayName;
        this.comment = comment;
        this.location = location;
        this.icon = icon;
        this.column = column;
    }

    @Override
    public String toString() {
        return comment.isEmpty() ? displayName : displayName + " - " + comment;
    }
}
