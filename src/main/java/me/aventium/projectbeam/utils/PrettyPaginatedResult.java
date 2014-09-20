package me.aventium.projectbeam.utils;

import com.sk89q.minecraft.util.pagination.PaginatedResult;

public abstract class PrettyPaginatedResult<T> extends PaginatedResult<T> {
    protected final String header;

    public PrettyPaginatedResult(String header) {
        this(header, 6);
    }

    public PrettyPaginatedResult(String header, int resultsPerPage) {
        super(resultsPerPage);
        this.header = header;
    }

    @Override
    public String formatHeader(int page, int maxPages) {
        String textColor = "§2";
        String highlight = "§a";

        String message = textColor + this.header + textColor + " (page " + highlight + page + textColor + " of " + highlight + maxPages + textColor + ")";
        return message;
    }
}
