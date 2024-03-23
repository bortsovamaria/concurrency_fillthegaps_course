package course.concurrency.m2_async.minPrice;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        ExecutorService executor = Executors.newCachedThreadPool();

        List<CompletableFuture<Double>> completableFutureList =
                shopIds.stream().map(shopId ->
                                CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                                        .completeOnTimeout(Double.POSITIVE_INFINITY, 2900, TimeUnit.MILLISECONDS)
                                        .exceptionally(ex -> Double.POSITIVE_INFINITY))
                        .collect(toList());

        return completableFutureList
                .stream()
                .mapToDouble(CompletableFuture::join)
                .filter(Double::isFinite)
                .min()
                .orElse(Double.NaN);
    }
}
