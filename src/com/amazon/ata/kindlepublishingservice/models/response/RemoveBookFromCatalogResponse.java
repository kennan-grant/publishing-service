package com.amazon.ata.kindlepublishingservice.models.response;

import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.models.BookRecommendation;

import java.util.List;

public class RemoveBookFromCatalogResponse {
    private final Book book;
    private final List<BookRecommendation> recommendations;

    // Private constructor used by the builder
    private RemoveBookFromCatalogResponse(Book book, List<BookRecommendation> recommendations) {
        this.book = book;
        this.recommendations = recommendations;
    }

    public Book getBook() {
        return book;
    }

    public List<BookRecommendation> getRecommendations() {
        return recommendations;
    }

    // Static builder class
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Book book;
        private List<BookRecommendation> recommendations;

        public Builder withBook(Book book) {
            this.book = book;
            return this;
        }

        public Builder withRecommendations(List<BookRecommendation> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public RemoveBookFromCatalogResponse build() {
            return new RemoveBookFromCatalogResponse(book, recommendations);
        }
    }
}

