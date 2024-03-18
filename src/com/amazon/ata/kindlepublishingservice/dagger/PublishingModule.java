package com.amazon.ata.kindlepublishingservice.dagger;

import com.amazon.ata.kindlepublishingservice.publishing.BookPublisher;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishTask;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import dagger.Module;
import dagger.Provides;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;

@Module
public class PublishingModule {

    @Provides
    @Singleton
    public BookPublisher provideBookPublisher(ScheduledExecutorService scheduledExecutorService,
                                              BookPublishRequestManager bookPublishRequestManager,
                                              PublishingStatusDao publishingStatusDao,
                                              CatalogDao catalogDao) {
        return new BookPublisher(scheduledExecutorService,
                new BookPublishTask(bookPublishRequestManager, publishingStatusDao, catalogDao));
    }

    @Provides
    @Singleton
    public ScheduledExecutorService provideBookPublisherScheduler() {
        return Executors.newScheduledThreadPool(1);
    }

    // Assuming the dependencies (BookPublishRequestManager, PublishingStatusDao, CatalogDao) are provided elsewhere in your module or another module.
}
