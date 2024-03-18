package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.recommendationsservice.types.BookGenre;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {
    BookPublishRequestManager bookPublishRequestManager;
    PublishingStatusDao publishingStatusDao;
    CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                           PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    public void run() {
        BookPublishRequest bookPublishRequest = null;
        try {
            CatalogItemVersion newVersion;
            bookPublishRequest = bookPublishRequestManager.getBookPublishRequestToProcess();
            if (bookPublishRequest == null) {
                return;
            }
            publishingStatusDao.addPublishingStatusItem(bookPublishRequest,
                    PublishingRecordStatus.IN_PROGRESS);

            Integer prevVersion = 0;
            CatalogItemVersion lastVersion = catalogDao.getLatestVersionOfBook(bookPublishRequest.getBookId());
            if (lastVersion != null) {
                prevVersion = lastVersion.getVersion();
                catalogDao.setInactive(lastVersion);
            }
            newVersion = catalogDao.addToCatalog(bookPublishRequest, 1 + prevVersion);
            publishingStatusDao.addPublishingStatusItem(
                    bookPublishRequest,
                    newVersion,
                    PublishingRecordStatus.SUCCESSFUL);
        } catch (Exception e) {
            publishingStatusDao.addPublishingStatusItem(
                    bookPublishRequest,
                    PublishingRecordStatus.FAILED);
        }
    }
}
