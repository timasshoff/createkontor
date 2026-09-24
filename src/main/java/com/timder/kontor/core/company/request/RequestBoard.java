package com.timder.kontor.core.company.request;

import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.value.ItemId;

import java.util.*;

public final class RequestBoard {

    private final Map<ItemId, List<Request>> requestsByProduct = new LinkedHashMap<>();
    private final Map<ItemId, Integer> lostToday = new LinkedHashMap<>();

    /**
     * Whether a new request for this product could be placed right now
     * @param product The product a new request would be for
     * @param legalForm The company's current legal form
     * @return True, if neither the products own limit nor the company's total limit is reached
     */
    public boolean hasRoom(ItemId product, LegalFormDef legalForm) {
        Objects.requireNonNull(product, "product must not be null.");
        Objects.requireNonNull(legalForm, "legalForm must not be null.");

        if (openRequestsForProduct(product) >= legalForm.maxOpenRequestsPerProduct()) {
            return false;
        }
        return openRequestsTotal() < legalForm.maxOpenRequestsTotal();
    }

    /**
     * Adds an already-rolled request to the board
     * @param request The request to add
     * @param legalForm The company's current legal form
     * @throws IllegalStateException If the board has no room for this product
     */
    public void add(Request request, LegalFormDef legalForm) {
        Objects.requireNonNull(request, "request must not be null.");

        if (request.getQuantity() > legalForm.maxOrderQuantity()) {
            throw new IllegalStateException("The request quantity exceeds the legal form limit.");
        }
        if (!hasRoom(request.getProduct(), legalForm)) {
            throw new IllegalStateException("The board has no room for " + request.getProduct());
        }
        requestsByProduct.computeIfAbsent(request.getProduct(), p -> new ArrayList<>()).add(request);
    }

    /**
     * Counts a request that could not be placed.
     * @param product The product the lost request was for
     */
    public void recordLostRequest(ItemId product) {
        Objects.requireNonNull(product, "product must not be null.");
        lostToday.merge(product, 1, Integer::sum);
    }

    /**
     * @param product The product
     * @return How many requests for this product were lost today because the board was full
     */
    public int lostRequestsToday(ItemId product) {
        return lostToday.getOrDefault(product, 0);
    }

    /**
     * Resets every product's lost-request counter.
     */
    public void resetLostRequestsToday() {
        lostToday.clear();
    }

    public int openRequestsForProduct(ItemId product) {
        return requestsByProduct.getOrDefault(product, List.of()).size();
    }

    public int openRequestsTotal() {
        int total = 0;
        for (List<Request> requests : requestsByProduct.values()) {
            total += requests.size();
        }
        return total;
    }

    public List<Request> openRequests(ItemId product) {
        return List.copyOf(requestsByProduct.getOrDefault(product, List.of()));
    }

    public List<Request> allOpenRequests() {
        List<Request> all = new ArrayList<>();
        for (List<Request> requests : requestsByProduct.values()) {
            all.addAll(requests);
        }
        return List.copyOf(all);
    }

    /**
     * Removes a request from the board so it can be turned into an order
     * @param requestNumber The number of the request to accept
     * @return The accepted request
     * @throws NoSuchElementException If no open request with this number exists
     */
    public Request accept(long requestNumber) {
        Request request = get(requestNumber);
        requestsByProduct.get(request.getProduct()).remove(request);
        return request;
    }

    private Request remove(long requestNumber) {
        for (List<Request> requests : requestsByProduct.values()) {
            Iterator<Request> it = requests.iterator();
            while (it.hasNext()) {
                Request request = it.next();
                if (request.getNumber() == requestNumber) {
                    it.remove();
                    return request;
                }
            }
        }
        throw new NoSuchElementException("No open request with number " + requestNumber + ".");
    }

    public Request get(long requestNumber) {
        for (List<Request> requests : requestsByProduct.values()) {
            for (Request request : requests) {
                if (request.getNumber() == requestNumber) {
                    return request;
                }
            }
        }
        throw new NoSuchElementException("No open request with number " + requestNumber + ".");
    }

    /**
     * Advanced every open requests offer time and removes the ones that have expired
     * @param ticks The elapsed ticks
     * @return Every request that expired during this advance
     */
    public List<Request> advance(long ticks) {
        List<Request> expired = new ArrayList<>();
        for (List<Request> requests : requestsByProduct.values()) {
            Iterator<Request> it = requests.iterator();
            while (it.hasNext()) {
                Request request = it.next();
                request.advanceOfferTime(ticks);
                if (request.hasExpired()) {
                    expired.add(request);
                    it.remove();
                }
            }
        }
        return expired;
    }

    public record SaveState(
            List<Request.SaveState> requests,
            Map<ItemId, Integer> lostToday
    ) {
        public SaveState {
            requests = List.copyOf(requests);
            lostToday = Map.copyOf(lostToday);
        }
    }

    public SaveState getSaveState() {
        List<Request.SaveState> requestStates = new ArrayList<>();
        for (List<Request> requests : requestsByProduct.values()) {
            for (Request request : requests) {
                requestStates.add(request.getSaveState());
            }
        }
        return new SaveState(requestStates, lostToday);
    }

    public static RequestBoard restore(SaveState saveState) {
        RequestBoard board = new RequestBoard();
        for (Request.SaveState requestState : saveState.requests()) {
            Request request = Request.restore(requestState);
            board.requestsByProduct.computeIfAbsent(request.getProduct(), p -> new ArrayList<>()).add(request);
        }
        board.lostToday.putAll(saveState.lostToday());
        return board;
    }
}
