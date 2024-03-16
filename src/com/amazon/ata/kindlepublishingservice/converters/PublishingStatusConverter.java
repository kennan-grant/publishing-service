package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

public class PublishingStatusConverter {

    private PublishingStatusConverter() {}

    public static PublishingStatusRecord toRecord(PublishingStatusItem publishingStatusItem) {
        return PublishingStatusRecord.builder()
                .withStatus(publishingStatusItem.getStatus().name())
                .withStatusMessage(publishingStatusItem.getStatusMessage())
                .withBookId(publishingStatusItem.getBookId())
                .build();
    }
}

