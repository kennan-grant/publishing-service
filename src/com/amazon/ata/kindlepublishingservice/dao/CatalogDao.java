package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    public void validateBookExists(String bookId) {
        if (getLatestVersionOfBook(bookId) == null) {
            throw new BookNotFoundException("Book with id " + bookId + " not found or already inactive.");
        }
    }

    public CatalogItemVersion removeBookFromCatalog(String bookId) {
        CatalogItemVersion latestVersion = getLatestVersionOfBook(bookId);
        if (latestVersion == null || latestVersion.isInactive()) {
            throw new BookNotFoundException("Book with id " + bookId + " not found or already inactive.");
        }
        latestVersion.setInactive(true);
        dynamoDbMapper.save(latestVersion);
        return latestVersion;
    }

    // Returns null if no version exists for the provided bookId
    public CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public void setInactive(CatalogItemVersion catalogItemVersion) {
        catalogItemVersion.setInactive(true);
        dynamoDbMapper.save(catalogItemVersion);
    }

    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook kindleFormattedBook) {
        CatalogItemVersion newOrUpdatedItem = new CatalogItemVersion();
        int newVersionNumber = 0;

        if (kindleFormattedBook.getBookId() != null) {
            validateBookExists(kindleFormattedBook.getBookId());
            CatalogItemVersion prevVersionItem = getLatestVersionOfBook(kindleFormattedBook.getBookId());
            newVersionNumber = prevVersionItem.getVersion();
            setInactive(prevVersionItem);
        } else {
            newOrUpdatedItem.setBookId(KindlePublishingUtils.generateBookId());
        }

        newOrUpdatedItem.setVersion(1 + newVersionNumber);

        newOrUpdatedItem.setBookId(kindleFormattedBook.getBookId());
        newOrUpdatedItem.setTitle(kindleFormattedBook.getTitle());
        newOrUpdatedItem.setAuthor(kindleFormattedBook.getAuthor());
        newOrUpdatedItem.setText(kindleFormattedBook.getText());
        newOrUpdatedItem.setGenre(kindleFormattedBook.getGenre());

        dynamoDbMapper.save(newOrUpdatedItem);
        return newOrUpdatedItem;
    }
}
