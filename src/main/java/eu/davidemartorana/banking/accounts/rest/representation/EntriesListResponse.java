package eu.davidemartorana.banking.accounts.rest.representation;

import java.util.*;

public class EntriesListResponse<T> extends LinkedHashMap<String, Object> {

    public static <T> EntriesListResponse<T> of(final String name, final Collection<T> collection) {
        final EntriesListResponse response = new EntriesListResponse();
        response.put(name, collection);
        response.put("total", collection.size());

        return response;
    }

    protected EntriesListResponse(){
        super();
    }

    public Collection<T> getContentList() {
        final Optional<Map.Entry<String, Object>> objectEntry = this.entrySet().stream().filter(entry -> !entry.getKey().equals("total")).findFirst();

        final Map.Entry<String, Object> contentEntry = objectEntry.orElseThrow(() -> new IllegalArgumentException("Content not found."));

        return (Collection<T>) contentEntry.getValue();
    }
}
