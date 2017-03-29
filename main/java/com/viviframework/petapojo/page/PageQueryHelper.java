package com.viviframework.petapojo.page;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class PageQueryHelper {

    private final static Pattern PATTERN_BRACKET = Pattern.compile("(\\(|\\)|[^\\(\\)]*)");
    private final static Pattern PATTERN_SELECT = Pattern.compile("select([\\W\\w]*)from", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS | Pattern.MULTILINE);
    private final static Pattern PATTERN_DISTINCT = Pattern.compile("\\ADISTINCT\\s", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS | Pattern.MULTILINE);

    public static PageQueryInfo buildQueryInfo(long skip, int take, String sql) {

        Matcher matcherCols = PATTERN_BRACKET.matcher(sql);
        List<String> tempList = new ArrayList<>();
        while (matcherCols.find()) {
            tempList.add(matcherCols.group());
        }
        List<String> copyList = new ArrayList<>();
        tempList.forEach(copyList::add);

        while (true) {
            int firstEnd = IntStream.range(0, copyList.size())
                    .filter(i -> copyList.get(i).toLowerCase().contains(")"))
                    .findFirst()
                    .orElse(-1);
            if (firstEnd < 0)
                break;

            int firstStart = IntStream.range(0, firstEnd)
                    .filter(i -> copyList.get(i).toLowerCase().contains("("))
                    .reduce((a, b) -> b)
                    .orElse(-1);
            if (firstStart < 0)
                break;

            IntStream.range(firstStart, firstEnd + 1)
                    .forEach(i -> copyList.set(i, ""));
        }

        int index = IntStream.range(0, copyList.size())
                .filter(i -> copyList.get(i).toLowerCase().contains("select"))
                .findFirst()
                .orElse(-1);
        int end = IntStream.range(0, copyList.size())
                .filter(i -> copyList.get(i).toLowerCase().contains("from)"))
                .findFirst()
                .orElse(-1);

        String sqlReplace = "";
        for (int i = index; i <= end; i++) {
            sqlReplace += tempList.get(i);
        }

        Matcher matcherSelect = PATTERN_SELECT.matcher(sqlReplace);
        String sqlSelectCols = null;
        int colsStartIndex = -1;
        int colsEndIndex = -1;
        if (matcherSelect.find()) {
            sqlSelectCols = matcherSelect.group(1);
            colsStartIndex = matcherSelect.start(1);
            colsEndIndex = matcherSelect.end(1);
        }

        String sqlCount = "";
        Matcher matcherDistinct = PATTERN_DISTINCT.matcher(sqlSelectCols);
        if (matcherDistinct.matches()) {
            sqlCount = String.format("%s COUNT(%s) %s", sql.substring(0, colsStartIndex), sqlSelectCols, sql.substring(colsEndIndex));
        } else {
            sqlCount = String.format("%s COUNT(1) %s", sql.substring(0, colsStartIndex), sql.substring(colsEndIndex));
        }

        String sqlPage = String.format("%s\nLIMIT %s OFFSET %s", sql, take, skip);

        PageQueryInfo queryInfo = new PageQueryInfo();
        queryInfo.setPageSql(sqlPage);
        queryInfo.setCountSql(sqlCount);
        return queryInfo;
    }

}
