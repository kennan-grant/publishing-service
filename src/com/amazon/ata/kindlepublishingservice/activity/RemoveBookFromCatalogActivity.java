package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.CatalogItemConverter;
import com.amazon.ata.kindlepublishingservice.converters.RecommendationsCoralConverter;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.models.requests.RemoveBookFromCatalogRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetBookResponse;
import com.amazon.ata.kindlepublishingservice.models.response.RemoveBookFromCatalogResponse;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import com.amazon.ata.recommendationsservice.types.BookRecommendation;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.List;

public class RemoveBookFromCatalogActivity {
    @Inject
    RemoveBookFromCatalogActivity() {}
    public RemoveBookFromCatalogResponse execute(RemoveBookFromCatalogRequest removeBookFromCatalogRequest) {
        CatalogItemVersion catalogItem = catalogDao.getBookFromCatalog(request.getBookId());
        List<BookRecommendation> recommendations = recommendationServiceClient.getBookRecommendations(
                BookGenre.valueOf(catalogItem.getGenre().name()));

        return RemoveBookFromCatalogResponse.builder()
                .withBook(CatalogItemConverter.toBook(catalogItem))
                .withRecommendations(RecommendationsCoralConverter.toCoral(recommendations))
                .build();
    }
}
