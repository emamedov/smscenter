package org.eminmamedov.smscenter.receivers.http;

public interface HttpSerializer {

    HttpRequest deserialize(String request);

    String serialize(HttpResponse response);

}
