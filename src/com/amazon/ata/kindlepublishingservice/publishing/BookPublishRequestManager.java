package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Queue;

public class BookPublishRequestManager {
    Queue<BookPublishRequest> bookPublishRequestQueue;

    @Inject
    public BookPublishRequestManager() {
        this.bookPublishRequestQueue = new ArrayDeque<>();
    }

    public void addBookPublishRequest(BookPublishRequest request) {
        this.bookPublishRequestQueue.offer(request);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        return this.bookPublishRequestQueue.isEmpty() ? null : this.bookPublishRequestQueue.poll();
    }

}