package com.viviframework.petapojo.page;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页泛型
 */
public class PageInfo<T> {
    private int currentPage;
    private int totalPages;
    private int totalItems;
    private int itemsPrePage;
    private List<T> items;

    public PageInfo() {
        items = new ArrayList<>();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getItemsPrePage() {
        return itemsPrePage;
    }

    public void setItemsPrePage(int itemsPrePage) {
        this.itemsPrePage = itemsPrePage;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
