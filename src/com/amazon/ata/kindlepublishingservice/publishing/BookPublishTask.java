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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.UUID;

public class BookPublishTask implements Runnable {
    BookPublishRequestManager bookPublishRequestManager;
    PublishingStatusDao publishingStatusDao;
    CatalogDao catalogDao;
    private static final Logger LOGGER = LogManager.getLogger(BookPublisher.class);

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                           PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    public void run() {
        BookPublishRequest bookPublishRequest = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (bookPublishRequest == null) {
            return;
        }
        publishingStatusDao.setPublishingStatus(
                bookPublishRequest.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS,
                bookPublishRequest.getBookId()
        );
        KindleFormattedBook kindleFormattedBook = KindleFormatConverter.format(bookPublishRequest);
        try {
            CatalogItemVersion newVersion = catalogDao.createOrUpdateBook(kindleFormattedBook);
            publishingStatusDao.setPublishingStatus(
                    bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL,
                    newVersion.getBookId() //// this part is crucial. Must get from new version, in case created.
            );
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            publishingStatusDao.setPublishingStatus(
                    bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED,
                    bookPublishRequest.getBookId(),
                    e.getMessage()
            );
        }
    }
}
